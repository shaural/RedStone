package com.cs407.team15.redstone.model;

public class User {
    public String uid;
    public String token;
    public String email;
    public String username;

    public User() {

    }

    public User(String id, String token, String email, String username) {
        this.uid = id;
        this.token = token;
        this.email = email;
        this.username = username;
    }
}
