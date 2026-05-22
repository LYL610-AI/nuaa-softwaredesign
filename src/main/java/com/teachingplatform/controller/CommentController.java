package com.teachingplatform.controller;

import com.teachingplatform.entity.Comment;
import com.teachingplatform.service.CommentService;
import com.teachingplatform.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    private final CommentService service;

    public CommentController(CommentService service) {
        this.service = service;
    }

    @GetMapping("/list/{postId}")
    public Result listByPost(@PathVariable String postId) {
        return Result.ok(service.listByPost(postId));
    }

    @PostMapping("/create")
    public Result create(@RequestBody Comment c, HttpServletRequest req) {
        String userId = (String) req.getAttribute("userId");
        boolean ok = service.create(c, userId);
        return ok ? Result.ok() : Result.error(500, "评论失败");
    }

    @PutMapping("/update/{id}")
    public Result update(@PathVariable String id, @RequestBody Comment c, HttpServletRequest req) {
        String userIdStr = (String) req.getAttribute("userId");
        c.setCommentId(id);
        boolean ok = service.update(c, userIdStr);
        return ok ? Result.ok() : Result.error(403, "无权修改此评论");
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable String id, HttpServletRequest req) {
        String userId = (String) req.getAttribute("userId");
        int permission = (int) req.getAttribute("permission");
        boolean ok = service.delete(id, userId, permission);
        return ok ? Result.ok() : Result.error(403, "无权删除此评论");
    }
}
