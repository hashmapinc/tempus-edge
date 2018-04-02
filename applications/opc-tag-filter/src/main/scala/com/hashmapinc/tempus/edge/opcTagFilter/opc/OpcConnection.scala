package com.hashmapinc.tempus.edge.opcTagFilter.opc

import scala.util.Try

import org.eclipse.milo.opcua.sdk.client.OpcUaClient
import org.eclipse.milo.opcua.sdk.client.api.config.{OpcUaClientConfig, OpcUaClientConfigBuilder}
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription
import org.eclipse.milo.opcua.stack.client.UaTcpStackClient
import com.typesafe.scalalogging.Logger

import com.hashmapinc.tempus.edge.opcTagFilter.Config
import com.hashmapinc.tempus.edge.proto.OpcConfig

object OpcConnection {
  private val log = Logger(getClass())

  //create client
  var client: Option[OpcUaClient] = None
  
  /**
   *  Creates an opcUaClient configuration
   *
   *  This function is not fault tolerant. The caller should handle errors.
   *
   *  @param opcConf           - OpcConfig proto object with opc configuration to use
   *
   *  @return opcClientConfig  - OpcUaClientConfig created from opcConf
   */
  def getClientConfig (
    opcConf: OpcConfig
  ): OpcUaClientConfig = {
    log.info("creating opc client configuration...")

    //=========================================================================
    // setup endpoint
    //=========================================================================
    val securityPolicy = OpcSecurity.getSecurityPolicy(opcConf)
    val opcEndpoint = opcConf.endpoint   

    // get all endpoints available at opcEndpoint that match the securityPolicy
    val endpoints = Try({
      UaTcpStackClient.getEndpoints(opcEndpoint).get
    }).recover({
      case e: Exception => {
        // try the explicit discovery endpoint as well
        val discoveryUrl = opcEndpoint + "/discovery"
        log.info("Trying explicit discovery URL: {}", discoveryUrl)
        UaTcpStackClient.getEndpoints(discoveryUrl).get
      }
    }).get.filter(_.getSecurityPolicyUri == securityPolicy.getSecurityPolicyUri)

    // TODO: Filter for securityMode as well
    // get endpoint from filtered endpoints
    val endpoint = Try(endpoints(1))
    if (endpoint.isSuccess) 
      log.info("Using endpoint: {} [{}]", endpoint.get.getEndpointUrl(), securityPolicy)
    else {
      log.error("No endpoints with proper security policies were found for securityPolicy = {} at opcEndpoint = ", securityPolicy, opcEndpoint)
      throw new Exception("Could not connect")
    }
    //=========================================================================

    // return config
    OpcUaClientConfig.builder()
      .setApplicationName(LocalizedText.english("hashmapinc tempus edge opc client"))
      .setApplicationUri("urn:hashmapinc:tempus:edge:opc-client")
      .setCertificate(OpcSecurity.getClientCertificate(opcConf))
      .setKeyPair(OpcSecurity.getClientKeyPair(opcConf))
      .setEndpoint(endpoint.get)
      .setIdentityProvider(OpcSecurity.getIdentityProvider(opcConf))
      .setRequestTimeout(uint(5000))
      .build
  }

  /**
   * Creates a new opc client.
   *
   * @param opcConf    - OpcConfig proto object with opc configuration to use
   *
   * @return opcClient - new opc client
   */
  def createOpcClient (
    opcConf: OpcConfig
  ): OpcUaClient = {
    log.info("creating new opc client..")
    new OpcUaClient(getClientConfig(opcConf))
  }

  /**
   * recursion utility for retrying client updating
   *
   * @param updatedClient           - Try[OpcUaClient] from the previous caller
   * @param remainingAttempts       - Int value of remaining retry attempts
   *
   * @return successfulUpdateClient - Try[OpcUaClient] result of updateClient attempt
   */
  @scala.annotation.tailrec
  def recursiveUpdateClient(
    updatedClient:      Try[OpcUaClient],
    remainingAttempts:  Int
  ): Try[OpcUaClient] = {
    if (updatedClient.isSuccess || remainingAttempts < 1) updatedClient else {
      log.error("Could not update OPC client. Received error: " + updatedClient.failed.get)
      log.error("Will retry connection {} more time(s) in {} milliseconds...", remainingAttempts, Config.OPC_RECONN_DELAY)
      Thread.sleep(Config.OPC_RECONN_DELAY)
      recursiveUpdateClient(Try(createOpcClient(Config.trackConfig.get.getOpcConfig)), remainingAttempts - 1)
    }
  }

  /**
   *  updates the client attribute to the latest client instance with latest 
   *  opc configuration.
   *
   *  If client creation fails, updateClient will retry every 
   *  Config.OPC_RECONN_DELAY milliseconds until it succeeds.
   */
  def updateClient: Unit = {
    this.synchronized {
      // destroy the existing client if it exists
      if (client.isDefined) {
        client.get.getSubscriptionManager.clearSubscriptions
        client.get.disconnect
      }

      // get latest local opcConfigs
      val opcConf = Try(Config.trackConfig.get.getOpcConfig)

      // if opcConfig exists and has an endpoint, let's connect!
      if (opcConf.isFailure || opcConf.get.endpoint.isEmpty)
        log.error("Could not update OPC client: no suitable OPC Configuration found. Will retry when new configs arrive.")
      else {
        client = recursiveUpdateClient(Try(createOpcClient(opcConf.get)), Config.OPC_RECONN_MAX_ATTEMPTS).toOption
        if (client.isDefined)
          log.info("OPC client successfully updated!")
        else 
          log.warn("OPC client update was unsuccessful. Will retry when new OPC configs arrive.")
      }
    }
  }
}