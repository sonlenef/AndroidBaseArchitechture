package dev.sonle.pdfscanner.core.ai

import kotlinx.coroutines.flow.Flow

/**
 * Interface defining operations for On-Device AI (SLMs like Gemini Nano)
 */
interface OnDeviceAiManager {
    /**
     * Checks if the on-device AI model is currently available and downloaded.
     */
    suspend fun isModelAvailable(): Boolean

    /**
     * Triggers the download of the on-device model if not already present.
     */
    suspend fun downloadModelIfNeeded(): Boolean

    /**
     * Generates a summary of the provided text using the on-device model.
     */
    suspend fun summarize(text: String): Result<String>

    /**
     * Generates smart reply suggestions based on the conversation context.
     */
    suspend fun generateSmartReply(context: String): Result<List<String>>
    
    /**
     * Generates text as a stream for real-time UI updates.
     */
    fun generateTextStream(prompt: String): Flow<String>
}
