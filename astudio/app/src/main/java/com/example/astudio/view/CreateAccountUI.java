package com.example.astudio.view;

public interface CreateAccountUI {

    interface CreateAccountListener {
        void onCreateAccount(String username, String email, String password, CreateAccountUI ui);
        void onProceedToLogin();
    }

    void setListener(CreateAccountListener listener);
}