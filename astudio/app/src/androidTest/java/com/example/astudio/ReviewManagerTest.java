/**
 * Unit tests for the ReviewManager class.
 *
 * These tests verify that reviews can be posted, updated, and deleted correctly
 * for a given book.
 */
package com.example.astudio;

import static org.junit.Assert.*;

import com.example.astudio.model.Book;
import com.example.astudio.model.Review;
import com.example.astudio.model.ReviewManager;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class ReviewManagerTest {

    private ReviewManager manager;
    private Book testBook;

    /**
     * Sets up a fresh ReviewManager and a sample Book before each test.
     */
    @Before
    public void setUp() {
        manager = new ReviewManager();
        // Create a book with title, author, and description for testing
        testBook = new Book("Test Book Title", "", 0f, "Test Author", "Test Description");
    }

    /**
     * Verifies that posting a review makes it retrievable via fetchReviewsForBook().
     *
     * Uses username "Khurshid", integer rating, and UID "abc123".
     */
    @Test
    public void postReview() {
        Review review = new Review(
                "Khurshid",   // username
                5f,           // integer rating
                "Great read!",
                "",
                testBook.getTitle(),
                "",
                "abc123"      // authorUID
        );

        // Post the review
        manager.postReview(testBook, review, new ReviewManager.OnReviewSavedListener() {
            @Override
            public void onReviewSaved() {
                // Success callback: nothing needed here
            }
            @Override
            public void onReviewSaveFailed(Exception e) {
                fail("postReview() should not fail: " + e.getMessage());
            }
        });

        // Fetch and check that it was stored correctly
        manager.fetchReviewsForBook(testBook, new ReviewManager.OnReviewsFetchedListener() {
            @Override
            public void onFetched(List<Review> reviews) {
                assertEquals(1, reviews.size());
                Review fetched = reviews.get(0);
                assertEquals("Khurshid", fetched.getUsername());
                assertEquals(5f, fetched.getRating(), 0f);
                assertEquals("Great read!", fetched.getComment());
                assertEquals("abc123", fetched.getAuthorUid());
            }
            @Override
            public void onError(String error) {
                fail("fetchReviewsForBook() should succeed, but got error: " + error);
            }
        });
    }

    /**
     * Verifies that updating a review changes its stored rating and comment.
     */
    @Test
    public void updateReview() {
        // Post an initial review
        Review review = new Review("Khurshid", 5f, "Nice book", "r1", testBook.getTitle(), "", "abc123");
        manager.postReview(testBook, review, new ReviewManager.OnReviewSavedListener() {
            @Override public void onReviewSaved() { }
            @Override public void onReviewSaveFailed(Exception e) { fail(e.getMessage()); }
        });

        // Change the review data
        review.setRating(4f);
        review.setComment("Actually pretty good");
        manager.updateReview(review, new ReviewManager.OnReviewUpdatedListener() {
            @Override
            public void onReviewUpdated() { }
            @Override
            public void onReviewUpdateFailed(Exception e) {
                fail("updateReview() should not fail: " + e.getMessage());
            }
        });

        // Fetch and confirm the updates
        manager.fetchReviewsForBook(testBook, new ReviewManager.OnReviewsFetchedListener() {
            @Override
            public void onFetched(List<Review> reviews) {
                assertEquals(1, reviews.size());
                Review updated = reviews.get(0);
                assertEquals(4f, updated.getRating(), 0f);
                assertEquals("Actually pretty good", updated.getComment());
            }
            @Override
            public void onError(String error) {
                fail("fetchReviewsForBook() after update should succeed");
            }
        });
    }

    /**
     * Verifies that deleting a review removes it from storage.
     */
    @Test
    public void deleteReview() {
        // Post a review that will be deleted
        Review review = new Review("Khurshid", 5f, "To be deleted", "r2", testBook.getTitle(), "", "abc123");
        manager.postReview(testBook, review, new ReviewManager.OnReviewSavedListener() {
            @Override public void onReviewSaved() { }
            @Override public void onReviewSaveFailed(Exception e) { fail(e.getMessage()); }
        });

        // Delete the posted review
        manager.deleteReview(review, new ReviewManager.OnReviewDeletedListener() {
            @Override public void onReviewDeleted() { }
            @Override public void onReviewDeleteFailed(Exception e) {
                fail("deleteReview() should not fail: " + e.getMessage());
            }
        });

        // Fetch to ensure it is gone
        manager.fetchReviewsForBook(testBook, new ReviewManager.OnReviewsFetchedListener() {
            @Override
            public void onFetched(List<Review> reviews) {
                assertTrue("Expected no reviews after delete", reviews.isEmpty());
            }
            @Override
            public void onError(String error) {
                fail("fetchReviewsForBook() after delete should succeed");
            }
        });
    }
}