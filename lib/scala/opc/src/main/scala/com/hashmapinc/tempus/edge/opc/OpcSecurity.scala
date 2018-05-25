package com.hashmapinc.tempus.edge.opc

import java.io.FileInputStream
import java.security.{KeyPair, KeyPairGenerator}
import java.security.cert.{X509Certificate, CertificateFactory}
import scala.util.Try

import com.typesafe.scalalogging.Logger
import org.eclipse.milo.opcua.sdk.client.api.identity.{IdentityProvider, AnonymousProvider, UsernameProvider}
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode
import org.eclipse.milo.opcua.stack.core.util.CryptoRestrictions
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateBuilder
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateGenerator

import com.hashmapinc.tempus.edge.proto.OpcConfig

object OpcSecurity {
  private val log = Logger(getClass())

  // TODO: Replace this with java keystore logic at some point or maybe some kind of privisioning logic
  // Generate keypair and certificate at run time
  val clientKeyPair = SelfSignedCertificateGenerator.generateRsaKeyPair(2048)
  val clientCertificate = (new SelfSignedCertificateBuilder(clientKeyPair)
    .setCommonName("Tempus Edge OPC Library")
    .setOrganization("hashmapinc")
    .setOrganizationalUnit("tempus edge")
    .setLocalityName("Atlanta")
    .setStateName("GA")
    .setCountryCode("US")
    .setApplicationUri("urn:hashmapinc:tempus:edge:opc")
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

    opcConf.clientIdentity match {
      case "" => new AnonymousProvider() // no id provided, connect anonymously
      case _  => new UsernameProvider(opcConf.clientIdentity, opcConf.clientPassword)
    }
  }
}