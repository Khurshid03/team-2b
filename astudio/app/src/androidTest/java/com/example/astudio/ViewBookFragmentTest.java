package com.example.astudio.view;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.action.ViewActions.scrollTo;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.astudio.R;
import com.example.astudio.controller.ControllerActivity;

@RunWith(AndroidJUnit4.class)
public class ViewBookFragmentTest {

    @Rule
    public ActivityScenarioRule<ControllerActivity> activityRule =
            new ActivityScenarioRule<>(ControllerActivity.class);

    /**
     * Types text into the view with the given ID and closes the soft keyboard.
     */
    private static void typeTextAndCloseKeyboard(int viewId, String text) {
        onView(withId(viewId))
            .perform(typeText(text));
        closeSoftKeyboard();
    }

    /**
     * Logs in, selects the first book in the hot-books list, and verifies
     * the ViewBookFragment displays title, author, description, and rating.
     */
    @Test
    public void bookDetails_displayCorrectlyAfterClick() throws InterruptedException {
        // 1) Log in as "tester"
        typeTextAndCloseKeyboard(R.id.Text_username, "Felix");
        onView(withId(R.id.LoginButton)).perform(click());
        // wait for navigation
        Thread.sleep(1000);

        // 2) Click first hot book
        onView(withId(R.id.hot_books_recycler))
            .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        // wait for fragment transition
        Thread.sleep(1000);

        // 3) Verify details
        onView(withId(R.id.book_title)).check(matches(isDisplayed()));
        onView(withId(R.id.book_author)).check(matches(isDisplayed()));
        onView(withId(R.id.book_description)).check(matches(isDisplayed()));
        onView(withId(R.id.book_rating)).check(matches(isDisplayed()));
    }

    /**
     * Tests the "Show More"/"Show Less" toggle on the description.
     */
    @Test
    public void showMoreToggle_changesText() throws InterruptedException {
        // Log in and navigate to view fragment
        typeTextAndCloseKeyboard(R.id.Text_username, "Felix");
        onView(withId(R.id.LoginButton)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.hot_books_recycler))
            .perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));
        Thread.sleep(1000);

        // Toggle and verify
        onView(withId(R.id.show_more_button))
            .check(matches(withText(R.string.show_more)))
            .perform(scrollTo(), click())
            .check(matches(withText(R.string.show_less)))
            .perform(scrollTo(), click())
            .check(matches(withText(R.string.show_more)));
    }
}