package com.example.astudio.view;

import com.example.astudio.model.Book;
import java.util.List;

public interface BrowseBooksUI {
    interface BrowseBooksListener {
        void onBookSelected(Book book);
        void onGenreSelected(String genre);
    }

    interface OnHotBookClickListener {
        void onHotBookClick(Book book);
    }

    interface OnGenreBookClickListener {
        void onGenreBookClick(Book book);
    }

    void setListener(BrowseBooksListener listener);
    void updateHotBooks(List<Book> books);
    void updateGenreBooks(List<Book> books);
}