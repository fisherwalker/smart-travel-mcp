package com.travel.mcp.repository;

import com.travel.mcp.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 评价 Repository
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /** 按目标类型+目标ID查询评价 */
    List<Review> findByTargetTypeAndTargetId(String targetType, Long targetId);

    /** 按评价对象类型查询 */
    List<Review> findByTargetType(String targetType);

    /** 按用户名查询 */
    List<Review> findByUserName(String userName);
}
