package com.travel.mcp.controller;

import com.travel.mcp.entity.User;
import com.travel.mcp.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户认证控制器
 * 提供注册、登录、登出功能
 * API Key 仅在 Session 中存储，不持久化到数据库
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ==================== 注册 ====================

    /** 用户注册 — 创建账号并设置 API Key */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> body, HttpSession session) {
        String username = body.getOrDefault("username", "").trim();
        String password = body.getOrDefault("password", "").trim();
        String apiKey = body.getOrDefault("apiKey", "").trim();

        if (username.length() < 2 || username.length() > 50) {
            return Map.of("success", false, "message", "用户名需要 2-50 个字符");
        }
        if (password.length() < 4) {
            return Map.of("success", false, "message", "密码至少需要 4 个字符");
        }
        if (apiKey.isBlank()) {
            return Map.of("success", false, "message", "请输入你的 DeepSeek API Key");
        }
        if (userRepository.existsByUsername(username)) {
            return Map.of("success", false, "message", "用户名已存在，请直接登录或换一个用户名");
        }

        User user = User.builder()
            .username(username)
            .password(passwordEncoder.encode(password))
            .build();
        userRepository.save(user);

        // 注册成功 → 自动登录：API Key 存入 Session
        session.setAttribute("user_id", user.getId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("deepseek_api_key", apiKey);

        log.info("✅ 新用户注册: {} (ID={})", username, user.getId());
        return Map.of("success", true, "message", "注册成功！欢迎使用智慧旅游助手",
            "username", username);
    }

    // ==================== 登录 ====================

    /** 用户登录 — 验证账号并设置 API Key */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body, HttpSession session) {
        String username = body.getOrDefault("username", "").trim();
        String password = body.getOrDefault("password", "").trim();
        String apiKey = body.getOrDefault("apiKey", "").trim();

        if (username.isBlank() || password.isBlank()) {
            return Map.of("success", false, "message", "用户名和密码不能为空");
        }
        if (apiKey.isBlank()) {
            return Map.of("success", false, "message", "请输入你的 DeepSeek API Key 才能使用 AI 功能");
        }

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return Map.of("success", false, "message", "用户名或密码错误");
        }

        // 登录成功 → API Key 存入 Session
        session.setAttribute("user_id", user.getId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("deepseek_api_key", apiKey);

        log.info("🔑 用户登录: {} (ID={})", username, user.getId());
        return Map.of("success", true, "message", "登录成功！",
            "username", username);
    }

    // ==================== 登出 ====================

    /** 用户登出 — 清除 Session 中的 API Key */
    @PostMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        String username = (String) session.getAttribute("username");
        log.info("👋 用户登出: {}", username != null ? username : "未登录");

        session.removeAttribute("deepseek_api_key");
        session.removeAttribute("user_id");
        session.removeAttribute("username");
        session.invalidate();

        return Map.of("success", true, "message", "已安全退出，API Key 已清除");
    }

    // ==================== 状态检查 ====================

    /** 检查当前登录状态 */
    @GetMapping("/status")
    public Map<String, Object> status(HttpSession session) {
        String username = (String) session.getAttribute("username");
        Long userId = (Long) session.getAttribute("user_id");
        String apiKey = (String) session.getAttribute("deepseek_api_key");

        if (userId != null && username != null && apiKey != null && !apiKey.isBlank()) {
            return Map.of("loggedIn", true, "username", username,
                "hasApiKey", true);
        }
        if (userId != null && username != null) {
            return Map.of("loggedIn", true, "username", username,
                "hasApiKey", false);
        }
        return Map.of("loggedIn", false);
    }
}
