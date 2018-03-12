package com.hashmapinc.tempus.edge.jsonTranslator.iofog

import collection.JavaConverters._
import scala.util.Try

import com.iotracks.elements.IOMessage
import com.typesafe.scalalogging.Logger
import com.google.protobuf.InvalidProtocolBufferException
import scalapb.json4s.JsonFormat

import com.hashmapinc.tempus.edge.proto.{MessageProtocols, ConfigMessageTypes, DataMessageTypes, JsonDataMessage}
import com.hashmapinc.tempus.edge.track.proto.{TrackConfig, TrackMetadata, MqttConfig, OpcConfig}

/**
 * This object holds the logic for handling iofog events
 */
object IofogController {
  private val log = Logger(getClass())

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
        val newMsgContent: Array[Byte] = if (msgProtocol.toChar == '{') {
          val json = new String(msgContent, "UTF-8")
          val json_pb = jsonToProtobuf(json)
          MessageProtocols.DATA.value.toByte +: DataMessageTypes.JSON.value.toByte +: json_pb.toByteArray
        } else {
          Array(0) //TODO: do this for real
        }

        // send the translated content to iofog
        IofogConnection.sendWSMessage(newMsgContent)
      } catch {
        case e: Exception => log.error("Error trying to parse iofog message: " + e)
      }
    })
  }
}