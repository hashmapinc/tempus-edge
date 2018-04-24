package com.hashmapinc.tempus.edge.iofog

import com.typesafe.scalalogging.Logger

import com.iotracks.elements.IOMessage

/**
 * This trait holds the async logic for handling iofog events
 */
trait IofogController {
  // must have a logger
  val log: Logger

  // must handle Iofog messages
  def onMessages(messages: java.util.List[IOMessage]): Unit

  /**
   * This function is the default for handling new configs
   * 
   * @param json - JsonObject holding new configs
   */
  def onNewConfig(
    json: javax.json.JsonObject
  ): Unit = {
    log.warn("New config received! This container does not process configuration from iofog")
    log.warn("Iofog sent the following config: " + json)
  }
}