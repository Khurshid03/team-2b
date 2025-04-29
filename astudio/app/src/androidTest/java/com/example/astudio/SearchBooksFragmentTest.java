package com.example.astudio.view;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.espresso.contrib.RecyclerViewActions;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.astudio.R;
import com.example.astudio.controller.ControllerActivity;

@RunWith(AndroidJUnit4.class)
public class SearchBooksFragmentTest {

    @Rule
    public ActivityScenarioRule<ControllerActivity> activityRule =
            new ActivityScenarioRule<>(ControllerActivity.class);

    /** Types text into a view and closes the keyboard. */
    private static void typeTextAndCloseKeyboard(int viewId, String text) {
        onView(withId(viewId)).perform(typeText(text));
        closeSoftKeyboard();
    }

    /** Launches the SearchBooksFragment directly after login. */
    @Before
    public void loginAndOpenSearch() throws InterruptedException {
        // Log in
        typeTextAndCloseKeyboard(R.id.Text_username, "Felix");
        onView(withId(R.id.CreateAccountButton)).perform(click());
        Thread.sleep(1000);

        // Show SearchBooksFragment
        activityRule.getScenario().onActivity(activity -> {
            activity.mainUI.displayFragment(SearchBooksFragment.newInstance(""));
        });
        Thread.sleep(1000);
    }

    /**
     * Enters a query, clicks Go, and verifies results RecyclerView is visible.
     */
    @Test
    public void search_validInput_displaysResults() throws InterruptedException {
        typeTextAndCloseKeyboard(R.id.search_input, "Kotlin");
        onView(withId(R.id.go_button)).perform(click());
        // Wait for network
        Thread.sleep(3000);

        onView(withId(R.id.search_books_recycler))
            .check(matches(isDisplayed()))
            .perform(RecyclerViewActions.scrollToPosition(0));
    }
}
