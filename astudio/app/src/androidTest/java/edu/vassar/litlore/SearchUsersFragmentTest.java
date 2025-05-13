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

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Before; // Import Before

import edu.vassar.litlore.R;
import edu.vassar.litlore.controller.ControllerActivity;


@RunWith(AndroidJUnit4.class)
public class SearchUsersFragmentTest {

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
     * This sets up the test environment.
     */
    @Before
    public void setupSearchUsersFragment() throws InterruptedException {
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

        onView(withId(R.id.hot_books_recycler)).check(matches(isDisplayed()));

        // 3) Navigate to Search Users Fragment using the Bottom Navigation View
        onView(withId(R.id.search_users)).perform(click());
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        onView(withId(R.id.search_users_recycler)).check(matches(isDisplayed()));
    }

    /**
     * Enters a query, clicks Go, and verifies results RecyclerView is visible.
     * This test assumes that the search API call will return results.
     */
    @Test
    public void searchUsers_validInput_displaysResults() throws InterruptedException {
        typeTextAndCloseKeyboard(R.id.search_input, "Tanish");
        onView(withId(R.id.go_button)).perform(click());
        // Wait for network call and RecyclerView update
        Thread.sleep(5000);

        onView(withId(R.id.search_users_recycler))
                .check(matches(isDisplayed()));

    }
}
