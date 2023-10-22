package com.example.reclaim.uitest

import android.content.Context
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.example.reclaim.MainActivity
import com.example.reclaim.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class MainActivityUiTest {

    @JvmField
    @Rule
    var activityTestRule = activityScenarioRule<MainActivity>()

    private val context: Context = InstrumentationRegistry.getInstrumentation().context



    @Test
    fun checkActivityVisibility() {
        onView(withId(R.id.main_activity))
            .check(matches(isDisplayed()))
    }



}