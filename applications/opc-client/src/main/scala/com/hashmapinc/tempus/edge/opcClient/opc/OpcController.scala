package com.hashmapinc.tempus.edge.opcClient.opc

import com.typesafe.scalalogging.Logger

object OpcController {
  private val log = Logger(getClass())

  /** This function drives the subscription updating process
   *
   *  This function uses the local OpcConfig Subscriptions to subscribe
   *  to opc values. This function will result in an opc connection with
   *  the same subscriptions as those present in the local trackConfig.
   */
  def updateSubscriptions: Unit = {
    log.info("Updating subscriptions...")
  }
}