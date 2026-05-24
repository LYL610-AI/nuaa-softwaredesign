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
                       @RequestParam(required = false) String auditState,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "6") int pageSize) {
        return Result.ok(activityService.list(keyword, region, state, auditState, page, pageSize));
    }

    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable String id) {
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
    public Result review(@PathVariable String id, @RequestBody Map<String, String> body, HttpServletRequest req) {
        int permission = (int) req.getAttribute("permission");
        if (permission != 3) {
            return Result.error(403, "仅管理员可审核活动");
        }
        String state = body.get("auditState");
        boolean ok = activityService.review(id, state);
        return ok ? Result.ok() : Result.error(500, "审核失败");
    }

    @PutMapping("/summary/review/{id}")
    public Result reviewSummary(@PathVariable String id, @RequestBody Map<String, String> body, HttpServletRequest req) {
        int permission = (int) req.getAttribute("permission");
        if (permission != 3) {
            return Result.error(403, "仅管理员可审核活动总结");
        }
        String state = body.get("auditState");
        boolean ok = activityService.reviewSummary(id, state);
        return ok ? Result.ok() : Result.error(500, "审核失败");
    }

    @PostMapping("/summary/{id}")
    public Result submitSummary(@PathVariable String id, @RequestBody Map<String, String> body, HttpServletRequest req) {
        String userId = (String) req.getAttribute("userId");
        String title = body.get("title");
        String content = body.get("content");
        if (title == null || content == null) {
            return Result.error(400, "标题和内容不能为空");
        }
        boolean ok = activityService.submitSummary(id, title, content, userId);
        return ok ? Result.ok() : Result.error(500, "提交失败");
    }

    @GetMapping("/summary/list")
    public Result listSummaries(@RequestParam(required = false) String auditState,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "50") int pageSize) {
        return Result.ok(activityService.listSummaries(auditState, page, pageSize));
    }

    @PutMapping("/update/{id}")
    public Result update(@PathVariable String id, @RequestBody Activity act, HttpServletRequest req) {
        String userId = (String) req.getAttribute("userId");
        act.setActivityId(id);
        boolean ok = activityService.update(act, userId);
        return ok ? Result.ok() : Result.error(403, "无权修改此活动");
    }

    @PutMapping("/state/{id}")
    public Result changeState(@PathVariable String id, @RequestBody Map<String, String> body, HttpServletRequest req) {
        String userId = (String) req.getAttribute("userId");
        String state = body.get("activityState");
        if (state == null || state.isEmpty()) {
            return Result.error(400, "状态不能为空");
        }
        boolean ok = activityService.changeState(id, state, userId);
        return ok ? Result.ok() : Result.error(403, "无权修改此活动状态");
    }

    @GetMapping("/reviewed")
    public Result listReviewed(@RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(activityService.listReviewed(page, pageSize));
    }

    @GetMapping("/summary/reviewed")
    public Result listSummariesReviewed(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(activityService.listSummariesReviewed(page, pageSize));
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable String id, HttpServletRequest req) {
        String userId = (String) req.getAttribute("userId");
        int permission = (int) req.getAttribute("permission");
        boolean ok = activityService.delete(id, userId, permission);
        return ok ? Result.ok() : Result.error(403, "无权删除此活动");
    }
}
