package com.teachingplatform.dao;

import com.teachingplatform.entity.Post;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class PostDao {

    private final JdbcTemplate jdbc;

    public PostDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Post> list(Integer activityId, String auditState, int page, int pageSize) {
        StringBuilder sql = new StringBuilder(
            "SELECT p.*, " +
            "COALESCE(v.user_identity, s.principle, a2.user_id) as author_name, " +
            "act.title as activity_title, " +
            "(SELECT COUNT(*) FROM comment c WHERE c.post_id = p.post_id) as comment_count " +
            "FROM post p " +
            "LEFT JOIN volunteer_user v ON p.user_id = v.user_id " +
            "LEFT JOIN school_user s ON p.user_id = s.user_id " +
            "LEFT JOIN administrator a2 ON p.user_id = a2.user_id " +
            "LEFT JOIN activity act ON p.activity_id = act.activity_id " +
            "WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (activityId != null) {
            sql.append(" AND p.activity_id = ?");
            params.add(activityId);
        }
        if (auditState != null && !auditState.isEmpty()) {
            sql.append(" AND p.audit_state = ?");
            params.add(auditState);
        }
        sql.append(" ORDER BY p.publish_time DESC LIMIT ?, ?");
        params.add((page - 1) * pageSize);
        params.add(pageSize);

        return jdbc.query(sql.toString(), (rs, rowNum) -> mapPost(rs), params.toArray());
    }

    public Post detail(int postId) {
        String sql = "SELECT p.*, COALESCE(v.user_identity, s.principle, a.user_id) as author_name, act.title as activity_title FROM post p " +
                     "LEFT JOIN volunteer_user v ON p.user_id = v.user_id " +
                     "LEFT JOIN school_user s ON p.user_id = s.user_id " +
                     "LEFT JOIN administrator a ON p.user_id = a.user_id " +
                     "LEFT JOIN activity act ON p.activity_id = act.activity_id " +
                     "WHERE p.post_id = ?";
        List<Post> list = jdbc.query(sql, (rs, rowNum) -> mapPost(rs), postId);
        return list.isEmpty() ? null : list.get(0);
    }

    public boolean create(Post p) {
        String sql = "INSERT INTO post (title, content, audit_state, publish_time, activity_id, user_id) VALUES (?, ?, '0', NOW(), ?, ?)";
        return jdbc.update(sql, p.getTitle(), p.getContent(), p.getActivityId(), p.getUserId()) > 0;
    }

    public boolean delete(int postId) {
        jdbc.update("DELETE FROM comment WHERE post_id = ?", postId);
        return jdbc.update("DELETE FROM post WHERE post_id = ?", postId) > 0;
    }

    public boolean review(int postId, String auditState) {
        String sql = "UPDATE post SET audit_state = ?, audit_time = NOW() WHERE post_id = ?";
        return jdbc.update(sql, auditState, postId) > 0;
    }

    public boolean update(Post p) {
        String sql = "UPDATE post SET title = ?, content = ? WHERE post_id = ?";
        return jdbc.update(sql, p.getTitle(), p.getContent(), p.getPostId()) > 0;
    }

    public List<Post> myPosts(int userId) {
        String sql = "SELECT p.*, COALESCE(v.user_identity, s.principle) as author_name, act.title as activity_title, " +
                     "(SELECT COUNT(*) FROM comment c WHERE c.post_id = p.post_id) as comment_count " +
                     "FROM post p " +
                     "LEFT JOIN volunteer_user v ON p.user_id = v.user_id " +
                     "LEFT JOIN school_user s ON p.user_id = s.user_id " +
                     "LEFT JOIN activity act ON p.activity_id = act.activity_id " +
                     "WHERE p.user_id = ? ORDER BY p.publish_time DESC";
        return jdbc.query(sql, (rs, rowNum) -> mapPost(rs), userId);
    }

    private Post mapPost(java.sql.ResultSet rs) throws java.sql.SQLException {
        Post p = new Post();
        p.setPostId(rs.getInt("post_id"));
        p.setTitle(rs.getString("title"));
        p.setContent(rs.getString("content"));
        p.setAuditState(rs.getString("audit_state"));
        p.setAuditTime(rs.getString("audit_time"));
        p.setPublishTime(rs.getString("publish_time"));
        p.setActivityId(rs.getInt("activity_id"));
        p.setUserId(rs.getInt("user_id"));
        try { p.setAuthorName(rs.getString("author_name")); } catch (java.sql.SQLException ignored) {}
        try { p.setActivityTitle(rs.getString("activity_title")); } catch (java.sql.SQLException ignored) {}
        try { p.setCommentCount(rs.getInt("comment_count")); } catch (java.sql.SQLException ignored) {}
        return p;
    }
}
