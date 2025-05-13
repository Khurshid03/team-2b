package edu.vassar.litlore.view;

import edu.vassar.litlore.model.User;
import java.util.List;

public interface ViewSearchUsersUI {
    /** Display the list of matching users */
    void displaySearchResults(List<User> users);
    /** Called on any error */
    void showSearchError(String message);
}