package edu.vassar.litlore;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.core.app.ActivityScenario;

import edu.vassar.litlore.R;
import edu.vassar.litlore.controller.ControllerActivity;
import edu.vassar.litlore.view.CreateAccountFragment;
import edu.vassar.litlore.view.CreateAccountUI;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumentation test suite for {@link CreateAccountFragment} hosted within {@link ControllerActivity}.
 * <p>Validates that the fragment correctly invokes its listener methods when
 * interacting with the UI components, including account creation and navigation to login.</p>
 */
@RunWith(AndroidJUnit4.class)
public class CreateAccountFragmentTest {
    private ActivityScenario<ControllerActivity> scenario;
    private TestCreateAccountListener listener;

    /**
     * Sets up the test environment by launching {@link ControllerActivity} and
     * attaching a test listener to the {@link CreateAccountFragment}.
     */
    @Before
    public void setUp() {
        listener = new TestCreateAccountListener();
        // Launch controller, which by default shows CreateAccountFragment
        scenario = ActivityScenario.launch(ControllerActivity.class);
        // Attach our listener to the CreateAccountFragment
        scenario.onActivity(activity -> {
            CreateAccountFragment frag = (CreateAccountFragment)
                    activity.getSupportFragmentManager()
                            .findFragmentById(R.id.fragmentContainerView);
            frag.setListener(listener);
        });
    }

    /**
     * Verifies that providing valid input triggers {@link CreateAccountUI.CreateAccountListener#onCreateAccount}
     * with the correct username, email, and password values.
     */
    @Test
    public void createAccount_withValidInput_callsListener() {
        String username = "bob";
        String email = "bob@example.com";
        String password = "hunter2";

        onView(withId(R.id.createAccountUsername))
                .perform(typeText(username), closeSoftKeyboard());
        onView(withId(R.id.CreateAccountEmail))
                .perform(typeText(email), closeSoftKeyboard());
        onView(withId(R.id.CreateAccountPassword))
                .perform(typeText(password), closeSoftKeyboard());

        onView(withId(R.id.CreateAccountButton)).perform(click());

        assertTrue("onCreateAccount should fire", listener.called);
        assertEquals(username, listener.username);
        assertEquals(email, listener.email);
        assertEquals(password, listener.password);
    }

    /**
     * Verifies that clicking the create account button with empty fields
     * still invokes {@link CreateAccountUI.CreateAccountListener#onCreateAccount}
     * with empty strings.
     */
    @Test
    public void createAccount_withEmptyInput_callsListenerWithEmpties() {
        onView(withId(R.id.CreateAccountButton)).perform(click());

        assertTrue("onCreateAccount should fire even when empty", listener.called);
        assertEquals("", listener.username);
        assertEquals("", listener.email);
        assertEquals("", listener.password);
    }

    /**
     * Verifies that clicking the "Proceed to Login" button invokes
     * {@link CreateAccountUI.CreateAccountListener#onProceedToLogin()}.
     */
    @Test
    public void proceedToLoginButton_triggersOnProceedToLogin() {
        onView(withId(R.id.ProceedToLoginButton)).perform(click());
        assertTrue("onProceedToLogin should fire", listener.proceeded);
    }

    /**
     * Test implementation of {@link CreateAccountUI.CreateAccountListener} that
     * captures callback invocations for assertions.
     */
    private static class TestCreateAccountListener implements CreateAccountUI.CreateAccountListener {
        boolean called = false;
        String username, email, password;
        boolean proceeded = false;

        /**
         * Called when the create account button is pressed.
         * @param u the username entered in the fragment
         * @param e the email entered in the fragment
         * @param p the password entered in the fragment
         * @param ui the fragment instance
         */
        @Override
        public void onCreateAccount(String u, String e, String p, CreateAccountUI ui) {
            called = true;
            username = u; email = e; password = p;
        }

        /**
         * Called when the proceed-to-login button is pressed.
         */
        @Override
        public void onProceedToLogin() {
            proceeded = true;
        }
    }
}
