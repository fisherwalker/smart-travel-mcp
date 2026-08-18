package com.travel.mcp.service;

import com.travel.mcp.entity.*;
import com.travel.mcp.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 智慧旅游核心服务
 * 所有 @Tool 方法会被 Spring AI 自动注册为 MCP 工具
 * AI 大模型可以直接调用这些方法来查询旅游信息
 */
@Slf4j
@Service
public class TravelAssistantService {

    private final ScenicSpotRepository scenicSpotRepository;
    private final HotelRepository hotelRepository;
    private final TravelRouteRepository travelRouteRepository;
    private final TravelOrderRepository travelOrderRepository;
    private final QWeatherService qWeatherService;

    public TravelAssistantService(ScenicSpotRepository scenicSpotRepository,
                                   HotelRepository hotelRepository,
                                   TravelRouteRepository travelRouteRepository,
                                   TravelOrderRepository travelOrderRepository,
                                   QWeatherService qWeatherService) {
        this.scenicSpotRepository = scenicSpotRepository;
        this.hotelRepository = hotelRepository;
        this.travelRouteRepository = travelRouteRepository;
        this.travelOrderRepository = travelOrderRepository;
        this.qWeatherService = qWeatherService;
    }

    // ==================== 工具1：搜索景点 ====================
    @Tool(name = "searchSpots", description = "搜索旅游景点，可按城市、类别或关键词查询。返回景点名称、门票价格、评分和简介")
    public String searchSpots(
            @ToolParam(description = "城市名称，如：北京、上海、杭州、成都、西安、三亚、东京、巴黎等") String city,
            @ToolParam(description = "景点类别：自然风光、人文古迹、主题乐园、美食购物（可选）") String category,
            @ToolParam(description = "搜索关键词，模糊匹配景点名称和描述（可选）") String keyword) {

        log.info("🔍 搜索景点: city={}, category={}, keyword={}", city, category, keyword);

        List<ScenicSpot> results;
        if (keyword != null && !keyword.isBlank()) {
            results = scenicSpotRepository.findByNameContaining(keyword);
        } else if (city != null && !city.isBlank() && category != null && !category.isBlank()) {
            results = scenicSpotRepository.findByCityAndCategory(city, category);
        } else if (city != null && !city.isBlank()) {
            results = scenicSpotRepository.findByCity(city);
        } else if (category != null && !category.isBlank()) {
            results = scenicSpotRepository.findByCategory(category);
        } else {
            results = scenicSpotRepository.findAllByOrderByRatingDesc();
        }

        if (results.isEmpty()) {
            return "😔 没有找到符合条件的景点，试试其他关键词吧！";
        }
        return "🏔️ **找到 " + results.size() + " 个景点**：\n\n" +
               results.stream()
                   .map(s -> String.format("**【%s】** | 📍%s | 🏷️%s | 💰¥%s | ⭐%.1f\n> %s",
                       s.getName(), s.getCity(), s.getCategory(),
                       s.getPrice().toString(), s.getRating(), s.getDescription()))
                   .collect(Collectors.joining("\n\n"));
    }

    // ==================== 工具2：搜索酒店 ====================
    @Tool(name = "searchHotels", description = "搜索酒店，按城市、最低星级、最高价格筛选。返回酒店名称、星级、价格、剩余房间和评分")
    public String searchHotels(
            @ToolParam(description = "城市名称，如：北京、上海、杭州、成都、西安、三亚等") String city,
            @ToolParam(description = "最低星级 1-5（可选，默认不限制）") Integer minStar,
            @ToolParam(description = "最高每晚价格（可选，默认不限制）") Double maxPrice) {

        log.info("🏨 搜索酒店: city={}, minStar={}, maxPrice={}", city, minStar, maxPrice);

        int star = (minStar != null) ? minStar : 1;
        BigDecimal price = (maxPrice != null) ? BigDecimal.valueOf(maxPrice) : new BigDecimal("99999");

        List<Hotel> results = hotelRepository.findByCityAndFilters(city, star, price);

        if (results.isEmpty()) {
            return "😔 " + city + " 没有找到" + star + "星级以上、¥" +
                   (maxPrice != null ? maxPrice.intValue() : "不限") + "以内的酒店。";
        }
        return "🏨 **" + city + " 酒店搜索结果**（" + star + "星以上，¥" +
               (maxPrice != null ? maxPrice.intValue() : "不限") + "以内）：\n\n" +
               results.stream()
                   .map(h -> String.format("**【%s】** | %s星级 | 💰¥%s/晚 | 🛏️%d间 | ⭐%.1f\n> 📍%s",
                       h.getName(), "⭐".repeat(h.getStar()),
                       h.getPricePerNight().toString(), h.getAvailableRooms(),
                       h.getRating(), h.getAddress()))
                   .collect(Collectors.joining("\n\n"));
    }

    // ==================== 工具3：推荐路线 ====================
    @Tool(name = "recommendRoutes", description = "推荐旅游路线，按出发城市和天数筛选。返回路线名称、天数、价格、包含景点和简介")
    public String recommendRoutes(
            @ToolParam(description = "出发城市，如：北京、杭州、南京、成都") String startCity,
            @ToolParam(description = "游玩天数（可选，1-7天）") Integer days) {

        log.info("🗺️ 推荐路线: startCity={}, days={}", startCity, days);

        List<TravelRoute> results;
        if (startCity != null && !startCity.isBlank() && days != null) {
            results = travelRouteRepository.findByStartCityAndDays(startCity, days);
        } else if (startCity != null && !startCity.isBlank()) {
            results = travelRouteRepository.findByStartCity(startCity);
        } else if (days != null) {
            results = travelRouteRepository.findByDays(days);
        } else {
            results = travelRouteRepository.findAll();
        }

        if (results.isEmpty()) {
            return "😔 没有找到符合条件的路线。试试其他出发城市或天数吧！";
        }
        return "🗺️ **推荐 " + results.size() + " 条路线**：\n\n" +
               results.stream()
                   .map(r -> String.format("**【%s】** | 📍%s出发 | 🕐%d天 | 💰¥%s\n> 📌 景点：%s\n> %s",
                       r.getName(), r.getStartCity(), r.getDays(),
                       r.getPrice().toString(), r.getSpots(), r.getDescription()))
                   .collect(Collectors.joining("\n\n"));
    }

    // ==================== 工具4：统计分析 ====================
    @Tool(name = "getTravelStats", description = "获取旅游数据统计分析，包括各城市景点分布、各星级酒店数量、热门路线推荐")
    public String getTravelStats() {

        log.info("📊 统计分析");

        StringBuilder sb = new StringBuilder();
        sb.append("📊 **旅游数据统计分析**\n\n");

        // 景点统计
        sb.append("### 🏔️ 各城市景点分布\n");
        List<Object[]> spotStats = scenicSpotRepository.countByCity();
        for (Object[] row : spotStats) {
            sb.append(String.format("- **%s**：%d个景点\n", row[0], row[1]));
        }

        // 酒店统计
        sb.append("\n### 🏨 各星级酒店分布\n");
        List<Object[]> hotelStats = hotelRepository.countByStar();
        for (Object[] row : hotelStats) {
            sb.append(String.format("- **%s星级**：%d家\n", row[0], row[1]));
        }

        // 最受欢迎景点
        sb.append("\n### ⭐ 评分最高景点 TOP3\n");
        scenicSpotRepository.findAllByOrderByRatingDesc().stream()
            .limit(3)
            .forEach(s -> sb.append(String.format("- **%s**（%s） ⭐%.1f\n", s.getName(), s.getCity(), s.getRating())));

        // 路线数量
        long routeCount = travelRouteRepository.count();
        sb.append("\n### 🗺️ 路线总数\n");
        sb.append("- ").append(routeCount).append(" 条精选路线\n");

        // 订单总数
        long orderCount = travelOrderRepository.count();
        sb.append("\n### 📝 订单总数\n");
        sb.append("- ").append(orderCount).append(" 笔订单\n");

        return sb.toString();
    }

    // ==================== 工具5：天气查询+出游建议 ====================
    @Tool(name = "getWeatherAdvice", description = "查询指定城市未来N天的天气，并给出游建议。返回每日温度、天气类型、湿度和出游建议")
    public String getWeatherAdvice(
            @ToolParam(description = "城市名称，如：北京、上海、杭州、成都、西安、三亚、东京等") String city,
            @ToolParam(description = "查询未来几天（1-7天，默认3天）") Integer days) {

        log.info("🌤️ 天气查询（Open-Meteo）: city={}, days={}", city, days);

        int queryDays = (days != null && days >= 1 && days <= 7) ? days : 3;

        try {
            Map<String, Object> weatherData = qWeatherService.get7DayWeather(city);
            return formatWeatherFromApi(weatherData, city, queryDays);
        } catch (Exception e) {
            log.error("天气 API 调用失败: {}", e.getMessage());
            throw new RuntimeException("天气数据暂时无法获取: " + e.getMessage());
        }
    }

    /**
     * 格式化 API 返回的天气数据
     */
    @SuppressWarnings("unchecked")
    private String formatWeatherFromApi(Map<String, Object> weatherData, String city, int queryDays) {
        List<Map<String, Object>> dailyList = (List<Map<String, Object>>) weatherData.get("daily");

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("🌤️ **%s 未来%d天天气预报**（数据来源：天气）\n\n", city, queryDays));

        int shown = 0;
        for (Map<String, Object> day : dailyList) {
            if (shown >= queryDays) break;

            String date = (String) day.get("fxDate");
            // Convert "2026-06-03" → "6月3日 周三"
            try {
                java.time.LocalDate ld = java.time.LocalDate.parse(date);
                String[] w = {"一","二","三","四","五","六","日"};
                date = ld.getMonthValue() + "月" + ld.getDayOfMonth() + "日 周" + w[ld.getDayOfWeek().getValue() - 1];
            } catch (Exception ignored) {}
            String textDay = (String) day.get("textDay");
            String tempMax = (String) day.get("tempMax");
            String tempMin = (String) day.get("tempMin");
            String humidity = (String) day.get("humidity");
            String windDir = (String) day.get("windDirDay");
            String windScale = (String) day.get("windScaleDay");
            String windSpeed = (String) day.getOrDefault("windSpeedKmph", windScale);

            // 天气图标映射
            String emoji = getWeatherEmoji(textDay);

            // 生成出游建议
            String advice = getTravelAdvice(textDay, tempMax, tempMin);

            // 风力描述：展示蒲福等级 + 风速
            String windDesc = windDir + " " + windScale + "级（" + windSpeed + "km/h）";

            sb.append(String.format("%s **%s** | %s | 🌡️ %s°C ~ %s°C | 💧%s%% | 🌬️%s\n> 💡 %s\n\n",
                emoji, date, textDay, tempMin, tempMax,
                humidity, windDesc, advice));
            shown++;
        }

        // 综合建议
        sb.append("---\n");
        sb.append(formatWeatherSummary(dailyList.subList(0, Math.min(queryDays, dailyList.size()))));
        return sb.toString();
    }

    private String getWeatherEmoji(String textDay) {
        if (textDay.contains("晴")) return "☀️";
        if (textDay.contains("多云")) return "⛅";
        if (textDay.contains("阴")) return "☁️";
        if (textDay.contains("雨")) return textDay.contains("大") || textDay.contains("暴") ? "⛈️" : "🌧️";
        if (textDay.contains("雪")) return "❄️";
        if (textDay.contains("雾") || textDay.contains("霾")) return "🌫️";
        return "🌡️";
    }

    private String getTravelAdvice(String textDay, String tempMax, String tempMin) {
        int high = Integer.parseInt(tempMax);
        int low = Integer.parseInt(tempMin);

        if (textDay.contains("雨") && (textDay.contains("大") || textDay.contains("暴"))) {
            return "有恶劣天气，建议推迟户外出行，安全第一";
        }
        if (textDay.contains("雨")) {
            return "有降雨，建议随身带伞，室内景点更合适";
        }
        if (textDay.contains("雪")) {
            return "有降雪，注意保暖和路面湿滑";
        }
        if (high > 35) {
            return "高温天气，注意防晒防暑，中午避免暴晒";
        }
        if (low < 5) {
            return "天气寒冷，注意保暖，穿厚外套";
        }
        if (high >= 20 && high <= 28 && !textDay.contains("雨")) {
            return "温度舒适，非常适合出游！";
        }
        return "天气尚可，可以正常出游";
    }

    @SuppressWarnings("unchecked")
    private String formatWeatherSummary(List<Map<String, Object>> dailyList) {
        long rainyDays = dailyList.stream()
            .filter(d -> ((String) d.get("textDay")).contains("雨")).count();
        long sunnyDays = dailyList.stream()
            .filter(d -> ((String) d.get("textDay")).contains("晴")).count();
        double avgHigh = dailyList.stream()
            .mapToInt(d -> Integer.parseInt((String) d.get("tempMax"))).average().orElse(20);

        StringBuilder advice = new StringBuilder("🎯 **综合出游建议**：\n");

        if (rainyDays == 0 && sunnyDays >= dailyList.size() / 2) {
            advice.append("- 👍 天气不错！非常适合户外旅游，建议去自然风光类景点\n");
        } else if (rainyDays > 0) {
            advice.append("- ☔ 有降雨天气，建议随身带伞，安排室内景点作为备选\n");
        }

        if (avgHigh > 32) {
            advice.append("- 🔥 温度较高，注意防晒防暑，多喝水\n");
        } else if (avgHigh < 15) {
            advice.append("- 🧥 温度偏低，建议带外套，注意保暖\n");
        } else if (avgHigh >= 20 && avgHigh <= 28) {
            advice.append("- 🌸 温度非常舒适，是出游的黄金天气！\n");
        }

        boolean hasBadWeather = dailyList.stream()
            .anyMatch(d -> {
                String t = (String) d.get("textDay");
                return t.contains("大") || t.contains("暴") || t.contains("雪");
            });
        if (hasBadWeather) {
            advice.append("- ⚠️ 有恶劣天气，建议推迟户外出行计划，安全第一\n");
        }

        return advice.toString();
    }
}
