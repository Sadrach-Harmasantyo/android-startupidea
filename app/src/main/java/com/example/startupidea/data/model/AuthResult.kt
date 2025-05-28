package com.example.startupidea.data.model

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Loading : AuthResult()
    object EmailConfirmationNeeded : AuthResult()
}