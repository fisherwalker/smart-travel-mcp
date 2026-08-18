<template>
  <div class="chat-view">
    <!-- 顶部状态栏 -->
    <header class="chat-header">
      <div class="header-left">
        <h2>{{ store.currentTitle || '智慧旅游 AI 助手' }}</h2>
        <span class="status-dot" :class="{ online: store.keyConfigured && store.loggedIn, offline: !store.keyConfigured || !store.loggedIn }"></span>
      </div>
      <div class="header-right">
        <span class="username-tag" v-if="store.loggedIn">{{ store.username }}</span>
        <span class="status-label" v-if="store.keyConfigured && store.loggedIn">在线</span>
        <span class="status-label warn" v-else>未登录</span>
      </div>
    </header>

    <!-- 消息列表 -->
    <ChatMessages
      :messages="store.messages"
      :streaming="store.streaming"
      :streamContent="store.streamContent"
    />

    <!-- 输入区 -->
    <ChatInput
      :disabled="store.streaming || !store.loggedIn"
      @send="handleSend"
    />

    <!-- 认证弹窗（登录/注册 + API Key） -->
    <Teleport to="body">
      <div class="modal-overlay" v-if="showAuthDialog" @click.self="() => {}">
        <div class="auth-modal">
          <div class="auth-modal-icon">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="var(--brand)" stroke-width="1.5" stroke-linecap="round">
              <path d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 11-7.778 7.778 5.5 5.5 0 017.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4"/>
            </svg>
          </div>
          <h3>{{ isRegisterMode ? '注册新账号' : '欢迎回来' }}</h3>
          <p class="auth-modal-desc">
            {{ isRegisterMode ? '创建账号后即可使用智慧旅游 AI 助手' : '登录以继续使用智慧旅游 AI 助手' }}<br>
            <a href="https://platform.deepseek.com/api_keys" target="_blank">🔗 前往 DeepSeek 获取你的 API Key →</a>
          </p>

          <!-- 用户名 -->
          <div class="auth-field">
            <label>用户名</label>
            <input
              v-model="authForm.username"
              type="text"
              placeholder="请输入用户名"
              class="auth-input"
              @keyup.enter="handleAuth"
            />
          </div>

          <!-- 密码 -->
          <div class="auth-field">
            <label>密码</label>
            <input
              v-model="authForm.password"
              type="password"
              placeholder="请输入密码"
              class="auth-input"
              @keyup.enter="handleAuth"
            />
          </div>

          <!-- API Key -->
          <div class="auth-field">
            <label>DeepSeek API Key</label>
            <input
              v-model="authForm.apiKey"
              type="password"
              placeholder="sk-xxxxxxxxxxxxxxxxxxxxxxxx"
              class="auth-input"
              @keyup.enter="handleAuth"
            />
          </div>

          <!-- 操作按钮 -->
          <button class="auth-btn primary" @click="handleAuth" :disabled="authLoading">
            <span class="loading-dots" v-if="authLoading"><i></i><i></i><i></i></span>
            <span v-else>{{ isRegisterMode ? '注册并开始使用' : '登录' }}</span>
          </button>

          <!-- 切换模式 -->
          <p class="auth-switch">
            {{ isRegisterMode ? '已有账号？' : '还没有账号？' }}
            <a href="javascript:void(0)" @click="isRegisterMode = !isRegisterMode">
              {{ isRegisterMode ? '去登录' : '去注册' }}
            </a>
          </p>

          <!-- 错误提示 -->
          <p class="auth-error" v-if="authError">{{ authError }}</p>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import ChatMessages from '../components/ChatMessages.vue'
import ChatInput from '../components/ChatInput.vue'
import { useChatStore } from '../stores/chat'

const store = useChatStore()
const showAuthDialog = ref(false)
const isRegisterMode = ref(false)
const authLoading = ref(false)
const authError = ref('')

const authForm = reactive({
  username: '',
  password: '',
  apiKey: ''
})

// 页面关闭时自动登出（清除 API Key）
// 使用 sendBeacon 确保请求在页面卸载时也能发送
function handleBeforeUnload() {
  if (store.loggedIn) {
    navigator.sendBeacon('/api/auth/logout')
  }
}

onMounted(() => {
  // 始终监听页面关闭事件，确保 API Key 被清除
  window.addEventListener('beforeunload', handleBeforeUnload)
  store.init().then(() => {
    if (!store.loggedIn) {
      showAuthDialog.value = true
    }
  })
})

onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload)
})

// 当登录状态变化时，显示/隐藏对话框
watch(() => store.loggedIn, (val) => {
  if (!val) {
    showAuthDialog.value = true
    authError.value = ''
  } else {
    showAuthDialog.value = false
  }
})

function handleSend(text: string) {
  if (!store.loggedIn) {
    showAuthDialog.value = true
    return
  }
  if (!store.keyConfigured) {
    ElMessage.warning('请先登录并配置 API Key')
    showAuthDialog.value = true
    return
  }
  store.sendMessage(text)
}

async function handleAuth() {
  authError.value = ''

  if (!authForm.username.trim() || authForm.username.trim().length < 2) {
    authError.value = '请输入有效的用户名（至少 2 个字符）'
    return
  }
  if (!authForm.password || authForm.password.length < 4) {
    authError.value = '密码至少需要 4 个字符'
    return
  }
  if (!authForm.apiKey.trim()) {
    authError.value = '请输入你的 DeepSeek API Key'
    return
  }

  authLoading.value = true
  try {
    let result
    if (isRegisterMode.value) {
      result = await store.doRegister(authForm.username.trim(), authForm.password, authForm.apiKey.trim())
    } else {
      result = await store.doLogin(authForm.username.trim(), authForm.password, authForm.apiKey.trim())
    }

    if (result.success) {
      ElMessage.success(result.message || '登录成功！')
      // 清除表单
      authForm.password = ''
      authForm.apiKey = ''
    } else {
      authError.value = result.message || '操作失败，请重试'
    }
  } catch {
    authError.value = '网络错误，请检查网络连接后重试'
  } finally {
    authLoading.value = false
  }
}
</script>

<style scoped>
.chat-view {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--card-bg);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
  overflow: hidden;
}

/* ── 头部 ── */
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 24px;
  border-bottom: 1px solid var(--border-subtle);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.chat-header h2 {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
}

.username-tag {
  font-size: 12px;
  font-weight: 500;
  padding: 3px 10px;
  background: var(--brand-light);
  border-radius: var(--radius-pill);
  color: var(--brand);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-dot.online {
  background: #67c23a;
  box-shadow: 0 0 8px rgba(103, 194, 58, 0.4);
}

.status-dot.offline {
  background: #e6a23c;
  box-shadow: 0 0 8px rgba(230, 162, 60, 0.4);
}

.status-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.status-label.warn {
  color: #e6a23c;
}

/* ── 模态弹窗 ── */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.auth-modal {
  width: 440px;
  background: #fff;
  border-radius: var(--radius-card);
  padding: 32px;
  box-shadow: var(--shadow-hover);
  text-align: center;
}

.auth-modal-icon { margin-bottom: 12px; }

.auth-modal h3 {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.auth-modal-desc {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.8;
  margin-bottom: 20px;
}

.auth-modal-desc a {
  color: var(--brand);
  text-decoration: none;
  font-weight: 500;
}

.auth-modal-desc a:hover { text-decoration: underline; }

/* ── 表单字段 ── */
.auth-field {
  margin-bottom: 14px;
  text-align: left;
}

.auth-field label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.auth-input {
  width: 100%;
  padding: 10px 14px;
  font-size: 14px;
  border: 2px solid var(--border-subtle);
  border-radius: 10px;
  outline: none;
  transition: border-color var(--transition);
  background: var(--bg-primary);
}

.auth-input:focus {
  border-color: var(--brand);
}

/* ── 按钮 ── */
.auth-btn {
  width: 100%;
  height: 44px;
  border: none;
  border-radius: var(--radius-pill);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 6px;
}

.auth-btn.primary {
  background: var(--brand);
  color: #fff;
  box-shadow: 0 4px 14px rgba(107, 158, 147, 0.3);
}

.auth-btn.primary:hover:not(:disabled) {
  background: var(--brand-dark);
  transform: translateY(-1px);
}

.auth-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.auth-switch {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 16px;
}

.auth-switch a {
  color: var(--brand);
  font-weight: 500;
  text-decoration: none;
}

.auth-switch a:hover { text-decoration: underline; }

.auth-error {
  margin-top: 12px;
  padding: 10px;
  font-size: 13px;
  color: #e74c3c;
  background: #fdf0ef;
  border-radius: 10px;
}

/* ── 加载动画 ── */
.loading-dots {
  display: flex;
  gap: 6px;
  align-items: center;
}

.loading-dots i {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #fff;
  animation: wave 1.2s ease-in-out infinite;
}

.loading-dots i:nth-child(1) { animation-delay: 0s; }
.loading-dots i:nth-child(2) { animation-delay: 0.2s; }
.loading-dots i:nth-child(3) { animation-delay: 0.4s; }

@keyframes wave {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.5; }
  30% { transform: translateY(-8px); opacity: 1; }
}
</style>
