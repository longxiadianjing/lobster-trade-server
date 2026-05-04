<template>
  <div class="admin-layout">
    <!-- 左侧导航 -->
    <aside class="admin-sidebar">
      <div class="sidebar-header">
        <span class="logo-text">🦞 管理后台</span>
      </div>
      <el-menu :default-active="activeMenu" @select="onMenuSelect" class="admin-menu">
        <el-menu-item index="dashboard">
          <el-icon><DataLine /></el-icon>
          <span>数据概览</span>
        </el-menu-item>
        <el-menu-item index="users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="products">
          <el-icon><Goods /></el-icon>
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="orders">
          <el-icon><List /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="disputes">
          <el-icon><Warning /></el-icon>
          <span>纠纷处理</span>
        </el-menu-item>
        <el-menu-item index="games">
          <el-icon><Guide /></el-icon>
          <span>游戏管理</span>
        </el-menu-item>
        <el-menu-item index="announcements">
          <el-icon><Bell /></el-icon>
          <span>公告管理</span>
        </el-menu-item>
        <el-menu-item index="coupons">
          <el-icon><Ticket /></el-icon>
          <span>优惠券管理</span>
        </el-menu-item>
        <el-menu-item index="hotsearch">
          <el-icon><TrendCharts /></el-icon>
          <span>热搜词管理</span>
        </el-menu-item>
        <el-menu-item index="certs">
          <el-icon><Document /></el-icon>
          <span>实名认证</span>
        </el-menu-item>
        <el-menu-item index="reviews">
          <el-icon><Star /></el-icon>
          <span>评价管理</span>
        </el-menu-item>
        <el-menu-item index="notifications">
          <el-icon><Message /></el-icon>
          <span>系统通知</span>
        </el-menu-item>
        <el-menu-item index="tickets">
          <el-icon><Tickets /></el-icon>
          <span>工单管理</span>
        </el-menu-item>
        <el-menu-item index="cs">
          <el-icon><ChatDotRound /></el-icon>
          <span>客服会话</span>
        </el-menu-item>
        <el-menu-item index="recslots">
          <el-icon><Goods /></el-icon>
          <span>推荐位管理</span>
        </el-menu-item>
        <el-menu-item index="audit">
          <el-icon><CopyDocument /></el-icon>
          <span>审计日志</span>
        </el-menu-item>
        <el-menu-item index="admins">
          <el-icon><Setting /></el-icon>
          <span>管理员管理</span>
        </el-menu-item>
      </el-menu>
      <div class="sidebar-footer">
        <el-button text @click="handleLogout" class="logout-btn">
          <el-icon><SwitchButton /></el-icon>
          退出登录
        </el-button>
      </div>
    </aside>

    <!-- 右侧内容 -->
    <div class="admin-main">
      <!-- 顶部 -->
      <header class="admin-header">
        <div class="header-left">
          <span class="page-title">{{ pageTitle }}</span>
        </div>
        <div class="header-right">
          <span class="admin-name">{{ adminInfo?.username || '' }}</span>
          <el-avatar :size="32">{{ (adminInfo?.username || 'A').charAt(0) }}</el-avatar>
        </div>
      </header>

      <!-- 内容区 -->
      <main class="admin-content">
        <component :is="currentComponent" @navigate="onMenuSelect" />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  DataLine, User, Goods, List, Warning, Bell, SwitchButton,
  Ticket, Stamp, Star, Message, Tickets, ChatDotRound, Document,
  TrendCharts, Guide, CopyDocument, Setting
} from '@element-plus/icons-vue'
import { getAdminInfo, adminLogin } from '@/api/admin'
import Dashboard from './Dashboard.vue'
import UserManage from './UserManage.vue'
import ProductManage from './ProductManage.vue'
import OrderManage from './OrderManage.vue'
import DisputeManage from './DisputeManage.vue'
import GameManage from './GameManage.vue'
import AnnouncementManage from './AnnouncementManage.vue'
import CouponManage from './CouponManage.vue'
import HotSearchManage from './HotSearchManage.vue'
import CertManage from './CertManage.vue'
import ReviewManage from './ReviewManage.vue'
import NotificationManage from './NotificationManage.vue'
import TicketManage from './TicketManage.vue'
import CsManage from './CsManage.vue'
import AuditLog from './AuditLog.vue'
import RecSlotManage from './RecSlotManage.vue'
import AdminManage from './AdminManage.vue'

const router = useRouter()
const activeMenu = ref('dashboard')
const adminInfo = ref(null)

const pageTitleMap = {
  dashboard: '数据概览',
  users: '用户管理',
  products: '商品管理',
  orders: '订单管理',
  disputes: '纠纷处理',
  games: '游戏管理',
  announcements: '公告管理',
  coupons: '优惠券管理',
  hotsearch: '热搜词管理',
  certs: '实名认证管理',
  reviews: '评价管理',
  notifications: '系统通知',
  tickets: '工单管理',
  cs: '客服会话',
  audit: '审计日志',
  recslots: '推荐位管理',
  admins: '管理员管理',
}
const pageTitle = computed(() => pageTitleMap[activeMenu.value] || '')

const componentMap = {
  dashboard: Dashboard,
  users: UserManage,
  products: ProductManage,
  orders: OrderManage,
  disputes: DisputeManage,
  games: GameManage,
  announcements: AnnouncementManage,
  coupons: CouponManage,
  hotsearch: HotSearchManage,
  certs: CertManage,
  reviews: ReviewManage,
  notifications: NotificationManage,
  tickets: TicketManage,
  cs: CsManage,
  audit: AuditLog,
  recslots: RecSlotManage,
  admins: AdminManage,
}
const currentComponent = computed(() => componentMap[activeMenu.value] || Dashboard)

const onMenuSelect = (idx) => { activeMenu.value = idx }

const handleLogout = () => {
  localStorage.removeItem('adminToken')
  router.push({ path: '/login' })
}

onMounted(async () => {
  const token = localStorage.getItem('adminToken')
  if (!token) {
    router.push({ path: '/login' })
    return
  }
  try {
    const res = await getAdminInfo()
    if (res.data) adminInfo.value = res.data
  } catch (e) {
    localStorage.removeItem('adminToken')
    router.push({ path: '/login' })
  }

  document.title = '管理后台 - 龙虾道具交易平台'
})
</script>

<style scoped>
.admin-layout { display: flex; height: 100vh; background: #f0f2f5; }
.admin-sidebar { width: 220px; background: #1a1a2e; display: flex; flex-direction: column; flex-shrink: 0; }
.sidebar-header { padding: 20px 16px; border-bottom: 1px solid rgba(255,255,255,0.1); }
.logo-text { font-size: 18px; font-weight: 700; color: #fff; }
.admin-menu { flex: 1; background: transparent; border: none; }
.admin-menu :deep(.el-menu-item) { color: rgba(255,255,255,0.7); }
.admin-menu :deep(.el-menu-item.is-active) { background: #667eea; color: #fff; }
.admin-menu :deep(.el-menu-item:hover) { background: rgba(102,126,234,0.3); color: #fff; }
.sidebar-footer { padding: 16px; border-top: 1px solid rgba(255,255,255,0.1); }
.logout-btn { color: rgba(255,255,255,0.5); width: 100%; justify-content: flex-start; }
.logout-btn:hover { color: #fff; }
.admin-main { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.admin-header { background: #fff; padding: 0 24px; height: 60px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.page-title { font-size: 16px; font-weight: 600; color: #333; }
.header-right { display: flex; align-items: center; gap: 10px; }
.admin-name { font-size: 14px; color: #666; }
.admin-content { flex: 1; overflow: auto; padding: 20px 24px; }
</style>