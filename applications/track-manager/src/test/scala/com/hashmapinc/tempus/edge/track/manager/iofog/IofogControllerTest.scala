package com.hashmapinc.tempus.edge.track.manager.iofog

import java.nio.file.{Paths, Files}
import org.scalatest.{FlatSpec, BeforeAndAfterEach}

import com.hashmapinc.tempus.edge.proto._
import com.hashmapinc.tempus.edge.track.proto._
import com.hashmapinc.tempus.edge.track.proto.TrackConfig._

class IofogControllerTest extends FlatSpec with BeforeAndAfterEach {
  // global testing vars
  val TEST_CONFIG_PATH = "./test_config.pb"

  //===========================================================================
  // test track config saving and loading from disk
  //===========================================================================
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
  "mergeConfigs" should "be testable" in {
    assert(true)
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