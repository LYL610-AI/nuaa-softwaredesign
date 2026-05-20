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
        return Result.ok(service.myPosts(Integer.parseInt(userId)));
    }

    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable int id) {
        Post p = service.detail(id);
        return p != null ? Result.ok(p) : Result.error(404, "帖子不存在");
    }

    @GetMapping("/list")
    public Result list(@RequestParam(required = false) Integer activityId,
                       @RequestParam(required = false) String auditState,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(service.list(activityId, auditState, page, pageSize));
    }

    @PostMapping("/create")
    public Result create(@RequestBody Post p, HttpServletRequest req) {
        String userId = (String) req.getAttribute("userId");
        boolean ok = service.create(p, Integer.parseInt(userId));
        return ok ? Result.ok() : Result.error(500, "发布失败");
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable int id) {
        boolean ok = service.delete(id);
        return ok ? Result.ok() : Result.error(500, "删除失败");
    }

    @PutMapping("/review/{id}")
    public Result review(@PathVariable int id, @RequestBody Map<String, String> body) {
        String state = body.get("auditState");
        boolean ok = service.review(id, state);
        return ok ? Result.ok() : Result.error(500, "审核失败");
    }

    @PutMapping("/update/{id}")
    public Result update(@PathVariable int id, @RequestBody Post p, HttpServletRequest req) {
        String userIdStr = (String) req.getAttribute("userId");
        p.setPostId(id);
        boolean ok = service.update(p, Integer.parseInt(userIdStr));
        return ok ? Result.ok() : Result.error(403, "无权修改此帖子");
    }
}
