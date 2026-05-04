<template>
  <div class="dashboard-v2">
    <!-- 快捷操作栏 -->
    <div class="quick-bar">
      <span class="quick-bar-title">快捷操作</span>
      <el-button size="small" @click="go('users')">用户管理</el-button>
      <el-button size="small" @click="go('products')">商品管理</el-button>
      <el-button size="small" @click="go('orders')">订单管理</el-button>
      <el-button size="small" @click="go('disputes')">纠纷处理</el-button>
      <el-button size="small" @click="go('announcements')">发布公告</el-button>
      <el-button size="small" @click="go('games')">游戏管理</el-button>
      <el-button size="small" @click="go('coupons')">优惠券</el-button>
      <el-button size="small" @click="go('tickets')">工单</el-button>
      <el-button size="small" @click="go('cs')">客服</el-button>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="4" v-for="(s, i) in stats" :key="i">
        <div class="stat-card" :style="{ background: s.bg }">
          <div class="stat-icon">{{ s.icon }}</div>
          <div class="stat-body">
            <div class="stat-num">{{ s.value }}</div>
            <div class="stat-label">{{ s.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 今日数据 + 系统状态 -->
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header><span>📅 今日数据</span></template>
          <div class="today-grid">
            <div class="today-item"><span class="today-label">今日新增用户</span><span class="today-val">{{ overview.todayUsers ?? 0 }}</span></div>
            <div class="today-item"><span class="today-label">今日订单</span><span class="today-val">{{ overview.todayOrders ?? 0 }}</span></div>
            <div class="today-item"><span class="today-label">今日交易额</span><span class="today-val text-purple">¥{{ overview.todayGmvFormatted ?? '0' }}</span></div>
            <div class="today-item"><span class="today-label">待处理纠纷</span><span class="today-val text-orange">{{ overview.pendingDisputes ?? 0 }}</span></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header><span>⚙️ 系统状态</span></template>
          <div class="sys-stats">
            <div class="sys-item"><span>游戏数量</span><span class="sys-val">{{ overview.totalGames ?? 0 }}</span></div>
            <div class="sys-item"><span>认证服务商</span><span class="sys-val text-blue">{{ certStats.pending ?? 0 }} 待审</span></div>
            <div class="sys-item"><span>待处理工单</span><span class="sys-val text-orange">{{ ticketStats.pending ?? 0 }}</span></div>
            <div class="sys-item"><span>客服会话</span><span class="sys-val">{{ csStats.active ?? 0 }} 进行中</span></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 近期订单 -->
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>📋 近期订单</span>
            <el-button size="small" link type="primary" style="float:right" @click="go('orders')">查看全部 →</el-button>
          </template>
          <el-table :data="recentOrders" size="small" :default-sort="{ prop: 'createTime', order: 'descending' }">
            <el-table-column prop="id" label="订单ID" width="90" />
            <el-table-column label="商品" min-width="180" show-overflow-tooltip>
              <template #default="{ row }">{{ row.productTitle || ('商品#' + row.productId) }}</template>
            </el-table-column>
            <el-table-column label="买家" width="100">
              <template #default="{ row }">{{ row.buyerUsername || row.buyerId || '-' }}</template>
            </el-table-column>
            <el-table-column label="卖家" width="100">
              <template #default="{ row }">{{ row.sellerUsername || row.sellerId || '-' }}</template>
            </el-table-column>
            <el-table-column label="金额" width="90">
              <template #default="{ row }"><strong style="color:#667eea">¥{{ row.orderAmount ?? row.amount ?? 0 }}</strong></template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="statusTypeMap[row.status]">{{ statusMap[row.status] }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="160" sortable />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getStatsOverview } from '@/api/admin'

const router = useRouter()
const emit = defineEmits(['navigate'])

const stats = ref([
  { label: '总用户', value: '-', icon: '👥', bg: 'linear-gradient(135deg,#667eea,#764ba2)' },
  { label: '总商品', value: '-', icon: '📦', bg: 'linear-gradient(135deg,#11998e,#38ef7d)' },
  { label: '总订单', value: '-', icon: '🧾', bg: 'linear-gradient(135deg,#f093fb,#f5576c)' },
  { label: '总交易额', value: '-', icon: '💰', bg: 'linear-gradient(135deg,#4facfe,#00f2fe)' },
  { label: '待处理纠纷', value: '-', icon: '⚠️', bg: 'linear-gradient(135deg,#fa709a,#fee140)' },
])

const overview = ref({})
const certStats = ref({})
const ticketStats = ref({})
const csStats = ref({})
const recentOrders = ref([])

const statusMap = {0:'待支付',1:'已支付',2:'进行中',3:'已完成',4:'已取消',5:'退款中',6:'已退款'}
const statusTypeMap = {0:'warning',1:'',2:'info',3:'success',4:'danger',5:'warning',6:'info'}

const go = (menu) => { emit('navigate', menu) }

onMounted(async () => {
  try {
    const [r1, r2, r3, r4] = await Promise.all([
      getStatsOverview(),
      fetch('/api/admin/certification/pending?page=1&size=1').then(r => r.json()),
      fetch('/api/admin/ticket/stats').then(r => r.json()),
      fetch('/api/admin/cs/stats').then(r => r.json()),
    ])
    if (r1.data) {
      overview.value = r1.data
      stats.value[0].value = r1.data.totalUsers ?? '-'
      stats.value[1].value = r1.data.totalProducts ?? '-'
      stats.value[2].value = r1.data.totalOrders ?? '-'
      stats.value[3].value = '¥' + (r1.data.totalGmvFormatted ?? r1.data.totalGmv ?? '0')
      stats.value[4].value = r1.data.pendingDisputes ?? '-'
      recentOrders.value = (r1.data.recentOrders || []).slice(0, 8)
    }
    if (r2.data?.total !== undefined) certStats.value = { pending: r2.data.total }
    if (r3.data) ticketStats.value = r3.data
    if (r4.data) csStats.value = r4.data
  } catch (e) { console.error(e) }

  document.title = '数据看板 - 龙虾道具交易平台'
})
</script>

<style scoped>
.dashboard-v2 { display: flex; flex-direction: column; gap: 16px; }

/* 快捷操作栏 */
.quick-bar {
  background: #fff;
  border-radius: 12px;
  padding: 12px 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  flex-wrap: wrap;
}
.quick-bar-title { font-size: 13px; color: #999; font-weight: 600; margin-right: 4px; white-space: nowrap; }
.quick-bar .el-button { border-radius: 20px; font-size: 13px; }

/* 统计卡片 */
.stat-card { border-radius: 12px; padding: 16px; display: flex; align-items: center; gap: 14px; color: #fff; }
.stat-icon { font-size: 28px; }
.stat-num { font-size: 24px; font-weight: 700; }
.stat-label { font-size: 13px; opacity: 0.85; }

/* 今日数据 */
.today-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.today-item { display: flex; flex-direction: column; gap: 4px; padding: 12px; background: #f8f9ff; border-radius: 8px; }
.today-label { font-size: 12px; color: #999; }
.today-val { font-size: 20px; font-weight: 700; color: #333; }
.text-purple { color: #667eea; }
.text-orange { color: #f5a623; }

/* 系统状态 */
.sys-stats { display: flex; flex-direction: column; gap: 0; }
.sys-item { display: flex; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid #f0f0f0; font-size: 14px; }
.sys-item:last-child { border-bottom: none; }
.sys-val { font-weight: 600; color: #667eea; }
.text-blue { color: #409eff; }
.text-orange { color: #f5a623; }
</style>
