package com.example.astudio.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * RetrofitClient is a singleton class that provides a Retrofit instance for making API calls.
 * It uses OkHttpClient with logging interceptor for debugging purposes.
 */
public class RetrofitClient {
    /**
     * The base URL for the API requests.
     */
    private static final String BASE_URL = "https://www.googleapis.com/books/v1/";
    /**
     * The Retrofit instance used for making API calls.
     */
    private static Retrofit retrofit;


    /**
     * Returns the singleton instance of the GoogleBooksApi.
     * This method initializes the Retrofit client with logging enabled for debugging.
     *
     * @return The singleton instance of the GoogleBooksApi.
     */
    public static GoogleBooksApi getInstance() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(logging).build();
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(GoogleBooksApi.class);
    }
}