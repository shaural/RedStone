package com.cs407.team15.redstone.model;

import java.util.List;

public class HammerUser {
    private List<String> hammerUsers;
    private String uid;

    public HammerUser(List<String> hammerUsers, String uid) {
        this.hammerUsers = hammerUsers;
        this.uid = uid;
    }

    public HammerUser() {}

    public List<String> getHammerUsers() {
        return hammerUsers;
    }

    public void setHammerUsers(List<String> hammerUsers) {
        this.hammerUsers = hammerUsers;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
