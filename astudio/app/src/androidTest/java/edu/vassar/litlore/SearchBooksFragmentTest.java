package edu.vassar.litlore;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.vassar.litlore.controller.ControllerActivity;
import edu.vassar.litlore.view.SearchBooksFragment;


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

    /**
     * Logs in using email/password and then navigates to the SearchBooksFragment.
     * This sets up the test environment.
     */
    @Before
    public void loginAndOpenSearch() throws InterruptedException {
        // 1) Go to login screen
        onView(withId(R.id.ProceedToLoginButton)).perform(click());

        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        onView(withId(R.id.LoginButton)).check(matches(isDisplayed()));

        // 2) Enter credentials and tap Login
        typeTextAndCloseKeyboard(R.id.textEmail, "felix@gmail.com");
        typeTextAndCloseKeyboard(R.id.textPassword, "Felix123");
        onView(withId(R.id.LoginButton)).perform(click());


        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

        onView(withId(R.id.hot_books_recycler)).check(matches(isDisplayed()));


        // 3) Show SearchBooksFragment directly after successful login
        activityRule.getScenario().onActivity(activity -> {
            activity.mainUI.displayFragment(SearchBooksFragment.newInstance(""));
        });
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        // Verify we are on the SearchBooksFragment
        onView(withId(R.id.search_input)).check(matches(isDisplayed())); // Check for a view unique to SearchBooksFragment
    }

    /**
     * Enters a query, clicks Go, and verifies results RecyclerView is visible.
     */
    @Test
    public void search_validInput_displaysResults() throws InterruptedException {
        typeTextAndCloseKeyboard(R.id.search_input, "Kotlin");
        onView(withId(R.id.go_button)).perform(click());
        Thread.sleep(5000);

        onView(withId(R.id.search_books_recycler))
                .check(matches(isDisplayed()));
    }
}
