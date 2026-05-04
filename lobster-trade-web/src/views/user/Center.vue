<template>
  <PageLayout>
    <template #sidebar>
      <div class="user-sidebar">
        <!-- 头像卡片 -->
        <el-card class="profile-card" shadow="never" :body-style="{ padding: '20px' }">
          <div class="profile-avatar">
            <el-avatar :size="56" class="avatar">{{ userInfo?.nickname?.charAt(0) || 'U' }}</el-avatar>
            <div class="profile-info">
              <div class="profile-nickname">{{ userInfo?.nickname || '用户' }}</div>
              <div class="profile-level">LV{{ userInfo?.user_level || 1 }} 玩家</div>
            </div>
          </div>
          <div class="profile-stats">
            <div class="stat-item">
              <span class="stat-value">{{ userInfo?.totalOrders || 0 }}</span>
              <span class="stat-label">交易次数</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-value">{{ userInfo?.creditScore || 100 }}</span>
              <span class="stat-label">信用评分</span>
            </div>
          </div>
        </el-card>

        <!-- 角色切换 -->
        <el-card class="role-card" shadow="never" :body-style="{ padding: '12px' }">
          <div class="role-tabs">
            <div
              class="role-tab"
              :class="{ active: currentRole === 'buyer' }"
              @click="switchRole('buyer')"
            >
              <span class="role-icon">🛒</span>
              <span>我是买家</span>
            </div>
            <div
              class="role-tab"
              :class="{ active: currentRole === 'seller' }"
              @click="switchRole('seller')"
            >
              <span class="role-icon">💰</span>
              <span>我是卖家</span>
            </div>
          </div>
        </el-card>

        <!-- 导航菜单 -->
        <el-card class="nav-card" shadow="never" :body-style="{ padding: '8px' }">
          <el-menu :default-active="activeNav" router @select="activeNav = $event">
            <el-menu-item index="/order/center" v-if="currentRole === 'buyer'">
              <el-icon><List /></el-icon>
              <span>我的订单</span>
            </el-menu-item>
            <el-menu-item index="/order/center" v-if="currentRole === 'seller'">
              <el-icon><List /></el-icon>
              <span>销售订单</span>
            </el-menu-item>
            <el-menu-item index="/product/list">
              <el-icon><Goods /></el-icon>
              <span>商品列表</span>
            </el-menu-item>
            <el-menu-item index="/product/publish">
              <el-icon><Sell /></el-icon>
              <span>发布商品</span>
            </el-menu-item>
            <el-menu-item index="/user/coupons">
              <el-icon><Ticket /></el-icon>
              <span>我的优惠券</span>
            </el-menu-item>
            <el-menu-item index="/cs/history">
              <el-icon><ChatDotRound /></el-icon>
              <span>客服记录</span>
            </el-menu-item>
          </el-menu>
        </el-card>
      </div>
    </template>

    <div class="uc-content">
      <!-- 快捷操作 -->
      <div class="quick-actions">
        <div class="action-card" @click="router.push({ path: '/order/center', query: { status: 'pending_pay' } })">
          <div class="action-icon unpaid">⏰</div>
          <div class="action-info">
            <span class="action-count">{{ stats.waitPay || 0 }}</span>
            <span class="action-label">待付款</span>
          </div>
        </div>
        <div class="action-card" @click="router.push({ path: '/order/center', query: { status: 'pending_confirm' } })">
          <div class="action-icon confirm">📦</div>
          <div class="action-info">
            <span class="action-count">{{ stats.waitConfirm || 0 }}</span>
            <span class="action-label">待发货/确认</span>
          </div>
        </div>
        <div class="action-card" @click="router.push({ path: '/order/center', query: { status: 'completed' } })">
          <div class="action-icon completed">✅</div>
          <div class="action-info">
            <span class="action-count">{{ stats.completed || 0 }}</span>
            <span class="action-label">已完成</span>
          </div>
        </div>
        <div class="action-card" @click="router.push({ path: '/product/list' })">
          <div class="action-icon favorited">❤️</div>
          <div class="action-info">
            <span class="action-count">{{ stats.favorites || 0 }}</span>
            <span class="action-label">我的收藏</span>
          </div>
        </div>
      </div>

      <!-- 核心指标卡片 -->
      <el-card class="stat-cards-card" shadow="never">
        <template #header>
          <span class="card-title">📊 核心指标</span>
        </template>
        <div class="stat-cards-grid">
          <div class="stat-card-item orders" @click="router.push({ path: '/order/center' })">
            <div class="stat-card-icon">📦</div>
            <div class="stat-card-info">
              <div class="stat-card-value">{{ coreStats.totalOrders }}</div>
              <div class="stat-card-label">总订单数</div>
            </div>
          </div>
          <div class="stat-card-item revenue" @click="router.push({ path: '/wallet' })">
            <div class="stat-card-icon">💰</div>
            <div class="stat-card-info">
              <div class="stat-card-value">¥{{ coreStats.totalRevenue }}</div>
              <div class="stat-card-label">累计交易额</div>
            </div>
          </div>
          <div class="stat-card-item rating" @click="router.push({ path: '/order/center' })">
            <div class="stat-card-icon">⭐</div>
            <div class="stat-card-info">
              <div class="stat-card-value">{{ coreStats.goodRate }}</div>
              <div class="stat-card-label">好评率</div>
            </div>
          </div>
          <div class="stat-card-item hot-game" @click="router.push({ path: '/product/list' })">
            <div class="stat-card-icon">🎮</div>
            <div class="stat-card-info">
              <div class="stat-card-value">{{ coreStats.hotGame }}</div>
              <div class="stat-card-label">热门游戏</div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 用户信息 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <span class="card-title">📋 账户信息</span>
        </template>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">手机号</span>
            <span class="info-value">{{ userInfo?.phone || '未绑定' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">注册时间</span>
            <span class="info-value">{{ formatDate(userInfo?.createTime) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">认证状态</span>
            <span class="info-value">
              <el-tag :type="userInfo?.realNameVerified ? 'success' : 'warning'" size="small">
                {{ userInfo?.realNameVerified ? '已实名' : '未实名' }}
              </el-tag>
            </span>
          </div>
          <div class="info-item">
            <span class="info-label">账户状态</span>
            <span class="info-value">
              <el-tag :type="userInfo?.status === 1 ? 'success' : 'danger'" size="small">
                {{ userInfo?.status === 1 ? '正常' : '已封禁' }}
              </el-tag>
            </span>
          </div>
        </div>
      </el-card>

      <!-- 快速入口 -->
      <el-card class="entry-card" shadow="never">
        <template #header>
          <span class="card-title">🚀 快速入口</span>
        </template>
        <div class="entry-grid">
          <div class="entry-item" @click="router.push({ path: '/wallet/recharge' })">
            <div class="entry-icon recharge">💳</div>
            <span class="entry-text">充值</span>
          </div>
          <div class="entry-item" @click="router.push({ path: '/wallet/withdraw' })">
            <div class="entry-icon withdraw">💸</div>
            <span class="entry-text">提现</span>
          </div>
          <div class="entry-item" @click="router.push({ path: '/user/security-center' })">
            <div class="entry-icon security">🔒</div>
            <span class="entry-text">安全</span>
          </div>
          <div class="entry-item" @click="router.push({ path: '/user/real-name-verify' })">
            <div class="entry-icon verify">📝</div>
            <span class="entry-text">实名</span>
          </div>
        </div>
      </el-card>
    </div>
  </PageLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import PageLayout from '@/components/PageLayout.vue'
import { List, Goods, Sell, Ticket, ChatDotRound } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const currentRole = ref('buyer')
const activeNav = ref('/order/center')
const userInfo = ref(null)
const stats = ref({})
const coreStats = ref({
  totalOrders: 28,
  totalRevenue: '3,480.00',
  goodRate: '98.5%',
  hotGame: '三角洲行动'
})

onMounted(() => {
  userInfo.value = {
    nickname: userStore.nickname || '用户',
    phone: userStore.phone || '',
    user_level: 1,
    totalOrders: 0,
    creditScore: 100,
    realNameVerified: false,
    status: 1,
    createTime: null
  }
  if (userStore.isLoggedIn && userStore.userId) {
    fetchUserInfo()
  }

  document.title = '个人中心 - 龙虾道具交易平台'
})

const switchRole = (role) => {
  currentRole.value = role
}

const fetchUserInfo = async () => {
  try {
    const res = await fetch('/api/user/info')
    if (res.ok) {
      const data = await res.json()
      if (data.code === 0 || data.code === 200) {
        userInfo.value = data.data
        userStore.updateInfo(data.data)
      }
    }
  } catch (e) {}
}

const formatDate = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}
</script>

<style scoped>
.user-sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.profile-card {
  border-radius: 12px;
  border: none;
}

.profile-avatar {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 16px;
}

:deep(.avatar) {
  background: linear-gradient(135deg, #667eea, #7B5FD4);
  font-size: 22px;
  font-weight: 700;
}

.profile-nickname {
  font-size: 16px;
  font-weight: 700;
  color: #333;
  margin-bottom: 4px;
}

.profile-level {
  font-size: 12px;
  color: #999;
}

.profile-stats {
  display: flex;
  align-items: center;
  padding: 12px;
  background: #f9f8ff;
  border-radius: 10px;
  gap: 16px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.stat-value {
  font-size: 18px;
  font-weight: 700;
  color: #667eea;
}

.stat-label {
  font-size: 11px;
  color: #999;
  margin-top: 2px;
}

.stat-divider {
  width: 1px;
  height: 30px;
  background: #e8e4f8;
}

.role-card {
  border-radius: 12px;
  border: none;
}

.role-tabs {
  display: flex;
  gap: 8px;
}

.role-tab {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  color: #666;
  background: #f5f5f5;
  transition: all 0.2s;
}

.role-tab.active {
  background: linear-gradient(135deg, #667eea, #7B5FD4);
  color: #fff;
}

.nav-card {
  border-radius: 12px;
  border: none;
}

:deep(.el-menu-item) {
  border-radius: 8px;
  margin: 3px 0;
  font-size: 13px;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #667eea, #7B5FD4) !important;
  color: #fff !important;
}

.uc-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.action-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.action-card:hover {
  border-color: #7B5FD4;
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(91, 71, 194, 0.12);
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}

.unpaid { background: #fff7e6; }
.confirm { background: #e6f7ff; }
.completed { background: #e8f5e9; }
.favorited { background: #fce4ec; }

.action-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.action-count {
  font-size: 22px;
  font-weight: 700;
  color: #333;
}

.action-label {
  font-size: 12px;
  color: #999;
}

.info-card, .entry-card, .stat-cards-card {
  border-radius: 12px;
  border: none;
}

.card-title {
  font-weight: 600;
  font-size: 15px;
}

.stat-cards-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.25s;
  border: 2px solid transparent;
}

.stat-card-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(0,0,0,0.1);
}

.stat-card-item.orders {
  background: linear-gradient(135deg, #fff0f5, #ffe4ec);
  border-color: #ffd6e0;
}
.stat-card-item.orders:hover { border-color: #f56c6c; }

.stat-card-item.revenue {
  background: linear-gradient(135deg, #f0fff4, #e3fcd8);
  border-color: #c5e8b7;
}
.stat-card-item.revenue:hover { border-color: #67c23a; }

.stat-card-item.rating {
  background: linear-gradient(135deg, #fffbf0, #fff3d6);
  border-color: #ffe0a0;
}
.stat-card-item.rating:hover { border-color: #f5a623; }

.stat-card-item.hot-game {
  background: linear-gradient(135deg, #f0f4ff, #dce8ff);
  border-color: #b8d0ff;
}
.stat-card-item.hot-game:hover { border-color: #667eea; }

.stat-card-icon {
  font-size: 32px;
  flex-shrink: 0;
}

.stat-card-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-card-value {
  font-size: 22px;
  font-weight: 800;
  color: #333;
  line-height: 1;
}

.stat-card-label {
  font-size: 12px;
  color: #888;
  font-weight: 500;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 12px;
  color: #999;
}

.info-value {
  font-size: 14px;
  color: #333;
  font-weight: 600;
}

.entry-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.entry-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px;
  background: #f9f8ff;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.entry-item:hover {
  background: linear-gradient(135deg, #667eea, #7B5FD4);
}

.entry-item:hover .entry-icon,
.entry-item:hover .entry-text {
  color: #fff;
}

.entry-icon {
  font-size: 24px;
}

.entry-text {
  font-size: 12px;
  font-weight: 600;
  color: #666;
}
</style>