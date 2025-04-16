package com.example.astudio.model;

/**
 * Manages the current user and ensures that only one instance of the user manager exists.
 * This class follows the Singleton pattern.
 */
public class UserManager {
    /**
     * The single instance of the UserManager class.
     */
    private static UserManager instance;

    /**
     * The current user managed by the UserManager.
     */
    private User currentUser;

    /**
     * Private constructor to prevent instantiation from outside the class.
     */
    private UserManager() { }

    /**
     * Returns the singleton instance of the UserManager class.
     * If the instance does not exist, it is created.
     *
     * @return The singleton instance of the UserManager.
     */
    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    /**
     * Sets the current user for the UserManager.
     *
     * @param user The user to be set as the current user.
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Returns the current user managed by the UserManager.
     *
     * @return The current user.
     */
    public User getCurrentUser() {
        return currentUser;
    }
}