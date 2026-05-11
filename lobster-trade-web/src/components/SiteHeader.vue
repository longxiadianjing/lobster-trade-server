<template>
  <div class="site-header" :data-theme="currentTheme">
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
        <ThemeSwitcher />
        <el-divider direction="vertical" class="header-divider" />
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
import ThemeSwitcher from '@/components/ThemeSwitcher/index.vue'

const userStore = useUserStore()
const router = useRouter()
const currentTheme = ref('game')
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
  position: sticky;
  top: 0;
  z-index: 100;
  background: #1e3a5f;
  box-shadow: 0 2px 12px rgba(0,0,0,0.18);
  min-height: 60px;
}
.header-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 24px;
  height: 60px;
  display: flex;
  align-items: center;
  gap: 24px;
}
.logo {
  font-size: 20px;
  font-weight: 700;
  color: #ffffff;
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
}
.logo .logo-icon { font-size: 22px; }
.logo-text { color: #ffffff; letter-spacing: 1px; }
.header-center { flex: 1; max-width: 520px; }
.header-actions { display: flex; align-items: center; gap: 10px; margin-left: auto; }
.sell-btn {
  background: #e94560 !important;
  border: none !important;
  border-radius: 8px !important;
  color: #ffffff !important;
  font-weight: 600 !important;
  padding: 6px 16px !important;
  height: 34px;
}
.sell-btn:hover { background: #ff6b6b !important; }
.action-text-btn {
  color: rgba(255,255,255,0.85) !important;
  font-size: 14px;
  padding: 4px 8px;
  border: none;
  background: transparent;
  cursor: pointer;
}
.action-text-btn:hover { color: #ffffff !important; background: rgba(255,255,255,0.1); border-radius: 6px; }
.user-avatar-link { display: flex; align-items: center; text-decoration: none; }
.user-avatar-link :deep(.el-avatar) {
  background: #e94560;
  color: #ffffff;
  font-size: 15px;
  font-weight: 600;
  border: 2px solid rgba(255,255,255,0.3);
  cursor: pointer;
  transition: border-color 0.2s;
}
.user-avatar-link :deep(.el-avatar):hover { border-color: #e94560; }
.header-divider {
  width: 1px;
  height: 24px;
  background: rgba(255,255,255,0.2);
  margin: 0 4px;
}
.notif-badge :deep(.el-badge__content) { background: #e94560; }
</style>