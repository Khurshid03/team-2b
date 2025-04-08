package com.example.astudio.controller;


import com.example.astudio.model.Book;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller that manages the book review flow.
 */
public class ReviewController {

    private final ReviewManager reviewManager;
    private final CmdLineUI view;

    public ReviewController() {
        this.reviewManager = new ReviewManager();
        this.view = new CmdLineUI();
    }

    /**
     * Handles the full review process.
     */
    public void runReviewFlow() {
        view.showMessage("=== Welcome to the Book Review App ===");

        // 1. Register user
        String username = view.promptUsername();
        String email = view.promptEmail();
        User user = new User(username, email);
        view.showMessage("HELLO, " + user.getUsername() + "!");

        // 2. Show genres
        List<String> genres = new ArrayList<>(Book.getAvailableGenres());
        view.showGenres(genres);

        List<Book> booksInGenre = new ArrayList<>();
        String selectedGenre;

        do {
            selectedGenre = view.promptGenre();
            booksInGenre = Book.getBooksByGenre(selectedGenre);

            if (booksInGenre.isEmpty()) {
                view.showMessage("❌ Genre not found or no books available. Please try again.");
            }

        } while (booksInGenre.isEmpty());

        view.showMessage("\n Great choice there " + user.getUsername() + "!");


        // 3. Show books in selected genre
        view.showBooksInGenre(booksInGenre);
        Book selectedBook = null;

        do {
            try {
                int bookID = view.promptBookId();
                selectedBook = Book.getBookById(bookID);

                if (selectedBook == null) {
                    view.showMessage("❌ Book ID not found. Please try again.");
                }

            } catch (NumberFormatException e) {
                view.showMessage("❌ Invalid input. Please enter a valid numeric book ID.");
            }

        } while (selectedBook == null);

        // show selected book
        view.showSelectedBook(selectedBook);



        // 4. Get review input
        double rating = view.promptRating();
        String comment = view.promptComment();

        Review review = user.writeReview(selectedBook, rating, comment);
        reviewManager.addReview(review);
        view.showMessage("\n✅ Review submitted successfully!");

        // 5. Show reviews for book
        List<Review> reviews = reviewManager.getReviewsForBook(selectedBook);
        view.showReviews(reviews, selectedBook.getTitle());
    }
}
