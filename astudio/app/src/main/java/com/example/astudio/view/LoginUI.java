package com.example.astudio.view;

public interface LoginUI {
    interface LoginListener {
        void onLogin(String username);
    }

    void setListener(LoginListener listener);
}
