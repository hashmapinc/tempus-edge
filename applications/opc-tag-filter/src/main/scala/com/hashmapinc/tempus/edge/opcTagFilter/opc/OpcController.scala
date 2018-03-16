package com.hashmapinc.tempus.edge.opcTagFilter.opc

import scala.util.Try
import scala.util.matching.Regex

import com.typesafe.scalalogging.Logger
import org.eclipse.milo.opcua.sdk.client.OpcUaClient

import com.hashmapinc.tempus.edge.proto.{MessageProtocols, ConfigMessageTypes, OpcConfig}
import com.hashmapinc.tempus.edge.opcTagFilter.Config
import com.hashmapinc.tempus.edge.opcTagFilter.iofog.IofogConnection

object OpcController {
  private val log = Logger(getClass())

  // global vars
  private val configProtocol = MessageProtocols.CONFIG.value.toByte
  private val subscriptionsSubmission = ConfigMessageTypes.OPC_SUBSCRIPTIONS_SUBMISSION.value.toByte

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
    OpcConfig.Subscriptions(List(
      OpcConfig.Subscriptions.Subscription("tag0", "deviceA"),
      OpcConfig.Subscriptions.Subscription("tag1", "deviceA"),
      OpcConfig.Subscriptions.Subscription("tag2", "deviceB"),
      OpcConfig.Subscriptions.Subscription("tag3", "deviceC")
    ))
  }

  /** This function drives the subscription updating process
   *
   *  This function uses the local OpcConfig regex's 
   *  to update the OpcConfig.subscriptions list.
   */
  def updateSubscriptions: Unit = {
    log.info("Updating subscriptions...")
    val newSubscriptions: Try[OpcConfig.Subscriptions] = Try({
      // concat regex strings with '|' separator and create single regex for each tagFilter
      val tagFilters = Config.trackConfig.get.getOpcConfig.getTagFilters
      val whitelistRegex = tagFilters.whitelist.mkString("|").r
      val blacklistRegex = tagFilters.blacklist.mkString("|").r
      createSubscriptions(whitelistRegex, blacklistRegex, OpcConnection.client.get)
    })

    if (newSubscriptions.isSuccess) 
      IofogConnection.sendWSMessage(configProtocol +: subscriptionsSubmission +: newSubscriptions.get.toByteArray)
    else 
      newSubscriptions.recover({ case e: Exception => log.error("Could not update subscriptions: " + e)})
  }
}