<template>
  <PageLayout>
    <template #sidebar>
      <el-card class="menu-card" shadow="never" :body-style="{ padding: '0' }">
        <template #header><span class="menu-title">💼 卖家中心</span></template>
        <el-menu :default-active="route.path" router>
          <el-menu-item index="/seller/stats">
            <el-icon><DataAnalysis /></el-icon><span>数据统计</span>
          </el-menu-item>
          <el-menu-item index="/product/list">
            <el-icon><Goods /></el-icon><span>我的商品</span>
          </el-menu-item>
          <el-menu-item index="/order/center">
            <el-icon><List /></el-icon><span>订单管理</span>
          </el-menu-item>
        </el-menu>
      </el-card>
    </template>

    <div class="stats-container">
      <div class="stats-actions">
        <el-button type="primary" size="large" @click="router.push({ path: '/wallet/withdraw' })">
          💸 立即提现
        </el-button>
      </div>
      <h2 class="page-title">📊 收入统计</h2>

      <!-- 核心指标 -->
      <div class="stats-cards">
        <div class="stat-card stat-today">
          <div class="stat-label">今日收入</div>
          <div class="stat-value">¥{{ stats.todayRevenue || '0.00' }}</div>
        </div>
        <div class="stat-card stat-month">
          <div class="stat-label">本月收入</div>
          <div class="stat-value">¥{{ stats.monthRevenue || '0.00' }}</div>
        </div>
        <div class="stat-card stat-total">
          <div class="stat-label">累计收入</div>
          <div class="stat-value">¥{{ stats.totalRevenue || '0.00' }}</div>
        </div>
        <div class="stat-card stat-orders">
          <div class="stat-label">完成订单</div>
          <div class="stat-value">{{ stats.totalOrders || 0 }} 单</div>
        </div>
      </div>

      <!-- 近7天趋势 -->
      <el-card class="trend-card" shadow="never">
        <template #header><span>📈 近7天收入趋势</span></template>
        <div class="trend-list" v-if="stats.recent7Days">
          <div class="trend-item" v-for="(val, key) in stats.recent7Days" :key="key">
            <span class="trend-date">{{ key }}</span>
            <span class="trend-amount">¥{{ val || '0.00' }}</span>
          </div>
        </div>
        <el-empty v-else description="暂无数据" />
      </el-card>
      <!-- 快捷入口 -->
      <div class="quick-links">
        <el-button type="primary" plain @click="router.push({ path: '/order/center' })">📦 查看所有订单</el-button>
        <el-button type="success" plain @click="router.push({ path: '/product/list' })">🎮 管理我的商品</el-button>
      </div>
    </div>
  </PageLayout>
</template>

<script setup>
  document.title = '数据统计 - 龙虾道具交易平台';
  import { ref, onMounted } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import request from '@/utils/request'
  import PageLayout from '@/components/PageLayout.vue'
  import { DataAnalysis, Goods, List } from '@element-plus/icons-vue'

  const route = useRoute()
  const router = useRouter()
  const stats = ref({
    totalRevenue: '0.00',
    todayRevenue: '0.00',
    monthRevenue: '0.00',
    totalOrders: 0,
    recent7Days: {}
  })

  const loadStats = async () => {
    try {
      const res = await request.get('/seller/stats/summary')
      if (res.code === 200 && res.data) {
        stats.value = res.data
      }
    } catch (e) { console.error(e) }
  }

  onMounted(() => { loadStats() })
</script>

<style scoped>
.stats-container { max-width: 900px; }
.page-title { font-size: 20px; font-weight: 700; margin-bottom: 20px; }
.stats-cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
.stat-card { padding: 20px; border-radius: 12px; text-align: center; }
.stat-today { background: linear-gradient(135deg, #667eea, #7B5FD4); color: #fff; }
.stat-month { background: linear-gradient(135deg, #f093fb, #f5576c); color: #fff; }
.stat-total { background: linear-gradient(135deg, #4facfe, #00f2fe); color: #fff; }
.stat-orders { background: linear-gradient(135deg, #43e97b, #38f9d7); color: #fff; }
.stat-label { font-size: 12px; opacity: 0.85; margin-bottom: 8px; }
.stat-value { font-size: 22px; font-weight: 700; }
.trend-card { border-radius: 12px; border: none; }
.trend-list { display: flex; gap: 12px; }
.trend-item { flex: 1; text-align: center; padding: 10px; background: #f9f8ff; border-radius: 8px; }
.trend-date { display: block; font-size: 11px; color: #999; margin-bottom: 4px; }
.trend-amount { font-size: 14px; font-weight: 700; color: #333; }
.menu-card { border-radius: 12px; border: none; }
.menu-title { font-weight: 600; font-size: 14px; }
.stats-actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.quick-links {
  display: flex;
  gap: 12px;
  margin-top: 20px;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .stats-cards { grid-template-columns: repeat(2, 1fr); }
  .trend-list { flex-wrap: wrap; }
}
</style>
