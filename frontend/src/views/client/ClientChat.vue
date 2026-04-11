<template>
  <div class="chat-page">
    <!-- Sidebar overlay for mobile -->
    <div
      v-if="sidebarOpen"
      class="sidebar-overlay"
      @click="sidebarOpen = false"
    />

    <!-- Sidebar -->
    <aside class="chat-sidebar" :class="{ open: sidebarOpen }">
      <div class="sidebar-header">
        <span class="sidebar-title">历史会话</span>
        <el-button size="small" type="primary" plain @click="startNewSession">
          + 新对话
        </el-button>
      </div>
      <div class="session-list">
        <div
          v-for="s in sessions"
          :key="s.id"
          class="session-item"
          :class="{ active: s.id === currentSessionId }"
          @click="switchSession(s.id)"
        >
          <div class="session-title">{{ s.title || '新对话' }}</div>
          <div class="session-time">{{ formatTime(s.createTime) }}</div>
        </div>
        <div v-if="sessions.length === 0" class="session-empty">
          暂无历史会话
        </div>
      </div>
    </aside>

    <!-- Main chat area -->
    <div class="chat-main">
      <!-- Top bar -->
      <header class="chat-topbar">
        <button class="menu-btn" @click="sidebarOpen = !sidebarOpen">
          <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <line x1="3" y1="6" x2="21" y2="6"/>
            <line x1="3" y1="12" x2="21" y2="12"/>
            <line x1="3" y1="18" x2="21" y2="18"/>
          </svg>
        </button>
        <div class="topbar-title">
          <span class="topbar-icon">💬</span>
          <span>ERP培训助手</span>
          <span v-if="clientAuth.projectName" class="topbar-project">— {{ clientAuth.projectName }}</span>
        </div>
        <router-link to="/client/learning" class="learning-link">📚 学习中心</router-link>
        <el-button text class="logout-btn" @click="handleLogout">退出</el-button>
      </header>

      <!-- Messages area -->
      <div ref="messagesContainer" class="messages-area">
        <div v-if="messages.length === 0 && !loadingMessages" class="welcome-prompt">
          <div class="welcome-icon">🎓</div>
          <h2>欢迎使用 ERP 培训助手</h2>
          <p>您好，{{ clientAuth.employeeName }}！有任何关于 ERP 系统操作的问题，都可以向我提问。</p>
          <div class="welcome-hints">
            <div class="hint-chip" @click="sendQuickQuestion('钢卷入库时怎么录入规格？')">钢卷入库时怎么录入规格？</div>
            <div class="hint-chip" @click="sendQuickQuestion('如何创建销售订单？')">如何创建销售订单？</div>
            <div class="hint-chip" @click="sendQuickQuestion('怎么设置客户信用额度？')">怎么设置客户信用额度？</div>
          </div>
        </div>

        <div v-if="loadingMessages" class="loading-messages">
          <el-icon class="is-loading" :size="24"><Loading /></el-icon>
          <span>加载中...</span>
        </div>

        <TransitionGroup name="msg" tag="div" class="messages-list">
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="message-row"
            :class="msg.role === 'USER' ? 'user-row' : 'ai-row'"
          >
            <div class="message-avatar" v-if="msg.role !== 'USER'">
              <span>🤖</span>
            </div>
            <div class="message-bubble" :class="msg.role === 'USER' ? 'user-bubble' : 'ai-bubble'">
              <div class="message-content">{{ msg.content }}</div>
              <div v-if="msg.role !== 'USER' && msg.relatedModule" class="message-module">
                <el-tag size="small" type="info">{{ msg.relatedModule }}</el-tag>
              </div>
              <div v-if="msg.role !== 'USER' && msg.relatedVideoUrl" class="message-video">
                <a :href="msg.relatedVideoUrl" target="_blank" class="video-link">📹 查看操作视频</a>
              </div>
              <div v-if="msg.role !== 'USER'" class="message-feedback">
                <button
                  class="feedback-btn"
                  :class="{ active: msg.helpful === true }"
                  title="有帮助"
                  @click="rateMsg(msg.id, true)"
                >👍 有帮助</button>
                <button
                  class="feedback-btn"
                  :class="{ active: msg.helpful === false }"
                  title="没帮助"
                  @click="rateMsg(msg.id, false)"
                >👎 没帮助</button>
              </div>
              <div class="message-time">{{ formatTime(msg.createTime) }}</div>
            </div>
            <div class="message-avatar" v-if="msg.role === 'USER'">
              <span>👤</span>
            </div>
          </div>
        </TransitionGroup>

        <!-- Typing indicator -->
        <div v-if="sending" class="message-row ai-row">
          <div class="message-avatar"><span>🤖</span></div>
          <div class="message-bubble ai-bubble typing-bubble">
            <div class="typing-dots">
              <span/><span/><span/>
            </div>
          </div>
        </div>
      </div>

      <!-- Input area -->
      <div class="input-area">
        <div class="input-wrapper">
          <textarea
            ref="inputEl"
            v-model="question"
            class="chat-input"
            :placeholder="'请输入您的问题，如：钢卷入库时怎么录入规格？'"
            rows="1"
            :disabled="sending"
            @keydown="handleKeydown"
            @input="autoResize"
          />
          <button
            class="send-btn"
            :disabled="!question.trim() || sending"
            @click="sendMessage"
          >
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="22" y1="2" x2="11" y2="13"/>
              <polygon points="22 2 15 22 11 13 2 9 22 2"/>
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Loading } from '@element-plus/icons-vue'
import { useClientAuthStore } from '@/stores/clientAuth'
import { clientAsk, clientGetSessions, clientGetMessages, clientRateMessage } from '@/api/qasession'
import type { QaSession, QaMessage } from '@/types'

const router = useRouter()
const clientAuth = useClientAuthStore()

const sidebarOpen = ref(false)
const sessions = ref<QaSession[]>([])
const currentSessionId = ref<number | undefined>(undefined)
const messages = ref<QaMessage[]>([])
const question = ref('')
const sending = ref(false)
const loadingMessages = ref(false)
const messagesContainer = ref<HTMLElement>()
const inputEl = ref<HTMLTextAreaElement>()

onMounted(async () => {
  await loadSessions()
})

async function loadSessions() {
  try {
    const res = await clientGetSessions()
    sessions.value = res.data.data || []
  } catch { /* ignore */ }
}

async function switchSession(id: number) {
  currentSessionId.value = id
  sidebarOpen.value = false
  loadingMessages.value = true
  try {
    const res = await clientGetMessages(id)
    messages.value = res.data.data || []
    await nextTick()
    scrollToBottom()
  } finally {
    loadingMessages.value = false
  }
}

function startNewSession() {
  currentSessionId.value = undefined
  messages.value = []
  sidebarOpen.value = false
}

function sendQuickQuestion(q: string) {
  question.value = q
  sendMessage()
}

async function sendMessage() {
  const text = question.value.trim()
  if (!text || sending.value) return

  const userMsg: QaMessage = {
    id: Date.now(),
    sessionId: currentSessionId.value || 0,
    role: 'USER',
    content: text,
    relatedModule: '',
    relatedVideoUrl: '',
    helpful: null,
    createTime: new Date().toISOString(),
  }
  messages.value.push(userMsg)
  question.value = ''
  resetTextarea()
  await nextTick()
  scrollToBottom()

  sending.value = true
  try {
    const res = await clientAsk({
      sessionId: currentSessionId.value,
      question: text,
    })
    const aiMsg = res.data.data
    messages.value.push(aiMsg)

    if (!currentSessionId.value) {
      currentSessionId.value = aiMsg.sessionId
      await loadSessions()
    }
  } catch {
    messages.value.push({
      id: Date.now() + 1,
      sessionId: currentSessionId.value || 0,
      role: 'AI',
      content: '抱歉，出了点问题，请稍后再试。',
      relatedModule: '',
      relatedVideoUrl: '',
      helpful: null,
      createTime: new Date().toISOString(),
    })
  } finally {
    sending.value = false
    await nextTick()
    scrollToBottom()
  }
}

async function rateMsg(messageId: number, helpful: boolean) {
  try {
    await clientRateMessage(messageId, helpful)
    const msg = messages.value.find((m) => m.id === messageId)
    if (msg) msg.helpful = helpful
  } catch { /* ignore */ }
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

function autoResize() {
  const el = inputEl.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 120) + 'px'
}

function resetTextarea() {
  const el = inputEl.value
  if (el) el.style.height = 'auto'
}

function scrollToBottom() {
  const container = messagesContainer.value
  if (container) {
    container.scrollTop = container.scrollHeight
  }
}

function formatTime(iso: string) {
  if (!iso) return ''
  const d = new Date(iso)
  const now = new Date()
  const isToday = d.toDateString() === now.toDateString()
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  if (isToday) return `${hh}:${mm}`
  const MM = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${MM}-${dd} ${hh}:${mm}`
}

function handleLogout() {
  clientAuth.logout()
  router.push('/client/login')
}

watch(messages, () => {
  nextTick(scrollToBottom)
}, { deep: true })
</script>

<style scoped>
.chat-page {
  height: 100vh;
  display: flex;
  background: #f7f8fa;
  overflow: hidden;
}

/* ---- Sidebar ---- */
.sidebar-overlay {
  display: none;
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.3);
  z-index: 90;
}

.chat-sidebar {
  width: 260px;
  background: #fff;
  border-right: 1px solid #e8e8ed;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid #e8e8ed;
}

.sidebar-title {
  font-weight: 600;
  font-size: 15px;
  color: #1d1d1f;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.session-item {
  padding: 12px;
  border-radius: 10px;
  cursor: pointer;
  margin-bottom: 4px;
  transition: background 0.2s;
}

.session-item:hover {
  background: #f0f2f5;
}

.session-item.active {
  background: #e8f0fe;
}

.session-title {
  font-size: 14px;
  color: #1d1d1f;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-time {
  font-size: 12px;
  color: #8e8e93;
  margin-top: 4px;
}

.session-empty {
  text-align: center;
  color: #8e8e93;
  font-size: 13px;
  padding: 32px 0;
}

/* ---- Main ---- */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

/* ---- Top bar ---- */
.chat-topbar {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #e8e8ed;
  flex-shrink: 0;
}

.menu-btn {
  display: none;
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  color: #3a3a4a;
  margin-right: 8px;
}

.topbar-title {
  flex: 1;
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
  display: flex;
  align-items: center;
  gap: 6px;
}

.topbar-icon {
  font-size: 20px;
}

.topbar-project {
  font-weight: 400;
  color: #8e8e93;
  font-size: 14px;
}

.learning-link {
  color: #4361ee;
  text-decoration: none;
  font-size: 14px;
  padding: 4px 12px;
  border-radius: 8px;
  transition: background 0.2s;
  white-space: nowrap;
}

.learning-link:hover {
  background: #f0f2f5;
}

.logout-btn {
  color: #8e8e93 !important;
  font-size: 14px;
}

.logout-btn:hover {
  color: #f56c6c !important;
}

/* ---- Messages ---- */
.messages-area {
  flex: 1;
  overflow-y: auto;
  padding: 24px 16px;
  scroll-behavior: smooth;
}

.welcome-prompt {
  text-align: center;
  padding: 60px 20px 40px;
}

.welcome-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.welcome-prompt h2 {
  font-size: 22px;
  font-weight: 700;
  color: #1d1d1f;
  margin: 0 0 8px;
}

.welcome-prompt p {
  font-size: 15px;
  color: #8e8e93;
  margin: 0 0 24px;
  line-height: 1.5;
}

.welcome-hints {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.hint-chip {
  background: #fff;
  border: 1px solid #e0e0e5;
  border-radius: 20px;
  padding: 8px 16px;
  font-size: 13px;
  color: #4361ee;
  cursor: pointer;
  transition: all 0.2s;
}

.hint-chip:hover {
  background: #4361ee;
  color: #fff;
  border-color: #4361ee;
}

.loading-messages {
  text-align: center;
  padding: 40px;
  color: #8e8e93;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.messages-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  max-width: 85%;
}

.user-row {
  margin-left: auto;
  flex-direction: row;
}

.ai-row {
  margin-right: auto;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
  background: #f0f2f5;
}

.message-bubble {
  border-radius: 16px;
  padding: 12px 16px;
  font-size: 14px;
  line-height: 1.6;
  position: relative;
}

.user-bubble {
  background: linear-gradient(135deg, #4361ee, #6c63ff);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.ai-bubble {
  background: #fff;
  color: #1d1d1f;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.message-content {
  white-space: pre-wrap;
  word-break: break-word;
}

.message-module {
  margin-top: 8px;
}

.message-video {
  margin-top: 6px;
}

.video-link {
  color: #4361ee;
  text-decoration: none;
  font-size: 13px;
}

.video-link:hover {
  text-decoration: underline;
}

.message-feedback {
  margin-top: 8px;
  display: flex;
  gap: 8px;
}

.feedback-btn {
  background: #f0f2f5;
  border: none;
  border-radius: 14px;
  padding: 4px 12px;
  font-size: 12px;
  cursor: pointer;
  color: #6e6e73;
  transition: all 0.2s;
}

.feedback-btn:hover {
  background: #e0e0e5;
}

.feedback-btn.active {
  background: #4361ee;
  color: #fff;
}

.message-time {
  font-size: 11px;
  color: #b0b0b5;
  margin-top: 6px;
}

.user-bubble .message-time {
  color: rgba(255, 255, 255, 0.7);
}

/* Typing indicator */
.typing-bubble {
  padding: 14px 20px;
}

.typing-dots {
  display: flex;
  gap: 5px;
}

.typing-dots span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #a0aec0;
  animation: typingBounce 1.4s infinite ease-in-out;
}

.typing-dots span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-dots span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typingBounce {
  0%, 80%, 100% {
    transform: scale(0.6);
    opacity: 0.4;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

/* Message transitions */
.msg-enter-active {
  transition: all 0.3s ease-out;
}

.msg-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

/* ---- Input area ---- */
.input-area {
  padding: 12px 16px 20px;
  background: #fff;
  border-top: 1px solid #e8e8ed;
  flex-shrink: 0;
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  max-width: 800px;
  margin: 0 auto;
  background: #f0f2f5;
  border-radius: 16px;
  padding: 8px 8px 8px 16px;
}

.chat-input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 14px;
  line-height: 1.5;
  resize: none;
  outline: none;
  font-family: inherit;
  color: #1d1d1f;
  max-height: 120px;
  min-height: 24px;
}

.chat-input::placeholder {
  color: #b0b0b5;
}

.send-btn {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  border: none;
  background: #4361ee;
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: background 0.2s;
}

.send-btn:hover:not(:disabled) {
  background: #3a56d4;
}

.send-btn:disabled {
  background: #c0c4cc;
  cursor: not-allowed;
}

/* ---- Responsive ---- */
@media (max-width: 768px) {
  .chat-sidebar {
    position: fixed;
    left: -280px;
    top: 0;
    bottom: 0;
    z-index: 100;
    transition: left 0.3s ease;
    box-shadow: 4px 0 20px rgba(0, 0, 0, 0.1);
  }

  .chat-sidebar.open {
    left: 0;
  }

  .sidebar-overlay {
    display: block;
  }

  .menu-btn {
    display: flex;
  }

  .message-row {
    max-width: 92%;
  }

  .welcome-prompt {
    padding: 40px 16px 24px;
  }

  .welcome-prompt h2 {
    font-size: 18px;
  }
}
</style>
