package com.hashmapinc.tempus.edge.track.manager

import com.typesafe.scalalogging.Logger

import com.hashmapinc.tempus.edge.track.manager.iofog.{IofogConnection, IofogController}

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
    
    IofogConnection.connect
    log.info("iofog connection was successful. Listening...")
  }
}
