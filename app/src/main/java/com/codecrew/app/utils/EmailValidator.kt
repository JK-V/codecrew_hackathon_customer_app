package com.codecrew.app.utils

import java.util.regex.Pattern

class EmailValidator {

    companion object {
        private val EMAIL_ADDRESS_PATTERN: Pattern = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )
    }

    fun isValid(email: String): Boolean {
        if (email.isBlank()) return false
        return EMAIL_ADDRESS_PATTERN.matcher(email).matches()
    }
}