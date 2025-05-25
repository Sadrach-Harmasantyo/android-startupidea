package com.example.startupidea.model

import kotlinx.serialization.Serializable

@Serializable
data class StartupIdea(
    val id: String,
    val created_at: String,
    val title: String,
    val description: String,
    val email: String,
    val phone: String,
    val logo_url: String? = null // Field baru untuk menyimpan URL gambar logo
)