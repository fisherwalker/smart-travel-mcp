<template>
  <div class="sidebar">
    <!-- 品牌 Logo 区 -->
    <div class="sidebar-brand">
      <div class="brand-icon">
        <svg width="36" height="36" viewBox="0 0 36 36" fill="none">
          <circle cx="18" cy="18" r="18" fill="url(#g1)"/>
          <path d="M12 22c0-4 2-8 6-8s6 4 6 8" stroke="#fff" stroke-width="2.2" stroke-linecap="round"/>
          <circle cx="12" cy="14" r="2.5" fill="#fff"/>
          <circle cx="24" cy="14" r="2.5" fill="#fff"/>
          <defs>
            <linearGradient id="g1" x1="0" y1="0" x2="36" y2="36">
              <stop stop-color="#6B9E93"/><stop offset="1" stop-color="#4A7D74"/>
            </linearGradient>
          </defs>
        </svg>
      </div>
      <div class="brand-text">
        <h1>智慧旅游</h1>
        <p>AI 旅行管家</p>
      </div>
    </div>

    <!-- 新建对话按钮 - 胶囊大按钮 -->
    <div class="sidebar-action">
      <button class="new-chat-btn" @click="store.newSession()">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
          <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
        </svg>
        新建对话
      </button>
    </div>

    <!-- 历史对话列表 -->
    <div class="sidebar-section">
      <h3 class="section-label">历史对话</h3>
      <div class="session-list" v-if="store.sessions.length > 0">
        <div
          v-for="s in store.sessions"
          :key="s.id"
          class="session-item"
          :class="{ active: s.id === store.currentSessionId }"
          @click="store.switchSession(s.id)"
          @dblclick="startRename(s)"
        >
          <svg class="session-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/>
          </svg>
          <span class="session-title" v-if="renamingId !== s.id">{{ s.sessionTitle }}</span>
          <input
            v-else
            v-model="renameTitle"
            class="rename-input"
            @blur="finishRename(s.id)"
            @keyup.enter="finishRename(s.id)"
            @click.stop
          />
          <button class="delete-btn" @click.stop="store.removeSession(s.id)" title="删除">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
              <polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"/>
            </svg>
          </button>
        </div>
      </div>
      <p v-else class="empty-text">暂无对话记录</p>
    </div>

    <!-- 快捷指令 -->
    <div class="sidebar-section">
      <h3 class="section-label">快捷指令</h3>
      <div class="quick-cmds">
        <button
          v-for="cmd in quickCommands"
          :key="cmd.label"
          class="quick-cmd-pill"
          @click="handleQuickSend(cmd.msg)"
        >
          {{ cmd.label }}
        </button>
      </div>
    </div>

    <!-- 底部设置 -->
    <div class="sidebar-footer">
      <button class="footer-link" @click="router.push('/settings')">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
          <circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 010 2.83 2 2 0 01-2.83 0l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-2 2 2 2 0 01-2-2v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83 0 2 2 0 010-2.83l.06-.06A1.65 1.65 0 004.68 15a1.65 1.65 0 00-1.51-1H3a2 2 0 01-2-2 2 2 0 012-2h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 010-2.83 2 2 0 012.83 0l.06.06A1.65 1.65 0 009 4.68a1.65 1.65 0 001-1.51V3a2 2 0 012-2 2 2 0 012 2v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 0 2 2 0 010 2.83l-.06.06A1.65 1.65 0 0019.4 9a1.65 1.65 0 001.51 1H21a2 2 0 012 2 2 2 0 01-2 2h-.09a1.65 1.65 0 00-1.51 1z"/>
        </svg>
        API Key 设置
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useChatStore } from '../stores/chat'
import { ElMessage } from 'element-plus'

const store = useChatStore()
const router = useRouter()

const renamingId = ref<number | null>(null)
const renameTitle = ref('')

/** 上下文感知的快捷指令 — 根据已检测到的城市动态替换目的地 */
const quickCommands = computed(() => {
  const city = store.currentCity || '北京'
  return [
    { label: '🏔️ 搜索景点', msg: `${city}有哪些自然风光的景点？` },
    { label: '🏨 搜索酒店', msg: `${city}300以内酒店有哪些？` },
    { label: '🗺️ 推荐路线', msg: `推荐一条从${city}出发的3日游路线` },
    { label: '🌤️ 查询天气', msg: `${city}明天天气怎么样？适合出去玩吗？` },
    { label: '📊 数据统计', msg: '帮我分析一下旅游数据' },
  ]
})

function startRename(s: any) {
  renamingId.value = s.id
  renameTitle.value = s.sessionTitle
}

function finishRename(id: number) {
  if (renameTitle.value.trim()) {
    store.rename(id, renameTitle.value.trim())
  }
  renamingId.value = null
}

function handleQuickSend(msg: string) {
  if (!store.keyConfigured) {
    ElMessage.warning('请先配置 API Key 再使用快捷指令')
    store.keyConfigured = false // 触发 ChatView 中的 key 配置弹窗
    return
  }
  store.sendMessage(msg)
}
</script>

<style scoped>
.sidebar {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--card-bg);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
  padding: 0;
  overflow: hidden;
}

/* ── 品牌区 ── */
.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px;
  border-bottom: 1px solid var(--border-subtle);
}

.brand-icon svg { display: block; }
.brand-text h1 { font-size: 16px; font-weight: 700; color: var(--text-primary); line-height: 1.3; }
.brand-text p { font-size: 11px; color: var(--text-secondary); margin-top: 2px; }

/* ── 新建对话 ── */
.sidebar-action {
  padding: 16px 20px;
}

.new-chat-btn {
  width: 100%;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: var(--brand);
  color: #fff;
  border: none;
  border-radius: var(--radius-pill);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition);
  box-shadow: 0 4px 14px rgba(107, 158, 147, 0.3);
}

.new-chat-btn:hover {
  background: var(--brand-dark);
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(107, 158, 147, 0.4);
}

/* ── 分区标签 ── */
.sidebar-section {
  padding: 0 20px 16px;
}

.section-label {
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1.5px;
  color: var(--text-secondary);
  margin-bottom: 10px;
}

/* ── 历史对话列表 ── */
.session-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
  max-height: 220px;
  overflow-y: auto;
}

.session-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 12px;
  border-radius: 10px;
  cursor: pointer;
  font-size: 13px;
  color: var(--text-primary);
  transition: all var(--transition);
  position: relative;
}

.session-item:hover {
  background: var(--brand-light);
  transform: translateY(-1px);
}

.session-item.active {
  background: var(--brand-light);
  color: var(--brand);
  font-weight: 600;
}

.session-icon {
  flex-shrink: 0;
  opacity: 0.4;
}

.session-item.active .session-icon {
  opacity: 1;
  color: var(--brand);
}

.session-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rename-input {
  flex: 1;
  padding: 2px 6px;
  font-size: 13px;
  border: 1px solid var(--brand);
  border-radius: 6px;
  outline: none;
  background: #fff;
}

.delete-btn {
  opacity: 0;
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  border-radius: 6px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  transition: all 0.2s;
}

.session-item:hover .delete-btn { opacity: 1; }
.delete-btn:hover { background: #fee; color: #e74c3c; }

.empty-text {
  font-size: 13px;
  color: var(--text-secondary);
  opacity: 0.6;
}

/* ── 快捷指令 ── */
.quick-cmds {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.quick-cmd-pill {
  width: 100%;
  padding: 9px 14px;
  font-size: 12px;
  text-align: left;
  background: var(--bg-primary);
  border: 1px solid transparent;
  border-radius: var(--radius-pill);
  color: var(--text-primary);
  cursor: pointer;
  transition: all var(--transition);
}

.quick-cmd-pill:hover {
  background: var(--brand-light);
  border-color: var(--brand);
  color: var(--brand);
  transform: translateY(-1px);
}

/* ── 底部 ── */
.sidebar-footer {
  margin-top: auto;
  padding: 16px 20px;
  border-top: 1px solid var(--border-subtle);
}

.footer-link {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px;
  font-size: 13px;
  background: none;
  border: none;
  border-radius: 10px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition);
}

.footer-link:hover {
  background: var(--bg-primary);
  color: var(--text-primary);
}
</style>
