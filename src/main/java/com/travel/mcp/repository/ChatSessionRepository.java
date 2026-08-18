package com.travel.mcp.repository;

import com.travel.mcp.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 聊天会话 Repository
 */
@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    /** 按用户ID查询所有会话，按更新时间倒序 */
    List<ChatSession> findByUserIdOrderByUpdatedAtDesc(Long userId);

    /** 按用户ID删除会话 */
    @Transactional
    void deleteByUserIdAndId(Long userId, Long id);
}
