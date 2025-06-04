package com.codecrew.app.login

data class LoginUiState(
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val isLoginInProgress: Boolean = false,
    val loginError: String? = null,
    val isLoginSuccessful: Boolean = false,
    val showPreferredDeviceDialog: Boolean = false
)