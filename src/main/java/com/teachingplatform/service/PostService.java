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

    public List<Post> list(Integer activityId, String auditState, int page, int pageSize) {
        return postDao.list(activityId, auditState, page, pageSize);
    }

    public Post detail(int postId) {
        return postDao.detail(postId);
    }

    public boolean create(Post p, int userId) {
        p.setUserId(userId);
        return postDao.create(p);
    }

    public boolean delete(int postId) {
        return postDao.delete(postId);
    }

    public boolean review(int postId, String auditState) {
        return postDao.review(postId, auditState);
    }

    public List<Post> myPosts(int userId) {
        return postDao.myPosts(userId);
    }
}
