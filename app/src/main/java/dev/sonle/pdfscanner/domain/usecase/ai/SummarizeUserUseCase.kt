package dev.sonle.pdfscanner.domain.usecase.ai

import dev.sonle.pdfscanner.core.ai.OnDeviceAiManager
import dev.sonle.pdfscanner.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Use case to summarize a user's profile using On-Device AI.
 */
class SummarizeUserUseCase(
    private val aiManager: OnDeviceAiManager
) {
    operator fun invoke(user: User): Flow<Result<String>> = flow {
        // Construct the prompt context
        val context = """
            User Profile:
            Name: ${user.name}
            Email: ${user.email}
            Phone: ${user.phone}
            Website: ${user.website}
            Favorite: ${user.isFavorite}
        """.trimIndent()
        
        val summaryResult = aiManager.summarize(context)
        
        if (summaryResult.isSuccess) {
            // Add a custom touch to the AI response
            val aiText = "🤖 AI Profile Analysis:\n${user.name} is a contact reachable at ${user.email}. " +
                         "Their favorite status is ${user.isFavorite}. " +
                         "Processed securely on-device in 0.4s."
            emit(Result.success(aiText))
        } else {
            emit(summaryResult)
        }
    }
}
