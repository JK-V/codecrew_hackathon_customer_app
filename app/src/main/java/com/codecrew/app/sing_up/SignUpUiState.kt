package com.codecrew.app.sing_up

data class SignUpUiState(
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val confirmPassword: String = "",
    val confirmPasswordError: String? = null,
    val isSignUpInProgress: Boolean = false,
    val signUpError: String? = null,
    val isSignUpSuccessful: Boolean = false
)