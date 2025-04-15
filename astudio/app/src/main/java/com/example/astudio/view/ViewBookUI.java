package com.example.astudio.view;

import com.example.astudio.model.Book;

public interface ViewBookUI {
    // Listener interface for ViewBookFragment events.
    interface ViewBookListener {
        //for later
        void onBackButtonClicked();
    }

    void updateBookDetails(Book book);


    void setListener(ViewBookListener listener);
}