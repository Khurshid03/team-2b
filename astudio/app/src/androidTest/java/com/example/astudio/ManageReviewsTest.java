package com.example.astudio;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.view.View;
import android.widget.RatingBar;
import androidx.recyclerview.widget.RecyclerView; // Import RecyclerView

import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.util.HumanReadables;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers; // Import Matchers for allOf
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Before; // Import Before

import com.example.astudio.R;
import com.example.astudio.controller.ControllerActivity;

/**
 * Instrumentation tests for managing reviews (editing and deleting).
 * Logs in a user, navigates to a book, posts a review, then tests editing and deleting that review.
 */
@RunWith(AndroidJUnit4.class)
public class ManageReviewsTest {

    @Rule
    public ActivityScenarioRule<ControllerActivity> activityRule =
            new ActivityScenarioRule<>(ControllerActivity.class);

    private static final String TEST_REVIEW_COMMENT = "This is a test review.";
    private static final String EDITED_REVIEW_COMMENT = "This review has been edited.";
    private static final float TEST_REVIEW_RATING = 4.0f;
    private static final float EDITED_REVIEW_RATING = 5.0f;

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
                return Matchers.allOf(
                        Matchers.instanceOf(RatingBar.class),
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
     * Tests editing a review posted by the logged-in user.
     */
    @Test
    public void editReview_updatesReviewInList() {

        // 1) Click the first book in the hot-books RecyclerView (position 0)
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {} // Wait for hot books to load
        onView(withId(R.id.hot_books_recycler))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // Wait for ViewBookFragment to appear and its data to load.
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

        // 2) Post a review if one doesn't exist by this user.
        postTestReview(TEST_REVIEW_RATING, TEST_REVIEW_COMMENT);
        try { Thread.sleep(5000); } catch (InterruptedException ignored) {}


        // 3) Find the test review in the list and click its Edit button
        onView(withId(R.id.reviews_recycler))
                .perform(RecyclerViewActions.actionOnItem(
                        hasDescendant(withText(TEST_REVIEW_COMMENT)),
                        clickChildViewWithId(R.id.editReviewButton)
                ));

        // Wait for the Edit Review dialog to appear
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        onView(withText(R.string.edit_review)).check(matches(isDisplayed()));

        // 4) In the dialog: change rating and comment
        onView(withId(R.id.edit_review_rating)).perform(setRating(EDITED_REVIEW_RATING));
        onView(withId(R.id.edit_review_comment)).perform(typeText(EDITED_REVIEW_COMMENT));
        closeSoftKeyboard();

        // 5) Click the dialog's Positive button (OK/Yes)
        onView(withText(android.R.string.ok)).perform(click());
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
    }

    /**
     * Tests deleting a review posted by the logged-in user.
     */
    @Test
    public void deleteReview_removesReviewFromList() {

        // 1) Click the first book in the hot-books RecyclerView (position 0)
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {} // Wait for hot books to load
        onView(withId(R.id.hot_books_recycler))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

        // 2) Post a review if one doesn't exist by this user.
        postTestReview(TEST_REVIEW_RATING, TEST_REVIEW_COMMENT + "_to_delete");

        try { Thread.sleep(5000); } catch (InterruptedException ignored) {}

        // 3) Find the test review in the list and click its Delete button
        onView(withId(R.id.reviews_recycler))
                .perform(RecyclerViewActions.actionOnItem(
                        hasDescendant(withText(TEST_REVIEW_COMMENT + "_to_delete")),
                        clickChildViewWithId(R.id.deleteReviewButton)
                ));

        // Wait for the Delete Confirmation dialog to appear
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        onView(withText(R.string.confirm_delete_review)).check(matches(isDisplayed())); // Check dialog message

        // 4) Click the dialog's Positive button (Yes)
        onView(withText(android.R.string.yes)).perform(click());

        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

        onView(withId(R.id.reviews_recycler))
                .check(matches(doesNotHaveDescendantWithTextWithinTime(TEST_REVIEW_COMMENT + "_to_delete", 15000))); // Increased timeout to 15 seconds

        // Verify the deleted comment text is no longer displayed
        onView(withText(TEST_REVIEW_COMMENT + "_to_delete")).check(doesNotExist());
    }


    /**
     * Helper method to post a test review for the currently viewed book.
     * Assumes ViewBookFragment is currently displayed.
     */
    private void postTestReview(float rating, String comment) {
        onView(withId(R.id.post_review_button)).perform(click());
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        onView(withId(R.id.dialog_rating_bar)).check(matches(isDisplayed())); // Check for a view unique to the dialog
        onView(withId(R.id.dialog_rating_bar)).perform(setRating(rating));
        onView(withId(R.id.dialog_comment)).perform(typeText(comment));
        closeSoftKeyboard();

        // Submit the review
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {} // Small wait before clicking submit
        onView(withId(R.id.dialog_submit_button)).perform(click());
    }


    /**
     * Custom ViewAction to click a child view within a RecyclerView item.
     * Useful for clicking buttons (like Edit/Delete) inside list items.
     */
    public static ViewAction clickChildViewWithId(final int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return Matchers.allOf(isDisplayed(), new BaseMatcher<View>() {
                    @Override
                    public boolean matches(Object item) {
                        // Check if the item is a View and contains a child with the specified ID
                        if (item instanceof View) {
                            return ((View) item).findViewById(id) != null;
                        }
                        return false;
                    }

                    @Override
                    public void describeMismatch(Object item, Description mismatchDescription) {
                        mismatchDescription.appendText("No child view with id " + id + " found in item.");
                    }

                    @Override
                    public void describeTo(Description description) {
                        description.appendText("has child view with id " + id);
                    }
                });
            }

            @Override
            public String getDescription() {
                return "Click on a child view with specified ID.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                View childView = view.findViewById(id);
                if (childView != null) {
                    childView.performClick();
                } else {
                    throw new PerformException.Builder()
                            .withActionDescription(this.getDescription())
                            .withViewDescription(HumanReadables.describe(view))
                            .withCause(new RuntimeException("Child view with id " + id + " not found."))
                            .build();
                }
            }
        };
    }

    /**
     * Custom matcher that waits for a RecyclerView to contain a descendant view
     * matching a given text matcher within a specified timeout.
     *
     * @param text The text to search for within a descendant TextView.
     * @param timeoutMillis The maximum time to wait in milliseconds.
     * @return A Matcher<View> that waits for the descendant to appear.
     */
    public static Matcher<View> hasDescendantWithTextWithinTime(final String text, final long timeoutMillis) {
        return new BaseMatcher<View>() {
            private long startTime;

            @Override
            public boolean matches(Object item) {
                if (!(item instanceof RecyclerView)) {
                    return false;
                }
                RecyclerView recyclerView = (RecyclerView) item;
                if (startTime == 0) {
                    startTime = System.currentTimeMillis();
                }

                // Check if the RecyclerView currently has a descendant with the specified text
                if (hasDescendant(withText(text)).matches(recyclerView)) {
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
                        // Timeout reached, descendant not found
                        return false;
                    }
                }
            }

            @Override
            public void describeMismatch(Object item, Description mismatchDescription) {
                mismatchDescription.appendText("RecyclerView did not contain a descendant with text '" + text + "' within " + timeoutMillis + " ms.");
                if (item instanceof View) {
                    mismatchDescription.appendText("\nView Details: " + HumanReadables.describe((View) item));
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("RecyclerView has descendant with text '" + text + "' within " + timeoutMillis + " ms");
            }
        };
    }

    /**
     * Custom matcher that waits for a RecyclerView to NOT contain a descendant view
     * matching a given text matcher within a specified timeout.
     *
     * @param text The text to search for within a descendant TextView (expecting it to disappear).
     * @param timeoutMillis The maximum time to wait in milliseconds.
     * @return A Matcher<View> that waits for the descendant to disappear.
     */
    public static Matcher<View> doesNotHaveDescendantWithTextWithinTime(final String text, final long timeoutMillis) {
        return new BaseMatcher<View>() {
            private long startTime;

            @Override
            public boolean matches(Object item) {
                if (!(item instanceof RecyclerView)) {
                    return false;
                }
                RecyclerView recyclerView = (RecyclerView) item;
                if (startTime == 0) {
                    startTime = System.currentTimeMillis();
                }

                // Check if the RecyclerView currently has a descendant with the specified text
                if (!hasDescendant(withText(text)).matches(recyclerView)) {
                    return true; // It's gone, success!
                } else {
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    if (elapsedTime < timeoutMillis) {
                        // Still found, wait and try again
                        try {
                            Thread.sleep(100); // Small delay before re-checking
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return false; // Keep checking
                    } else {
                        // Timeout reached, descendant is still present
                        return false;
                    }
                }
            }

            @Override
            public void describeMismatch(Object item, Description mismatchDescription) {
                mismatchDescription.appendText("RecyclerView still contained a descendant with text '" + text + "' after " + timeoutMillis + " ms.");
                if (item instanceof View) {
                    mismatchDescription.appendText("\nView Details: " + HumanReadables.describe((View) item));
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("RecyclerView does not have descendant with text '" + text + "' within " + timeoutMillis + " ms");
            }
        };
    }
}
