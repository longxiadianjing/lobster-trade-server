<template>
  <div class="cs-widget">
    <!-- 悬浮按钮 -->
    <div v-if="!isOpen" class="cs-trigger" @click="openWidget">
      <el-icon :size="24"><ChatDotRound /></el-icon>
      <span>在线客服</span>
      <span v-if="unreadCount > 0" class="cs-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
    </div>

    <!-- 聊天面板 -->
    <div v-else class="cs-panel">
      <div class="cs-header">
        <div class="cs-header-info">
          <div class="cs-avatar">🦞</div>
          <div>
            <div class="cs-title">龙虾客服</div>
            <div class="cs-subtitle">专业·快捷·友善</div>
          </div>
        </div>
        <div class="cs-header-actions">
          <el-button v-if="currentSession && currentSession.status !== 2" text size="small" @click="closeCurrentSession">结束会话</el-button>
          <el-button text size="small" @click="isOpen = false"><el-icon><Close /></el-icon></el-button>
        </div>
      </div>

      <!-- 会话列表 / 聊天区域 -->
      <div class="cs-body">
        <!-- 会话列表（未进入聊天时） -->
        <div v-if="!currentSession" class="cs-session-list">
          <div v-if="sessions.length === 0" class="cs-empty">
            <el-icon :size="40" color="#ccc"><ChatDotRound /></el-icon>
            <p>暂无会话记录</p>
            <p class="cs-empty-sub">点击下方按钮发起新会话</p>
          </div>
          <div v-else class="cs-session-items">
            <div
              v-for="s in sessions"
              :key="s.id"
              class="cs-session-item"
              :class="{ active: selectedSessionId === s.id }"
              @click="selectSession(s.id)"
            >
              <div class="cs-si-left">
                <div class="cs-si-status" :class="statusClass(s.status)"></div>
                <div class="cs-si-info">
                  <div class="cs-si-subject">{{ s.subject || '在线客服' }}</div>
                  <div class="cs-si-time">{{ formatTime(s.createTime) }}</div>
                </div>
              </div>
              <el-tag v-if="s.priority === 1" type="danger" size="small">紧急</el-tag>
            </div>
          </div>

          <div class="cs-start-area">
            <el-input v-model="newSubject" placeholder="请简要描述您的问题（选填）" clearable />
            <el-button type="primary" :loading="starting" @click="startNewSession" style="margin-top: 8px; width: 100%;">
              <el-icon><Plus /></el-icon>
              发起新会话
            </el-button>
          </div>
        </div>

        <!-- 聊天界面 -->
        <div v-else class="cs-chat">
          <div class="cs-chat-header">
            <span class="cs-chat-title">{{ currentSession.subject || '在线客服' }}</span>
            <span class="cs-chat-status" :class="'status-' + currentSession.status">
              {{ statusText(currentSession.status) }}
            </span>
          </div>

          <div class="cs-messages" ref="messagesRef">
            <div v-if="messages.length === 0" class="cs-msg-empty">
              暂无消息，发送消息开始对话
            </div>
            <div
              v-for="msg in messages"
              :key="msg.id"
              class="cs-msg-item"
              :class="msg.senderType === 1 ? 'cs-msg-operator' : 'cs-msg-customer'"
            >
              <div v-if="msg.senderType === 0" class="cs-msg-avatar customer-avatar">
                {{ userStore.nickname?.charAt(0) || '我' }}
              </div>
              <div class="cs-msg-content">
                <div v-if="msg.messageType === 'image'" class="cs-msg-bubble cs-msg-image">
                  <el-image
                    :src="msg.attachmentUrl || msg.content"
                    fit="cover"
                    style="width: 120px; height: 120px; border-radius: 8px;"
                    :preview-src-list="[msg.attachmentUrl || msg.content]"
                    preview-teleported
                  />
                </div>
                <div v-else class="cs-msg-bubble">{{ msg.content }}</div>
                <div class="cs-msg-time">{{ formatTime(msg.createTime) }}</div>
              </div>
              <div v-if="msg.senderType === 1" class="cs-msg-avatar operator-avatar">🦞</div>
            </div>
          </div>

          <div v-if="currentSession.status !== 2" class="cs-input-area">
            <el-input
              v-model="inputMessage"
              placeholder="输入消息..."
              type="textarea"
              :rows="2"
              resize="none"
              @keydown.enter.ctrl="sendMessage"
            />
            <div class="cs-input-actions">
              <el-upload
                ref="imageUploadRef"
                action=""
                :auto-upload="false"
                :show-file-list="false"
                :on-change="handleImageChange"
                accept="image/*"
              >
                <el-button text size="small" :loading="uploadingImage" style="padding: 4px 8px;">
                  <el-icon><Picture /></el-icon>
                  <span style="font-size: 12px; margin-left: 2px;">截图</span>
                </el-button>
              </el-upload>
              <span class="cs-hint">Ctrl+Enter 发送</span>
              <el-button type="primary" :disabled="!inputMessage.trim()" :loading="sending" @click="sendMessage">
                发送
              </el-button>
            </div>
          </div>
          <div v-else class="cs-input-area cs-closed">
            <span>会话已结束</span>
            <el-button size="small" @click="currentSession = null; selectedSessionId = null;">返回列表</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useUserStore } from '@/stores/user'
import { startCsSession, getCsSession, sendCsMessage, closeCsSession, getMyCsSessions } from '@/api/cs'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Close, Plus, Picture } from '@element-plus/icons-vue'
import { uploadImage } from '@/api/product'

const userStore = useUserStore()
const isOpen = ref(false)
const unreadCount = ref(0)
const starting = ref(false)
const sending = ref(false)
const currentSession = ref(null)
const selectedSessionId = ref(null)
const newSubject = ref('')
const inputMessage = ref('')
const messages = ref([])
const messagesRef = ref(null)
const sessions = ref([])
const imageUploadRef = ref(null)
const uploadingImage = ref(false)
const imageFiles = ref([])

const statusClass = (status) => {
  return ['wait', 'active', 'closed'][status] || 'wait'
}

const statusText = (status) => {
  return ['等待中', '进行中', '已关闭'][status] || ''
}

const formatTime = (time) => {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  return d.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' }) + ' ' + d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const openWidget = async () => {
  isOpen.value = true
  if (userStore.isLoggedIn) {
    await loadSessions()
  }
}

const loadSessions = async () => {
  try {
    const res = await getMyCsSessions()
    if (res.data) {
      sessions.value = res.data
      const hasUnread = sessions.value.some(s => s.status === 1)
      unreadCount.value = hasUnread ? 1 : 0
    }
  } catch (e) {
    console.error('加载会话列表失败', e)
  }
}

const startNewSession = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  starting.value = true
  try {
    const res = await startCsSession({ subject: newSubject.value, priority: 0 })
    if (res.data) {
      currentSession.value = res.data
      messages.value = res.data.messages || []
      sessions.value.unshift(res.data)
      newSubject.value = ''
      scrollToBottom()
    }
  } catch (e) {
    ElMessage.error('发起会话失败')
  } finally {
    starting.value = false
  }
}

const selectSession = async (id) => {
  selectedSessionId.value = id
  try {
    const res = await getCsSession(id)
    if (res.data) {
      currentSession.value = res.data
      messages.value = res.data.messages || []
      scrollToBottom()
    }
  } catch (e) {
    ElMessage.error('加载会话失败')
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || !currentSession.value) return
  sending.value = true
  try {
    const res = await sendCsMessage({
      sessionId: currentSession.value.id,
      content: inputMessage.value.trim(),
      messageType: 'text'
    })
    if (res.data) {
      currentSession.value = res.data
      messages.value = res.data.messages || []
      inputMessage.value = ''
      nextTick(() => scrollToBottom())
    }
  } catch (e) {
    ElMessage.error('发送失败')
  } finally {
    sending.value = false
  }
}

const handleImageChange = async (file) => {
  if (!currentSession.value) {
    ElMessage.warning('请先进入会话')
    return
  }
  imageFiles.value = [file]
  await sendImage(file)
}

const sendImage = async (file) => {
  uploadingImage.value = true
  try {
    const res = await uploadImage(file.raw)
    const imageUrl = res.data
    const msgRes = await sendCsMessage({
      sessionId: currentSession.value.id,
      content: imageUrl,
      messageType: 'image',
      attachmentUrl: imageUrl
    })
    if (msgRes.data) {
      currentSession.value = msgRes.data
      messages.value = msgRes.data.messages || []
      imageFiles.value = []
      nextTick(() => scrollToBottom())
    }
  } catch (e) {
    ElMessage.error('图片发送失败')
  } finally {
    uploadingImage.value = false
  }
}

const closeCurrentSession = async () => {
  if (!currentSession.value) return
  try {
    await closeCsSession(currentSession.value.id)
    currentSession.value.status = 2
    ElMessage.success('会话已结束')
  } catch (e) {
    ElMessage.error('关闭会话失败')
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

onMounted(() => {
  // 组件挂载后不主动打开
})
</script>

<style scoped>
.cs-widget {
  position: fixed;
  z-index: 9999;
}

.cs-trigger {
  position: fixed;
  bottom: 24px;
  right: 24px;
  background: linear-gradient(135deg, #764ba2, #8b6fd4);
  color: #fff;
  padding: 12px 18px;
  border-radius: 24px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  box-shadow: 0 4px 16px rgba(103, 71, 199, 0.4);
  transition: all 0.3s;
}

.cs-trigger:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(103, 71, 199, 0.5);
}

.cs-badge {
  background: #f56c6c;
  color: #fff;
  border-radius: 10px;
  padding: 1px 6px;
  font-size: 11px;
  min-width: 18px;
  text-align: center;
}

.cs-panel {
  position: fixed;
  bottom: 80px;
  right: 24px;
  width: 380px;
  height: 520px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.cs-header {
  background: linear-gradient(135deg, #764ba2, #8b6fd4);
  color: #fff;
  padding: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.cs-header-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.cs-avatar {
  font-size: 28px;
}

.cs-title {
  font-size: 16px;
  font-weight: 700;
}

.cs-subtitle {
  font-size: 12px;
  opacity: 0.8;
}

.cs-header-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.cs-body {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* 会话列表 */
.cs-session-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  display: flex;
  flex-direction: column;
}

.cs-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  gap: 8px;
}

.cs-empty-sub {
  font-size: 12px;
  color: #bbb;
}

.cs-session-items {
  flex: 1;
  overflow-y: auto;
}

.cs-session-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  border-radius: 10px;
  cursor: pointer;
  margin-bottom: 6px;
  transition: background 0.2s;
}

.cs-session-item:hover, .cs-session-item.active {
  background: #f5f0ff;
}

.cs-si-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.cs-si-status {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.cs-si-status.wait { background: #ffc107; }
.cs-si-status.active { background: #67c23a; }
.cs-si-status.closed { background: #999; }

.cs-si-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.cs-si-subject {
  font-size: 13px;
  font-weight: 600;
  color: #333;
}

.cs-si-time {
  font-size: 11px;
  color: #999;
}

.cs-start-area {
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

/* 聊天界面 */
.cs-chat {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.cs-chat-header {
  padding: 12px 16px;
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.cs-chat-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.cs-chat-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
}

.cs-chat-status.status-0 { background: #fff7e6; color: #faad14; }
.cs-chat-status.status-1 { background: #e6f7ff; color: #1890ff; }
.cs-chat-status.status-2 { background: #f5f5f5; color: #999; }

.cs-messages {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cs-msg-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #bbb;
  font-size: 13px;
}

.cs-msg-item {
  display: flex;
  gap: 8px;
  align-items: flex-end;
}

.cs-msg-customer {
  flex-direction: row;
}

.cs-msg-operator {
  flex-direction: row-reverse;
}

.cs-msg-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
}

.customer-avatar {
  background: linear-gradient(135deg, #764ba2, #8b6fd4);
  color: #fff;
}

.operator-avatar {
  background: #f0f0f0;
  font-size: 18px;
}

.cs-msg-content {
  max-width: 70%;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.cs-msg-customer .cs-msg-content {
  align-items: flex-start;
}

.cs-msg-operator .cs-msg-content {
  align-items: flex-end;
}

.cs-msg-bubble {
  padding: 8px 12px;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.5;
  word-break: break-word;
}

.cs-msg-customer .cs-msg-bubble {
  background: #f0f0f0;
  color: #333;
  border-bottom-left-radius: 4px;
}

.cs-msg-operator .cs-msg-bubble {
  background: linear-gradient(135deg, #764ba2, #8b6fd4);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.cs-msg-time {
  font-size: 10px;
  color: #bbb;
}

.cs-input-area {
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.cs-closed {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #999;
  font-size: 13px;
}

.cs-msg-image {
  padding: 4px !important;
  background: transparent !important;
}

.cs-msg-operator .cs-msg-image {
  background: rgba(118, 75, 162, 0.1) !important;
}

.cs-msg-customer .cs-msg-image {
  background: #f0f0f0 !important;
}

.cs-input-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.cs-hint {
  font-size: 11px;
  color: #bbb;
}
</style>