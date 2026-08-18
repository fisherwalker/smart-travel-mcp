package com.travel.mcp.repository;

import com.travel.mcp.entity.TravelRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 旅游路线 Repository
 */
@Repository
public interface TravelRouteRepository extends JpaRepository<TravelRoute, Long> {

    /** 按出发城市查询 */
    List<TravelRoute> findByStartCity(String startCity);

    /** 按出发城市+天数查询 */
    List<TravelRoute> findByStartCityAndDays(String startCity, Integer days);

    /** 按天数查询 */
    List<TravelRoute> findByDays(Integer days);

    /** 按评分排序 */
    List<TravelRoute> findAllByOrderByDaysAsc();
}
