package com.cs407.team15.redstone.model;

public class User {
    public String uid;
    public String token;
    public String email;
    public String username;
    public int login_attempt;
    public String status;

    public User() {

    }

    public User(String id, String token, String email, String username) {
        this.uid = id;
        this.token = token;
        this.email = email;
        this.username = username;
    }
}
