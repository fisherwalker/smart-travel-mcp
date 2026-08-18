package com.travel.mcp.repository;

import com.travel.mcp.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 聊天消息 Repository
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /** 按会话ID查询消息，按时间正序 */
    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

    /** 按用户ID和会话ID删除消息（用户隔离） */
    @Transactional
    void deleteByUserIdAndSessionId(Long userId, Long sessionId);
}
