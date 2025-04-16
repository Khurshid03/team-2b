package com.example.astudio.model;

public class UserManager {
    // Singleton instance.
    private static UserManager instance;
    private User currentUser;

    // Private constructor.
    private UserManager() { }

    // Return the singleton instance.
    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }


    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }
}