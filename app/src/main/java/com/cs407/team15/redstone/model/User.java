package com.cs407.team15.redstone.model;

public class User {
    public String uid;
    public String token;
    public String email;
    public String username;
    public int login_attempt;
    public String status;
    public int userLikes;
    public int userDislikes;
    public int recievedDislikes;
    public int recievedLikes;
    public int userType;
    public boolean isHammerUser;

    public User() {

    }

    public User(String id, String token, String email, String username) {
        this.uid = id;
        this.token = token;
        this.email = email;
        this.username = username;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getLogin_attempt() {
        return login_attempt;
    }

    public void setLogin_attempt(int login_attempt) {
        this.login_attempt = login_attempt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getUserLikes() {
        return userLikes;
    }

    public void setUserLikes(int userLikes) {
        this.userLikes = userLikes;
    }

    public int getUserDislikes() {
        return userDislikes;
    }

    public void setUserDislikes(int userDislikes) {
        this.userDislikes = userDislikes;
    }

    public int getRecievedDislikes() {
        return recievedDislikes;
    }

    public void setRecievedDislikes(int recievedDislikes) {
        this.recievedDislikes = recievedDislikes;
    }

    public int getRecievedLikes() {
        return recievedLikes;
    }

    public void setRecievedLikes(int recievedLikes) {
        this.recievedLikes = recievedLikes;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }
}
