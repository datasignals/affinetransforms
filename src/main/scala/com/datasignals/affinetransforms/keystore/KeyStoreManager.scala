package com.datasignals.affinetransforms.keystore

import java.security.cert.Certificate
import java.security.{Key, PrivateKey}
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.net.ssl.{KeyManager, KeyManagerFactory, SSLContext}

class KeyStoreManager(info: KeyStoreInfo, format: String = "PKCS12")
  extends AbstractKeyStoreManager(info, format) {

  def keyManagerFactory(password: Array[Char]): KeyManagerFactory = {
    val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm)
    kmf.init(keyStore, password)
    kmf
  }

  def setKeyEntry(keyInfo:KeyInfo, key: PrivateKey, chain: Array[Certificate]): Unit =
    keyStore.setKeyEntry(keyInfo.alias, key, keyInfo.password, chain)

  def setKeyEntry(keyInfo:KeyInfo, key: PrivateKey, cert: Certificate): Unit =
    keyStore.setKeyEntry(keyInfo.alias, key, keyInfo.password, Array(cert))

  def setKeyEntry(keyInfo:KeyInfo, key: SecretKey): Unit =
    keyStore.setKeyEntry(keyInfo.alias, key, keyInfo.password, null)

  def setRawKeyEntry(keyInfo: KeyInfo, bytes: Array[Byte]): Unit =
    setKeyEntry(keyInfo, new SecretKeySpec(bytes, "AES"))

  def getKey(keyInfo: KeyInfo): Key = keyStore.getKey(keyInfo.alias, keyInfo.password)

  def getRawKey(keyInfo: KeyInfo): Array[Byte] = getKey(keyInfo).getEncoded

  @deprecated("Use SSLContextFactory instead", "blumamba-util-keystore v0.2.0")
  def sslContext(version: String = "TLSv1.2", password: Array[Char]): SSLContext = {
    val context: SSLContext = SSLContext.getInstance(version)
    val kms = keyManagers(password)
    context.init(kms, null, rnd)
    context
  }

  private[keystore] def keyManagers(password: Array[Char]): Array[KeyManager] =
    keyManagerFactory(password).getKeyManagers

}

