package com.teachingplatform.entity;

public class VolunteerUser {
    private String userId;
    private String userPassword;
    private int userPermission;
    private String userIdentity;
    private String userSex;
    private String userEdu;
    private String userPhone;
    private String registerTime;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserPassword() { return userPassword; }
    public void setUserPassword(String userPassword) { this.userPassword = userPassword; }
    public int getUserPermission() { return userPermission; }
    public void setUserPermission(int userPermission) { this.userPermission = userPermission; }
    public String getUserIdentity() { return userIdentity; }
    public void setUserIdentity(String userIdentity) { this.userIdentity = userIdentity; }
    public String getUserSex() { return userSex; }
    public void setUserSex(String userSex) { this.userSex = userSex; }
    public String getUserEdu() { return userEdu; }
    public void setUserEdu(String userEdu) { this.userEdu = userEdu; }
    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
    public String getRegisterTime() { return registerTime; }
    public void setRegisterTime(String registerTime) { this.registerTime = registerTime; }
}
