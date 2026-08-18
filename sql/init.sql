-- ============================================
-- 智慧旅游AI助手 - 数据库初始化脚本
-- 数据库: MySQL 8.0
-- 用法: mysql -u root -p < sql/init.sql
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS smart_travel
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE smart_travel;

-- ==================== 景点表 ====================
CREATE TABLE IF NOT EXISTS scenic_spot (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '景点名称',
    city VARCHAR(50) NOT NULL COMMENT '所在城市',
    category VARCHAR(30) COMMENT '类别：自然风光/人文古迹/主题乐园/美食购物',
    price DECIMAL(10,2) COMMENT '门票价格',
    rating DOUBLE COMMENT '评分1-5',
    description TEXT COMMENT '景点描述',
    image_url VARCHAR(255) COMMENT '图片URL',
    INDEX idx_scenic_city (city),
    INDEX idx_scenic_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='景点表';

-- ==================== 酒店表 ====================
CREATE TABLE IF NOT EXISTS hotel (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '酒店名称',
    city VARCHAR(50) NOT NULL COMMENT '所在城市',
    star INT COMMENT '星级1-5',
    price_per_night DECIMAL(10,2) COMMENT '每晚价格',
    available_rooms INT COMMENT '剩余房间数',
    address VARCHAR(200) COMMENT '地址',
    rating DOUBLE COMMENT '评分',
    INDEX idx_hotel_city (city),
    INDEX idx_hotel_star (star)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='酒店表';

-- ==================== 旅游路线表 ====================
CREATE TABLE IF NOT EXISTS travel_route (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '路线名称',
    start_city VARCHAR(50) NOT NULL COMMENT '出发城市',
    dest_cities VARCHAR(200) COMMENT '目的地城市(JSON)',
    days INT COMMENT '天数',
    price DECIMAL(10,2) COMMENT '价格',
    spots VARCHAR(500) COMMENT '包含景点(JSON)',
    description TEXT COMMENT '路线描述',
    INDEX idx_route_start_city (start_city),
    INDEX idx_route_days (days)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='旅游路线表';

-- ==================== 订单表 ====================
CREATE TABLE IF NOT EXISTS travel_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_name VARCHAR(50) NOT NULL COMMENT '用户名',
    order_type VARCHAR(20) COMMENT '类型：HOTEL/ROUTE',
    item_id BIGINT COMMENT '预订项ID',
    item_name VARCHAR(100) COMMENT '预订项名称',
    quantity INT COMMENT '数量',
    total_price DECIMAL(10,2) COMMENT '总价',
    order_date DATE COMMENT '下单日期',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态',
    INDEX idx_order_user (user_name),
    INDEX idx_order_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ==================== 评价表 ====================
CREATE TABLE IF NOT EXISTS review (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_name VARCHAR(50) NOT NULL COMMENT '用户名',
    target_type VARCHAR(20) COMMENT '评价对象类型：SPOT/HOTEL/ROUTE',
    target_id BIGINT COMMENT '对象ID',
    rating INT COMMENT '评分1-5',
    content TEXT COMMENT '评价内容',
    review_date DATE COMMENT '评价日期',
    INDEX idx_review_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- ==================== 用户表 ====================
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT 'BCrypt密码',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ==================== 聊天会话表（用户隔离） ====================
CREATE TABLE IF NOT EXISTS chat_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '所属用户ID',
    session_title VARCHAR(100) COMMENT '会话标题',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_session_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话表';

-- ==================== 聊天消息表（用户隔离） ====================
CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '所属用户ID',
    session_id BIGINT NOT NULL COMMENT '所属会话ID',
    role VARCHAR(20) COMMENT '角色：user/assistant/tool/system',
    content TEXT COMMENT '消息内容',
    tool_name VARCHAR(50) COMMENT '工具名称',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_msg_session (session_id),
    INDEX idx_msg_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- ==================== 天气数据表 ====================
CREATE TABLE IF NOT EXISTS weather_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    city VARCHAR(50) NOT NULL COMMENT '城市',
    weather_date DATE COMMENT '日期',
    temperature_high INT COMMENT '最高温℃',
    temperature_low INT COMMENT '最低温℃',
    weather_type VARCHAR(20) COMMENT '晴/多云/阴/雨/雪',
    humidity INT COMMENT '湿度%',
    wind_level VARCHAR(10) COMMENT '风力',
    travel_advice VARCHAR(255) COMMENT '出游建议',
    INDEX idx_weather_city_date (city, weather_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='天气数据表';
