package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.core.app.ApplicationProvider
import com.example.data.UpdateChecker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
        composeRule.onNodeWithTag("hero_next_song_button").assertIsDisplayed()
        composeRule.onNode(hasScrollToNodeAction()).performTouchInput { swipeUp() }
        composeRule.mainClock.advanceTimeBy(100L)
        composeRule.onNodeWithTag("check_update_button").assertIsDisplayed()
    }

    @Test
    fun `update parser returns update when remote versionCode is higher`() {
        val json = """
            {
              "versionCode": 5,
              "versionName": "1.4",
              "notes": "Mejoras navideñas",
              "downloadUrl": "https://github.com/Naruto2020-bl/NAVIDAD-/raw/main/apk/app-debug.apk"
            }
        """.trimIndent()

        val update = UpdateChecker.parseUpdateIfNewer(json, currentVersionCode = 2)
        assertNotNull(update)
        assertEquals(5, update?.versionCode)
        assertEquals("1.4", update?.versionName)
    }

    @Test
    fun `update parser returns null when remote versionCode is equal or lower`() {
        val json = """{"versionCode": 2, "versionName": "1.1"}"""
        assertNull(UpdateChecker.parseUpdateIfNewer(json, currentVersionCode = 2))
    }
}
