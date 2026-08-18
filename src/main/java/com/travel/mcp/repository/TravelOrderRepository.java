package com.travel.mcp.repository;

import com.travel.mcp.entity.TravelOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 订单 Repository
 */
@Repository
public interface TravelOrderRepository extends JpaRepository<TravelOrder, Long> {

    /** 按用户名查询所有订单 */
    List<TravelOrder> findByUserName(String userName);

    /** 按用户名+状态查询 */
    List<TravelOrder> findByUserNameAndStatus(String userName, String status);

    /** 按状态查询 */
    List<TravelOrder> findByStatus(String status);

    /** 统计用户订单数 */
    long countByUserName(String userName);
}
