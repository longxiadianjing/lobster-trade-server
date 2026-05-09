<template>
  <div class="cs-history-page">
    <div class="cs-history-header">
      <div class="header-left">
        <el-icon size="20"><ChatDotRound /></el-icon>
        <span>我的客服会话</span>
      </div>
      <el-button type="primary" size="small" @click="startNewSession">
        <el-icon><Plus /></el-icon>发起新会话
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="cs-stats-row">
      <div class="stat-card" :class="{ active: filterStatus === '' }" @click="filterStatus = ''">
        <div class="stat-num">{{ stats.total }}</div>
        <div class="stat-label">全部会话</div>
      </div>
      <div class="stat-card" :class="{ active: filterStatus === 0 }" @click="filterStatus = 0">
        <div class="stat-num waiting">{{ stats.waiting }}</div>
        <div class="stat-label">等待中</div>
      </div>
      <div class="stat-card" :class="{ active: filterStatus === 1 }" @click="filterStatus = 1">
        <div class="stat-num active">{{ stats.active }}</div>
        <div class="stat-label">进行中</div>
      </div>
      <div class="stat-card" :class="{ active: filterStatus === 2 }" @click="filterStatus = 2">
        <div class="stat-num closed">{{ stats.closed }}</div>
        <div class="stat-label">已关闭</div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="cs-toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索会话主题..."
        clearable
        style="width: 260px"
        @clear="loadSessions"
        @keydown.enter="loadSessions"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button @click="loadSessions">搜索</el-button>
    </div>

    <!-- 会话列表 -->
    <div class="cs-list" v-loading="loading">
      <div v-if="sessions.length === 0 && !loading" class="cs-empty-state">
        <el-icon :size="60" color="#ddd"><ChatDotRound /></el-icon>
        <p>暂无客服会话记录</p>
        <p class="sub">点击右上角「发起新会话」联系客服</p>
      </div>

      <div
        v-for="session in sessions"
        :key="session.id"
        class="cs-session-card"
        :class="{ 'is-closed': session.status === 2 }"
        @click="openSession(session)"
      >
        <div class="csc-left">
          <div class="csc-status-dot" :class="'s' + session.status"></div>
          <div class="csc-info">
            <div class="csc-subject">
              {{ session.subject || '在线客服' }}
              <el-tag v-if="session.priority === 1" type="danger" size="small" class="csc-tag">紧急</el-tag>
            </div>
            <div class="csc-meta">
              <span class="csc-no">{{ session.sessionNo }}</span>
              <span class="csc-time">{{ formatDate(session.createTime) }}</span>
            </div>
          </div>
        </div>
        <div class="csc-right">
          <span class="csc-status-text">{{ statusText(session.status) }}</span>
          <el-icon class="csc-arrow"><ArrowRight /></el-icon>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="cs-pagination" v-if="total > 0">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="loadSessions"
      />
    </div>

    <!-- 聊天抽屉 -->
    <el-drawer
      v-model="chatDrawer"
      :title="chatSession?.subject || '客服会话'"
      size="420px"
      :with-header="true"
      direction="rtl"
    >
      <div class="cs-chat-drawer">
        <!-- 消息区域 -->
        <div class="cd-messages" ref="cdMessagesRef">
          <div v-if="cdMessages.length === 0" class="cd-empty">
            暂无消息，开始对话吧
          </div>
          <div
            v-for="msg in cdMessages"
            :key="msg.id"
            class="cd-msg"
            :class="msg.senderType === 1 ? 'cd-msg-operator' : 'cd-msg-customer'"
          >
            <div class="cd-avatar">
              {{ msg.senderType === 1 ? '🦞' : (userStore.nickname?.charAt(0) || '我') }}
            </div>
            <div class="cd-bubble-wrap">
              <div class="cd-bubble">{{ msg.content }}</div>
              <div class="cd-time">{{ formatTime(msg.createTime) }}</div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="cd-input" v-if="chatSession?.status !== 2">
          <el-input
            v-model="cdInput"
            placeholder="输入消息，Ctrl+Enter 发送"
            type="textarea"
            :rows="2"
            resize="none"
            @keydown.enter.ctrl="cdSend"
          />
          <el-button type="primary" :disabled="!cdInput.trim()" :loading="cdSending" @click="cdSend">
            发送
          </el-button>
        </div>
        <div class="cd-input cd-closed" v-else>
          <span>会话已结束</span>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
  document.title = '客服记录 - 龙虾道具交易平台';

import { ref, reactive, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useUserStore } from '@/stores/user'
import { getMyCsSessions, startCsSession, getCsSession, sendCsMessage, closeCsSession } from '@/api/cs'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Plus, Search, ArrowRight } from '@element-plus/icons-vue'

const userStore = useUserStore()

const loading = ref(false)
const sessions = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const keyword = ref('')
const filterStatus = ref('')

const stats = reactive({ total: 0, waiting: 0, active: 0, closed: 0 })

const chatDrawer = ref(false)
const chatSession = ref(null)
const cdMessages = ref([])
const cdInput = ref('')
const cdSending = ref(false)
const cdMessagesRef = ref(null)
const cdDrawerVisible = ref(false)

let cdPollTimer = null
let sseSource = null
let sseConnecting = ref(false)

// Play notification beep using Web Audio API (no external file needed)
const playBeep = () => {
  try {
    const ctx = new (window.AudioContext || window.webkitAudioContext)()
    const osc = ctx.createOscillator()
    const gain = ctx.createGain()
    osc.connect(gain)
    gain.connect(ctx.destination)
    osc.frequency.value = 880
    osc.type = 'sine'
    gain.gain.setValueAtTime(0.25, ctx.currentTime)
    gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.35)
    osc.start(ctx.currentTime)
    osc.stop(ctx.currentTime + 0.35)
    ctx.close()
  } catch (e) { /* ignore */ }
}

const connectSSE = (token, sessionId) => {
  if (sseSource) { sseSource.close(); sseSource = null }
  const url = `/api/cs/subscribe/${sessionId}?token=${encodeURIComponent(token)}`
  sseSource = new EventSource(url)
  sseConnecting.value = true
  sseSource.onopen = () => { sseConnecting.value = false }
  sseSource.onerror = () => {
    sseConnecting.value = false
    sseSource.close(); sseSource = null
  }
  sseSource.addEventListener('new_message', (e) => {
    const data = JSON.parse(e.data)
    if (data.id === sessionId) {
      cdMessages.value = data.messages || []
      nextTick(scrollCdBottom)
    }
    playBeep()
  })
  sseSource.addEventListener('operator_assigned', (e) => {
    playBeep()
    if (data.sessionId === sessionId && chatSession.value) {
      chatSession.value.handlerName = data.operatorName
    }
  })
  sseSource.addEventListener('session_closed', (e) => {
    if (chatSession.value) chatSession.value.status = 2
  })
}

const disconnectSSE = () => {
  if (sseSource) { sseSource.close(); sseSource = null }
}

const startCdPolling = () => {
  stopCdPolling()
  cdPollTimer = setInterval(async () => {
    if (cdDrawerVisible.value && chatSession.value && chatSession.value.status !== 2) {
      try {
        const res = await getCsSession(chatSession.value.id)
        cdMessages.value = res.data?.messages || []
        chatSession.value = res.data
        await nextTick()
        scrollCdBottom()
      } catch (e) { /* silent */ }
    }
  }, 5000)
}
const stopCdPolling = () => { if (cdPollTimer) { clearInterval(cdPollTimer); cdPollTimer = null } }

const statusText = (status) => ['等待中', '进行中', '已关闭'][status] || ''

const formatDate = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const loadSessions = async (page = 1) => {
  loading.value = true
  currentPage.value = page
  try {
    const all = await getMyCsSessions()
    // Filter locally (backend doesn't have search yet)
    let filtered = all.data || []
    if (keyword.value) {
      filtered = filtered.filter(s => (s.subject || '').includes(keyword.value))
    }
    if (filterStatus.value !== '') {
      filtered = filtered.filter(s => s.status === filterStatus.value)
    }
    total.value = filtered.length
    sessions.value = filtered.slice((page - 1) * pageSize, page * pageSize)

    // Stats
    const allData = all.data || []
    stats.total = allData.length
    stats.waiting = allData.filter(s => s.status === 0).length
    stats.active = allData.filter(s => s.status === 1).length
    stats.closed = allData.filter(s => s.status === 2).length
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const openSession = async (session) => {
  chatSession.value = session
  try {
    const res = await getCsSession(session.id)
    cdMessages.value = res.data?.messages || []
    chatDrawer.value = true
    cdDrawerVisible.value = true
    // Connect SSE for real-time updates
    const token = userStore.token || ''
    connectSSE(token, session.id)
    await nextTick()
    scrollCdBottom()
    startCdPolling()
  } catch (e) {
    ElMessage.error('加载会话失败')
  }
}

const cdSend = async () => {
  if (!cdInput.value.trim() || cdSending.value) return
  cdSending.value = true
  try {
    const res = await sendCsMessage({
      sessionId: chatSession.value.id,
      content: cdInput.value.trim(),
      messageType: 'text'
    })
    cdMessages.value = res.data?.messages || []
    cdInput.value = ''
    chatSession.value = res.data
    await nextTick()
    scrollCdBottom()
  } catch (e) {
    ElMessage.error('发送失败')
  } finally {
    cdSending.value = false
  }
}

const scrollCdBottom = () => {
  if (cdMessagesRef.value) {
    cdMessagesRef.value.scrollTop = cdMessagesRef.value.scrollHeight
  }
}

const startNewSession = async () => {
  try {
    const res = await startCsSession({ subject: '' })
    ElMessage.success('会话已创建')
    await loadSessions()
    if (res.data) {
      openSession(res.data)
    }
  } catch (e) {
    ElMessage.error('创建会话失败')
  }
}

watch(chatDrawer, (val) => {
  if (!val) {
    stopCdPolling()
    disconnectSSE()
    cdDrawerVisible.value = false
  }
})

onMounted(() => {
  loadSessions()
})

onUnmounted(() => {
  stopCdPolling()
  disconnectSSE()
})
</script>

<style scoped>
.cs-history-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px 16px;
  min-height: 100vh;
  background: #f5f6f8;
}

.cs-history-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.cs-stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  border-radius: 10px;
  padding: 16px;
  text-align: center;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s;
}

.stat-card.active {
  border-color: #409eff;
  background: #ecf5ff;
}

.stat-num {
  font-size: 28px;
  font-weight: 700;
  color: #333;
}

.stat-num.waiting { color: #e6a23c; }
.stat-num.active { color: #409eff; }
.stat-num.closed { color: #909399; }

.stat-label {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.cs-toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  align-items: center;
}

.cs-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.cs-empty-state {
  text-align: center;
  padding: 60px 0;
  color: #999;
  background: white;
  border-radius: 12px;
}

.cs-empty-state p { margin: 10px 0 0; }
.cs-empty-state .sub { font-size: 13px; color: #bbb; }

.cs-session-card {
  background: white;
  border-radius: 10px;
  padding: 14px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #eee;
}

.cs-session-card:hover { background: #f9faff; border-color: #409eff; }
.cs-session-card.is-closed { opacity: 0.7; }

.csc-left { display: flex; align-items: center; gap: 12px; flex: 1; min-width: 0; }

.csc-status-dot {
  width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0;
}
.csc-status-dot.s0 { background: #e6a23c; }
.csc-status-dot.s1 { background: #409eff; }
.csc-status-dot.s2 { background: #909399; }

.csc-info { min-width: 0; }
.csc-subject {
  font-size: 14px; font-weight: 500; color: #333;
  display: flex; align-items: center; gap: 6px; flex-wrap: wrap;
}
.csc-tag { margin-left: 4px; }
.csc-meta { display: flex; gap: 12px; margin-top: 4px; }
.csc-no { font-size: 12px; color: #bbb; }
.csc-time { font-size: 12px; color: #bbb; }

.csc-right { display: flex; align-items: center; gap: 8px; flex-shrink: 0; }
.csc-status-text { font-size: 12px; color: #999; }
.csc-arrow { color: #ccc; }

.cs-pagination { display: flex; justify-content: center; margin-top: 20px; }

/* Drawer chat */
.cs-chat-drawer {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 60px);
}

.cd-messages {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cd-empty {
  text-align: center;
  color: #bbb;
  margin-top: 40px;
  font-size: 13px;
}

.cd-msg { display: flex; gap: 8px; align-items: flex-end; }
.cd-msg-operator { flex-direction: row; }
.cd-msg-customer { flex-direction: row-reverse; }

.cd-avatar {
  width: 32px; height: 32px; border-radius: 50%;
  background: #409eff; color: white;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; flex-shrink: 0;
}
.cd-msg-operator .cd-avatar { background: #ff6b35; }

.cd-bubble-wrap { max-width: 75%; }
.cd-bubble {
  padding: 8px 12px; border-radius: 10px;
  background: #f0f0f0; color: #333; font-size: 14px; line-height: 1.5;
  word-break: break-word;
}
.cd-msg-customer .cd-bubble { background: #409eff; color: white; }
.cd-time { font-size: 11px; color: #bbb; margin-top: 3px; text-align: right; }

.cd-input {
  border-top: 1px solid #eee;
  padding: 10px;
  display: flex;
  gap: 8px;
  align-items: flex-end;
}
.cd-input .el-button { flex-shrink: 0; }
.cd-closed { justify-content: center; color: #999; font-size: 13px; padding: 16px; }
</style>
