package com.example.astudio;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertTrue;

import android.view.View;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.recyclerview.widget.RecyclerView; // Import RecyclerView
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.NoMatchingViewException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Before;

import com.example.astudio.R;
import com.example.astudio.controller.ControllerActivity;

import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;

@RunWith(AndroidJUnit4.class)
public class ViewSavedBooksFragmentTest {

    @Rule
    public ActivityScenarioRule<ControllerActivity> activityRule =
            new ActivityScenarioRule<>(ControllerActivity.class);

    /** Types text into a view and closes the keyboard. */
    private static void typeTextAndCloseKeyboard(int viewId, String text) {
        onView(withId(viewId)).perform(typeText(text));
        Espresso.closeSoftKeyboard(); // Use Espresso.closeSoftKeyboard()
    }

    /**
     * Logs in using email/password and then navigates to the BrowseBooksFragment before the test.
     * This sets up the test environment.
     */
    @Before
    public void setupBrowseFragment() throws InterruptedException {
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
        onView(withId(R.id.hot_books_recycler)).check(matches(isDisplayed()));
    }

    /**
     * Tests the save and unsave book functionality by navigating to a book,
     * saving it, verifying it's in the saved list, unsaving it, and
     * verifying it's removed from the saved list.
     */
    @Test
    public void saveAndUnsaveBook_updatesSavedList() throws InterruptedException {

        /** 1) Click the first book in the hot-books RecyclerView (position 0) */
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
        onView(withId(R.id.hot_books_recycler))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

        /** Get the title of the book being viewed using a custom ViewAssertion */
        final String[] bookTitle = {null};
        onView(withId(R.id.book_title))
                .check(new ViewAssertion() {
                    @Override
                    public void check(View view, NoMatchingViewException noViewFoundException) throws AssertionError {
                        if (view instanceof TextView) {
                            bookTitle[0] = ((TextView) view).getText().toString();
                        } else {
                            throw new AssertionError("View is not a TextView");
                        }
                    }
                });
        assertTrue("Could not get book title from ViewBookFragment", bookTitle[0] != null && !bookTitle[0].isEmpty());


        /** 2) Click the Save button */
        onView(withId(R.id.saved_books_button)).perform(click());

        /** Wait for the save operation to complete and UI to update */
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

        /** Verify the button text changes to "Saved" */
        onView(withId(R.id.saved_books_button)).check(matches(withText(R.string.saved)));

        /** 3) Navigate to Saved Books Fragment using the Bottom Navigation View */
        onView(withId(R.id.nav_saved_books)).perform(click());

        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

        onView(withId(R.id.savedBooksRecyclerView)).check(matches(isDisplayed()));
        onView(withId(R.id.savedBooksRecyclerView)).check(matches(hasDescendant(withText(bookTitle[0]))));


        /** 4) Navigate back to the ViewBookFragment */
        Espresso.pressBack();
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        /** Verify we are back on the ViewBookFragment */
        onView(withId(R.id.post_review_button)).check(matches(isDisplayed()));


        /** 5) Click the Saved button to Unsave */
        onView(withId(R.id.saved_books_button)).perform(click());
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

        /** Verify the button text changes back to "Save" */
        onView(withId(R.id.saved_books_button)).check(matches(withText(R.string.save)));


        /** 6) Navigate back to Saved Books Fragment */
        onView(withId(R.id.nav_saved_books)).perform(click());
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

        /** Verify we are on the ViewSavedBooksFragment */
        onView(withId(R.id.savedBooksRecyclerView)).check(matches(isDisplayed()));

        /** Verify the book is no longer in the list by checking for its title */
        onView(withId(R.id.savedBooksRecyclerView)).check(matches(not(hasDescendant(withText(bookTitle[0])))));
    }
}
