package com.hashmapinc.tempus.edge.opcClient.iofog

import collection.JavaConverters._

import com.iotracks.elements.IOMessage
import com.typesafe.scalalogging.Logger

import com.hashmapinc.tempus.edge.proto.{MessageProtocols, ConfigMessageTypes, DataMessageTypes}
import com.hashmapinc.tempus.edge.opcClient.Config

/**
 * This object holds the async logic for handling iofog events
 */
object IofogController {
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
    // handle messages based on message type
    messages.asScala.map((msg) => {
      try {
        // determine if message is a CONFIG message
        val msgContent  = msg.getContentData
        val msgProtocol = msgContent(0) 
        val msgType     = msgContent(1) 

        // handle config messages
        if (msgProtocol == MessageProtocols.CONFIG.value.toByte) {
          // handle config update alerts
          if (msgType == ConfigMessageTypes.UPDATE_ALERT.value.toByte) 
            Config.updateConfigs
          else 
            log.error("Could not handle message with protocol " + msgProtocol + " and type " + msgType)
        
        // handle data messages
        } else if (msgProtocol == MessageProtocols.DATA.value.toByte) {
          // handle opc data messages
          if (msgType == DataMessageTypes.OPC.value.toByte) 
            log.info("Received new OPC message type from iofog.")
            // TODO: process new opc messages in the Opc Controller
          else 
            log.error("Could not handle message with protocol " + msgProtocol + " and type " + msgType)

        // log that an unhandled message arrived.
        } else 
          log.error("Could not handle message with protocol " + msgProtocol + " and type " + msgType)
      } catch {
        case e: Exception => log.error("Error trying to parse iofog message: " + e)
      }
    })
  }
}