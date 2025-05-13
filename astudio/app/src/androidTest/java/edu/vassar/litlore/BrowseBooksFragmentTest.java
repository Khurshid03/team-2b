package edu.vassar.litlore;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import edu.vassar.litlore.R;
import edu.vassar.litlore.controller.ControllerActivity;
import edu.vassar.litlore.model.Book;
import edu.vassar.litlore.view.BrowseBooksFragment;
import edu.vassar.litlore.view.BrowseBooksUI;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * End-to-end tests for browsing books, navigating through the create-account flow.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class BrowseBooksFragmentTest {

    @Rule
    public ActivityScenarioRule<ControllerActivity> activityRule =
            new ActivityScenarioRule<>(ControllerActivity.class);

    private TestBrowseListener listener;

    /**
     * Performs create-account flow to land on BrowseBooksFragment, then attaches a listener.
     */
    @Before
    public void setUp() {
        listener = new TestBrowseListener();
        // 1) Fill and submit CreateAccountFragment
        onView(withId(R.id.createAccountUsername)).perform(typeText("Felix"));
        closeSoftKeyboard();
        onView(withId(R.id.CreateAccountEmail)).perform(typeText("felix@example.com"));
        closeSoftKeyboard();
        onView(withId(R.id.CreateAccountPassword)).perform(typeText("pass123"));
        closeSoftKeyboard();
        onView(withId(R.id.CreateAccountButton)).perform(click());
        // 2) Force display BrowseBooksFragment synchronously to ensure it's present
        activityRule.getScenario().onActivity(activity -> {
            BrowseBooksFragment frag = new BrowseBooksFragment();
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainerView, frag)
                    .commitNow();
            // Re-attach our test listener after onAttach override
            BrowseBooksFragment attached = (BrowseBooksFragment) activity.getSupportFragmentManager()
                    .findFragmentById(R.id.fragmentContainerView);
            attached.setListener(listener);
        });
    }

    /**
     * Verifies welcome message is visible after navigation.
     */
    @Test
    public void welcomeMessage_isVisible() {
        onView(withId(R.id.welcome_message))
                .check(matches(withEffectiveVisibility(VISIBLE)));
    }

    /**
     * Verifies hot books list is visible.
     */
    @Test
    public void hotBooksRecycler_isVisible() {
        onView(withId(R.id.hot_books_recycler))
                .check(matches(withEffectiveVisibility(VISIBLE)));
    }

    /**
     * Verifies genre buttons list is visible.
     */
    @Test
    public void genreButtonsRecycler_isVisible() {
        onView(withId(R.id.genre_button_recycler))
                .check(matches(withEffectiveVisibility(VISIBLE)));
    }

    /**
     * Verifies genre books grid is visible.
     */
    @Test
    public void genreBooksRecycler_isVisible() {
        onView(withId(R.id.genre_books_recycler))
                .check(matches(withEffectiveVisibility(VISIBLE)));
    }

    /**
     * Tapping the "Mystery" genre button fires the listener callback.
     */
    @Test
    public void genreClick_triggersListener() {
        // Click the "Mystery" button within the genre RecyclerView
        onView(allOf(
                withId(R.id.genreButton),
                withText("Mystery"),
                isDescendantOfA(withId(R.id.genre_button_recycler))
        )).perform(click());
        assertTrue("Listener should be called when clicking Mystery", listener.genreCalled);
    }
    private static class TestBrowseListener implements BrowseBooksUI.BrowseBooksListener {
        boolean genreCalled = false;
        @Override public void onBookSelected(Book book) {}
        @Override public void onGenreSelected(String genre) { genreCalled = true; }
    }
}
