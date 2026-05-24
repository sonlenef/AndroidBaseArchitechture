package dev.sonle.pdfscanner.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dev.sonle.pdfscanner.core.util.Constants

class ReviewPromptPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun getSuccessfulScanCount(): Int =
        prefs.getInt(KEY_SUCCESSFUL_SCAN_COUNT, 0)

    fun incrementSuccessfulScanCount() {
        prefs.edit {
            putInt(KEY_SUCCESSFUL_SCAN_COUNT, getSuccessfulScanCount() + 1)
        }
    }

    fun hasReviewPromptBeenHandled(): Boolean =
        prefs.getBoolean(KEY_REVIEW_PROMPT_HANDLED, false)

    fun markReviewPromptHandled() {
        prefs.edit { putBoolean(KEY_REVIEW_PROMPT_HANDLED, true) }
    }

    fun prepareDebugReviewPromptOnHome() {
        prefs.edit {
            putBoolean(KEY_REVIEW_PROMPT_HANDLED, false)
            putInt(KEY_SUCCESSFUL_SCAN_COUNT, 3)
            putBoolean(KEY_REVIEW_PROMPT_PENDING_ON_HOME, true)
        }
    }

    fun getSuccessfulShareCount(): Int =
        prefs.getInt(KEY_SUCCESSFUL_SHARE_COUNT, 0)

    fun incrementSuccessfulShareCount() {
        prefs.edit {
            putInt(KEY_SUCCESSFUL_SHARE_COUNT, getSuccessfulShareCount() + 1)
        }
    }

    fun isReviewPromptPendingOnHome(): Boolean =
        prefs.getBoolean(KEY_REVIEW_PROMPT_PENDING_ON_HOME, false)

    fun setReviewPromptPendingOnHome(pending: Boolean) {
        prefs.edit { putBoolean(KEY_REVIEW_PROMPT_PENDING_ON_HOME, pending) }
    }

    companion object {
        private const val KEY_SUCCESSFUL_SCAN_COUNT = "review_successful_scan_count"
        private const val KEY_SUCCESSFUL_SHARE_COUNT = "review_successful_share_count"
        private const val KEY_REVIEW_PROMPT_HANDLED = "review_prompt_handled"
        private const val KEY_REVIEW_PROMPT_PENDING_ON_HOME = "review_prompt_pending_on_home"
    }
}
