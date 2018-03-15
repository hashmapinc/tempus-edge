package com.hashmapinc.tempus.edge.opcTagFilter.iofog

import com.iotracks.api.IOFogClient
import com.iotracks.elements.IOMessage
import com.typesafe.scalalogging.Logger

import com.hashmapinc.tempus.edge.opcTagFilter.Config

/**
 * This object creates and manages the iofog connection.
 */
object IofogConnection {
  private val log = Logger(getClass())

  val client = new IOFogClient("", 0, Config.CONTAINER_ID) //use default values for client

  /**
   * This function sends a configuration request to the iofog service
   */
  def requestConfigs: Unit = {
    log.info("Requesting config from iofog...")
    client.fetchContainerConfig(IofogListener)
  }
  
  /**
   * This function connects the web socket logic to the web socket events
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
   * This function sends a message to the iofog message stream.
   *
   * @param msgContent - Byte array holding the msgContent to be sent
   */
  def sendWSMessage(
    msgContent: Array[Byte]
  ): Unit = {
    log.info("Writing new message to iofog...")

    // create message
    val msg = new IOMessage
    msg.setContentData(msgContent)

    // send message
    client.sendMessageToWebSocket(msg)
  }
}