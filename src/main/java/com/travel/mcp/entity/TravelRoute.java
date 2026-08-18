package com.travel.mcp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 旅游路线实体
 */
@Entity
@Table(name = "travel_route")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 路线名称 */
    @Column(nullable = false, length = 100)
    private String name;

    /** 出发城市 */
    @Column(name = "start_city", nullable = false, length = 50)
    private String startCity;

    /** 目的地城市（JSON数组字符串） */
    @Column(name = "dest_cities", length = 200)
    private String destCities;

    /** 天数 */
    private Integer days;

    /** 价格 */
    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    /** 包含景点（JSON数组字符串） */
    @Column(length = 500)
    private String spots;

    /** 路线描述 */
    @Column(columnDefinition = "TEXT")
    private String description;
}
