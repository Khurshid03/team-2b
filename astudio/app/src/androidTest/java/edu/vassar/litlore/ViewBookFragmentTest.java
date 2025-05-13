package edu.vassar.litlore;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.action.ViewActions.scrollTo; // Keep this import

import androidx.test.espresso.contrib.RecyclerViewActions; // Keep this import
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Before; // Import Before

import edu.vassar.litlore.R;
import edu.vassar.litlore.controller.ControllerActivity;

// Removed imports for custom matcher


/**
 * Instrumentation tests for ViewBookFragment.
 * Verifies book details display and description toggle functionality
 * after navigating from the browse screen.
 */
@RunWith(AndroidJUnit4.class)
public class ViewBookFragmentTest {

    @Rule
    public ActivityScenarioRule<ControllerActivity> activityRule =
            new ActivityScenarioRule<>(ControllerActivity.class);

    /**
     * Types text into the view with the given ID and closes the soft keyboard.
     *
     * @param viewId the resource ID of the input view
     * @param text the text to type into the view
     */
    private static void typeTextAndCloseKeyboard(int viewId, String text) {
        onView(withId(viewId))
                .perform(typeText(text), closeSoftKeyboard()); // Chain closeSoftKeyboard()
    }

    /**
     * Logs in via the real “Proceed to Login” / Login flow and navigates
     * to the BrowseBooksFragment. This is a common setup step for tests
     * that start from the browse screen.
     */
    @Before
    public void setupBrowseFragment() throws InterruptedException {
        // 1) Go to login screen
        onView(withId(R.id.ProceedToLoginButton)).perform(click());

        // 2) Enter credentials and tap Login
        typeTextAndCloseKeyboard(R.id.textEmail, "felix@gmail.com");
        typeTextAndCloseKeyboard(R.id.textPassword, "Felix123");
        onView(withId(R.id.LoginButton)).perform(click());

        Thread.sleep(3000);
    }


    /**
     * Tests that book details (title, author, description, rating) display correctly
     * after clicking the first hot book in the BrowseBooksFragment.
     */
    @Test
    public void bookDetails_displayCorrectlyAfterClick() throws InterruptedException {

        // 3) Click first hot book (position 0)
        onView(withId(R.id.hot_books_recycler))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        Thread.sleep(3000);

        // 4) Verify details are shown (check for existence and display)
        onView(withId(R.id.book_title)).check(matches(isDisplayed()));
        onView(withId(R.id.book_author)).check(matches(isDisplayed()));
        onView(withId(R.id.book_description)).check(matches(isDisplayed()));
        onView(withId(R.id.book_rating)).check(matches(isDisplayed()));
    }

    /**
     * Tests the "Show More"/"Show Less" toggle on the book description.
     * This test verifies the button text changes upon clicking and scrolls if necessary.
     */
    @Test
    public void showMoreToggle_changesText() throws InterruptedException {
        onView(withId(R.id.hot_books_recycler))
                .perform(RecyclerViewActions.actionOnItemAtPosition(2, click()));

        Thread.sleep(3000);

        onView(withId(R.id.show_more_button))
                .check(matches(withText(R.string.show_more))) // Verify initial text
                .perform(scrollTo()); // Scroll to "Show more" button

        onView(withId(R.id.show_more_button)).check(matches(isDisplayed()));
        onView(withId(R.id.show_more_button)).perform(click()); // Click "Show more"

        Thread.sleep(1000);

        onView(withId(R.id.show_more_button)).perform(scrollTo()); // Scroll again
        onView(withId(R.id.show_more_button)).perform(scrollTo()); // Scroll a third time

        onView(withId(R.id.show_more_button)).check(matches(isDisplayed()));

        onView(withId(R.id.show_more_button))
                .check(matches(withText(R.string.show_less)))
                .perform(scrollTo(), click());

        Thread.sleep(1000);

        onView(withId(R.id.show_more_button))
                .check(matches(withText(R.string.show_more)))
                .perform(scrollTo()); // Scroll to "Show more" button
        onView(withId(R.id.show_more_button)).check(matches(isDisplayed()));
    }
}
