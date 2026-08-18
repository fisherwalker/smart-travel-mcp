package com.travel.mcp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 酒店实体
 */
@Entity
@Table(name = "hotel")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 酒店名称 */
    @Column(nullable = false, length = 100)
    private String name;

    /** 所在城市 */
    @Column(nullable = false, length = 50)
    private String city;

    /** 星级 1-5 */
    private Integer star;

    /** 每晚价格 */
    @Column(name = "price_per_night", precision = 10, scale = 2)
    private BigDecimal pricePerNight;

    /** 剩余房间数 */
    @Column(name = "available_rooms")
    private Integer availableRooms;

    /** 地址 */
    @Column(length = 200)
    private String address;

    /** 评分 */
    private Double rating;
}
