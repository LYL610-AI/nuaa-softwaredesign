package com.teachingplatform.entity;

public class Registration {
    private int registrationId;
    private String phoneNumber;
    private String realName;
    private String idNumber;
    private String gender;
    private String degree;
    private String schoolWork;
    private String auditState;
    private String entryTime;
    private int activityId;
    private int userId;
    private String activityTitle;

    public int getRegistrationId() { return registrationId; }
    public void setRegistrationId(int registrationId) { this.registrationId = registrationId; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }
    public String getSchoolWork() { return schoolWork; }
    public void setSchoolWork(String schoolWork) { this.schoolWork = schoolWork; }
    public String getAuditState() { return auditState; }
    public void setAuditState(String auditState) { this.auditState = auditState; }
    public String getEntryTime() { return entryTime; }
    public void setEntryTime(String entryTime) { this.entryTime = entryTime; }
    public int getActivityId() { return activityId; }
    public void setActivityId(int activityId) { this.activityId = activityId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getActivityTitle() { return activityTitle; }
    public void setActivityTitle(String activityTitle) { this.activityTitle = activityTitle; }
}
