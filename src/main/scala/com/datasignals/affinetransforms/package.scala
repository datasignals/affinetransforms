package com.datasignals.affinetransforms

import com.datasignals.affinetransforms.entry.Record

import java.io.{InputStream, OutputStream}
import java.nio.file.{Files, Path}
import java.security.{KeyStore, SecureRandom, Security}
import scala.reflect.runtime.universe

//import com.typesafe.scalalogging.LazyLogging
import org.bouncycastle.jce.provider.BouncyCastleProvider
import sun.security.x509.X500Name

package object keystore /*extends LazyLogging*/ {

  Security.addProvider(new BouncyCastleProvider())

  private[keystore] val rnd: SecureRandom = new SecureRandom()

  sealed trait KeyStoreInfo {
    val password: Array[Char]
  }

  case class KeyStorePathInfo(location: Path, password: Array[Char])
    extends KeyStoreInfo
  case class KeyStoreStreamInfo(istream: InputStream, ostream: OutputStream, password: Array[Char])
    extends KeyStoreInfo

  case class KeyInfo(alias: String, password: Array[Char])
  case class CertInfo(alias: String)
  case class CertAndKeyGenInfo(alg: String, sigAlg: String, length: Int, validSec: Long)
  case class KeyGenInfo(alg: String, length: Int)

    def getX500Name(app: String, org: String, city: String, country: String): X500Name =
      new X500Name(s"CN=$app,O=$org,L=$city,C=$country")

    private[keystore] def openKeyStore(location: Path, password: Array[Char], format: String = "PKCS12"): KeyStore = {
      //Loading keystore from specified file
      //logger.debug("Opening keystore at {}...", location)
      if(!(location eq null) && Files.isReadable(location)) {
        val is = Files.newInputStream(location)
        try {
          loadKeystore(is, password, format)
        } finally {
          is.close()
        }
      } else {
        //logger.debug("Keystore file at {} does not exists or is not readable.", location)
        loadKeystore(null, password, format)
      }
    }

    private[keystore] def loadKeystore(stream: InputStream, password: Array[Char], format: String = "PKCS12"): KeyStore = {
      //logger.debug("Loading keystore from a stream {}...", stream)
      val keyStore = KeyStore.getInstance(format)
      keyStore.load(stream, password)
      //logger.debug("Keystore is loaded from the stream {}.", stream)
      keyStore
    }

    private[keystore] def saveKeyStore(keyStore: KeyStore, location: Path, password: Array[Char]): Unit = {
      if(location eq null) return
      if(Files.exists(location) && !Files.isWritable(location)) return
      val os = Files.newOutputStream(location)
      try {
        storeKeyStore(keyStore, os, password)
      } finally {
        os.close ()
      }
    }

    @inline
    private[keystore] def storeKeyStore(keyStore: KeyStore, stream: OutputStream, password: Array[Char]):Unit = {
      if(stream eq null) return
      keyStore.store(stream, password)
    }
}

package object string {

  private val hexArray: Array[Char] = "0123456789abcdef".toCharArray

  implicit class ImplicitHexBytesString(val s: String) extends AnyVal {

    def toHexBytes: Array[Byte] = {
      val len = s.length
      val data = new Array[Byte](len >> 1)
      for(i <- 0 until len by 2) {
        data(i >> 1) = ((Character.digit(s.charAt(i), 16) << 4)
          + Character.digit(s.charAt(i + 1), 16)).toByte
      }
      data

    }

  }

  implicit class ImplicitHexStringByteArray(val bytes: Array[Byte]) extends AnyVal {

    def toHexString(separator: CharSequence = null): String = {
      if(bytes eq null) return null
      val bLength: Int = bytes.length
      if(bLength == 0) return ""
      val sep: CharSequence = if(separator == null) "" else separator
      val sepLength: Int = sep.length()
      val hexChars: Array[Char] = new Array[Char]((bLength << 1) + sepLength * (bLength - 1))
        for(j <- 0 until bLength) {
          val v: Int = bytes(j) & 0xFF
          val a = hexArray(v >>> 4)
          val b = hexArray(v & 0x0F)

          if(j == 0) {
            hexChars(0) = a
            hexChars(1) = b
          } else {
            val i = (j << 1) + sepLength * (j - 1)
            for (k <- 0 until sepLength) {
              hexChars(i + k) = sep.charAt(k)
            }
            hexChars(i + sepLength) = a
            hexChars(i + sepLength + 1) = b
          }
        }
        new String(hexChars)
    }

  }


}


package object transformation {
  type BlockTransformation = GenericBlockTransformation[Byte, Byte]
  //type BlockWrapper = GenericBlockWrapper[Byte, Byte]
  //type StreamTransformation = GenericStreamTransformation[Byte, Byte]
  //type ArrayTransformation = GenericArrayTransformation[Byte, Byte]
}

package object record {
  final type SemiRawRecord[K] = Record[K, Array[Byte]]
  final type RawRecord = SemiRawRecord[Array[Byte]]
  final type RecordFactory[K, V] = (K, V) => Record[K, V]
  final type SemiRawRecordFactory[K] = RecordFactory[K, Array[Byte]]
  final type RawRecordFactory = SemiRawRecordFactory[Array[Byte]]
}

package object reflection {

  //The following two methods is for a workaround the scala bug https://github.com/scala/bug/issues/8302
  @inline
  def clear(): Unit = universe.asInstanceOf[scala.reflect.runtime.JavaUniverse].undoLog.clear()
  @inline
  def undo[T](block: => T): T = universe.asInstanceOf[scala.reflect.runtime.JavaUniverse].undoLog.undo(block)

}
