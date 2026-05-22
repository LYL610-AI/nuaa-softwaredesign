package com.teachingplatform.service;

import com.teachingplatform.dao.CommentDao;
import com.teachingplatform.entity.Comment;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    private final CommentDao commentDao;

    public CommentService(CommentDao commentDao) {
        this.commentDao = commentDao;
    }

    public List<Comment> listByPost(String postId) {
        return commentDao.listByPost(postId);
    }

    public boolean create(Comment c, String userId) {
        c.setUserId(userId);
        return commentDao.create(c);
    }

    public boolean update(Comment c, String userId) {
        Comment existing = commentDao.findById(c.getCommentId());
        if (existing == null || !existing.getUserId().equals(userId)) {
            return false;
        }
        return commentDao.update(c);
    }

    public boolean delete(String commentId, String userId, int permission) {
        Comment existing = commentDao.findById(commentId);
        if (existing == null) return false;
        if (!existing.getUserId().equals(userId) && permission != 3) {
            return false;
        }
        return commentDao.delete(commentId);
    }
}
