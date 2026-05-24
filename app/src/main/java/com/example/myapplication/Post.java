package com.example.myapplication;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Post implements Serializable {
    @SerializedName(value = "postId", alternate = {"post_id"})
    private String postId;

    @SerializedName(value = "userId", alternate = {"user_id"})
    private String userId;

    @SerializedName(value = "userName", alternate = {"authorName", "user_name"})
    private String userName;
    private String title;
    private String content;

    @SerializedName(value = "auditState", alternate = {"audit_state"})
    private String auditState;

    @SerializedName(value = "publishTime", alternate = {"publish_time"})
    private String publishTime;

    @SerializedName(value = "activityId", alternate = {"activity_id"})
    private String activityId;

    @SerializedName(value = "auditTime", alternate = {"audit_time"})
    private String auditTime;

    @SerializedName(value = "pictureUrl", alternate = {"picture_url"})
    private String pictureUrl;

    public Post() {}

    public String getPostId() { return postId; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getAuditState() {
        if (auditState == null) return null;
        switch (auditState) {
            case "0": return "待审核";
            case "1": return "通过";
            case "2": return "未通过";
            default: return auditState;
        }
    }
    public String getPublishTime() { return publishTime; }
    public String getActivityId() { return activityId; }
    public String getAuditTime() { return auditTime; }

    public String getPictureUrl() { return pictureUrl; }
    public void setPictureUrl(String pictureUrl) { this.pictureUrl = pictureUrl; }
}