package com.example.astudio.view;

import com.example.astudio.model.User;
import java.util.List;

public interface ViewSearchUsersUI {
    /** Display the list of matching users */
    void displaySearchResults(List<User> users);
    /** Called on any error */
    void showSearchError(String message);
}