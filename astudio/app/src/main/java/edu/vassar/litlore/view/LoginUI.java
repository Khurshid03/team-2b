package edu.vassar.litlore.view;

/**
 * Interface for the Login UI, defining the contract for the login view.
 * This interface allows for setting a listener to handle login events.
 */
public interface LoginUI {

    interface LoginListener {

        /**
         * Called when the login button is clicked.
         * @param username The username entered by the user.
         */
        void onLogin(String username);

        void onLogin(String email, String password, LoginUI ui);
    }

    void setListener(LoginListener listener);
}
