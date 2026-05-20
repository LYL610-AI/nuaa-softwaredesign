package com.teachingplatform.dao;

import com.teachingplatform.entity.Registration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class RegistrationDao {

    private final JdbcTemplate jdbc;

    public RegistrationDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean submit(Registration reg) {
        String sql = "INSERT INTO registration (phone_number, real_name, id_number, gender, degree, school_work, audit_state, entry_time, activity_id, user_id) VALUES (?, ?, ?, ?, ?, ?, '0', NOW(), ?, ?)";
        return jdbc.update(sql,
                reg.getPhoneNumber(), reg.getRealName(), reg.getIdNumber(),
                reg.getGender(), reg.getDegree(), reg.getSchoolWork(),
                reg.getActivityId(), reg.getUserId()) > 0;
    }

    public boolean cancel(int registrationId) {
        String sql = "DELETE FROM registration WHERE registration_id = ? AND audit_state = '0'";
        return jdbc.update(sql, registrationId) > 0;
    }

    public List<Registration> listByActivity(int activityId) {
        String sql = "SELECT r.*, a.title as activity_title FROM registration r LEFT JOIN activity a ON r.activity_id = a.activity_id WHERE r.activity_id = ? ORDER BY r.entry_time DESC";
        return jdbc.query(sql, (rs, rowNum) -> mapRegistration(rs), activityId);
    }

    public boolean review(int registrationId, String auditState) {
        String sql = "UPDATE registration SET audit_state = ? WHERE registration_id = ?";
        return jdbc.update(sql, auditState, registrationId) > 0;
    }

    public List<Registration> myRegistrations(int userId) {
        String sql = "SELECT r.*, a.title as activity_title FROM registration r LEFT JOIN activity a ON r.activity_id = a.activity_id WHERE r.user_id = ? ORDER BY r.entry_time DESC";
        return jdbc.query(sql, (rs, rowNum) -> mapRegistration(rs), userId);
    }

    public List<Registration> listAll(String auditState, int page, int pageSize) {
        StringBuilder sql = new StringBuilder("SELECT r.*, a.title as activity_title FROM registration r LEFT JOIN activity a ON r.activity_id = a.activity_id WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (auditState != null && !auditState.isEmpty()) {
            sql.append(" AND r.audit_state = ?");
            params.add(auditState);
        }
        sql.append(" ORDER BY r.entry_time DESC LIMIT ?, ?");
        params.add((page - 1) * pageSize);
        params.add(pageSize);

        return jdbc.query(sql.toString(), (rs, rowNum) -> mapRegistration(rs), params.toArray());
    }

    private Registration mapRegistration(java.sql.ResultSet rs) throws java.sql.SQLException {
        Registration r = new Registration();
        r.setRegistrationId(rs.getInt("registration_id"));
        r.setPhoneNumber(rs.getString("phone_number"));
        r.setRealName(rs.getString("real_name"));
        r.setIdNumber(rs.getString("id_number"));
        r.setGender(rs.getString("gender"));
        r.setDegree(rs.getString("degree"));
        r.setSchoolWork(rs.getString("school_work"));
        r.setAuditState(rs.getString("audit_state"));
        r.setEntryTime(rs.getString("entry_time"));
        r.setActivityId(rs.getInt("activity_id"));
        r.setUserId(rs.getInt("user_id"));
        try { r.setActivityTitle(rs.getString("activity_title")); } catch (java.sql.SQLException ignored) {}
        return r;
    }
}
