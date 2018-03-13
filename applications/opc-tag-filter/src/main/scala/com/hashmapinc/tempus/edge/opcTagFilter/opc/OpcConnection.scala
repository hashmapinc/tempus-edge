package com.hashmapinc.tempus.edge.opcTagFilter.opc

import java.io.File
import java.util.Arrays
import scala.util.{Failure, Success, Try}

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

object OpcConnection {
  private val logger = Logger(getClass())

  //create client
  var client = getUpdatedClient
  if (client.isSuccess) logger.info("Successfully created client.")
  
  /**
   * Uses values in the Config object to create an opcUaClient configuration
   */
  def getClientConfig: OpcUaClientConfig = {
    logger.info("creating opc client configuration...")
    
    //=========================================================================
    // security configs
    //=========================================================================
    val securityTempDir = new File(System.getProperty("java.io.tmpdir"), "security")
    if (!securityTempDir.exists() && !securityTempDir.mkdirs()) {
      throw new Exception("unable to create security dir: " + securityTempDir)
    }
    logger.info("security temp dir: {}", securityTempDir.getAbsolutePath())

    // TODO: Implement other securityPolicy options
    val securityPolicy = SecurityPolicy.None 
    //=========================================================================

    //=========================================================================
    // endpoint configs
    //=========================================================================
    val endpoints= Try({
      UaTcpStackClient.getEndpoints(Config.trackConfig.get.getOpcConfig.endpoint).get
    }).recoverWith({
      case e: Exception => {
        // try the explicit discovery endpoint as well
        val discoveryUrl = Config.trackConfig.get.getOpcConfig.endpoint + "/discovery"
        logger.info("Trying explicit discovery URL: {}", discoveryUrl)
        Success(UaTcpStackClient.getEndpoints(discoveryUrl).get)
      }
    })

    val endpoint = Arrays.stream(endpoints.get).
      filter(e => e.getSecurityPolicyUri().equals(securityPolicy.getSecurityPolicyUri())).
      findFirst().orElseThrow(()=> new Exception("no desired endpoints returned"))

    logger.info("Using endpoint: {} [{}]", endpoint.getEndpointUrl(), securityPolicy)
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
   * Gets the latest opc configuration and updates the client with a new instance.
   */
  def getUpdatedClient: Try[OpcUaClient] = {
    logger.info("creating new opc client..")
    val updatedClient = Try(new OpcUaClient(getClientConfig))
    if (updatedClient.isFailure) {
      logger.error("unable to create opc client." + 
        " Will retry when new configuration arrives...")
    }

    //return updated client
    updatedClient
  }

  /**
   * updates the client attribute to the latest client instance with latest opc configuration
   */
  def updateClient: Unit = {
    client = getUpdatedClient
  }
}