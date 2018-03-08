package com.hashmapinc.tempus.edge.track.manager.iofog

import javax.json.JsonObject

import com.iotracks.api.listener.IOFogAPIListener
import com.iotracks.elements.IOMessage
import com.typesafe.scalalogging.Logger

import com.hashmapinc.tempus.edge.proto.{MessageProtocols, ConfigMessageTypes}
import com.hashmapinc.tempus.edge.track.proto.TrackConfig

/**
 * This object dispatches IoFog events to the IofogController
 */
object IofogListener extends IOFogAPIListener {
  private val log = Logger(getClass())

  /**
   * Handles new messages
   *
   * @param messages - list of received messages
   */
  @Override
  def onMessages(
    messages: java.util.List[IOMessage]
  ): Unit = {
    log.info("Received " + messages.size.toString + " message(s)")
    try {
      // send messages to IofogController for dispatch
      IofogController.onMessages(messages)
    } catch {
      case e: Exception => log.error("Error handling iofog messages: " + e)
    }
  }

  /**
   * Method is triggered when container receives messages from query request.
   *
   * @param timeframestart - time-frame start date of returned messages
   * @param timeframeend - time-frame end date of returned messages
   * @param messages - list of received messages
   */
  @Override
  def onMessagesQuery(
    timeframestart: Long, 
    timeframeend: Long, 
    messages: java.util.List[IOMessage]
  ): Unit = {
    log.info("Received " + messages.size.toString + " message(s) from query request")
    // do nothing
  }

  /**
   * This function logs any errors thrown by the parent class
   * 
   * @param throwable - Throwable describing the error that occurred
   */
  @Override
  def onError(
    throwable: Throwable
  ): Unit = {
    log.error(throwable.toString)
    // do nothing
  }

  /**
   * Method is triggered when Container receives BAD_REQUEST response from ioFog.
   *
   * @param error - error messages
   */
  @Override
  def onBadRequest(
    error: String
  ): Unit = {
    log.error("Bad ioFog Request: " + error)
    // do nothing
  }

  /**
   * This function handles onMessageReceipt events.
   * Method is triggered when Container receives message's receipt.
   *
   * @param messageId - generated id of sent message
   * @param timestamp - timestamp generated when message was received by ioFog
   */
  @Override
  def onMessageReceipt(
    messageId: String,
    timestamp: Long
  ): Unit = {
    log.info("Received message receipt for " + messageId)
    // do nothing
  }

  /**
   * This function handles new configs
   * 
   * @param json - JsonObject holding new configs
   */
  @Override
  def onNewConfig(
    json: JsonObject
  ): Unit = {
    log.info("Received new config from iofog:" + json.toString)

    // merge new config with existing config, save configs, notify track of new config
    try {
      IofogController.onNewIofogConfig(json)
    } catch {
      case e: Exception => log.error("Error while processing new ioFog config: " + e)
    }
  }

  /**
   * This function handles new config signals.
   */
  @Override
  def onNewConfigSignal: Unit = {
    log.info("Received new config signal")

    // send new configs to the onNewConfig handler above
    try {
      IofogConnection.requestConfigs
    } catch {
      case e: Exception => log.error("Error while handling new ioFog config signal: " + e)
    }
  }
}