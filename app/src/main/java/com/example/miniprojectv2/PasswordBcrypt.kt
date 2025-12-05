package com.example.miniprojectv2

import at.favre.lib.crypto.bcrypt.BCrypt

object PasswordBcrypt {

    // Hash password saat register
    fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }

    // Verifikasi password saat login
    fun verifyPassword(password: String, hashed: String): Boolean {
        val result = BCrypt.verifyer().verify(password.toCharArray(), hashed)
        return result.verified
    }


}
