package com.hashmapinc.tempus.edge.opcTagFilter.opc

import java.io.File
import java.util.Arrays
import scala.util.Try

import org.eclipse.milo.opcua.sdk.client.OpcUaClient
import org.eclipse.milo.opcua.sdk.client.api.config.{OpcUaClientConfig, OpcUaClientConfigBuilder}
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy
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
   * Creates an opcUaClient configuration
   *
   * @param opcConf           - OpcConfig proto object with opc configuration to use
   *
   * @return opcClientConfig  - OpcUaClientConfig created from opcConf
   */
  def getClientConfig (
    opcConf: OpcConfig
  ): OpcUaClientConfig = {
    log.info("creating opc client configuration...")

    // get security policy
    val securityPolicy = OpcSecurity.getSecurityPolicy(opcConf)

    //=========================================================================
    // endpoint configs
    //=========================================================================
    val opcEndpoint = opcConf.endpoint    
    val endpoints= Try({
      UaTcpStackClient.getEndpoints(opcEndpoint).get
    }).recover({
      case e: Exception => {
        // try the explicit discovery endpoint as well
        val discoveryUrl = opcEndpoint + "/discovery"
        log.info("Trying explicit discovery URL: {}", discoveryUrl)
        UaTcpStackClient.getEndpoints(discoveryUrl).get
      }
    }).get

    val endpoint = Arrays.stream(endpoints).
      filter(e => e.getSecurityPolicyUri().equals(securityPolicy.getSecurityPolicyUri())).
      findFirst().orElseThrow(()=> new Exception("no desired endpoints returned"))

    log.info("Using endpoint: {} [{}]", endpoint.getEndpointUrl(), securityPolicy)
    //=========================================================================

    // return config
    OpcUaClientConfig.builder()
      .setApplicationName(LocalizedText.english("hashmapinc tempus edge opc client"))
      .setApplicationUri("urn:hashmapinc:tempus:edge:opc-client")
      .setEndpoint(endpoint)
      .setIdentityProvider(new AnonymousProvider())
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
        val client = recursiveUpdateClient(Try(createOpcClient(opcConf.get)), Config.OPC_RECONN_MAX_ATTEMPTS).toOption
        if (client.isDefined)
          log.info("OPC client successfully updated!")
        else 
          log.warn("OPC client update was unsuccessful. Will retry when new OPC configs arrive.")
      }
    }
  }
}