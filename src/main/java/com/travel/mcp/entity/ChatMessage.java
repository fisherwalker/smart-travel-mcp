package com.travel.mcp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天消息实体
 */
@Entity
@Table(name = "chat_message")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属用户ID（数据隔离） */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 所属会话ID */
    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    /** 角色：user / assistant / tool / system */
    @Column(length = 20)
    private String role;

    /** 消息内容 */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** 工具名称（仅 role=tool 时有值） */
    @Column(name = "tool_name", length = 50)
    private String toolName;

    /** 创建时间 */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
