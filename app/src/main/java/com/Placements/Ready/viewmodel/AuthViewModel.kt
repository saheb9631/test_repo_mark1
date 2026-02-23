package com.Placements.Ready.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Placements.Ready.data.AuthManager
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val authManager = AuthManager()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    val currentUser: StateFlow<FirebaseUser?> = authManager.authStateFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = authManager.currentUser
        )

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        data class Success(val user: FirebaseUser) : AuthState()
        data class Error(val message: String) : AuthState()
    }

    fun signInWithEmail(email: String, password: String) {
        // Prevent duplicate requests
        if (_authState.value is AuthState.Loading) return
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authManager.signIn(email, password)
            result.fold(
                onSuccess = { _authState.value = AuthState.Success(it) },
                onFailure = { e ->
                    Log.e("AuthViewModel", "Sign in failed for $email: ${e.message}", e)
                    _authState.value = AuthState.Error(e.message ?: "Login failed")
                }
            )
        }
    }

    fun signUpWithEmail(email: String, password: String) {
        if (_authState.value is AuthState.Loading) return
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authManager.signUp(email, password)
            result.fold(
                onSuccess = { _authState.value = AuthState.Success(it) },
                onFailure = { e ->
                    Log.e("AuthViewModel", "Sign up failed for $email: ${e.message}", e)
                    _authState.value = AuthState.Error(e.message ?: "Sign up failed")
                }
            )
        }
    }

    fun signInWithGoogle(idToken: String) {
        if (_authState.value is AuthState.Loading) return
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authManager.signInWithGoogle(idToken)
            result.fold(
                onSuccess = { _authState.value = AuthState.Success(it) },
                onFailure = { e ->
                    Log.e("AuthViewModel", "Google login failed: ${e.message}", e)
                    _authState.value = AuthState.Error(e.message ?: "Google login failed")
                }
            )
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun signOut(context: android.content.Context) {
        authManager.signOut(context)
        _authState.value = AuthState.Idle
    }

    suspend fun sendVerificationEmail(): Result<Unit> {
        val result = authManager.sendVerificationEmail()
        result.onFailure { error ->
            Log.e("AuthViewModel", "Failed to send verification email: ${error.message}", error)
        }
        return result
    }

    suspend fun reloadUser(): Result<Boolean> {
        return authManager.reloadUser()
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return authManager.sendPasswordResetEmail(email)
    }
}
