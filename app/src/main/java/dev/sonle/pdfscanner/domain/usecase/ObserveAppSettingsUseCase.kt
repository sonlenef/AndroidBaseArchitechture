package dev.sonle.pdfscanner.domain.usecase

import dev.sonle.pdfscanner.domain.model.AppSettings
import dev.sonle.pdfscanner.domain.repository.AppSettingsRepository
import kotlinx.coroutines.flow.Flow

class ObserveAppSettingsUseCase(
    private val appSettingsRepository: AppSettingsRepository
) {
    operator fun invoke(): Flow<AppSettings> = appSettingsRepository.observeSettings()
}
