package com.teachingplatform.service;

import com.teachingplatform.dao.ActivityDao;
import com.teachingplatform.dao.RegistrationDao;
import com.teachingplatform.dao.UserDao;
import com.teachingplatform.entity.Activity;
import com.teachingplatform.entity.Registration;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RegistrationService {
    private final RegistrationDao registrationDao;
    private final UserDao userDao;
    private final ActivityDao activityDao;

    public RegistrationService(RegistrationDao registrationDao, UserDao userDao, ActivityDao activityDao) {
        this.registrationDao = registrationDao;
        this.userDao = userDao;
        this.activityDao = activityDao;
    }

    public int countByActivity(String activityId) {
        return registrationDao.countByActivity(activityId);
    }

    public boolean submit(Registration reg, String userId) {
        if (registrationDao.existsByUserAndActivity(userId, reg.getActivityId())) {
            return false;
        }
        Map<String, Object> profile = userDao.getUserInfo(userId, 1);
        if (profile != null) {
            if (reg.getRealName() == null || reg.getRealName().isEmpty()) {
                reg.setRealName((String) profile.getOrDefault("userName", ""));
            }
            if (reg.getPhoneNumber() == null || reg.getPhoneNumber().isEmpty()) {
                reg.setPhoneNumber((String) profile.getOrDefault("userPhone", ""));
            }
            if (reg.getIdNumber() == null || reg.getIdNumber().isEmpty()) {
                reg.setIdNumber((String) profile.getOrDefault("idNumber", ""));
            }
            if (reg.getGender() == null || reg.getGender().isEmpty()) {
                reg.setGender((String) profile.getOrDefault("userSex", ""));
            }
            if (reg.getDegree() == null || reg.getDegree().isEmpty()) {
                reg.setDegree((String) profile.getOrDefault("userEdu", ""));
            }
        }
        reg.setUserId(userId);
        return registrationDao.submit(reg);
    }

    public boolean hasRegistered(String userId, String activityId) {
        return registrationDao.existsByUserAndActivity(userId, activityId);
    }

    public boolean cancel(String registrationId, String userId) {
        Registration reg = registrationDao.findById(registrationId);
        if (reg == null || !reg.getUserId().equals(userId)) {
            return false;
        }
        return registrationDao.cancel(registrationId);
    }

    public List<Registration> listByActivity(String activityId) {
        List<Registration> list = registrationDao.listByActivity(activityId);
        for (Registration r : list) {
            r.setIdNumber(maskIdNumber(r.getIdNumber()));
        }
        return list;
    }

    public boolean review(String registrationId, String auditState, String schoolUserId) {
        Registration reg = registrationDao.findById(registrationId);
        if (reg == null) return false;
        Activity activity = activityDao.detail(reg.getActivityId());
        if (activity == null || !activity.getUserId().equals(schoolUserId)) {
            return false;
        }
        return registrationDao.review(registrationId, auditState);
    }

    public List<Registration> myRegistrations(String userId) {
        List<Registration> list = registrationDao.myRegistrations(userId);
        for (Registration r : list) {
            r.setIdNumber(maskIdNumber(r.getIdNumber()));
        }
        return list;
    }

    private String maskIdNumber(String idNumber) {
        if (idNumber == null || idNumber.length() <= 4) return idNumber;
        int visible = Math.max(1, idNumber.length() / 3);
        return idNumber.substring(0, visible) + "*".repeat(idNumber.length() - visible * 2) + idNumber.substring(idNumber.length() - visible);
    }

    public List<Registration> listAll(String auditState, int page, int pageSize) {
        return registrationDao.listAll(auditState, page, pageSize);
    }
}
