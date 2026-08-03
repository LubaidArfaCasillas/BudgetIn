package com.iyas.budgetin.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
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
    val isAccountDeleted: Boolean = false,
    val isPasswordChanged: Boolean = false
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
        val email = auth.currentUser?.email
        if (auth.currentUser == null || email == null) {
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
                // Re-authenticate with fresh reference
                auth.currentUser!!.reauthenticate(credential).await()
                // Use fresh reference after re-auth to delete
                auth.currentUser!!.delete().await()
                // Sign out untuk memutus semua listener Firestore
                auth.signOut()
                // Delay agar listener sempat berhenti sebelum navigasi
                delay(300)
                _uiState.value = AuthUiState(isAccountDeleted = true)
            } catch (e: Exception) {
                val errorMsg = e.message ?: ""
                val msg = when {
                    errorMsg.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) -> "Password salah"
                    errorMsg.contains("invalid", ignoreCase = true) && errorMsg.contains("password", ignoreCase = true) -> "Password salah"
                    errorMsg.contains("wrong-password", ignoreCase = true) -> "Password salah"
                    errorMsg.contains("too-many-requests", ignoreCase = true) -> "Terlalu banyak percobaan, coba lagi nanti"
                    errorMsg.contains("network", ignoreCase = true) -> "Koneksi gagal, periksa internet Anda"
                    errorMsg.contains("requires-recent-login", ignoreCase = true) -> "Silakan login ulang lalu coba lagi"
                    else -> "Gagal menghapus akun: $errorMsg"
                }
                _uiState.value = AuthUiState(error = msg)
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, confirmNewPassword: String) {
        val email = auth.currentUser?.email
        if (auth.currentUser == null || email == null) {
            _uiState.value = AuthUiState(error = "Tidak ada pengguna yang login")
            return
        }
        if (currentPassword.isBlank() || newPassword.isBlank()) {
            _uiState.value = AuthUiState(error = "Semua field harus diisi")
            return
        }
        if (newPassword != confirmNewPassword) {
            _uiState.value = AuthUiState(error = "Password baru tidak cocok")
            return
        }
        if (newPassword.length < 6) {
            _uiState.value = AuthUiState(error = "Password baru minimal 6 karakter")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            try {
                val credential = EmailAuthProvider.getCredential(email, currentPassword)
                auth.currentUser!!.reauthenticate(credential).await()
                auth.currentUser!!.updatePassword(newPassword).await()
                _uiState.value = AuthUiState(isPasswordChanged = true)
            } catch (e: Exception) {
                val errorMsg = e.message ?: ""
                val msg = when {
                    errorMsg.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) -> "Password lama salah"
                    errorMsg.contains("invalid", ignoreCase = true) && errorMsg.contains("password", ignoreCase = true) -> "Password lama salah"
                    errorMsg.contains("wrong-password", ignoreCase = true) -> "Password lama salah"
                    errorMsg.contains("too-many-requests", ignoreCase = true) -> "Terlalu banyak percobaan, coba lagi nanti"
                    errorMsg.contains("network", ignoreCase = true) -> "Koneksi gagal, periksa internet Anda"
                    else -> "Gagal mengubah password: $errorMsg"
                }
                _uiState.value = AuthUiState(error = msg)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
