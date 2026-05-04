<template>
  <div class="site-header">
    <div class="header-inner">
      <router-link to="/home" class="logo">
        <span class="logo-icon">🦞</span>
        <span class="logo-text">龙虾道具交易</span>
      </router-link>

      <div class="header-center">
        <slot name="center" />
      </div>

      <div class="header-actions">
        <slot name="actions" />
        <template v-if="userStore.isLoggedIn">
          <el-button @click="router.push({ path: '/product/publish' })" class="sell-btn" type="primary" size="small">
            <el-icon><Sell /></el-icon>
            我要卖
          </el-button>
          <el-button @click="router.push({ path: '/wallet' })" text class="action-text-btn">我的钱包</el-button>
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="notif-badge">
            <el-button text class="notif-btn" @click="router.push({ path: '/user/message' })">
              <el-icon :size="20"><Bell /></el-icon>
            </el-button>
          </el-badge>
          <router-link to="/user" class="user-avatar-link">
            <el-avatar :size="32">{{ userStore.nickname?.charAt(0) || '我' }}</el-avatar>
          </router-link>
        </template>
        <template v-else>
          <el-button @click="router.push({ path: '/login' })" text class="action-text-btn">登录</el-button>
          <el-button @click="router.push({ path: '/register' })" type="primary" size="small">免费入驻</el-button>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { getUnreadCount } from '@/api/notification'
import { Sell, Bell } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()
const unreadCount = ref(0)
let pollTimer = null

const fetchUnread = async () => {
  if (!userStore.isLoggedIn) return
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data || 0
  } catch (e) {}
}

onMounted(() => {
  fetchUnread()
  pollTimer = setInterval(fetchUnread, 30000)
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<style scoped>
.site-header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  box-shadow: 0 2px 12px rgba(102, 126, 234, 0.25);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  flex-shrink: 0;
}

.logo-icon {
  font-size: 24px;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 0.5px;
}

.header-center {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.sell-btn {
  background: rgba(255,255,255,0.15) !important;
  border: 1px solid rgba(255,255,255,0.3) !important;
  color: #fff !important;
  border-radius: 20px !important;
  font-weight: 600 !important;
}

.sell-btn:hover {
  background: rgba(255,255,255,0.25) !important;
}

.action-text-btn {
  color: rgba(255,255,255,0.85) !important;
  font-size: 14px !important;
}

.action-text-btn:hover {
  color: #fff !important;
}

.user-avatar-link {
  display: flex;
  align-items: center;
  margin-left: 4px;
}

.notif-badge {
  margin: 0 4px;
}

.notif-btn {
  color: rgba(255,255,255,0.85) !important;
  padding: 4px 6px !important;
  border-radius: 8px !important;
}

.notif-btn:hover {
  color: #fff !important;
  background: rgba(255,255,255,0.15) !important;
}

:deep(.el-avatar) {
  border: 2px solid rgba(255,255,255,0.5);
}

:deep(.el-button--primary) {
  background: #fff !important;
  color: #667eea !important;
  border: none !important;
  font-weight: 600 !important;
}

:deep(.el-button--primary:hover) {
  background: rgba(255,255,255,0.9) !important;
}
</style>