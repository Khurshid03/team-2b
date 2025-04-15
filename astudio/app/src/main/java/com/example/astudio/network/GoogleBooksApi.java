package com.example.astudio.network;

import com.example.astudio.model.BookResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import com.example.astudio.model.BookResponse;

public interface GoogleBooksApi {
    @GET("volumes")
    Call<BookResponse> searchBooks(
            @Query("q") String query,
            @Query("key") String apiKey,
            @Query("maxResults") int maxResults
    );
}