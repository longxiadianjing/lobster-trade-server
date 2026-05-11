<template>
  <div class="im-container">
    <!-- 左侧：会话列表 -->
    <div class="im-sidebar">
      <div class="sidebar-header">
        <h3>我的消息</h3>
        <span class="total-unread" v-if="totalUnread > 0">{{ totalUnread > 99 ? '99+' : totalUnread }}</span>
      </div>
      <div class="session-list">
        <div v-if="loading" class="loading-tip">加载中...</div>
        <div v-else-if="sessions.length === 0" class="empty-tip">
          <p>暂无会话</p>
          <p class="sub">在订单中与对方沟通即可创建会话</p>
        </div>
        <div
          v-for="s in sessions"
          :key="s.id"
          class="session-item"
          :class="{ active: currentSession && currentSession.id === s.id }"
          @click="selectSession(s)"
        >
          <div class="session-avatar">
            <span class="role-tag" :class="s.roleTag">{{ s.roleTag === 'buyer' ? '买' : '卖' }}</span>
          </div>
          <div class="session-info">
            <div class="session-top">
              <span class="counterpart-name">{{ s.counterpartName }}</span>
              <span class="order-no">{{ s.orderNo }}</span>
            </div>
            <div class="session-bottom">
              <span class="last-msg">{{ s.lastMessage || '暂无消息' }}</span>
              <span class="unread-badge" v-if="s.myUnread > 0">{{ s.myUnread > 99 ? '99+' : s.myUnread }}</span>
            </div>
          </div>
          <div class="session-time">{{ formatTime(s.lastMessageAt) }}</div>
        </div>
      </div>
    </div>

    <!-- 右侧：聊天窗口 -->
    <div class="im-main" v-if="currentSession">
      <!-- 聊天头部 -->
      <div class="chat-header">
        <div class="chat-info">
          <span class="chat-title">{{ currentSession.counterpartName }}</span>
          <span class="order-tag">订单：{{ currentSession.orderNo }}</span>
          <span class="product-tag">{{ currentSession.productTitle }}</span>
        </div>
        <el-button size="small" @click="viewOrder">查看订单</el-button>
      </div>

      <!-- 消息列表 -->
      <div class="message-list" ref="msgListRef">
        <div v-if="messagesLoading" class="loading-tip">加载消息中...</div>
        <template v-else>
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="message-item"
            :class="msgClass(msg)"
          >
            <div class="msg-avatar">{{ msg.senderNickname?.slice(0, 1) || '系' }}</div>
            <div class="msg-body">
              <div class="msg-meta">
                <span class="msg-sender">{{ msg.senderNickname }}</span>
                <span class="msg-time">{{ formatTime(msg.createTime) }}</span>
              </div>
              <div class="msg-content" :class="msg.messageType">
                <span v-if="msg.messageType === 'system'" class="system-msg">{{ msg.content }}</span>
                <template v-else>{{ msg.content }}</template>
              </div>
            </div>
          </div>
        </template>
        <div v-if="messages.length === 0 && !messagesLoading" class="empty-msg">
          <p>📭 暂无消息</p>
          <p class="empty-sub">开始和{{ currentSession?.counterpartName }}聊聊吧</p>
        </div>
      </div>

      <!-- 消息输入 -->
      <div class="message-input">
        <el-input
          v-model="inputText"
          type="textarea"
          :rows="3"
          placeholder="输入消息..."
          @keydown.enter.exact.prevent="sendTextMessage"
        />
        <div class="input-footer">
          <span class="hint">按 Enter 发送，Shift+Enter 换行</span>
          <el-button type="primary" :disabled="!inputText.trim()" @click="sendTextMessage">发送</el-button>
        </div>
      </div>
    </div>

    <!-- 未选择会话 -->
    <div class="im-main im-empty" v-else>
      <div class="empty-state">
        <div class="empty-icon">💬</div>
        <p>选择一个会话开始聊天</p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getMySessions, getSession, getOrCreateSessionByOrder, sendMessage, markRead, getOrCreateSessionByProduct } from '@/api/im'
import { ElMessage } from 'element-plus'

export default {
  name: 'ImIndex',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const userStore = useUserStore()
    const sessions = ref([])
    const currentSession = ref(null)
    const messages = ref([])
    const loading = ref(false)
    const messagesLoading = ref(false)
    const inputText = ref('')
    const msgListRef = ref(null)
    const originalTitle = document.title
    const isTitleFlashing = ref(false)
    let imSource = null
    let sseToken = null

    const totalUnread = computed(() =>
      sessions.value.reduce((sum, s) => sum + (s.myUnread || 0), 0)
    )

    const loadSessions = async () => {
      loading.value = true
      try {
        const res = await getMySessions()
        if (res.code === 200 && res.data) {
          sessions.value = res.data.map(s => {
            const uId = parseInt(localStorage.getItem('userId') || userStore?.userInfo?.id || 0)
            const rTag = s.buyerId === uId ? 'buyer' : 'seller'
            const cId = s.buyerId === uId ? s.sellerId : s.buyerId
            const cName = s.buyerId === uId ? s.sellerNickname : s.buyerNickname
            return { ...s, roleTag: rTag, counterpartId: cId, counterpartName: cName }
          })
        }
      } catch (e) {
        console.error('loadSessions error', e)
      } finally {
        loading.value = false
      }
    }

    const selectSession = async (s) => {
      currentSession.value = s
      connectSSE(s.id)
      messagesLoading.value = true
      messages.value = []
      try {
        const res = await getSession(s.id)
        if (res.code === 200 && res.data) {
          messages.value = res.data.recentMessages || []
          await markRead(s.id)
          s.myUnread = 0
          await nextTick()
          scrollToBottom()
        }
      } catch (e) {
        console.error('selectSession error', e)
      } finally {
        messagesLoading.value = false
      }
    }

    const sendTextMessage = async () => {
      if (!inputText.value.trim() || !currentSession.value) return
      const content = inputText.value.trim()
      inputText.value = ''
      try {
        const res = await sendMessage({ sessionId: currentSession.value.id, content, messageType: 'text' })
        if (res.code === 200 && res.data) {
          messages.value = res.data.recentMessages || []
          await nextTick()
          scrollToBottom()
        } else {
          ElMessage.error(res.message || '发送失败')
        }
      } catch (e) {
        ElMessage.error('发送失败')
      }
    }

    const scrollToBottom = () => {
      if (msgListRef.value) {
        msgListRef.value.scrollTop = msgListRef.value.scrollHeight
      }
    }

    const msgClass = (msg) => {
      if (msg.senderRole === 'system') return 'msg-system'
      if (msg.senderRole === 'buyer') return 'msg-buyer'
      return 'msg-seller'
    }

    const formatTime = (time) => {
      if (!time) return ''
      const d = new Date(time)
      const now = new Date()
      const diff = now - d
      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
      if (diff < 86400000) return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
      return d.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
    }

    const viewOrder = () => {
      if (currentSession.value?.orderId) {
        router.push({ path: '/order/detail/' + currentSession.value.orderId })
      }
    }

    // 直接加载指定会话
    const loadSessionDirect = async (sessionId) => {
      try {
        const res = await getSession(sessionId)
        if (res.code === 200 && res.data) {
          const s = res.data
          const uId = parseInt(localStorage.getItem('userId') || userStore?.userInfo?.id || 0)
          const rTag = s.buyerId === uId ? 'buyer' : 'seller'
          const cName = s.buyerId === uId ? s.sellerNickname : s.buyerNickname
          currentSession.value = { ...s, roleTag: rTag, counterpartName: cName }
          messagesLoading.value = true
          messages.value = []
          messages.value = s.recentMessages || []
          messagesLoading.value = false
          await markRead(sessionId)
          await nextTick()
          scrollToBottom()
        }
      } catch (e) {
        console.error('loadSessionDirect error', e)
      }
    }

    // 用 sellerId 创建新会话（通过商品关联）
    const createSessionWithSeller = async (sellerId) => {
      try {
        const res = await getOrCreateSessionByProduct(sellerId)
        if (res.code === 200 && res.data?.id) {
          router.replace({ path: '/im', query: { sessionId: res.data.id } })
          await loadSessionDirect(res.data.id)
        } else {
          ElMessage.error(res.message || '创建会话失败')
        }
      } catch (e) {
        console.error('createSessionWithSeller error', e)
      }
    }

    // SSE实时推送
    const connectSSE = (sessionId) => {
      disconnectSSE()
      const token = localStorage.getItem('token') || ''
      const url = `/api/im/subscribe/${sessionId}?token=${encodeURIComponent(token)}`
      imSource = new EventSource(url)
      imSource.addEventListener('new_message', (e) => {
        try {
          const data = JSON.parse(e.data)
          const uId = parseInt(localStorage.getItem('userId') || userStore?.userInfo?.id || 0)
          if (data.senderId !== uId) {
            messages.value.push(data)
            nextTick(() => scrollToBottom())
          }
        } catch (ex) {
          console.error('SSE parse error', ex)
        }
      })
      imSource.onerror = () => {
        console.warn('SSE error, reconnecting...')
        setTimeout(() => connectSSE(sessionId), 3000)
      }
    }

    const disconnectSSE = () => {
      if (imSource) {
        imSource.close()
        imSource = null
      }
    }

    // 轮询新消息
    let pollTimer = null
    let oldMessagesLength = 0

    const startPolling = () => {
      pollTimer = setInterval(async () => {
        if (!currentSession.value) return
        try {
          const res = await getSession(currentSession.value.id)
          if (res.code === 200 && res.data) {
            const newLen = (res.data.recentMessages || []).length
            if (newLen > oldMessagesLength) {
              isTitleFlashing.value = true
              let flashCount = 0
              const flashInterval = setInterval(() => {
                document.title = flashCount % 2 === 0 ? '💬 您有新消息！' : originalTitle
                flashCount++
                if (flashCount >= 6) {
                  clearInterval(flashInterval)
                  document.title = originalTitle
                  isTitleFlashing.value = false
                }
              }, 800)
            }
            oldMessagesLength = newLen
            messages.value = res.data.recentMessages || []
            await nextTick()
            scrollToBottom()
          }
        } catch (e) { /* ignore */ }
      }, 5000)
    }

    const stopPolling = () => {
      if (pollTimer) {
        clearInterval(pollTimer)
        pollTimer = null
      }
    }

    onMounted(async () => {
      document.title = '消息中心 - 龙虾道具交易平台'
      const sessionId = route.query.sessionId
      const sellerId = route.query.sellerId

      if (sessionId) {
        await loadSessions()
        await loadSessionDirect(sessionId)
        startPolling()
        return
      } else if (sellerId) {
        await loadSessions()
        await createSessionWithSeller(sellerId)
        startPolling()
        return
      }

      await loadSessions()
      if (sessions.value.length > 0) {
        await selectSession(sessions.value[0])
      }
      startPolling()
    })

    onUnmounted(() => {
      stopPolling()
      disconnectSSE()
      document.title = originalTitle
    })

    return {
      sessions,
      currentSession,
      messages,
      loading,
      messagesLoading,
      inputText,
      msgListRef,
      totalUnread,
      selectSession,
      sendTextMessage,
      msgClass,
      formatTime,
      viewOrder
    }
  }
}
</script>

<style scoped>
.im-container {
  display: flex;
  height: calc(100vh - 120px);
  background: #f5f6f7;
}
.im-sidebar {
  width: 300px;
  background: #fff;
  border-right: 1px solid #eee;
  display: flex;
  flex-direction: column;
}
.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
}
.sidebar-header h3 { margin: 0; font-size: 16px; }
.total-unread {
  display: inline-block;
  background: #ff4d4f;
  color: #fff;
  border-radius: 10px;
  padding: 1px 7px;
  font-size: 11px;
  margin-left: 8px;
  vertical-align: middle;
}
.session-list { flex: 1; overflow-y: auto; }
.session-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid #f5f6f7;
  transition: background 0.2s;
}
.session-item:hover, .session-item.active { background: #f0f7ff; }
.session-item.active { border-left: 3px solid #3b82f6; }
.session-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: #3b82f6;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  flex-shrink: 0;
}
.role-tag { color: #fff; font-size: 14px; font-weight: 600; }
.session-info { flex: 1; min-width: 0; }
.session-top { display: flex; justify-content: space-between; align-items: center; }
.counterpart-name { font-weight: 600; font-size: 14px; color: #333; }
.order-no { font-size: 11px; color: #999; }
.session-bottom { display: flex; justify-content: space-between; margin-top: 4px; }
.last-msg { font-size: 12px; color: #888; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 140px; }
.unread-badge {
  background: #ff4d4f;
  color: #fff;
  border-radius: 10px;
  padding: 1px 6px;
  font-size: 11px;
  min-width: 18px;
  text-align: center;
}
.session-time { font-size: 11px; color: #bbb; margin-left: 8px; flex-shrink: 0; }

.im-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
}
.im-main.im-empty { align-items: center; justify-content: center; }
.empty-state { text-align: center; color: #999; }
.empty-icon { font-size: 48px; margin-bottom: 16px; }

.chat-header {
  padding: 12px 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.chat-info { display: flex; align-items: center; gap: 12px; }
.chat-title { font-weight: 600; font-size: 15px; }
.order-tag, .product-tag { font-size: 12px; color: #888; background: #f5f6f7; padding: 2px 8px; border-radius: 4px; }

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.message-item { display: flex; align-items: flex-start; }
.msg-buyer { flex-direction: row; }
.msg-seller { flex-direction: row-reverse; }
.msg-system { justify-content: center; }
.msg-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #3b82f6;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  flex-shrink: 0;
  margin: 0 10px;
}
.msg-buyer .msg-avatar { background: #3b82f6; }
.msg-seller .msg-avatar { background: #10b981; }
.msg-body { max-width: 70%; }
.msg-meta { display: flex; gap: 8px; align-items: center; margin-bottom: 4px; }
.msg-buyer .msg-meta { flex-direction: row; }
.msg-seller .msg-meta { flex-direction: row-reverse; }
.msg-sender { font-size: 12px; color: #666; font-weight: 500; }
.msg-time { font-size: 11px; color: #bbb; }
.msg-content {
  display: inline-block;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}
.msg-buyer .msg-content { background: #e8f0ff; color: #333; }
.msg-seller .msg-content { background: #f0fdf4; color: #333; }
.msg-system .msg-content { background: transparent; color: #aaa; font-size: 12px; font-style: italic; }
.system-msg { font-style: italic; color: #bbb; }
.empty-msg { text-align: center; color: #bbb; margin-top: 60px; font-size: 14px; }
.empty-sub { font-size: 12px; margin-top: 4px; color: #bbb; }

.message-input {
  border-top: 1px solid #eee;
  padding: 12px 20px;
}
.input-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 8px; }
.hint { font-size: 12px; color: #bbb; }
.loading-tip, .empty-tip { text-align: center; padding: 40px; color: #999; font-size: 14px; }
.empty-tip .sub { font-size: 12px; margin-top: 4px; }
</style>