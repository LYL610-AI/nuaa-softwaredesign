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

    public List<Comment> listByPost(int postId) {
        return commentDao.listByPost(postId);
    }

    public boolean create(Comment c, int userId) {
        c.setUserId(userId);
        return commentDao.create(c);
    }

    public boolean update(Comment c, int userId) {
        Comment existing = commentDao.findById(c.getCommentId());
        if (existing == null || existing.getUserId() != userId) {
            return false;
        }
        return commentDao.update(c);
    }

    public boolean delete(int commentId) {
        return commentDao.delete(commentId);
    }
}
