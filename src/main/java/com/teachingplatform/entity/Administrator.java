package com.teachingplatform.entity;

public class Administrator {
    private String userId;
    private String userPassword;
    private int userPermission;
    private String userName;
    private String userPhone;
    private String registerTime;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserPassword() { return userPassword; }
    public void setUserPassword(String userPassword) { this.userPassword = userPassword; }
    public int getUserPermission() { return userPermission; }
    public void setUserPermission(int userPermission) { this.userPermission = userPermission; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
    public String getRegisterTime() { return registerTime; }
    public void setRegisterTime(String registerTime) { this.registerTime = registerTime; }
}
