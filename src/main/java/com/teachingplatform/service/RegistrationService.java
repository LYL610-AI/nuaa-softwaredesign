package com.teachingplatform.service;

import com.teachingplatform.dao.RegistrationDao;
import com.teachingplatform.entity.Registration;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegistrationService {
    private final RegistrationDao registrationDao;

    public RegistrationService(RegistrationDao registrationDao) {
        this.registrationDao = registrationDao;
    }

    public boolean submit(Registration reg, int userId) {
        reg.setUserId(userId);
        return registrationDao.submit(reg);
    }

    public boolean cancel(int registrationId) {
        return registrationDao.cancel(registrationId);
    }

    public List<Registration> listByActivity(int activityId) {
        return registrationDao.listByActivity(activityId);
    }

    public boolean review(int registrationId, String auditState) {
        return registrationDao.review(registrationId, auditState);
    }

    public List<Registration> myRegistrations(int userId) {
        return registrationDao.myRegistrations(userId);
    }

    public List<Registration> listAll(String auditState, int page, int pageSize) {
        return registrationDao.listAll(auditState, page, pageSize);
    }
}
