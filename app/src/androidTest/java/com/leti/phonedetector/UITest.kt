package com.leti.phonedetector

import android.content.Context
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import com.leti.phonedetector.database.PhoneLogDBHelper
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class UITest {

    @Before
    fun fillDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = PhoneLogDBHelper(context)
        db.fillSampleData()
    }

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun filterLog() {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withText("Show spam")).perform(click())
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withText("Show not spam")).perform(click())
        onView(withId(R.id.log_layout)).check(doesNotExist())
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withText("Show spam")).perform(click())
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withText("Show not spam")).perform(click())
        onView(allOf(ViewMatchers.withId(R.id.log_layout), isDisplayed()))
    }

    @Test
    fun testSample() {
        if (getRVcount() > 0) {
            onView(withId(R.id.list_of_phones))
                .perform(
                    RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                        0,
                        click()
                    )
                )
            onView(withId(R.id.overlay_layout)).check(matches(isDisplayed()))
            onView(withText("Block number")).check(matches(isDisplayed()))
            onView(withId(R.id.overlay_button_exit)).perform(click())
            onView(withId(R.id.overlay_layout)).check(doesNotExist())
            onView(withId(R.id.list_of_phones))
                .perform(
                    RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                        1,
                        click()
                    )
                )
            onView(withId(R.id.overlay_layout)).check(matches(isDisplayed()))
            onView(withText(R.string.button_add_contact)).check(matches(isDisplayed()))
            onView(withId(R.id.overlay_button_exit)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun testSearch() {
        onView(withId(R.id.action_search)).perform(click())
        onView(withId(R.id.search_src_text)).perform(typeText("Max"))
        onView(withId(R.id.log_element_text_name)).check(matches(hasValueEqualTo("Max") as Matcher<in View>?))
        onView(withId(R.id.list_of_phones))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    click()
                )
            )
        onView(withId(R.id.overlay_layout)).check(matches(isDisplayed()))
        onView(withText(R.string.button_add_contact)).check(matches(isDisplayed()))
        onView(withId(R.id.overlay_button_exit)).check(matches(isDisplayed()))
        onView(withId(R.id.overlay_button_exit)).perform(click())
    }

    @Test
    fun testSearchNotFound() {
        onView(withId(R.id.action_search)).perform(click())
        onView(withId(R.id.search_src_text)).perform(typeText("+79291045342"))
        onView(withId(R.id.search_src_text)).perform(pressKey(KeyEvent.KEYCODE_ENTER))
        onView(withText("No")).perform(click())
        Espresso.pressBack()
    }

    @Test
    fun testSettings() {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withText("Settings")).perform(click())
        onView(withId(androidx.preference.R.id.recycler_view))
            .perform(actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText(R.string.use_getcontact)), click()))
        onView(withId(androidx.preference.R.id.recycler_view))
            .perform(actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText(R.string.use_neberitrubku)), click()))
        onView(withId(androidx.preference.R.id.recycler_view))
            .perform(actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText(R.string.show_empty_user)), click()))
        onView(withId(androidx.preference.R.id.recycler_view))
            .perform(actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText(R.string.create_notification)), click()))
        onView(withId(androidx.preference.R.id.recycler_view))
            .perform(actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText(R.string.notification_instead_overlay)), click()))
        onView(withId(androidx.preference.R.id.recycler_view))
            .perform(actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText(R.string.no_cache_empty_phones)), click()))
        onView(withId(androidx.preference.R.id.recycler_view))
            .perform(actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText(R.string.always_network)), click()))
        Espresso.pressBack()
    }

    private fun getRVcount(): Int {
        val recyclerView =
            activityRule.activity.findViewById(R.id.list_of_phones) as RecyclerView
        return recyclerView.adapter!!.itemCount
    }
}

fun hasValueEqualTo(content: String): Any {
    return object : TypeSafeMatcher<View?>() {
        override fun describeTo(description: Description) {
            description.appendText("Has EditText/TextView the value:  $content")
        }

        override fun matchesSafely(view: View?): Boolean {
            if (view !is TextView && view !is EditText) {
                return false
            }
            if (view != null) {
                val text: String
                text = if (view is TextView) {
                    view.text.toString()
                } else {
                    (view as EditText).text.toString()
                }
                return text.equals(content, ignoreCase = true)
            }
            return false
        }
    }
}

