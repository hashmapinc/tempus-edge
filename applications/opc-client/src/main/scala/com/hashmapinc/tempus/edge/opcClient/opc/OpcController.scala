package com.hashmapinc.tempus.edge.opcClient.opc

import collection.JavaConverters._
import scala.util.Try
import java.util.function.BiConsumer

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

import com.hashmapinc.tempus.edge.proto.TrackConfig
import com.hashmapinc.tempus.edge.opcClient.Config

/** 
 *  This object holds the logic for handlign OPC events
 */
object OpcController {
  private val log = Logger(getClass())

  // anonymous function used to wire onNewSubscribedValue handler to new subscription value events in milo
  val onItemCreated: BiConsumer[UaMonitoredItem, Integer] = (item: UaMonitoredItem, id: Integer) => {
    item.setValueConsumer(onNewSubscribedValue)
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
    println("subscription value received: item={}, value={}", item.getReadValueId().getNodeId(), value.getValue())
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
    val subscription = opcClient.getSubscriptionManager.createSubscription(1000.0).get()
    val subs = subscription.createMonitoredItems(
      TimestampsToReturn.Both,
      subRequests.asJava,
      onItemCreated
    ).get

    // report failed subs
    subs.asScala.filter(_.getStatusCode.isBad).foreach(badSub =>
      log.error("Could not subscribe the following subRequest: " + badSub)
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
      val subRequests: List[MonitoredItemCreateRequest] = getSubscriptionRequests(trackConfig)

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
      case e: Exception => log.error("Could not update subscriptions. Error: " + e)
    })
  }
}