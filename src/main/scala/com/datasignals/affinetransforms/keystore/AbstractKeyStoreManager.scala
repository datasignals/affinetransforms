package com.datasignals.affinetransforms.keystore

import java.nio.file.{Files, Path}
import java.security.cert.Certificate
import java.security.{KeyStore, PublicKey}
import scala.collection.mutable.ArrayBuilder

object AbstractKeyStoreManager {

  private[keystore] val DIGEST = new SHA256Digest()

  @inline
  def digest(digest: Digest)(array: Array[Byte]): Array[Byte] = {
    digest.update(array, 0, array.length)
    val result = new Array[Byte](digest.getDigestSize)
    digest.doFinal(result, 0)
    result
  }

}

abstract class AbstractKeyStoreManager(protected[this] val info: KeyStoreInfo, private[this] val format: String)
  extends AutoCloseable {

  protected[this] val keyStore: KeyStore = info match {
    case inf: KeyStorePathInfo => openKeyStore(inf.location, inf.password, format)
    case inf: KeyStoreStreamInfo => loadKeystore(inf.istream, inf.password, format)
  }

  final def getCertificateChain(alias: String): Array[Certificate] = keyStore.getCertificateChain(alias)
  final def getCertificate(alias: String): Certificate = keyStore.getCertificate(alias)
  final def getPublicKey(alias: String): PublicKey = keyStore.getCertificate(alias).getPublicKey

  final def getCertificates: Array[Certificate] = {
    val b = ArrayBuilder.make[Certificate]
    val aliases = keyStore.aliases().asIterator()
    while(aliases.hasNext) {
      val alias = aliases.next()
      val cert = keyStore.getCertificate(alias)
      if(!(cert eq null)) {
        b += cert
      }
    }
    b.result()
  }

  final def getFingerprint(alias: String, digest: Digest): Array[Byte] =
    AbstractKeyStoreManager.digest(digest)(getCertificate(alias).getEncoded)

  final def getFingerprint(alias: String): Array[Byte] = getFingerprint(alias, AbstractKeyStoreManager.DIGEST)

  final def getFingerprints(digest: Digest = new SHA256Digest): Array[Array[Byte]] = {
    val b = ArrayBuilder.make[Array[Byte]]
    val aliases = keyStore.aliases().asIterator()
    while(aliases.hasNext) {
      val alias = aliases.next()
      val cert = keyStore.getCertificate(alias)
      if(!(cert eq null)) {
        b += AbstractKeyStoreManager.digest(digest)(cert.getEncoded)
      }
    }
    b.result()
  }

  def flush(): Unit = {
    info match {
      case inf: KeyStorePathInfo =>
        if(inf.location eq null) return
        val parent: Path = inf.location.getParent
        if(! (parent eq null) && ! Files.exists (parent) ) Files.createDirectories (parent)
        saveKeyStore (keyStore, inf.location, inf.password)
      case inf: KeyStoreStreamInfo =>
        storeKeyStore (keyStore, inf.ostream, inf.password)
    }
  }

  override def close(): Unit = flush()

}
