package com.teachingplatform.dao;

import com.teachingplatform.entity.Activity;
import com.teachingplatform.util.IdGenerator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class ActivityDao {

    private final JdbcTemplate jdbc;

    public ActivityDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Activity> list(String keyword, String region, String state, String auditState, int page, int pageSize) {
        StringBuilder sql = new StringBuilder(
            "SELECT a.*, s.school_name, s.principle, s.user_phone as school_phone FROM activity a " +
            "LEFT JOIN school_user s ON a.user_id = s.user_id WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (auditState != null && !auditState.isEmpty()) {
            sql.append(" AND a.audit_state = ?");
            params.add(auditState);
        } else {
            sql.append(" AND a.audit_state = '1'");
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND a.title LIKE ?");
            params.add("%" + keyword + "%");
        }
        if (region != null && !region.isEmpty()) {
            sql.append(" AND a.school_address LIKE ?");
            params.add("%" + region + "%");
        }
        if (state != null && !state.isEmpty()) {
            sql.append(" AND a.activity_state = ?");
            params.add(state);
        }
        sql.append(" ORDER BY a.publish_time DESC LIMIT ?, ?");
        params.add((page - 1) * pageSize);
        params.add(pageSize);

        return jdbc.query(sql.toString(), (rs, rowNum) -> mapActivity(rs), params.toArray());
    }

    public int count(String keyword, String region, String state, String auditState) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM activity WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (auditState != null && !auditState.isEmpty()) {
            sql.append(" AND audit_state = ?");
            params.add(auditState);
        } else {
            sql.append(" AND audit_state = '1'");
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND title LIKE ?");
            params.add("%" + keyword + "%");
        }
        if (region != null && !region.isEmpty()) {
            sql.append(" AND school_address LIKE ?");
            params.add("%" + region + "%");
        }
        if (state != null && !state.isEmpty()) {
            sql.append(" AND activity_state = ?");
            params.add(state);
        }
        Integer count = jdbc.queryForObject(sql.toString(), Integer.class, params.toArray());
        return count != null ? count : 0;
    }

    public Activity detail(String activityId) {
        String sql = "SELECT a.*, s.school_name, s.principle, s.user_phone as school_phone FROM activity a LEFT JOIN school_user s ON a.user_id = s.user_id WHERE a.activity_id = ?";
        List<Activity> list = jdbc.query(sql, (rs, rowNum) -> mapActivity(rs), activityId);
        return list.isEmpty() ? null : list.get(0);
    }

    public boolean create(Activity act) {
        act.setActivityId(IdGenerator.generate());
        String sql = "INSERT INTO activity (activity_id, title, content, recruits_number, start_date, end_date, activity_state, audit_state, publish_time, user_id, school_address, picture_url) VALUES (?, ?, ?, ?, ?, ?, '0', '0', NOW(), ?, ?, ?)";
        return jdbc.update(sql,
                act.getActivityId(), act.getTitle(), act.getContent(), act.getRecruitsNumber(),
                act.getStartDate(), act.getEndDate(), act.getUserId(), act.getSchoolAddress(), act.getPictureUrl()) > 0;
    }

    public boolean review(String activityId, String auditState) {
        String sql = "UPDATE activity SET audit_state = ?, audit_time = NOW() WHERE activity_id = ?";
        return jdbc.update(sql, auditState, activityId) > 0;
    }

    public boolean update(Activity act) {
        String sql = "UPDATE activity SET title = ?, content = ?, recruits_number = ?, start_date = ?, end_date = ?, activity_state = ?, school_address = ?, picture_url = ? WHERE activity_id = ?";
        return jdbc.update(sql, act.getTitle(), act.getContent(), act.getRecruitsNumber(), act.getStartDate(), act.getEndDate(), act.getActivityState(), act.getSchoolAddress(), act.getPictureUrl(), act.getActivityId()) > 0;
    }

    public boolean changeState(String activityId, String activityState) {
        String sql = "UPDATE activity SET activity_state = ? WHERE activity_id = ?";
        return jdbc.update(sql, activityState, activityId) > 0;
    }

    public boolean submitSummary(String activityId, String title, String content) {
        String sql = "UPDATE activity SET summary_title = ?, summary_content = ?, summary_audit_state = '0', summary_submit_time = NOW() WHERE activity_id = ?";
        return jdbc.update(sql, title, content, activityId) > 0;
    }

    public List<Activity> listSummaries(String auditState, int page, int pageSize) {
        StringBuilder sql = new StringBuilder(
            "SELECT a.*, s.school_name, s.principle, s.user_phone as school_phone FROM activity a " +
            "LEFT JOIN school_user s ON a.user_id = s.user_id " +
            "WHERE a.summary_title IS NOT NULL"
        );
        List<Object> params = new ArrayList<>();
        if (auditState != null && !auditState.isEmpty()) {
            sql.append(" AND a.summary_audit_state = ?");
            params.add(auditState);
        }
        sql.append(" ORDER BY a.summary_submit_time DESC LIMIT ?, ?");
        params.add((page - 1) * pageSize);
        params.add(pageSize);
        return jdbc.query(sql.toString(), (rs, rowNum) -> mapActivitySummary(rs), params.toArray());
    }

    public boolean reviewSummary(String activityId, String auditState) {
        if ("1".equals(auditState)) {
            String sql = "UPDATE activity SET summary_audit_state = '1', activity_state = '2' WHERE activity_id = ?";
            return jdbc.update(sql, activityId) > 0;
        }
        String sql = "UPDATE activity SET summary_audit_state = ? WHERE activity_id = ?";
        return jdbc.update(sql, auditState, activityId) > 0;
    }

    public boolean delete(String activityId) {
        return jdbc.update("DELETE FROM activity WHERE activity_id = ?", activityId) > 0;
    }

    public List<Activity> listReviewed(int page, int pageSize) {
        String sql = "SELECT a.*, s.school_name, s.principle, s.user_phone as school_phone FROM activity a LEFT JOIN school_user s ON a.user_id = s.user_id WHERE a.audit_state IN ('1','2') ORDER BY a.audit_time DESC LIMIT ?, ?";
        return jdbc.query(sql, (rs, rowNum) -> mapActivity(rs), (page - 1) * pageSize, pageSize);
    }

    public List<Activity> listSummariesReviewed(int page, int pageSize) {
        String sql = "SELECT a.*, s.school_name, s.principle, s.user_phone as school_phone FROM activity a LEFT JOIN school_user s ON a.user_id = s.user_id WHERE a.summary_title IS NOT NULL AND a.summary_audit_state IN ('1','2') ORDER BY a.summary_submit_time DESC LIMIT ?, ?";
        return jdbc.query(sql, (rs, rowNum) -> mapActivitySummary(rs), (page - 1) * pageSize, pageSize);
    }

    public Map<String, Object> countByState(String keyword, String region, String auditState) {
        StringBuilder baseSql = new StringBuilder("SELECT COUNT(*) FROM activity WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (auditState != null && !auditState.isEmpty()) {
            baseSql.append(" AND audit_state = ?");
            params.add(auditState);
        } else {
            baseSql.append(" AND audit_state = '1'");
        }
        if (keyword != null && !keyword.isEmpty()) {
            baseSql.append(" AND title LIKE ?");
            params.add("%" + keyword + "%");
        }
        if (region != null && !region.isEmpty()) {
            baseSql.append(" AND school_address LIKE ?");
            params.add("%" + region + "%");
        }

        String baseWhere = baseSql.toString().substring(baseSql.indexOf("WHERE"));
        Map<String, Object> stats = new HashMap<>();

        int total = countWithWhere("SELECT COUNT(*) FROM activity " + baseWhere, params);
        int recruiting = countWithWhere("SELECT COUNT(*) FROM activity " + baseWhere + " AND activity_state = '0'", params);
        int ongoing = countWithWhere("SELECT COUNT(*) FROM activity " + baseWhere + " AND activity_state = '1'", params);
        int ended = countWithWhere("SELECT COUNT(*) FROM activity " + baseWhere + " AND activity_state = '2'", params);

        stats.put("total", total);
        stats.put("recruiting", recruiting);
        stats.put("ongoing", ongoing);
        stats.put("ended", ended);
        return stats;
    }

    private int countWithWhere(String sql, List<Object> params) {
        Integer count = jdbc.queryForObject(sql, Integer.class, params.toArray());
        return count != null ? count : 0;
    }

    public List<Activity> myActivities(String userId) {
        String sql = "SELECT a.*, s.school_name, s.principle, s.user_phone as school_phone FROM activity a LEFT JOIN school_user s ON a.user_id = s.user_id WHERE a.user_id = ? ORDER BY a.publish_time DESC";
        return jdbc.query(sql, (rs, rowNum) -> mapActivity(rs), userId);
    }

    private Activity mapActivity(java.sql.ResultSet rs) throws java.sql.SQLException {
        Activity a = new Activity();
        a.setActivityId(rs.getString("activity_id"));
        a.setTitle(rs.getString("title"));
        a.setContent(rs.getString("content"));
        a.setRecruitsNumber(rs.getInt("recruits_number"));
        try { a.setStartDate(rs.getString("start_date")); } catch (java.sql.SQLException ignored) {}
        try { a.setEndDate(rs.getString("end_date")); } catch (java.sql.SQLException ignored) {}
        a.setActivityState(rs.getString("activity_state"));
        a.setAuditState(rs.getString("audit_state"));
        a.setAuditTime(rs.getString("audit_time"));
        a.setPublishTime(rs.getString("publish_time"));
        a.setUserId(rs.getString("user_id"));
        try { a.setSchoolName(rs.getString("school_name")); } catch (java.sql.SQLException ignored) {}
        try { a.setPrinciple(rs.getString("principle")); } catch (java.sql.SQLException ignored) {}
        try { a.setSchoolPhone(rs.getString("school_phone")); } catch (java.sql.SQLException ignored) {}
        try { a.setSchoolAddress(rs.getString("school_address")); } catch (java.sql.SQLException ignored) {}
        try { a.setSummaryTitle(rs.getString("summary_title")); } catch (java.sql.SQLException ignored) {}
        try { a.setSummaryContent(rs.getString("summary_content")); } catch (java.sql.SQLException ignored) {}
        try { a.setSummaryAuditState(rs.getString("summary_audit_state")); } catch (java.sql.SQLException ignored) {}
        try { a.setSummarySubmitTime(rs.getString("summary_submit_time")); } catch (java.sql.SQLException ignored) {}
        try { a.setPictureUrl(rs.getString("picture_url")); } catch (java.sql.SQLException ignored) {}
        return a;
    }

    private Activity mapActivitySummary(java.sql.ResultSet rs) throws java.sql.SQLException {
        Activity a = mapActivity(rs);
        try { a.setSummaryTitle(rs.getString("summary_title")); } catch (java.sql.SQLException ignored) {}
        try { a.setSummaryContent(rs.getString("summary_content")); } catch (java.sql.SQLException ignored) {}
        try { a.setSummaryAuditState(rs.getString("summary_audit_state")); } catch (java.sql.SQLException ignored) {}
        try { a.setSummarySubmitTime(rs.getString("summary_submit_time")); } catch (java.sql.SQLException ignored) {}
        try { a.setPictureUrl(rs.getString("picture_url")); } catch (java.sql.SQLException ignored) {}
        return a;
    }
}
