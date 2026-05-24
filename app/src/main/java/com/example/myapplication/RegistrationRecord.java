package com.example.myapplication;

public class RegistrationRecord implements java.io.Serializable {
    private String registrationId;
    private String activityId;
    private String activityTitle;
    @com.google.gson.annotations.SerializedName(value = "applyDate", alternate = {"entryTime", "entry_time"})
    private String applyDate;
    private String auditState;

    public RegistrationRecord() {}

    public RegistrationRecord(String activityTitle, String applyDate, String auditState) {
        this.activityTitle = activityTitle;
        this.applyDate = applyDate;
        this.auditState = auditState;
    }

    public String getRegistrationId() { return registrationId; }
    public void setRegistrationId(String registrationId) { this.registrationId = registrationId; }

    public String getActivityId() { return activityId; }
    public void setActivityId(String activityId) { this.activityId = activityId; }

    public String getActivityTitle() { return activityTitle; }
    public void setActivityTitle(String activityTitle) { this.activityTitle = activityTitle; }

    public String getApplyDate() { return applyDate; }
    public void setApplyDate(String applyDate) { this.applyDate = applyDate; }

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
}