package com.travel.mcp.controller;

import com.travel.mcp.dto.ChatRequest;
import com.travel.mcp.dto.ChatResponse;
import com.travel.mcp.entity.ChatMessage;
import com.travel.mcp.entity.ChatSession;
import com.travel.mcp.repository.ChatMessageRepository;
import com.travel.mcp.repository.ChatSessionRepository;
import com.travel.mcp.repository.ScenicSpotRepository;
import com.travel.mcp.service.DeepSeekService;
import com.travel.mcp.service.QWeatherService;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 聊天控制器
 *
 * 提供 SSE 流式 AI 对话 + 会话管理 + DeepSeek Key 配置
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class ChatController {

    private final DeepSeekService deepSeekService;
    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final QWeatherService qWeatherService;
    private final ScenicSpotRepository scenicSpotRepository;
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
        2, 8, 60L, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(100),
        new ThreadPoolExecutor.CallerRunsPolicy());

    @PreDestroy
    public void shutdown() {
        log.info("正在关闭线程池...");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                log.warn("线程池未能在30秒内终止，已强制关闭");
            } else {
                log.info("线程池已优雅关闭");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            log.warn("线程池关闭被中断");
        }
    }

    public ChatController(DeepSeekService deepSeekService,
                          ChatSessionRepository sessionRepository,
                          ChatMessageRepository messageRepository,
                          QWeatherService qWeatherService,
                          ScenicSpotRepository scenicSpotRepository) {
        this.deepSeekService = deepSeekService;
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.qWeatherService = qWeatherService;
        this.scenicSpotRepository = scenicSpotRepository;
    }

    // ==================== DeepSeek Key 管理 ====================

    /** 设置 DeepSeek API Key（存入 Session） */
    @PostMapping("/config/deepseek-key")
    public Map<String, Object> setApiKey(@RequestBody Map<String, String> body, HttpSession session) {
        String apiKey = body.getOrDefault("apiKey", "").trim();
        if (apiKey.isEmpty()) {
            return Map.of("success", false, "message", "API Key 不能为空");
        }
        session.setAttribute("deepseek_api_key", apiKey);
        log.info("🔑 DeepSeek API Key 已存入 Session: {}", session.getId());
        return Map.of("success", true, "message", "API Key 配置成功");
    }

    /** 检查是否已配置 API Key（有默认 Key 所以始终返回 true） */
    @GetMapping("/config/deepseek-key")
    public Map<String, Object> checkApiKey() {
        return Map.of("configured", true);
    }

    // ==================== AI 对话 ====================

    /** 非流式聊天 */
    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request, HttpSession session) {
        Long userId = getUserId(session);
        if (userId == null) {
            ChatResponse resp = new ChatResponse();
            resp.setNeedApiKey(true);
            resp.setReply("请先登录");
            return resp;
        }
        String apiKey = (String) session.getAttribute("deepseek_api_key");
        if (apiKey == null || apiKey.isBlank()) {
            ChatResponse resp = new ChatResponse();
            resp.setNeedApiKey(true);
            return resp;
        }
        try {
            Long sessionId = getOrCreateSession(userId, request.getSessionId());
            String reply = deepSeekService.chat(userId, sessionId, request.getMessage(), apiKey);

            ChatSession chatSession = sessionRepository.findById(sessionId).orElse(null);
            String title = chatSession != null ? chatSession.getSessionTitle() : null;
            return new ChatResponse(sessionId, title, reply);
        } catch (Exception e) {
            log.error("AI 对话失败", e);
            return new ChatResponse(request.getSessionId(), null,
                "❌ AI 服务调用失败：" + e.getMessage());
        }
    }

    /** SSE 流式聊天 */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody ChatRequest request, HttpSession session) {
        Long userId = getUserId(session);
        String apiKey = (String) session.getAttribute("deepseek_api_key");
        SseEmitter emitter = new SseEmitter(300000L);  // 5分钟超时

        // 未登录/未配置 API Key 时直接返回错误
        if (userId == null || apiKey == null || apiKey.isBlank()) {
            executor.execute(() -> {
                try {
                    emitter.send(SseEmitter.event().name("error")
                        .data("{\"error\":\"请先登录并配置 DeepSeek API Key\",\"needApiKey\":true}"));
                    emitter.complete();
                } catch (Exception ignored) {}
            });
            return emitter;
        }

        Long sessionId = getOrCreateSession(userId, request.getSessionId());

        final String finalApiKey = apiKey;
        final Long finalUserId = userId;
        final Long finalSessionId = sessionId;

        executor.execute(() -> {
            try {
                // 1. 发送会话 ID
                emitter.send(SseEmitter.event().name("session")
                    .data("{\"sessionId\":" + finalSessionId + "}"));

                // 2. 调用 DeepSeek（流式：先检查工具 → 再逐字流式输出）
                deepSeekService.chatStream(finalUserId, finalSessionId, request.getMessage(), finalApiKey, emitter);

            } catch (Exception e) {
                log.error("SSE 流式对话异常", e);
                try {
                    String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                    emitter.send(SseEmitter.event().name("error")
                        .data("{\"error\":\"" + msg.replace("\"", "'") + "\"}"));
                    emitter.complete();
                } catch (Exception ex) {
                    emitter.completeWithError(ex);
                }
            }
        });

        return emitter;
    }

    // ==================== 会话管理（用户隔离） ====================

    /** 获取当前用户的所有会话 */
    @GetMapping("/sessions")
    public List<ChatSession> getSessions(HttpSession session) {
        Long userId = getUserId(session);
        if (userId == null) return List.of();
        return sessionRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    /** 创建新会话 */
    @PostMapping("/sessions")
    public ChatSession createSession(HttpSession session) {
        Long userId = getUserId(session);
        if (userId == null) {
            // 未登录时创建匿名临时会话（不关联用户）
            return sessionRepository.save(ChatSession.builder()
                .userId(0L)
                .sessionTitle("新的对话")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        }
        ChatSession chatSession = ChatSession.builder()
            .userId(userId)
            .sessionTitle("新的对话")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        return sessionRepository.save(chatSession);
    }

    /** 重命名会话 */
    @PutMapping("/sessions/{id}")
    public Map<String, Object> renameSession(@PathVariable Long id, @RequestBody Map<String, String> body,
                                              HttpSession session) {
        Long userId = getUserId(session);
        if (userId == null) return Map.of("success", false, "message", "请先登录");

        ChatSession chatSession = sessionRepository.findById(id).orElse(null);
        if (chatSession == null) {
            return Map.of("success", false, "message", "会话不存在");
        }
        // 校验会话归属
        if (!chatSession.getUserId().equals(userId)) {
            log.warn("⚠️ 用户 {} 尝试操作不属于自己的会话 {}", userId, id);
            return Map.of("success", false, "message", "无权操作此会话");
        }
        String title = body.getOrDefault("title", "").trim();
        if (title.isEmpty()) {
            return Map.of("success", false, "message", "标题不能为空");
        }
        chatSession.setSessionTitle(title);
        sessionRepository.save(chatSession);
        return Map.of("success", true);
    }

    /** 删除会话 */
    @DeleteMapping("/sessions/{id}")
    public Map<String, Object> deleteSession(@PathVariable Long id, HttpSession session) {
        Long userId = getUserId(session);
        if (userId == null) return Map.of("success", false, "message", "请先登录");

        ChatSession chatSession = sessionRepository.findById(id).orElse(null);
        if (chatSession == null) {
            return Map.of("success", false, "message", "会话不存在");
        }
        // 校验会话归属
        if (!chatSession.getUserId().equals(userId)) {
            log.warn("⚠️ 用户 {} 尝试删除不属于自己的会话 {}", userId, id);
            return Map.of("success", false, "message", "无权操作此会话");
        }
        messageRepository.deleteByUserIdAndSessionId(userId, id);
        sessionRepository.deleteByUserIdAndId(userId, id);
        return Map.of("success", true);
    }

    /** 获取会话消息 */
    @GetMapping("/sessions/{id}/messages")
    public List<ChatMessage> getMessages(@PathVariable Long id, HttpSession session) {
        Long userId = getUserId(session);
        if (userId == null) return List.of();

        // 校验会话归属
        ChatSession chatSession = sessionRepository.findById(id).orElse(null);
        if (chatSession == null || !chatSession.getUserId().equals(userId)) {
            return List.of();
        }
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(id);
    }

    // ==================== 天气 Widget API ====================

    /** 获取城市实时天气数据（供前端 WeatherWidget 调用） */
    @GetMapping("/weather/{city}")
    public Map<String, Object> getWeather(@PathVariable String city) {
        try {
            Map<String, Object> weatherData = qWeatherService.get7DayWeather(city);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> daily = (List<Map<String, Object>>) weatherData.get("daily");
            if (daily != null && !daily.isEmpty()) {
                Map<String, Object> today = daily.get(0);
                // 从 seed 数据获取热门景点（简化：从数据库查询）
                List<Map<String, String>> spots = getHotSpots(city);
                return Map.of(
                    "success", true,
                    "city", city,
                    "temp", today.get("tempMax") + "°C",
                    "desc", today.get("textDay"),
                    "humidity", today.get("humidity") + "%",
                    "wind", today.get("windDirDay") + " " + today.get("windScaleDay") + "级",
                    "tip", generateTip(today),
                    "spots", spots
                );
            }
            return Map.of("success", false, "message", "暂无天气数据");
        } catch (Exception e) {
            log.warn("天气查询失败 city={}: {}", city, e.getMessage());
            return Map.of("success", false, "message", "天气数据获取失败");
        }
    }

    // ==================== 辅助方法 ====================

    /** 从 Session 获取当前登录用户ID（返回 null 表示未登录） */
    private Long getUserId(HttpSession session) {
        return (Long) session.getAttribute("user_id");
    }

    private String getApiKey(HttpSession session) {
        return (String) session.getAttribute("deepseek_api_key");
    }

    /**
     * 获取或创建用户会话 — 用户隔离版本
     * - 如果传了 sessionId 且属于当前用户，直接返回
     * - 如果传了 sessionId 但不属于当前用户，拒绝并创建新会话
     * - 如果没传 sessionId，查找用户最近会话，没有则创建新会话
     */
    private Long getOrCreateSession(Long userId, Long sessionId) {
        // 情况1：传了有效的 sessionId 且属于当前用户
        if (sessionId != null && sessionId > 0) {
            ChatSession existing = sessionRepository.findById(sessionId).orElse(null);
            if (existing != null && existing.getUserId().equals(userId)) {
                return sessionId;
            }
            // sessionId 存在但不属于当前用户 → 拒绝，创建新会话
            log.warn("⚠️ 用户 {} 尝试使用不属于自己的会话 {}", userId, sessionId);
        }
        // 情况2：没传 sessionId 或 sessionId 无效 → 查找用户最近会话
        List<ChatSession> userSessions = sessionRepository.findByUserIdOrderByUpdatedAtDesc(userId);
        if (!userSessions.isEmpty()) {
            return userSessions.get(0).getId();
        }
        // 情况3：用户没有任何会话 → 创建新会话
        ChatSession newSession = ChatSession.builder()
            .userId(userId)
            .sessionTitle("新的对话")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        return sessionRepository.save(newSession).getId();
    }

    /** 从数据库获取城市热门景点 */
    private List<Map<String, String>> getHotSpots(String city) {
        var spots = scenicSpotRepository.findByCity(city);
        if (spots.size() > 5) spots = spots.subList(0, 5);
        return spots.stream()
            .map(s -> Map.of("name", s.getName(), "rating", String.format("%.1f", s.getRating())))
            .toList();
    }

    /** 根据天气数据生成出游建议 */
    @SuppressWarnings("unchecked")
    private String generateTip(Map<String, Object> today) {
        String textDay = (String) today.get("textDay");
        String tempMax = (String) today.get("tempMax");
        int high = Integer.parseInt(tempMax);
        if (textDay.contains("雨") && (textDay.contains("大") || textDay.contains("暴")))
            return "有恶劣天气，建议推迟户外出行，安全第一 ⚠️";
        if (textDay.contains("雨"))
            return "有降雨，建议随身带伞，室内景点更合适 ☔";
        if (textDay.contains("雪"))
            return "有降雪，注意保暖和路面湿滑 ❄️";
        if (high > 35)
            return "高温天气，注意防晒防暑，中午避免暴晒 🔥";
        if (high >= 20 && high <= 28 && !textDay.contains("雨"))
            return "温度舒适，非常适合出游！🌸";
        return "天气尚可，可以正常出游 👍";
    }
}
