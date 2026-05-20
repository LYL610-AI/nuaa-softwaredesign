package com.teachingplatform.dao;

import com.teachingplatform.entity.*;
import com.teachingplatform.util.JwtUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.security.MessageDigest;
import java.util.*;

@Repository
public class UserDao {

    private final JdbcTemplate jdbc;

    public UserDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> login(String userId, String password, int role) {
        String table;
        if (role == 1) table = "volunteer_user";
        else if (role == 2) table = "school_user";
        else table = "administrator";

        String sql = "SELECT user_id, user_password, user_permission, user_phone, register_time FROM " + table + " WHERE user_id = ?";
        List<Map<String, Object>> rows = jdbc.queryForList(sql, userId);
        if (rows.isEmpty()) return null;

        Map<String, Object> row = rows.get(0);
        String pwd = (String) row.get("user_password");
        if (!pwd.equals(hashPassword(password))) return null;

        Map<String, Object> user = new HashMap<>();
        user.put("userId", row.get("user_id"));
        user.put("userPermission", row.get("user_permission"));
        user.put("userPhone", row.get("user_phone"));
        user.put("registerTime", row.get("register_time") != null ? row.get("register_time").toString() : "");
        user.put("token", JwtUtil.generate(userId, (int) row.get("user_permission")));
        return user;
    }

    public boolean registerVolunteer(VolunteerUser vu) {
        String sql = "INSERT INTO volunteer_user (user_id, user_password, user_permission, user_identity, user_sex, user_edu, user_phone, register_time) VALUES (?, ?, 1, ?, ?, ?, ?, NOW())";
        return jdbc.update(sql,
                vu.getUserId(), hashPassword(vu.getUserPassword()),
                vu.getUserIdentity(), vu.getUserSex(), vu.getUserEdu(), vu.getUserPhone()) > 0;
    }

    public boolean registerSchool(SchoolUser su) {
        String sql = "INSERT INTO school_user (user_id, user_password, user_permission, type, address, license, principle, user_phone, register_time) VALUES (?, ?, 2, ?, ?, ?, ?, ?, NOW())";
        return jdbc.update(sql,
                su.getUserId(), hashPassword(su.getUserPassword()),
                su.getType(), su.getAddress(), su.getLicense(), su.getPrinciple(), su.getUserPhone()) > 0;
    }

    public boolean exists(String userId) {
        String[] tables = {"volunteer_user", "school_user", "administrator"};
        for (String table : tables) {
            Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM " + table + " WHERE user_id = ?", Integer.class, userId);
            if (count != null && count > 0) return true;
        }
        return false;
    }

    public Map<String, Object> getUserInfo(String userId, int permission) {
        String table;
        if (permission == 1) table = "volunteer_user";
        else if (permission == 2) table = "school_user";
        else table = "administrator";

        String sql = "SELECT * FROM " + table + " WHERE user_id = ?";
        List<Map<String, Object>> rows = jdbc.queryForList(sql, userId);
        if (rows.isEmpty()) return null;

        Map<String, Object> row = rows.get(0);
        Map<String, Object> user = new HashMap<>();
        user.put("userId", row.get("user_id"));
        user.put("userPermission", row.get("user_permission"));
        user.put("userPhone", row.get("user_phone"));
        user.put("registerTime", row.get("register_time") != null ? row.get("register_time").toString() : "");
        if (permission == 1) {
            user.put("userIdentity", row.get("user_identity"));
            user.put("userSex", row.get("user_sex"));
            user.put("userEdu", row.get("user_edu"));
        } else if (permission == 2) {
            user.put("type", row.get("type"));
            user.put("address", row.get("address"));
            user.put("license", row.get("license"));
            user.put("principle", row.get("principle"));
        }
        return user;
    }

    public boolean updatePassword(String userId, int permission, String oldPwd, String newPwd) {
        String table;
        if (permission == 1) table = "volunteer_user";
        else if (permission == 2) table = "school_user";
        else table = "administrator";

        String sql = "UPDATE " + table + " SET user_password = ? WHERE user_id = ? AND user_password = ?";
        return jdbc.update(sql, hashPassword(newPwd), userId, hashPassword(oldPwd)) > 0;
    }
}
