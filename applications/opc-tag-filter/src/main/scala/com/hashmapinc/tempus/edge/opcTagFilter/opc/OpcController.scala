package com.hashmapinc.tempus.edge.opcTagFilter.opc

import scala.util.Try
import scala.util.matching.Regex
import scala.collection.mutable.Queue

import com.typesafe.scalalogging.Logger
import org.eclipse.milo.opcua.sdk.client.OpcUaClient
import org.eclipse.milo.opcua.stack.core.Identifiers
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseDirection
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseResultMask
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass
import org.eclipse.milo.opcua.stack.core.types.structured.BrowseDescription
import org.eclipse.milo.opcua.stack.core.types.structured.BrowseResult
import org.eclipse.milo.opcua.stack.core.types.structured.ReferenceDescription
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint

import com.hashmapinc.tempus.edge.proto.{MessageProtocols, ConfigMessageTypes, OpcConfig}
import com.hashmapinc.tempus.edge.opcTagFilter.Config
import com.hashmapinc.tempus.edge.opcTagFilter.iofog.IofogConnection

object OpcController {
  private val log = Logger(getClass())

  // global vars
  private val configProtocol = MessageProtocols.CONFIG.value.toByte
  private val subscriptionsSubmission = ConfigMessageTypes.OPC_SUBSCRIPTIONS_SUBMISSION.value.toByte
  val SYSTEM_NODE_REGEX_STRING = ".*\\.\\_.*"
  val SYSTEM_NODE_REGEX = SYSTEM_NODE_REGEX_STRING.r

  /** This function generates a device name for a given nodeId from a series of deviceMaps.
   *
   *  The first matched deviceMap will be used to generate a deviceName from a nodeId.
   *  If no matches exist, the nodeId will be used as the deviceName.
   *
   *  @param nodeId       - string value to check for matches
   *  @param deviceMaps   - OpcConfig.DeviceMaps object for mapping string matches to device names
   *
   *  @return deviceName  - string value of the matching deviceName 
   */
  def getDeviceName(
    nodeId: String,
    deviceMaps: Seq[OpcConfig.DeviceMap]
  ): String = {
    // get all matching device names
    val deviceName = deviceMaps.flatMap(dMap => {
      if (dMap.pattern.r.findFirstIn(nodeId).isDefined) Option(dMap.deviceName) else None
    })

    // return nodeId if no match found, otherwise return first match
    if (deviceName.isEmpty) nodeId else deviceName(0)
  }

  /** This function creates subscriptions from given regexs and opc client
   *
   *  @param whitelist  - Regex that tags must match to be a subscription
   *  @param blacklist  - Regex that tags must not match to be a subscription
   *  @param tagRoot    - NodeId root to begin tag BFS from
   *  @param opcClient  - milo OPC UA client to use for finding tags.
   *
   *  @return subscriptions - OpcConfig.Subscriptions protobuf object with new subscriptions
   */
  def createSubscriptions(
    whitelist:  Regex,
    blacklist:  Regex,
    tagRoot:    NodeId,
    opcClient:  OpcUaClient
  ): OpcConfig.Subscriptions = {
    log.info("Creating subscriptions...")
    opcClient.connect.get // connect 

    @scala.annotation.tailrec
    def recurseTags(
      nodeQueue:      Queue[NodeId],            // queue holding the nodes to visit
      currentMatches: List[OpcConfig.OpcNode]   // list of current matching tags
    ): List[OpcConfig.OpcNode] = {
      if (nodeQueue.isEmpty) currentMatches
      else {
        // browse current node
        val curNode = nodeQueue.dequeue
        val updatedMatches: List[OpcConfig.OpcNode] = Try({
          val browseDesc = new BrowseDescription(
            curNode,
            BrowseDirection.Forward,
            Identifiers.References,
            true,
            uint(NodeClass.Object.getValue() | NodeClass.Variable.getValue()),
            uint(BrowseResultMask.All.getValue())
          )
          val browseResult = opcClient.browse(browseDesc).get

          // add next search layer to the queue
          browseResult.getReferences.map(ref => 
            ref.getNodeId().local().ifPresent(refNodeId => 
              // add non-system nodes to the next search layer in the queue
              if (SYSTEM_NODE_REGEX.findFirstIn(refNodeId.getIdentifier.toString).isEmpty) 
                nodeQueue.enqueue(refNodeId)
            )
          )

          // check for match
          val curID = curNode.getIdentifier.toString
          if (whitelist.findFirstIn(curID).isEmpty) // id does not match
            currentMatches
          else {                                    // id does match
            log.info("adding node with id = " + curID + " to match list.")
            OpcConfig.OpcNode(curNode.getNamespaceIndex.intValue, curID) +: currentMatches
          } 
        }).recover({case e: Exception => {
            log.error("Error while traversing opc heirarchy: " + e)
            currentMatches
        }}).get
        
        // recurse
        recurseTags(nodeQueue, updatedMatches)
      }
    }
    val whitelistedNodes: List[OpcConfig.OpcNode] = recurseTags(Queue(tagRoot), List())
    val matchingNodes = if 
        (blacklist.toString.isEmpty) whitelistedNodes 
      else 
        whitelistedNodes.filter(node => blacklist.findFirstIn(node.id).isEmpty)

    // create subs
    val deviceMaps = Config.trackConfig.get.getOpcConfig.deviceMaps
    OpcConfig.Subscriptions(
      matchingNodes.map(node => 
        OpcConfig.Subscriptions.Subscription(Option(node), getDeviceName(node.id, deviceMaps))
      )
    )
  }

  /** This function drives the subscription updating process
   *
   *  This function uses the local OpcConfig regex's to update the 
   *  OpcConfig.subscriptions list.
   */
  def updateSubscriptions: Unit = {
    log.info("Updating subscriptions...")
    val newSubscriptions: Try[OpcConfig.Subscriptions] = Try({
      // concat regex strings with '|' separator and create single regex for each tagFilter
      val tagFilters = Config.trackConfig.get.getOpcConfig.getTagFilters
      val whitelistRegex = tagFilters.whitelist.mkString("|").r
      val blacklistRegex = (SYSTEM_NODE_REGEX_STRING +: tagFilters.blacklist).mkString("|").r // add regex to filter out any system tags

      // create tag root nodeId
      val root = Try({
        val rootNode = Config.trackConfig.get.getOpcConfig.tagRoot
        if (rootNode.isEmpty) Identifiers.RootFolder else new NodeId(rootNode.get.namespace, rootNode.get.id)
      }).recover({ case e: Exception => {
        log.error("Could not create tag root nodeId. Error: " + e)
        Identifiers.RootFolder // use opc root as default
      }}).get

      OpcConnection.synchronized {
        try 
          createSubscriptions(whitelistRegex, blacklistRegex, root, OpcConnection.client.get) 
        catch {
          case e: Exception => createSubscriptions(whitelistRegex, blacklistRegex, root, OpcConnection.client.get)
        }
      }
    })

    if (newSubscriptions.isSuccess && !newSubscriptions.get.nodes.isEmpty) {
      log.info("Successfully created {} new subscription(s). Sending now...", newSubscriptions.get.nodes.length)
      IofogConnection.sendWSMessage(configProtocol +: subscriptionsSubmission +: newSubscriptions.get.toByteArray)
    } else if (newSubscriptions.isFailure)
      newSubscriptions.recover({ case e: Exception => log.error("Could not update subscriptions: " + e)})
    else
      log.info("No new subscriptions found.")
  }
}