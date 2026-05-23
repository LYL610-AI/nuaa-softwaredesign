package com.teachingplatform.dao;

import com.teachingplatform.entity.Comment;
import com.teachingplatform.util.IdGenerator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CommentDao {

    private final JdbcTemplate jdbc;

    public CommentDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Comment findById(String commentId) {
        String sql = "SELECT * FROM comment WHERE comment_id = ?";
        List<Comment> list = jdbc.query(sql, (rs, rowNum) -> mapComment(rs), commentId);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Comment> listByPost(String postId) {
        String sql = "SELECT c.*, COALESCE(v.user_name, s.principle, a.user_id) as author_name FROM comment c " +
                     "LEFT JOIN volunteer_user v ON c.user_id = v.user_id " +
                     "LEFT JOIN school_user s ON c.user_id = s.user_id " +
                     "LEFT JOIN administrator a ON c.user_id = a.user_id " +
                     "WHERE c.post_id = ? ORDER BY c.publish_time ASC";
        return jdbc.query(sql, (rs, rowNum) -> mapComment(rs), postId);
    }

    public boolean create(Comment c) {
        c.setCommentId(IdGenerator.generate());
        String sql = "INSERT INTO comment (comment_id, content, publish_time, post_id, user_id) VALUES (?, ?, NOW(), ?, ?)";
        return jdbc.update(sql, c.getCommentId(), c.getContent(), c.getPostId(), c.getUserId()) > 0;
    }

    public boolean update(Comment c) {
        String sql = "UPDATE comment SET content = ? WHERE comment_id = ?";
        return jdbc.update(sql, c.getContent(), c.getCommentId()) > 0;
    }

    public boolean delete(String commentId) {
        String sql = "DELETE FROM comment WHERE comment_id = ?";
        return jdbc.update(sql, commentId) > 0;
    }

    private Comment mapComment(java.sql.ResultSet rs) throws java.sql.SQLException {
        Comment c = new Comment();
        c.setCommentId(rs.getString("comment_id"));
        c.setContent(rs.getString("content"));
        c.setPublishTime(rs.getString("publish_time"));
        c.setPostId(rs.getString("post_id"));
        c.setUserId(rs.getString("user_id"));
        try { c.setAuthorName(rs.getString("author_name")); } catch (java.sql.SQLException ignored) {}
        return c;
    }
}
