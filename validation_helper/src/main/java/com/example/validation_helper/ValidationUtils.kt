package com.example.validation_helper

import android.util.Patterns
import java.util.regex.Pattern

object ValidationUtils {

    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun isValidName(name: String): Boolean {
        val nameRegex = "^[a-zA-Zא-תבגדהוזחטיכלמנסעפצקרשתםןץףך\\s]{2,}\$"
        return Pattern.matches(nameRegex, name)
    }

    fun isValidPhoneNumber(phone: String): Boolean {
        val phoneRegex = "^05\\d{8}$"
        return Pattern.matches(phoneRegex, phone)
    }

    fun isValidIsraeliId(id: String): Boolean {
        val trimmedId = id.trim()
        return trimmedId.length == 9 && trimmedId.all { it.isDigit() }
    }
}