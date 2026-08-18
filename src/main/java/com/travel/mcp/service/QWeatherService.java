package com.travel.mcp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * 天气服务（wttr.in 免费 API，无需 Key）
 * 简单稳定，全球覆盖，无需注册
 */
@Slf4j
@Service
public class QWeatherService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public QWeatherService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /** 中文城市名 → wttr.in 拼音名 */
    private static final Map<String, String> CITY_ALIAS = new LinkedHashMap<>();
    static {
        CITY_ALIAS.put("北京", "Beijing");
        CITY_ALIAS.put("上海", "Shanghai");
        CITY_ALIAS.put("广州", "Guangzhou");
        CITY_ALIAS.put("深圳", "Shenzhen");
        CITY_ALIAS.put("杭州", "Hangzhou");
        CITY_ALIAS.put("南京", "Nanjing");
        CITY_ALIAS.put("成都", "Chengdu");
        CITY_ALIAS.put("重庆", "Chongqing");
        CITY_ALIAS.put("武汉", "Wuhan");
        CITY_ALIAS.put("西安", "Xian");
        CITY_ALIAS.put("苏州", "Suzhou");
        CITY_ALIAS.put("长沙", "Changsha");
        CITY_ALIAS.put("厦门", "Xiamen");
        CITY_ALIAS.put("青岛", "Qingdao");
        CITY_ALIAS.put("大连", "Dalian");
        CITY_ALIAS.put("昆明", "Kunming");
        CITY_ALIAS.put("三亚", "Sanya");
        CITY_ALIAS.put("哈尔滨", "Harbin");
        CITY_ALIAS.put("拉萨", "Lhasa");
        CITY_ALIAS.put("天津", "Tianjin");
        CITY_ALIAS.put("郑州", "Zhengzhou");
        CITY_ALIAS.put("大理", "Dali");
        CITY_ALIAS.put("桂林", "Guilin");
        CITY_ALIAS.put("东京", "Tokyo");
        CITY_ALIAS.put("曼谷", "Bangkok");
        CITY_ALIAS.put("巴黎", "Paris");
    }

    /**
     * 获取城市 N 天天气预报
     */
    public Map<String, Object> get7DayWeather(String cityName) {
        // 中→英城市名
        String lookup = CITY_ALIAS.getOrDefault(cityName, cityName);
        String url = "http://wttr.in/" + lookup + "?format=j1";

        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                log.info("wttr.in 第{}次请求: {}", attempt, url);
                JsonNode root = objectMapper.readTree(restTemplate.getForObject(url, String.class));

                List<Map<String, Object>> dailyList = new ArrayList<>();
                JsonNode weather = root.path("weather");

                for (int i = 0; i < weather.size() && i < 7; i++) {
                    JsonNode day = weather.get(i);
                    JsonNode hourly = day.path("hourly").get(0);

                    // 风速（km/h → 蒲福风力等级）
                    int windKmph = hourly.path("windspeedKmph").asInt(0);
                    int beaufort = kmphToBeaufort(windKmph);
                    String windDir = hourly.path("winddir16Point").asText();

                    Map<String, Object> d = new LinkedHashMap<>();
                    d.put("fxDate", day.path("date").asText());
                    d.put("tempMax", day.path("maxtempC").asText());
                    d.put("tempMin", day.path("mintempC").asText());
                    d.put("textDay", translateWeatherDesc(hourly.path("weatherDesc").get(0).path("value").asText()));
                    d.put("humidity", hourly.path("humidity").asText());
                    d.put("windDirDay", windDir);
                    d.put("windScaleDay", String.valueOf(beaufort));   // 蒲福风力等级 0-12
                    d.put("windSpeedKmph", String.valueOf(windKmph));  // 实际风速 km/h
                    dailyList.add(d);
                }

                Map<String, Object> result = new LinkedHashMap<>();
                result.put("code", "200");
                result.put("city", cityName);
                result.put("daily", dailyList);
                log.info("✅ wttr.in 返回 {} 天天气数据", dailyList.size());
                return result;

            } catch (Exception ex) {
                log.warn("wttr.in 第{}次失败: {}", attempt, ex.getMessage());
                if (attempt < 2) try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            }
        }
        throw new RuntimeException("天气数据获取失败（已重试2次）。请检查网络连接。");
    }

    /**
     * 将 wttr.in 英文天气描述翻译为中文
     */
    static String translateWeatherDesc(String english) {
        if (english == null || english.isBlank()) return "未知";
        // 先尝试精确匹配
        String result = WEATHER_DESC_ZH.get(english);
        if (result != null) return result;
        // 模糊匹配：忽略大小写和空格
        String lower = english.toLowerCase().replace(" ", "");
        for (var entry : WEATHER_DESC_ZH.entrySet()) {
            if (entry.getKey().toLowerCase().replace(" ", "").equals(lower)) {
                return entry.getValue();
            }
        }
        // 部分匹配
        return english;  // 保留原文，让上层 getWeatherEmoji 用关键词匹配
    }

    /** 英文天气描述 → 中文映射 */
    private static final Map<String, String> WEATHER_DESC_ZH = new LinkedHashMap<>();
    static {
        WEATHER_DESC_ZH.put("Sunny", "晴");
        WEATHER_DESC_ZH.put("Clear", "晴");
        WEATHER_DESC_ZH.put("Partly cloudy", "多云");
        WEATHER_DESC_ZH.put("Partly Cloudy", "多云");
        WEATHER_DESC_ZH.put("Cloudy", "多云");
        WEATHER_DESC_ZH.put("Overcast", "阴");
        WEATHER_DESC_ZH.put("Mist", "薄雾");
        WEATHER_DESC_ZH.put("Fog", "雾");
        WEATHER_DESC_ZH.put("Freezing fog", "冻雾");
        WEATHER_DESC_ZH.put("Patchy rain possible", "局部阵雨");
        WEATHER_DESC_ZH.put("Patchy rain nearby", "局部阵雨");
        WEATHER_DESC_ZH.put("Patchy light rain", "局部小雨");
        WEATHER_DESC_ZH.put("Light rain", "小雨");
        WEATHER_DESC_ZH.put("Moderate rain", "中雨");
        WEATHER_DESC_ZH.put("Moderate rain at times", "间歇中雨");
        WEATHER_DESC_ZH.put("Heavy rain", "大雨");
        WEATHER_DESC_ZH.put("Heavy rain at times", "间歇大雨");
        WEATHER_DESC_ZH.put("Light drizzle", "小毛毛雨");
        WEATHER_DESC_ZH.put("Patchy light drizzle", "局部毛毛雨");
        WEATHER_DESC_ZH.put("Thundery outbreaks possible", "可能有雷暴");
        WEATHER_DESC_ZH.put("Torrential rain shower", "暴雨");
        WEATHER_DESC_ZH.put("Light rain shower", "小阵雨");
        WEATHER_DESC_ZH.put("Moderate or heavy rain shower", "中到大阵雨");
        WEATHER_DESC_ZH.put("Patchy snow possible", "局部降雪");
        WEATHER_DESC_ZH.put("Patchy light snow", "局部小雪");
        WEATHER_DESC_ZH.put("Light snow", "小雪");
        WEATHER_DESC_ZH.put("Moderate snow", "中雪");
        WEATHER_DESC_ZH.put("Heavy snow", "大雪");
        WEATHER_DESC_ZH.put("Blizzard", "暴风雪");
        WEATHER_DESC_ZH.put("Blowing snow", "吹雪");
        WEATHER_DESC_ZH.put("Ice pellets", "冰粒");
        WEATHER_DESC_ZH.put("Light sleet", "小雨夹雪");
        WEATHER_DESC_ZH.put("Moderate or heavy sleet", "中到大雨夹雪");
        WEATHER_DESC_ZH.put("Freezing drizzle", "冻毛毛雨");
        WEATHER_DESC_ZH.put("Light freezing rain", "小冻雨");
        WEATHER_DESC_ZH.put("Moderate or heavy freezing rain", "中到大冻雨");
        WEATHER_DESC_ZH.put("Hail", "冰雹");
        WEATHER_DESC_ZH.put("Windy", "大风");
        WEATHER_DESC_ZH.put("Breezy", "微风");
        WEATHER_DESC_ZH.put("Squalls", "狂风");
    }

    /**
     * 将风速 km/h 转换为蒲福风力等级（0-12）
     * @see <a href="https://en.wikipedia.org/wiki/Beaufort_scale">Beaufort scale</a>
     */
    static int kmphToBeaufort(int kmph) {
        if (kmph < 1)   return 0;   // 无风
        if (kmph <= 5)  return 1;   // 软风
        if (kmph <= 11) return 2;   // 轻风
        if (kmph <= 19) return 3;   // 微风
        if (kmph <= 28) return 4;   // 和风
        if (kmph <= 38) return 5;   // 清风
        if (kmph <= 49) return 6;   // 强风
        if (kmph <= 61) return 7;   // 疾风/劲风
        if (kmph <= 74) return 8;   // 大风
        if (kmph <= 88) return 9;   // 烈风
        if (kmph <= 102) return 10; // 狂风/风暴
        if (kmph <= 117) return 11; // 暴风
        return 12;                   // 飓风
    }
}
