package dev.sonle.pdfscanner.domain.usecase

import dev.sonle.pdfscanner.domain.model.AppSettings
import dev.sonle.pdfscanner.domain.repository.AppSettingsRepository

class UpdateAppSettingsUseCase(
    private val appSettingsRepository: AppSettingsRepository
) {
    suspend operator fun invoke(settings: AppSettings) {
        appSettingsRepository.updateSettings(settings)
    }
}
