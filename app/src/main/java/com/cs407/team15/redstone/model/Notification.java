package com.cs407.team15.redstone.model;

public class Notification {
    private String userid;
    private String notificationId; // notification id
    private String userEmail;
    private String text;
    private String postid; // location id
    private boolean ispost;

    public Notification(String userid, String userEmail, String text, String postid, String notificationId, boolean ispost) {
        this.userid = userid;
        this.userEmail = userEmail;
        this.text = text;
        this.postid = postid;
        this.notificationId = notificationId;
        this.ispost = ispost;
    }

    public Notification() {
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public boolean isIspost() {
        return ispost;
    }

    public void setIspost(boolean ispost) {
        this.ispost = ispost;
    }
}
