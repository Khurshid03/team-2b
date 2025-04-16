package com.example.astudio.model;

import java.io.Serializable;

public class User implements Serializable {
    private String username;
    private String email;
    // email will be used in the next iteration

    public User(String username) {
        this.username = username;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }
}