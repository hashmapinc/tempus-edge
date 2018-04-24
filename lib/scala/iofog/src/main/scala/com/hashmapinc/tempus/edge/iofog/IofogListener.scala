package com.hashmapinc.tempus.edge.iofog

import com.iotracks.api.listener.IOFogAPIListener
import com.iotracks.elements.IOMessage
import com.typesafe.scalalogging.Logger

/**
 * This class holds the async logic for handling iofog events
 *
 * @param controller - the IofogController instance with event handling logic
 */
class IofogListener(controller: IofogController) extends IOFogAPIListener {
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
      controller.onMessages(messages)
    } catch {
      case e: Exception => log.error("Error handling iofog messages: " + e)
    }
  }

  /**
   * Method is triggered when Container receives messages from Query request.
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
    // do nothing with messages for now (this may change later)
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
  }

  /**
   * This function handles new configs
   * 
   * @param json - JsonObject holding new configs
   */
  @Override
  def onNewConfig(
    json: javax.json.JsonObject
  ): Unit = {
    controller.onNewConfig(json)
  }

  /**
   * This function handles new config signals
   */
  @Override
  def onNewConfigSignal: Unit = {
    log.info("Received new config signal")
    IofogConnection.requestConfigs(this)
  }
}