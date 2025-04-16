package com.example.astudio.network;

import com.example.astudio.model.BookResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import com.example.astudio.model.BookResponse;

/**
 * Interface for Google Books API.
 * This interface defines the methods to interact with the Google Books API.
 */
public interface GoogleBooksApi {
    @GET("volumes")

    /**
     * Searches for books using the Google Books API.
     *
     * @param query The search query (e.g., book title, author).
     * @param apiKey Your Google Books API key.
     * @param maxResults The maximum number of results to return.
     * @return A Call object for the API request.
     */
    Call<BookResponse> searchBooks(
            @Query("q") String query,
            @Query("key") String apiKey,
            @Query("maxResults") int maxResults
    );
}