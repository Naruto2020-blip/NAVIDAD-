package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `app name matches Navidad`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Navidad", appName)
    }

    @Test
    fun `main activity launches and renders countdown screen`() {
        composeRule.mainClock.autoAdvance = false
        composeRule.mainClock.advanceTimeBy(200L)
        composeRule.onNodeWithText("Magia de Navidad").assertIsDisplayed()
        composeRule.onNodeWithTag("countdown_days").assertIsDisplayed()
        composeRule.onNodeWithTag("hero_music_pill").assertIsDisplayed()
    }
}
