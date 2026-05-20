package com.teachingplatform.entity;

public class SchoolUser {
    private String userId;
    private String userPassword;
    private int userPermission;
    private String type;
    private String address;
    private String license;
    private String principle;
    private String userPhone;
    private String registerTime;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserPassword() { return userPassword; }
    public void setUserPassword(String userPassword) { this.userPassword = userPassword; }
    public int getUserPermission() { return userPermission; }
    public void setUserPermission(int userPermission) { this.userPermission = userPermission; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getLicense() { return license; }
    public void setLicense(String license) { this.license = license; }
    public String getPrinciple() { return principle; }
    public void setPrinciple(String principle) { this.principle = principle; }
    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
    public String getRegisterTime() { return registerTime; }
    public void setRegisterTime(String registerTime) { this.registerTime = registerTime; }
}
