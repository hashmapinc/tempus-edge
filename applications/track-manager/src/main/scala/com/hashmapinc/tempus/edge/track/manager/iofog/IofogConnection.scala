package com.hashmapinc.tempus.edge.track.manager.iofog

import com.iotracks.api.IOFogClient
import com.iotracks.elements.IOMessage
import com.typesafe.scalalogging.Logger

import com.hashmapinc.tempus.edge.proto.{MessageProtocols, ConfigMessageTypes}

/**
 * This object creates and manages the iofog connection.
 */
object IofogConnection {
  private val log = Logger(getClass())

  // get container ID
  private val selfname = System.getenv("SELFNAME")
  val CONTAINER_ID = if(selfname == null) "" else selfname
  val client = new IOFogClient("", 0, CONTAINER_ID) //use default values for client
  
  /**
   * This function connects web socket logic to web socket events
   */
  def connect: Unit = {
    log.info("Creating iofog connection")
    try {
      client.openMessageWebSocket(IofogListener)
    } catch {
      case e: Exception => log.error("IoFog websocket error: " + e.toString)
    }
    try {
      client.openControlWebSocket(IofogListener)
    } catch {
      case e: Exception => log.error("IoFog websocket error: " + e.toString)
    }
  }

  /**
   * This function sends a configuration request to the iofog service
   */
  def requestConfigs: Unit = {
    log.info("Requesting config from iofog...")
    client.fetchContainerConfig(IofogListener)
  }

  /**
   * This function sends a message to the iofog message queue notifying
   * all listeners that there is a new track config.
   */
  def sendNewConfigAlert: Unit = {
    log.info("Notifying track of new TrackConfig...")
    
    // create message content
    val msgProtocol = MessageProtocols.CONFIG.value.toByte
    val msgType = ConfigMessageTypes.UPDATE_ALERT.value.toByte
    val msgContent = Array(msgProtocol, msgType)

    // create message
    val msg = new IOMessage
    msg.setContentData(msgContent)

    // send message
    client.sendMessageToWebSocket(msg)
  }
}