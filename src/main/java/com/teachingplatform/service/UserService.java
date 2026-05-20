package com.teachingplatform.service;

import com.teachingplatform.dao.UserDao;
import com.teachingplatform.entity.*;
import org.springframework.stereotype.Service;

import java.util.Map;

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
}
