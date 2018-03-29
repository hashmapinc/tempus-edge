package com.hashmapinc.tempus.edge.opcTagFilter.opc

import java.io.File
import scala.util.Try

import com.typesafe.scalalogging.Logger
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy

import com.hashmapinc.tempus.edge.proto.OpcConfig

object OpcSecurity {
  private val log = Logger(getClass())
  
  /**
   * Creates a opcUaClient configuration
   *
   * @param opcConf - OpcConfig proto object with opc configuration to use
   *
   * @return securityPolicy - SecurityPolicy created from opcConf
   */
  def getSecurityPolicy (
    opcConf: OpcConfig
  ): SecurityPolicy = {
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
      if (opcConf.securityType == OpcConfig.SecurityType.NONE) SecurityPolicy.None
      else {
        SecurityPolicy.None
      }
    
    securityPolicy
  }
}