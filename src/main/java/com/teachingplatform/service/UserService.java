package com.teachingplatform.service;

import com.teachingplatform.dao.UserDao;
import com.teachingplatform.entity.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {
    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public boolean phoneExists(String phone) {
        return userDao.existsByPhone(phone);
    }

    public boolean idNumberExists(String idNumber) {
        return userDao.existsByIdNumber(idNumber);
    }

    public boolean licenseExists(String license) {
        return userDao.existsByLicense(license);
    }

    public Map<String, Object> login(String phone, String password, int role) {
        return userDao.login(phone, password, role);
    }

    public boolean register(Map<String, String> data) {
        String phone = data.get("userPhone");
        if (userDao.existsByPhone(phone)) return false;

        int permission = Integer.parseInt(data.get("userPermission"));
        if (permission == 1) {
            String idNumber = data.get("idNumber");
            if (idNumber != null && userDao.existsByIdNumber(idNumber)) return false;
            VolunteerUser vu = new VolunteerUser();
            vu.setUserPassword(data.get("password"));
            vu.setUserName(data.get("userName"));
            vu.setIdNumber(idNumber);
            vu.setUserSex(data.get("userSex"));
            vu.setUserEdu(data.get("userEdu"));
            vu.setUserPhone(data.get("userPhone"));
            return userDao.registerVolunteer(vu);
        } else {
            String license = data.get("license");
            if (license != null && userDao.existsByLicense(license)) return false;
            SchoolUser su = new SchoolUser();
            su.setUserPassword(data.get("password"));
            su.setSchoolName(data.get("schoolName"));
            su.setType(data.get("type"));
            su.setAddress(data.get("address"));
            su.setLicense(license);
            su.setPrinciple(data.get("principle"));
            su.setUserPhone(data.get("userPhone"));
            return userDao.registerSchool(su);
        }
    }

    public Map<String, Object> getUserInfo(String userId, int permission) {
        return userDao.getUserInfo(userId, permission);
    }

    public boolean changePassword(String userId, int permission, String oldPwd, String newPwd) {
        return userDao.updatePassword(userId, permission, oldPwd, newPwd);
    }

    public boolean updateProfile(String userId, int permission, Map<String, String> data) {
        if (permission == 1) {
            VolunteerUser vu = new VolunteerUser();
            vu.setUserId(userId);
            vu.setUserName(data.get("userName"));
            vu.setUserSex(data.get("userSex"));
            vu.setUserEdu(data.get("userEdu"));
            vu.setUserPhone(data.get("userPhone"));
            return userDao.updateVolunteer(vu);
        } else if (permission == 2) {
            SchoolUser su = new SchoolUser();
            su.setUserId(userId);
            su.setSchoolName(data.get("schoolName"));
            su.setType(data.get("type"));
            su.setAddress(data.get("address"));
            su.setLicense(data.get("license"));
            su.setPrinciple(data.get("principle"));
            su.setUserPhone(data.get("userPhone"));
            return userDao.updateSchool(su);
        }
        return false;
    }

    public Map<String, Object> listUsers(int permission, String keyword, int page, int pageSize) {
        List<Map<String, Object>> list = userDao.listUsers(permission, keyword, page, pageSize);
        int total = userDao.countUsers(permission, keyword);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        return result;
    }

    public boolean adminResetPassword(String userId, int permission, String newPassword) {
        return userDao.adminResetPassword(userId, permission, newPassword);
    }

    public boolean deleteUser(String userId, int permission) {
        return userDao.deleteUser(userId, permission);
    }

    public boolean adminUpdateUser(int permission, Map<String, Object> data) {
        if (permission == 1) return userDao.adminUpdateVolunteer(data);
        else if (permission == 2) return userDao.adminUpdateSchool(data);
        return false;
    }

    public boolean recoverPasswordByLicense(String license, String newPassword) {
        return userDao.recoverPasswordByLicense(license, newPassword);
    }

    public boolean recoverPasswordByIdNumber(String idNumber, String newPassword) {
        return userDao.recoverPasswordByIdNumber(idNumber, newPassword);
    }
}
