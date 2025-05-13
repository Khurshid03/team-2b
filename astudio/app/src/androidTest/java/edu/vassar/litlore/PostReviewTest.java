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
import android.widget.RatingBar;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Before;

import edu.vassar.litlore.R;
import edu.vassar.litlore.controller.ControllerActivity;

/**
 * Instrumentation tests for the Post Review flow.
 * Logs in a user, navigates to a book's details, opens the post-review dialog,
 * submits a review, and verifies the review appears in the list.
 */
@RunWith(AndroidJUnit4.class)
public class PostReviewTest {

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
     * Custom ViewAction to set a rating on a RatingBar.
     */
    private static ViewAction setRating(final float rating) {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() {
                // Ensure the view is a RatingBar and is displayed
                return org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.instanceOf(RatingBar.class),
                        isDisplayed()
                );
            }
            @Override public String getDescription() {
                return "Set rating on RatingBar";
            }
            @Override public void perform(UiController uiController, View view) {
                ((RatingBar) view).setRating(rating);
            }
        };
    }

    /**
     * Logs in via the real “Proceed to Login” / Login flow and navigates
     * to the BrowseBooksFragment. This is a common setup step for tests
     * that start from the browse screen.
     */
    @Before
    public void setupBrowseFragment() throws InterruptedException {
        // 1) Go to login screen (assuming activity starts on CreateAccountFragment)
        onView(withId(R.id.ProceedToLoginButton)).perform(click());

        // Verify we are on the LoginFragment
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        onView(withId(R.id.LoginButton)).check(matches(isDisplayed()));

        // 2) Enter credentials and tap Login
        typeTextAndCloseKeyboard(R.id.textEmail, "felix@gmail.com");
        typeTextAndCloseKeyboard(R.id.textPassword, "Felix123");
        onView(withId(R.id.LoginButton)).perform(click());

        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

        // Verify we are on the BrowseBooksFragment
        onView(withId(R.id.hot_books_recycler)).check(matches(isDisplayed()));
    }


    /**
     * Logs in, selects the first hot book, opens the post-review dialog,
     * submits a review, and verifies it appears in the reviews list.
     */
    @Test
    public void postReview_addsReviewToList() {

        // 3) Click the first book in the hot-books RecyclerView (position 0)
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
        onView(withId(R.id.hot_books_recycler))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

        // 4) Open the post review dialog
        onView(withId(R.id.post_review_button)).perform(click());

        // 5) In the dialog: set rating and enter comment
        onView(withId(R.id.dialog_rating_bar)).perform(setRating(4.0f));
        onView(withId(R.id.dialog_comment)).perform(typeText("Great read!"));
        closeSoftKeyboard();

        // 6) Submit the review
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        onView(withId(R.id.dialog_submit_button)).perform(click());

        try { Thread.sleep(4000); } catch (InterruptedException ignored) {}
        onView(withId(R.id.reviews_recycler))
                .perform(RecyclerViewActions.scrollTo(
                        hasDescendant(withText("Great read!"))
                ));

        // Verify the comment text is displayed after scrolling
        onView(withText("Great read!")).check(matches(isDisplayed()));

    }
}
