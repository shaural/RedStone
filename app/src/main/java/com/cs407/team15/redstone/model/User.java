package com.cs407.team15.redstone.model;

public class User {
    public String uid;
    public String token;
    public String email;
    public String username;
    public int login_attempt;
    public int userLikes;
    public int userDislikes;
    public int recievedDislikes;
    public int recievedLikes;
    public int userType;

    public User() {

    }

    public User(String id, String token, String email, String username) {
        this.uid = id;
        this.token = token;
        this.email = email;
        this.username = username;
    }
}
