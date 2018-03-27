package com.hashmapinc.tempus.edge.track.manager.iofog

import java.nio.file.{Paths, Files}
import javax.json.Json

import org.scalatest.FlatSpec
import scalapb.json4s.JsonFormat

import com.hashmapinc.tempus.edge.proto._
import com.hashmapinc.tempus.edge.proto.TrackConfig._

class IofogControllerTest extends FlatSpec {
  //===========================================================================
  // test track config saving and loading from disk
  //===========================================================================
  val TEST_CONFIG_PATH = "./test_config.pb"

  "TrackConfig loading and saving" should "have no test config file when tests start" in {
    // check that test config file does not exist
    val configPath = Paths.get(TEST_CONFIG_PATH)
    assert(!Files.exists(configPath))
  }
  it should "save and load an empty TrackConfig with no existing pb file on disk" in {
    // write empty config
    val trackConfig_original = TrackConfig()
    IofogController.saveTrackConfig(trackConfig_original, TEST_CONFIG_PATH)

    // read saved config
    val trackConfig_loaded = IofogController.loadTrackConfig(TEST_CONFIG_PATH)

    // ensure they are the same
    assert(trackConfig_original == trackConfig_loaded)
  }
  it should "overwrite an existing pb file on disk with a new TrackConfig" in {
    // check that test config file exists from previous test
    assert(Files.exists(Paths.get(TEST_CONFIG_PATH)))

    // create new track meta
    val trackMeta = TrackMetadata("TEST TRACK", 0, "{}")
    val newTrackConfig = TrackConfig().withTrackMetadata(trackMeta)

    // overwrite the file on disk
    IofogController.saveTrackConfig(newTrackConfig, TEST_CONFIG_PATH)

    // load config and check that it is the same as the config saved
    val loadedTrackConfig = IofogController.loadTrackConfig(TEST_CONFIG_PATH)
    assert(newTrackConfig == loadedTrackConfig)
  }
  it should "return an empty track config when reading a malformed config file" in {
    // write garbage to the config file
    val garbage: Array[Byte] = "garbage".toCharArray.map(_.toByte)
    Files.write(Paths.get(TEST_CONFIG_PATH), garbage)

    // try to load config. Check that the config returned is empty
    val loadedTrackConfig = IofogController.loadTrackConfig(TEST_CONFIG_PATH)
    assert(TrackConfig() == loadedTrackConfig)
  }
  it should "clean up test config files after testing is done" in {
    // delete the test config file
    val testConfigPath = Paths.get(TEST_CONFIG_PATH)
    assert(Files.deleteIfExists(testConfigPath)) // returns true if file exists
  }
  //===========================================================================


  //===========================================================================
  // test config merging
  //===========================================================================
  // define standard configs for merge testing
  val trackMeta_A = Option(TrackMetadata("TEST_TRACK_A", 0, "{}"))
  val trackMeta_B = Option(TrackMetadata("TEST_TRACK_B", 0, "{}"))
  val mqttConfig_A = Option(MqttConfig().withSecurityType(MqttConfig.SecurityType.NONE).withUser(MqttConfig.User("user_A", "password_A")))
  val mqttConfig_B = Option(MqttConfig().withSecurityType(MqttConfig.SecurityType.TOKEN).withUser(MqttConfig.User("user_B", "password_B")))
  val opcConfig_A = Option(OpcConfig("ENDPOINT A", OpcConfig.SecurityType.NONE))
  val opcConfig_B = Option(OpcConfig("ENDPOINT B", OpcConfig.SecurityType.NONE))

  "mergeConfigs" should "return an empty track config if no new configs are provided" in {
    assert(TrackConfig() == IofogController.mergeConfigs(TrackConfig())(None,None,None,None))
  }
  it should "not change a track config if no new configs are provided to merge with" in {
    val oldConfig = TrackConfig(trackMeta_A, mqttConfig_A, opcConfig_A)
    assert(oldConfig == IofogController.mergeConfigs(oldConfig)(None,None,None,None))
  }
  it should "completely replace an old config if a new track config is provided" in {
    val oldConfig = TrackConfig(trackMeta_A, mqttConfig_A, opcConfig_A)
    val newConfig = TrackConfig(trackMeta_B, mqttConfig_B, opcConfig_B)
    assert(newConfig == IofogController.mergeConfigs(oldConfig)(Option(newConfig),None,None,None))
    assert(newConfig == IofogController.mergeConfigs(oldConfig)(Option(newConfig),trackMeta_A,None,None))
    assert(newConfig == IofogController.mergeConfigs(oldConfig)(Option(newConfig),None,mqttConfig_A,None))
    assert(newConfig == IofogController.mergeConfigs(oldConfig)(Option(newConfig),None,None,opcConfig_A))
    assert(newConfig == IofogController.mergeConfigs(oldConfig)(Option(newConfig),trackMeta_A,mqttConfig_A,None))
    assert(newConfig == IofogController.mergeConfigs(oldConfig)(Option(newConfig),trackMeta_A,None,opcConfig_A))
    assert(newConfig == IofogController.mergeConfigs(oldConfig)(Option(newConfig),None,mqttConfig_A,opcConfig_A))
  }
  it should "update with new track metadata if track metadata is provided" in {
    val oldConfig = TrackConfig(trackMeta_A, mqttConfig_A, opcConfig_A)
    assert(trackMeta_B.get == IofogController.mergeConfigs(oldConfig)(None,trackMeta_B,None,None).getTrackMetadata)
    assert(trackMeta_B.get == IofogController.mergeConfigs(oldConfig)(None,trackMeta_B,mqttConfig_B,None).getTrackMetadata)
    assert(trackMeta_B.get == IofogController.mergeConfigs(oldConfig)(None,trackMeta_B,None,opcConfig_B).getTrackMetadata)
    assert(trackMeta_B.get == IofogController.mergeConfigs(oldConfig)(None,trackMeta_B,mqttConfig_B,opcConfig_B).getTrackMetadata)
  }
  it should "update with new mqtt config if mqtt config is provided" in {
    val oldConfig = TrackConfig(trackMeta_A, mqttConfig_A, opcConfig_A)
    assert(mqttConfig_B.get == IofogController.mergeConfigs(oldConfig)(None,None,mqttConfig_B,None).getMqttConfig)
    assert(mqttConfig_B.get == IofogController.mergeConfigs(oldConfig)(None,trackMeta_B,mqttConfig_B,None).getMqttConfig)
    assert(mqttConfig_B.get == IofogController.mergeConfigs(oldConfig)(None,None,mqttConfig_B,opcConfig_B).getMqttConfig)
    assert(mqttConfig_B.get == IofogController.mergeConfigs(oldConfig)(None,trackMeta_B,mqttConfig_B,opcConfig_B).getMqttConfig)
  }
  it should "update with new opc config if opc config is provided" in {
    val oldConfig = TrackConfig(trackMeta_A, mqttConfig_A, opcConfig_A)
    assert(opcConfig_B.get == IofogController.mergeConfigs(oldConfig)(None,None,None,opcConfig_B).getOpcConfig)
    assert(opcConfig_B.get == IofogController.mergeConfigs(oldConfig)(None,trackMeta_B,None,opcConfig_B).getOpcConfig)
    assert(opcConfig_B.get == IofogController.mergeConfigs(oldConfig)(None,None,mqttConfig_B,opcConfig_B).getOpcConfig)
    assert(opcConfig_B.get == IofogController.mergeConfigs(oldConfig)(None,trackMeta_B,mqttConfig_B,opcConfig_B).getOpcConfig)
  }
  //===========================================================================


  //===========================================================================
  // test config parsing from json objects
  //===========================================================================
  // create sample json objects from json files
  val jsonConfig_A = Json.createReader(getClass.getResourceAsStream("/config_A.json")).readObject
  val jsonConfig_B = Json.createReader(getClass.getResourceAsStream("/config_B.json")).readObject
  val jsonConfig_C = Json.createReader(getClass.getResourceAsStream("/config_C.json")).readObject
  val jsonConfig_D = Json.createReader(getClass.getResourceAsStream("/config_D.json")).readObject
  val jsonConfig_E = Json.createReader(getClass.getResourceAsStream("/config_E.json")).readObject
  val jsonConfig_F = Json.createReader(getClass.getResourceAsStream("/config_F.json")).readObject
  val jsonConfig_G = Json.createReader(getClass.getResourceAsStream("/config_G.json")).readObject
  val jsonConfig_H = Json.createReader(getClass.getResourceAsStream("/config_H.json")).readObject
  val jsonConfig_I = Json.createReader(getClass.getResourceAsStream("/config_I.json")).readObject
  val garbageConfig = Json.createReader(getClass.getResourceAsStream("/garbage_config.json")).readObject
  
  // create empty configs for comparisons
  val EMPTY_TRACK_CONFIG    = TrackConfig()
  val EMPTY_TRACK_METADATA  = TrackMetadata()
  val EMPTY_MQTT_CONFIG     = MqttConfig()
  val EMPTY_OPC_CONFIG      = OpcConfig()

  "JSON config parsing" should "properly parse a complete track config" in {
    val parsedConfig = IofogController.mergeConfigs(TrackConfig()) _ tupled IofogController.parseJsonConfig(jsonConfig_A)
    val correctConfig = JsonFormat.fromJsonString[TrackConfig](jsonConfig_A.toString)

    assert(parsedConfig == correctConfig)
  }
  it should "properly parse valid partial configs" in {
    val parsedConfig_B = IofogController.mergeConfigs(TrackConfig()) _ tupled IofogController.parseJsonConfig(jsonConfig_B)
    assert(parsedConfig_B.getTrackMetadata  != EMPTY_TRACK_METADATA)
    assert(parsedConfig_B.getMqttConfig     == EMPTY_MQTT_CONFIG)
    assert(parsedConfig_B.getOpcConfig      == EMPTY_OPC_CONFIG)
    val parsedConfig_C = IofogController.mergeConfigs(TrackConfig()) _ tupled IofogController.parseJsonConfig(jsonConfig_C)
    assert(parsedConfig_C.getTrackMetadata  == EMPTY_TRACK_METADATA)
    assert(parsedConfig_C.getMqttConfig     != EMPTY_MQTT_CONFIG)
    assert(parsedConfig_C.getOpcConfig      == EMPTY_OPC_CONFIG)
    val parsedConfig_D = IofogController.mergeConfigs(TrackConfig()) _ tupled IofogController.parseJsonConfig(jsonConfig_D)
    assert(parsedConfig_D.getTrackMetadata  == EMPTY_TRACK_METADATA)
    assert(parsedConfig_D.getMqttConfig     == EMPTY_MQTT_CONFIG)
    assert(parsedConfig_D.getOpcConfig      != EMPTY_OPC_CONFIG)
    val parsedConfig_E = IofogController.mergeConfigs(TrackConfig()) _ tupled IofogController.parseJsonConfig(jsonConfig_E)
    assert(parsedConfig_E.getTrackMetadata  != EMPTY_TRACK_METADATA)
    assert(parsedConfig_E.getMqttConfig     != EMPTY_MQTT_CONFIG)
    assert(parsedConfig_E.getOpcConfig      == EMPTY_OPC_CONFIG)
    val parsedConfig_F = IofogController.mergeConfigs(TrackConfig()) _ tupled IofogController.parseJsonConfig(jsonConfig_F)
    assert(parsedConfig_F.getTrackMetadata  != EMPTY_TRACK_METADATA)
    assert(parsedConfig_F.getMqttConfig     == EMPTY_MQTT_CONFIG)
    assert(parsedConfig_F.getOpcConfig      != EMPTY_OPC_CONFIG)
    val parsedConfig_G = IofogController.mergeConfigs(TrackConfig()) _ tupled IofogController.parseJsonConfig(jsonConfig_G)
    assert(parsedConfig_G.getTrackMetadata  == EMPTY_TRACK_METADATA)
    assert(parsedConfig_G.getMqttConfig     != EMPTY_MQTT_CONFIG)
    assert(parsedConfig_G.getOpcConfig      != EMPTY_OPC_CONFIG)
  }
  it should "gracefully ignore empty json" in {
    val emptyConfig = TrackConfig()
    val parsedConfig_H_empty = IofogController.mergeConfigs(emptyConfig) _ tupled IofogController.parseJsonConfig(jsonConfig_H)
    assert(parsedConfig_H_empty == emptyConfig)

    val fullConfig = IofogController.mergeConfigs(emptyConfig) _ tupled IofogController.parseJsonConfig(jsonConfig_A)
    val parsedConfig_H_full = IofogController.mergeConfigs(fullConfig) _ tupled IofogController.parseJsonConfig(jsonConfig_H)
    assert(parsedConfig_H_full == fullConfig)
  }
  it should "gracefully ignore extra fields in otherwise valid json" in {
    val emptyConfig = TrackConfig()
    val properConfig = IofogController.mergeConfigs(emptyConfig) _ tupled IofogController.parseJsonConfig(jsonConfig_A)
    val parsedConfig_I = IofogController.mergeConfigs(emptyConfig) _ tupled IofogController.parseJsonConfig(jsonConfig_I)
    assert(parsedConfig_I == properConfig)
  }
  it should "gracefully ignore garbage json" in {
    val emptyConfig = TrackConfig()
    val parsedConfig_garbage_empty = IofogController.mergeConfigs(emptyConfig) _ tupled IofogController.parseJsonConfig(garbageConfig)
    assert(parsedConfig_garbage_empty == emptyConfig)

    val fullConfig = IofogController.mergeConfigs(emptyConfig) _ tupled IofogController.parseJsonConfig(jsonConfig_A)
    val parsedConfig_garbage_full = IofogController.mergeConfigs(fullConfig) _ tupled IofogController.parseJsonConfig(garbageConfig)
    assert(parsedConfig_garbage_full == fullConfig)
  }
  //===========================================================================


  //===========================================================================
  // test config parsing from protobuf byte arrays
  //===========================================================================
  // create sample protobuf arrays
  val trackConfig_pb = TrackConfig.parseFrom(getClass.getResourceAsStream("/trackConfig.pb"))
  val trackMetadata_pb = TrackMetadata.parseFrom(getClass.getResourceAsStream("/trackMetadata.pb"))
  val mqttConfig_pb = MqttConfig.parseFrom(getClass.getResourceAsStream("/mqttConfig.pb"))
  val opcConfig_pb = OpcConfig.parseFrom(getClass.getResourceAsStream("/opcConfig.pb"))
  val opcSubscriptions_pb = OpcConfig.Subscriptions.parseFrom(getClass.getResourceAsStream("/opcSubscriptions.pb"))

  // create sample tempus edge message content arrays
  val trackConfig_msgContent = MessageProtocols.CONFIG.value.toByte +: ConfigMessageTypes.TRACK_CONFIG_SUBMISSION.value.toByte +: trackConfig_pb.toByteArray
  val trackMetadata_msgContent = MessageProtocols.CONFIG.value.toByte +: ConfigMessageTypes.TRACK_METADATA_SUBMISSION.value.toByte +: trackMetadata_pb.toByteArray
  val mqttConfig_msgContent = MessageProtocols.CONFIG.value.toByte +: ConfigMessageTypes.MQTT_CONFIG_SUBMISSION.value.toByte +: mqttConfig_pb.toByteArray
  val opcConfig_msgContent = MessageProtocols.CONFIG.value.toByte +: ConfigMessageTypes.OPC_CONFIG_SUBMISSION.value.toByte +: opcConfig_pb.toByteArray
  val opcSubscriptions_msgContent = MessageProtocols.CONFIG.value.toByte +: ConfigMessageTypes.OPC_SUBSCRIPTIONS_SUBMISSION.value.toByte +: opcSubscriptions_pb.toByteArray
  val garbage_msgContent: Array[Byte] = "garbage".toCharArray.map(_.toByte)

  "parseConfigMessageContent" should "properly parse protobufs from valid tempus edge message content" in {
    val emptyConfig = TrackConfig()
    val parsed_trackConfig = IofogController.mergeConfigs(emptyConfig) _ tupled IofogController.parseConfigMessageContent(trackConfig_msgContent)
    val parsed_trackMetadata = IofogController.mergeConfigs(emptyConfig) _ tupled IofogController.parseConfigMessageContent(trackMetadata_msgContent)
    val parsed_mqttConfig = IofogController.mergeConfigs(emptyConfig) _ tupled IofogController.parseConfigMessageContent(mqttConfig_msgContent)
    val parsed_opcConfig = IofogController.mergeConfigs(emptyConfig) _ tupled IofogController.parseConfigMessageContent(opcConfig_msgContent)
    val parsed_opcSubscriptions = IofogController.mergeConfigs(emptyConfig) _ tupled IofogController.parseConfigMessageContent(opcSubscriptions_msgContent)

    assert(trackConfig_pb == parsed_trackConfig)
    assert(trackMetadata_pb == parsed_trackMetadata.getTrackMetadata)
    assert(mqttConfig_pb == parsed_mqttConfig.getMqttConfig)
    assert(opcConfig_pb == parsed_opcConfig.getOpcConfig)
    assert(opcSubscriptions_pb == parsed_opcSubscriptions.getOpcConfig.getSubs)
  }
  it should "gracefully ignore invalid tempus edge message content" in {
    val emptyConfig = TrackConfig()
    val parsedConfig_empty = IofogController.mergeConfigs(emptyConfig) _ tupled IofogController.parseConfigMessageContent(garbage_msgContent)
    assert(emptyConfig == parsedConfig_empty)

    val fullConfig = IofogController.mergeConfigs(emptyConfig) _ tupled IofogController.parseConfigMessageContent(trackConfig_msgContent)
    val parsedConfig_full = IofogController.mergeConfigs(fullConfig) _ tupled IofogController.parseConfigMessageContent(garbage_msgContent)
    assert(fullConfig == parsedConfig_full)
  }
  //===========================================================================
}