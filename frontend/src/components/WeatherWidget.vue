<template>
  <div class="widget-panel">
    <!-- 面板头部 -->
    <div class="widget-header">
      <h3>旅行信息</h3>
      <span class="widget-badge" v-if="store.currentCity">实时</span>
    </div>

    <!-- ========== 未询问地点时的占位提示 ========== -->
    <div v-if="!store.currentCity" class="placeholder-area">
      <div class="placeholder-illustration">
        <svg width="80" height="80" viewBox="0 0 80 80" fill="none">
          <circle cx="40" cy="40" r="40" fill="url(#phBg)"/>
          <circle cx="40" cy="32" r="8" stroke="#fff" stroke-width="2.5" fill="none"/>
          <path d="M24 60c0-10 7-18 16-18s16 8 16 18" stroke="#fff" stroke-width="2.5" stroke-linecap="round" fill="none"/>
          <circle cx="30" cy="54" r="2" fill="#fff"/>
          <defs>
            <linearGradient id="phBg" x1="0" y1="0" x2="80" y2="80">
              <stop stop-color="#C5D5D0"/><stop offset="1" stop-color="#A8BFB8"/>
            </linearGradient>
          </defs>
        </svg>
      </div>
      <p class="placeholder-title">🌍 你还未询问地点</p>
      <p class="placeholder-hint">在对话中输入城市名称<br>我将为你展示该城市的旅行信息</p>
      <div class="placeholder-examples">
        <span class="example-chip" v-for="ex in exampleCities" :key="ex" @click="quickAskCity(ex)">{{ ex }}</span>
      </div>
    </div>

    <!-- ========== 已检测到城市：展示旅行信息 ========== -->
    <template v-else>
      <!-- 加载中 -->
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>正在获取 {{ store.currentCity }} 的实时数据...</p>
      </div>

      <!-- 天气主体卡片 -->
      <template v-else-if="weatherData">
        <div class="weather-hero glass-card">
          <div class="hero-bg">
            <svg width="100%" height="100%" viewBox="0 0 280 160" preserveAspectRatio="none">
              <defs>
                <linearGradient id="sky" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stop-color="#87CEEB"/><stop offset="100%" stop-color="#E8F4F8"/>
                </linearGradient>
              </defs>
              <rect width="280" height="160" fill="url(#sky)"/>
              <circle cx="200" cy="50" r="30" fill="#FFD93D" opacity="0.8"/>
              <ellipse cx="60" cy="100" rx="50" ry="16" fill="#fff" opacity="0.5"/>
              <ellipse cx="160" cy="120" rx="60" ry="14" fill="#fff" opacity="0.4"/>
              <ellipse cx="240" cy="95" rx="35" ry="10" fill="#fff" opacity="0.3"/>
            </svg>
          </div>
          <div class="hero-content">
            <div class="hero-city">{{ store.currentCity }}</div>
            <div class="hero-temp">{{ weatherData.temp }}</div>
            <div class="hero-desc">{{ weatherData.desc }}</div>
          </div>
        </div>

        <!-- 环境指标卡片行 -->
        <div class="metrics-row">
          <div class="metric-card">
            <div class="metric-icon temp-icon">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <path d="M14 14.76V3.5a2.5 2.5 0 00-5 0v11.26a4.5 4.5 0 105 0z"/>
              </svg>
            </div>
            <div class="metric-info">
              <span class="metric-label">温度</span>
              <span class="metric-value">{{ weatherData.temp }}</span>
            </div>
          </div>
          <div class="metric-card">
            <div class="metric-icon humidity-icon">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <path d="M12 2.69l5.66 5.66a8 8 0 11-11.31 0z"/>
              </svg>
            </div>
            <div class="metric-info">
              <span class="metric-label">湿度</span>
              <span class="metric-value">{{ weatherData.humidity }}</span>
            </div>
          </div>
        </div>

        <!-- 风力卡片 -->
        <div class="metric-card wide">
          <div class="metric-icon wind-icon">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
              <path d="M9.59 4.59A2 2 0 1111 8H2m10.59 11.41A2 2 0 1014 16H2m15.73-8.27A2.5 2.5 0 1119.5 12H2"/>
            </svg>
          </div>
          <div class="metric-info">
            <span class="metric-label">风力</span>
            <span class="metric-value">{{ weatherData.wind }}</span>
          </div>
        </div>

        <!-- 旅行贴士 -->
        <div class="travel-tip glass-card">
          <div class="tip-header">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="var(--brand)" stroke-width="2" stroke-linecap="round">
              <circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/>
              <line x1="12" y1="8" x2="12.01" y2="8"/>
            </svg>
            <span>旅行贴士</span>
          </div>
          <p>{{ weatherData.tip }}</p>
        </div>

        <!-- 热门景点标签 -->
        <div class="hot-spots" v-if="weatherData.spots && weatherData.spots.length > 0">
          <h4>🔥 热门景点</h4>
          <div class="spot-tags">
            <span class="spot-tag" v-for="spot in weatherData.spots" :key="spot.name">
              {{ spot.name }} <small>⭐{{ spot.rating }}</small>
            </span>
          </div>
        </div>
      </template>

      <!-- 加载失败 -->
      <div v-else class="error-state">
        <p>😔 暂时无法获取 {{ store.currentCity }} 的天气数据</p>
        <button class="retry-btn" @click="fetchWeather">重试</button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useChatStore } from '../stores/chat'

const store = useChatStore()
const loading = ref(false)
const weatherData = ref<any>(null)

const exampleCities = ['北京', '上海', '杭州', '成都', '西安', '三亚', '昆明', '拉萨', '东京', '曼谷', '巴黎']

watch(() => store.currentCity, (city) => {
  if (city) fetchWeather()
})

onMounted(() => {
  if (store.currentCity) fetchWeather()
})

async function fetchWeather() {
  const city = store.currentCity
  if (!city) return
  loading.value = true
  weatherData.value = null
  try {
    const res = await fetch(`/api/weather/${encodeURIComponent(city)}`)
    const data = await res.json()
    if (data.success) {
      weatherData.value = data
    } else {
      weatherData.value = null
    }
  } catch {
    weatherData.value = null
  } finally {
    loading.value = false
  }
}

function quickAskCity(city: string) {
  store.updateCity(city)
}
</script>

<style scoped>
.widget-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
  overflow-y: auto;
  padding-right: 4px;
}

/* ── 头部 ── */
.widget-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 4px;
}

.widget-header h3 {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
}

.widget-badge {
  font-size: 10px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: var(--radius-pill);
  background: var(--brand-light);
  color: var(--brand);
}

/* ── 占位区 ── */
.placeholder-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 32px 16px;
  gap: 12px;
}

.placeholder-illustration { margin-bottom: 8px; opacity: 0.85; }
.placeholder-title { font-size: 17px; font-weight: 700; color: var(--text-primary); }
.placeholder-hint { font-size: 13px; color: var(--text-secondary); line-height: 1.8; margin: 0; }

.placeholder-examples {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
  margin-top: 12px;
}

.example-chip {
  padding: 6px 16px;
  font-size: 13px;
  font-weight: 500;
  background: var(--brand-light);
  border: 1px solid var(--brand);
  border-radius: var(--radius-pill);
  color: var(--brand);
  cursor: pointer;
  transition: all var(--transition);
}

.example-chip:hover { background: var(--brand); color: #fff; transform: translateY(-1px); }

/* ── 加载 ── */
.loading-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--text-secondary);
  font-size: 13px;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--border-subtle);
  border-top-color: var(--brand);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

.error-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--text-secondary);
  font-size: 13px;
}

.retry-btn {
  padding: 6px 16px;
  font-size: 12px;
  border: 1px solid var(--brand);
  border-radius: var(--radius-pill);
  background: var(--brand-light);
  color: var(--brand);
  cursor: pointer;
}

.retry-btn:hover { background: var(--brand); color: #fff; }

/* ── 毛玻璃卡片 ── */
.glass-card {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
  border: 1px solid rgba(255,255,255,0.6);
  overflow: hidden;
}

.weather-hero { position: relative; height: 140px; }
.hero-bg { position: absolute; inset: 0; }
.hero-bg svg { width: 100%; height: 100%; }
.hero-content { position: relative; z-index: 1; padding: 20px; color: #2C3E50; }
.hero-city { font-size: 13px; opacity: 0.8; margin-bottom: 4px; }
.hero-temp { font-size: 36px; font-weight: 700; line-height: 1.1; }
.hero-desc { font-size: 12px; opacity: 0.7; margin-top: 4px; }

.metrics-row { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }

.metric-card {
  background: var(--card-bg);
  border-radius: var(--radius-card);
  padding: 16px;
  box-shadow: var(--shadow-card);
  display: flex;
  align-items: center;
  gap: 12px;
  transition: all var(--transition);
  cursor: default;
}

.metric-card:hover { transform: translateY(-2px); box-shadow: var(--shadow-hover); }
.metric-card.wide { grid-column: 1 / -1; }

.metric-icon {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.temp-icon { background: #FFF0E5; color: #E89B5E; }
.humidity-icon { background: #E5F0FF; color: #5B9BD5; }
.wind-icon { background: #E8F5EF; color: var(--brand); }

.metric-info { display: flex; flex-direction: column; }
.metric-label { font-size: 11px; color: var(--text-secondary); font-weight: 500; }
.metric-value { font-size: 16px; font-weight: 700; color: var(--text-primary); }

.travel-tip { padding: 18px; }
.tip-header { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; }
.tip-header span { font-size: 14px; font-weight: 600; color: var(--text-primary); }
.travel-tip p { font-size: 13px; color: var(--text-secondary); line-height: 1.7; margin: 0; }

.hot-spots { padding: 4px 4px 0; }
.hot-spots h4 { font-size: 13px; font-weight: 600; color: var(--text-primary); margin-bottom: 10px; }

.spot-tags { display: flex; flex-wrap: wrap; gap: 8px; }
.spot-tag {
  padding: 6px 14px;
  font-size: 12px;
  background: var(--card-bg);
  border-radius: var(--radius-pill);
  color: var(--text-primary);
  box-shadow: var(--shadow-card);
  transition: all var(--transition);
  cursor: pointer;
}

.spot-tag:hover { transform: translateY(-2px); box-shadow: var(--shadow-hover); background: var(--brand); color: #fff; }
.spot-tag small { opacity: 0.6; margin-left: 2px; }
</style>
