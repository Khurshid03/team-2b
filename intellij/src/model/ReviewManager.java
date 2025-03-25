package model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages storage and retrieval of book reviews.
 */
public class ReviewManager {
    private final List<Review> reviews = new ArrayList<>();

    public void addReview(Review review) {
        reviews.add(review);
    }

    public List<Review> getReviewsForBook(Book book) {
        return reviews.stream()
                .filter(r -> r.getBook().equals(book))
                .collect(Collectors.toList());
    }

    public List<Review> getReviewsByUser(User user) {
        return reviews.stream()
                .filter(r -> r.getUser().equals(user))
                .collect(Collectors.toList());
    }
}