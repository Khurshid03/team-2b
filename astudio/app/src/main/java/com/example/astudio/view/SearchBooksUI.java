package com.example.astudio.view;

import com.example.astudio.model.Book;
import java.util.List;

/**
 * Interface to define the UI actions for searching books.
 * It includes methods for displaying search results or showing failure messages.
 */
public interface SearchBooksUI {

    /**
     * Called when the search for books is successful.
     * @param books List of books fetched from the API based on the search query.
     */
    void onSearchBooksSuccess(List<Book> books);

    /**
     * Called when the search for books fails.
     * @param errorMessage Error message describing the failure.
     */
    void onSearchBooksFailure(String errorMessage);
}