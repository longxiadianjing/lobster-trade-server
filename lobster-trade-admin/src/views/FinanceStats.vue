<template>
  <div class="finance-stats">
    <el-card shadow="never">
      <template #header>
        <div class="card-header-flex">
          <span class="card-title">财务统计</span>
          <el-radio-group v-model="period" size="small" @change="changePeriod">
            <el-radio-button label="today">今日</el-radio-button>
            <el-radio-button label="week">近7天</el-radio-button>
            <el-radio-button label="month">近30天</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <!-- 核心指标卡片 -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-label">今日 GMV</div>
          <div class="stat-value primary">¥{{ formatMoney(stats.todayGmv) }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">近7天 GMV</div>
          <div class="stat-value purple">¥{{ formatMoney(stats.weekGmv) }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">近30天 GMV</div>
          <div class="stat-value orange">¥{{ formatMoney(stats.monthGmv) }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">平台累计 GMV</div>
          <div class="stat-value dark">¥{{ formatMoney(stats.totalGmv) }}</div>
        </div>
      </div>

      <!-- 充值/提现/平台收入 -->
      <div class="stats-grid mt-16">
        <div class="stat-card">
          <div class="stat-label">今日充值</div>
          <div class="stat-value success">+¥{{ formatMoney(stats.todayRecharge) }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">近30天充值</div>
          <div class="stat-value success">+¥{{ formatMoney(stats.monthRecharge) }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">今日提现</div>
          <div class="stat-value danger">-¥{{ formatMoney(stats.todayWithdraw) }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">近30天提现</div>
          <div class="stat-value danger">-¥{{ formatMoney(stats.monthWithdraw) }}</div>
        </div>
        <div class="stat-card wide">
          <div class="stat-label">近30天平台服务费收入</div>
          <div class="stat-value dark">¥{{ formatMoney(stats.platformIncome) }}</div>
          <div class="stat-hint">= 已完成订单托管金额 - 卖家实收</div>
        </div>
      </div>

      <!-- 趋势图 + 类型分布 -->
      <div class="charts-row mt-16">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <div class="card-header-flex">
              <span class="card-title">GMV 趋势</span>
              <el-select v-model="trendDays" size="small" style="width:100px" @change="loadTrend">
                <el-option label="7天" :value="7" />
                <el-option label="15天" :value="15" />
                <el-option label="30天" :value="30" />
              </el-select>
            </div>
          </template>
          <div ref="gmvChartRef" class="chart-container"></div>
        </el-card>

        <el-card class="chart-card" shadow="never">
          <template #header>
            <span class="card-title">交易类型分布（近30天）</span>
          </template>
          <div ref="typeChartRef" class="chart-container"></div>
        </el-card>
      </div>

      <!-- 订单趋势 -->
      <div class="mt-16">
        <el-card shadow="never">
          <template #header>
            <div class="card-header-flex">
              <span class="card-title">每日订单量趋势</span>
              <el-select v-model="trendDays" size="small" style="width:100px" @change="loadOrderTrend">
                <el-option label="7天" :value="7" />
                <el-option label="15天" :value="15" />
                <el-option label="30天" :value="30" />
              </el-select>
            </div>
          </template>
          <div ref="orderTrendRef" class="chart-container" style="height:200px"></div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
document.title = '财务统计 - 龙虾道具交易平台'
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import adminRequest from '@/utils/adminRequest'

const period = ref('today')
const trendDays = ref(30)
const stats = ref({})
const loading = ref(false)
let gmvChart, typeChart, orderTrendChart
const gmvChartRef = ref(null)
const typeChartRef = ref(null)
const orderTrendRef = ref(null)

const formatMoney = (v) => {
  if (v === null || v === undefined) return '0.00'
  return Number(v).toFixed(2)
}

const loadStats = async () => {
  try {
    const res = await adminRequest.get('/admin/finance/stats-overview')
    stats.value = res.data || {}
  } catch (e) {
    // handled
  }
}

const loadTrend = async () => {
  if (!gmvChart) return
  try {
    const res = await adminRequest.get('/admin/finance/gmv-trend', { params: { days: trendDays.value } })
    const data = res.data || []
    gmvChart.setOption({
      xAxis: { data: data.map(d => d.date.slice(5)) },
      series: [{ data: data.map(d => d.gmv) }]
    })
  } catch (e) {}
}

const loadOrderTrend = async () => {
  if (!orderTrendChart) return
  try {
    const res = await adminRequest.get('/admin/finance/order-trend', { params: { days: trendDays.value } })
    const data = res.data || []
    orderTrendChart.setOption({
      xAxis: { data: data.map(d => d.date.slice(5)) },
      series: [{ data: data.map(d => d.count) }]
    })
  } catch (e) {}
}

const loadTypeDist = async () => {
  if (!typeChart) return
  try {
    const res = await adminRequest.get('/admin/finance/trade-type-dist')
    const data = res.data || []
    const TYPE_MAP = { boost: '代练', escort: '护送', accompany: '陪玩', goods: '商品' }
    const pieData = data.map(d => ({ name: TYPE_MAP[d.type] || d.type, value: d.count }))
    typeChart.setOption({
      series: [{ data: pieData }]
    })
  } catch (e) {}
}

const changePeriod = () => {
  loadStats()
}

const initCharts = () => {
  if (gmvChartRef.value) {
    gmvChart = echarts.init(gmvChartRef.value)
    gmvChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 50, right: 20, top: 20, bottom: 30 },
      xAxis: { type: 'category', data: [] },
      yAxis: { type: 'value', axisLabel: { formatter: v => '¥' + v } },
      series: [{ type: 'line', smooth: true, areaStyle: {}, data: [] }]
    })
  }
  if (typeChartRef.value) {
    typeChart = echarts.init(typeChartRef.value)
    typeChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: 0 },
      series: [{ type: 'pie', radius: ['40%', '70%'], data: [] }]
    })
  }
  if (orderTrendRef.value) {
    orderTrendChart = echarts.init(orderTrendRef.value)
    orderTrendChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 50, right: 20, top: 20, bottom: 30 },
      xAxis: { type: 'category', data: [] },
      yAxis: { type: 'value' },
      series: [{ type: 'bar', data: [] }]
    })
  }
}

const resizeCharts = () => {
  gmvChart?.resize()
  typeChart?.resize()
  orderTrendChart?.resize()
}

onMounted(async () => {
  await loadStats()
  initCharts()
  await loadTrend()
  await loadOrderTrend()
  await loadTypeDist()
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts)
  gmvChart?.dispose()
  typeChart?.dispose()
  orderTrendChart?.dispose()
})
</script>

<style scoped>
.finance-stats { padding: 0; }
.card-header-flex { display:flex; align-items:center; justify-content:space-between; }
.card-title { font-size: 15px; font-weight: 600; }
.stats-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 16px; }
.stat-card { background: #f8f9fb; border-radius: 8px; padding: 16px; }
.stat-card.wide { grid-column: span 1; }
.stat-label { font-size: 12px; color: #888; margin-bottom: 6px; }
.stat-value { font-size: 22px; font-weight: 700; line-height: 1.2; }
.stat-value.primary { color: #409eff; }
.stat-value.purple { color: #7c3aed; }
.stat-value.orange { color: #f59e0b; }
.stat-value.dark { color: #333; }
.stat-value.success { color: #22c55e; }
.stat-value.danger { color: #ef4444; }
.stat-hint { font-size: 11px; color: #aaa; margin-top: 4px; }
.mt-16 { margin-top: 16px; }
.charts-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.chart-card { border: 1px solid #f0f0f0; }
.chart-container { height: 220px; }
</style>