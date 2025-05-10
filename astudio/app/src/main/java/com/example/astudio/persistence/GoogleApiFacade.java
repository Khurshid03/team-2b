package com.example.astudio.persistence;

import android.util.Log;

import com.example.astudio.model.Book;
import com.example.astudio.model.BookResponse;
import com.example.astudio.network.GoogleBooksApi;
import com.example.astudio.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * GoogleApiFacade provides a centralized API for all Google Books network operations.
 * This decouples network logic from UI layers, allowing ControllerActivity to delegate
 * search and fetch requests to this facade.
 */
public class GoogleApiFacade {
    private static final String TAG = "GoogleApiFacade";
    // TODO: Consider moving API key to BuildConfig or a secure config file
    private static final String API_KEY = "API_KEY";

    private final GoogleBooksApi api;

    public GoogleApiFacade() {
        this.api = RetrofitClient.getInstance();
    }

    /**
     * Searches books using a free-text query.
     * @param query The search query (e.g., subject:Fantasy, author:Rowling, etc.).
     * @param maxResults Max number of results to return.
     * @param onSuccess Callback with list of Book models on success.
     * @param onFailure Callback with error message on failure.
     */
    public void searchBooks(String query,
                            int maxResults,
                            Consumer<List<Book>> onSuccess,
                            Consumer<String> onFailure) {
        api.searchBooks(query, API_KEY, maxResults)
                .enqueue(new Callback<BookResponse>() {
                    @Override
                    public void onResponse(Call<BookResponse> call, Response<BookResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().items != null) {
                            List<Book> books = new ArrayList<>();
                            for (BookResponse.Item item : response.body().items) {
                                if (item.volumeInfo == null) continue;

                                String title = item.volumeInfo.title != null ? item.volumeInfo.title : "No Title";
                                String thumb = "";
                                if (item.volumeInfo.imageLinks != null && item.volumeInfo.imageLinks.thumbnail != null) {
                                    thumb = item.volumeInfo.imageLinks.thumbnail.replace("http://", "https://");
                                }
                                float rating = item.volumeInfo.averageRating != null ? item.volumeInfo.averageRating : 0f;
                                String author = "Unknown Author";
                                if (item.volumeInfo.authors != null && !item.volumeInfo.authors.isEmpty()) {
                                    author = String.join(", ", item.volumeInfo.authors);
                                }
                                String description = item.volumeInfo.description != null
                                        ? item.volumeInfo.description
                                        : "No description available.";

                                books.add(new Book(title, thumb, rating, author, description));
                            }
                            onSuccess.accept(books);
                        } else {
                            String msg = response.message() != null && !response.message().isEmpty()
                                    ? response.message()
                                    : "Unknown error from Google Books API";
                            Log.e(TAG, "searchBooks:onResponse error: " + msg);
                            onFailure.accept(msg);
                        }
                    }

                    @Override
                    public void onFailure(Call<BookResponse> call, Throwable t) {
                        String err = t.getMessage() != null ? t.getMessage() : "Network error";
                        Log.e(TAG, "searchBooks:onFailure", t);
                        onFailure.accept(err);
                    }
                });
    }

    /**
     * Convenience for genre-based searches.
     */
    public void fetchBooksByGenre(String genre,
                                  int maxResults,
                                  Consumer<List<Book>> onSuccess,
                                  Consumer<String> onFailure) {
        // Prefixing with subject: for genre searches
        searchBooks("subject:" + genre, maxResults, onSuccess, onFailure);
    }

    /**
     * Convenience for fetching "top rated" fiction.
     */
    public void fetchTopRatedBooks(int maxResults,
                                   Consumer<List<Book>> onSuccess,
                                   Consumer<String> onFailure) {
        searchBooks("top rated fiction", maxResults, onSuccess, onFailure);
    }
}
