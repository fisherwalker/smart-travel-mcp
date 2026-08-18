<template>
  <div class="chat-input-area">
    <div class="input-shell" :class="{ focused: isFocused }">
      <textarea
        ref="textareaRef"
        v-model="inputText"
        class="chat-textarea"
        :disabled="disabled"
        placeholder="输入你的问题，例如：杭州有哪些景点？"
        rows="1"
        @keydown.enter.exact="handleSend"
        @focus="isFocused = true"
        @blur="isFocused = false"
        @input="autoResize"
      ></textarea>
      <button
        class="send-pill"
        :class="{ ready: inputText.trim() && !disabled }"
        :disabled="disabled || !inputText.trim()"
        @click="handleSend"
        title="发送 (Enter)"
      >
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/>
        </svg>
      </button>
    </div>
    <p class="input-hint">Enter 发送 · Shift+Enter 换行</p>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'

const props = defineProps<{
  disabled: boolean
}>()

const emit = defineEmits<{
  send: [text: string]
}>()

const inputText = ref('')
const isFocused = ref(false)
const textareaRef = ref<HTMLTextAreaElement>()

function autoResize() {
  nextTick(() => {
    const el = textareaRef.value
    if (!el) return
    el.style.height = 'auto'
    el.style.height = Math.min(el.scrollHeight, 120) + 'px'
  })
}

function handleSend(e?: KeyboardEvent) {
  if (e && e.shiftKey) return
  if (e) e.preventDefault()
  if (!inputText.value.trim() || props.disabled) return
  emit('send', inputText.value.trim())
  inputText.value = ''
  nextTick(() => {
    const el = textareaRef.value
    if (el) el.style.height = 'auto'
  })
}
</script>

<style scoped>
.chat-input-area {
  padding: 16px 24px 12px;
  border-top: 1px solid var(--border-subtle);
  flex-shrink: 0;
}

/* ── 输入外壳 ── */
.input-shell {
  position: relative;
  display: flex;
  align-items: flex-end;
  background: var(--bg-primary);
  border-radius: 20px;
  padding: 8px 12px;
  border: 2px solid transparent;
  transition: all var(--transition);
}

.input-shell.focused {
  border-color: var(--brand);
  background: #fff;
  box-shadow: 0 0 0 4px rgba(107, 158, 147, 0.08);
}

/* ── Textarea ── */
.chat-textarea {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
  line-height: 1.6;
  padding: 6px 48px 6px 8px;
  resize: none;
  color: var(--text-primary);
  font-family: inherit;
  max-height: 120px;
}

.chat-textarea::placeholder {
  color: #c5bfb2;
}

.chat-textarea:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* ── 发送按钮 ── */
.send-pill {
  position: absolute;
  right: 10px;
  bottom: 10px;
  width: 38px;
  height: 38px;
  border-radius: 50%;
  border: none;
  background: var(--border-subtle);
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition);
  flex-shrink: 0;
}

.send-pill.ready {
  background: var(--brand);
  color: #fff;
  box-shadow: 0 4px 14px rgba(107, 158, 147, 0.35);
}

.send-pill.ready:hover {
  background: var(--brand-dark);
  transform: scale(1.08);
  box-shadow: 0 6px 20px rgba(107, 158, 147, 0.45);
}

.send-pill:disabled {
  cursor: not-allowed;
}

.input-hint {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 8px;
  text-align: center;
  opacity: 0.5;
}
</style>
