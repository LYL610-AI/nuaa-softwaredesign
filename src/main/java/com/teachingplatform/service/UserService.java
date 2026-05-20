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

    public Map<String, Object> login(String userId, String password, int role) {
        return userDao.login(userId, password, role);
    }

    public boolean register(Map<String, String> data) {
        String userId = data.get("userId");
        if (userDao.exists(userId)) return false;

        int permission = Integer.parseInt(data.get("userPermission"));
        if (permission == 1) {
            VolunteerUser vu = new VolunteerUser();
            vu.setUserId(userId);
            vu.setUserPassword(data.get("password"));
            vu.setUserIdentity(data.get("userIdentity"));
            vu.setUserSex(data.get("userSex"));
            vu.setUserEdu(data.get("userEdu"));
            vu.setUserPhone(data.get("userPhone"));
            return userDao.registerVolunteer(vu);
        } else {
            SchoolUser su = new SchoolUser();
            su.setUserId(userId);
            su.setUserPassword(data.get("password"));
            su.setType(data.get("type"));
            su.setAddress(data.get("address"));
            su.setLicense(data.get("license"));
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
            vu.setUserIdentity(data.get("userIdentity"));
            vu.setUserSex(data.get("userSex"));
            vu.setUserEdu(data.get("userEdu"));
            vu.setUserPhone(data.get("userPhone"));
            return userDao.updateVolunteer(vu);
        } else if (permission == 2) {
            SchoolUser su = new SchoolUser();
            su.setUserId(userId);
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

    public boolean deleteUser(String userId, int permission) {
        return userDao.deleteUser(userId, permission);
    }
}
