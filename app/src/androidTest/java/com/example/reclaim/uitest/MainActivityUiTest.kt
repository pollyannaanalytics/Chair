package com.example.reclaim

import android.content.Context
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class ChairUiTest {

    @JvmField
    @Rule
    var activityTestRule = activityScenarioRule<MainActivity>()

    private val context: Context = InstrumentationRegistry.getInstrumentation().context



    @Test
    fun checkActivityVisibility() {
        onView(withId(R.id.main_activity))
            .check(matches(isDisplayed()))
    }


    @Test
    fun checkLoadingPageVisibility() {
        onView(withId(R.id.loading_page_layout))
            .check(matches(isDisplayed()))
    }




    @Test
    fun checkLoadingLottieVisibility() {
        onView(withId(R.id.loading_animation))
            .check(matches(isDisplayed()))
    }


    @Test
    fun checkLoadingTextViewVisibility(){
        onView(withId(R.id.loading_hint))
            .check(matches(isDisplayed()))

        onView(withId(R.id.loading_hint_author))
            .check(matches(isDisplayed()))

    }



    @Test
    fun checkLoadingHintCorrect() {
        val loadingHintText = context.resources.getString(R.string.loading_quote)
        val loadingHintAuthorText = context.resources.getString(R.string.loading_author)

        // check hint show or not
        onView(withId(R.id.loading_hint))
            .check(matches(ViewMatchers.withText(loadingHintText)))


        // check author show or not
        onView(withId(R.id.loading_hint_author))
            .check(matches(ViewMatchers.withText(loadingHintAuthorText)))

    }


}