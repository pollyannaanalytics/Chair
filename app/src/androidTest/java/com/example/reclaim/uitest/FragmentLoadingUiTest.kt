package com.example.reclaim.uitest

import android.content.Context
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.reclaim.MainActivity
import com.example.reclaim.R
import com.example.reclaim.loading.LoadingFragment
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FragmentLoadingUiTest{

    @Rule
    @JvmField
    var activityTestRule = activityScenarioRule<MainActivity>()
//
    private val context: Context = InstrumentationRegistry.getInstrumentation().context
    private lateinit var scenario: FragmentScenario<LoadingFragment>

    @Before
    fun setUp(){
        scenario = launchFragmentInContainer<LoadingFragment>(themeResId = R.style.Base_Theme_Reclaim)
        scenario.moveToState(androidx.lifecycle.Lifecycle.State.STARTED)
    }


    @Test
    fun testLoadingAnimation(){
        onView(ViewMatchers.withId(R.id.loading_animation))
            .check(matches(ViewMatchers.isDisplayed()))
    }



}