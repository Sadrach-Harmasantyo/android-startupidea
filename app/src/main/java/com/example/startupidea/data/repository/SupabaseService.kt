package com.example.startupidea.data.repository

import android.util.Log
import com.example.startupidea.data.model.StartupIdea
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File

object SupabaseService {
    // Ganti dengan URL dan API Key Supabase Anda
    private val supabase = createSupabaseClient(
        supabaseUrl = "https://nsnometnhdfmazfdojnv.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im5zbm9tZXRuaGRmbWF6ZmRvam52Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDc4Mzc1MDgsImV4cCI6MjA2MzQxMzUwOH0.O2-0b-X1YOq8iPS56ymlgesLwfwHF7rPN7j9oQ2RZkc"
    ) {
        install(io.github.jan.supabase.postgrest.Postgrest)
        install(GoTrue)
        install(Storage)
    }

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    init {
        // Periksa status login saat inisialisasi
        _isLoggedIn.value = supabase.gotrue.currentSessionOrNull() != null
    }

    // Fungsi baru untuk memeriksa dan memperbarui sesi
    suspend fun checkAndUpdateSession(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Coba dapatkan sesi saat ini
            val currentSession = supabase.gotrue.currentSessionOrNull()
            
            // Jika sesi ada, perbarui status login
            val isValid = currentSession != null
            
            // Jika sesi ada tapi kedaluwarsa, coba refresh token
            if (isValid && currentSession != null) {
                try {
                    // Cek apakah token perlu di-refresh (jika mendekati kedaluwarsa)
                    // Supabase GoTrue biasanya akan menangani refresh token secara otomatis
                    // tapi kita bisa memaksa refresh jika diperlukan
                    supabase.gotrue.refreshCurrentSession()
                    Log.d("SupabaseService", "Session refreshed successfully")
                } catch (e: Exception) {
                    Log.e("SupabaseService", "Failed to refresh session: ${e.message}")
                    // Jika refresh gagal, logout
                    signOut()
                    return@withContext false
                }
            }
            
            _isLoggedIn.value = isValid
            isValid
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error checking session: ${e.message}")
            _isLoggedIn.value = false
            false
        }
    }

    suspend fun getIdeas(): List<StartupIdea> = withContext(Dispatchers.IO) {
        val result = supabase.postgrest["ideas"].select()
//        val result = supabase.postgrest["ideas"].select {
//            order("created_at", Order.DESCENDING)
//        }
        result.decodeList<StartupIdea>()
    }

    suspend fun insertIdea(idea: StartupIdea) = withContext(Dispatchers.IO) {
        supabase.postgrest["ideas"].insert(idea)
    }

    suspend fun signIn(email: String, password: String): Boolean = withContext(Dispatchers.IO) {
        try {
            supabase.gotrue.loginWith(Email) {
                this.email = email
                this.password = password
            }
            _isLoggedIn.value = true
            true
        } catch (e: Exception) {
            e.printStackTrace()
            // Tambahkan log untuk debugging
            Log.e("SupabaseService", "Login error: ${e.message}")
            
            // Tambahkan penanganan error yang lebih spesifik
            when {
                e.message?.contains("Invalid login credentials", ignoreCase = true) == true -> {
                    Log.e("SupabaseService", "Email atau password salah")
                }
                e.message?.contains("Email not confirmed", ignoreCase = true) == true -> {
                    Log.e("SupabaseService", "Email belum dikonfirmasi")
                }
                e.message?.contains("User not found", ignoreCase = true) == true -> {
                    Log.e("SupabaseService", "Pengguna tidak ditemukan")
                }
            }
            
            false
        }
    }

    suspend fun signUp(email: String, password: String): Boolean = withContext(Dispatchers.IO) {
        try {
            supabase.gotrue.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            // Tambahkan log untuk debugging
            Log.e("SupabaseService", "Signup error: ${e.message}")
            
            // Tambahkan penanganan error yang lebih spesifik
            when {
                e.message?.contains("User already registered", ignoreCase = true) == true -> {
                    Log.e("SupabaseService", "Email sudah terdaftar")
                }
                e.message?.contains("Password should be", ignoreCase = true) == true -> {
                    Log.e("SupabaseService", "Password tidak memenuhi persyaratan")
                }
            }
            
            false
        }
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        try {
            supabase.gotrue.logout()
            _isLoggedIn.value = false
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteIdea(ideaId: String) = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["ideas"].delete {
                eq("id", ideaId)
            }
            true
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error deleting idea: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun getCurrentSession() = withContext(Dispatchers.IO) {
        try {
            supabase.gotrue.currentSessionOrNull()
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error getting current session: ${e.message}")
            null
        }
    }

    suspend fun uploadImage(file: File, fileName: String): String = withContext(Dispatchers.IO) {
        try {
            // Upload file ke bucket "logos"
            val bucket = supabase.storage.from("startup-logos")
            
            // Baca file sebagai ByteArray
            val fileBytes = file.readBytes()
            
            // Upload file dengan parameter yang benar
            bucket.upload(path = fileName, data = fileBytes, upsert = true)
            
            // Dapatkan URL publik dari file yang diupload
            val publicUrl = bucket.publicUrl(fileName)
            Log.d("SupabaseService", "Image uploaded successfully: $publicUrl")
            publicUrl
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error uploading image: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    suspend fun updateIdea(idea: StartupIdea) = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["ideas"].update(
                {
                    set("title", idea.title)
                    set("description", idea.description)
                    set("phone", idea.phone)
                    if (idea.logo_url != null) {
                        set("logo_url", idea.logo_url)
                    }
                }
            ) {
                eq("id", idea.id)
            }
            true
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error updating idea: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}
