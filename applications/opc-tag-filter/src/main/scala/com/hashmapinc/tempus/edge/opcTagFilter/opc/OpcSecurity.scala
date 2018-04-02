package com.hashmapinc.tempus.edge.opcTagFilter.opc

import java.io.FileInputStream
import java.security.{KeyPair, KeyPairGenerator}
import java.security.cert.{X509Certificate, CertificateFactory}
import scala.util.Try

import com.typesafe.scalalogging.Logger
import org.eclipse.milo.opcua.sdk.client.api.identity.{IdentityProvider, AnonymousProvider}
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode
import org.eclipse.milo.opcua.stack.core.util.CryptoRestrictions
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateBuilder
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateGenerator

import com.hashmapinc.tempus.edge.proto.OpcConfig
import com.hashmapinc.tempus.edge.opcTagFilter.Config

object OpcSecurity {
  private val log = Logger(getClass())

  val clientKeyPair = SelfSignedCertificateGenerator.generateRsaKeyPair(2048)
  val certificate = (new SelfSignedCertificateBuilder(clientKeyPair)
    .setCommonName("Tempus Edge OPC Tag Filter")
    .setOrganization("hashmapinc")
    .setOrganizationalUnit("tempus edge")
    .setLocalityName("Atlanta")
    .setStateName("GA")
    .setCountryCode("US")
    .setApplicationUri("urn:hashmapinc:tempus:edge:opc-client")
  ).build
  
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
    log.info("Getting OPC security policy...")

    // Create security policy based on opcConf
    opcConf.securityType match {
      case OpcConfig.SecurityType.NONE            => SecurityPolicy.None
      case OpcConfig.SecurityType.BASIC128RSA15   => SecurityPolicy.Basic128Rsa15
      case OpcConfig.SecurityType.BASIC256        => SecurityPolicy.Basic256
      case OpcConfig.SecurityType.BASIC256SHA256  => SecurityPolicy.Basic256Sha256
      case _ => {
        log.warn("Could not parse OpcConfig Security Type: {}. NONE will be used.", opcConf.securityType)
        SecurityPolicy.None
      }
    }
  }

  /**
   * Creates a securityMode from opcConf
   *
   * @param opcConf - OpcConfig proto object with opc configuration to use
   *
   * @return MessageSecurityMode - MessageSecurityMode created from opcConf
   */
  def getSecurityMode (
    opcConf: OpcConfig
  ): MessageSecurityMode = {
    log.info("Getting OPC security mode...")

    // Create security mode based on opcConf
    opcConf.securityType match {
      case OpcConfig.SecurityType.NONE            => MessageSecurityMode.None
      case OpcConfig.SecurityType.BASIC128RSA15   => MessageSecurityMode.SignAndEncrypt
      case OpcConfig.SecurityType.BASIC256        => MessageSecurityMode.SignAndEncrypt
      case OpcConfig.SecurityType.BASIC256SHA256  => MessageSecurityMode.SignAndEncrypt
      case _ => {
        log.warn("Could not parse OpcConfig Security Mode from Type: {}. MessageSecurityMode.None will be used.", opcConf.securityType)
        MessageSecurityMode.None
      }
    }
  }

  /**
   * Creates an identity provider from opcConf
   *
   * @param opcConf - OpcConfig proto object with opc configuration to use
   *
   * @return IdentityProvider - IdentityProvider created from opcConf
   */
  def getIdentityProvider (
    opcConf: OpcConfig
  ): IdentityProvider = {
    log.info("Getting OPC security policy...")
    new AnonymousProvider()
  }

  /**
   * Loads a client key pair
   *
   * @param opcConf - OpcConfig proto object with opc configuration to use
   *
   * @return KeyPair - KeyPair created from opcConf
   */
  def getClientKeyPair (
    opcConf: OpcConfig
  ): KeyPair = {
    log.info("Getting OPC client key pair...")
    // TODO: Load from keystore
    clientKeyPair
  }

  /**
   * Loads an X509Certificate
   *
   * @param opcConf - OpcConfig proto object with opc configuration to use
   *
   * @return X509Certificate - X509Certificate created from opcConf
   */
  def getClientCertificate (
    opcConf: OpcConfig
  ): X509Certificate = {
    log.info("Getting OPC client certificate...")
    // TODO: Load from keystore
    /**
    log.info("Attempting client certificate load from {}", Config.CERTIFICATE_PATH)
    
    val certInputStream = new FileInputStream(Config.CERTIFICATE_PATH)
    CertificateFactory
      .getInstance("X.509")
      .generateCertificate(certInputStream)
      .asInstanceOf[X509Certificate]
    */
    certificate
  }
}