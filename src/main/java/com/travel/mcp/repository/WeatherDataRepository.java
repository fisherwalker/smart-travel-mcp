package com.travel.mcp.repository;

import com.travel.mcp.entity.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 天气数据 Repository
 */
@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {

    /** 按城市+日期段查询天气 */
    List<WeatherData> findByCityAndWeatherDateBetween(String city, LocalDate startDate, LocalDate endDate);

    /** 按城市+指定日期查询天气 */
    WeatherData findByCityAndWeatherDate(String city, LocalDate date);

    /** 按城市查询未来N天天气 */
    List<WeatherData> findByCityAndWeatherDateGreaterThanEqualOrderByWeatherDateAsc(
            String city, LocalDate date);
}
