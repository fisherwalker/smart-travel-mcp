package com.travel.mcp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 天气数据实体
 */
@Entity
@Table(name = "weather_data")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 城市 */
    @Column(nullable = false, length = 50)
    private String city;

    /** 日期 */
    @Column(name = "weather_date")
    private LocalDate weatherDate;

    /** 最高温℃ */
    @Column(name = "temperature_high")
    private Integer temperatureHigh;

    /** 最低温℃ */
    @Column(name = "temperature_low")
    private Integer temperatureLow;

    /** 天气类型：晴/多云/阴/雨/雪 */
    @Column(name = "weather_type", length = 20)
    private String weatherType;

    /** 湿度% */
    private Integer humidity;

    /** 风力 */
    @Column(name = "wind_level", length = 10)
    private String windLevel;

    /** 出游建议 */
    @Column(name = "travel_advice", length = 255)
    private String travelAdvice;
}
