package com.hashmapinc.tempus.edge.opcClient.opc

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

import com.hashmapinc.tempus.edge.opcClient.Config
import com.hashmapinc.tempus.edge.proto.OpcConfig

object OpcConnection {
  private val log = Logger(getClass())

  //create client
  var client: Option[OpcUaClient] = None
  
  /**
   * Creates a opcUaClient configuration
   *
   * @param opcEndpoint - String holding the opc endpoint to connect to
   * @param securityType - SecurityType protobuf object describing the security to use
   */
  def getClientConfig (
    opcEndpoint: String,
    securityType: OpcConfig.SecurityType
  ): OpcUaClientConfig = {
    log.info("creating opc client configuration...")
    
    //=========================================================================
    // security configs
    //=========================================================================
    val securityTempDir = new File(System.getProperty("java.io.tmpdir"), "security")
    if (!securityTempDir.exists() && !securityTempDir.mkdirs()) {
      throw new Exception("unable to create security dir: " + securityTempDir)
    }
    log.info("security temp dir: {}", securityTempDir.getAbsolutePath())

    // TODO: Implement other securityPolicy options
    val securityPolicy = 
      if (securityType == OpcConfig.SecurityType.NONE) SecurityPolicy.None
      else {
        log.error("could not implement securityType: " + securityType)
        throw new Exception("unable to implement securityType: " + securityType)
      }

    //=========================================================================

    //=========================================================================
    // endpoint configs
    //=========================================================================
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
   * @param opcEndpoint - String holding the opc endpoint to connect to
   * @param securityType - SecurityType protobuf object describing the 
   *                       security to use
   *
   * @return opcClient - new opc client
   */
  def createOpcClient (
    opcEndpoint: String,
    securityType: OpcConfig.SecurityType
  ): OpcUaClient = {
    log.info("creating new opc client..")
    new OpcUaClient(getClientConfig(opcEndpoint, securityType))
  }

  /**
   *  updates the client attribute to the latest client instance with latest 
   *  opc configuration.
   *
   *  If client creation fails, updateClient will retry every 
   *  Config.OPC_RECONN_DELAY mSeconds until it succeeds.
   */
  def updateClient: Unit = {
    val endpoint      = Try(Config.trackConfig.get.getOpcConfig.endpoint).getOrElse("")
    val securityType  = Try(Config.trackConfig.get.getOpcConfig.securityType).getOrElse(OpcConfig.SecurityType.NONE)

    if (endpoint.isEmpty)
      log.error("No OPC endpoint defined. Cannot create opc client. Will retry on new configuration.")
    else {
      var updatedClient = Try(Option(createOpcClient(endpoint, securityType)))

      while (updatedClient.isFailure) { // TODO: this feels not Scala-y. Find a way to do this elegantly
        log.error("unable to update opc client. Will retry in {} milliseconds...", Config.OPC_RECONN_DELAY)
        Thread.sleep(Config.OPC_RECONN_DELAY)
        updatedClient = Try({
          val endpoint = Config.trackConfig.get.getOpcConfig.endpoint
          val securityType = Config.trackConfig.get.getOpcConfig.securityType
          Option(createOpcClient(endpoint, securityType))
        })
      }
      
      log.info("Successfully updated client.")
      this.synchronized {
        client = updatedClient.getOrElse(None)
      }
    }
  }

  /**
   * This function subscribs the opc client to the tag given
   *
   * @param tag - String value of the tag to subscribe to
   */
  def subscribe(
    tag: String
  ): Unit = {

  }
}