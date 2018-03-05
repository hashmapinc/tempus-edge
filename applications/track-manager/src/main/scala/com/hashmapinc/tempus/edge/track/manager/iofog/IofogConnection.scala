package com.hashmapinc.tempus.edge.track.manager.iofog

import com.iotracks.api.IOFogClient
import com.typesafe.scalalogging.Logger

import com.hashmapinc.tempus.edge.track.manager.Config

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
    //get initial config
    requestConfigs

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
}