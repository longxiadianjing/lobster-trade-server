<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon blue">
          <el-icon :size="28"><User /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-label">用户总数</p>
          <p class="stat-value">{{ stats.totalUsers }}</p>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon green">
          <el-icon :size="28"><List /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-label">今日订单</p>
          <p class="stat-value">{{ stats.todayOrders }}</p>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon purple">
          <el-icon :size="28"><Goods /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-label">商品总数</p>
          <p class="stat-value">{{ stats.totalProducts }}</p>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon orange">
          <el-icon :size="28"><Coin /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-label">今日GMV</p>
          <p class="stat-value">¥{{ stats.todayGmv }}</p>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-row">
      <el-card class="chart-card" shadow="never">
        <template #header>
          <div class="card-header-flex">
            <span class="card-title">近7天订单趋势</span>
            <el-radio-group v-model="chartType" size="small">
              <el-radio-button label="count">订单量</el-radio-button>
              <el-radio-button label="gmv">GMV</el-radio-button>
            </el-radio-group>
          </div>
        </template>
        <div ref="orderChartRef" class="chart-container"></div>
      </el-card>

      <el-card class="chart-card" shadow="never">
        <template #header>
          <span class="card-title">仲裁处理</span>
        </template>
        <div class="dispute-summary">
          <div class="dispute-item">
            <span class="d-label">待处理</span>
            <span class="d-value warning">{{ disputeStats.pending }}</span>
          </div>
          <div class="dispute-item">
            <span class="d-label">已处理</span>
            <span class="d-value success">{{ disputeStats.resolved }}</span>
          </div>
        </div>
        <el-button v-if="disputeStats.pending > 0" type="primary" size="small" class="mt-16" @click="$router.push('/disputes')">
          去处理
        </el-button>
      </el-card>
    </div>

    <!-- 最近订单 -->
    <el-card class="recent-orders" shadow="never">
      <template #header>
        <div class="card-header-flex">
          <span class="card-title">最近订单</span>
          <el-button text type="primary" @click="$router.push('/orders')">查看更多</el-button>
        </div>
      </template>
      <el-table :data="recentOrders" stripe @row-click="goOrderDetail">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="productTitle" label="商品" min-width="200" />
        <el-table-column prop="buyerId" label="买家ID" width="80" />
        <el-table-column prop="sellerId" label="卖家ID" width="80" />
        <el-table-column prop="orderAmount" label="金额" width="100">
          <template #default="{ row }">
            <span class="price">¥{{ row.orderAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="160" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, List, Goods, Coin } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import request from '@/utils/request'

const router = useRouter()
const stats = ref({ totalUsers: 0, todayOrders: 0, totalProducts: 0, todayGmv: '0.00' })
const orderTrend = ref([])
const disputeStats = ref({ pending: 0, resolved: 0 })
const recentOrders = ref([])
const chartType = ref('count')
const orderChartRef = ref(null)
let orderChart = null

const statusMap = {
  pending_pay: '待付款', paid: '已付款', in_progress: '进行中', submitted: '待收货',
  confirmed: '已确认', completed: '已完成', disputed: '仲裁中', cancelled: '已取消'
}
const statusTypeMap = {
  pending_pay: 'warning', paid: 'primary', in_progress: '', submitted: 'success',
  confirmed: 'warning', completed: 'info', disputed: 'danger', cancelled: 'info'
}

const getStatusText = (s) => statusMap[s] || s
const getStatusType = (s) => statusTypeMap[s] || ''

const goOrderDetail = (row) => { router.push('/orders') }

const initChart = () => {
  if (!orderChartRef.value) return
  orderChart = echarts.init(orderChartRef.value)
  updateChart()
}

const updateChart = () => {
  if (!orderChart) return
  const labels = orderTrend.value.map(d => d.label)
  const values = chartType.value === 'count'
    ? orderTrend.value.map(d => d.count)
    : orderTrend.value.map(d => Math.round(Number(d.gmv || 0)))

  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: chartType.value === 'count'
        ? '{b}<br/>订单 {c} 单'
        : '{b}<br/>GMV ¥{c}'
    },
    grid: { left: 50, right: 20, top: 20, bottom: 30 },
    xAxis: {
      type: 'category',
      data: labels,
      axisLabel: { color: '#999', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#999', fontSize: 11 },
      splitLine: { lineStyle: { color: '#f0f0f0' } }
    },
    series: [{
      type: 'bar',
      data: values,
      itemStyle: {
        color: chartType.value === 'count'
          ? { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: '#667eea' }, { offset: 1, color: '#764ba2' }] }
          : { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: '#f5a623' }, { offset: 1, color: '#f7b033' }] }
      },
      barMaxWidth: 40
    }]
  }
  orderChart.setOption(option)
}

watch(chartType, () => updateChart())

watch(orderTrend, () => updateChart(), { deep: true })

const loadDashboard = async () => {
  try {
    const res = await request.get('/admin/dashboard/overview')
    if (res.data) {
      const d = res.data
      stats.value.totalUsers = d.totalUsers || 0
      stats.value.todayOrders = d.todayOrders || 0
      stats.value.totalProducts = d.totalProducts || 0
      stats.value.todayGmv = d.todayGmvFormatted || formatGmv(d.todayGmv)
      orderTrend.value = d.orderTrend || []
      disputeStats.value.pending = d.pendingDisputes || 0
      disputeStats.value.resolved = (d.totalDisputes || 0) - (d.pendingDisputes || 0)
      recentOrders.value = d.recentOrders || []
    }
  } catch (e) {
    console.error('Dashboard数据加载失败', e)
  }
}

const formatGmv = (v) => {
  if (!v) return '0.00'
  if (v >= 10000) return (v / 10000).toFixed(1) + '万'
  return Number(v).toFixed(2)
}

onMounted(() => {
  loadDashboard()
  setTimeout(initChart, 100)
})

onUnmounted(() => {
  if (orderChart) {
    orderChart.dispose()
    orderChart = null
  }
})
</script>

<style scoped>
.dashboard { padding: 0; }

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.stat-icon.blue { background: linear-gradient(135deg, #667eea, #764ba2); }
.stat-icon.green { background: linear-gradient(135deg, #67c23a, #85ce61); }
.stat-icon.purple { background: linear-gradient(135deg, #9c27b0, #e91e63); }
.stat-icon.orange { background: linear-gradient(135deg, #f5a623, #f7b033); }

.stat-label { font-size: 13px; color: #999; margin: 0 0 4px; }
.stat-value { font-size: 28px; font-weight: 700; color: #333; margin: 0; }

.charts-row {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
  margin-bottom: 24px;
}

.chart-card { background: #fff; border-radius: 12px; }
.chart-card :deep(.el-card__header) { padding: 14px 20px; border-bottom: 1px solid #f0f0f0; }
.card-title { font-size: 15px; font-weight: 600; color: #333; }
.card-header-flex { display: flex; align-items: center; justify-content: space-between; }

.chart-container { height: 200px; width: 100%; }

.dispute-summary {
  display: flex;
  gap: 20px;
  padding: 10px 0;
}

.dispute-item {
  flex: 1;
  background: #f9fafb;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
}

.d-label { display: block; font-size: 12px; color: #999; margin-bottom: 6px; }
.d-value { font-size: 26px; font-weight: 700; }
.d-value.warning { color: #f56c6c; }
.d-value.success { color: #67c23a; }

.mt-16 { margin-top: 16px; }

.recent-orders { background: #fff; border-radius: 12px; }
.recent-orders :deep(.el-card__header) { padding: 14px 20px; border-bottom: 1px solid #f0f0f0; }
.recent-orders :deep(.el-table) { border: none; }
.recent-orders :deep(.el-table__body tr) { cursor: pointer; }
.recent-orders :deep(.el-table__body tr:hover td) { background: #f5f0ff; }
.price { font-weight: 600; color: #667eea; }

@media (max-width: 1200px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .charts-row { grid-template-columns: 1fr; }
}
</style>
