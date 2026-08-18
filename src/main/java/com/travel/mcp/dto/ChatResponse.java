package com.travel.mcp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 聊天响应 DTO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatResponse {

    private Long sessionId;
    private String sessionTitle;
    private String reply;
    private Boolean needApiKey;

    public ChatResponse() {}

    public ChatResponse(Long sessionId, String sessionTitle, String reply) {
        this.sessionId = sessionId;
        this.sessionTitle = sessionTitle;
        this.reply = reply;
    }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public String getSessionTitle() { return sessionTitle; }
    public void setSessionTitle(String sessionTitle) { this.sessionTitle = sessionTitle; }
    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
    public Boolean getNeedApiKey() { return needApiKey; }
    public void setNeedApiKey(Boolean needApiKey) { this.needApiKey = needApiKey; }
}
