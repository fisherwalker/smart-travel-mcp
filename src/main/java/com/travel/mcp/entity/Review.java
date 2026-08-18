package com.travel.mcp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 评价实体
 */
@Entity
@Table(name = "review")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 用户名 */
    @Column(name = "user_name", nullable = false, length = 50)
    private String userName;

    /** 评价对象类型：SPOT / HOTEL / ROUTE */
    @Column(name = "target_type", length = 20)
    private String targetType;

    /** 对象ID */
    @Column(name = "target_id")
    private Long targetId;

    /** 评分 1-5 */
    private Integer rating;

    /** 评价内容 */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** 评价日期 */
    @Column(name = "review_date")
    private LocalDate reviewDate;
}
