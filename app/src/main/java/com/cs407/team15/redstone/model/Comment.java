package com.cs407.team15.redstone.model;

import java.util.ArrayList;

public class Comment {
    private String comment;
    private Long like;
    private String publisher;
    private String publisherid;
    private String commentid;
    private String path;
    private String timestamp;
    String tags;
    private String locationId;

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public long getLike() {
        return like;
    }

    public void setLike(long like) {
        this.like = like;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Comment(String comment,Long like, String publisher, String commentid, String path) {
        this.comment = comment;
        this.publisher = publisher;
        this.commentid = commentid;
        this.path = path;
        this.like=like;
    }
    public Comment(String comment, String publisher, String commentid, String path, Long like) {
        this.comment = comment;
        this.publisher = publisher;
        this.commentid = commentid;
        this.path = path;
        this.like = like;
    }

//    public Long getLike() {
//        return like;
//    }
//
//    public void setLike(Long like) {
//        this.like = like;
//    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Comment() {
    }

    public String getPublisherid() {
        return publisherid;
    }

    public void setPublisherid(String publisherid) {
        this.publisherid = publisherid;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getCommentid() {
        return commentid;
    }

    public void setCommentid(String commentid) {
        this.commentid = commentid;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }



    @Override
    public String toString() {
        return "Comment{" +
                "comment='" + comment + '\'' +
                ", publisher='" + publisher + '\'' +
                ", commentid='" + commentid + '\'' +
                ", path='" + path + '\'' +
                ", like=" + like +
                ", timestamp='" + timestamp + '\'' +
                ", tags='" + tags + '\'' +
                '}';
    }
}
