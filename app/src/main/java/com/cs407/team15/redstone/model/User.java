package com.cs407.team15.redstone.model;

public class User {
    public String id;
    public String token;
    public String email;

    public User() {

    }

    public User(String id, String token, String email) {
        this.id = id;
        this.token = token;
        this.email = email;
    }
}
