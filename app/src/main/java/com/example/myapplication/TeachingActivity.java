package com.example.myapplication;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class TeachingActivity implements Serializable {
    @SerializedName(value = "activityId", alternate = {"activity_id"})
    private String activityId;
    @SerializedName(value = "userId", alternate = {"user_id"})
    private String userId;
    private String title;
    private String content;
    @SerializedName(value = "recruitsNumber", alternate = {"recruits_number"})
    private int recruitsNumber;
    @SerializedName(value = "schoolAddress", alternate = {"school_address"})
    private String schoolAddress;
    @SerializedName(value = "startDate", alternate = {"start_date"})
    private String startDate;
    @SerializedName(value = "endDate", alternate = {"end_date"})
    private String endDate;
    @SerializedName(value = "activityState", alternate = {"activity_state"})
    private String activityState;
    @SerializedName(value = "auditState", alternate = {"audit_state"})
    private String auditState;
    @SerializedName(value = "publishTime", alternate = {"publish_time"})
    private String publishTime;
    private String summary;
    @SerializedName(value = "summaryState", alternate = {"summary_state"})
    private String summaryState;
private String summaryTitle;
    @SerializedName(value = "summaryContent", alternate = {"summary_content"})
    private String summaryContent;
    @SerializedName(value = "summaryAuditState", alternate = {"summary_audit_state"})
    private String summaryAuditState;
    @SerializedName(value = "summarySubmitTime", alternate = {"summary_submit_time"})
    private String summarySubmitTime;

    @SerializedName(value = "pictureUrl", alternate = {"picture_url"})
    private String pictureUrl;

    public String getActivityId() { return activityId; }
    public String getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public int getRecruitsNumber() { return recruitsNumber; }
    public String getSchoolAddress() { return schoolAddress; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getActivityState() {
        if (activityState == null) return null;
        switch (activityState) {
            case "0": return "招募中";
            case "1": return "进行中";
            case "2": return "结束";
            default: return activityState;
        }
    }
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
    public String getSummary() { return summary; }
    public String getSummaryState() {
        if (summaryState == null) return null;
        switch (summaryState) {
            case "0": return "未审核";
            case "1": return "通过";
            case "2": return "未通过";
            default: return summaryState;
        }
    }
public String getSummaryTitle() { return summaryTitle; }
    public String getSummaryContent() { return summaryContent; }
    public String getSummaryAuditState() {
        if (summaryAuditState == null) return null;
        switch (summaryAuditState) {
            case "0": return "待审核";
            case "1": return "通过";
            case "2": return "未通过";
            default: return summaryAuditState;
        }
    }
    public String getSummarySubmitTime() { return summarySubmitTime; }

    public String getPictureUrl() { return pictureUrl; }
    public void setPictureUrl(String pictureUrl) { this.pictureUrl = pictureUrl; }

    public TeachingActivity() {}

    public TeachingActivity(String title, String schoolAddress, String activityState, String content) {
        this.title = title;
        this.schoolAddress = schoolAddress;
        this.activityState = activityState;
        this.content = content;
    }
}