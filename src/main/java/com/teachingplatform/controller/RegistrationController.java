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
        return ok ? Result.ok() : Result.error(500, "报名失败");
    }

    @DeleteMapping("/cancel/{id}")
    public Result cancel(@PathVariable String id) {
        boolean ok = service.cancel(id);
        return ok ? Result.ok() : Result.error(500, "取消失败");
    }

    @PutMapping("/review/{id}")
    public Result review(@PathVariable String id, @RequestBody Map<String, String> body) {
        String state = body.get("auditState");
        boolean ok = service.review(id, state);
        return ok ? Result.ok() : Result.error(500, "审核失败");
    }
}
