package com.hashmapinc.tempus.edge.trackManager

import scala.util.Try

import com.typesafe.scalalogging.Logger
import scalapb.json4s.JsonFormat

import com.hashmapinc.tempus.edge.proto._

/**
 * This object holds the logic for parsing config messages
 */
object Parsers {
  private val log = Logger(getClass())

  // global vars
  val EMPTY_TRACK_CONFIG = Option.empty[TrackConfig]
  val EMPTY_TRACK_METADATA = Option.empty[TrackMetadata]
  val EMPTY_MQTT_CONFIG = Option.empty[MqttConfig]
  val EMPTY_OPC_CONFIG = Option.empty[OpcConfig]

  /**
   * This function converts a json object into a TrackMetadata protobuf object Option
   *
   * This function is not exception handled. It is intended to be called inside
   * of a scala.util.Try and handled appropriately by the caller.
   *
   * @param json        - JsonObject holding the track metadata 
   * @return newConfig  - TrackMetadata object Option parsed from json
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
   * @param json        - JsonObject holding the opc config data 
   * @return newConfig  - MqttConfig object Option parsed from json
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
   * @param json        - JsonObject holding the track metadata 
   * @return newConfig  - OpcConfig object Option parsed from json
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
    else if (msgType == ConfigMessageTypes.OPC_SUBSCRIPTIONS_SUBMISSION.value.toByte) {
      val subs = OpcConfig.Subscriptions.parseFrom(msgPayload)
      val newOpcConfig = Try({
        Config.loadTrackConfig(Config.PATH_TO_TRACK_CONFIG).getOpcConfig.withSubs(subs)
      }).getOrElse(OpcConfig().withSubs(subs))
      (EMPTY_TRACK_CONFIG, EMPTY_TRACK_METADATA, EMPTY_MQTT_CONFIG, Option(newOpcConfig))
    } else {
      log.error("Could not parse config message of type {}", msgType)
      (EMPTY_TRACK_CONFIG, EMPTY_TRACK_METADATA, EMPTY_MQTT_CONFIG, EMPTY_OPC_CONFIG)
    }
  }
}