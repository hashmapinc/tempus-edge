package com.hashmapinc.tempus.edge.jsonTranslator

import com.typesafe.scalalogging.Logger

import com.hashmapinc.tempus.edge.jsonTranslator.iofog.{IofogConnection, IofogController}

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
