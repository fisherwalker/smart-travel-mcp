<template>
  <div class="chat-messages" ref="messagesContainer">
    <!-- 欢迎消息 -->
    <div v-if="messages.length === 0 && !streaming" class="welcome-area">
      <div class="welcome-illustration">
        <svg width="80" height="80" viewBox="0 0 80 80" fill="none">
          <circle cx="40" cy="40" r="40" fill="url(#wbg)"/>
          <path d="M26 48c0-10 6-20 14-20s14 10 14 20" stroke="#fff" stroke-width="3" stroke-linecap="round"/>
          <circle cx="28" cy="32" r="4" fill="#fff"/>
          <circle cx="52" cy="32" r="4" fill="#fff"/>
          <defs>
            <linearGradient id="wbg" x1="0" y1="0" x2="80" y2="80">
              <stop stop-color="#6B9E93"/><stop offset="1" stop-color="#88B8A8"/>
            </linearGradient>
          </defs>
        </svg>
      </div>
      <h2>欢迎使用智慧旅游 AI 助手</h2>
      <p class="welcome-sub">我是你的专属旅行管家，可以帮你：</p>
      <div class="welcome-cards">
        <div class="welcome-card">
          <span class="wc-icon">🏔️</span>
          <strong>搜索景点</strong>
          <span>"杭州有哪些自然风光的景点？"</span>
        </div>
        <div class="welcome-card">
          <span class="wc-icon">🏨</span>
          <strong>查找酒店</strong>
          <span>"北京300以内的四星酒店"</span>
        </div>
        <div class="welcome-card">
          <span class="wc-icon">🗺️</span>
          <strong>推荐路线</strong>
          <span>"推荐成都3日游攻略"</span>
        </div>
        <div class="welcome-card">
          <span class="wc-icon">🌤️</span>
          <strong>查询天气</strong>
          <span>"北京明天适合出去玩吗？"</span>
        </div>
      </div>
    </div>

    <!-- 消息列表 -->
    <div v-for="msg in messages" :key="msg._localId || msg.id" class="message-wrapper">
      <!-- 用户消息 -->
      <div v-if="msg.role === 'user'" class="message user-message">
        <div class="message-bubble user-bubble">{{ msg.content }}</div>
      </div>

      <!-- AI 消息 -->
      <div v-else-if="msg.role === 'assistant' || msg.role === 'streaming-assistant'" class="message bot-message">
        <div class="message-bubble bot-bubble">
          <div v-html="renderMarkdown(msg.content)"></div>
          <!-- 流式打字光标 -->
          <span v-if="msg.role === 'streaming-assistant'" class="typing-cursor">|</span>
        </div>
      </div>

      <!-- 工具调用 -->
      <div v-else-if="msg.role === 'tool'" class="message tool-message">
        <span class="tool-tag">🔧 {{ msg.toolName }}</span>
      </div>
    </div>

    <!-- AI 思考中的三点波浪加载 -->
    <div v-if="streaming && messages.length > 0" class="thinking-indicator">
      <i></i><i></i><i></i>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/atom-one-dark.css'

const props = defineProps<{
  messages: any[]
  streaming: boolean
  streamContent: string
}>()

const messagesContainer = ref<HTMLElement>()

// ── Marked 配置 ──
const renderer = new marked.Renderer()

renderer.code = function ({ text, lang }: { text: string; lang?: string }) {
  const language = lang && hljs.getLanguage(lang) ? lang : 'plaintext'
  let highlighted: string
  try {
    highlighted = hljs.highlight(text, { language }).value
  } catch {
    highlighted = hljs.highlightAuto(text).value
  }
  const langLabel = lang ? `<span class="code-lang">${lang}</span>` : ''
  const copyBtn = `<button class="code-copy-btn" onclick="(function(btn){var p=btn.parentElement;var c=p.querySelector('code');navigator.clipboard.writeText(c.textContent||'').then(function(){btn.textContent='已复制';setTimeout(function(){btn.textContent='复制'},2000)})(this))">复制</button>`
  return `<div class="code-block-wrapper">${langLabel}${copyBtn}<pre><code class="hljs language-${language}">${highlighted}</code></pre></div>`
}

marked.setOptions({ renderer, breaks: true, gfm: true })

watch(
  () => [props.messages.length, props.streamContent, props.streaming],
  () => {
    nextTick(() => {
      if (messagesContainer.value) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
      }
    })
  }
)

function renderMarkdown(text: string): string {
  if (!text) return ''
  return marked.parse(text) as string
}
</script>

<style scoped>
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  scroll-behavior: smooth;
}

/* ── 欢迎区 ── */
.welcome-area {
  text-align: center;
  padding: 32px 16px;
}

.welcome-illustration { margin-bottom: 20px; }

.welcome-area h2 {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.welcome-sub {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 28px;
}

.welcome-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  max-width: 480px;
  margin: 0 auto;
}

.welcome-card {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  padding: 14px 16px;
  background: var(--bg-primary);
  border-radius: 14px;
  text-align: left;
  transition: all var(--transition);
  cursor: default;
}

.welcome-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-card);
}

.welcome-card .wc-icon { font-size: 22px; }
.welcome-card strong { font-size: 13px; color: var(--text-primary); }
.welcome-card span { font-size: 11px; color: var(--text-secondary); }

/* ── 消息容器 ── */
.message-wrapper { margin-bottom: 20px; }
.message { display: flex; max-width: 78%; }
.user-message { margin-left: auto; justify-content: flex-end; }
.bot-message { margin-right: auto; }
.tool-message { justify-content: center; margin: 6px auto; }

/* ── 气泡 ── */
.message-bubble {
  padding: 12px 18px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.8;
  word-break: break-word;
}

.user-bubble {
  background: var(--brand-light);
  color: var(--text-primary);
  border-bottom-right-radius: 6px;
}

.bot-bubble {
  background: var(--card-bg);
  box-shadow: var(--shadow-card);
  border-bottom-left-radius: 6px;
}

/* ── Markdown 内容 ── */
.bot-bubble :deep(h1),
.bot-bubble :deep(h2),
.bot-bubble :deep(h3) { margin: 8px 0 4px; font-size: 15px; color: var(--text-primary); }

.bot-bubble :deep(strong) { color: var(--brand); }

.bot-bubble :deep(blockquote) {
  padding: 4px 12px;
  margin: 8px 0;
  border-left: 3px solid var(--brand);
  background: var(--brand-light);
  color: var(--text-secondary);
  border-radius: 0 8px 8px 0;
}

.bot-bubble :deep(code) {
  background: var(--bg-primary);
  padding: 2px 6px;
  border-radius: 5px;
  font-size: 13px;
  color: var(--brand-dark);
}

.bot-bubble :deep(p) { margin: 4px 0; }

/* ── 工具调用标签 ── */
.tool-tag {
  font-size: 11px;
  padding: 3px 12px;
  border-radius: var(--radius-pill);
  background: var(--accent-light);
  color: #b8963c;
}

/* ── 流式光标 ── */
.typing-cursor {
  color: var(--brand);
  font-weight: bold;
  margin-left: 1px;
}

/* ── 思考三点波浪加载 ── */
.thinking-indicator {
  display: flex;
  gap: 6px;
  padding: 6px 20px;
}

.thinking-indicator i {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--brand);
  animation: thinkWave 1.2s ease-in-out infinite;
  opacity: 0.4;
}

.thinking-indicator i:nth-child(1) { animation-delay: 0s; }
.thinking-indicator i:nth-child(2) { animation-delay: 0.2s; }
.thinking-indicator i:nth-child(3) { animation-delay: 0.4s; }

@keyframes thinkWave {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-9px); opacity: 1; }
}

/* ── 代码块 ── */
.bot-bubble :deep(.code-block-wrapper) {
  position: relative;
  margin: 8px 0;
  border-radius: 10px;
  overflow: hidden;
  background: #1e1e1e;
}

.bot-bubble :deep(.code-block-wrapper pre) {
  margin: 0;
  padding: 16px;
  overflow-x: auto;
}

.bot-bubble :deep(.code-block-wrapper code) {
  background: transparent;
  padding: 0;
  font-size: 13px;
  line-height: 1.6;
  color: #d4d4d4;
  font-family: 'Fira Code', 'Cascadia Code', 'JetBrains Mono', Consolas, monospace;
}

.bot-bubble :deep(.code-lang) {
  position: absolute;
  top: 0; left: 0;
  padding: 2px 10px;
  font-size: 11px;
  color: #9cdcfe;
  background: rgba(255,255,255,0.06);
  border-radius: 0 0 6px 0;
  user-select: none;
  pointer-events: none;
}

.bot-bubble :deep(.code-copy-btn) {
  position: absolute;
  top: 4px; right: 4px;
  padding: 2px 10px;
  font-size: 11px;
  color: #ccc;
  background: rgba(255,255,255,0.08);
  border: none;
  border-radius: 5px;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s;
}

.bot-bubble :deep(.code-block-wrapper:hover .code-copy-btn) { opacity: 1; }
.bot-bubble :deep(.code-copy-btn:hover) { background: rgba(255,255,255,0.16); color: #fff; }
</style>
