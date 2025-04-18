package com.example.astudio.view;

import android.os.Bundle;
import com.example.astudio.model.Book;

/**
 * Simple args builder for ViewBookFragment tests.
 */
public class ViewBookFragmentArgsBuilder {
    /**
     * Wraps a Book into the same bundle key that ViewBookFragment looks for.
     */
    public static Bundle withBook(Book book) {
        Bundle args = new Bundle();
        args.putSerializable("book", book);
        return args;
    }
}