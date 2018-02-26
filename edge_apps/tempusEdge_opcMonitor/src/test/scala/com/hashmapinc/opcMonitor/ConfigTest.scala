package com.hashmapinc.opcMonitor

import org.scalatest.FlatSpec

class ConfigTest extends FlatSpec {

  "Config" should "start with no config" in {
    assert(!Config.iofogConfig.isDefined)
  }

  it should "start with 'production' context" in {
    assert(Config.context == "production")
  }

  it should "accept and retain updates" in {
    val opcEndpoint = "opcHost:80/endpoint"

    val newConfig = new IofogConfig(opcEndpoint)

    Config.update(newConfig)

    assert(Config.iofogConfig.get.opcEndpoint == opcEndpoint)
    assert(Config.context == "production")
  }

  it should "accept and retain test configs" in {
    val opcEndpoint = "opcHost:80/endpoint"

    val newConfig = new IofogConfig(opcEndpoint)

    Config.initTestContext(newConfig)

    assert(Config.iofogConfig.get.opcEndpoint == opcEndpoint)
    assert(Config.context == "test")
  }

  it should "reset to default configs when 'reset' is invoked" in {
    Config.reset
    assert(Config.context == "production")
    assert(!Config.iofogConfig.isDefined)
  }
}