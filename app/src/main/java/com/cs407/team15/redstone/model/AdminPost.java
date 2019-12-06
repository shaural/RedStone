package com.cs407.team15.redstone.model;

import java.io.Serializable;

public class AdminPost implements Serializable {

    private String postid;
    private String postimage;
    private String description;
    private String publisher;
    private String publisherid;
    private String category;
    private String timestamp;

    public AdminPost(String postid, String postimage, String description, String publisher, String category, String timestamp, String publisherid) {
        this.postid = postid;
        this.postimage = postimage;
        this.description = description;
        this.publisher = publisher;
        this.category = category;
        this.timestamp = timestamp;
        this.publisherid = publisherid;
    }

    public AdminPost() {
    }

    public String getPublisherid() {
        return publisherid;
    }

    public void setPublisherid(String publisherid) {
        this.publisherid = publisherid;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public String getPostimage() {
        return postimage;
    }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }


}
