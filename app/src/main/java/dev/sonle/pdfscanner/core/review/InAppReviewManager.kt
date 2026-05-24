package dev.sonle.pdfscanner.core.review

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.android.play.core.review.ReviewManagerFactory
import dev.sonle.pdfscanner.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Requests the Google Play In-App Review flow per [official guidance](https://developer.android.com/guide/playcore/in-app-review).
 *
 * - Uses application [Context] for [ReviewManagerFactory].
 * - Runs on the main thread (required for [launchReviewFlow]).
 * - Falls back to the Play Store listing only when the in-app flow cannot be requested or launched.
 * - Does **not** fall back when the flow completes successfully but Google chooses not to show UI (quota).
 */
class InAppReviewManager(
    private val appContext: Context,
    private val listingApplicationId: String = BuildConfig.PLAY_STORE_APPLICATION_ID
) {
    private val reviewManager by lazy { ReviewManagerFactory.create(appContext) }

    suspend fun requestReview(activity: Activity): InAppReviewOutcome = withContext(Dispatchers.Main) {
        try {
            val reviewInfo = reviewManager.requestReviewFlow().await()
            reviewManager.launchReviewFlow(activity, reviewInfo).await()
            Timber.i(
                "InAppReviewManager: in-app review flow finished (hostPkg=%s)",
                activity.packageName
            )
            InAppReviewOutcome.FlowFinished
        } catch (e: Exception) {
            Timber.w(
                e,
                "InAppReviewManager: in-app review failed, opening store listing=%s",
                listingApplicationId
            )
            if (openStoreListing(activity)) {
                InAppReviewOutcome.StoreFallback
            } else {
                InAppReviewOutcome.Unavailable
            }
        }
    }

    private fun openStoreListing(activity: Activity): Boolean {
        val marketUri = Uri.parse("market://details?id=$listingApplicationId")
        val webUri = Uri.parse("https://play.google.com/store/apps/details?id=$listingApplicationId")

        val playStoreIntent = Intent(Intent.ACTION_VIEW, marketUri).apply {
            setPackage("com.android.vending")
        }
        if (tryStart(activity, playStoreIntent)) return true

        val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
        if (tryStart(activity, marketIntent)) return true

        val webIntent = Intent(Intent.ACTION_VIEW, webUri)
        return tryStart(activity, webIntent)
    }

    private fun tryStart(activity: Activity, intent: Intent): Boolean {
        if (intent.resolveActivity(activity.packageManager) == null) return false
        return try {
            activity.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            Timber.d(e, "InAppReviewManager: no activity for %s", intent.data)
            false
        }
    }
}
