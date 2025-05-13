package edu.vassar.litlore;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Before;

import edu.vassar.litlore.R;
import edu.vassar.litlore.controller.ControllerActivity;


@RunWith(AndroidJUnit4.class)
public class ViewProfileFragmentTest {

    @Rule
    public ActivityScenarioRule<ControllerActivity> activityRule =
            new ActivityScenarioRule<>(ControllerActivity.class);

    /** Types text into a view and closes the keyboard. */
    private static void typeTextAndCloseKeyboard(int viewId, String text) {
        onView(withId(viewId)).perform(typeText(text));
        closeSoftKeyboard();
    }

    /**
     * Logs in using email/password and then navigates to the ViewProfileFragment before each test.
     * This sets up the test environment.
     */
    @Before
    public void setupViewProfileFragment() throws InterruptedException {
        /** 1) Go to login screen (assuming activity starts on CreateAccountFragment) */
        onView(withId(R.id.ProceedToLoginButton)).perform(click());

        /** Verify we are on the LoginFragment */
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        onView(withId(R.id.LoginButton)).check(matches(isDisplayed()));

        /** 2) Enter credentials and tap Login */
        typeTextAndCloseKeyboard(R.id.textEmail, "felix@gmail.com");
        typeTextAndCloseKeyboard(R.id.textPassword, "Felix123");
        onView(withId(R.id.LoginButton)).perform(click());

        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

        /** Verify we are on the BrowseBooksFragment before navigating to Profile */
        onView(withId(R.id.hot_books_recycler)).check(matches(isDisplayed()));

        /** 3) Navigate to Profile Fragment using the Bottom Navigation View*/
        onView(withId(R.id.nav_profile)).perform(click());

        /** Wait for fragment transaction to ViewProfileFragment*/
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        /** Verify we are on the ViewProfileFragment */
        onView(withId(R.id.tvUsername)).check(matches(isDisplayed())); // Assuming tvUsername is the ID for the username TextView
    }

    /**
     * Tests that the username, follower count, following count, and reviews RecyclerView are displayed.
     * This test assumes the profile data is loaded and displayed successfully after navigation.
     */
    @Test
    public void profileUI_elementsAreDisplayed() {
        onView(withId(R.id.tvUsername)).check(matches(isDisplayed()));
        onView(withId(R.id.followersButton)).check(matches(isDisplayed()));
        onView(withId(R.id.followingButton)).check(matches(isDisplayed()));
        onView(withId(R.id.Reviews)).check(matches(isDisplayed()));
    }
}
