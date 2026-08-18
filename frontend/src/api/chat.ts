import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
  withCredentials: true
})

// ==================== 用户认证 ====================

/** 用户注册 */
export function register(username: string, password: string, apiKey: string) {
  return api.post('/auth/register', { username, password, apiKey })
}

/** 用户登录 */
export function login(username: string, password: string, apiKey: string) {
  return api.post('/auth/login', { username, password, apiKey })
}

/** 用户登出 */
export function logout() {
  return api.post('/auth/logout')
}

/** 检查登录状态 */
export function checkAuthStatus() {
  return api.get('/auth/status')
}

// ==================== API Key ====================

/** 设置 DeepSeek API Key */
export function setApiKey(apiKey: string) {
  return api.post('/config/deepseek-key', { apiKey })
}

/** 检查是否已配置 Key */
export function checkApiKey() {
  return api.get('/config/deepseek-key')
}

/** 获取所有会话 */
export function getSessions() {
  return api.get('/sessions')
}

/** 创建新会话 */
export function createSession() {
  return api.post('/sessions')
}

/** 删除会话 */
export function deleteSession(id: number) {
  return api.delete(`/sessions/${id}`)
}

/** 重命名会话 */
export function renameSession(id: number, title: string) {
  return api.put(`/sessions/${id}`, { title })
}

/** 获取会话消息 */
export function getMessages(sessionId: number) {
  return api.get(`/sessions/${sessionId}/messages`)
}

export default api
