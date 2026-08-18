import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getSessions, createSession, deleteSession, renameSession, getMessages, setApiKey, checkApiKey, checkAuthStatus, register, login, logout } from '../api/chat'
import { streamChat } from '../utils/sse'
import { ElMessage } from 'element-plus'

/** 简单城市名检测 — 从用户消息中提取城市名 */
const KNOWN_CITIES = ['北京', '上海', '杭州', '成都', '南京', '深圳', '广州', '武汉', '西安', '重庆', '苏州', '厦门', '青岛', '大连', '昆明', '三亚', '哈尔滨', '长沙', '郑州', '天津', '大理', '桂林', '东京', '曼谷', '巴黎']

function detectCity(text: string): string | null {
  for (const city of KNOWN_CITIES) {
    if (text.includes(city)) return city
  }
  return null
}

export const useChatStore = defineStore('chat', () => {
  const sessions = ref<any[]>([])
  const currentSessionId = ref<number | null>(null)
  const messages = ref<any[]>([])
  const streaming = ref(false)
  const streamContent = ref('')
  const keyConfigured = ref(true)
  const loggedIn = ref(false)
  const username = ref('')
  const currentCity = ref<string | null>(null)

  const currentTitle = computed(() => {
    const s = sessions.value.find((s: any) => s.id === currentSessionId.value)
    return s?.sessionTitle || ''
  })

  async function init() {
    // 检查登录状态
    try {
      const status = await checkAuthStatus()
      if (status.data.loggedIn) {
        loggedIn.value = true
        username.value = status.data.username || ''
        keyConfigured.value = status.data.hasApiKey || false
      } else {
        loggedIn.value = false
        keyConfigured.value = false
      }
    } catch {
      loggedIn.value = false
      keyConfigured.value = false
    }
    if (loggedIn.value) {
      await loadSessions()
      if (sessions.value.length > 0) {
        await switchSession(sessions.value[0].id)
      }
    }
  }

  // ==================== 认证 ====================

  async function doRegister(user: string, pass: string, key: string) {
    const res = await register(user, pass, key)
    if (res.data.success) {
      loggedIn.value = true
      username.value = res.data.username
      keyConfigured.value = true
      await init()
    }
    return res.data
  }

  async function doLogin(user: string, pass: string, key: string) {
    const res = await login(user, pass, key)
    if (res.data.success) {
      loggedIn.value = true
      username.value = res.data.username
      keyConfigured.value = true
      await init()
    }
    return res.data
  }

  async function doLogout() {
    try {
      await logout()
    } catch { /* ignore */ }
    loggedIn.value = false
    username.value = ''
    keyConfigured.value = false
    sessions.value = []
    messages.value = []
    currentSessionId.value = null
  }

  async function loadSessions() {
    try {
      const res = await getSessions()
      sessions.value = res.data
    } catch { /* ignore */ }
  }

  async function switchSession(id: number) {
    currentSessionId.value = id
    try {
      const res = await getMessages(id)
      messages.value = res.data
    } catch { messages.value = [] }
  }

  async function newSession() {
    try {
      const res = await createSession()
      sessions.value.unshift(res.data)
      await switchSession(res.data.id)
    } catch { /* ignore */ }
  }

  async function removeSession(id: number) {
    try {
      await deleteSession(id)
      sessions.value = sessions.value.filter((s: any) => s.id !== id)
      if (currentSessionId.value === id) {
        if (sessions.value.length > 0) {
          await switchSession(sessions.value[0].id)
        } else {
          messages.value = []
          currentSessionId.value = null
        }
      }
    } catch { /* ignore */ }
  }

  async function rename(id: number, title: string) {
    try {
      await renameSession(id, title)
      const session = sessions.value.find((s: any) => s.id === id)
      if (session) session.sessionTitle = title
    } catch { /* ignore */ }
  }

  /**
   * 发送消息 — 纯 SSE 流式，无轮询兜底
   *
   * 改动说明（按文档要求）：
   * - 使用原生 fetch API（utils/sse.ts 已实现）代替 axios 处理流式接口
   * - 乐观更新：网络请求前立即 push 用户消息，界面第一时间响应
   * - 实时拼接：在 while 循环中将文字碎块拼接到 streamContent
   * - 移除轮询兜底：SSE 已经可靠，polling 是多余的复杂性
   */
  function sendMessage(text: string) {
    // ── 检测并更新当前城市 ──
    const city = detectCity(text)
    if (city) currentCity.value = city

    // ── 乐观更新：立即添加用户消息 ──
    messages.value.push({ role: 'user', content: text, _localId: 'u' + Date.now() })

    // ── 添加 AI 占位气泡，streamContent 将实时注入其中 ──
    const streamingId = 's' + Date.now()
    messages.value.push({ role: 'streaming-assistant', content: '', _localId: streamingId })
    streaming.value = true
    streamContent.value = ''

    // ── 纯 SSE 流式请求 ──
    streamChat(currentSessionId.value, text, {
      onSession(sessionId) {
        currentSessionId.value = sessionId
      },
      onThinking(message) {
        // 思考过程实时显示在气泡中
        const idx = messages.value.findIndex((m: any) => m._localId === streamingId)
        if (idx >= 0 && messages.value[idx].role === 'streaming-assistant') {
          messages.value[idx] = { ...messages.value[idx], content: '🤔 ' + message }
        }
      },
      onChunk(content) {
        // 实时拼接：将解析出的文字碎块拼接到 streamContent
        streamContent.value += content
        // 同步更新消息气泡
        const idx = messages.value.findIndex((m: any) => m._localId === streamingId)
        if (idx >= 0 && messages.value[idx].role === 'streaming-assistant') {
          const current = messages.value[idx].content
          const newContent = current.startsWith('🤔') ? content : current + content
          messages.value[idx] = { ...messages.value[idx], content: newContent }
        }
      },
      onDone(fullContent) {
        const idx = messages.value.findIndex((m: any) => m._localId === streamingId)
        if (idx >= 0 && messages.value[idx].role === 'streaming-assistant') {
          messages.value[idx] = { role: 'assistant', content: fullContent, _localId: streamingId }
        }
        streamContent.value = ''
        streaming.value = false
        loadSessions()
      },
      onError(error) {
        const idx = messages.value.findIndex((m: any) => m._localId === streamingId)
        if (idx >= 0) messages.value.splice(idx, 1)
        ElMessage.error(error)
        streaming.value = false
      },
      onNeedApiKey() {
        const idx = messages.value.findIndex((m: any) => m._localId === streamingId)
        if (idx >= 0) messages.value.splice(idx, 1)
        keyConfigured.value = false
        streaming.value = false
      }
    })
  }

  async function configureApiKey(key: string) {
    const res = await setApiKey(key)
    if (res.data.success) keyConfigured.value = true
    return res.data
  }

  function updateCity(city: string | null) {
    currentCity.value = city
  }

  return {
    sessions, currentSessionId, messages, streaming, streamContent, keyConfigured, loggedIn, username, currentCity, currentTitle,
    init, loadSessions, switchSession, newSession, removeSession, rename,
    sendMessage, configureApiKey, updateCity,
    doRegister, doLogin, doLogout
  }
})
