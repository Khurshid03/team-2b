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

import androidx.annotation.NonNull;

/**
 * FirestoreFacade provides a centralized API for all Firebase Firestore database operations.
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

    public FirestoreFacade() {
        db = FirebaseFirestore.getInstance();
    }

    // --- User Management ---
    public interface OnUserProfileFetchedListener { void onFetched(User user); void onError(String error); }
    public interface OnUserSearchListener   { void onResults(List<User> users); void onError(String error); }

    public void saveNewUser(String uid, String username, String email,
                            Runnable onSuccess, OnFailureListener onFailure) {
        Map<String,Object> data = new HashMap<>();
        data.put("username", username);
        data.put("email", email);
        data.put("bio", "");
        db.collection(USERS_COLLECTION).document(uid)
                .set(data)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(onFailure);
    }

    public void fetchUserProfile(String uid, OnUserProfileFetchedListener listener) {
        db.collection(USERS_COLLECTION).document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            user.setId(documentSnapshot.getId());
                            listener.onFetched(user);
                        } else {
                            Log.e(TAG, "Failed to parse user data for UID: " + uid);
                            listener.onError("Failed to parse user data.");
                        }
                    } else {
                        Log.w(TAG, "User document not found for UID: " + uid);
                        listener.onFetched(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user profile for UID: " + uid, e);
                    listener.onError(e.getMessage());
                });
    }

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


    public void searchUsers(String query, OnUserSearchListener listener) {
        db.collection(USERS_COLLECTION)
                .orderBy("username")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> results = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        if (user != null) {
                            user.setId(document.getId());
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
    public interface OnReviewsFetchedListener { void onFetched(List<Review> reviews); void onError(String error); }

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
                .document(book.getTitle())
                .collection(USER_REVIEWS_SUBCOLLECTION)
                .add(reviewData)
                .addOnSuccessListener(documentReference -> onSuccess.accept(documentReference.getId()))
                .addOnFailureListener(onFailure);
    }

    public void fetchReviewsForBook(Book book, OnReviewsFetchedListener listener) {
        String bookId = book.getTitle();
        Log.d(TAG, "fetchReviewsForBook: Attempting to fetch reviews for book ID: " + bookId);
        db.collection(REVIEWS_COLLECTION)
                .document(bookId)
                .collection(USER_REVIEWS_SUBCOLLECTION)
                .orderBy("timestamp", Query.Direction.DESCENDING) // Kept orderBy here as it's for a specific book, less likely to be the issue source.
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

        if (username == null || username.trim().isEmpty()) {
            Log.e(TAG, "parseReviewDocument: Review " + reviewId + " has null or empty username. Skipping parse. Username was: [" + username + "]");
            return null;
        }
        if (comment == null) {
            Log.w(TAG, "parseReviewDocument: Review " + reviewId + " has null comment. Proceeding with empty comment.");
            comment = "";
        }
        if (authorUid == null || authorUid.trim().isEmpty()) {
            Log.w(TAG, "parseReviewDocument: Review " + reviewId + " has null or empty authorUid. AuthorUID was: [" + authorUid + "]");
        }
        if (bookId == null || bookId.trim().isEmpty()) {
            Log.w(TAG, "parseReviewDocument: Review " + reviewId + " has null or empty bookId after attempting to parse path. BookID was: [" + bookId + "]");
        }
        if (thumbnailUrl == null) {
            Log.w(TAG, "parseReviewDocument: Review " + reviewId + " has null thumbnailUrl. Proceeding with null/empty thumbnail.");
            thumbnailUrl = "";
        }

        Log.i(TAG, "parseReviewDocument: Successfully parsed review: ID=" + reviewId + ", User=" + username + ", BookID=" + bookId + ", Rating=" + ratingValue + ", AuthorUID=" + authorUid);
        return new Review(username, ratingValue, comment, reviewId, bookId, thumbnailUrl, authorUid);
    }

    public interface OnSavedBooksFetchedListener { void onFetched(List<Book> books); void onError(String error); }
    public interface OnBookSaveOpListener { void onSuccess(boolean isSaved); void onError(String error); }

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

    public void saveBook(String uid, Book book, OnBookSaveOpListener listener) {
        db.collection(USERS_COLLECTION).document(uid)
                .collection(SAVED_BOOKS_SUBCOLLECTION).document(book.getTitle())
                .set(book)
                .addOnSuccessListener(aVoid -> listener.onSuccess(true))
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    public void removeSavedBook(String uid, Book book, OnBookSaveOpListener listener) {
        db.collection(USERS_COLLECTION).document(uid)
                .collection(SAVED_BOOKS_SUBCOLLECTION).document(book.getTitle())
                .delete()
                .addOnSuccessListener(aVoid -> listener.onSuccess(false))
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    public void isBookSaved(String uid, Book book, Consumer<Boolean> listenerCallback) {
        db.collection(USERS_COLLECTION).document(uid)
                .collection(SAVED_BOOKS_SUBCOLLECTION).document(book.getTitle())
                .get()
                .addOnSuccessListener(documentSnapshot -> listenerCallback.accept(documentSnapshot.exists()))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking if book is saved: " + e.getMessage(), e);
                    listenerCallback.accept(false);
                });
    }

    public interface OnCountFetchedListener { void onCount(int count); void onError(String error); }
    public interface OnFollowedListFetchedListener { void onFetched(List<String> usernames); void onError(String error); }

    public void fetchFollowingCount(String uid, OnCountFetchedListener listener) {
        db.collection(USERS_COLLECTION).document(uid)
                .collection(FOLLOW_SUBCOLLECTION)
                .get()
                .addOnSuccessListener(querySnapshot -> listener.onCount(querySnapshot.size()))
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    public void fetchFollowersCount(String username, OnCountFetchedListener listener) {
        db.collectionGroup(FOLLOW_SUBCOLLECTION)
                .whereEqualTo("followed", username)
                .get()
                .addOnSuccessListener(querySnapshot -> listener.onCount(querySnapshot.size()))
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    public void followUser(String myId, String followedUsername, Runnable onSuccess, Consumer<String> onError) {
        Map<String,Object> data = new HashMap<>();
        data.put("followed", followedUsername);
        data.put("timestamp", System.currentTimeMillis());
        db.collection(USERS_COLLECTION).document(myId)
                .collection(FOLLOW_SUBCOLLECTION).document(followedUsername)
                .set(data)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public void unfollowUser(String myId, String followedUsername, Runnable onSuccess, Consumer<String> onError) {
        db.collection(USERS_COLLECTION).document(myId)
                .collection(FOLLOW_SUBCOLLECTION).document(followedUsername)
                .delete()
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public void fetchFollowingUsernames(String uid, OnFollowedListFetchedListener listener) {
        db.collection(USERS_COLLECTION).document(uid)
                .collection(FOLLOW_SUBCOLLECTION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> usernames = new ArrayList<>();
                    for(QueryDocumentSnapshot document : queryDocumentSnapshots) {
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
