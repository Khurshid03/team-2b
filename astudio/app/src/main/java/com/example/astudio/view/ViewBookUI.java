package com.example.astudio.view;

import com.example.astudio.model.Book;


/**
 * Interface defining methods for displaying and interacting with book details in the ViewBookFragment.
 * This includes updating the book's details and handling user interactions such as clicking the back button.
 */

public interface ViewBookUI {
    // Listener interface for ViewBookFragment events.
    interface ViewBookListener {
        //for later
        void onBackButtonClicked();
    }

    /**
     * Updates the UI with the details of the specified book, including title, author, description, and other relevant information.
     *
     * @param book The book whose details are to be displayed.
     */
    void updateBookDetails(Book book);

    /**
     * Sets the listener to handle events such as the back button click in the ViewBookFragment.
     *
     * @param listener The listener to be set for handling fragment events.
     */
    void setListener(ViewBookListener listener);
}