<template>
  <div class="settings-view">
    <div class="settings-card">
      <!-- 返回按钮 -->
      <button class="back-btn" @click="router.push('/')">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
          <polyline points="15 18 9 12 15 6"/>
        </svg>
        返回对话
      </button>

      <div class="settings-icon">
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="var(--brand)" stroke-width="1.5" stroke-linecap="round">
          <path d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 11-7.778 7.778 5.5 5.5 0 017.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4"/>
        </svg>
      </div>
      <h2>配置 DeepSeek API Key</h2>
      <p class="settings-desc">
        请输入你的 DeepSeek API Key 以启用 AI 对话功能。<br>
        <a href="https://platform.deepseek.com/api_keys" target="_blank">前往 DeepSeek 平台获取 API Key →</a>
      </p>

      <div class="key-input-wrap">
        <input
          v-model="apiKey"
          type="password"
          placeholder="sk-xxxxxxxxxxxxxxxxxxxxxxxx"
          class="key-input"
          @keyup.enter="saveKey"
        />
      </div>

      <button class="save-btn" :disabled="saving" @click="saveKey">
        <span class="loading-dots" v-if="saving"><i></i><i></i><i></i></span>
        <span v-else>保存并返回</span>
      </button>

      <div v-if="statusMsg" class="status-msg" :class="{ success: statusSuccess, error: !statusSuccess }">
        {{ statusMsg }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { setApiKey, checkApiKey } from '../api/chat'

const router = useRouter()
const apiKey = ref('')
const saving = ref(false)
const statusMsg = ref('')
const statusSuccess = ref(false)

checkApiKey().then(res => {
  if (res.data.configured) {
    statusMsg.value = '已配置 API Key，可直接使用。输入新 Key 将覆盖。'
    statusSuccess.value = true
  }
})

async function saveKey() {
  if (!apiKey.value.trim()) {
    statusMsg.value = '请输入有效的 API Key'
    statusSuccess.value = false
    return
  }
  saving.value = true
  try {
    const res = await setApiKey(apiKey.value.trim())
    if (res.data.success) {
      statusMsg.value = 'API Key 配置成功！'
      statusSuccess.value = true
      setTimeout(() => router.push('/'), 800)
    } else {
      statusMsg.value = res.data.message || '配置失败'
      statusSuccess.value = false
    }
  } catch {
    statusMsg.value = '网络错误，请稍后重试'
    statusSuccess.value = false
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.settings-view {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  padding: 20px;
}

.settings-card {
  width: 100%;
  max-width: 460px;
  background: var(--card-bg);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
  padding: 36px 32px;
  text-align: center;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 6px 12px;
  border-radius: var(--radius-pill);
  transition: all var(--transition);
  margin-bottom: 20px;
}

.back-btn:hover {
  background: var(--bg-primary);
  color: var(--text-primary);
}

.settings-icon { margin-bottom: 16px; }

.settings-card h2 {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.settings-desc {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.8;
  margin-bottom: 24px;
}

.settings-desc a {
  color: var(--brand);
  text-decoration: none;
  font-weight: 500;
}

.settings-desc a:hover { text-decoration: underline; }

.key-input-wrap { margin-bottom: 16px; }

.key-input {
  width: 100%;
  padding: 12px 16px;
  font-size: 14px;
  border: 2px solid var(--border-subtle);
  border-radius: var(--radius-pill);
  outline: none;
  transition: border-color var(--transition);
  background: var(--bg-primary);
}

.key-input:focus { border-color: var(--brand); }

.save-btn {
  width: 100%;
  height: 44px;
  background: var(--brand);
  color: #fff;
  border: none;
  border-radius: var(--radius-pill);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition);
  display: flex;
  align-items: center;
  justify-content: center;
}

.save-btn:hover:not(:disabled) {
  background: var(--brand-dark);
}

.save-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.status-msg {
  margin-top: 16px;
  padding: 10px 16px;
  border-radius: 10px;
  font-size: 13px;
}

.status-msg.success {
  background: var(--brand-light);
  color: var(--brand);
}

.status-msg.error {
  background: #FEF0F0;
  color: #e74c3c;
}

/* ── 加载波浪点 ── */
.loading-dots { display: flex; gap: 6px; align-items: center; }
.loading-dots i {
  width: 7px; height: 7px; border-radius: 50%; background: #fff;
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
