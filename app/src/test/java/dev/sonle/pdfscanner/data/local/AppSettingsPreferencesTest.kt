package dev.sonle.pdfscanner.data.local

import android.content.Context
import dev.sonle.pdfscanner.core.util.Constants
import dev.sonle.pdfscanner.domain.model.AppLanguage
import dev.sonle.pdfscanner.domain.model.AppSettings
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class AppSettingsPreferencesTest {

    private lateinit var preferences: AppSettingsPreferences

    @Before
    fun setup() {
        val context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
        preferences = AppSettingsPreferences(context)
    }

    @Test
    fun `writeSettings should persist app language`() {
        val settings = AppSettings.Default.copy(appLanguage = AppLanguage.SPANISH)

        preferences.writeSettings(settings)

        assertEquals(AppLanguage.SPANISH, preferences.readSettings().appLanguage)
    }

    @Test
    fun `readSettings should default to english when unset`() {
        assertEquals(AppLanguage.ENGLISH, preferences.readSettings().appLanguage)
    }

    @Test
    fun `readSettings should migrate legacy system value to english`() {
        val context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("settings_app_language", "SYSTEM")
            .apply()

        assertEquals(AppLanguage.ENGLISH, AppSettingsPreferences(context).readSettings().appLanguage)
    }
}
