package com.cs407.team15.redstone.model;

public class Comment {
    private String comment;
    private Long like;
    private String publisher;
    private String commentid;
    private String path;
    private String timestamp;

    public Comment(String comment,Long like, String publisher, String commentid, String path) {
        this.comment = comment;
        this.publisher = publisher;
        this.commentid = commentid;
        this.path = path;
        this.like=like;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Comment() {
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
                ", like='" + like + '\'' +
                '}';}
    public Long getLike() {
        return like;
    }

    public void setLike(Long score) {
        this.like = score;
    }
}
