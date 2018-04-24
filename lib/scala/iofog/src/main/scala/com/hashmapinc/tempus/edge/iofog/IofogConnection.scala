package com.hashmapinc.tempus.edge.iofog

import com.iotracks.api.IOFogClient
import com.iotracks.elements.IOMessage
import com.typesafe.scalalogging.Logger

/**
 * This object creates and manages the iofog connection.
 */
object IofogConnection {
  private val log = Logger(getClass())

  private val selfname = System.getenv("SELFNAME")
  val CONTAINER_ID = if(selfname == null) "" else selfname

  val client = new IOFogClient("", 0, CONTAINER_ID) // use default values for client
  
  /**
   * This function connects the web socket logic to the web socket events
   *
   * @param controller - IofogController object 
   */
  def connect(
    controller: IofogController
  ): Unit = {
    log.info("Creating iofog listener...")
    val listener = new IofogListener(controller)    

    log.info("Creating iofog connection...")
    try {
      client.openMessageWebSocket(listener)
    } catch {
      case e: Exception => log.error("IoFog websocket error: " + e.toString)
    }
    try {
      client.openControlWebSocket(listener)
    } catch {
      case e: Exception => log.error("IoFog websocket error: " + e.toString)
    }
  }

  /**
   * This function sends a configuration request to the iofog service
   *
   * @param listener - IofogListener object with logic for handling the new configs
   */
  def requestConfigs(
    listener: IofogListener
  ): Unit = {
    log.info("Requesting config from iofog...")
    client.fetchContainerConfig(listener)
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