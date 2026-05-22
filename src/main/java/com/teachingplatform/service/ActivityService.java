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

    public Map<String, Object> list(String keyword, String region, String state, String auditState, int page, int pageSize) {
        List<Activity> list = activityDao.list(keyword, region, state, auditState, page, pageSize);
        int total = activityDao.count(keyword, region, state, auditState);
        Map<String, Object> stats = activityDao.countByState(keyword, region, auditState);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("stats", stats);
        return result;
    }

    public Activity detail(String activityId) {
        return activityDao.detail(activityId);
    }

    public boolean create(Activity act, String userId) {
        act.setUserId(userId);
        return activityDao.create(act);
    }

    public boolean review(String activityId, String auditState) {
        return activityDao.review(activityId, auditState);
    }

    public boolean submitSummary(String activityId, String title, String content, String userId) {
        Activity existing = activityDao.detail(activityId);
        if (existing == null || !existing.getUserId().equals(userId)) {
            return false;
        }
        return activityDao.submitSummary(activityId, title, content);
    }

    public List<Activity> listSummaries(String auditState, int page, int pageSize) {
        return activityDao.listSummaries(auditState, page, pageSize);
    }

    public boolean reviewSummary(String activityId, String auditState) {
        return activityDao.reviewSummary(activityId, auditState);
    }

    public boolean update(Activity act, String userId) {
        Activity existing = activityDao.detail(act.getActivityId());
        if (existing == null || !existing.getUserId().equals(userId)) {
            return false;
        }
        return activityDao.update(act);
    }

    public boolean changeState(String activityId, String activityState, String userId) {
        Activity existing = activityDao.detail(activityId);
        if (existing == null || !existing.getUserId().equals(userId)) {
            return false;
        }
        return activityDao.changeState(activityId, activityState);
    }

    public boolean delete(String activityId, String userId, int permission) {
        Activity existing = activityDao.detail(activityId);
        if (existing == null) return false;
        if (!existing.getUserId().equals(userId) && permission != 3) {
            return false;
        }
        return activityDao.delete(activityId);
    }

    public List<Activity> listReviewed(int page, int pageSize) {
        return activityDao.listReviewed(page, pageSize);
    }

    public List<Activity> listSummariesReviewed(int page, int pageSize) {
        return activityDao.listSummariesReviewed(page, pageSize);
    }

    public List<Activity> myActivities(String userId) {
        return activityDao.myActivities(userId);
    }
}
