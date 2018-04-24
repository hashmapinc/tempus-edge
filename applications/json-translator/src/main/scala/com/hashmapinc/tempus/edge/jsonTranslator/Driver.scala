package com.hashmapinc.tempus.edge.jsonTranslator

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
    
    IofogConnection.connect(IofogController)
    log.info("iofog connection was successful. Listening...")
  }
}
