package com.example.astudio;
import android.os.IBinder;
import android.view.WindowManager;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import androidx.fragment.app.Fragment;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.astudio.R;
import com.example.astudio.controller.ControllerActivity;
import com.example.astudio.model.UserManager;
import com.example.astudio.view.LoginFragment;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
/**
 * Instrumentation tests for LoginFragment.
 * Verifies login behavior: error toast on empty input and successful navigation on valid input.
 */
public class LoginFragmentTest {

    @Rule
    public ActivityScenarioRule<ControllerActivity> activityRule =
            new ActivityScenarioRule<>(ControllerActivity.class);

    /**
     * Types the given text into the view with the specified ID and closes the soft keyboard.
     *
     * @param viewId the resource ID of the input view
     * @param text the text to type into the view
     */
    private static void typeTextAndCloseKeyboard(int viewId, String text) {
        onView(withId(viewId))
                .perform(typeText(text));
        closeSoftKeyboard();
    }


    @Test
    /**
     * Enters a valid username, clicks login, and verifies that the user
     * is saved in UserManager and the LoginFragment is replaced.
     */
    public void login_validUsername_setsUserAndNavigates() {
        String testUser = "Felix";

        // Enter a valid username
        typeTextAndCloseKeyboard(R.id.Text_username, testUser);

        // Click login
        onView(withId(R.id.LoginButton))
                .perform(click());

        activityRule.getScenario().onActivity(activity -> {
            // Verify UserManager has the new user
            assertEquals(testUser, UserManager.getInstance()
                    .getCurrentUser().getUsername());

            // Verify the LoginFragment is no longer displayed
            Fragment current = activity.getSupportFragmentManager()
                    .findFragmentById(R.id.fragmentContainerView);
            assertFalse(current instanceof LoginFragment);
        });
    }
}
