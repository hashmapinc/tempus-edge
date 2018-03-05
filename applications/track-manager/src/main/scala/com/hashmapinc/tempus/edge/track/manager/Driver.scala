package com.hashmapinc.tempus.edge.track.manager

import com.typesafe.scalalogging.Logger

import com.hashmapinc.tempus.edge.track.manager.iofog.{IofogConnection, IofogController}

/**
 * Driver for the track manager
 *
 * @author randypitcherii
 */
object Driver {
  private val log = Logger(getClass())

  def main(
    args: Array[String]
  ): Unit = {
    log.info("Starting Manager")

    log.info("SELFNAME = " + System.getenv("SELFNAME"))
    log.info("CONTAINERID = " + Config.CONTAINER_ID)
    
    log.info("Connecting to iofog...")
    IofogConnection.connect
    log.info("iofog connection was successful. Listening...")
  }
}
