package edu.vassar.litlore;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.view.View;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.contrib.RecyclerViewActions;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Before;

import edu.vassar.litlore.controller.ControllerActivity;

import org.hamcrest.Matcher;
import org.hamcrest.BaseMatcher;
import androidx.test.espresso.util.HumanReadables;
import org.hamcrest.Description;


@RunWith(AndroidJUnit4.class)
public class FollowUnfollowTest {

    @Rule
    public ActivityScenarioRule<ControllerActivity> activityRule =
            new ActivityScenarioRule<>(ControllerActivity.class);

    /** Types text into a view and closes the keyboard. */
    private static void typeTextAndCloseKeyboard(int viewId, String text) {
        onView(withId(viewId)).perform(typeText(text));
        closeSoftKeyboard();
    }

    /**
     * Logs in using email/password and then navigates to the SearchUsersFragment before each test.
     */
    @Before
    public void setupSearchUsersFragment() throws InterruptedException {
        /** 1) Go to login screen */
        onView(withId(R.id.ProceedToLoginButton)).perform(click());

        /** Verify we are on the LoginFragment */
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        onView(withId(R.id.LoginButton)).check(matches(isDisplayed()));

        /** 2) Enter credentials and tap Login */
        typeTextAndCloseKeyboard(R.id.textEmail, "felix@gmail.com");
        typeTextAndCloseKeyboard(R.id.textPassword, "Felix123");
        onView(withId(R.id.LoginButton)).perform(click());

        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

        /** Verify we are on the BrowseBooksFragment before navigating to Search Users */
        onView(withId(R.id.hot_books_recycler)).check(matches(isDisplayed()));

        /** 3) Navigate to Search Users Fragment using the Bottom Navigation View*/
        onView(withId(R.id.search_users)).perform(click());

        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
        onView(withId(R.id.search_users_recycler)).check(matches(isDisplayed()));
    }

    /**
     * Tests the follow functionality: search for a user, navigate to profile, click follow,
     * and verify the button text changes to "Following".
     */
    @Test
    public void followUser_changesButtonToFollowing() throws InterruptedException {
        /** 1) Search for a user */
        typeTextAndCloseKeyboard(R.id.search_input, "Tanish");
        onView(withId(R.id.go_button)).perform(click());
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}


        onView(withId(R.id.search_users_recycler)).check(matches(isDisplayed()));
        onView(withId(R.id.search_users_recycler)).check(matches(hasDescendant(withText("Tanish"))));


        /** 2) Click the first user item in the search results (assuming "testuser" is the first result) */
        onView(withId(R.id.search_users_recycler))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

        onView(withId(R.id.tvUsername)).check(matches(isDisplayed()));
        onView(withId(R.id.btnFollow)).check(matches(isDisplayed()));


        /** 3) Click the Follow button if it currently says "Follow" */
        onView(withId(R.id.btnFollow))
                .check(matches(withTextWithinTime(R.string.follow, 10000)))
                .perform(click());

        onView(withId(R.id.btnFollow))
                .check(matches(withTextWithinTime(R.string.Following, 10000)));
    }

    /**
     * Tests the unfollow functionality: search for a user, navigate to profile,
     * ensure user is followed, click following, and verify the button text changes to "Follow".
     */
    @Test
    public void unfollowUser_changesButtonToFollow() throws InterruptedException {
        typeTextAndCloseKeyboard(R.id.search_input, "Tanish");
        onView(withId(R.id.go_button)).perform(click());
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
        onView(withId(R.id.search_users_recycler)).check(matches(isDisplayed()));
        onView(withId(R.id.search_users_recycler)).check(matches(hasDescendant(withText("Tanish"))));

        onView(withId(R.id.search_users_recycler))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

        onView(withId(R.id.tvUsername)).check(matches(isDisplayed()));
        onView(withId(R.id.btnFollow)).check(matches(isDisplayed()));

        onView(withId(R.id.btnFollow))
                .check(matches(withTextWithinTime(R.string.Following, 10000)));


        onView(withId(R.id.btnFollow)).perform(click());

        onView(withId(R.id.btnFollow))
                .check(matches(withTextWithinTime(R.string.follow, 10000)));
    }

    /**
     * Custom matcher that waits for a view to have specific text within a specified timeout.
     *
     * @param stringResId The string resource ID of the expected text.
     * @param timeoutMillis The maximum time to wait in milliseconds.
     * @return A Matcher<View> that waits for the view to have the specified text.
     */
    public static Matcher<View> withTextWithinTime(final int stringResId, final long timeoutMillis) {
        return new BaseMatcher<View>() {
            private long startTime;
            private String expectedText;

            @Override
            public boolean matches(Object item) {
                if (!(item instanceof View)) {
                    return false;
                }
                View view = (View) item;
                if (expectedText == null) {
                    expectedText = view.getContext().getString(stringResId);
                }
                if (startTime == 0) {
                    startTime = System.currentTimeMillis();
                }

                // Check if the view currently has the expected text
                if (withText(expectedText).matches(view)) {
                    return true;
                } else {
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    if (elapsedTime < timeoutMillis) {
                        // Not found yet, wait and try again
                        try {
                            Thread.sleep(100); // Small delay before re-checking
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return false; // Keep checking
                    } else {
                        // Timeout reached, text not found
                        return false;
                    }
                }
            }

            @Override
            public void describeMismatch(Object item, Description mismatchDescription) {
                mismatchDescription.appendText("View did not have text '" + expectedText + "' within " + timeoutMillis + " ms.");
                if (item instanceof View) {
                    mismatchDescription.appendText("\nView Details: " + HumanReadables.describe((View) item));
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("view has text '" + expectedText + "' within " + timeoutMillis + " ms");
            }
        };
    }
}
