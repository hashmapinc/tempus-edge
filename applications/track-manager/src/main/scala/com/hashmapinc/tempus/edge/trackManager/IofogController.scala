package com.hashmapinc.tempus.edge.trackManager

import collection.JavaConverters._
import java.nio.file.{Files, Paths}
import javax.json.JsonObject
import scala.util.Try

import com.iotracks.elements.IOMessage
import com.typesafe.scalalogging.Logger
import com.google.protobuf.InvalidProtocolBufferException
import scalapb.json4s.JsonFormat

import com.hashmapinc.tempus.edge.iofog.IofogConnection
import com.hashmapinc.tempus.edge.proto._

/**
 * This object holds the logic for handling iofog events
 */
object IofogController extends com.hashmapinc.tempus.edge.iofog.IofogController {
  val log = Logger(getClass())

  /**
   * This function sends a message to the iofog message queue notifying
   * all listeners that there is a new track config.
   */
  def sendNewConfigAlert: Unit = {
    log.info("Notifying track of new TrackConfig...")
    
    // create message content
    val msgProtocol = MessageProtocols.CONFIG.value.toByte
    val msgType = ConfigMessageTypes.UPDATE_ALERT.value.toByte
    val payload = Array(msgProtocol, msgType)

    // send message
    IofogConnection.sendWSMessage(payload)
  }

  /**
   * This function processes a new config from iofog
   *
   * @param json - JsonObject holding the new track config
   */
  def processNewIofogConfig(
    json: javax.json.JsonObject
  ): Unit = {
    // parse json into tuple of configs
    val parsedConfigs = Parsers.parseJsonConfig(json)

    // merge and save configs
    this.synchronized {
      val oldConfig = Config.loadTrackConfig(Config.PATH_TO_TRACK_CONFIG)
      val mergedConfig = Config.mergeConfigs(oldConfig) _ tupled parsedConfigs // this is scala black magic for de-tupling and calling mergeConfigs
      Config.saveTrackConfig(mergedConfig, Config.PATH_TO_TRACK_CONFIG)
    }

    // alert track of new configs
    sendNewConfigAlert
  }

  /**
   * This function processes new iofog messages from the IofogListener
   *
   * @param messages - list of IOMessages from IofogListener
   */
  override def onMessages(
    messages: java.util.List[IOMessage]
  ): Unit = {
    // dispatch messages based on message types
    messages.asScala.map((msg) => {
      try {
        // determine if message is a CONFIG message
        val msgContent = msg.getContentData
        val isConfigMsg = (msgContent(0) == MessageProtocols.CONFIG.value.toByte)

        // handle config messages
        if (isConfigMsg) {
          // parse configs into configs tuple
          val newConfigs = Parsers.parseConfigMessageContent(msgContent)

          // update configs
          this.synchronized {
            val oldConfig = Config.loadTrackConfig(Config.PATH_TO_TRACK_CONFIG)
            val mergedConfig = Config.mergeConfigs(oldConfig) _ tupled newConfigs
            Config.saveTrackConfig(mergedConfig, Config.PATH_TO_TRACK_CONFIG)
          }

          // alert track of new configs
          sendNewConfigAlert
        } 
      } catch {
        case e: Exception => log.error("Error trying to parse iofog message: " + e)
      }
    })
  }

  /**
   * This function handles new configs
   * 
   * @param json - JsonObject holding new configs
   */
  override def onNewConfig(
    json: JsonObject
  ): Unit = {
    log.info("Received new config from iofog:" + json.toString)

    // merge new config with existing config, save configs, notify track of new config
    try {
      processNewIofogConfig(json)
    } catch {
      case e: Exception => log.error("Error while processing new ioFog config: " + e)
    }
  }
}