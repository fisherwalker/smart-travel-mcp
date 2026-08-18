package com.travel.mcp.repository;

import com.travel.mcp.entity.ScenicSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 景点 Repository
 */
@Repository
public interface ScenicSpotRepository extends JpaRepository<ScenicSpot, Long> {

    /** 按城市查询 */
    List<ScenicSpot> findByCity(String city);

    /** 按类别查询 */
    List<ScenicSpot> findByCategory(String category);

    /** 按城市+类别查询 */
    List<ScenicSpot> findByCityAndCategory(String city, String category);

    /** 按名称模糊搜索 */
    List<ScenicSpot> findByNameContaining(String keyword);

    /** 按描述模糊搜索 */
    @Query("SELECT s FROM ScenicSpot s WHERE s.description LIKE %:keyword%")
    List<ScenicSpot> searchByDescription(@Param("keyword") String keyword);

    /** 城市景点数量统计 */
    @Query("SELECT s.city, COUNT(s) FROM ScenicSpot s GROUP BY s.city ORDER BY COUNT(s) DESC")
    List<Object[]> countByCity();

    /** 按评分排序 */
    List<ScenicSpot> findAllByOrderByRatingDesc();

    /** 按价格升序 */
    List<ScenicSpot> findByCityOrderByPriceAsc(String city);
}
