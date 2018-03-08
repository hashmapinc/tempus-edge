package com.hashmapinc.tempus.edge.track.manager.iofog

import collection.JavaConverters._
import java.nio.file.{Files, Paths}
import scala.util.Try

import com.iotracks.elements.IOMessage
import com.typesafe.scalalogging.Logger
import com.google.protobuf.InvalidProtocolBufferException
import scalapb.json4s.JsonFormat

import com.hashmapinc.tempus.edge.proto.{MessageProtocols, ConfigMessageTypes}
import com.hashmapinc.tempus.edge.track.proto.{TrackConfig, TrackMetadata, MqttConfig, OpcConfig}

/**
 * This object holds the logic for handling iofog events
 */
object IofogController {
  private val log = Logger(getClass())

  // global vars
  val PATH_TO_TRACK_CONFIG = "/mnt/config/config.pb"
  val EMPTY_TRACK_CONFIG = Option.empty[TrackConfig]
  val EMPTY_TRACK_METADATA = Option.empty[TrackMetadata]
  val EMPTY_MQTT_CONFIG = Option.empty[MqttConfig]
  val EMPTY_OPC_CONFIG = Option.empty[OpcConfig]

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
   * @param trackConfig - TrackConfig protobuf object containing new configs
   * @param pathToConfig - String value holding path to the config.pb file
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
   * @param oldTrackConfig - TrackConfig protobuf object containing old configs
   *
   * @param newTrackConfig - TrackConfig protobuf object containing new configs
   * @param newTrackMetadata - TrackMetadata protobuf object containing new track metadata
   * @param newMqttConfig - MqttConfig protobuf object containing new mqtt configs
   * @param newOpcConfig - OpcConfig protobuf object containing new opc configs
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

  /**
   * This function converts a json object into a TrackMetadata protobuf object Option
   *
   * This function is not exception handled. It is intended to be called inside
   * of a scala.util.Try and handled appropriately by the caller.
   *
   * @param json - JsonObject holding the track metadata 
   * @return newConfig - TrackMetadata object Option parsed from json
   */
  def jsonToTrackMetadata(
    json: javax.json.JsonObject
  ): Option[TrackMetadata] = {
    log.info("Parsing track metadata from JSON...")
    val trackMetadata = JsonFormat.fromJsonString[TrackMetadata](json.get("trackMetadata").toString)
    Option(trackMetadata)
  }

  /**
   * This function converts a json object into a MqttConfig protobuf object Option
   *
   * This function is not exception handled. It is intended to be called inside
   * of a scala.util.Try and handled appropriately by the caller.
   *
   * @param json - JsonObject holding the opc config data 
   * @return newConfig - MqttConfig object Option parsed from json
   */
  def jsonToMqttConfig(
    json: javax.json.JsonObject
  ): Option[MqttConfig] = {
    log.info("Parsing mqtt config from JSON...")
    val mqttConfig = JsonFormat.fromJsonString[MqttConfig](json.get("mqttConfig").toString)
    Option(mqttConfig)
  }

  /**
   * This function converts a json object into a OpcConfig protobuf object Option
   *
   * This function is not exception handled. It is intended to be called inside
   * of a scala.util.Try and handled appropriately by the caller.
   *
   * @param json - JsonObject holding the track metadata 
   * @return newConfig - OpcConfig object Option parsed from json
   */
  def jsonToOpcConfig(
    json: javax.json.JsonObject
  ): Option[OpcConfig] = {
    log.info("Parsing opc config from JSON...")
    val opcConfig = JsonFormat.fromJsonString[OpcConfig](json.get("opcConfig").toString)
    Option(opcConfig)
  }

  /**
   * This function converts a json config into a tuple of proper config protobufs
   *
   * @param json - JsonObject holding the new track config 
   * @return newConfigs - Tuple containing parsed protobuf configs
   */
  def parseJsonConfig(
    json: javax.json.JsonObject
  ): (
    Option[TrackConfig], 
    Option[TrackMetadata], 
    Option[MqttConfig], 
    Option[OpcConfig]
  ) = {
    log.info("Parsing new configs from JSON...")
    val baseConfig = TrackConfig() // add configs to this base

    val trackMetadata = Try(jsonToTrackMetadata(json)).getOrElse(None)
    val mqttConfig =    Try(jsonToMqttConfig(json)).getOrElse(None)
    val opcConfig =     Try(jsonToOpcConfig(json)).getOrElse(None)

    (EMPTY_TRACK_CONFIG, trackMetadata, mqttConfig, opcConfig)
  }

  /**
   * This function converts a json config into a tuple of proper config protobufs
   *
   * @param msgContent - byte array containing iofog message content 
   * @return newConfigs - Tuple containing parsed protobuf configs
   */
  def parseConfigMessageContent(
    msgContent: Array[Byte]
  ): (
    Option[TrackConfig], 
    Option[TrackMetadata], 
    Option[MqttConfig], 
    Option[OpcConfig]
  ) = {
    log.info("Parsing new configs from config message content...")

    // extract message type and payload
    val msgType = msgContent(1)
    val msgPayload = msgContent.slice(2, msgContent.size)

    // parse and return configs tuple
    if (msgType == ConfigMessageTypes.TRACK_CONFIG_SUBMISSION.value.toByte)
      (Option(TrackConfig.parseFrom(msgPayload)), EMPTY_TRACK_METADATA, EMPTY_MQTT_CONFIG, EMPTY_OPC_CONFIG)
    else if (msgType == ConfigMessageTypes.TRACK_METADATA_SUBMISSION.value.toByte)
      (EMPTY_TRACK_CONFIG, Option(TrackMetadata.parseFrom(msgPayload)), EMPTY_MQTT_CONFIG, EMPTY_OPC_CONFIG)
    else if (msgType == ConfigMessageTypes.MQTT_CONFIG_SUBMISSION.value.toByte)
      (EMPTY_TRACK_CONFIG, EMPTY_TRACK_METADATA, Option(MqttConfig.parseFrom(msgPayload)), EMPTY_OPC_CONFIG)
    else if (msgType == ConfigMessageTypes.OPC_CONFIG_SUBMISSION.value.toByte)
      (EMPTY_TRACK_CONFIG, EMPTY_TRACK_METADATA, EMPTY_MQTT_CONFIG, Option(OpcConfig.parseFrom(msgPayload)))
    else {
      log.error("Could not parse config message of type {}", msgType)
      (EMPTY_TRACK_CONFIG, EMPTY_TRACK_METADATA, EMPTY_MQTT_CONFIG, EMPTY_OPC_CONFIG)
    }
  }

  /**
   * This function processes a new config from iofog
   *
   * @param json - JsonObject holding the new track config
   */
  def onNewIofogConfig(
    json: javax.json.JsonObject
  ): Unit = {
    // parse json into tuple of configs
    val parsedConfigs = parseJsonConfig(json)

    // merge and save configs
    this.synchronized {
      val oldConfig = loadTrackConfig(PATH_TO_TRACK_CONFIG)
      val mergedConfig = mergeConfigs(oldConfig) _ tupled parsedConfigs // this is scala black magic for de-tupling and calling mergeConfigs
      saveTrackConfig(mergedConfig, PATH_TO_TRACK_CONFIG)
    }

    // alert track of new configs
    IofogConnection.sendNewConfigAlert
  }

  /**
   * This function processes new TrackConfig messages from iofog
   *
   * @param newConfig - TrackConfig holding the new track config
   */
  def onMessages(
    messages: java.util.List[IOMessage]
  ): Unit = {
    // dispatch messages based on message types
    messages.asScala.map((msg) => {
      try {
        // determine if message is a CONFIG message
        val msgContent = msg.getContentData
        val isConfigMsg = (msgContent(0) == MessageProtocols.CONFIG.value.toByte)

        // handle config messages
        if (isConfigMsg) {
          // parse configs into configs tuple
          val newConfigs = parseConfigMessageContent(msgContent)

          // update configs
          this.synchronized {
            val oldConfig = loadTrackConfig(PATH_TO_TRACK_CONFIG)
            val mergedConfig = mergeConfigs(oldConfig) _ tupled newConfigs
            saveTrackConfig(mergedConfig, PATH_TO_TRACK_CONFIG)
          }

          // alert track of new configs
          IofogConnection.sendNewConfigAlert
        } 
      } catch {
        case e: Exception => log.error("Error trying to parse iofog message: " + e)
      }
    })
  }
}