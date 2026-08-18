package com.travel.mcp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 订单实体
 */
@Entity
@Table(name = "travel_order")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 用户名 */
    @Column(name = "user_name", nullable = false, length = 50)
    private String userName;

    /** 订单类型：HOTEL / ROUTE */
    @Column(name = "order_type", length = 20)
    private String orderType;

    /** 预订项ID */
    @Column(name = "item_id")
    private Long itemId;

    /** 预订项名称 */
    @Column(name = "item_name", length = 100)
    private String itemName;

    /** 数量 */
    private Integer quantity;

    /** 总价 */
    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;

    /** 下单日期 */
    @Column(name = "order_date")
    private LocalDate orderDate;

    /** 状态：PENDING / CONFIRMED / CANCELLED */
    @Column(length = 20)
    private String status;
}
