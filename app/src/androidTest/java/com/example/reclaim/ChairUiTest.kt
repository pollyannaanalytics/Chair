package com.example.reclaim

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class ChairUiTest {

    @JvmField
    @Rule
    var activityTestRule = activityScenarioRule<MainActivity>()


    @Test
    fun checkActivityVisibility() {
        onView(withId(R.id.main_activity))
            .check(matches(isDisplayed()))
    }



}