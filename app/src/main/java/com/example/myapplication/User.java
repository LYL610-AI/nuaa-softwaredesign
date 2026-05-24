package com.example.myapplication;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class User implements Serializable {
    // 基础共有字段 (由 API 从 volunteer_user / school_user / administrator 表 JOIN 返回)
    @SerializedName(value = "userId", alternate = {"user_id"})
    private String userId;

    @SerializedName(value = "userPassword", alternate = {"user_password"})
    private String userPassword;

    @SerializedName(value = "userPermission", alternate = {"user_permission"})
    private String userPermission;  // 1-志愿者, 2-学校, 3-管理员

    @SerializedName(value = "userPhone", alternate = {"user_phone"})
    private String userPhone;

    @SerializedName(value = "registerTime", alternate = {"register_time"})
    private String registerTime;

    // 扩展字段 (对应 school_user 表)
    @SerializedName(value = "schoolName", alternate = {"school_name"})
    private String schoolName;

    @SerializedName(value = "schoolAddress", alternate = {"school_address"})
    private String schoolAddress;

    private String address;     // /user/info 返回的学校地址

    private String type;           // 学校类型
    private String license;        // 办学许可证
    private String principle;      // 负责人

    // 扩展字段 (对应 volunteer_user 表)
    @SerializedName(value = "userEdu", alternate = {"user_edu"})
    private String userEdu;

    @SerializedName(value = "userIdentity", alternate = {"user_identity", "idNumber", "id_number"})
    private String userIdentity;

    @SerializedName(value = "userSex", alternate = {"user_sex"})
    private String userSex;

    @SerializedName(value = "userName", alternate = {"realName", "real_name", "user_name"})
    private String userName;

    @SerializedName(value = "permission")
    private String permission;  // 兼容登录接口返回的 permission 字段

    private String phone;  // 登录接口返回的 phone 字段

    public User() {}

    public User(String userId, String userPassword, String userPermission, String userPhone) {
        this.userId = userId;
        this.userPassword = userPassword;
        this.userPermission = userPermission;
        this.userPhone = userPhone;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserPassword() { return userPassword; }
    public void setUserPassword(String userPassword) { this.userPassword = userPassword; }

    public String getUserPermission() {
        return (userPermission != null && !userPermission.isEmpty()) ? userPermission : permission;
    }
    public void setUserPermission(String userPermission) { this.userPermission = userPermission; }

    public String getUserPhone() {
        return (userPhone != null && !userPhone.isEmpty()) ? userPhone : phone;
    }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }

    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }

    public String getSchoolAddress() {
        return (schoolAddress != null && !schoolAddress.isEmpty()) ? schoolAddress : address;
    }
    public void setSchoolAddress(String schoolAddress) { this.schoolAddress = schoolAddress; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getLicense() { return license; }
    public void setLicense(String license) { this.license = license; }

    public String getPrinciple() { return principle; }
    public void setPrinciple(String principle) { this.principle = principle; }

    public String getUserEdu() { return userEdu; }
    public void setUserEdu(String userEdu) { this.userEdu = userEdu; }
    public String getUserIdentity() { return userIdentity; }
    public void setUserIdentity(String userIdentity) { this.userIdentity = userIdentity; }
    public String getUserSex() { return userSex; }
    public void setUserSex(String userSex) { this.userSex = userSex; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getRealName() { return userName; }
    public void setRealName(String realName) { this.userName = realName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }

    public boolean isAdmin() { return "3".equals(getUserPermission()); }
    public boolean isSchool() { return "2".equals(getUserPermission()); }
    public boolean isVolunteer() { return "1".equals(getUserPermission()); }
}