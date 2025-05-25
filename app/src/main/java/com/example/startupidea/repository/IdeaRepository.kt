package com.example.startupidea.repository

import com.example.startupidea.data.SupabaseService
import com.example.startupidea.model.StartupIdea
import java.io.File

class IdeaRepository {
    suspend fun getAllIdeas(): List<StartupIdea> = SupabaseService.getIdeas()
    suspend fun addIdea(idea: StartupIdea) = SupabaseService.insertIdea(idea)
    suspend fun updateIdea(idea: StartupIdea) = SupabaseService.updateIdea(idea)
    suspend fun uploadImage(file: File, fileName: String): String = SupabaseService.uploadImage(file, fileName)
    suspend fun deleteIdea(ideaId: String): Boolean = SupabaseService.deleteIdea(ideaId)
}