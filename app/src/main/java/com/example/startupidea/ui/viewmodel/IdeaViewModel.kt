package com.example.startupidea.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.startupidea.data.model.StartupIdea
import com.example.startupidea.data.repository.IdeaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*

class IdeaViewModel : ViewModel() {
    private val repository = IdeaRepository()

    private val _ideas = MutableStateFlow<List<StartupIdea>>(emptyList())
    val ideas: StateFlow<List<StartupIdea>> get() = _ideas
    
    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    fun fetchIdeas() {
        viewModelScope.launch {
            _ideas.value = repository.getAllIdeas()
        }
    }

    fun submitIdea(title: String, desc: String, email: String, phone: String, logoFile: File? = null) {
        val id = UUID.randomUUID().toString()
        val createdAt = DateTimeFormatter.ISO_INSTANT.format(Instant.now()) // format ISO 8601 UTC

        viewModelScope.launch {
            try {
                _isUploading.value = true
                
                // Upload gambar jika ada
                var logoUrl: String? = null
                if (logoFile != null) {
                    val fileName = "logo_${id}_${System.currentTimeMillis()}.jpg"
                    logoUrl = repository.uploadImage(logoFile, fileName)
                }
                
                val idea = StartupIdea(
                    id = id,
                    created_at = createdAt,
                    title = title,
                    description = desc,
                    email = email,
                    phone = phone,
                    logo_url = logoUrl
                )

                repository.addIdea(idea)
                fetchIdeas()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun deleteIdea(ideaId: String) {
        viewModelScope.launch {
            try {
                val success = repository.deleteIdea(ideaId)
                if (success) {
                    fetchIdeas() // Refresh daftar ide setelah menghapus
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateIdea(ideaId: String, title: String, desc: String, email: String, phone: String, logoFile: File? = null) {
        viewModelScope.launch {
            try {
                _isUploading.value = true
                
                // Upload gambar jika ada
                var logoUrl: String? = null
                if (logoFile != null) {
                    val fileName = "logo_${ideaId}_${System.currentTimeMillis()}.jpg"
                    logoUrl = repository.uploadImage(logoFile, fileName)
                }
                
                // Dapatkan ide yang akan diupdate
                val currentIdeas = _ideas.value
                val ideaToUpdate = currentIdeas.find { it.id == ideaId }
                
                ideaToUpdate?.let {
                    val updatedIdea = StartupIdea(
                        id = ideaId,
                        created_at = it.created_at,
                        title = title,
                        description = desc,
                        email = email,
                        phone = phone,
                        logo_url = logoUrl ?: it.logo_url // Gunakan URL logo baru jika ada, jika tidak gunakan yang lama
                    )
                    
                    repository.updateIdea(updatedIdea)
                    fetchIdeas() // Refresh daftar ide setelah update
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isUploading.value = false
            }
        }
    }
}
