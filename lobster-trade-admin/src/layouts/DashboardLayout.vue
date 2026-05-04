<template>
  <div class="layout">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="logo">
        <span class="logo-icon">🦞</span>
        <span class="logo-text">龙虾后台</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        router
        background-color="#1a1a2e"
        text-color="#999"
        active-text-color="#fff"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataLine /></el-icon>
          <span>数据看板</span>
        </el-menu-item>
        <el-menu-item index="/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/orders">
          <el-icon><List /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/products">
          <el-icon><Goods /></el-icon>
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/disputes">
          <el-icon><Warning /></el-icon>
          <span>仲裁管理</span>
        </el-menu-item>
        <el-menu-item index="/games">
          <el-icon><Monitor /></el-icon>
          <span>游戏管理</span>
        </el-menu-item>
        <el-menu-item index="/order-input">
          <el-icon><DocumentAdd /></el-icon>
          <span>手动录单</span>
        </el-menu-item>
        <el-menu-item index="/certifications">
          <el-icon><Medal /></el-icon>
          <span>服务商认证</span>
        </el-menu-item>
        <el-menu-item index="/hot-search">
          <el-icon><TrendCharts /></el-icon>
          <span>热搜词</span>
        </el-menu-item>
        <el-menu-item index="/recommend-slot">
          <el-icon><Goods /></el-icon>
          <span>推荐位</span>
        </el-menu-item>
        <el-menu-item index="/announcements">
          <el-icon><Bell /></el-icon>
          <span>公告管理</span>
        </el-menu-item>
        <el-menu-item index="/notifications">
          <el-icon><MessageBox /></el-icon>
          <span>通知管理</span>
        </el-menu-item>
        <el-menu-item index="/tickets">
          <el-icon><Ticket /></el-icon>
          <span>工单管理</span>
        </el-menu-item>
        <el-menu-item index="/customer-service">
          <el-icon><ChatDotRound /></el-icon>
          <span>客服会话</span>
        </el-menu-item>
        <el-menu-item index="/reviews">
          <el-icon><Star /></el-icon>
          <span>评价管理</span>
        </el-menu-item>
        <el-menu-item index="/export">
          <el-icon><Download /></el-icon>
          <span>数据导出</span>
        </el-menu-item>
        <el-menu-item index="/real-name">
          <el-icon><Postcard /></el-icon>
          <span>实名认证</span>
        </el-menu-item>
        <el-menu-item index="/audit-log">
          <el-icon><Document /></el-icon>
          <span>审计日志</span>
        </el-menu-item>
        <el-menu-item index="/coupon">
          <el-icon><Ticket /></el-icon>
          <span>优惠券管理</span>
        </el-menu-item>
        <el-menu-item index="/admin-manage">
          <el-icon><Key /></el-icon>
          <span>管理员权限</span>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-footer">
        <div class="footer-top">
          <div class="admin-info">
            <el-avatar :size="28">{{ adminStore.info?.username?.charAt(0) || 'A' }}</el-avatar>
            <span class="admin-name">{{ adminStore.info?.username || '管理员' }}</span>
          </div>
          <el-button text size="small" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            退出
          </el-button>
        </div>
      </div>
    </aside>

    <!-- 右侧内容 -->
    <div class="main">
      <!-- 顶部导航 -->
      <header class="topbar">
        <div class="breadcrumb">
          <span class="page-title">{{ pageTitle }}</span>
        </div>
        <div class="topbar-right">
          <span class="time">{{ currentTime }}</span>
        </div>
      </header>

      <!-- 页面内容 -->
      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/admin'
import { DataLine, User, List, Goods, Warning, Monitor, SwitchButton, TrendCharts, Bell, MessageBox, Ticket, Star, Download, Postcard, Document, Key } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const adminStore = useAdminStore()

const currentTime = ref('')
let timer = null

const activeMenu = computed(() => route.path)
const pageTitle = computed(() => {
  const map = {
    '/dashboard': '数据看板',
    '/users': '用户管理',
    '/orders': '订单管理',
    '/products': '商品管理',
    '/disputes': '仲裁管理',
    '/games': '游戏管理'
  }
  return map[route.path] || ''
})

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
}

const handleLogout = () => {
  adminStore.logout()
  router.push('/login')
}

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  clearInterval(timer)
})
</script>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  width: 220px;
  background: #1a1a2e;
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 100;
  overflow: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 20px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}

.logo-icon {
  font-size: 24px;
}

.logo-text {
  font-size: 16px;
  font-weight: 700;
  color: #fff;
}

.el-menu {
  flex: 1;
  border: none;
  overflow-y: auto;
  min-height: 0;
}

.el-menu-item {
  height: 50px;
  line-height: 50px;
  font-size: 14px;
}

.el-menu-item.is-active {
  background: rgba(255,255,255,0.1) !important;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255,255,255,0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
}

.footer-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.admin-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.admin-name {
  font-size: 13px;
  color: #aaa;
}

.main {
  flex: 1;
  margin-left: 220px;
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.topbar {
  height: 56px;
  background: #fff;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 50;
}

.breadcrumb {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.topbar-right {
  font-size: 13px;
  color: #999;
}

.content {
  padding: 24px;
  flex: 1;
  overflow-y: auto;
}
</style>