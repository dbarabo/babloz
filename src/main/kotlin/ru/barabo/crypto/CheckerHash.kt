package ru.barabo.crypto

import sun.misc.BASE64Encoder
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec

object CheckerHash {

    private val cipher = Cipher.getInstance("DES")

    init {
        val keySpec = SecretKeyFactory.getInstance("DES").generateSecret(
                DESKeySpec("babloziki Phrase".toByteArray() ) )

        cipher.init(Cipher.ENCRYPT_MODE, keySpec)
    }

    fun toHashPassword(password: String): String = BASE64Encoder().encode(cipher.doFinal(password.toByteArray()) )
}