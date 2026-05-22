package dev.sonle.pdfscanner.core.performance

import dev.sonle.pdfscanner.core.config.EnvironmentConfig
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class PerformanceMonitorTest {

    private val firebasePerformance: FirebasePerformance = mockk()
    private val trace: Trace = mockk(relaxed = true)

    @Before
    fun setup() {
        mockkObject(EnvironmentConfig)
        every { EnvironmentConfig.performanceMonitoringEnabled } returns true
    }

    @After
    fun tearDown() {
        unmockkObject(EnvironmentConfig)
    }

    @Test
    fun `startUserActionTrace should start and stop trace`() {
        every { firebasePerformance.newTrace("user_action_save_pdf") } returns trace

        val monitor = PerformanceMonitor(firebasePerformance)
        val handle = monitor.startUserActionTrace("save_pdf")

        verify { trace.start() }
        monitor.stopTrace(handle)
        verify { trace.stop() }
    }

    @Test
    fun `stopTrace should be no-op when handle is null`() {
        val monitor = PerformanceMonitor(firebasePerformance)
        monitor.stopTrace(null)
    }
}
