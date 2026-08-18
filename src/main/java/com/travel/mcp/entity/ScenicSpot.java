package com.travel.mcp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 景点实体
 */
@Entity
@Table(name = "scenic_spot")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScenicSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 景点名称 */
    @Column(nullable = false, length = 100)
    private String name;

    /** 所在城市 */
    @Column(nullable = false, length = 50)
    private String city;

    /** 类别：自然风光/人文古迹/主题乐园/美食购物 */
    @Column(length = 30)
    private String category;

    /** 门票价格 */
    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    /** 评分 1-5 */
    private Double rating;

    /** 景点描述 */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 图片URL */
    @Column(length = 255)
    private String imageUrl;
}
