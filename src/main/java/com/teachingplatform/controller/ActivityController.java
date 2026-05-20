package com.teachingplatform.controller;

import com.teachingplatform.entity.Activity;
import com.teachingplatform.service.ActivityService;
import com.teachingplatform.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping("/list")
    public Result list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String region,
                       @RequestParam(required = false) String state,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "6") int pageSize) {
        return Result.ok(activityService.list(keyword, region, state, page, pageSize));
    }

    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable int id) {
        Activity act = activityService.detail(id);
        if (act == null) {
            return Result.error(404, "活动不存在");
        }
        return Result.ok(act);
    }

    @GetMapping("/my")
    public Result myActivities(HttpServletRequest req) {
        String userId = (String) req.getAttribute("userId");
        return Result.ok(activityService.myActivities(userId));
    }

    @PostMapping("/create")
    public Result create(@RequestBody Activity act, HttpServletRequest req) {
        String userId = (String) req.getAttribute("userId");
        boolean ok = activityService.create(act, userId);
        if (!ok) {
            return Result.error(500, "发布失败");
        }
        return Result.ok();
    }

    @PutMapping("/review/{id}")
    public Result review(@PathVariable int id, @RequestBody Map<String, String> body) {
        String state = body.get("auditState");
        boolean ok = activityService.review(id, state);
        return ok ? Result.ok() : Result.error(500, "审核失败");
    }

    @PutMapping("/summary/review/{id}")
    public Result reviewSummary(@PathVariable int id, @RequestBody Map<String, String> body) {
        String state = body.get("auditState");
        boolean ok = activityService.review(id, state);
        return ok ? Result.ok() : Result.error(500, "审核失败");
    }

    @PutMapping("/update/{id}")
    public Result update(@PathVariable int id, @RequestBody Activity act, HttpServletRequest req) {
        String userId = (String) req.getAttribute("userId");
        act.setActivityId(id);
        boolean ok = activityService.update(act, userId);
        return ok ? Result.ok() : Result.error(403, "无权修改此活动");
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable int id, HttpServletRequest req) {
        String userId = (String) req.getAttribute("userId");
        int permission = (int) req.getAttribute("permission");
        boolean ok = activityService.delete(id, userId, permission);
        return ok ? Result.ok() : Result.error(403, "无权删除此活动");
    }
}
