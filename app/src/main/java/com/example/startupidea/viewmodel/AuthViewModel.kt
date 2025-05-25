package com.example.startupidea.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.startupidea.data.AuthResult
import com.example.startupidea.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()
    
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn
    
    private val _authResult = MutableStateFlow<AuthResult>(AuthResult.Success)
    val authResult: StateFlow<AuthResult> = _authResult
    
    // Untuk menyimpan email yang perlu dikonfirmasi
    private val _pendingConfirmationEmail = MutableStateFlow<String?>(null)
    val pendingConfirmationEmail: StateFlow<String?> = _pendingConfirmationEmail
    
    init {
        // Inisialisasi status login dari repository
        viewModelScope.launch {
            _isLoggedIn.value = repository.isLoggedIn.value
        }
    }
    
    // Fungsi baru untuk memeriksa status login
    fun checkLoginStatus() {
        viewModelScope.launch {
            _isLoggedIn.value = repository.checkAndUpdateSession()
        }
    }
    
    suspend fun signIn(email: String, password: String): Boolean {
        _authResult.value = AuthResult.Loading
        
        return when (val result = repository.signIn(email, password)) {
            is AuthResult.Success -> {
                _isLoggedIn.value = true
                _authResult.value = result
                true
            }
            is AuthResult.EmailConfirmationNeeded -> {
                _pendingConfirmationEmail.value = email
                _authResult.value = result
                false
            }
            is AuthResult.Error -> {
                _authResult.value = result
                false
            }
            else -> {
                _authResult.value = AuthResult.Error("Terjadi kesalahan")
                false
            }
        }
    }
    
    suspend fun signUp(email: String, password: String): Boolean {
        _authResult.value = AuthResult.Loading
        
        return when (val result = repository.signUp(email, password)) {
            is AuthResult.EmailConfirmationNeeded -> {
                _pendingConfirmationEmail.value = email
                _authResult.value = AuthResult.EmailConfirmationNeeded
                true
            }
            is AuthResult.Error -> {
                val errorMsg = when {
                    result.message.contains("User already registered", ignoreCase = true) -> 
                        "Email sudah terdaftar. Silakan login."
                    result.message.contains("Password should be", ignoreCase = true) -> 
                        "Password tidak memenuhi persyaratan. Gunakan minimal 6 karakter."
                    result.message.contains("network", ignoreCase = true) -> 
                        "Koneksi internet terputus. Periksa koneksi Anda."
                    else -> "Pendaftaran gagal: ${result.message}"
                }
                _authResult.value = AuthResult.Error(errorMsg)
                false
            }
            else -> {
                _authResult.value = AuthResult.Error("Terjadi kesalahan")
                false
            }
        }
    }
    
    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
            _isLoggedIn.value = false
            _authResult.value = AuthResult.Success
            _pendingConfirmationEmail.value = null
        }
    }
    
    fun clearPendingConfirmation() {
        _pendingConfirmationEmail.value = null
        _authResult.value = AuthResult.Success
    }
}