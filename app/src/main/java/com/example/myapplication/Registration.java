package com.example.myapplication;

import com.google.gson.annotations.SerializedName;

public class Registration {
    @SerializedName(value = "registrationId", alternate = {"registration_id"})
    private String registrationId;
    @SerializedName(value = "userId", alternate = {"user_id"})
    private String userId;
    @SerializedName(value = "activityId", alternate = {"activity_id"})
    private String activityId;
    @SerializedName(value = "realName", alternate = {"real_name"})
    private String realName;
    @SerializedName(value = "idNumber", alternate = {"id_number"})
    private String idNumber;
    @SerializedName(value = "phoneNumber", alternate = {"phone_number"})
    private String phoneNumber;
    private String gender;
    private String degree;
    private String introduce;
    @SerializedName(value = "auditState", alternate = {"audit_state"})
    private String auditState;
    @SerializedName(value = "entryTime", alternate = {"entry_time"})
    private String entryTime;

    public String getRegistrationId() { return registrationId; }
    public void setRegistrationId(String registrationId) { this.registrationId = registrationId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getActivityId() { return activityId; }
    public void setActivityId(String activityId) { this.activityId = activityId; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }
    public String getIntroduce() { return introduce; }
    public void setIntroduce(String introduce) { this.introduce = introduce; }
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
    public String getEntryTime() { return entryTime; }
    public void setEntryTime(String entryTime) { this.entryTime = entryTime; }
}