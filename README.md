# 🏔️ 智慧旅游AI助手 (Smart Travel AI Assistant) — Java大作业

> 基于 **Spring AI MCP**（Model Context Protocol）的智能旅游管理系统 · Java 课程大作业
> An AI-powered Smart Travel Management System built on **Spring AI MCP** · Java Course Project

---

## 📖 项目简介 / About

**中文**：本项目将传统旅游管理系统升级为 **MCP 架构的 AI 助手**。用户只需通过自然语言，即可完成景点搜索、酒店查询、路线推荐、天气出行建议等操作，并支持 Web 聊天界面与 Claude Desktop 两种接入方式。数据采用用户隔离，支持注册 / 登录，安全可靠。

**English**: This project upgrades a traditional travel management system into an **MCP-based AI assistant**. By simply typing natural language, users can search scenic spots, query hotels, get route recommendations, and receive weather travel advice. It supports both a **Web chat interface** and **Claude Desktop**, with per-user data isolation and secure registration/login.

---

## ✨ 功能亮点 / Features

- 🗣️ **自然语言交互** — 无需复杂操作，像聊天一样获得出行建议
- 🔎 **智能检索** — 景点 / 酒店 / 路线 / 天气一站式查询
- 🧠 **AI Function Calling** — DeepSeek 大模型 + Spring AI MCP Server 自动调用工具
- ⚡ **SSE 流式输出** — 聊天回复实时流式呈现
- 🔐 **用户认证与隔离** — 注册 / 登录、Session 管理、数据按用户隔离
- 🖥️ **双模式接入** — Web (SSE) 与 Claude Desktop (STDIO)

---

## 🛠️ 技术栈 / Tech Stack

| 层级 Layer | 技术 Technology |
|---|---|
| 后端框架 Backend | Java 17 + Spring Boot 3.2.5 |
| AI 集成 AI Integration | Spring AI 1.0.0-M6（MCP Server） |
| 大模型 LLM | DeepSeek API（HTTP 客户端直连） |
| 数据库 Database | MySQL 8.0（主库）+ H2（STDIO 备用） |
| ORM | JPA / Hibernate |
| 认证 Auth | BCrypt 密码加密 + Session 管理 |
| 前端 Frontend | Vue 3.4 + TypeScript + Vite 5 + Element Plus 2.7 |
| 状态管理 State | Pinia |
| 路由 Router | Vue Router 4 |
| Markdown | marked + highlight.js |
| MCP 模式 Modes | SSE（Web）+ STDIO（Claude Desktop） |
| 天气 Weather | Open-Meteo 免费 API |

---

## 🚀 快速开始 / Quick Start

### 1. 数据库准备 / Prepare Database

```bash
# 创建数据库并导入种子数据
# Create the database and import seed data
mysql -u root -p < sql/init.sql
```

### 2. 修改配置文件 / Configure

编辑 `src/main/resources/application.yml`，修改数据库连接和 DeepSeek API Key。
Edit `src/main/resources/application.yml` to set your database connection and DeepSeek API Key.

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/smart_travel?useSSL=false&serverTimezone=Asia/Shanghai
    username: root      # 改成你的 / change to yours
    password: root      # 改成你的 / change to yours

deepseek:
  api-key: sk-xxxxxxxx  # 你的 DeepSeek API Key / your DeepSeek API Key
```

### 3. 构建前端（可选）/ Build Frontend (Optional)

前端已预构建到 `src/main/resources/static/`，可直接使用。如需修改前端：
The frontend is pre-built into `src/main/resources/static/` and ready to use. To modify it:

```bash
cd frontend
npm install
npm run build      # 输出到 ../src/main/resources/static/ / outputs to ../src/main/resources/static/
npm run dev        # 开发模式 / dev mode http://localhost:5173
```

### 4. 在 IDEA 中运行 / Run in IDEA

1. 用 IntelliJ IDEA 打开本项目 / Open the project with IntelliJ IDEA
2. 等待 Maven 自动下载依赖 / Wait for Maven to download dependencies
3. 运行 `SmartTravelMcpApplication` / Run `SmartTravelMcpApplication`
4. 浏览器访问 `http://localhost:8080` / Open `http://localhost:8080`

### 5. 命令行运行 / Run via Command Line

```bash
# Windows
start.bat

# 或手动 / or manually
mvnw.cmd spring-boot:run
```

### 6. Claude Desktop 连接（STDIO 模式）/ Connect Claude Desktop (STDIO)

先打包 / Build first:

```bash
mvnw.cmd package -DskipTests
```

在 `claude_desktop_config.json` 中添加 / Add to `claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "smart-travel": {
      "command": "java",
      "args": ["-Dspring.profiles.active=stdio", "-jar", "target/smart-travel-mcp-1.0.0.jar"]
    }
  }
}
```

---

## 🔐 用户认证 / Authentication

- **注册 Register**：提供用户名和密码即可注册 / register with username and password
- **登录 Login**：Session 管理，登录后可配置个人 DeepSeek API Key / session-based, configure your own API Key after login
- **API Key**：仅存储在 Session 中不持久化 / stored in-session only, not persisted
- **消息隔离 Isolation**：聊天会话和消息按用户隔离 / chat sessions and messages isolated per user

---

## 🧩 功能模块 / Function Modules

| 功能 Feature | MCP Tool | 示例对话 Example |
|---|---|---|
| 搜索景点 Search Spots | `searchSpots` | "杭州有哪些自然风光的景点？" |
| 搜索酒店 Search Hotels | `searchHotels` | "北京300以内的四星级酒店" |
| 推荐路线 Recommend Routes | `recommendRoutes` | "推荐一条从成都出发的3日游" |
| 数据统计 Stats | `getTravelStats` | "分析一下旅游数据" |
| 天气建议 Weather | `getWeatherAdvice` | "北京明天适合出去玩吗？" |

---

## 🗄️ 数据库表 / Database Tables

| 表名 Table | 说明 Description | 种子数据 Seed |
|---|---|---|
| `user` | 用户账户 User accounts | — |
| `scenic_spot` | 景点 Scenic spots | 12 条 / 12 (4 城市各 3 个) |
| `hotel` | 酒店 Hotels | 8 条 / 8 (4 城市各 2 个) |
| `travel_route` | 旅游路线 Travel routes | 6 条 / 6 |
| `travel_order` | 订单 Orders | 3 条 / 3 |
| `review` | 评价 Reviews | 10 条 / 10 |
| `weather_data` | 天气数据 Weather data | 5 城市 × 7 天 = 35 条 |
| `chat_session` | 聊天会话 Chat sessions | — |
| `chat_message` | 聊天消息 Chat messages | — |

---

## 📁 项目结构 / Project Structure

```
javawork/
├── pom.xml                           # Maven 构建配置 / Maven build config
├── README.md
├── start.bat                         # Windows 快速启动脚本 / quick start script
├── .gitignore                        # Git 忽略规则（target/、node_modules、敏感配置等）
├── sql/
│   ├── init.sql                      # 数据库建表 + 种子数据 / schema + seed
│   └── migrate_v2_add_user_id.sql    # v2 用户隔离迁移 / v2 user-isolation migration
├── .idea/                            # IntelliJ IDEA 项目配置（IDE 专用，不影响运行）
│   ├── .gitignore                    # 忽略 workspace.xml 等本地文件
│   ├── compiler.xml                  # 编译配置（Lombok 注解处理、Javac -parameters）
│   ├── encodings.xml                 # 源码文件编码（UTF-8）
│   ├── jarRepositories.xml           # Maven 远程仓库列表（central、spring-milestones 等）
│   ├── misc.xml                      # 项目根管理（JDK 17、pom.xml 入口）
│   └── workspace.xml                 # 个人工作区状态（默认忽略，不入库）
├── .mvn/                             # Maven Wrapper（无需预装 Maven 即可构建）
│   ├── maven.config                  # Maven JVM 参数（含可选代理配置示例）
│   └── wrapper/
│       ├── maven-wrapper.jar         # Wrapper 启动器
│       └── maven-wrapper.properties  # Maven 版本与下载地址（阿里云镜像）
├── .run/                             # IntelliJ IDEA 运行配置
│   └── SmartTravelMcpApplication.run.xml  # 一键启动 Spring Boot 主类（含代理参数）
├── frontend/                         # Vue 3 前端源码 / Vue 3 frontend source
│   ├── package.json
│   ├── vite.config.ts
│   └── src/
│       ├── main.ts
│       ├── App.vue
│       ├── api/chat.ts
│       ├── components/               # ChatInput, ChatMessages, Sidebar, WeatherWidget
│       ├── views/                    # ChatView, SettingsView
│       ├── router/index.ts
│       ├── stores/chat.ts            # Pinia 状态管理 / state
│       └── utils/sse.ts              # SSE 流式工具 / SSE streaming utils
└── src/main/
    ├── java/com/travel/mcp/
    │   ├── SmartTravelMcpApplication.java
    │   ├── config/
    │   │   ├── DataInitConfig.java       # 种子数据初始化 / seed init
    │   │   └── DeepSeekConfig.java       # DeepSeek 配置 / config
    │   ├── controller/
    │   │   ├── AuthController.java       # 登录 / 注册 API
    │   │   ├── ChatController.java       # 聊天 / SSE 端点
    │   │   └── PageController.java       # 页面路由 / page routes
    │   ├── dto/
    │   │   ├── ChatRequest.java
    │   │   ├── ChatResponse.java
    │   │   ├── DeepSeekMessage.java
    │   │   └── DeepSeekResponse.java
    │   ├── entity/                      # JPA 实体 / entities
    │   │   ├── User.java · ScenicSpot.java · Hotel.java · TravelRoute.java
    │   │   ├── TravelOrder.java · Review.java · WeatherData.java
    │   │   └── ChatSession.java · ChatMessage.java
    │   ├── repository/                  # Spring Data JPA 仓库 / repositories
    │   └── service/
    │       ├── TravelAssistantService.java  # MCP 工具（5 个 @Tool）
    │       ├── DeepSeekService.java         # DeepSeek API 客户端 / client
    │       └── QWeatherService.java         # 天气服务 / weather service
    └── resources/
        ├── application.yml              # 主配置（MySQL + SSE）/ main config
        ├── application-stdio.yml        # STDIO 配置（H2 内存数据库）
        └── static/                      # 前端构建产物 / frontend build output
```

---

## 🏗️ 架构说明 / Architecture

### MCP 工具调用流程 / MCP Tool Call Flow

```
用户输入 User Input → DeepSeek API（Function Calling）
         → Spring AI MCP Server
         → @Tool 方法执行（JPA 查询数据库 / 天气 API）
         → 结果返回 DeepSeek → 生成自然语言回复 → SSE 流式输出
```

### 双模式支持 / Dual Mode Support

| 模式 Mode | 配置 Config | 数据库 DB | 前端 Frontend |
|---|---|---|---|
| SSE（Web） | `application.yml` | MySQL | Vue 3 SPA |
| STDIO（Claude Desktop） | `application-stdio.yml` | H2 内存 | 无 / None |

---

## ⚠️ 注意事项 / Notes

1. **代理配置 Proxy**：如访问 DeepSeek API 需要代理，修改 `application.yml` 中的 `proxy` 配置 / set the `proxy` config if needed
2. **天气 API Weather**：使用 Open-Meteo 免费 API，无需 Key，每天 10000 次免费调用 / free, no key, 10,000 calls/day
3. **数据库密码 DB password**：请勿将真实密码提交到版本控制，建议使用环境变量 / don't commit real passwords, prefer env vars
4. **前端构建 Frontend build**：修改前端后需 `npm run build` 或使用 `npm run dev` / rebuild after changes

---

## 📄 许可证 / License

本项目仅用于学习与课程作业用途。 / This project is for educational and course-work purposes only.
