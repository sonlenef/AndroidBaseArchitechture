package dev.sonle.pdfscanner.domain.repository

import dev.sonle.pdfscanner.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {
    fun observeSettings(): Flow<AppSettings>
    fun currentSettings(): AppSettings
    suspend fun updateSettings(settings: AppSettings)
}
