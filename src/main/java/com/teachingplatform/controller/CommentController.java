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
    public Result listByPost(@PathVariable int postId) {
        return Result.ok(service.listByPost(postId));
    }

    @PostMapping("/create")
    public Result create(@RequestBody Comment c, HttpServletRequest req) {
        String userId = (String) req.getAttribute("userId");
        boolean ok = service.create(c, Integer.parseInt(userId));
        return ok ? Result.ok() : Result.error(500, "评论失败");
    }

    @PutMapping("/update/{id}")
    public Result update(@PathVariable int id, @RequestBody Comment c, HttpServletRequest req) {
        String userIdStr = (String) req.getAttribute("userId");
        c.setCommentId(id);
        boolean ok = service.update(c, Integer.parseInt(userIdStr));
        return ok ? Result.ok() : Result.error(403, "无权修改此评论");
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable int id) {
        boolean ok = service.delete(id);
        return ok ? Result.ok() : Result.error(500, "删除失败");
    }
}
