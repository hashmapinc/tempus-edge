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
    log.info("Starting track manager")
    
    IofogConnection.connect
    log.info("iofog connection was successful. Listening...")
  }
}
