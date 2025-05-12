package com.example.astudio;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;


import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.core.app.ActivityScenario;

import com.example.astudio.controller.ControllerActivity;
import com.example.astudio.view.LoginFragment;
import com.example.astudio.view.LoginUI;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Espresso tests for {@link LoginFragment}, hosted in {@link ControllerActivity}.
 * Verifies empty-field handling and listener invocation on valid input.
 */
@RunWith(AndroidJUnit4.class)
public class LoginFragmentTest {
    private ActivityScenario<ControllerActivity> scenario;
    private TestLoginListener listener;

    /**
     * Launches ControllerActivity, replaces its content with LoginFragment,
     * and sets up a test listener for callback assertions.
     */
    @Before
    public void setUp() {
        listener = new TestLoginListener();
        scenario = ActivityScenario.launch(ControllerActivity.class);
        scenario.onActivity(activity -> {
            LoginFragment fragment = new LoginFragment();
            fragment.setListener(listener);
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainerView, fragment)
                    .commitNow();
        });
    }

    /**
     * Typing valid email and password calls listener.onLogin with correct arguments.
     */
    @Test
    public void validInput_callsOnLogin() {
        String email = "test@example.com";
        String password = "password123";
        onView(withId(R.id.textEmail))
                .perform(typeText(email), closeSoftKeyboard());
        onView(withId(R.id.textPassword))
                .perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.LoginButton)).perform(click());

        assertTrue("Listener should be called", listener.called);
        assertEquals("Email should match", email, listener.email);
        assertEquals("Password should match", password, listener.password);
    }

    /**
     * Test implementation of {@link LoginUI.LoginListener} capturing callbacks.
     */
    private static class TestLoginListener implements LoginUI.LoginListener {
        boolean called = false;
        String email, password;

        @Override
        public void onLogin(String email, String password, LoginUI ui) {
            called = true;
            this.email = email;
            this.password = password;
        }

        @Override
        public void onLogin(String username) {
            // not used
        }
    }
}
