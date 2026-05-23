package com.teachingplatform.dao;

import com.teachingplatform.entity.*;
import com.teachingplatform.util.IdGenerator;
import com.teachingplatform.util.JwtUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class UserDao {

    private final JdbcTemplate jdbc;

    public UserDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Map<String, Object> login(String phone, String password, int role) {
        String table;
        if (role == 1) table = "volunteer_user";
        else if (role == 2) table = "school_user";
        else table = "administrator";

        String sql = "SELECT * FROM " + table + " WHERE user_phone = ?";
        List<Map<String, Object>> rows = jdbc.queryForList(sql, phone);
        if (rows.isEmpty()) return null;

        Map<String, Object> row = rows.get(0);
        String pwd = (String) row.get("user_password");
        if (!pwd.equals(password)) return null;

        String userId = (String) row.get("user_id");
        Map<String, Object> user = new HashMap<>();
        user.put("userId", userId);
        user.put("userPermission", row.get("user_permission"));
        user.put("userPhone", row.get("user_phone"));
        user.put("registerTime", row.get("register_time") != null ? row.get("register_time").toString() : "");
        if (role == 1) user.put("userName", row.get("user_name"));
        if (role == 2) user.put("schoolName", row.get("school_name"));
        user.put("token", JwtUtil.generate(userId, (int) row.get("user_permission")));
        return user;
    }

    public boolean registerVolunteer(VolunteerUser vu) {
        vu.setUserId(IdGenerator.generate());
        String sql = "INSERT INTO volunteer_user (user_id, user_password, user_permission, user_name, id_number, user_sex, user_edu, user_phone, register_time) VALUES (?, ?, 1, ?, ?, ?, ?, ?, NOW())";
        return jdbc.update(sql,
                vu.getUserId(), vu.getUserPassword(),
                vu.getUserName(), vu.getIdNumber(), vu.getUserSex(), vu.getUserEdu(), vu.getUserPhone()) > 0;
    }

    public boolean registerSchool(SchoolUser su) {
        su.setUserId(IdGenerator.generate());
        String sql = "INSERT INTO school_user (user_id, user_password, user_permission, school_name, type, address, license, principle, user_phone, register_time) VALUES (?, ?, 2, ?, ?, ?, ?, ?, ?, NOW())";
        return jdbc.update(sql,
                su.getUserId(), su.getUserPassword(), su.getSchoolName(),
                su.getType(), su.getAddress(), su.getLicense(), su.getPrinciple(), su.getUserPhone()) > 0;
    }

    public boolean existsByPhone(String phone) {
        String[] tables = {"volunteer_user", "school_user", "administrator"};
        for (String table : tables) {
            Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM " + table + " WHERE user_phone = ?", Integer.class, phone);
            if (count != null && count > 0) return true;
        }
        return false;
    }

    public boolean existsByIdNumber(String idNumber) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM volunteer_user WHERE id_number = ?", Integer.class, idNumber);
        return count != null && count > 0;
    }

    public boolean existsByLicense(String license) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM school_user WHERE license = ?", Integer.class, license);
        return count != null && count > 0;
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
            user.put("userName", row.get("user_name"));
            user.put("idNumber", row.get("id_number"));
            user.put("userSex", row.get("user_sex"));
            user.put("userEdu", row.get("user_edu"));
        } else if (permission == 2) {
            user.put("schoolName", row.get("school_name"));
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
        return jdbc.update(sql, newPwd, userId, oldPwd) > 0;
    }

    public boolean updateVolunteer(VolunteerUser vu) {
        String sql = "UPDATE volunteer_user SET user_name = ?, user_sex = ?, user_edu = ?, user_phone = ? WHERE user_id = ?";
        return jdbc.update(sql, vu.getUserName(), vu.getUserSex(), vu.getUserEdu(), vu.getUserPhone(), vu.getUserId()) > 0;
    }

    public boolean updateSchool(SchoolUser su) {
        String sql = "UPDATE school_user SET school_name = ?, type = ?, address = ?, license = ?, principle = ?, user_phone = ? WHERE user_id = ?";
        return jdbc.update(sql, su.getSchoolName(), su.getType(), su.getAddress(), su.getLicense(), su.getPrinciple(), su.getUserPhone(), su.getUserId()) > 0;
    }

    public boolean adminUpdateVolunteer(Map<String, Object> data) {
        String pwd = (String) data.get("userPassword");
        if (pwd != null && !pwd.isEmpty()) {
            String sql = "UPDATE volunteer_user SET user_name = ?, user_phone = ?, user_sex = ?, user_edu = ?, user_password = ? WHERE user_id = ?";
            return jdbc.update(sql,
                    data.get("userName"), data.get("userPhone"),
                    data.get("userSex"), data.get("userEdu"),
                    pwd, data.get("userId")) > 0;
        } else {
            String sql = "UPDATE volunteer_user SET user_name = ?, user_phone = ?, user_sex = ?, user_edu = ? WHERE user_id = ?";
            return jdbc.update(sql,
                    data.get("userName"), data.get("userPhone"),
                    data.get("userSex"), data.get("userEdu"),
                    data.get("userId")) > 0;
        }
    }

    public boolean adminUpdateSchool(Map<String, Object> data) {
        String pwd = (String) data.get("userPassword");
        if (pwd != null && !pwd.isEmpty()) {
            String sql = "UPDATE school_user SET school_name = ?, type = ?, address = ?, license = ?, principle = ?, user_phone = ?, user_password = ? WHERE user_id = ?";
            return jdbc.update(sql,
                    data.get("schoolName"), data.get("type"), data.get("address"),
                    data.get("license"), data.get("principle"), data.get("userPhone"),
                    pwd, data.get("userId")) > 0;
        } else {
            String sql = "UPDATE school_user SET school_name = ?, type = ?, address = ?, license = ?, principle = ?, user_phone = ? WHERE user_id = ?";
            return jdbc.update(sql,
                    data.get("schoolName"), data.get("type"), data.get("address"),
                    data.get("license"), data.get("principle"), data.get("userPhone"),
                    data.get("userId")) > 0;
        }
    }

    public boolean recoverPasswordByLicense(String license, String newPassword) {
        return jdbc.update("UPDATE school_user SET user_password = ? WHERE license = ?", newPassword, license) > 0;
    }

    public boolean recoverPasswordByIdNumber(String idNumber, String newPassword) {
        return jdbc.update("UPDATE volunteer_user SET user_password = ? WHERE id_number = ?", newPassword, idNumber) > 0;
    }

    public List<Map<String, Object>> listUsers(int permission, String keyword, int page, int pageSize) {
        String table;
        if (permission == 1) table = "volunteer_user";
        else if (permission == 2) table = "school_user";
        else table = "administrator";

        StringBuilder sql = new StringBuilder("SELECT * FROM " + table + " WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND user_phone LIKE ?");
            params.add("%" + keyword + "%");
        }
        sql.append(" ORDER BY register_time DESC LIMIT ?, ?");
        params.add((page - 1) * pageSize);
        params.add(pageSize);
        return jdbc.queryForList(sql.toString(), params.toArray());
    }

    public int countUsers(int permission, String keyword) {
        String table;
        if (permission == 1) table = "volunteer_user";
        else if (permission == 2) table = "school_user";
        else table = "administrator";

        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM " + table + " WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND user_phone LIKE ?");
            params.add("%" + keyword + "%");
        }
        Integer count = jdbc.queryForObject(sql.toString(), Integer.class, params.toArray());
        return count != null ? count : 0;
    }

    public boolean adminResetPassword(String userId, int permission, String newPassword) {
        String table;
        if (permission == 1) table = "volunteer_user";
        else if (permission == 2) table = "school_user";
        else table = "administrator";
        return jdbc.update("UPDATE " + table + " SET user_password = ? WHERE user_id = ?",
                newPassword, userId) > 0;
    }

    public boolean deleteUser(String userId, int permission) {
        String table;
        if (permission == 1) table = "volunteer_user";
        else if (permission == 2) table = "school_user";
        else table = "administrator";
        return jdbc.update("DELETE FROM " + table + " WHERE user_id = ?", userId) > 0;
    }
}
