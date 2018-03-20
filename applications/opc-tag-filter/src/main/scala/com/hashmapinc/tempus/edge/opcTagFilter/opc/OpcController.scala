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

  /** This function recurses through the OPC server and finds matching tags
   *
   *  @param whitelist - Regex that tags must match to be a subscription
   *  @param opcClient  - milo OPC UA client to use for finding tags.
   *  @param currentNode  - NodeId currently being evaluated for matches
   *  @param currentMatches  - list of matches recursively found so far
   *
   *  @return matches - string array containing matching tags
   */
  

  /** This function creates subscriptions from given regexs and opc client
   *
   *  @param whitelist - Regex that tags must match to be a subscription
   *  @param blacklist - Regex that tags must not match to be a subscription
   *  @param opcClient  - milo OPC UA client to use for finding tags.
   *
   *  @return subscriptions - OpcConfig.Subscriptions protobuf object with new subscriptions
   */
  def createSubscriptions(
    whitelist: Regex,
    blacklist: Regex,
    opcClient: OpcUaClient
  ): OpcConfig.Subscriptions = {
    log.info("Creating subscriptions...")

    @scala.annotation.tailrec
    def recurseTags(
      nodeQueue: Queue[NodeId], // queue holding the nodes to visit
      currentMatches: List[String] // list of current matching tags
    ): List[String] = {
      if (nodeQueue.isEmpty) currentMatches
      else {
        // browse current node
        val curNode = nodeQueue.dequeue
        val updatedMatches: List[String] = Try({
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
              nodeQueue.enqueue(refNodeId)
            )
          )

          // check for match
          currentMatches // TODO: append tag if curNode is a match
        }).getOrElse(currentMatches)
        
        // recurse
        recurseTags(nodeQueue, updatedMatches)
      }
    }
    val matches: List[String] = recurseTags(Queue(Identifiers.RootFolder), List())

    // TODO: actually populate this with a toDevice method
    OpcConfig.Subscriptions(
      matches.map(tag => OpcConfig.Subscriptions.Subscription(tag, tag)) // should be (tag, toDevice(tag))
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
      val blacklistRegex = tagFilters.blacklist.mkString("|").r
      OpcConnection.synchronized {
        createSubscriptions(whitelistRegex, blacklistRegex, OpcConnection.client.get)
      }
    })

    if (newSubscriptions.isSuccess) 
      IofogConnection.sendWSMessage(configProtocol +: subscriptionsSubmission +: newSubscriptions.get.toByteArray)
    else 
      newSubscriptions.recover({ case e: Exception => log.error("Could not update subscriptions: " + e)})
  }
}