package dev.sonle.pdfscanner.core.ads

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume

/**
 * GDPR / EEA consent via Google User Messaging Platform (UMP).
 * Must complete before loading personalized ads.
 */
class AdConsentManager(
    private val context: Context
) {
    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)

    val canRequestAds: Boolean
        get() = consentInformation.canRequestAds()

    val isConsentFormAvailable: Boolean
        get() = consentInformation.isConsentFormAvailable

    suspend fun gatherConsent(activity: Activity): Boolean = suspendCancellableCoroutine { cont ->
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Timber.w("UMP consent form error: ${formError.message}")
                    }
                    if (cont.isActive) {
                        cont.resume(consentInformation.canRequestAds())
                    }
                }
            },
            { error ->
                Timber.w("UMP consent info update failed: ${error.message}")
                if (cont.isActive) {
                    cont.resume(consentInformation.canRequestAds())
                }
            }
        )
    }

    fun resetConsentForTesting() {
        consentInformation.reset()
    }
}
