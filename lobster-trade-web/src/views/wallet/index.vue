<template>
  <PageLayout>
    <template #sidebar>
      <el-card class="menu-card" shadow="never" :body-style="{ padding: '0' }">
        <template #header>
          <span class="menu-title">💳 资金管理</span>
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
          <el-menu-item index="/user/favorites">
            <el-icon><Star /></el-icon>
            <span>我的收藏</span>
          </el-menu-item>
          <el-menu-item index="/wallet">
            <el-icon><Wallet /></el-icon>
            <span>我的钱包</span>
          </el-menu-item>
          <el-menu-item index="/order/center">
            <el-icon><List /></el-icon>
            <span>订单中心</span>
          </el-menu-item>
          <el-menu-item index="/product/list">
            <el-icon><Goods /></el-icon>
            <span>商品列表</span>
          </el-menu-item>
          <el-menu-item index="/product/publish">
            <el-icon><Sell /></el-icon>
            <span>发布商品</span>
          </el-menu-item>
          <el-menu-item index="/user/coupon-center">
            <el-icon><Ticket /></el-icon>
            <span>优惠券</span>
          </el-menu-item>
          <el-menu-item index="/user/security-center">
            <el-icon><Lock /></el-icon>
            <span>安全中心</span>
          </el-menu-item>
        </el-menu>
      </el-card>
    </template>

    <div class="wallet-content">
      <!-- 余额卡片 -->
      <el-card class="balance-card" shadow="never">
        <div class="balance-header">
          <span class="balance-title">账户余额</span>
          <span class="balance-sub">资金由第三方银行托管，安全保障</span>
        </div>
        <div class="balance-amount">
          <span class="currency">¥</span>
          <span class="amount">{{ walletStore.balance }}</span>
        </div>
        <div class="balance-detail">
          <div class="detail-item">
            <span class="detail-label">可用余额</span>
            <span class="detail-value">¥{{ walletStore.availableBalance }}</span>
          </div>
          <div class="detail-split"></div>
          <div class="detail-item">
            <span class="detail-label">冻结金额</span>
            <span class="detail-value frozen">¥{{ walletStore.frozenBalance }}</span>
          </div>
        </div>
        <div class="balance-actions">
          <el-button type="primary" size="large" @click="router.push({ path: '/wallet/recharge' })" class="action-btn recharge">
            <el-icon><Wallet /></el-icon>
            充值
          </el-button>
          <el-button type="success" size="large" @click="router.push({ path: '/wallet/withdraw' })" class="action-btn withdraw">
            <el-icon><Money /></el-icon>
            提现
          </el-button>
          <el-button size="large" @click="router.push({ path: '/wallet/transactions' })" class="action-btn trans">
            <el-icon><List /></el-icon>
            交易流水
          </el-button>
        </div>
      </el-card>

      <!-- 本周收益图表 -->
      <el-card class="chart-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">📊 本周交易额趋势</span>
            <span class="chart-sub">近7天交易额一览</span>
          </div>
        </template>
        <div class="chart-wrap">
          <div class="chart-bars">
            <div
              v-for="(day, idx) in weekData"
              :key="idx"
              class="bar-col"
            >
              <div class="bar-value">¥{{ day.amount }}</div>
              <div class="bar-track">
                <div
                  class="bar-fill"
                  :style="{ height: (day.amount / maxAmount * 100) + '%' }"
                ></div>
              </div>
              <div class="bar-label">{{ day.label }}</div>
              <div class="bar-date">{{ day.date }}</div>
            </div>
          </div>
          <div class="chart-summary">
            <div class="summary-item">
              <span class="summary-label">本周总收入</span>
              <span class="summary-value green">¥{{ weekTotal }}</span>
            </div>
            <div class="summary-divider"></div>
            <div class="summary-item">
              <span class="summary-label">本周订单数</span>
              <span class="summary-value"> {{ weekOrders }} 笔</span>
            </div>
            <div class="summary-divider"></div>
            <div class="summary-item">
              <span class="summary-label">最高单日</span>
              <span class="summary-value orange">¥{{ maxAmount }}</span>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 支付安全卡片 -->
      <el-card class="security-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">🛡️ 支付安全保障</span>
          </div>
        </template>
        <div class="security-list">
          <div class="security-item">
            <div class="security-icon shield">
              <el-icon><Lock /></el-icon>
            </div>
            <div class="security-info">
              <div class="security-name">资金托管</div>
              <div class="security-desc">交易完成前，资金由平台安全托管</div>
            </div>
            <el-tag type="success" size="small">已开启</el-tag>
          </div>
          <div class="security-item">
            <div class="security-icon verify">
              <el-icon><CircleCheckFilled /></el-icon>
            </div>
            <div class="security-info">
              <div class="security-name">支付密码</div>
              <div class="security-desc">已设置支付密码，保障资金安全</div>
            </div>
            <el-tag :type="userStore.hasPayPassword ? 'success' : 'warning'" size="small">
              {{ userStore.hasPayPassword ? '已设置' : '未设置' }}
            </el-tag>
          </div>
        </div>
      </el-card>
    </div>
  </PageLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useWalletStore } from '@/stores/wallet'
import { useUserStore } from '@/stores/user'
import PageLayout from '@/components/PageLayout.vue'
import { User, Wallet, List, Goods, Sell, Ticket, Lock, Star, ChatDotRound, Money, CircleCheckFilled } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const walletStore = useWalletStore()
const userStore = useUserStore()

// 本周收益数据（近7天）
const weekData = ref([
  { label: '周一', date: '04-22', amount: 120 },
  { label: '周二', date: '04-23', amount: 280 },
  { label: '周三', date: '04-24', amount: 85 },
  { label: '周四', date: '04-25', amount: 450 },
  { label: '周五', date: '04-26', amount: 360 },
  { label: '周六', date: '04-27', amount: 620 },
  { label: '周日', date: '04-28', amount: 195 },
])

const weekTotal = computed(() => weekData.value.reduce((sum, d) => sum + d.amount, 0))
const weekOrders = computed(() => Math.floor(weekTotal.value / 150))
const maxAmount = computed(() => Math.max(...weekData.value.map(d => d.amount)))

onMounted(() => {
  document.title = '我的钱包 - 龙虾道具交易平台'
  walletStore.fetchWalletInfo()
})
</script>

<style scoped>
.wallet-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.menu-card {
  border-radius: 12px;
  border: none;
}

.menu-title {
  font-weight: 600;
  font-size: 14px;
}

.balance-card {
  border-radius: 12px;
  border: none;
  background: linear-gradient(135deg, #667eea, #7B5FD4);
  color: #1a1a1a;
}

.balance-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.balance-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
}

.balance-sub {
  font-size: 12px;
  color: #666;
}

.balance-amount {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-bottom: 20px;
}

.currency {
  font-size: 20px;
  font-weight: 600;
  color: #e94560;
}

.amount {
  font-size: 42px;
  font-weight: 700;
  letter-spacing: -1px;
  color: #e94560;
}

.balance-detail {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 24px;
  padding: 16px;
  background: rgba(255,255,255,0.1);
  border-radius: 10px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-label {
  font-size: 12px;
  color: #1a1a1a;
  opacity: 0.7;
}

.detail-value {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.detail-split {
  width: 1px;
  height: 30px;
  background: rgba(255,255,255,0.2);
}

.balance-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  flex: 1;
  border-radius: 8px !important;
  font-weight: 600 !important;
}

.recharge {
  background: rgba(255,255,255,0.2) !important;
  border: 1px solid rgba(255,255,255,0.3) !important;
  color: #fff !important;
}

.recharge:hover {
  background: rgba(255,255,255,0.3) !important;
}

.withdraw {
  background: #fff !important;
  color: #67c23a !important;
  border: none !important;
}

.withdraw:hover {
  background: #f0f7eb !important;
}

.trans {
  background: rgba(255,255,255,0.2) !important;
  border: 1px solid rgba(255,255,255,0.3) !important;
  color: #fff !important;
}

.trans:hover {
  background: rgba(255,255,255,0.3) !important;
}

/* 图表卡片 */
.chart-card {
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

.chart-sub {
  font-size: 12px;
  color: #999;
}

.chart-wrap {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chart-bars {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  height: 140px;
  padding: 0 4px;
}

.bar-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
}

.bar-value {
  font-size: 10px;
  color: #764ba2;
  font-weight: 700;
  margin-bottom: 4px;
  white-space: nowrap;
}

.bar-track {
  flex: 1;
  width: 100%;
  display: flex;
  align-items: flex-end;
  background: #f5f7fa;
  border-radius: 6px 6px 0 0;
  overflow: hidden;
}

.bar-fill {
  width: 100%;
  background: linear-gradient(to top, #764ba2, #a78bfa);
  border-radius: 6px 6px 0 0;
  transition: height 0.6s ease;
  min-height: 4px;
}

.bar-label {
  font-size: 11px;
  font-weight: 600;
  color: #333;
  margin-top: 4px;
}

.bar-date {
  font-size: 10px;
  color: #bbb;
}

.chart-summary {
  display: flex;
  align-items: center;
  gap: 0;
  padding: 14px 16px;
  background: #f9f8ff;
  border-radius: 10px;
  border: 1px solid #e8e0ff;
}

.summary-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.summary-label {
  font-size: 11px;
  color: #999;
}

.summary-value {
  font-size: 16px;
  font-weight: 800;
  color: #333;
}

.summary-value.green { color: #67c23a; }
.summary-value.orange { color: #f5a623; }

.summary-divider {
  width: 1px;
  height: 32px;
  background: #e0d8f5;
}

.security-card {
  border-radius: 12px;
  border: none;
}

.security-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.security-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px;
  background: #f9f8ff;
  border-radius: 10px;
}

.security-icon {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.shield {
  background: linear-gradient(135deg, #e8f5e9, #c8e6c9);
  color: #4caf50;
}

.verify {
  background: linear-gradient(135deg, #e3f2fd, #bbdefb);
  color: #2196f3;
}

.security-info {
  flex: 1;
}

.security-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 2px;
}

.security-desc {
  font-size: 12px;
  color: #999;
}

:deep(.el-menu-item) {
  border-radius: 8px;
  margin: 4px 0;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #667eea, #7B5FD4) !important;
  color: #fff !important;
}

:deep(.el-menu-item.is-active .el-icon) {
  color: #fff !important;
}
</style>
