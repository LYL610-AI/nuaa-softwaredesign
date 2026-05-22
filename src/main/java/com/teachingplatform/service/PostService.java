package com.teachingplatform.service;

import com.teachingplatform.dao.PostDao;
import com.teachingplatform.entity.Post;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {
    private final PostDao postDao;

    public PostService(PostDao postDao) {
        this.postDao = postDao;
    }

    public List<Post> list(String activityId, String auditState, int page, int pageSize) {
        return postDao.list(activityId, auditState, page, pageSize);
    }

    public Post detail(String postId) {
        return postDao.detail(postId);
    }

    public boolean create(Post p, String userId) {
        p.setUserId(userId);
        return postDao.create(p);
    }

    public boolean delete(String postId) {
        return postDao.delete(postId);
    }

    public boolean review(String postId, String auditState) {
        return postDao.review(postId, auditState);
    }

    public boolean update(Post p, String userId) {
        Post existing = postDao.detail(p.getPostId());
        if (existing == null || !existing.getUserId().equals(userId)) {
            return false;
        }
        return postDao.update(p);
    }

    public List<Post> listReviewed(int page, int pageSize) {
        return postDao.listReviewed(page, pageSize);
    }

    public List<Post> myPosts(String userId) {
        return postDao.myPosts(userId);
    }
}
