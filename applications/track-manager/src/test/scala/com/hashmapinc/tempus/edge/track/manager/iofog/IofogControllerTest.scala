package com.hashmapinc.tempus.edge.track.manager.iofog

import java.nio.file.{Paths, Files}
import org.scalatest.FlatSpec

import com.hashmapinc.tempus.edge.proto._
import com.hashmapinc.tempus.edge.track.proto._
import com.hashmapinc.tempus.edge.track.proto.TrackConfig._

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
  val mqttConfig_A = Option(
    MqttConfig().withSecurityType(MqttConfig.SecurityType.NONE).withUser(MqttConfig.User("user_A", "password_A"))
  )
  val mqttConfig_B = Option(
    MqttConfig().withSecurityType(MqttConfig.SecurityType.TOKEN).withUser(MqttConfig.User("user_B", "password_B"))
  )
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
  "parseJsonConfig" should "be testable" in {
    assert(true)
  }
  //===========================================================================


  //===========================================================================
  // test config parsing from protobuf byte arrays
  //===========================================================================
  "parseConfigMessageContent" should "be testable" in {
    assert(true)
  }
  //===========================================================================
}