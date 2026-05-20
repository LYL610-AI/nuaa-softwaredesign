package com.teachingplatform.service;

import com.teachingplatform.dao.ActivityDao;
import com.teachingplatform.entity.Activity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ActivityService {
    private final ActivityDao activityDao;

    public ActivityService(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }

    public Map<String, Object> list(String keyword, String region, String state, int page, int pageSize) {
        List<Activity> list = activityDao.list(keyword, state, page, pageSize);
        int total = activityDao.count(keyword, state);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        return result;
    }

    public Activity detail(int activityId) {
        return activityDao.detail(activityId);
    }

    public boolean create(Activity act, String userId) {
        act.setUserId(userId);
        return activityDao.create(act);
    }

    public boolean review(int activityId, String auditState) {
        return activityDao.review(activityId, auditState);
    }

    public boolean update(Activity act, String userId) {
        Activity existing = activityDao.detail(act.getActivityId());
        if (existing == null || !existing.getUserId().equals(userId)) {
            return false;
        }
        return activityDao.update(act);
    }

    public boolean delete(int activityId, String userId, int permission) {
        Activity existing = activityDao.detail(activityId);
        if (existing == null) return false;
        if (!existing.getUserId().equals(userId) && permission != 3) {
            return false;
        }
        return activityDao.delete(activityId);
    }

    public List<Activity> myActivities(String userId) {
        return activityDao.myActivities(userId);
    }
}
