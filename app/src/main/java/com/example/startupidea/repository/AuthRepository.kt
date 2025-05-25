package com.example.startupidea.repository

import com.example.startupidea.data.SupabaseService
import com.example.startupidea.data.AuthResult
import kotlinx.coroutines.flow.StateFlow

class AuthRepository {
    val isLoggedIn: StateFlow<Boolean> = SupabaseService.isLoggedIn

    suspend fun checkAndUpdateSession(): Boolean = SupabaseService.checkAndUpdateSession()

    suspend fun signIn(email: String, password: String): AuthResult {
        return try {
            val result = SupabaseService.signIn(email, password)
            if (result) AuthResult.Success else AuthResult.Error("Email atau password salah")
        } catch (e: Exception) {
            when {
                e.message?.contains("Email not confirmed", ignoreCase = true) == true -> 
                    AuthResult.EmailConfirmationNeeded
                else -> AuthResult.Error(e.message ?: "Login gagal")
            }
        }
    }

    suspend fun signUp(email: String, password: String): AuthResult {
        return try {
            val result = SupabaseService.signUp(email, password)
            if (result) AuthResult.EmailConfirmationNeeded else AuthResult.Error("Pendaftaran gagal")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Pendaftaran gagal")
        }
    }

    suspend fun signOut() = SupabaseService.signOut()

    suspend fun getCurrentSession() = SupabaseService.getCurrentSession()
} 