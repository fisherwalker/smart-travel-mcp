package com.travel.mcp.repository;

import com.travel.mcp.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * 酒店 Repository
 */
@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    /** 按城市查询 */
    List<Hotel> findByCity(String city);

    /** 按城市+最低星级+最高价格查询 */
    @Query("SELECT h FROM Hotel h WHERE h.city = :city " +
           "AND h.star >= :minStar AND h.pricePerNight <= :maxPrice " +
           "ORDER BY h.rating DESC")
    List<Hotel> findByCityAndFilters(@Param("city") String city,
                                     @Param("minStar") Integer minStar,
                                     @Param("maxPrice") BigDecimal maxPrice);

    /** 按星级查询 */
    List<Hotel> findByStar(Integer star);

    /** 各星级酒店数量统计 */
    @Query("SELECT h.star, COUNT(h) FROM Hotel h GROUP BY h.star ORDER BY h.star")
    List<Object[]> countByStar();

    /** 按城市查询、按价格升序 */
    List<Hotel> findByCityOrderByPricePerNightAsc(String city);

    /** 查询有可用房间的酒店 */
    List<Hotel> findByCityAndAvailableRoomsGreaterThanOrderByPricePerNightAsc(
            String city, Integer minRooms);
}
