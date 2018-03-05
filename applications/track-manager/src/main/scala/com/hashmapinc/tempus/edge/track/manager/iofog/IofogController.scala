package com.hashmapinc.tempus.edge.track.manager.iofog

import com.iotracks.elements.IOMessage
import com.typesafe.scalalogging.Logger

import com.hashmapinc.tempus.edge.proto.{MessageProtocols, ConfigMessageTypes}
import com.hashmapinc.tempus.edge.track.proto.TrackConfig

/**
 * This object holds the logic for handling iofog events
 */
object IofogController {
  private val log = Logger(getClass())

  /**
   * This function loads and returns the TrackConfig on disc if it exists
   *
   * @return trackConfig - TrackConfig loaded from disc or None
   */
  def loadTrackConfig(): TrackConfig = {
    log.info("Loading track config...")
    // TODO: Do this for real
    TrackConfig()
  }

  /**
   * This function saves trackConfig to disc
   *
   * @param trackConfig - TrackConfig protobuf object containing new configs
   */
  def saveTrackConfig(
    trackConfig: TrackConfig
  ): Unit = {
    log.info("Saving new track config.")
    // TODO: Do this for real
  }

  /**
   * This function converts a json config into a proper TrackConfig protobuf 
   *
   * @param json - JsonObject holding the new track config 
   * @return newConfig - TrackConfig protobuf object with new track config
   */
  def parseJsonConfig(
    json: javax.json.JsonObject
  ): TrackConfig = {
    log.info("Parsing new json configs as TrackConfig protobuf")

    // parse json into TrackConfig protobuf object
    val newConfig = TrackConfig() // TODO: do this for real
    log.info("New TrackConfig: " + newConfig.toString)

    newConfig
  }

  /**
   * This function merges a newConfig into an oldConfig. If newConfig does not
   * define a field that oldConfig does, the field will be unchanged. If 
   * newConfig does define a field, its value will be used in mergedConfig
   *
   * @param oldConfig - TrackConfig protobuf object holding the starting config to merge into
   * @param newConfig - TrackConfig protobuf object holding the new configs to merge
   *
   * @return mergedConfig - TrackConfig protobuf object holding the merged track configs
   */
  def mergeConfigs(
    oldConfig: TrackConfig,
    newConfig: TrackConfig
  ): TrackConfig = {
    log.info("Beginning config merge process...")

    // TODO: do this for real
    TrackConfig()
  }

  /**
   * This function processes a new config from iofog
   *
   * @param json - JsonObject holding the new track config
   */
  def onNewIofogConfig(
    json: javax.json.JsonObject
  ): Unit = {
    // parse config
    val newConfig: TrackConfig = parseJsonConfig(json)

    // merge and save configs
    this.synchronized {
      val mergedConfig = mergeConfigs(loadTrackConfig, newConfig)
      saveTrackConfig(mergedConfig)
    }

    // alert track of new configs
    IofogConnection.sendNewConfigAlert
  }
}