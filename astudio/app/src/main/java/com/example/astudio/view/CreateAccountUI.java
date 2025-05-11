package com.example.astudio.view;

/**
 * Interface defining the UI contract for the Create Account screen.
 * It outlines the methods the View should expose and the listener interface for handling UI events.
 */
public interface CreateAccountUI {

    /**
     * Listener interface for handling user interactions on the Create Account screen.
     */
    interface CreateAccountListener {
        /**
         * Called when the "Create Account" button is clicked.
         *
         * @param username The entered username.
         * @param email The entered email address.
         * @param password The entered password.
         * @param ui The {@link CreateAccountUI} instance that triggered the event.
         */
        void onCreateAccount(String username, String email, String password, CreateAccountUI ui);

        /**
         * Called when the "Proceed to Login" button is clicked.
         */
        void onProceedToLogin();
    }

    /**
     * Sets the listener for handling UI events from the Create Account screen.
     *
     * @param listener The {@link CreateAccountListener} to set.
     */
    void setListener(CreateAccountListener listener);
}
