<template>
  <div class="cs-admin">
    <el-card shadow="never">
      <template #header>
        <div class="card-header-flex">
          <span class="card-title">客服会话管理</span>
          <el-tag type="info">实时聊天 · 仅展示进行中会话</el-tag>
        </div>
      </template>

      <div class="filter-row">
        <el-input v-model="filters.keyword" placeholder="搜索会话编号 / 客户ID" style="width:220px" clearable @keyup.enter="loadSessions" />
        <el-select v-model="filters.status" placeholder="状态" style="width:140px" clearable @change="loadSessions">
          <el-option label="等待中" :value="0" />
          <el-option label="进行中" :value="1" />
          <el-option label="已关闭" :value="2" />
        </el-select>
        <el-button type="primary" @click="loadSessions">搜索</el-button>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-row">
        <el-statistic v-for="s in statsData" :key="s.label" :title="s.label" :value="s.value" :value-style="{ fontSize: '18px' }" />
      </div>

      <el-table :data="sessions" v-loading="loading" stripe class="mt-16" @row-click="openChat">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="sessionNo" label="会话编号" width="150" />
        <el-table-column prop="customerNickname" label="客户" width="120">
          <template #default="{ row }">
            <span>{{ row.customerNickname || '用户' + row.customerId }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="subject" label="主题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="priority" label="优先级" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.priority === 1" type="danger" size="small">紧急</el-tag>
            <span v-else class="text-muted">普通</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operatorNickname" label="客服" width="100">
          <template #default="{ row }">
            {{ row.operatorNickname || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click.stop="openChat(row)">接入</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="loadSessions"
        class="mt-16"
      />
    </el-card>

    <!-- 聊天弹窗 -->
    <el-dialog v-model="chatVisible" :title="'会话: ' + (currentSession?.sessionNo || '')" width="720px" :close-on-click-modal="false" destroy-on-close>
      <div v-if="currentSession" class="chat-dialog">
        <div class="chat-sidebar">
          <div class="chat-info">
            <div class="ci-row"><label>客户：</label><span>{{ currentSession.customerNickname || '用户' + currentSession.customerId }}</span></div>
            <div class="ci-row"><label>主题：</label><span>{{ currentSession.subject || '-' }}</span></div>
            <div class="ci-row"><label>状态：</label><el-tag :type="statusTag(currentSession.status)" size="small">{{ statusText(currentSession.status) }}</el-tag></div>
          </div>
          <div class="assign-area" v-if="currentSession.status !== 2">
            <el-select v-if="!currentSession.operatorId" v-model="selectedOperatorId" placeholder="分配给自己" size="small" style="width:160px">
              <el-option label="我接入" :value="1" />
            </el-select>
            <el-tag v-else type="success" size="small">已由 {{ currentSession.operatorNickname }} 处理</el-tag>
          </div>
        </div>

        <!-- 消息列表 -->
        <div class="chat-messages" ref="chatMessagesRef">
          <div v-if="messages.length === 0" class="msg-empty">暂无消息</div>
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="msg-item"
            :class="msg.senderType === 1 ? 'msg-operator' : 'msg-customer'"
          >
            <div v-if="msg.senderType === 0" class="msg-avatar customer-avatar">
              {{ (currentSession.customerNickname || '客').charAt(0) }}
            </div>
            <div class="msg-content">
              <div class="msg-bubble">{{ msg.content }}</div>
              <div class="msg-time">{{ formatTime(msg.createTime) }}</div>
            </div>
            <div v-if="msg.senderType === 1" class="msg-avatar operator-avatar">🦞</div>
          </div>
        </div>

        <!-- 输入区 -->
        <div class="chat-input-area" v-if="currentSession.status !== 2">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="3"
            resize="none"
            placeholder="输入回复内容..."
            @keydown.enter.ctrl="doSendMessage"
          />
          <div class="chat-actions">
            <span class="hint">Ctrl+Enter 发送</span>
            <el-button type="primary" :disabled="!inputMessage.trim()" :loading="sending" @click="doSendMessage">发送</el-button>
          </div>
        </div>
        <div v-else class="chat-input-area chat-closed">
          <span>会话已关闭</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const sending = ref(false)
const sessions = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const chatVisible = ref(false)
const currentSession = ref(null)
const messages = ref([])
const inputMessage = ref('')
const chatMessagesRef = ref(null)
const selectedOperatorId = ref(null)
const statsData = ref([
  { label: '总数', value: 0 },
  { label: '等待中', value: 0 },
  { label: '进行中', value: 0 },
  { label: '已关闭', value: 0 }
])
const filters = reactive({ keyword: '', status: '' })

const statusText = (v) => ['等待中', '进行中', '已关闭'][v] || '-'
const statusTag = (v) => ['info', 'primary', ''][v] || ''
const formatTime = (t) => {
  if (!t) return ''
  const d = new Date(t)
  return d.toLocaleString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

const loadStats = async () => {
  try {
    const res = await request.get('/admin/ticket/stats')
    // reuse stats from service_ticket — cs sessions are separate
    // for cs sessions, derive from list
  } catch (e) { /* silent */ }
}

const loadSessions = async () => {
  loading.value = true
  try {
    const params = { page: currentPage.value, size: pageSize.value }
    if (filters.status !== '') params.status = filters.status
    const res = await request.get('/admin/cs/sessions', { params })
    sessions.value = res.data?.records || []
    total.value = res.data?.total || 0

    const all = sessions.value
    statsData.value = [
      { label: '总数', value: res.data?.total || all.length },
      { label: '等待中', value: all.filter(s => s.status === 0).length },
      { label: '进行中', value: all.filter(s => s.status === 1).length },
      { label: '已关闭', value: all.filter(s => s.status === 2).length }
    ]
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const openChat = async (row) => {
  currentSession.value = row
  messages.value = []
  chatVisible.value = true
  selectedOperatorId.value = row.operatorId
  // poll messages every 3s
  await loadCsSession(row.id)
}

const loadCsSession = async (sessionId) => {
  try {
    const res = await request.get(`/admin/cs/session/${sessionId}`)
    if (res.data) {
      currentSession.value = res.data
      messages.value = res.data.messages || []
      scrollToBottom()
    }
  } catch (e) { /* silent */ }
}

const doSendMessage = async () => {
  if (!inputMessage.value.trim() || !currentSession.value) return
  sending.value = true
  try {
    await request.post('/admin/cs/message', {
      sessionId: currentSession.value.id,
      content: inputMessage.value.trim(),
      messageType: 'text'
    })
    inputMessage.value = ''
    await loadCsSession(currentSession.value.id)
  } catch (e) {
    ElMessage.error('发送失败')
  } finally {
    sending.value = false
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatMessagesRef.value) {
      chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
    }
  })
}

let pollTimer = null
const startPolling = () => {
  pollTimer = setInterval(async () => {
    if (chatVisible.value && currentSession.value && currentSession.value.status !== 2) {
      await loadCsSession(currentSession.value.id)
    }
  }, 3000)
}
const stopPolling = () => { if (pollTimer) clearInterval(pollTimer) }

onMounted(() => {
  loadSessions()
  startPolling()
})
</script>

<style scoped>
.cs-admin { padding: 0; }
.card-header-flex { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-size: 15px; font-weight: 600; }
.filter-row { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; margin-bottom: 12px; }
.stats-row { display: flex; gap: 32px; padding: 12px 0; border-top: 1px solid #f0f0f0; border-bottom: 1px solid #f0f0f0; margin-bottom: 4px; }
.mt-16 { margin-top: 16px; }
.text-muted { color: #999; font-size: 13px; }
.chat-dialog { display: flex; flex-direction: column; height: 520px; }
.chat-sidebar { padding-bottom: 10px; border-bottom: 1px solid #f0f0f0; display: flex; align-items: center; justify-content: space-between; }
.chat-info { display: flex; gap: 16px; font-size: 13px; }
.ci-row { display: flex; gap: 4px; align-items: center; }
.ci-row label { color: #888; }
.assign-area { display: flex; gap: 8px; align-items: center; }
.chat-messages { flex: 1; overflow-y: auto; padding: 12px 0; display: flex; flex-direction: column; gap: 10px; min-height: 300px; }
.msg-empty { flex: 1; display: flex; align-items: center; justify-content: center; color: #bbb; font-size: 13px; }
.msg-item { display: flex; gap: 8px; align-items: flex-end; }
.msg-customer { flex-direction: row; }
.msg-operator { flex-direction: row-reverse; }
.msg-avatar { width: 28px; height: 28px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 700; flex-shrink: 0; }
.customer-avatar { background: linear-gradient(135deg, #6747C7, #8b6fd4); color: #fff; }
.operator-avatar { background: #f0f0f0; font-size: 16px; }
.msg-content { max-width: 70%; display: flex; flex-direction: column; gap: 2px; }
.msg-customer .msg-content { align-items: flex-start; }
.msg-operator .msg-content { align-items: flex-end; }
.msg-bubble { padding: 7px 12px; border-radius: 10px; font-size: 13px; line-height: 1.5; word-break: break-word; }
.msg-customer .msg-bubble { background: #f0f0f0; color: #333; border-bottom-left-radius: 3px; }
.msg-operator .msg-bubble { background: linear-gradient(135deg, #6747C7, #8b6fd4); color: #fff; border-bottom-right-radius: 3px; }
.msg-time { font-size: 10px; color: #bbb; }
.chat-input-area { padding-top: 10px; border-top: 1px solid #f0f0f0; display: flex; flex-direction: column; gap: 8px; }
.chat-closed { display: flex; align-items: center; justify-content: center; color: #999; height: 60px; }
.chat-actions { display: flex; align-items: center; justify-content: space-between; }
.hint { font-size: 11px; color: #bbb; }
</style>
