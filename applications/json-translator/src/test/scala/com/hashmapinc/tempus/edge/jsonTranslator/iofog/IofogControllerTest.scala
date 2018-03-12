package com.hashmapinc.tempus.edge.jsonTranslator.iofog

import java.nio.file.{Paths, Files}
import javax.json.Json

import org.scalatest.FlatSpec
import scalapb.json4s.JsonFormat

import com.hashmapinc.tempus.edge.proto._
import com.hashmapinc.tempus.edge.track.proto._

class IofogControllerTest extends FlatSpec {
  //===========================================================================
  // test track config saving and loading from disk
  //===========================================================================
  "jsonToProtobuf" should "be testable" in {
    // check that test config file does not exist
    assert(true)
  }
  //===========================================================================
}