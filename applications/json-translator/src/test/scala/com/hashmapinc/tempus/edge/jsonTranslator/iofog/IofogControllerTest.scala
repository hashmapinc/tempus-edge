package com.hashmapinc.tempus.edge.jsonTranslator.iofog

import java.io.StringReader
import javax.json.Json

import org.scalatest.FlatSpec

import com.hashmapinc.tempus.edge.proto._
import com.hashmapinc.tempus.edge.track.proto._

class IofogControllerTest extends FlatSpec {
  // create sample protobuf objects from pb files
  val trackConfig_pb = TrackConfig.parseFrom(getClass.getResourceAsStream("/trackConfig.pb"))
  val trackMetadata_pb = TrackMetadata.parseFrom(getClass.getResourceAsStream("/trackMetadata.pb"))
  val mqttConfig_pb = MqttConfig.parseFrom(getClass.getResourceAsStream("/mqttConfig.pb"))
  val opcConfig_pb = OpcConfig.parseFrom(getClass.getResourceAsStream("/opcConfig.pb"))
  val jsonData_pb = JsonDataMessage("{\"a\": 3}")

  // create sample json objects from json files
  val trackConfig_json = Json.createReader(getClass.getResourceAsStream("/config_A.json")).readObject
  val trackMetadata_json = Json.createReader(getClass.getResourceAsStream("/config_B.json")).readObject
  val mqttConfig_json = Json.createReader(getClass.getResourceAsStream("/config_C.json")).readObject
  val opcConfig_json = Json.createReader(getClass.getResourceAsStream("/config_D.json")).readObject
  val jsonData_js = Json.createReader(new StringReader("{\"a\": 3}")).readObject
  
  //===========================================================================
  // test json to protobuf conversion
  //===========================================================================
  "jsonToProtobuf" should "properly convert tempus edge message contents into json byte arrays" in {
    val convertedPB = IofogController.jsonToProtobuf("{\"a\": 3}")
    assert(convertedPB == jsonData_pb)
  }
  //===========================================================================

  //===========================================================================
  // test protobuf to json conversion
  //===========================================================================
  "protobufToJson" should "properly convert tempus edge message contents into json byte arrays" in {
    // get converted json from config protocol messages
    val updateAlert_json = IofogController.protobufToJson(Array(MessageProtocols.CONFIG.value.toByte,ConfigMessageTypes.UPDATE_ALERT.value.toByte))
    val trackConfig_json = IofogController.protobufToJson(MessageProtocols.CONFIG.value.toByte +: ConfigMessageTypes.TRACK_CONFIG_SUBMISSION.value.toByte +: trackConfig_pb.toByteArray)
    val trackMetadata_json = IofogController.protobufToJson(MessageProtocols.CONFIG.value.toByte +: ConfigMessageTypes.TRACK_METADATA_SUBMISSION.value.toByte +: trackMetadata_pb.toByteArray)
    val mqttConfig_json = IofogController.protobufToJson(MessageProtocols.CONFIG.value.toByte +: ConfigMessageTypes.MQTT_CONFIG_SUBMISSION.value.toByte +: mqttConfig_pb.toByteArray)
    val opcConfig_json = IofogController.protobufToJson(MessageProtocols.CONFIG.value.toByte +: ConfigMessageTypes.OPC_CONFIG_SUBMISSION.value.toByte +: opcConfig_pb.toByteArray)

    // get converted json from data protocol messages
    val jsonData_json = IofogController.protobufToJson(MessageProtocols.DATA.value.toByte +: DataMessageTypes.JSON.value.toByte +: jsonData_pb.toByteArray)

    // get garbage
    val garbageConfig_json = IofogController.protobufToJson(MessageProtocols.CONFIG.value.toByte +: 99.toByte +: trackConfig_pb.toByteArray)
    val garbageData_json = IofogController.protobufToJson(MessageProtocols.DATA.value.toByte +: 99.toByte +: trackConfig_pb.toByteArray)
    val garbage_json = IofogController.protobufToJson(99.toByte +: 99.toByte +: trackConfig_pb.toByteArray)

    // check that return arrays do or do not exist as expected
    assert(updateAlert_json.isEmpty)
    assert(!trackConfig_json.isEmpty)
    assert(!trackMetadata_json.isEmpty)
    assert(!mqttConfig_json.isEmpty)
    assert(!opcConfig_json.isEmpty)
    
    assert(!jsonData_json.isEmpty)

    assert(garbageConfig_json.isEmpty)
    assert(garbageData_json.isEmpty)
    assert(garbage_json.isEmpty)
  }
  //===========================================================================
}