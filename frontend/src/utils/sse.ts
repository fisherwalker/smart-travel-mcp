/**
 * SSE 流式聊天客户端
 * 使用 Fetch API + ReadableStream 读取 SSE 事件
 *
 * 修复说明：
 * - Spring SseEmitter 产出格式为 `data:value`（冒号后无空格），
 *   原来检查 `data: ` 会导致所有事件被丢弃，仅靠轮询兜底造成"回答不及时"。
 * - 增加 `event:` 行解析，按事件名精确路由，不再仅靠 JSON 字段推断。
 */
export interface SSEEvent {
  type: 'session' | 'thinking' | 'message' | 'done' | 'error' | 'keepalive' | 'needApiKey'
  data: any
}

export function streamChat(
  sessionId: number | null,
  message: string,
  handlers: {
    onSession?: (sessionId: number) => void
    onThinking?: (message: string) => void
    onChunk?: (content: string) => void
    onDone?: (fullContent: string) => void
    onError?: (error: string) => void
    onNeedApiKey?: () => void
  }
): Promise<void> {
  return new Promise((resolve) => {
    fetch('/api/chat/stream', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ sessionId, message })
    }).then(async response => {
      if (!response.ok) {
        handlers.onError?.('网络请求失败: ' + response.status)
        resolve()
        return
      }

      const reader = response.body?.getReader()
      if (!reader) {
        handlers.onError?.('浏览器不支持流式读取')
        resolve()
        return
      }

      const decoder = new TextDecoder()
      let buffer = ''
      let fullContent = ''
      let streamEnded = false

      try {
        while (true) {
          const { done, value } = await reader.read()
          if (done) {
            if (!streamEnded) {
              handlers.onDone?.(fullContent)
            }
            break
          }

          buffer += decoder.decode(value, { stream: true })
          const lines = buffer.split('\n')
          buffer = lines.pop() || ''

          // 当前事件名（来自最近的 event: 行）
          let currentEvent: string | null = null

          for (let i = 0; i < lines.length; i++) {
            const line = lines[i].trim()
            if (!line) {
              // 空行 = SSE 事件边界，重置事件名
              currentEvent = null
              continue
            }

            // ── 处理 event: 行 ──
            if (line.startsWith('event:')) {
              currentEvent = line.substring(6).trim()
              continue
            }

            // ── 处理 data: 行（兼容冒号后有/无空格） ──
            if (!line.startsWith('data:')) continue

            // 提取 data 值：跳过 "data:" 后可选的一个空格
            let dataStr = line.substring(5) // "data:".length === 5
            if (dataStr.startsWith(' ')) dataStr = dataStr.substring(1)
            if (!dataStr) continue

            // 流结束标志
            if (dataStr === '[DONE]') {
              streamEnded = true
              continue
            }

            try {
              const eventData = JSON.parse(dataStr)

              // ── 按事件名路由（优先） ──
              if (currentEvent === 'session') {
                if (eventData.sessionId) {
                  handlers.onSession?.(eventData.sessionId)
                }
              } else if (currentEvent === 'thinking') {
                if (eventData.message) {
                  handlers.onThinking?.(eventData.message)
                }
              } else if (currentEvent === 'message') {
                if (eventData.content) {
                  fullContent += eventData.content
                  handlers.onChunk?.(eventData.content)
                }
              } else if (currentEvent === 'done') {
                streamEnded = true
                handlers.onDone?.(eventData.fullContent || fullContent)
              } else if (currentEvent === 'error') {
                handlers.onError?.(eventData.error || '未知服务端错误')
              } else if (currentEvent === 'keepalive') {
                // 保活事件，忽略
              } else {
                // ── 无 event: 行时的兜底推断 ──
                routeByDataFields(eventData)
              }
            } catch {
              // JSON 解析失败：可能是纯文本，交给 onChunk 处理
              if (currentEvent === null || currentEvent === 'message') {
                fullContent += dataStr
                handlers.onChunk?.(dataStr)
              }
            }
          }
        }
      } catch (err: any) {
        handlers.onError?.(err.message || '流式读取中断')
      }
      resolve()

      /** 没有 event: 行时，根据 JSON 字段推断事件类型（向后兼容） */
      function routeByDataFields(eventData: any) {
        if (eventData.needApiKey) {
          handlers.onNeedApiKey?.()
          return
        }
        if (eventData.sessionId) {
          handlers.onSession?.(eventData.sessionId)
        } else if (eventData.done) {
          streamEnded = true
          handlers.onDone?.(eventData.fullContent || fullContent)
        } else if (eventData.error) {
          handlers.onError?.(eventData.error)
        } else if (eventData.thinking || eventData.message) {
          handlers.onThinking?.(eventData.message || eventData.thinking)
        } else if (eventData.content) {
          fullContent += eventData.content
          handlers.onChunk?.(eventData.content)
        }
        // keepalive 事件忽略
      }
    }).catch(err => {
      handlers.onError?.(err.message || '网络请求失败')
      resolve()
    })
  })
}
