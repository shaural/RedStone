package com.cs407.team15.redstone.model;

public class Notification {
    private String userid;
    private String commentid; // notification id
    private String userEmail;
    private String text;
    private String postid; // location id
    private boolean ispost;

    public Notification(String userid, String userEmail, String text, String postid, String commentid, boolean ispost) {
        this.userid = userid;
        this.userEmail = userEmail;
        this.text = text;
        this.postid = postid;
        this.commentid = commentid;
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

    public String getCommentid() {
        return commentid;
    }

    public void setCommentid(String commentid) {
        this.commentid = commentid;
    }

    public boolean isIspost() {
        return ispost;
    }

    public void setIspost(boolean ispost) {
        this.ispost = ispost;
    }
}
