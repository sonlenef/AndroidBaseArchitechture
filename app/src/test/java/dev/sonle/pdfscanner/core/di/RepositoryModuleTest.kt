package dev.sonle.pdfscanner.core.di

import dev.sonle.pdfscanner.domain.repository.RecentScanRepository
import dev.sonle.pdfscanner.domain.repository.ScannerRepository
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class RepositoryModuleTest {

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `repository module should resolve scanner and recent scan repositories`() {
        val context = RuntimeEnvironment.getApplication()

        startKoin {
            androidContext(context)
            modules(databaseModule, repositoryModule)
        }

        val koin = org.koin.java.KoinJavaComponent.getKoin()
        assertNotNull(koin.get<ScannerRepository>())
        assertNotNull(koin.get<RecentScanRepository>())
    }
}
