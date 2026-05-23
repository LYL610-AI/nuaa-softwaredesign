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

    @GetMapping("/checkPhone")
    public Result checkPhone(@RequestParam String phone) {
        return Result.ok(userService.phoneExists(phone));
    }

    @GetMapping("/checkIdNumber")
    public Result checkIdNumber(@RequestParam String idNumber) {
        return Result.ok(userService.idNumberExists(idNumber));
    }

    @GetMapping("/checkLicense")
    public Result checkLicense(@RequestParam String license) {
        return Result.ok(userService.licenseExists(license));
    }

    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String password = body.get("password");
        int role = Integer.parseInt(body.getOrDefault("role", "1"));
        Map<String, Object> user = userService.login(phone, password, role);
        if (user == null) {
            return Result.error(401, "手机号或密码错误");
        }
        return Result.ok(user);
    }

    @PostMapping("/register")
    public Result register(@RequestBody Map<String, String> body) {
        boolean ok = userService.register(body);
        if (!ok) {
            return Result.error(400, "注册失败，手机号或身份证号/许可证号已注册");
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

    @PutMapping("/reset-password/{userId}")
    public Result resetPassword(@PathVariable String userId,
                                @RequestBody Map<String, String> body,
                                HttpServletRequest req) {
        int adminPermission = (int) req.getAttribute("permission");
        if (adminPermission != 3) {
            return Result.error(403, "无权操作");
        }
        int permission = Integer.parseInt(body.get("permission"));
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.length() < 6) {
            return Result.error(400, "密码至少6位");
        }
        boolean ok = userService.adminResetPassword(userId, permission, newPassword);
        return ok ? Result.ok() : Result.error(500, "重置失败");
    }

    @PutMapping("/admin-update/{userId}")
    public Result adminUpdateUser(@PathVariable String userId,
                                  @RequestBody Map<String, Object> body,
                                  HttpServletRequest req) {
        int adminPermission = (int) req.getAttribute("permission");
        if (adminPermission != 3) {
            return Result.error(403, "无权操作");
        }
        int permission = Integer.parseInt(body.get("permission").toString());
        body.put("userId", userId);
        boolean ok = userService.adminUpdateUser(permission, body);
        return ok ? Result.ok() : Result.error(500, "修改失败");
    }

    @PostMapping("/recover-password")
    public Result recoverPassword(@RequestBody Map<String, String> body) {
        String type = body.get("type"); // "school" or "volunteer"
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.length() < 6) {
            return Result.error(400, "密码至少6位");
        }
        if ("school".equals(type)) {
            String license = body.get("license");
            boolean ok = userService.recoverPasswordByLicense(license, newPassword);
            return ok ? Result.ok() : Result.error(400, "办学许可证号不存在");
        } else if ("volunteer".equals(type)) {
            String idNumber = body.get("idNumber");
            boolean ok = userService.recoverPasswordByIdNumber(idNumber, newPassword);
            return ok ? Result.ok() : Result.error(400, "身份证号不存在");
        }
        return Result.error(400, "参数错误");
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
