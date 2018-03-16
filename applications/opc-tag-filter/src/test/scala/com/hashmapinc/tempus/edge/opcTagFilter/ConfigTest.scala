package com.hashmapinc.tempus.edge.opcTagFilter

import java.nio.file.{Files, Paths}

import org.scalatest.FlatSpec

import com.hashmapinc.tempus.edge.proto.TrackConfig

class ConfigTest extends FlatSpec {
  //===========================================================================
  // test track config saving and loading from disk
  //===========================================================================
  val TEST_CONFIG_PATH = "./test_config.pb"
  
  "loadConfigs" should "have no test config file when tests start" in {
    // check that test config file does not exist
    val configPath = Paths.get(TEST_CONFIG_PATH)
    assert(!Files.exists(configPath))
  }
  it should "properly read config pb files from disk" in {
    // empty config to disk
    val emptyConfig = TrackConfig()
    Files.write(Paths.get(TEST_CONFIG_PATH), emptyConfig.toByteArray)

    // confirm the config is properly loaded
    val loadedConfigs = Config.loadConfigs(TEST_CONFIG_PATH)
    assert(emptyConfig == loadedConfigs.get)
  }
  it should "return None if no config can be deserialized" in {
    // write garbage to the config file
    val garbage: Array[Byte] = "garbage".toCharArray.map(_.toByte)
    Files.write(Paths.get(TEST_CONFIG_PATH), garbage)

    // try to load config. Check that Option(None)
    val loadedConfigs = Config.loadConfigs(TEST_CONFIG_PATH)
    assert(loadedConfigs.isEmpty)
  }
  it should "clean up test config files after testing is done" in {
    // delete the test config file
    val testConfigPath = Paths.get(TEST_CONFIG_PATH)
    assert(Files.deleteIfExists(testConfigPath)) // returns true if file exists
  }
  //===========================================================================
}