package dev.sonle.pdfscanner.presentation.features.viewer

import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PdfViewerPagerSwipeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun horizontalPager_shouldChangePage_whenSwipeLeft() {
        composeRule.setContent {
            var settledPage by remember { mutableIntStateOf(0) }
            val pagerState = rememberPagerState(
                initialPage = 0,
                pageCount = { 3 }
            )

            LaunchedEffect(pagerState) {
                androidx.compose.runtime.snapshotFlow { pagerState.settledPage }
                    .collect { settledPage = it }
            }

            androidx.compose.foundation.pager.HorizontalPager(
                state = pagerState,
                modifier = Modifier.testTag(PDF_VIEWER_PAGER_TEST_TAG),
                beyondViewportPageCount = 1,
                userScrollEnabled = true
            ) { page ->
                androidx.compose.material3.Text(
                    text = "Page $page",
                    modifier = Modifier.testTag("pdf_page_$page")
                )
            }
        }

        composeRule.onNodeWithTag("pdf_page_0").assertExists()
        composeRule.onNodeWithTag(PDF_VIEWER_PAGER_TEST_TAG).performTouchInput {
            swipeLeft()
        }
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onNodeWithTag("pdf_page_1")
                .runCatching { assertExists() }
                .isSuccess
        }
    }
}
