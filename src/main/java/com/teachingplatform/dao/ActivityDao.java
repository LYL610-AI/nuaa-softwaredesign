package com.teachingplatform.dao;

import com.teachingplatform.entity.Activity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class ActivityDao {

    private final JdbcTemplate jdbc;

    public ActivityDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Activity> list(String keyword, String state, int page, int pageSize) {
        StringBuilder sql = new StringBuilder(
            "SELECT a.*, s.principle as school_name FROM activity a " +
            "LEFT JOIN school_user s ON a.user_id = s.user_id WHERE a.audit_state = '1'"
        );
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND a.title LIKE ?");
            params.add("%" + keyword + "%");
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

    public int count(String keyword, String state) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM activity WHERE audit_state = '1'");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND title LIKE ?");
            params.add("%" + keyword + "%");
        }
        if (state != null && !state.isEmpty()) {
            sql.append(" AND activity_state = ?");
            params.add(state);
        }
        Integer count = jdbc.queryForObject(sql.toString(), Integer.class, params.toArray());
        return count != null ? count : 0;
    }

    public Activity detail(int activityId) {
        String sql = "SELECT a.*, s.principle as school_name FROM activity a LEFT JOIN school_user s ON a.user_id = s.user_id WHERE a.activity_id = ?";
        List<Activity> list = jdbc.query(sql, (rs, rowNum) -> mapActivity(rs), activityId);
        return list.isEmpty() ? null : list.get(0);
    }

    public boolean create(Activity act) {
        String sql = "INSERT INTO activity (title, content, recruits_number, volunteer_duration, activity_state, audit_state, publish_time, user_id) VALUES (?, ?, ?, ?, '0', '0', NOW(), ?)";
        return jdbc.update(sql,
                act.getTitle(), act.getContent(), act.getRecruitsNumber(),
                act.getVolunteerDuration(), act.getUserId()) > 0;
    }

    public boolean review(int activityId, String auditState) {
        String sql = "UPDATE activity SET audit_state = ?, audit_time = NOW() WHERE activity_id = ?";
        return jdbc.update(sql, auditState, activityId) > 0;
    }

    public List<Activity> myActivities(String userId) {
        String sql = "SELECT a.*, s.principle as school_name FROM activity a LEFT JOIN school_user s ON a.user_id = s.user_id WHERE a.user_id = ? ORDER BY a.publish_time DESC";
        return jdbc.query(sql, (rs, rowNum) -> mapActivity(rs), userId);
    }

    private Activity mapActivity(java.sql.ResultSet rs) throws java.sql.SQLException {
        Activity a = new Activity();
        a.setActivityId(rs.getInt("activity_id"));
        a.setTitle(rs.getString("title"));
        a.setContent(rs.getString("content"));
        a.setRecruitsNumber(rs.getInt("recruits_number"));
        a.setVolunteerDuration(rs.getInt("volunteer_duration"));
        a.setActivityState(rs.getString("activity_state"));
        a.setAuditState(rs.getString("audit_state"));
        a.setAuditTime(rs.getString("audit_time"));
        a.setPublishTime(rs.getString("publish_time"));
        a.setUserId(rs.getString("user_id"));
        try { a.setSchoolName(rs.getString("school_name")); } catch (java.sql.SQLException ignored) {}
        return a;
    }
}
