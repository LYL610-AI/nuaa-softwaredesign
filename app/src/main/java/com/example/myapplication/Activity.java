package com.example.myapplication;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Activity implements Serializable {

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

    @SerializedName(value = "schoolName", alternate = {"school_name"})
    private String schoolName;

    private String address;

    @SerializedName(value = "startDate", alternate = {"start_date"})
    private String startDate;

    @SerializedName(value = "endDate", alternate = {"end_date"})
    private String endDate;

    @SerializedName(value = "activityState", alternate = {"activity_state"})
    private String activityState;

    @SerializedName(value = "auditState", alternate = {"audit_state"})
    private String auditState;

    @SerializedName(value = "auditTime", alternate = {"audit_time"})
    private String auditTime;

    @SerializedName(value = "publishTime", alternate = {"publish_time"})
    private String publishTime;

    private String summary;

    @SerializedName(value = "summaryState", alternate = {"summary_state"})
    private String summaryState;

@SerializedName(value = "summaryTitle", alternate = {"summary_title"})
    private String summaryTitle;

    @SerializedName(value = "summaryContent", alternate = {"summary_content"})
    private String summaryContent;

    @SerializedName(value = "summaryAuditState", alternate = {"summary_audit_state"})
    private String summaryAuditState;

    @SerializedName(value = "summarySubmitTime", alternate = {"summary_submit_time"})
    private String summarySubmitTime;

    @SerializedName(value = "pictureUrl", alternate = {"picture_url"})
    private String pictureUrl;

    public Activity() {}

    public String getActivityId() { return activityId; }
    public void setActivityId(String activityId) { this.activityId = activityId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getRecruitsNumber() { return recruitsNumber; }
    public void setRecruitsNumber(int recruitsNumber) { this.recruitsNumber = recruitsNumber; }

    public String getSchoolAddress() { return schoolAddress; }
    public void setSchoolAddress(String schoolAddress) { this.schoolAddress = schoolAddress; }

    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getActivityState() {
        if (activityState == null) return null;
        switch (activityState) {
            case "0": return "招募中";
            case "1": return "进行中";
            case "2": return "结束";
            default: return activityState;
        }
    }
    public void setActivityState(String activityState) { this.activityState = activityState; }

    public String getAuditState() {
        if (auditState == null) return null;
        switch (auditState) {
            case "0": return "待审核";
            case "1": return "通过";
            case "2": return "未通过";
            default: return auditState;
        }
    }
    public void setAuditState(String auditState) { this.auditState = auditState; }

    public String getAuditTime() { return auditTime; }
    public void setAuditTime(String auditTime) { this.auditTime = auditTime; }

    public String getPublishTime() { return publishTime; }
    public void setPublishTime(String publishTime) { this.publishTime = publishTime; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getSummaryState() {
        if (summaryState == null) return null;
        switch (summaryState) {
            case "0": return "未审核";
            case "1": return "通过";
            case "2": return "未通过";
            default: return summaryState;
        }
    }
    public void setSummaryState(String summaryState) { this.summaryState = summaryState; }

public String getSummaryTitle() { return summaryTitle; }
    public void setSummaryTitle(String summaryTitle) { this.summaryTitle = summaryTitle; }

    public String getSummaryContent() { return summaryContent; }
    public void setSummaryContent(String summaryContent) { this.summaryContent = summaryContent; }

    public String getSummaryAuditState() {
        if (summaryAuditState == null) return null;
        switch (summaryAuditState) {
            case "0": return "待审核";
            case "1": return "通过";
            case "2": return "未通过";
            default: return summaryAuditState;
        }
    }
    public void setSummaryAuditState(String summaryAuditState) { this.summaryAuditState = summaryAuditState; }

    public String getSummarySubmitTime() { return summarySubmitTime; }
    public void setSummarySubmitTime(String summarySubmitTime) { this.summarySubmitTime = summarySubmitTime; }

    public String getPictureUrl() { return pictureUrl; }
    public void setPictureUrl(String pictureUrl) { this.pictureUrl = pictureUrl; }
}