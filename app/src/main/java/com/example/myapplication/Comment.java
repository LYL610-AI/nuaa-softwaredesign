package com.example.myapplication;

import java.io.Serializable;

public class Comment implements Serializable {
    private String commentId;
    private String postId;
    private String userId;
    @com.google.gson.annotations.SerializedName(value = "userName", alternate = {"user_name", "authorName"})
    private String userName;
    private String content;
    private String publishTime;

    public Comment() {}

    public String getCommentId() { return commentId; }
    public String getPostId() { return postId; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getContent() { return content; }
    public String getPublishTime() { return publishTime; }
}