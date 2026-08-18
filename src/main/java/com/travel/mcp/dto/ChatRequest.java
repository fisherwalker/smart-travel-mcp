package com.travel.mcp.dto;

/**
 * 聊天请求 DTO
 */
public class ChatRequest {

    private Long sessionId;
    private String message;

    public ChatRequest() {}

    public ChatRequest(Long sessionId, String message) {
        this.sessionId = sessionId;
        this.message = message;
    }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
