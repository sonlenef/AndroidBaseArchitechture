package dev.sonle.pdfscanner.core.ads

import com.google.android.gms.ads.AdRequest

object AdRequestFactory {
    fun create(): AdRequest = AdRequest.Builder().build()
}
