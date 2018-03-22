package com.hashmapinc.tempus.edge.opcTagFilter

import com.typesafe.scalalogging.Logger

import com.hashmapinc.tempus.edge.opcTagFilter.iofog.IofogConnection
import com.hashmapinc.tempus.edge.opcTagFilter.opc.{OpcConnection, OpcController}

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

    log.info("SELFNAME = " + System.getenv("SELFNAME"))
    log.info("CONTAINERID = " + Config.CONTAINER_ID)

    Config.updateConfigs                  // load initial configs.
    if (Config.trackConfig.isDefined) {
      OpcConnection.updateClient          // init client connection.
      OpcController.updateSubscriptions   // look for subscriptions.
    }
      
    log.info("Connecting to iofog...")
    IofogConnection.connect
    log.info("iofog connection was successful. Listening...")
  }
}
