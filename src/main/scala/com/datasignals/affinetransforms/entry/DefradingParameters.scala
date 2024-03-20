package com.datasignals.affinetransforms.entry

import com.datasignals.affinetransforms.entry.Bits.LOG_LONG_BYTES
import com.datasignals.affinetransforms.keystore.{KeyInfo, KeyStoreManager}

object DefradingParameters {

  private val Defrading = "defrading"
  private val Shift = "shift"
  private val Encrypt = "encrypt"

  def apply(dim: Int, keyStoreManager: KeyStoreManager, defradingPassword: String, shiftPassword: String, encryptPassword: String): DefradingParameters = {
    val d = dim << LOG_LONG_BYTES
    val inverseMatrix = new Array[Long](dim * dim)
    Bits.getLongs(
      inverseMatrix, 0,
      keyStoreManager.getRawKey(KeyInfo(Defrading, defradingPassword.toCharArray)), 0, dim * d
    )

    val shift = new Array[Long](dim)
    Bits.getLongs(shift, 0,
      keyStoreManager.getRawKey(KeyInfo(Shift, shiftPassword.toCharArray)), 0, d)
    val key = keyStoreManager
      .getRawKey(KeyInfo(Encrypt, encryptPassword.toCharArray))

    new DefradingParameters(inverseMatrix, shift, key)
  }

}

class DefradingParameters(val matrix: Array[Long], val shift: Array[Long], val key: Array[Byte])
