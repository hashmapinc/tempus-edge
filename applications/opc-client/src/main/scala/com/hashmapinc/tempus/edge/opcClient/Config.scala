package com.hashmapinc.tempus.edge.opcClient

import java.nio.file.{Files,Paths}
import scala.util.Try

import com.typesafe.scalalogging.Logger

import com.hashmapinc.tempus.edge.track.proto.{TrackConfig, OpcConfig}

/**
 * This object is responsible for holding and updating opcMonitor configurations
 */
object Config {
  private val log = Logger(getClass())

  // track config setup
  val PATH_TO_TRACK_CONFIG = "/mnt/config/config.pb"
  var trackConfig: Option[TrackConfig] = None

  // container ID setup
  private val selfname = System.getenv("SELFNAME")
  val CONTAINER_ID = if(selfname == null) "" else selfname

  /**
   * This function performs the initialization of Config
   */
  def init: Unit = {
    log.info("Attempting initial load of trackConfig from " + PATH_TO_TRACK_CONFIG)

    // update configs
    trackConfig = loadConfigs(PATH_TO_TRACK_CONFIG)
  }

  /**
   * This function loads track config and returns track config
   * 
   * @param configPath - string containing absolute path to track config protobuf file
   *
   * @return newConfig - Option[TrackConfig] object holding the config loaded from configPath
   */
  def loadConfigs(
    configPath: String
  ): Option[TrackConfig] = {
    log.info("Loading new configs from " + configPath)
    val rawProto = this.synchronized {
      val loadPath = Paths.get(configPath)
      Files.readAllBytes(loadPath)
    }
    val newConfig: Option[TrackConfig] = Try(
      Option(TrackConfig.parseFrom(rawProto))
    ).getOrElse(None)

    // log results
    if (newConfig.isEmpty)
      log.warn("could not load trackConfig from " + configPath)
    else 
      log.info("New trackConfig loaded successfully: " + newConfig.toString)
    
    newConfig
  }
}