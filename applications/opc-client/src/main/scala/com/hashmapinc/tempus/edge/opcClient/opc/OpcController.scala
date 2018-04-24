package com.hashmapinc.tempus.edge.opcClient.opc

import collection.JavaConverters._
import scala.util.Try
import java.util.function.BiConsumer
import java.util.concurrent.ExecutionException

import com.typesafe.scalalogging.Logger
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem
import org.eclipse.milo.opcua.sdk.client.OpcUaClient
import org.eclipse.milo.opcua.stack.core.AttributeId
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId

import com.hashmapinc.tempus.edge.proto.{TrackConfig, OpcConfig, OpcMessage, MessageProtocols, DataMessageTypes}
import com.hashmapinc.tempus.edge.iofog.IofogConnection
import com.hashmapinc.tempus.edge.opcClient.Config

/** 
 *  This object holds the logic for handling OPC events
 */
object OpcController {
  private val log = Logger(getClass())

  // Retry if this err msg is encountered
  val RECOVERABE_ERR_MSG = "UaServiceFaultException: status=Bad_SessionIdInvalid, message=The session id is not valid."

  // anonymous function used to wire onNewSubscribedValue handler to new subscription value events in milo
  val onItemCreated: BiConsumer[UaMonitoredItem, Integer] = (item: UaMonitoredItem, id: Integer) => {
    item.setValueConsumer(onNewSubscribedValue)
  }

  /** This function parses a new OPC reading into an OpcMessage protobuf object
   *
   *  This function is not fault tolerant and should be error checked by the caller
   *
   *  To support multiple data types from opc, this function is on the longer side.
   *  The logic is largely just ensuring the right value data type is serialized.
   *
   *  @param item     - UaMonitoredItem with a new value to handle
   *  @param value    - DataValue that changed and must be handled
   *
   *  @return opcMsg  - OpcMessage protobuf object parsed from item and value
   */
  def serializeOpcMessage(
    item: UaMonitoredItem, 
    value: DataValue
  ): OpcMessage = {
    // extract node information
    val node        = item.getReadValueId.getNodeId
    val namespace   = node.getNamespaceIndex.intValue
    val identifier  = node.getIdentifier.toString
    val deviceName  = Try({
      val subEntry = Config.trackConfig.get.getOpcConfig.getSubs.nodes.filter(_.getNode.id == identifier)
      if (subEntry.isEmpty) identifier else subEntry(0).deviceName
    }).getOrElse(identifier)

    // extract time information
    val srcDatetime     = value.getSourceTime.getJavaDate.toString    
    val serverDatetime  = value.getServerTime.getJavaDate.toString

    // create OpcNode protobuf object
    val protoNode = OpcConfig.OpcNode().withNamespace(namespace).withId(identifier)

    // extract value and create OpcMessage
    val opcMsg = (OpcMessage()
      .withNode(protoNode)
      .withDeviceName(deviceName)
      .withSourceDatetime(srcDatetime)
      .withServerDatetime(serverDatetime))

    // extract value and return opcMessage with proper value
    val valueType = value.getValue.getValue.getClass.toString
    valueType match {
      case "class java.lang.Integer" => opcMsg.withValueInt32(value.getValue.getValue.asInstanceOf[java.lang.Integer])
      case "class java.lang.Long" =>    opcMsg.withValueInt64(value.getValue.getValue.asInstanceOf[java.lang.Long])
      case "class java.lang.Float" =>   opcMsg.withValueFloat(value.getValue.getValue.asInstanceOf[java.lang.Float])
      case "class java.lang.Double" =>  opcMsg.withValueDouble(value.getValue.getValue.asInstanceOf[java.lang.Double])
      case "class java.lang.Boolean" => opcMsg.withValueBoolean(value.getValue.getValue.asInstanceOf[java.lang.Boolean])
      case _ =>                         opcMsg.withValueString(value.getValue.getValue.toString) // put everything else into string format
    }
  }

  /** This function handles new readings from subscribed OPC entities
   *
   *  This function reads new values, serializes to protobuf, and sends the new
   *  reading to iofog.
   *
   *  Because of limitations in Scala/Java interop, this function has to be
   *  defined without params in the def statement. This allows the compiler to treat
   *  this as a BiConsumer; this is necessary to work with Milo.
   *
   *  @param item  - UaMonitoredItem with a new value to handle
   *  @param value - DataValue that changed and must be handled
   */
  def onNewSubscribedValue: BiConsumer[UaMonitoredItem, DataValue] = (item: UaMonitoredItem, value: DataValue) => {
    log.info("Received new subscription value...")

    Try({
      // serialize item and value into protobuf object
      val opcMsg = serializeOpcMessage(item, value)
      log.info("New subscription value parsed to OPC Message: " + opcMsg.toString)
      
      // construct iofog message payload
      val msgProtocol = MessageProtocols.DATA.value.toByte
      val msgType     = DataMessageTypes.OPC.value.toByte
      val payload     = msgProtocol +: msgType +: opcMsg.toByteArray

      // send the message
      IofogConnection.sendWSMessage(payload)

    }).recover({
      case e: Exception => {
        log.error("Error handling message from node: " + item.getReadValueId.getNodeId)
        log.error("Error: " + e)
      }
    })
  }

  /** This function constructs a milo-friendly list of sub requests
   *
   *  This funcion is not error safe and should be error checked by caller.
   *
   *  @param trackConfig  - TrackConfig protobuf object with OpcConfig to
   *                        use for creating the subscription requests
   *  @return subRequests - List[MonitoredItemCreateRequest] with latest sub requests
   */
  def getSubscriptionRequests(
    trackConfig:  TrackConfig
  ): List[MonitoredItemCreateRequest] = {
    log.info("Creating subscription requests...")
    
    val opcConf = trackConfig.getOpcConfig

    // create sub params
    val queueSize = uint(1)
    val updateFrequency = if (opcConf.subsUpdateFreq == 0) Config.DEFAULT_UPDATE_FREQ else opcConf.subsUpdateFreq
    
    // create opcNodes to subscribe to
    val opcNodes: List[NodeId] = opcConf.getSubs.nodes.flatMap(entry => {
      Try({
        List(new NodeId(entry.getNode.namespace, entry.getNode.id))
      }).recover({case e: Exception => 
        log.error("Could not create node to subscribe to: " + e)
        List(): List[NodeId]
      }).get
    }).toList

    // create subscription requests from nodes
    opcNodes.zipWithIndex.flatMap({case(node, id) => 
      Try({
        val clientHandle = uint(id)
        val readValueId = new ReadValueId(
          node, 
          AttributeId.Value.uid(), 
          null, 
          QualifiedName.NULL_VALUE
        )
        val parameters = new MonitoringParameters(
          clientHandle, 
          updateFrequency, 
          null, 
          queueSize, 
          true
        )
        List(new MonitoredItemCreateRequest(
          readValueId, 
          MonitoringMode.Reporting, 
          parameters
        ))
      }).recover({case e: Exception => 
        log.error("Could not subscribe to node: " + e)
        List(): List[MonitoredItemCreateRequest]
      }).get
    })
  }

  /** This function unsubscribes from all current subscriptions in opcClient
   *
   *  This funcion is not error safe and should be error checked by caller.
   *
   *  @param opcClient  - OpcUaClient containing the subscriptions to be removed
   */
  def unsubscribeAll(
    opcClient:  OpcUaClient
  ): Unit = {
    log.info("Unsubscribing from all subscriptions...")
    // connect
    opcClient.connect.get

    // unsubscribe
    opcClient.getSubscriptionManager.clearSubscriptions
  }

  /** This function uses opcClient to subscribe to all subRequests
   *
   *  This funcion is not fully error safe and should be error checked by caller.
   *
   *  @param subRequests    - List[MonitoredItemCreateRequest] to subscribe to
   *  @param opcClient      - OpcUaClient to subscribe with
   */
  def subscribe(
    subRequests:    List[MonitoredItemCreateRequest],
    opcClient:      OpcUaClient
  ): Unit = {
    log.info("Subscribing to new OPC entities...")
    // connect
    opcClient.connect.get

    // subscribe
    val subscription = opcClient.getSubscriptionManager.createSubscription(1000.0).get
    val subs = subscription.createMonitoredItems(
      TimestampsToReturn.Both,
      subRequests.asJava,
      onItemCreated
    ).get

    // report failed subs
    subs.asScala.filter(_.getStatusCode.isBad).foreach(badSub =>
      log.error("Could not subscribe the following node: " + badSub.getReadValueId.getNodeId)
    )
  }

  /** This function drives the subscription updating process
   *
   *  This function uses the local OpcConfig Subscriptions to subscribe
   *  to opc values. This function modify active subscriptions to be
   *  the same subscriptions as those present in the local trackConfig.
   */
  def updateSubscriptions: Unit = {
    log.info("Updating subscriptions...")
    Try({
      // gather new subscriptions
      val trackConfig = Config.trackConfig.get
      val subRequests = getSubscriptionRequests(trackConfig)

      OpcConnection synchronized {
        // synchronously connect client 
        val client = OpcConnection.client.get
        client.connect.get

        // remove current subs
        unsubscribeAll(client)

        // add new subs
        subscribe(subRequests, client)
      }
    }).recover({
      case e: ExecutionException => {
        if (e.getMessage == RECOVERABE_ERR_MSG)
          updateSubscriptions // retry. The session should be good now; it refreshes on failure
        else
         log.error("Could not update subscrptions. Error: " + e)
      }
      case e: Exception => log.error("Could not update subscriptions. Error: " + e)
    })
  }
}