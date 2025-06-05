package com.codecrew.app.sing_up


import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.codecrew.app.model.CustomerData
import com.codecrew.app.model.RetrofitClient
import com.codecrew.app.model.RetrofitInterface
import com.codecrew.app.utils.EmailValidator
import com.codecrew.app.utils.UserPreferences
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    private val appApplication = getApplication<Application>()

    fun onEmailChanged(email: String) {
        _uiState.update { currentState ->
            currentState.copy(
                email = email,
                emailError = null,
                signUpError = null
            )
        }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { currentState ->
            currentState.copy(
                password = password,
                passwordError = null,
                signUpError = null
            )
        }
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _uiState.update { currentState ->
            currentState.copy(
                confirmPassword = confirmPassword,
                confirmPasswordError = null,
                signUpError = null
            )
        }
    }

    fun attemptSignUp() {
        if (!validateInputs()) {
            return
        }

        _uiState.update { it.copy(isSignUpInProgress = true, signUpError = null) }

        val customerApi = RetrofitClient.create()

        viewModelScope.launch {
            try {
                val response: CustomerData = customerApi.signupCustomer(
                    customerData = CustomerData(
                    firstName = "Dummiyaapa",
                    lastName = "Dummiyaapa",
                    email = _uiState.value.email,
                    userIdentity = _uiState.value.email,
                    userAccessToken = "This is not available"
                ))

                Log.i("SignupModel", "--Jaye onResponse ${response.toString()}")

                if(response != null){
                    UserPreferences.saveRegisteredEmail(
                        appApplication.applicationContext,
                        _uiState.value.email
                    )
                    UserPreferences.setHasCompletedSignUp(appApplication.applicationContext, true)

                    UserPreferences.setCustId(appApplication.applicationContext, response.custId)

                    _uiState.update {
                        it.copy(
                            isSignUpInProgress = false,
                            isSignUpSuccessful = true
                        )
                    }
                }else{
                    _uiState.update {
                        it.copy(
                            isSignUpInProgress = false,
                            isSignUpSuccessful = false
                        )
                    }
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSignUpInProgress = false,
                        signUpError = e.message ?: "An unknown error occurred",
                        isSignUpSuccessful = false
                    )
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        val email = _uiState.value.email
        val password = _uiState.value.password
        val confirmPassword = _uiState.value.confirmPassword

        var isValid = true
        var emailError: String? = null
        var passwordError: String? = null
        var confirmPasswordError: String? = null

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
        } else if (password.length < 6) {
            passwordError = "Password must be at least 6 characters"
            isValid = false
        }

        if (confirmPassword.isBlank()) {
            confirmPasswordError = "Confirm password cannot be empty"
            isValid = false
        } else if (password != confirmPassword) {
            confirmPasswordError = "Passwords do not match"
            isValid = false
        }

        _uiState.update { currentState ->
            currentState.copy(
                emailError = emailError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError
            )
        }
        return isValid
    }

    fun onSignUpNavigationConsumed() {
        _uiState.update { it.copy(isSignUpSuccessful = false) }
    }
}