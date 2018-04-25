package com.hashmapinc.tempus.edge.trackManager

import java.nio.file.{Files, Paths}
import scala.util.Try

import com.typesafe.scalalogging.Logger
import com.google.protobuf.InvalidProtocolBufferException

import com.hashmapinc.tempus.edge.proto._

/**
 * This object holds the logic for interacting with configs
 */
object Config {
  private val log = Logger(getClass())
  val PATH_TO_TRACK_CONFIG = "/mnt/config/config.pb"

  /**
   * This function loads and returns the TrackConfig on disc if it exists
   *
   * @param pathToConfig - String value holding path to the config.pb file
   *
   * @return trackConfig - TrackConfig loaded from disc or None
   */
  def loadTrackConfig(
    pathToConfig: String
  ): TrackConfig = {
    log.info("Loading track config...")
    // create Path
    val configPath = Paths.get(pathToConfig)

    // load if config exists, return empty config if not
    try {
      if (!Files.exists(configPath)) TrackConfig()
      else TrackConfig.parseFrom(Files.readAllBytes(configPath))
    } catch {
      case e: InvalidProtocolBufferException => {
        log.error("Could not parse saved config at " + pathToConfig)
        log.error("Empty track config will be used instead...")
        TrackConfig()
      }
    }
  }

  /**
   * This function saves trackConfig to disc
   *
   * @param trackConfig   - TrackConfig protobuf object containing new configs
   * @param pathToConfig  - String value holding path to the config.pb file
   */
  def saveTrackConfig(
    trackConfig: TrackConfig,
    pathToConfig: String
  ): Unit = {
    log.info("Saving track config...")
    // create Path
    val configPath = Paths.get(pathToConfig)

    // write track config
    Files.write(configPath, trackConfig.toByteArray) // default is to overwrite if exists
  }

  /**
   * This function merges new configs onto an old config.
   *
   * If a newTrackConfig exists, it is used. If not, for every other new*Config
   * passed to this method, that config component of the old config is overwritten.
   *
   * @param oldTrackConfig    - TrackConfig protobuf object containing old configs
   *
   * @param newTrackConfig    - TrackConfig protobuf object containing new configs
   * @param newTrackMetadata  - TrackMetadata protobuf object containing new track metadata
   * @param newMqttConfig     - MqttConfig protobuf object containing new mqtt configs
   * @param newOpcConfig      - OpcConfig protobuf object containing new opc configs
   */
  def mergeConfigs(
    oldTrackConfig:   TrackConfig,
  )(
    newTrackConfig:   Option[TrackConfig],
    newTrackMetadata: Option[TrackMetadata],
    newMqttConfig:    Option[MqttConfig],
    newOpcConfig:     Option[OpcConfig]
  ): TrackConfig = {
    log.info("Merging configs...")

    // if there's a newTrackConfig, return it
    if (!newTrackConfig.isEmpty) newTrackConfig.get
    else { // no newTrackConfig; merge remaining configs
      val mergedTrackMetadata = if(!newTrackMetadata.isEmpty) newTrackMetadata else oldTrackConfig.trackMetadata
      val mergedMqttConfig = if(!newMqttConfig.isEmpty) newMqttConfig else oldTrackConfig.mqttConfig
      val mergedOpcConfig = if(!newOpcConfig.isEmpty) newOpcConfig else oldTrackConfig.opcConfig
      TrackConfig(mergedTrackMetadata, mergedMqttConfig, mergedOpcConfig)
    }
  }
}