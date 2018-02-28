package com.hashmapinc.tempus.edge.track_manager

import com.typesafe.scalalogging.Logger
import play.api.libs.json.Json

/**
 *  Case class for structuring incoming iofog JSON configurations.
 */
case class IofogConfig(
  // OPC configs 
  opcEndpoint: String
)
object IofogConfig {
  // define implicit config reader for json to case-class conversion
  // https://www.playframework.com/documentation/2.6.x/ScalaJsonAutomated
  implicit val iofConfigReader = Json.reads[IofogConfig]
}


/**
 * This object is responsible for holding and updating opcMonitor configurations
 */
object Config {
  private val log = Logger(getClass())

  // Set default configs
  log.info("Setting default configs")
  var context = "production"
  var iofogConfig: Option[IofogConfig] = None

  // get container ID
  private val selfname = System.getenv("SELFNAME")
  val CONTAINER_ID = if(selfname == null) "" else selfname

  // define message type
  val CONFIG_ALERT_MSG:   Byte = 2
  val UPDATE_CONFIG_MSG:  Byte = 3

  /**
   * This function updates the configs
   * 
   * @param newConfig - IofogConfig case class object holding new config values
   */
  def update(
    newConfig: IofogConfig
  ): Unit = {
    log.info("Updating configs")
    log.info("newConfig: " + newConfig.toString)

    // update configs
    this.synchronized {
      context = "production"
      iofogConfig = Option(newConfig)
    }
  }

  /**
   * This function sets the configs to test values
   * 
   * @param testConfig - IofogConfig instance with configuration to use
   */
  def initTestContext(
    testConfig: IofogConfig
  ): Unit = {
    log.info("Setting test configs")
    log.info("testConfig: " + testConfig.toString)

    // update configs
    this.synchronized {
      context = "test"
      iofogConfig = Option(testConfig)
    }
  }

  /**
   * This function resets the configs to default values
   */
  def reset: Unit = {
    log.info("Reseting configs")

    // update configs
    this.synchronized {
      context = "production"
      iofogConfig = None
    }
  }
}