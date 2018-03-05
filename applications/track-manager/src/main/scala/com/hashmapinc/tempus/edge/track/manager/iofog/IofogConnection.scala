package com.hashmapinc.tempus.edge.track.manager.iofog

import com.iotracks.api.IOFogClient
import com.typesafe.scalalogging.Logger

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
  }
}