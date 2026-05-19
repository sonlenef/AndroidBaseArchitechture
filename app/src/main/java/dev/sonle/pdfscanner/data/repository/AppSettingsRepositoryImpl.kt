package dev.sonle.pdfscanner.data.repository

import dev.sonle.pdfscanner.data.local.AppSettingsPreferences
import dev.sonle.pdfscanner.domain.model.AppSettings
import dev.sonle.pdfscanner.domain.repository.AppSettingsRepository
import kotlinx.coroutines.flow.Flow

class AppSettingsRepositoryImpl(
    private val preferences: AppSettingsPreferences
) : AppSettingsRepository {

    override fun observeSettings(): Flow<AppSettings> = preferences.observeSettings()

    override fun currentSettings(): AppSettings = preferences.readSettings()

    override suspend fun updateSettings(settings: AppSettings) {
        preferences.writeSettings(settings)
    }
}
