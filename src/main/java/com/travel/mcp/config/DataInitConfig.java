package com.travel.mcp.config;

import com.travel.mcp.entity.*;
import com.travel.mcp.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 种子数据初始化
 * 启动时检测数据库是否为空，为空则自动插入演示数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitConfig {

    private final ScenicSpotRepository scenicSpotRepository;
    private final HotelRepository hotelRepository;
    private final TravelRouteRepository travelRouteRepository;
    private final TravelOrderRepository travelOrderRepository;
    private final ReviewRepository reviewRepository;
    private final WeatherDataRepository weatherDataRepository;
    private final com.travel.mcp.repository.ChatSessionRepository chatSessionRepository;

    @PostConstruct
    public void initData() {
        if (scenicSpotRepository.count() > 0) {
            log.info("✅ 数据库已有数据，跳过种子数据初始化");
            // 但确保至少有一个默认会话
            initDefaultSession();
            return;
        }

        log.info("🌱 开始初始化种子数据...");

        initScenicSpots();
        initHotels();
        initTravelRoutes();
        initWeatherData();
        initReviews();
        initOrders();

        log.info("✅ 种子数据初始化完成！");
        log.info("   - 景点: {} 条", scenicSpotRepository.count());
        log.info("   - 酒店: {} 条", hotelRepository.count());
        log.info("   - 路线: {} 条", travelRouteRepository.count());
        log.info("   - 天气: {} 条", weatherDataRepository.count());
        log.info("   - 评价: {} 条", reviewRepository.count());
        log.info("   - 订单: {} 条", travelOrderRepository.count());

        initDefaultSession();
    }

    // ==================== 景点数据（覆盖中国 Top20 旅游城市 + 境外热门城市） ====================
    private void initScenicSpots() {
        List<ScenicSpot> spots = List.of(
            // 北京
            ScenicSpot.builder().name("故宫").city("北京").category("人文古迹")
                .price(new BigDecimal("60.00")).rating(4.8)
                .description("中国古代宫殿建筑的精华，世界五大宫之首").imageUrl("/images/gugong.jpg").build(),
            ScenicSpot.builder().name("长城（八达岭）").city("北京").category("人文古迹")
                .price(new BigDecimal("40.00")).rating(4.7)
                .description("世界文化遗产，万里长城最具代表性的一段").imageUrl("/images/changcheng.jpg").build(),
            ScenicSpot.builder().name("颐和园").city("北京").category("自然风光")
                .price(new BigDecimal("30.00")).rating(4.6)
                .description("中国现存最大的皇家园林，以昆明湖、万寿山为基址").imageUrl("/images/yiheyuan.jpg").build(),
            // 上海
            ScenicSpot.builder().name("外滩").city("上海").category("自然风光")
                .price(new BigDecimal("0.00")).rating(4.8)
                .description("上海城市名片，黄浦江畔万国建筑博览群，夜景璀璨").imageUrl("/images/waitan.jpg").build(),
            ScenicSpot.builder().name("上海迪士尼乐园").city("上海").category("主题乐园")
                .price(new BigDecimal("475.00")).rating(4.7)
                .description("中国大陆首座迪士尼主题乐园，七大主题园区").imageUrl("/images/disney.jpg").build(),
            // 广州
            ScenicSpot.builder().name("广州塔").city("广州").category("人文古迹")
                .price(new BigDecimal("150.00")).rating(4.5)
                .description("广州新地标，600米高电视塔，可俯瞰全城").imageUrl("/images/guangzhouta.jpg").build(),
            ScenicSpot.builder().name("长隆野生动物世界").city("广州").category("主题乐园")
                .price(new BigDecimal("300.00")).rating(4.7)
                .description("亚洲最大的野生动物主题公园，可以看到大熊猫三胞胎").imageUrl("/images/changlong.jpg").build(),
            // 深圳
            ScenicSpot.builder().name("世界之窗").city("深圳").category("主题乐园")
                .price(new BigDecimal("220.00")).rating(4.4)
                .description("汇集世界奇观、历史遗迹、古今名胜的大型文化主题公园").imageUrl("/images/shijiezhichuang.jpg").build(),
            // 杭州
            ScenicSpot.builder().name("西湖").city("杭州").category("自然风光")
                .price(new BigDecimal("0.00")).rating(4.9)
                .description("天下西湖三十六，就中最好是杭州。中国最美的城市湖泊").imageUrl("/images/xihu.jpg").build(),
            ScenicSpot.builder().name("灵隐寺").city("杭州").category("人文古迹")
                .price(new BigDecimal("45.00")).rating(4.7)
                .description("中国佛教著名寺院，始建于东晋咸和元年，距今约1700年").imageUrl("/images/lingyinsi.jpg").build(),
            // 南京
            ScenicSpot.builder().name("中山陵").city("南京").category("人文古迹")
                .price(new BigDecimal("0.00")).rating(4.7)
                .description("中国近代伟大民主革命先行者孙中山先生的陵寝").imageUrl("/images/zhongshanling.jpg").build(),
            ScenicSpot.builder().name("夫子庙-秦淮河").city("南京").category("美食购物")
                .price(new BigDecimal("0.00")).rating(4.5)
                .description("南京最具代表性的历史文化街区").imageUrl("/images/fuzimiao.jpg").build(),
            // 成都
            ScenicSpot.builder().name("宽窄巷子").city("成都").category("美食购物")
                .price(new BigDecimal("0.00")).rating(4.5)
                .description("成都三大历史文化名城保护街区之一，品茶、美食、川剧变脸").imageUrl("/images/kuanzhaixiangzi.jpg").build(),
            ScenicSpot.builder().name("大熊猫繁育研究基地").city("成都").category("主题乐园")
                .price(new BigDecimal("55.00")).rating(4.8)
                .description("全球最大的大熊猫人工繁育基地").imageUrl("/images/xiongmao.jpg").build(),
            // 武汉
            ScenicSpot.builder().name("黄鹤楼").city("武汉").category("人文古迹")
                .price(new BigDecimal("70.00")).rating(4.6)
                .description("天下江山第一楼，李白崔颢留下传世名篇").imageUrl("/images/huanghelou.jpg").build(),
            // 西安
            ScenicSpot.builder().name("兵马俑").city("西安").category("人文古迹")
                .price(new BigDecimal("120.00")).rating(4.9)
                .description("世界第八大奇迹，秦始皇陵大型陪葬坑").imageUrl("/images/bingmayong.jpg").build(),
            ScenicSpot.builder().name("大雁塔").city("西安").category("人文古迹")
                .price(new BigDecimal("50.00")).rating(4.6)
                .description("唐代玄奘为保存佛经而建，西安标志性建筑").imageUrl("/images/dayanta.jpg").build(),
            // 重庆
            ScenicSpot.builder().name("洪崖洞").city("重庆").category("美食购物")
                .price(new BigDecimal("0.00")).rating(4.6)
                .description("依山而建的吊脚楼群，山城夜景最佳观赏地").imageUrl("/images/hongyadong.jpg").build(),
            // 苏州
            ScenicSpot.builder().name("拙政园").city("苏州").category("自然风光")
                .price(new BigDecimal("80.00")).rating(4.8)
                .description("中国四大名园之首，江南古典园林代表作").imageUrl("/images/zhuozhengyuan.jpg").build(),
            // 厦门
            ScenicSpot.builder().name("鼓浪屿").city("厦门").category("自然风光")
                .price(new BigDecimal("35.00")).rating(4.7)
                .description("海上花园，万国建筑博物馆，钢琴之岛").imageUrl("/images/gulangyu.jpg").build(),
            // 青岛
            ScenicSpot.builder().name("崂山").city("青岛").category("自然风光")
                .price(new BigDecimal("90.00")).rating(4.5)
                .description("道教名山，海上第一名山，山海相连的绝美景色").imageUrl("/images/laoshan.jpg").build(),
            // 三亚
            ScenicSpot.builder().name("亚龙湾").city("三亚").category("自然风光")
                .price(new BigDecimal("0.00")).rating(4.8)
                .description("天下第一湾，洁白沙滩与碧蓝海水交相辉映").imageUrl("/images/yalongwan.jpg").build(),
            // 昆明
            ScenicSpot.builder().name("石林").city("昆明").category("自然风光")
                .price(new BigDecimal("130.00")).rating(4.6)
                .description("世界自然遗产，喀斯特地貌奇观").imageUrl("/images/shilin.jpg").build(),
            // 哈尔滨
            ScenicSpot.builder().name("中央大街").city("哈尔滨").category("美食购物")
                .price(new BigDecimal("0.00")).rating(4.5)
                .description("亚洲最长的步行街，巴洛克建筑精华").imageUrl("/images/zhongyangdajie.jpg").build(),
            // 长沙
            ScenicSpot.builder().name("岳麓山").city("长沙").category("自然风光")
                .price(new BigDecimal("0.00")).rating(4.5)
                .description("南岳衡山七十二峰之一，千年学府岳麓书院所在").imageUrl("/images/yuelushan.jpg").build(),
            // 天津
            ScenicSpot.builder().name("天津之眼").city("天津").category("人文古迹")
                .price(new BigDecimal("70.00")).rating(4.4)
                .description("世界上唯一建在桥上的摩天轮，海河夜景尽收眼底").imageUrl("/images/tianjinzhiyan.jpg").build(),
            // 拉萨
            ScenicSpot.builder().name("布达拉宫").city("拉萨").category("人文古迹")
                .price(new BigDecimal("200.00")).rating(4.9)
                .description("世界屋脊上的宫殿，藏传佛教圣地").imageUrl("/images/budalagong.jpg").build(),
            // 大理
            ScenicSpot.builder().name("洱海").city("大理").category("自然风光")
                .price(new BigDecimal("0.00")).rating(4.7)
                .description("苍山洱海，风花雪月，云南最浪漫的目的地").imageUrl("/images/erhai.jpg").build(),
            // 桂林
            ScenicSpot.builder().name("漓江").city("桂林").category("自然风光")
                .price(new BigDecimal("215.00")).rating(4.8)
                .description("桂林山水甲天下，漓江百里画廊美不胜收").imageUrl("/images/lijiang.jpg").build(),
            // 境外热门目的地
            ScenicSpot.builder().name("东京塔").city("东京").category("人文古迹")
                .price(new BigDecimal("120.00")).rating(4.6)
                .description("东京地标建筑，可360度俯瞰东京全景").imageUrl("/images/dongjingta.jpg").build(),
            ScenicSpot.builder().name("浅草寺").city("东京").category("人文古迹")
                .price(new BigDecimal("0.00")).rating(4.7)
                .description("东京最古老的寺庙，雷门大灯笼是必打卡之地").imageUrl("/images/qiancaosi.jpg").build(),
            ScenicSpot.builder().name("大皇宫").city("曼谷").category("人文古迹")
                .price(new BigDecimal("100.00")).rating(4.8)
                .description("泰国最具代表性的建筑群，金碧辉煌的皇家宫殿").imageUrl("/images/dahuanggong.jpg").build(),
            ScenicSpot.builder().name("埃菲尔铁塔").city("巴黎").category("人文古迹")
                .price(new BigDecimal("220.00")).rating(4.8)
                .description("法国象征，世界最著名的建筑之一").imageUrl("/images/teieta.jpg").build(),
            ScenicSpot.builder().name("卢浮宫").city("巴黎").category("人文古迹")
                .price(new BigDecimal("150.00")).rating(4.9)
                .description("世界最大博物馆，蒙娜丽莎和维纳斯的家").imageUrl("/images/lufugong.jpg").build()
        );
        scenicSpotRepository.saveAll(spots);
    }

    // ==================== 酒店数据（扩充至更多城市） ====================
    private void initHotels() {
        List<Hotel> hotels = List.of(
            Hotel.builder().name("北京王府井希尔顿酒店").city("北京").star(5)
                .pricePerNight(new BigDecimal("888.00")).availableRooms(15)
                .address("北京市东城区王府井大街").rating(4.8).build(),
            Hotel.builder().name("北京如家快捷酒店(天安门店)").city("北京").star(2)
                .pricePerNight(new BigDecimal("199.00")).availableRooms(30)
                .address("北京市东城区大栅栏西街").rating(4.2).build(),
            Hotel.builder().name("上海和平饭店").city("上海").star(5)
                .pricePerNight(new BigDecimal("1588.00")).availableRooms(8)
                .address("上海市黄浦区南京东路").rating(4.9).build(),
            Hotel.builder().name("上海全季酒店(外滩店)").city("上海").star(3)
                .pricePerNight(new BigDecimal("368.00")).availableRooms(20)
                .address("上海市黄浦区中山东一路").rating(4.5).build(),
            Hotel.builder().name("广州四季酒店").city("广州").star(5)
                .pricePerNight(new BigDecimal("1280.00")).availableRooms(10)
                .address("广州市天河区珠江新城").rating(4.8).build(),
            Hotel.builder().name("杭州西子湖四季酒店").city("杭州").star(5)
                .pricePerNight(new BigDecimal("1580.00")).availableRooms(5)
                .address("杭州市西湖区灵隐路").rating(4.9).build(),
            Hotel.builder().name("杭州全季酒店(西湖店)").city("杭州").star(3)
                .pricePerNight(new BigDecimal("268.00")).availableRooms(25)
                .address("杭州市上城区延安路").rating(4.5).build(),
            Hotel.builder().name("南京金陵饭店").city("南京").star(5)
                .pricePerNight(new BigDecimal("680.00")).availableRooms(10)
                .address("南京市鼓楼区汉中路").rating(4.7).build(),
            Hotel.builder().name("南京汉庭酒店(夫子庙店)").city("南京").star(2)
                .pricePerNight(new BigDecimal("159.00")).availableRooms(35)
                .address("南京市秦淮区建康路").rating(4.3).build(),
            Hotel.builder().name("成都博舍酒店").city("成都").star(5)
                .pricePerNight(new BigDecimal("1200.00")).availableRooms(8)
                .address("成都市锦江区中纱帽街").rating(4.9).build(),
            Hotel.builder().name("成都7天连锁酒店(春熙路店)").city("成都").star(2)
                .pricePerNight(new BigDecimal("139.00")).availableRooms(40)
                .address("成都市锦江区春熙路").rating(4.1).build(),
            Hotel.builder().name("西安索菲特传奇酒店").city("西安").star(5)
                .pricePerNight(new BigDecimal("880.00")).availableRooms(12)
                .address("西安市碑林区东新街").rating(4.8).build(),
            Hotel.builder().name("西安汉庭(钟楼店)").city("西安").star(2)
                .pricePerNight(new BigDecimal("169.00")).availableRooms(30)
                .address("西安市碑林区东大街").rating(4.3).build(),
            Hotel.builder().name("三亚亚特兰蒂斯酒店").city("三亚").star(5)
                .pricePerNight(new BigDecimal("2288.00")).availableRooms(6)
                .address("三亚市海棠区海棠北路").rating(4.9).build(),
            Hotel.builder().name("厦门康莱德酒店").city("厦门").star(5)
                .pricePerNight(new BigDecimal("1080.00")).availableRooms(8)
                .address("厦门市思明区演武西路").rating(4.8).build(),
            Hotel.builder().name("东京新大谷饭店").city("东京").star(5)
                .pricePerNight(new BigDecimal("1600.00")).availableRooms(10)
                .address("东京都千代田区纪尾井町").rating(4.7).build(),
            Hotel.builder().name("曼谷悦榕庄酒店").city("曼谷").star(5)
                .pricePerNight(new BigDecimal("900.00")).availableRooms(12)
                .address("曼谷沙吞南路").rating(4.8).build(),
            Hotel.builder().name("巴黎丽兹酒店").city("巴黎").star(5)
                .pricePerNight(new BigDecimal("3800.00")).availableRooms(3)
                .address("巴黎旺多姆广场").rating(5.0).build()
        );
        hotelRepository.saveAll(hotels);
    }

    // ==================== 旅游路线数据（扩充至更多城市） ====================
    private void initTravelRoutes() {
        List<TravelRoute> routes = List.of(
            TravelRoute.builder().name("北京经典三日游").startCity("北京").days(3)
                .price(new BigDecimal("1599.00"))
                .destCities("[\"北京\"]")
                .spots("[\"故宫\",\"长城（八达岭）\",\"颐和园\",\"天安门广场\"]")
                .description("故宫→天安门→八达岭长城→颐和园，含2晚四星酒店+接送").build(),
            TravelRoute.builder().name("北京深度五日游").startCity("北京").days(5)
                .price(new BigDecimal("2899.00"))
                .destCities("[\"北京\"]")
                .spots("[\"故宫\",\"长城（八达岭）\",\"颐和园\",\"天坛\",\"圆明园\"]")
                .description("涵盖北京所有核心景点，含4晚五星酒店+专车+导游").build(),
            TravelRoute.builder().name("上海都市两日游").startCity("上海").days(2)
                .price(new BigDecimal("899.00"))
                .destCities("[\"上海\"]")
                .spots("[\"外滩\",\"上海迪士尼乐园\",\"南京路\"]")
                .description("外滩夜景→迪士尼狂欢→南京路购物，含1晚市区酒店").build(),
            TravelRoute.builder().name("杭州-千岛湖两日游").startCity("杭州").days(2)
                .price(new BigDecimal("699.00"))
                .destCities("[\"杭州\",\"淳安\"]")
                .spots("[\"西湖\",\"灵隐寺\",\"千岛湖\"]")
                .description("西湖泛舟→灵隐寺祈福→千岛湖游船登岛，含1晚湖景酒店").build(),
            TravelRoute.builder().name("杭州休闲四日游").startCity("杭州").days(4)
                .price(new BigDecimal("1899.00"))
                .destCities("[\"杭州\"]")
                .spots("[\"西湖\",\"灵隐寺\",\"千岛湖\",\"宋城\",\"龙井村\"]")
                .description("深度慢游，含3晚五星酒店+特色美食体验+茶园品茶").build(),
            TravelRoute.builder().name("南京文化两日游").startCity("南京").days(2)
                .price(new BigDecimal("599.00"))
                .destCities("[\"南京\"]")
                .spots("[\"中山陵\",\"夫子庙-秦淮河\",\"玄武湖\"]")
                .description("中山陵→夫子庙秦淮河夜游→玄武湖骑行，含1晚市区酒店").build(),
            TravelRoute.builder().name("成都美食三日游").startCity("成都").days(3)
                .price(new BigDecimal("1299.00"))
                .destCities("[\"成都\"]")
                .spots("[\"宽窄巷子\",\"大熊猫繁育研究基地\",\"都江堰\",\"锦里\"]")
                .description("看熊猫→品火锅→逛古街→赏川剧变脸，含2晚酒店+美食打卡").build(),
            TravelRoute.builder().name("西安古都三日游").startCity("西安").days(3)
                .price(new BigDecimal("1699.00"))
                .destCities("[\"西安\"]")
                .spots("[\"兵马俑\",\"大雁塔\",\"回民街\"]")
                .description("兵马俑→大雁塔→古城墙骑行→回民街美食，含2晚酒店").build(),
            TravelRoute.builder().name("三亚阳光五日游").startCity("三亚").days(5)
                .price(new BigDecimal("3999.00"))
                .destCities("[\"三亚\"]")
                .spots("[\"亚龙湾\",\"天涯海角\",\"蜈支洲岛\"]")
                .description("阳光沙滩→潜水体验→海鲜大餐，含4晚海景度假酒店").build(),
            TravelRoute.builder().name("东京三日文化游").startCity("东京").days(3)
                .price(new BigDecimal("5999.00"))
                .destCities("[\"东京\"]")
                .spots("[\"东京塔\",\"浅草寺\",\"秋叶原\"]")
                .description("东京塔→浅草寺祈福→秋叶原动漫购物，含2晚市区酒店").build(),
            TravelRoute.builder().name("曼谷三日风情游").startCity("曼谷").days(3)
                .price(new BigDecimal("3599.00"))
                .destCities("[\"曼谷\"]")
                .spots("[\"大皇宫\",\"水上市场\",\"考山路\"]")
                .description("大皇宫→水上市场→泰式按摩→夜市美食，含2晚酒店").build(),
            TravelRoute.builder().name("巴黎浪漫四日游").startCity("巴黎").days(4)
                .price(new BigDecimal("8999.00"))
                .destCities("[\"巴黎\"]")
                .spots("[\"埃菲尔铁塔\",\"卢浮宫\",\"塞纳河\"]")
                .description("铁塔登顶→卢浮宫艺术之旅→塞纳河游船→法式大餐，含3晚精品酒店").build()
        );
        travelRouteRepository.saveAll(routes);
    }

    // ==================== 天气数据（扩充至更多城市） ====================
    private void initWeatherData() {
        LocalDate today = LocalDate.now();
        String[][] cityWeathers = {
            {"北京", "32", "20", "晴", "35", "2-3级", "天气晴好，适合户外游览，建议去故宫、长城"},
            {"杭州", "28", "22", "多云", "65", "1-2级", "多云舒适，很适合游西湖、品龙井茶"},
            {"南京", "30", "21", "晴", "45", "2级", "天气不错，推荐去中山陵登高望远"},
            {"成都", "27", "19", "阴", "70", "1级", "阴天凉爽，逛宽窄巷子吃火锅舒服"},
            {"上海", "29", "22", "多云", "60", "3级", "温度适宜，适合外滩漫步"},
            {"广州", "33", "25", "多云", "80", "2级", "天气较热，可去长隆或广州塔室内游览"},
            {"深圳", "31", "24", "晴", "75", "2级", "阳光明媚，适合去世界之窗或海边"},
            {"武汉", "32", "23", "晴", "65", "2级", "晴朗天气，宜登黄鹤楼远眺长江"},
            {"西安", "28", "18", "多云", "50", "2级", "凉爽宜人，兵马俑和古城墙必打卡"},
            {"重庆", "29", "22", "雾", "78", "1级", "雾都风韵，洪崖洞夜景不容错过"},
            {"苏州", "27", "20", "小雨", "85", "2级", "烟雨江南韵味足，园林最美时"},
            {"厦门", "30", "23", "晴", "70", "3级", "海风宜人，鼓浪屿漫步正当时"},
            {"青岛", "25", "18", "晴", "60", "4级", "海风习习，啤酒与沙滩的浪漫"},
            {"三亚", "33", "26", "晴", "78", "3级", "热带阳光，海滩度假完美天气"},
            {"昆明", "24", "15", "晴", "55", "2级", "四季如春，滇池石林皆相宜"},
            {"哈尔滨", "22", "12", "多云", "45", "3级", "凉爽宜人，中央大街俄式风情"},
            {"长沙", "30", "22", "多云", "72", "2级", "逛吃橘子洲，岳麓书院品文韵"},
            {"天津", "28", "19", "晴", "55", "3级", "天津之眼配海河夜游，惬意"},
            {"拉萨", "22", "8", "晴", "30", "2级", "日光之城，布达拉宫圣洁庄严"},
            {"东京", "25", "18", "多云", "60", "2级", "气候宜人，适合游览东京塔和浅草寺"},
            {"曼谷", "35", "28", "晴", "80", "2级", "热带热情，大皇宫金碧辉煌"},
            {"巴黎", "22", "14", "多云", "55", "3级", "浪漫之都，铁塔和卢浮宫等你探索"}
        };

        for (String[] cityWeather : cityWeathers) {
            String city = cityWeather[0];
            for (int i = 0; i < 7; i++) {
                // 每天微调温度，模拟真实变化
                int tempOffset = (i % 3) - 1; // -1, 0, 1 循环
                int high = Integer.parseInt(cityWeather[1]) + tempOffset;
                int low = Integer.parseInt(cityWeather[2]) + tempOffset;
                String type = cityWeather[3];
                // 随机变一下天气类型
                if (i == 2 || i == 5) type = "多云";
                if (i == 3 || i == 6) type = i == 6 ? "阴" : "小雨";

                String advice = cityWeather[6];
                if (type.equals("小雨")) {
                    advice = "有小雨，建议带伞，室内景点如博物馆更合适";
                } else if (type.equals("阴")) {
                    advice = "阴天不晒，正是出游好天气，" + city + "的景点等着你！";
                }

                weatherDataRepository.save(WeatherData.builder()
                    .city(city)
                    .weatherDate(today.plusDays(i))
                    .temperatureHigh(high)
                    .temperatureLow(low)
                    .weatherType(type)
                    .humidity(Integer.parseInt(cityWeather[4]) + i * 2)
                    .windLevel(cityWeather[5])
                    .travelAdvice(advice)
                    .build());
            }
        }
    }

    // ==================== 评价数据（扩充覆盖更多城市） ====================
    private void initReviews() {
        List<Review> reviews = List.of(
            Review.builder().userName("小明").targetType("SPOT").targetId(1L)
                .rating(5).content("故宫太壮观了！红墙黄瓦，金碧辉煌，强烈推荐请导游讲解")
                .reviewDate(LocalDate.now().minusDays(3)).build(),
            Review.builder().userName("旅行达人").targetType("SPOT").targetId(1L)
                .rating(5).content("每次来北京必去，建议早上去人少，拍照效果好")
                .reviewDate(LocalDate.now().minusDays(7)).build(),
            Review.builder().userName("背包客小王").targetType("SPOT").targetId(9L) // 西湖
                .rating(5).content("西湖太美了！免费的景点却有最美的风景，杭州人民的幸福")
                .reviewDate(LocalDate.now().minusDays(1)).build(),
            Review.builder().userName("吃货张三").targetType("SPOT").targetId(13L) // 宽窄巷子
                .rating(4).content("宽窄巷子好吃好玩，就是人有点多，火锅一级棒！")
                .reviewDate(LocalDate.now().minusDays(5)).build(),
            Review.builder().userName("熊猫爱好者").targetType("SPOT").targetId(14L) // 大熊猫
                .rating(5).content("大熊猫太可爱了！一定要早上9点前去，能看到它们吃竹子")
                .reviewDate(LocalDate.now().minusDays(2)).build(),
            Review.builder().userName("古城迷").targetType("SPOT").targetId(17L) // 兵马俑
                .rating(5).content("兵马俑太震撼了，每一个俑的面部表情都不一样，震撼人心！")
                .reviewDate(LocalDate.now().minusDays(4)).build(),
            Review.builder().userName("海滩控").targetType("SPOT").targetId(23L) // 亚龙湾
                .rating(5).content("三亚的沙滩真的是国内顶级，海水清澈见底，完全不输东南亚")
                .reviewDate(LocalDate.now().minusDays(6)).build(),
            Review.builder().userName("文艺青年").targetType("SPOT").targetId(20L) // 拙政园
                .rating(5).content("一步一景，江南园林的精髓都在这里了，下雨天更有味道")
                .reviewDate(LocalDate.now().minusDays(9)).build(),
            Review.builder().userName("全球旅行家").targetType("SPOT").targetId(33L) // 埃菲尔铁塔
                .rating(5).content("巴黎的象征！特别是晚上灯光秀的时候，浪漫到哭")
                .reviewDate(LocalDate.now().minusDays(12)).build(),
            Review.builder().userName("寺庙朝圣者").targetType("SPOT").targetId(27L) // 布达拉宫
                .rating(5).content("一生必去一次的地方，红白宫殿在蓝天白云下格外庄严")
                .reviewDate(LocalDate.now().minusDays(15)).build(),
            Review.builder().userName("商务人士李总").targetType("HOTEL").targetId(1L)
                .rating(5).content("王府井希尔顿位置绝佳，步行就能到故宫，服务一流")
                .reviewDate(LocalDate.now().minusDays(4)).build(),
            Review.builder().userName("学生党小刘").targetType("HOTEL").targetId(2L)
                .rating(4).content("性价比很高，199一晚在市中心，干净整洁，推荐学生党")
                .reviewDate(LocalDate.now().minusDays(6)).build(),
            Review.builder().userName("品质游客").targetType("HOTEL").targetId(6L) // 杭州四季
                .rating(5).content("西湖边的顶级酒店，虽然贵但是值，推开窗就是西湖！")
                .reviewDate(LocalDate.now().minusDays(10)).build(),
            Review.builder().userName("海岛度假客").targetType("HOTEL").targetId(14L) // 三亚亚特兰蒂斯
                .rating(5).content("七星级的体验，水下套房太梦幻了！")
                .reviewDate(LocalDate.now().minusDays(2)).build(),
            Review.builder().userName("自驾游爱好者").targetType("ROUTE").targetId(1L)
                .rating(4).content("三天玩北京刚刚好，不赶也不松，导游很专业")
                .reviewDate(LocalDate.now().minusDays(8)).build(),
            Review.builder().userName("美食探店员").targetType("ROUTE").targetId(7L) // 成都三日
                .rating(5).content("成都美食之旅太值了！火锅、串串、兔头、蹄花，三天胖五斤！")
                .reviewDate(LocalDate.now().minusDays(3)).build(),
            Review.builder().userName("古都探索者").targetType("ROUTE").targetId(8L) // 西安三日
                .rating(5).content("兵马俑果然名不虚传，导游讲解很到位，回民街小吃超赞")
                .reviewDate(LocalDate.now().minusDays(11)).build()
        );
        reviewRepository.saveAll(reviews);
    }

    // ==================== 示例订单 ====================
    private void initOrders() {
        List<TravelOrder> orders = List.of(
            TravelOrder.builder().userName("张三").orderType("HOTEL")
                .itemId(1L).itemName("北京王府井希尔顿酒店").quantity(2)
                .totalPrice(new BigDecimal("1776.00"))
                .orderDate(LocalDate.now().minusDays(1))
                .status("CONFIRMED").build(),
            TravelOrder.builder().userName("张三").orderType("ROUTE")
                .itemId(1L).itemName("北京经典三日游").quantity(1)
                .totalPrice(new BigDecimal("1599.00"))
                .orderDate(LocalDate.now().minusDays(3))
                .status("CONFIRMED").build(),
            TravelOrder.builder().userName("李四").orderType("HOTEL")
                .itemId(6L).itemName("南京汉庭酒店(夫子庙店)").quantity(1)
                .totalPrice(new BigDecimal("159.00"))
                .orderDate(LocalDate.now().minusDays(2))
                .status("PENDING").build()
        );
        travelOrderRepository.saveAll(orders);
    }

    /** 创建默认会话（如果不存在） */
    private void initDefaultSession() {
        if (chatSessionRepository.count() == 0) {
            chatSessionRepository.save(
                com.travel.mcp.entity.ChatSession.builder()
                    .userId(0L)  // 系统默认会话，不属于任何真实用户
                    .sessionTitle("默认对话")
                    .build()
            );
        }
    }
}
