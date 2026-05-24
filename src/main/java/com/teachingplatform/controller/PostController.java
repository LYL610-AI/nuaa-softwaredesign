package com.teachingplatform.controller;

import com.teachingplatform.entity.Post;
import com.teachingplatform.service.PostService;
import com.teachingplatform.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/post")
public class PostController {

    private final PostService service;

    public PostController(PostService service) {
        this.service = service;
    }

    @GetMapping("/my")
    public Result myPosts(HttpServletRequest req) {
        String userId = (String) req.getAttribute("userId");
        return Result.ok(service.myPosts(userId));
    }

    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable String id) {
        Post p = service.detail(id);
        return p != null ? Result.ok(p) : Result.error(404, "帖子不存在");
    }

    @GetMapping("/list")
    public Result list(@RequestParam(required = false) String activityId,
                       @RequestParam(required = false) String auditState,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(service.list(activityId, auditState, page, pageSize));
    }

    @PostMapping("/create")
    public Result create(@RequestBody Post p, HttpServletRequest req) {
        String userId = (String) req.getAttribute("userId");
        boolean ok = service.create(p, userId);
        return ok ? Result.ok() : Result.error(500, "发布失败");
    }

    @GetMapping("/reviewed")
    public Result listReviewed(@RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(service.listReviewed(page, pageSize));
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable String id, HttpServletRequest req) {
        String userId = (String) req.getAttribute("userId");
        int permission = (int) req.getAttribute("permission");
        boolean ok = service.delete(id, userId, permission);
        return ok ? Result.ok() : Result.error(403, "无权删除此帖子");
    }

    @PutMapping("/review/{id}")
    public Result review(@PathVariable String id, @RequestBody Map<String, String> body, HttpServletRequest req) {
        int permission = (int) req.getAttribute("permission");
        if (permission != 3) {
            return Result.error(403, "仅管理员可审核帖子");
        }
        String state = body.get("auditState");
        boolean ok = service.review(id, state);
        return ok ? Result.ok() : Result.error(500, "审核失败");
    }

    @PutMapping("/update/{id}")
    public Result update(@PathVariable String id, @RequestBody Post p, HttpServletRequest req) {
        String userIdStr = (String) req.getAttribute("userId");
        p.setPostId(id);
        boolean ok = service.update(p, userIdStr);
        return ok ? Result.ok() : Result.error(403, "无权修改此帖子");
    }
}
