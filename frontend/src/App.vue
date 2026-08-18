<template>
  <div class="app-shell">
    <!-- 左侧导航栏 260px -->
    <aside class="app-sidebar">
      <Sidebar />
    </aside>

    <!-- 中部核心区 max-width 800px -->
    <main class="app-main">
      <router-view />
    </main>

    <!-- 右侧信息栏 320px -->
    <aside class="app-widget">
      <WeatherWidget />
    </aside>
  </div>
</template>

<script setup lang="ts">
import Sidebar from './components/Sidebar.vue'
import WeatherWidget from './components/WeatherWidget.vue'
</script>

<style>
/* ================================================================
   Global Reset & CSS Variables — 沉浸式度假风格色彩系统
   ================================================================ */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

:root {
  /* 主色调 */
  --bg-primary:    #F7F4ED;   /* 全局背景 (暖米色/沙纹感) */
  --brand:         #6B9E93;   /* 主品牌色 青瓷绿 */
  --accent:        #E5C287;   /* 辅助强调色 沙滩金 */
  --card-bg:       #FFFFFF;   /* 卡片背景 */
  --text-primary:  #2C3E50;   /* 主要文字 */
  --text-secondary:#8FA1B3;   /* 次要文字 */
  --border-subtle: #E8E3D8;   /* 柔和边框 */

  /* 语义衍生 */
  --brand-light:   #E8F3F1;   /* 品牌色浅底 */
  --brand-dark:    #568A80;   /* 品牌色深色 (hover) */
  --accent-light:  #FBF4E5;   /* 强调色浅底 */
  --shadow-card:   0 4px 20px rgba(0, 0, 0, 0.03), 0 2px 8px rgba(0, 0, 0, 0.02);
  --shadow-hover:  0 8px 30px rgba(0, 0, 0, 0.06), 0 4px 12px rgba(0, 0, 0, 0.03);
  --radius-card:   16px;
  --radius-pill:   999px;
  --spacing-sm:    8px;
  --spacing-md:    16px;
  --spacing-lg:    24px;
  --spacing-xl:    32px;
  --transition:    0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

body {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC",
    "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
  background: var(--bg-primary);
  color: var(--text-primary);
  height: 100vh;
  overflow: hidden;
  /* 叠加轻微噪点纹理 */
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)' opacity='0.03'/%3E%3C/svg%3E");
}

/* 滚动条美化 */
::-webkit-scrollbar { width: 5px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb { background: #d5cfc4; border-radius: 999px; }
::-webkit-scrollbar-thumb:hover { background: #bfb8ab; }

/* ================================================================
   三栏布局骨架
   ================================================================ */
.app-shell {
  display: flex;
  height: 100vh;
  overflow: hidden;
  max-width: 1440px;
  margin: 0 auto;
  padding: var(--spacing-lg);
  gap: var(--spacing-lg);
}

.app-sidebar {
  width: 260px;
  flex-shrink: 0;
  overflow-y: auto;
  overflow-x: hidden;
}

.app-main {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  min-width: 0;
}

.app-widget {
  width: 320px;
  flex-shrink: 0;
  overflow-y: auto;
  overflow-x: hidden;
}
</style>
