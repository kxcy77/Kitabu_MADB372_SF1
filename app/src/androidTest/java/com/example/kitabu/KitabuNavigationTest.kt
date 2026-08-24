package com.example.kitabu

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KitabuNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun catalogIsStartDestination_andReservationsNavigationWorks() {
        composeRule.onNodeWithText("Library catalog").assertIsDisplayed()
        composeRule.onNodeWithText("Reservations").performClick()
        composeRule.onNodeWithText("My reservations").assertIsDisplayed()
        composeRule.onNodeWithText("Clean Code").assertIsDisplayed()
    }
}
