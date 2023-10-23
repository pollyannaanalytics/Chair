package com.example.reclaim.uitest

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.reclaim.MainActivity
import com.example.reclaim.R
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class MainActivityUiTest {

    @Before
    fun setUp(){
        ActivityScenario.launch(MainActivity::class.java)

    }


    @Test
    fun checkActivityVisibility() {
        onView(withId(R.id.main_activity))
            .check(matches(isDisplayed()))
    }


    @Test
    fun checkLoadingPageVisibility(){
        onView(withId(R.id.loading_page_layout))
            .check(matches(isDisplayed()))
        
    }


}