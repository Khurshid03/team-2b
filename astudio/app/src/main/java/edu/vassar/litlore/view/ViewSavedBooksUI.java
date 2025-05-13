package edu.vassar.litlore.view;

import edu.vassar.litlore.model.Book;
import java.util.List;

public interface ViewSavedBooksUI {
    /** Called by the controller when it has loaded the saved-books list */
    void displaySavedBooks(List<Book> savedBooks);

    /** called on any error */
    void showError(String message);
}