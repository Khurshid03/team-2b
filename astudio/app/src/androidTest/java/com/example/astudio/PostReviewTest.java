package com.example.astudio.view;

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

import com.example.astudio.R;
import com.example.astudio.controller.ControllerActivity;

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
                return org.hamcrest.Matchers.instanceOf(RatingBar.class);
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
     * Logs in, selects the first hot book, opens the post-review dialog,
     * submits a review, and verifies it appears in the reviews list.
     */
    @Test
    public void postReview_addsReviewToList() {
        // 1) Log in
        typeTextAndCloseKeyboard(R.id.Text_username, "Felix");
        onView(withId(R.id.LoginButton)).perform(click());

        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        // 2) Click the first book in the hot-books RecyclerView
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
        onView(withId(R.id.hot_books_recycler))
            .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // 3) Open the post review dialog
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        onView(withId(R.id.post_review_button)).perform(click());

        // 4) In the dialog: set rating and enter comment
        onView(withId(R.id.dialog_rating_bar)).perform(setRating(4.0f));
        onView(withId(R.id.dialog_comment)).perform(typeText("Great read!"));
        closeSoftKeyboard();

        // 5) Submit the review
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        onView(withId(R.id.dialog_submit_button)).perform(click());

        // 6) Scroll the reviews RecyclerView and verify the comment
        onView(withId(R.id.reviews_recycler))
            .perform(RecyclerViewActions.scrollTo(
                hasDescendant(withText("Great read!"))
            ));
        onView(withText("Great read!")).check(matches(isDisplayed()));
    }
}
