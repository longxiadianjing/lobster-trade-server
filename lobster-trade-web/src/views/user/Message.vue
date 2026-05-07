<template>
  <PageLayout>
    <template #sidebar>
      <el-card class="menu-card" shadow="never" :body-style="{ padding: '0' }">
        <template #header>
          <span class="menu-title">💬 消息中心</span>
        </template>
        <el-menu :default-active="route.path" router @select="() => {}">
          <el-menu-item index="/user">
            <el-icon><User /></el-icon>
            <span>个人信息</span>
          </el-menu-item>
          <el-menu-item index="/user/message">
            <el-icon><ChatDotRound /></el-icon>
            <span>消息中心</span>
          </el-menu-item>
          <el-menu-item index="/wallet">
            <el-icon><Wallet /></el-icon>
            <span>我的钱包</span>
          </el-menu-item>
          <el-menu-item index="/order/center">
            <el-icon><List /></el-icon>
            <span>订单中心</span>
          </el-menu-item>
          <el-menu-item index="/user/favorites">
            <el-icon><Star /></el-icon>
            <span>我的收藏</span>
          </el-menu-item>
        </el-menu>
      </el-card>
    </template>

    <div class="message-content">
      <el-card class="main-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">📬 我的消息</span>
            <el-button v-if="totalUnread > 0" type="primary" plain size="small" @click="handleMarkAllRead" :loading="loading">
              全部标记已读
            </el-button>
          </div>
        </template>

        <!-- 未读提示条 -->
        <div class="unread-banner" v-if="totalUnread > 0">
          <el-icon><Bell /></el-icon>
          <span>您有 <strong>{{ totalUnread }}</strong> 条未读消息</span>
        </div>

        <!-- 分类Tab -->
        <el-tabs v-model="activeTab" class="msg-tabs" @tab-change="handleTabChange">
          <!-- 全部 -->
          <el-tab-pane name="all">
            <template #label>
              <span class="tab-label">
                <span>📋 全部</span>
                <el-badge :value="totalUnread" :hidden="!totalUnread" />
              </span>
            </template>
            <div class="msg-list" v-if="messageList.length > 0">
              <div
                v-for="msg in messageList"
                :key="msg.id"
                class="msg-item"
                :class="{ unread: msg.status === 0 }"
                @click="handleRead(msg)"
              >
                <div class="msg-icon-wrap" :class="getIconClass(msg.type)">
                  <el-icon><component :is="getIcon(msg.type)" /></el-icon>
                </div>
                <div class="msg-body">
                  <div class="msg-title-row">
                    <span class="msg-title">{{ msg.title }}</span>
                    <el-tag v-if="msg.status === 0" type="danger" size="small" effect="plain" class="unread-tag">未读</el-tag>
                  </div>
                  <div class="msg-content">{{ msg.content }}</div>
                  <div class="msg-footer">
                    <span class="msg-time">{{ formatTime(msg.createTime) }}</span>
                    <el-button v-if="msg.linkUrl" type="primary" link size="small" @click.stop="handleMsgAction(msg)">
                      查看详情
                    </el-button>
                  </div>
                </div>
                <div class="msg-actions">
                  <el-button v-if="msg.status === 0" type="primary" link size="small" @click.stop="handleMarkRead(msg)">
                    标为已读
                  </el-button>
                </div>
              </div>
            </div>
            <el-empty v-else-if="!loading" description="暂无消息" :image-size="80" />
            <div v-if="loading" class="loading-wrap">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>加载中...</span>
            </div>
          </el-tab-pane>

          <!-- 系统通知 -->
          <el-tab-pane name="system">
            <template #label>
              <span class="tab-label">
                <span>🔔 系统通知</span>
                <el-badge :value="unreadMap.system" :hidden="!unreadMap.system" />
              </span>
            </template>
            <div class="msg-list" v-if="messageList.length > 0">
              <div
                v-for="msg in messageList"
                :key="msg.id"
                class="msg-item"
                :class="{ unread: msg.status === 0 }"
                @click="handleRead(msg)"
              >
                <div class="msg-icon-wrap system-icon">
                  <el-icon><Bell /></el-icon>
                </div>
                <div class="msg-body">
                  <div class="msg-title-row">
                    <span class="msg-title">{{ msg.title }}</span>
                    <el-tag v-if="msg.status === 0" type="danger" size="small" effect="plain" class="unread-tag">未读</el-tag>
                  </div>
                  <div class="msg-content">{{ msg.content }}</div>
                  <div class="msg-footer">
                    <span class="msg-time">{{ formatTime(msg.createTime) }}</span>
                    <el-button v-if="msg.linkUrl" type="primary" link size="small" @click.stop="handleMsgAction(msg)">
                      查看详情
                    </el-button>
                  </div>
                </div>
                <div class="msg-actions">
                  <el-button v-if="msg.status === 0" type="primary" link size="small" @click.stop="handleMarkRead(msg)">
                    标为已读
                  </el-button>
                </div>
              </div>
            </div>
            <el-empty v-else-if="!loading" description="暂无系统通知" :image-size="80" />
            <div v-if="loading" class="loading-wrap">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>加载中...</span>
            </div>
          </el-tab-pane>

          <!-- 订单动态 -->
          <el-tab-pane name="order">
            <template #label>
              <span class="tab-label">
                <span>📦 订单动态</span>
                <el-badge :value="unreadMap.order" :hidden="!unreadMap.order" />
              </span>
            </template>
            <div class="msg-list" v-if="messageList.length > 0">
              <div
                v-for="msg in messageList"
                :key="msg.id"
                class="msg-item"
                :class="{ unread: msg.status === 0 }"
                @click="handleRead(msg)"
              >
                <div class="msg-icon-wrap order-icon">
                  <el-icon><ShoppingCart /></el-icon>
                </div>
                <div class="msg-body">
                  <div class="msg-title-row">
                    <span class="msg-title">{{ msg.title }}</span>
                    <el-tag v-if="msg.status === 0" type="danger" size="small" effect="plain" class="unread-tag">未读</el-tag>
                  </div>
                  <div class="msg-content">{{ msg.content }}</div>
                  <div class="msg-footer">
                    <span class="msg-time">{{ formatTime(msg.createTime) }}</span>
                    <el-button v-if="msg.linkUrl" type="primary" link size="small" @click.stop="handleMsgAction(msg)">
                      查看详情
                    </el-button>
                  </div>
                </div>
                <div class="msg-actions">
                  <el-button v-if="msg.status === 0" type="primary" link size="small" @click.stop="handleMarkRead(msg)">
                    标为已读
                  </el-button>
                </div>
              </div>
            </div>
            <el-empty v-else-if="!loading" description="暂无订单动态" :image-size="80" />
            <div v-if="loading" class="loading-wrap">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>加载中...</span>
            </div>
          </el-tab-pane>

          <!-- 客服消息 -->
          <el-tab-pane name="cs">
            <template #label>
              <span class="tab-label">
                <span>🎧 客服消息</span>
                <el-badge :value="unreadMap.cs" :hidden="!unreadMap.cs" />
              </span>
            </template>
            <div class="msg-list" v-if="messageList.length > 0">
              <div
                v-for="msg in messageList"
                :key="msg.id"
                class="msg-item"
                :class="{ unread: msg.status === 0 }"
                @click="handleRead(msg)"
              >
                <div class="msg-icon-wrap cs-icon">
                  <el-icon><Headset /></el-icon>
                </div>
                <div class="msg-body">
                  <div class="msg-title-row">
                    <span class="msg-title">{{ msg.title }}</span>
                    <el-tag v-if="msg.status === 0" type="danger" size="small" effect="plain" class="unread-tag">未读</el-tag>
                  </div>
                  <div class="msg-content">{{ msg.content }}</div>
                  <div class="msg-footer">
                    <span class="msg-time">{{ formatTime(msg.createTime) }}</span>
                    <el-button v-if="msg.linkUrl" type="primary" link size="small" @click.stop="handleMsgAction(msg)">
                      查看详情
                    </el-button>
                  </div>
                </div>
                <div class="msg-actions">
                  <el-button v-if="msg.status === 0" type="primary" link size="small" @click.stop="handleMarkRead(msg)">
                    标为已读
                  </el-button>
                </div>
              </div>
            </div>
            <el-empty v-else-if="!loading" description="暂无客服消息" :image-size="80" />
            <div v-if="loading" class="loading-wrap">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>加载中...</span>
            </div>
          </el-tab-pane>
        </el-tabs>

        <!-- 分页 -->
        <div class="pagination-wrap" v-if="total > 0 && !loading">
          <el-pagination
            background
            layout="prev, pager, next"
            :total="total"
            :page-size="pageSize"
            :current-page="currentPage"
            @current-change="handlePageChange"
          />
        </div>
      </el-card>
    </div>
  </PageLayout>
</template>

<script setup>
import { ref, computed, onMounted, markRaw } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageLayout from '@/components/PageLayout.vue'
import { User, Wallet, List, Star, ChatDotRound, Bell, ShoppingCart, Headset, Loading } from '@element-plus/icons-vue'
import { getNotificationList, markNotificationRead, markAllNotificationsRead, getUnreadCount } from '@/api/notification'

const router = useRouter()
const route = useRoute()
const activeTab = ref('all')
const loading = ref(false)
const messageList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const totalUnread = ref(0)
const allMessagesMap = ref({})

// 类型映射: 后端type(1=系统,2=订单,3=账户,4=活动) -> 前端category
const typeToCategory = (type) => {
  if (type === 1) return 'system'
  if (type === 2) return 'order'
  if (type === 3) return 'account'
  if (type === 4) return 'activity'
  return 'system'
}

const getIcon = (type) => {
  if (type === 1) return markRaw(Bell)
  if (type === 2) return markRaw(ShoppingCart)
  if (type === 3) return markRaw(Headset)
  return markRaw(Bell)
}

const getIconClass = (type) => {
  if (type === 1) return 'system-icon'
  if (type === 2) return 'order-icon'
  if (type === 3) return 'cs-icon'
  return 'system-icon'
}

const unreadMap = computed(() => {
  const all = allMessagesMap.value
  return {
    system: Object.values(all).filter(m => m.type === 1 && m.status === 0).length,
    order: Object.values(all).filter(m => m.type === 2 && m.status === 0).length,
    cs: Object.values(all).filter(m => m.type === 3 && m.status === 0).length,
  }
})

const formatTime = (time) => {
  if (!time) return ''
  let d
  if (typeof time === 'string') {
    d = new Date(time.replace('T', ' ').substring(0, 19))
  } else {
    d = new Date(time)
  }
  const now = new Date()
  const diff = now - d
  if (diff < 60 * 1000) return '刚刚'
  if (diff < 60 * 60 * 1000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 24 * 60 * 60 * 1000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 2 * 24 * 60 * 60 * 1000) return '昨天'
  if (diff < 7 * 24 * 60 * 60 * 1000) return `${Math.floor(diff / 86400000)}天前`
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const fetchMessages = async () => {
  loading.value = true
  try {
    const typeMap = { all: null, system: 1, order: 2, cs: 3 }
    const type = typeMap[activeTab.value] !== undefined ? typeMap[activeTab.value] : undefined
    const params = { page: currentPage.value, size: pageSize.value }
    if (type !== undefined) params.type = type

    const res = await getNotificationList(params)
    // 后端返回 {code:0, message:"success", data: {records:[], total:N}} → axios interceptor 返回 response.data
    messageList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error('fetchMessages error', e)
  } finally {
    loading.value = false
  }
}

const fetchUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    totalUnread.value = res || 0
  } catch (e) {
    console.error('fetchUnreadCount error', e)
  }
}

// 加载所有分类未读数（全量查一次后本地聚合）
const fetchAllMessagesForUnreadMap = async () => {
  try {
    const promises = [1, 2, 3].map(type =>
      getNotificationList({ type, page: 1, size: 100 }).catch(() => ({ records: [] }))
    )
    const results = await Promise.all(promises)
    const map = {}
    results.forEach(res => {
      ;(res.data?.records || []).forEach(m => { map[m.id] = m })
    })
    allMessagesMap.value = map
  } catch (e) {
    console.error('fetchAllMessagesForUnreadMap error', e)
  }
}

const handleRead = async (msg) => {
  if (msg.status === 0) {
    await handleMarkRead(msg)
  }
  if (msg.linkUrl) {
    router.push(msg.linkUrl)
  }
}

const handleMarkRead = async (msg) => {
  try {
    await markNotificationRead(msg.id)
    msg.status = 1
    totalUnread.value = Math.max(0, totalUnread.value - 1)
  } catch (e) {
    console.error(e)
  }
}

const handleMarkAllRead = async () => {
  try {
    await markAllNotificationsRead()
    totalUnread.value = 0
    messageList.value.forEach(m => { m.status = 1 })
    allMessagesMap.value = {}
    ElMessage.success('全部消息已标记为已读')
  } catch (e) {
    console.error(e)
  }
}

const handleMsgAction = (msg) => {
  if (msg.linkUrl) {
    router.push(msg.linkUrl)
  }
}

const handleTabChange = () => {
  currentPage.value = 1
  fetchMessages()
}

const handlePageChange = (page) => {
  currentPage.value = page
  fetchMessages()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => {
  document.title = '消息中心 - 龙虾道具交易平台'
  fetchMessages()
  fetchUnreadCount()
  fetchAllMessagesForUnreadMap()
})
</script>

<style scoped>
.message-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.menu-card {
  border-radius: 12px;
  border: none;
}

.menu-title {
  font-weight: 600;
  font-size: 14px;
}

.main-card {
  border-radius: 12px;
  border: none;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-weight: 600;
  font-size: 15px;
}

.unread-banner {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: linear-gradient(135deg, #fff0f0, #fff5f5);
  border: 1px solid #ffd0d0;
  border-radius: 8px;
  font-size: 13px;
  color: #f56c6c;
  margin-bottom: 12px;
}

.unread-banner strong {
  color: #f56c6c;
  font-weight: 700;
}

.msg-tabs :deep(.el-tabs__header) {
  margin-bottom: 12px;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

.msg-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.msg-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 16px;
  background: #fafafa;
  border-radius: 10px;
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.2s;
}

.msg-item:hover {
  background: #f5f0ff;
  border-color: #e8e0ff;
}

.msg-item.unread {
  background: #fff5f5;
  border-color: #ffd5d5;
}

.msg-icon-wrap {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.system-icon { background: linear-gradient(135deg, #fff3e0, #ffe0b2); color: #f56c6c; }
.order-icon { background: linear-gradient(135deg, #e3f2fd, #bbdefb); color: #1976d2; }
.cs-icon { background: linear-gradient(135deg, #e8f5e9, #c8e6c9); color: #4caf50; }

.msg-body {
  flex: 1;
  min-width: 0;
}

.msg-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.msg-title {
  font-size: 14px;
  font-weight: 700;
  color: #333;
}

.unread-tag {
  font-size: 10px;
  padding: 0 4px;
  height: 18px;
  line-height: 18px;
}

.msg-content {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.msg-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.msg-time {
  font-size: 12px;
  color: #bbb;
}

.msg-actions {
  flex-shrink: 0;
}

.loading-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 40px;
  color: #999;
  font-size: 14px;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #667eea, #7B5FD4) !important;
  color: #fff !important;
}

:deep(.el-tabs__item.is-active) {
  color: #764ba2 !important;
  font-weight: 700;
}

:deep(.el-tabs__active-bar) {
  background-color: #764ba2 !important;
}

:deep(.el-tabs__nav-wrap::after) {
  background-color: #f0f0f0 !important;
}
</style>