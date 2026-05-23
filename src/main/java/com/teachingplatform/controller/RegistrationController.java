package com.teachingplatform.controller;

import com.teachingplatform.entity.Registration;
import com.teachingplatform.service.RegistrationService;
import com.teachingplatform.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/registration")
public class RegistrationController {

    private final RegistrationService service;

    public RegistrationController(RegistrationService service) {
        this.service = service;
    }

    @GetMapping("/my")
    public Result myRegistrations(HttpServletRequest req) {
        String userId = (String) req.getAttribute("userId");
        return Result.ok(service.myRegistrations(userId));
    }

    @GetMapping("/list/all")
    public Result listAll(@RequestParam(required = false) String auditState,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "50") int pageSize) {
        return Result.ok(service.listAll(auditState, page, pageSize));
    }

    @GetMapping("/list/{activityId}")
    public Result listByActivity(@PathVariable String activityId) {
        return Result.ok(service.listByActivity(activityId));
    }

    @PostMapping("/submit")
    public Result submit(@RequestBody Registration reg, HttpServletRequest req) {
        String userId = (String) req.getAttribute("userId");
        boolean ok = service.submit(reg, userId);
        if (!ok) {
            return Result.error(400, "您已报名过该活动，不能重复报名");
        }
        return Result.ok();
    }

    @GetMapping("/check/{activityId}")
    public Result check(@PathVariable String activityId, HttpServletRequest req) {
        String userId = (String) req.getAttribute("userId");
        return Result.ok(service.hasRegistered(userId, activityId));
    }

    @DeleteMapping("/cancel/{id}")
    public Result cancel(@PathVariable String id) {
        boolean ok = service.cancel(id);
        return ok ? Result.ok() : Result.error(500, "取消失败");
    }

    @PutMapping("/review/{id}")
    public Result review(@PathVariable String id, @RequestBody Map<String, String> body, HttpServletRequest req) {
        int permission = (int) req.getAttribute("permission");
        if (permission != 2) {
            return Result.error(403, "仅学校用户可审核报名");
        }
        String state = body.get("auditState");
        boolean ok = service.review(id, state);
        return ok ? Result.ok() : Result.error(500, "审核失败");
    }
}
