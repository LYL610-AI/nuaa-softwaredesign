package com.teachingplatform.controller;

import com.teachingplatform.service.UserService;
import com.teachingplatform.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        String password = body.get("password");
        int role = Integer.parseInt(body.getOrDefault("role", "1"));
        Map<String, Object> user = userService.login(userId, password, role);
        if (user == null) {
            return Result.error(401, "账号或密码错误");
        }
        return Result.ok(user);
    }

    @PostMapping("/register")
    public Result register(@RequestBody Map<String, String> body) {
        boolean ok = userService.register(body);
        if (!ok) {
            return Result.error(400, "注册失败，账号已存在");
        }
        return Result.ok();
    }

    @GetMapping("/info")
    public Result getInfo(HttpServletRequest req) {
        String userId = (String) req.getAttribute("userId");
        int permission = (int) req.getAttribute("permission");
        Map<String, Object> user = userService.getUserInfo(userId, permission);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        return Result.ok(user);
    }

    @PutMapping("/password")
    public Result changePassword(@RequestBody Map<String, String> body, HttpServletRequest req) {
        String userId = (String) req.getAttribute("userId");
        int permission = (int) req.getAttribute("permission");
        boolean ok = userService.changePassword(userId, permission, body.get("oldPwd"), body.get("newPwd"));
        if (!ok) {
            return Result.error(400, "原密码错误");
        }
        return Result.ok();
    }

    @PutMapping("/update")
    public Result updateProfile(@RequestBody Map<String, String> body, HttpServletRequest req) {
        String userId = (String) req.getAttribute("userId");
        int permission = (int) req.getAttribute("permission");
        boolean ok = userService.updateProfile(userId, permission, body);
        return ok ? Result.ok() : Result.error(400, "修改失败");
    }

    @GetMapping("/list")
    public Result listUsers(@RequestParam(defaultValue = "1") int permission,
                            @RequestParam(required = false) String keyword,
                            @RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "10") int pageSize,
                            HttpServletRequest req) {
        int adminPermission = (int) req.getAttribute("permission");
        if (adminPermission != 3) {
            return Result.error(403, "无权操作");
        }
        return Result.ok(userService.listUsers(permission, keyword, page, pageSize));
    }

    @DeleteMapping("/delete/{userId}")
    public Result deleteUser(@PathVariable String userId,
                             @RequestParam int permission,
                             HttpServletRequest req) {
        int adminPermission = (int) req.getAttribute("permission");
        if (adminPermission != 3) {
            return Result.error(403, "无权操作");
        }
        boolean ok = userService.deleteUser(userId, permission);
        return ok ? Result.ok() : Result.error(500, "删除失败");
    }
}
