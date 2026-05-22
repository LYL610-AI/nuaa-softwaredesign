package com.teachingplatform.entity;

public class Activity {
    private String activityId;
    private String title;
    private String content;
    private int recruitsNumber;
    private int volunteerDuration;
    private String activityState;
    private String auditState;
    private String auditTime;
    private String publishTime;
    private String userId;
    private String schoolName;
    private String schoolAddress;
    private String summaryTitle;
    private String summaryContent;
    private String summaryAuditState;
    private String summarySubmitTime;

    public String getActivityId() { return activityId; }
    public void setActivityId(String activityId) { this.activityId = activityId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public int getRecruitsNumber() { return recruitsNumber; }
    public void setRecruitsNumber(int recruitsNumber) { this.recruitsNumber = recruitsNumber; }
    public int getVolunteerDuration() { return volunteerDuration; }
    public void setVolunteerDuration(int volunteerDuration) { this.volunteerDuration = volunteerDuration; }
    public String getActivityState() { return activityState; }
    public void setActivityState(String activityState) { this.activityState = activityState; }
    public String getAuditState() { return auditState; }
    public void setAuditState(String auditState) { this.auditState = auditState; }
    public String getAuditTime() { return auditTime; }
    public void setAuditTime(String auditTime) { this.auditTime = auditTime; }
    public String getPublishTime() { return publishTime; }
    public void setPublishTime(String publishTime) { this.publishTime = publishTime; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    public String getSchoolAddress() { return schoolAddress; }
    public void setSchoolAddress(String schoolAddress) { this.schoolAddress = schoolAddress; }
    public String getAddress() { return schoolAddress; }
    public void setAddress(String address) { this.schoolAddress = address; }
    public String getSummaryTitle() { return summaryTitle; }
    public void setSummaryTitle(String summaryTitle) { this.summaryTitle = summaryTitle; }
    public String getSummaryContent() { return summaryContent; }
    public void setSummaryContent(String summaryContent) { this.summaryContent = summaryContent; }
    public String getSummaryAuditState() { return summaryAuditState; }
    public void setSummaryAuditState(String summaryAuditState) { this.summaryAuditState = summaryAuditState; }
    public String getSummarySubmitTime() { return summarySubmitTime; }
    public void setSummarySubmitTime(String summarySubmitTime) { this.summarySubmitTime = summarySubmitTime; }
}
