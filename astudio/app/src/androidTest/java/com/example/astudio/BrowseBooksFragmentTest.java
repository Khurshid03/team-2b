package com.example.astudio;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import androidx.test.espresso.matcher.ViewMatchers.Visibility;

import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.fragment.app.Fragment;
import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.astudio.controller.ControllerActivity;
import com.example.astudio.view.SearchBooksFragment;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Before;

/**
 * Instrumentation tests for BrowseBooksFragment.
 * Ensures a user must log in before browsing and verifies UI elements and navigation.
 */

@RunWith(AndroidJUnit4.class)
public class BrowseBooksFragmentTest {

    @Rule
    public ActivityScenarioRule<ControllerActivity> activityRule =
        new ActivityScenarioRule<>(ControllerActivity.class);

    /**
     * Logs in as a test user before each browse test to navigate into BrowseBooksFragment.
     */
    @Before
    public void loginBeforeBrowse() {
        // Log in as "tester" to reach BrowseBooksFragment
        typeTextAndCloseKeyboard(R.id.Text_username, "Felix");
        onView(withId(R.id.CreateAccountButton)).perform(click());
    }

    /**
     * Types the given text into the view with the specified ID and closes the soft keyboard.
     *
     * @param viewId resource ID of the input view
     * @param text   text to type
     */
    private static void typeTextAndCloseKeyboard(int viewId, String text) {
        Espresso.onView(withId(viewId))
                .perform(typeText(text));
        Espresso.closeSoftKeyboard();
    }

    /**
     * Checks that the welcome message displays the current username correctly.
     */
    @Test
    public void welcomeMessage_withCurrentUser_displaysUsername() {
        // Set a user via UserManager if needed before launching; assuming default empty shows placeholder.
        onView(withId(R.id.welcome_message))
                .check(matches(withText("Welcome to Litlore, Felix!")));
    }


    /**
     * Enters a search query and clicks "Go", then verifies navigation to SearchBooksFragment.
     */
    @Test
    public void goButton_withInput_navigatesToSearchFragment() {
        typeTextAndCloseKeyboard(R.id.search_input, "Kotlin");
        onView(withId(R.id.go_button)).perform(click());

        activityRule.getScenario().onActivity(activity -> {
            Fragment current = activity.getSupportFragmentManager()
                    .findFragmentById(R.id.fragmentContainerView);
            assertTrue(current instanceof SearchBooksFragment);
            assertEquals("Kotlin",
                    ((SearchBooksFragment) current).getArguments().getString("query"));
        });
    }

    /**
     * Asserts that the hot books RecyclerView is visible in the layout.
     */
    @Test
    public void hotBooksRecycler_isVisibleInLayout() {
        onView(withId(R.id.hot_books_recycler))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
    }

    /**
     * Asserts that the genre books RecyclerView is visible in the layout.
     */
    @Test
    public void genreBooksRecycler_isVisibleInLayout() {
        onView(withId(R.id.genre_books_recycler))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
    }

    /**
     * Asserts that the genre buttons RecyclerView is visible in the layout.
     */
    @Test
    public void genreButtonsRecycler_isVisibleInLayout() {
        onView(withId(R.id.genre_button_recycler))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
    }
}