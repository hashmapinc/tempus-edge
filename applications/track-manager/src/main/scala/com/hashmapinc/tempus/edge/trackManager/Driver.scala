package com.hashmapinc.tempus.edge.trackManager

import com.typesafe.scalalogging.Logger

import com.hashmapinc.tempus.edge.iofog.IofogConnection

/**
 * Driver for the overall edge application
 *
 * @author randypitcherii
 */
object Driver {
  private val log = Logger(getClass())

  def main(
    args: Array[String]
  ): Unit = {
    log.info("Starting edge application driver...")
    log.info("CONTAINER_ID = " + IofogConnection.CONTAINER_ID)
    
    log.info("Connecting to iofog...")
    IofogConnection.connect(IofogController)
    log.info("iofog connection was successful. Listening...")
  }
}
