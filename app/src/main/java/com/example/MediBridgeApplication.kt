package com.example

import android.app.Application
import com.example.data.ai.GeminiAiService
import com.example.data.local.MediBridgeDatabase
import com.example.data.repository.MediBridgeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class MediBridgeApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob())

    val database: MediBridgeDatabase by lazy {
        MediBridgeDatabase.getDatabase(this, applicationScope)
    }

    val repository: MediBridgeRepository by lazy {
        MediBridgeRepository(database.mediBridgeDao())
    }

    val aiService: GeminiAiService by lazy {
        GeminiAiService()
    }
}
