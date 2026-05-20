package com.teachingplatform.interceptor;

import com.teachingplatform.util.JwtUtil;
import com.teachingplatform.util.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
        String method = req.getMethod();
        String uri = req.getRequestURI();

        // 公开的 GET 接口
        if ("GET".equals(method) && (
                uri.contains("/api/activity/list") ||
                uri.contains("/api/activity/detail/") ||
                uri.contains("/api/post/list") ||
                uri.contains("/api/post/detail/") ||
                uri.contains("/api/comment/list/"))) {
            return true;
        }

        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write(objectMapper.writeValueAsString(Result.error(401, "请先登录")));
            return false;
        }

        String token = authHeader.substring(7);
        String[] result = JwtUtil.parse(token);
        if (result == null) {
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write(objectMapper.writeValueAsString(Result.error(401, "登录已过期，请重新登录")));
            return false;
        }

        req.setAttribute("userId", result[0]);
        req.setAttribute("permission", Integer.parseInt(result[1]));
        return true;
    }
}
