package com.iyas.budgetin.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.userProfileChangeRequest

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val isAccountDeleted: Boolean = false
)

class AuthViewModel(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val isLoggedIn: Boolean get() = auth.currentUser != null

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState(error = "Email dan password tidak boleh kosong")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            try {
                auth.signInWithEmailAndPassword(email.trim(), password).await()
                _uiState.value = AuthUiState(isSuccess = true)
            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("no user record") == true -> "Email tidak terdaftar"
                    e.message?.contains("password is invalid") == true -> "Password salah"
                    e.message?.contains("badly formatted") == true -> "Format email tidak valid"
                    else -> e.message ?: "Login gagal, coba lagi"
                }
                _uiState.value = AuthUiState(error = msg)
            }
        }
    }

    fun register(email: String, password: String, confirmPassword: String, nickname: String) {
        if (email.isBlank() || password.isBlank() || nickname.isBlank()) {
            _uiState.value = AuthUiState(error = "Email, password, dan nama tidak boleh kosong")
            return
        }
        if (password != confirmPassword) {
            _uiState.value = AuthUiState(error = "Password tidak cocok")
            return
        }
        if (password.length < 6) {
            _uiState.value = AuthUiState(error = "Password minimal 6 karakter")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            try {
                auth.createUserWithEmailAndPassword(email.trim(), password).await()
                val user = auth.currentUser
                if (user != null) {
                    val profileUpdates = userProfileChangeRequest {
                        displayName = nickname.trim()
                    }
                    user.updateProfile(profileUpdates).await()
                }
                _uiState.value = AuthUiState(isSuccess = true)
            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("email address is already") == true -> "Email sudah digunakan"
                    e.message?.contains("badly formatted") == true -> "Format email tidak valid"
                    else -> e.message ?: "Registrasi gagal, coba lagi"
                }
                _uiState.value = AuthUiState(error = msg)
            }
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun deleteAccount(password: String) {
        val user = auth.currentUser
        val email = user?.email
        if (user == null || email == null) {
            _uiState.value = AuthUiState(error = "Tidak ada pengguna yang login")
            return
        }
        if (password.isBlank()) {
            _uiState.value = AuthUiState(error = "Password tidak boleh kosong")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            try {
                val credential = EmailAuthProvider.getCredential(email, password)
                user.reauthenticate(credential).await()
                user.delete().await()
                _uiState.value = AuthUiState(isAccountDeleted = true)
            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("password is invalid") == true -> "Password salah"
                    e.message?.contains("wrong-password") == true -> "Password salah"
                    e.message?.contains("INVALID_LOGIN_CREDENTIALS") == true -> "Password salah"
                    else -> e.message ?: "Gagal menghapus akun"
                }
                _uiState.value = AuthUiState(error = msg)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
