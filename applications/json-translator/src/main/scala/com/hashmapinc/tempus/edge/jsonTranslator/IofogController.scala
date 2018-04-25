package com.hashmapinc.tempus.edge.jsonTranslator

import collection.JavaConverters._
import scala.util.Try

import com.iotracks.elements.IOMessage
import com.typesafe.scalalogging.Logger
import scalapb.json4s.JsonFormat

import com.hashmapinc.tempus.edge.proto._
import com.hashmapinc.tempus.edge.iofog.IofogConnection

/**
 * This object holds the logic for handling iofog events
 */
object IofogController extends com.hashmapinc.tempus.edge.iofog.IofogController {
  val log = Logger(getClass())

  /**
   * This function converts a json object into a protobuf object Option
   *
   * @param json - String object holding the json to be protobuf'd 
   * @return pb - JsonDataMessage protobuf object Option parsed from json
   */
  def jsonToProtobuf(
    json: String
  ): JsonDataMessage = {
    log.info("Converting JSON to JsonDataMessage...")
    JsonDataMessage(json.toString)
  }

  /**
   * This function converts a tempus edge msgContent byte array to a json string byte array
   *
   * @param msgContent - Byte array holding the msg content of a tempus edge message 
   * @return newMsgContent - Byte Array holding the json-ified protobuf string
   */
  def protobufToJson(
    msgContent: Array[Byte]
  ): Option[Array[Byte]] = {
    log.info("Converting tempus edge message to JSON...")

    // get protocol, type, and raw proto array
    val msgProtocol = msgContent(0)
    val msgType = msgContent(1)
    val rawProto = msgContent.slice(2, msgContent.size)

    // handle each tempus edge message protocol+type below
    if (msgProtocol == MessageProtocols.CONFIG.value.toByte) {
      if (msgType == ConfigMessageTypes.UPDATE_ALERT.value.toByte) {
        log.error("Cannot convert UPDATE_ALERT CONFIG messages to JSON. Skipping this message...")
        None
      } else if (msgType == ConfigMessageTypes.TRACK_CONFIG_SUBMISSION.value.toByte) {
        val pb = TrackConfig.parseFrom(rawProto)
        val jsonString = JsonFormat.toJsonString(pb)
        Option(jsonString.toArray.map(_.toByte))
      } else if (msgType == ConfigMessageTypes.TRACK_METADATA_SUBMISSION.value.toByte) {
        val pb = TrackMetadata.parseFrom(rawProto)
        val jsonString = JsonFormat.toJsonString(pb)
        Option(jsonString.toArray.map(_.toByte))
      } else if (msgType == ConfigMessageTypes.MQTT_CONFIG_SUBMISSION.value.toByte) {
        val pb = MqttConfig.parseFrom(rawProto)
        val jsonString = JsonFormat.toJsonString(pb)
        Option(jsonString.toArray.map(_.toByte))
      } else if (msgType == ConfigMessageTypes.OPC_CONFIG_SUBMISSION.value.toByte) {
        val pb = OpcConfig.parseFrom(rawProto)
        val jsonString = JsonFormat.toJsonString(pb)
        Option(jsonString.toArray.map(_.toByte))
      } else {
        log.error("Could not deserialize tempus edge message with protocol " + msgProtocol + " and type " + msgType)
        None
      }

    } else if (msgProtocol == MessageProtocols.DATA.value.toByte) {
      if (msgType == DataMessageTypes.JSON.value.toByte) {
        log.error("Cannot convert UPDATE_ALERT CONFIG messages to JSON. Skipping this message...")
        val pb = JsonDataMessage.parseFrom(rawProto)
        val jsonString = JsonFormat.toJsonString(pb)
        Option(jsonString.toArray.map(_.toByte))
      } else {
        log.error("Could not deserialize tempus edge message with protocol " + msgProtocol + " and type " + msgType)
        None
      }

    } else {
      log.error("Could not deserialize tempus edge message with protocol " + msgProtocol + " and type " + msgType)
      None
    }
  }

  /**
   * This function processes new iofog messages from the IofogListener
   *
   * @param messages - list of IOMessages from IofogListener
   */
  def onMessages(
    messages: java.util.List[IOMessage]
  ): Unit = {
    // dispatch messages based on message types
    messages.asScala.map((msg) => {
      try {
        // determine if message is a JSON message or a 
        val msgContent = msg.getContentData
        val msgProtocol = msgContent(0)
        val msgType = msgContent(1)

        // handle json strings
        val newMsgContent: Option[Array[Byte]] = if (msgProtocol.toChar == '{') {
          val json = new String(msgContent, "UTF-8")
          val json_pb = jsonToProtobuf(json)
          Option(MessageProtocols.DATA.value.toByte +: DataMessageTypes.JSON.value.toByte +: json_pb.toByteArray)
        } else {
          protobufToJson(msgContent)
        }

        // send the translated content to iofog if it exists
        if (!newMsgContent.isEmpty) IofogConnection.sendWSMessage(newMsgContent.get)
        
      } catch {
        case e: Exception => log.error("Error trying to parse iofog message: " + e)
      }
    })
  }
}