package com.codecrew.app.login

import com.codecrew.app.utils.UserPreferences
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.codecrew.app.utils.EmailValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val appApplication = getApplication<Application>()

    init {
        val registeredEmail = UserPreferences.getRegisteredEmail(appApplication.applicationContext)
        if (registeredEmail != null) {
            _uiState.update { it.copy(email = registeredEmail) }
        }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { currentState ->
            currentState.copy(
                email = email,
                emailError = null,
                loginError = null
            )
        }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { currentState ->
            currentState.copy(
                password = password,
                passwordError = null,
                loginError = null
            )
        }
    }

    fun attemptLogin() {
        if (!validateInputs()) {
            return
        }

        _uiState.update { it.copy(isLoginInProgress = true, loginError = null) }

        viewModelScope.launch {
            try {
                kotlinx.coroutines.delay(1500) // Simulate network/processing delay

                val enteredEmail = _uiState.value.email

                val registeredEmail =
                    UserPreferences.getRegisteredEmail(appApplication.applicationContext)

                if (registeredEmail != null && registeredEmail == enteredEmail) {
                    _uiState.update {
                        it.copy(
                            isLoginInProgress = false,
                            showPreferredDeviceDialog = true // Show dialog on successful "validation"
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoginInProgress = false,
                            loginError = "Invalid email or password."
                        )
                    }
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoginInProgress = false,
                        loginError = e.message ?: "An unknown login error occurred."
                    )
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        val email = _uiState.value.email
        val password = _uiState.value.password
        var isValid = true
        var emailError: String? = null
        var passwordError: String? = null

        if (email.isBlank()) {
            emailError = "Email cannot be empty"
            isValid = false
        } else if (!EmailValidator().isValid(email)) {
            emailError = "Invalid email format"
            isValid = false
        }

        if (password.isBlank()) {
            passwordError = "Password cannot be empty"
            isValid = false
        }
        _uiState.update { currentState ->
            currentState.copy(
                emailError = emailError,
                passwordError = passwordError
            )
        }
        return isValid
    }

    fun onPreferredDeviceDialogDismissed() {
        _uiState.update { it.copy(showPreferredDeviceDialog = false) }
    }

    fun onPreferredDeviceConfirmed(isPreferred: Boolean) {
        _uiState.update {
            it.copy(
                showPreferredDeviceDialog = false,
                isLoginSuccessful = true // Now mark login as fully successful
            )
        }
    }

    fun onLoginNavigationConsumed() {
        _uiState.update { it.copy(isLoginSuccessful = false) }
    }
}