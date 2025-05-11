package com.example.astudio.persistence;

import android.util.Log;

import com.example.astudio.model.Book;
import com.example.astudio.model.Review;
import com.example.astudio.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * FirestoreFacade provides a centralized API for all Firebase Firestore database operations.
 * It encapsulates interactions with various collections and subcollections like Users, Reviews, and SavedBooks.
 */
public class FirestoreFacade {
    private static final String TAG = "FirestoreFacade";
    private final FirebaseFirestore db;

    // Collection/subcollection names
    private static final String USERS_COLLECTION = "Users";
    private static final String REVIEWS_COLLECTION = "Reviews";
    private static final String USER_REVIEWS_SUBCOLLECTION = "UserReviews";
    private static final String SAVED_BOOKS_SUBCOLLECTION = "SavedBooks";
    private static final String FOLLOW_SUBCOLLECTION = "Follow";

    /**
     * Constructs a new FirestoreFacade instance, initializing the FirebaseFirestore database connection.
     */
    public FirestoreFacade() {
        db = FirebaseFirestore.getInstance();
    }

    // --- User Management ---

    /**
     * Listener for fetching a single user profile.
     */
    public interface OnUserProfileFetchedListener {
        /**
         * Called when the user profile is successfully fetched.
         * @param user The fetched {@link User} object, or null if not found.
         */
        void onFetched(User user);
        /**
         * Called when an error occurs during fetching.
         * @param error A descriptive error message.
         */
        void onError(String error);
    }

    /**
     * Listener for searching users.
     */
    public interface OnUserSearchListener   {
        /**
         * Called when user search results are successfully fetched.
         * @param users A list of matching {@link User} objects.
         */
        void onResults(List<User> users);
        /**
         * Called when an error occurs during searching.
         * @param error A descriptive error message.
         */
        void onError(String error);
    }

    /**
     * Saves a new user document to the "Users" collection.
     * @param uid The unique ID for the user (typically Firebase Auth UID).
     * @param username The desired username.
     * @param email The user's email address.
     * @param onSuccess Runnable to execute on successful save.
     * @param onFailure OnFailureListener to handle save failure.
     */
    public void saveNewUser(String uid, String username, String email,
                            Runnable onSuccess, OnFailureListener onFailure) {
        Map<String,Object> data = new HashMap<>();
        data.put("username", username);
        data.put("email", email);
        data.put("bio", ""); // Initialize bio as empty string
        db.collection(USERS_COLLECTION).document(uid)
                .set(data)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(onFailure);
    }

    /**
     * Fetches a user profile by their unique ID.
     * @param uid The unique ID of the user.
     * @param listener The callback listener for the fetch operation.
     */
    public void fetchUserProfile(String uid, OnUserProfileFetchedListener listener) {
        db.collection(USERS_COLLECTION).document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            user.setId(documentSnapshot.getId()); // Set the Firestore document ID as the user ID
                            listener.onFetched(user);
                        } else {
                            Log.e(TAG, "Failed to parse user data for UID: " + uid);
                            listener.onError("Failed to parse user data.");
                        }
                    } else {
                        Log.w(TAG, "User document not found for UID: " + uid);
                        listener.onFetched(null); // User not found
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user profile for UID: " + uid, e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Fetches the username for a given user ID.
     * @param uid The unique ID of the user.
     * @param onSuccess Consumer to accept the username string on success.
     * @param onFailure OnFailureListener to handle fetch failure or if username is not found.
     */
    public void fetchUsernameForUid(String uid, Consumer<String> onSuccess, OnFailureListener onFailure) {
        db.collection(USERS_COLLECTION)
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        if (username != null && !username.isEmpty()) {
                            onSuccess.accept(username);
                        } else {
                            Log.e(TAG, "Username is null or empty for UID: " + uid);
                            onFailure.onFailure(new Exception("Username not found for user."));
                        }
                    } else {
                        Log.w(TAG, "User document not found for UID (fetchUsernameForUid): " + uid);
                        onFailure.onFailure(new Exception("User document not found."));
                    }
                })
                .addOnFailureListener(onFailure);
    }

    /**
     * Searches for users whose usernames start with the given query string.
     * @param query The search query string.
     * @param listener The callback listener for the search operation.
     */
    public void searchUsers(String query, OnUserSearchListener listener) {
        db.collection(USERS_COLLECTION)
                .orderBy("username")
                .startAt(query)
                .endAt(query + "\uf8ff") // Unicode character to match all strings starting with query
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> results = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        if (user != null) {
                            user.setId(document.getId()); // Set the Firestore document ID
                            results.add(user);
                        } else {
                            Log.w(TAG, "Null user object encountered during search for query: " + query);
                        }
                    }
                    listener.onResults(results);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error searching users with query: " + query, e);
                    listener.onError(e.getMessage());
                });
    }

    // --- Review Management ---

    /**
     * Listener for fetching a list of reviews.
     */
    public interface OnReviewsFetchedListener {
        /**
         * Called when reviews are successfully fetched.
         * @param reviews A list of {@link Review} objects.
         */
        void onFetched(List<Review> reviews);
        /**
         * Called when an error occurs during fetching.
         * @param error A descriptive error message.
         */
        void onError(String error);
    }

    /**
     * Submits a new review for a specific book.
     * The review is saved to a subcollection under the book's document.
     * @param book The {@link Book} the review is for.
     * @param review The {@link Review} object to submit.
     * @param onSuccess Consumer to accept the newly created review document ID on success.
     * @param onFailure OnFailureListener to handle submission failure.
     */
    public void submitReview(Book book, Review review,
                             Consumer<String> onSuccess, OnFailureListener onFailure) {
        Map<String,Object> reviewData = new HashMap<>();
        reviewData.put("username", review.getUsername());
        reviewData.put("rating",   review.getRating());
        reviewData.put("comment",  review.getComment());
        reviewData.put("timestamp", System.currentTimeMillis());
        reviewData.put("thumbnailUrl", review.getThumbnailUrl());
        reviewData.put("authorUid", review.getAuthorUid());

        db.collection(REVIEWS_COLLECTION)
                .document(book.getTitle()) // Using book title as document ID
                .collection(USER_REVIEWS_SUBCOLLECTION)
                .add(reviewData)
                .addOnSuccessListener(documentReference -> onSuccess.accept(documentReference.getId()))
                .addOnFailureListener(onFailure);
    }

    /**
     * Fetches all reviews for a specific book.
     * Reviews are ordered by timestamp in descending order.
     * @param book The {@link Book} to fetch reviews for.
     * @param listener The callback listener for the fetch operation.
     */
    public void fetchReviewsForBook(Book book, OnReviewsFetchedListener listener) {
        String bookId = book.getTitle(); // Assuming book title is used as document ID
        Log.d(TAG, "fetchReviewsForBook: Attempting to fetch reviews for book ID: " + bookId);
        db.collection(REVIEWS_COLLECTION)
                .document(bookId)
                .collection(USER_REVIEWS_SUBCOLLECTION)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Review> reviewList = new ArrayList<>();
                    Log.i(TAG, "fetchReviewsForBook: SUCCESS. Found " + queryDocumentSnapshots.size() + " review documents for book '" + bookId + "'. Processing...");
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Log.d(TAG, "fetchReviewsForBook: Processing document ID: " + document.getId());
                        try {
                            Review parsedReview = parseReviewDocument(document);
                            if (parsedReview != null) {
                                reviewList.add(parsedReview);
                                Log.d(TAG, "fetchReviewsForBook: Successfully parsed and added review ID: " + parsedReview.getReviewId());
                            } else {
                                Log.w(TAG, "fetchReviewsForBook: parseReviewDocument returned null for doc ID: " + document.getId());
                            }
                        } catch(Exception ex) {
                            Log.e(TAG, "fetchReviewsForBook: EXCEPTION while parsing review document ID: " + document.getId(), ex);
                        }
                    }
                    Log.i(TAG, "fetchReviewsForBook: Finished processing. Parsed " + reviewList.size() + " reviews for book '" + bookId + "'.");
                    listener.onFetched(reviewList);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "fetchReviewsForBook: FAILURE for book '" + bookId + "'. Error: " + e.getMessage(), e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Fetches all reviews posted by a specific username using a collection group query.
     * @param username The username of the author.
     * @param listener The callback listener for the fetch operation.
     */
    public void fetchUserReviewsByUsername(String username, OnReviewsFetchedListener listener) {
        Log.i(TAG, "fetchUserReviewsByUsername: Attempting to fetch reviews for username: '" + username + "' (Query simplified - no orderBy)");
        db.collectionGroup(USER_REVIEWS_SUBCOLLECTION)
                .whereEqualTo("username", username)
                // .orderBy("timestamp", Query.Direction.DESCENDING) // Temporarily commented out for debugging
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Review> reviewList = new ArrayList<>();
                    Log.i(TAG, "fetchUserReviewsByUsername: SUCCESS (simplified query). Found " + queryDocumentSnapshots.size() + " review documents for username '" + username + "'. Processing...");
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.w(TAG, "fetchUserReviewsByUsername: No review documents found for username '" + username + "' with simplified query. This strongly points to a data mismatch (e.g., case sensitivity in 'username' field or field name itself) or no reviews by this user.");
                    }
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Log.d(TAG, "fetchUserReviewsByUsername: Processing document ID: " + document.getId() + ", Path: " + document.getReference().getPath());
                        try {
                            Review parsedReview = parseReviewDocument(document);
                            if (parsedReview != null) {
                                reviewList.add(parsedReview);
                                Log.d(TAG, "fetchUserReviewsByUsername: Successfully parsed and added review ID: " + parsedReview.getReviewId() + " for user '" + username + "'");
                            } else {
                                Log.w(TAG, "fetchUserReviewsByUsername: parseReviewDocument returned null for doc ID: " + document.getId() + " for user '" + username + "'");
                            }
                        } catch(Exception ex) {
                            Log.e(TAG, "fetchUserReviewsByUsername: EXCEPTION while parsing review document ID: " + document.getId() + " for user '" + username + "'", ex);
                        }
                    }
                    Log.i(TAG, "fetchUserReviewsByUsername: Finished processing (simplified query). Parsed " + reviewList.size() + " reviews for username '" + username + "'.");
                    listener.onFetched(reviewList);
                })
                .addOnFailureListener(e -> {
                    // Note: If the FAILED_PRECONDITION error appears even with orderBy commented out,
                    // it might be that a whereEqualTo on a collectionGroup query *still* sometimes suggests an index
                    // on just that field for optimal performance, or there's another issue.
                    // However, typically, the orderBy is the main driver for composite indexes.
                    Log.e(TAG, "fetchUserReviewsByUsername: FAILURE (simplified query) for username '" + username + "'. Error: " + e.getMessage(), e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Updates an existing review in Firestore.
     * Only the rating and comment fields are updated.
     * @param review The {@link Review} object with updated data. Must contain valid bookId and reviewId.
     * @param onSuccess Runnable to execute on successful update.
     * @param onError Consumer to accept an error message on update failure.
     */
    public void updateReview(Review review, Runnable onSuccess, Consumer<String> onError) {
        if (review.getBookId() == null || review.getReviewId() == null) {
            Log.e(TAG, "Cannot update review, bookId or reviewId is null.");
            onError.accept("Review identifiers missing.");
            return;
        }
        db.collection(REVIEWS_COLLECTION)
                .document(review.getBookId())
                .collection(USER_REVIEWS_SUBCOLLECTION)
                .document(review.getReviewId())
                .update("rating", review.getRating(), "comment", review.getComment())
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    /**
     * Deletes a review from Firestore.
     * @param review The {@link Review} object to delete. Must contain valid bookId and reviewId.
     * @param onSuccess Runnable to execute on successful deletion.
     * @param onError Consumer to accept an error message on deletion failure.
     */
    public void deleteReview(Review review, Runnable onSuccess, Consumer<String> onError) {
        if (review.getBookId() == null || review.getReviewId() == null) {
            Log.e(TAG, "Cannot delete review, bookId or reviewId is null.");
            onError.accept("Review identifiers missing.");
            return;
        }
        db.collection(REVIEWS_COLLECTION)
                .document(review.getBookId())
                .collection(USER_REVIEWS_SUBCOLLECTION)
                .document(review.getReviewId())
                .delete()
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    /**
     * Parses a Firestore document snapshot into a {@link Review} object.
     * Extracts review data and attempts to determine the book ID from the document path.
     * @param document The Firestore {@link QueryDocumentSnapshot} to parse.
     * @return A {@link Review} object if parsing is successful, otherwise null.
     */
    private Review parseReviewDocument(QueryDocumentSnapshot document) {
        if (document == null || !document.exists()) {
            Log.w(TAG, "parseReviewDocument: Input document was null or does not exist.");
            return null;
        }
        Log.d(TAG, "parseReviewDocument: Attempting to parse document ID: " + document.getId() + " with data: " + document.getData());

        String username = document.getString("username");
        String comment = document.getString("comment");
        String reviewId = document.getId();
        String bookId = null;

        // Extract bookId from the document path: /Reviews/{bookId}/UserReviews/{reviewId}
        if (document.getReference() != null &&
                document.getReference().getParent() != null &&
                document.getReference().getParent().getParent() != null) {
            bookId = document.getReference().getParent().getParent().getId();
        } else {
            Log.w(TAG, "parseReviewDocument: Could not determine bookId for review: " + reviewId + " from path: " + (document.getReference() != null ? document.getReference().getPath() : "null reference"));
        }

        String thumbnailUrl = document.getString("thumbnailUrl");
        String authorUid = document.getString("authorUid");

        float ratingValue = 0f;
        Object ratingObject = document.get("rating");
        if (ratingObject instanceof Number) {
            ratingValue = ((Number) ratingObject).floatValue();
        } else {
            Log.w(TAG, "parseReviewDocument: Rating not found or not a number for review: " + reviewId + ". Field 'rating' was: [" + ratingObject + "]. Defaulting to 0f.");
        }

        // Basic validation
        if (username == null || username.trim().isEmpty()) {
            Log.e(TAG, "parseReviewDocument: Review " + reviewId + " has null or empty username. Skipping parse. Username was: [" + username + "]");
            return null; // Username is essential
        }
        if (comment == null) {
            Log.w(TAG, "parseReviewDocument: Review " + reviewId + " has null comment. Proceeding with empty comment.");
            comment = ""; // Allow null comment, treat as empty
        }
        if (authorUid == null || authorUid.trim().isEmpty()) {
            Log.w(TAG, "parseReviewDocument: Review " + reviewId + " has null or empty authorUid. AuthorUID was: [" + authorUid + "]");
            // Not strictly required for parsing, but good to log
        }
        if (bookId == null || bookId.trim().isEmpty()) {
            Log.w(TAG, "parseReviewDocument: Review " + reviewId + " has null or empty bookId after attempting to parse path. BookID was: [" + bookId + "]");
            // BookId is important, but might be derivable elsewhere if needed. Log for now.
        }
        if (thumbnailUrl == null) {
            Log.w(TAG, "parseReviewDocument: Review " + reviewId + " has null thumbnailUrl. Proceeding with null/empty thumbnail.");
            thumbnailUrl = ""; // Allow null thumbnail URL, treat as empty
        }


        Log.i(TAG, "parseReviewDocument: Successfully parsed review: ID=" + reviewId + ", User=" + username + ", BookID=" + bookId + ", Rating=" + ratingValue + ", AuthorUID=" + authorUid);
        return new Review(username, ratingValue, comment, reviewId, bookId, thumbnailUrl, authorUid);
    }

    // --- Saved Books Management ---

    /**
     * Listener for fetching a list of saved books.
     */
    public interface OnSavedBooksFetchedListener {
        /**
         * Called when saved books are successfully fetched.
         * @param books A list of saved {@link Book} objects.
         */
        void onFetched(List<Book> books);
        /**
         * Called when an error occurs during fetching.
         * @param error A descriptive error message.
         */
        void onError(String error);
    }

    /**
     * Listener for book save/remove operations.
     */
    public interface OnBookSaveOpListener {
        /**
         * Called on successful save or remove operation.
         * @param isSaved True if the book is now saved, false if removed.
         */
        void onSuccess(boolean isSaved);
        /**
         * Called when an error occurs during the operation.
         * @param error A descriptive error message.
         */
        void onError(String error);
    }

    /**
     * Fetches the list of books saved by a specific user.
     * @param uid The unique ID of the user.
     * @param listener The callback listener for the fetch operation.
     */
    public void fetchSavedBooks(String uid, OnSavedBooksFetchedListener listener) {
        db.collection(USERS_COLLECTION)
                .document(uid)
                .collection(SAVED_BOOKS_SUBCOLLECTION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Book> bookList = new ArrayList<>();
                    for(QueryDocumentSnapshot document : queryDocumentSnapshots){
                        Book book = document.toObject(Book.class);
                        if(book != null) {
                            bookList.add(book);
                        }
                    }
                    listener.onFetched(bookList);
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    /**
     * Saves a book to a user's "SavedBooks" subcollection.
     * Uses the book's title as the document ID.
     * @param uid The unique ID of the user.
     * @param book The {@link Book} to save.
     * @param listener The callback listener for the save operation.
     */
    public void saveBook(String uid, Book book, OnBookSaveOpListener listener) {
        db.collection(USERS_COLLECTION).document(uid)
                .collection(SAVED_BOOKS_SUBCOLLECTION).document(book.getTitle()) // Using book title as document ID
                .set(book)
                .addOnSuccessListener(aVoid -> listener.onSuccess(true))
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    /**
     * Removes a book from a user's "SavedBooks" subcollection.
     * Uses the book's title as the document ID.
     * @param uid The unique ID of the user.
     * @param book The {@link Book} to remove.
     * @param listener The callback listener for the remove operation.
     */
    public void removeSavedBook(String uid, Book book, OnBookSaveOpListener listener) {
        db.collection(USERS_COLLECTION).document(uid)
                .collection(SAVED_BOOKS_SUBCOLLECTION).document(book.getTitle()) // Using book title as document ID
                .delete()
                .addOnSuccessListener(aVoid -> listener.onSuccess(false))
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    /**
     * Checks if a specific book is saved by a user.
     * @param uid The unique ID of the user.
     * @param book The {@link Book} to check.
     * @param listenerCallback Consumer to accept a boolean indicating if the book is saved.
     */
    public void isBookSaved(String uid, Book book, Consumer<Boolean> listenerCallback) {
        db.collection(USERS_COLLECTION).document(uid)
                .collection(SAVED_BOOKS_SUBCOLLECTION).document(book.getTitle()) // Using book title as document ID
                .get()
                .addOnSuccessListener(documentSnapshot -> listenerCallback.accept(documentSnapshot.exists()))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking if book is saved: " + e.getMessage(), e);
                    listenerCallback.accept(false); // Assume not saved on error
                });
    }

    // --- Following Management ---

    /**
     * Listener for fetching a count (e.g., followers or following).
     */
    public interface OnCountFetchedListener {
        /**
         * Called when the count is successfully fetched.
         * @param count The fetched count.
         */
        void onCount(int count);
        /**
         * Called when an error occurs during fetching.
         * @param error A descriptive error message.
         */
        void onError(String error);
    }

    /**
     * Listener for fetching a list of usernames being followed.
     */
    public interface OnFollowedListFetchedListener {
        /**
         * Called when the list of followed usernames is successfully fetched.
         * @param usernames A list of usernames being followed.
         */
        void onFetched(List<String> usernames);
        /**
         * Called when an error occurs during fetching.
         * @param error A descriptive error message.
         */
        void onError(String error);
    }

    /**
     * Fetches the number of users a specific user is following.
     * @param uid The unique ID of the user whose following count is needed.
     * @param listener The callback listener for the fetch operation.
     */
    public void fetchFollowingCount(String uid, OnCountFetchedListener listener) {
        db.collection(USERS_COLLECTION).document(uid)
                .collection(FOLLOW_SUBCOLLECTION)
                .get()
                .addOnSuccessListener(querySnapshot -> listener.onCount(querySnapshot.size()))
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    /**
     * Fetches the number of followers a specific user has.
     * Uses a collection group query on the "Follow" subcollection.
     * @param username The username of the user whose followers count is needed.
     * @param listener The callback listener for the fetch operation.
     */
    public void fetchFollowersCount(String username, OnCountFetchedListener listener) {
        db.collectionGroup(FOLLOW_SUBCOLLECTION)
                .whereEqualTo("followed", username)
                .get()
                .addOnSuccessListener(querySnapshot -> listener.onCount(querySnapshot.size()))
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    /**
     * Initiates the action of one user following another.
     * Creates a document in the follower's "Follow" subcollection.
     * @param myId The unique ID of the user performing the follow action.
     * @param followedUsername The username of the user being followed.
     * @param onSuccess Runnable to execute on successful follow.
     * @param onError Consumer to accept an error message on failure.
     */
    public void followUser(String myId, String followedUsername, Runnable onSuccess, Consumer<String> onError) {
        Map<String,Object> data = new HashMap<>();
        data.put("followed", followedUsername);
        data.put("timestamp", System.currentTimeMillis()); // Optional: Add timestamp
        db.collection(USERS_COLLECTION).document(myId)
                .collection(FOLLOW_SUBCOLLECTION).document(followedUsername) // Using followed username as document ID
                .set(data)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    /**
     * Initiates the action of one user unfollowing another.
     * Deletes the corresponding document in the follower's "Follow" subcollection.
     * @param myId The unique ID of the user performing the unfollow action.
     * @param followedUsername The username of the user being unfollowed.
     * @param onSuccess Runnable to execute on successful unfollow.
     * @param onError Consumer to accept an error message on failure.
     */
    public void unfollowUser(String myId, String followedUsername, Runnable onSuccess, Consumer<String> onError) {
        db.collection(USERS_COLLECTION).document(myId)
                .collection(FOLLOW_SUBCOLLECTION).document(followedUsername) // Using followed username as document ID
                .delete()
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    /**
     * Fetches the list of usernames that a specific user is following.
     * @param uid The unique ID of the user whose following list is needed.
     * @param listener The callback listener for the fetch operation.
     */
    public void fetchFollowingUsernames(String uid, OnFollowedListFetchedListener listener) {
        db.collection(USERS_COLLECTION).document(uid)
                .collection(FOLLOW_SUBCOLLECTION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> usernames = new ArrayList<>();
                    for(QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // The document ID in the 'Follow' subcollection is the username of the followed user
                        usernames.add(document.getId());
                    }
                    listener.onFetched(usernames);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching following list for UID: " + uid, e);
                    listener.onError(e.getMessage());
                });
    }
}
