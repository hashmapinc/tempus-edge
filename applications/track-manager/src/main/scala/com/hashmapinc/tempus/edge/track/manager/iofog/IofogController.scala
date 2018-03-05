package com.hashmapinc.tempus.edge.track.manager.iofog

import javax.json.JsonObject
import play.api.libs.json.Json
import com.iotracks.api.listener.IOFogAPIListener
import com.iotracks.elements.IOMessage
import com.typesafe.scalalogging.Logger

import com.hashmapinc.tempus.edge.proto.{MessageProtocols, ConfigMessageTypes}
import com.hashmapinc.tempus.edge.track.proto.TrackConfig

/**
 * This object holds the logic for handling iofog events
 */
object IofogController {
  private val log = Logger(getClass())
}