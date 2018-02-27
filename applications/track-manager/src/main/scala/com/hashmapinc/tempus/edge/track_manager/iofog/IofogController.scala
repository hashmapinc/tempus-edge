package com.hashmapinc.tempus.edge.track_manager.iofog

import javax.json.JsonObject
import play.api.libs.json.Json
import com.iotracks.api.listener.IOFogAPIListener
import com.iotracks.elements.IOMessage
import com.typesafe.scalalogging.Logger

import com.hashmapinc.tempus.edge.track_manager.{IofogConfig, Config}

/**
 * This object holds the async logic for handling iofog events
 */
object IofogController extends IOFogAPIListener {
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
    // do nothing with messages for now (this may change later)
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
    json: JsonObject
  ): Unit = {
    log.info("Parsing new config from iofog")
    try {
      val newConfig = Json.parse(json.toString).as[IofogConfig]
      Config.update(newConfig)
    } catch {
      case e: Exception => log.error("onNewConfig error: " + e.toString)
    }
  }

  /**
   * This function handles new config signals
   */
  @Override
  def onNewConfigSignal: Unit = {
    log.info("Received new config signal")
    IofogConnection.requestConfigs
  }
}