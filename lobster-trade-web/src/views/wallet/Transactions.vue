<template>
  <PageLayout>
    <template #sidebar>
      <el-card class="menu-card" shadow="never" :body-style="{ padding: '0' }">
        <template #header>
          <span class="menu-title">💳 资金管理</span>
        </template>
        <el-menu :default-active="route.path" router>
          <el-menu-item index="/user">
            <el-icon><User /></el-icon>
            <span>个人信息</span>
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
        </el-menu>
      </el-card>
    </template>

    <div class="txn-content">
      <el-card class="txn-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">📜 交易流水</span>
            <el-select v-model="filterType" placeholder="全部类型" clearable size="small" style="width: 120px;" @change="loadTransactions">
              <el-option label="全部" value="" />
              <el-option label="收入" :value="1" />
              <el-option label="支出" :value="2" />
              <el-option label="冻结" :value="3" />
              <el-option label="解冻" :value="4" />
              <el-option label="退款" :value="5" />
            </el-select>
          </div>
        </template>

        <div class="stats-row">
          <div class="stat-box">
            <span class="stat-label">总收入</span>
            <span class="stat-value income">+¥{{ stats.income || 0 }}</span>
          </div>
          <div class="stat-box">
            <span class="stat-label">总支出</span>
            <span class="stat-value expense">-¥{{ stats.expense || 0 }}</span>
          </div>
          <div class="stat-box">
            <span class="stat-label">冻结金额</span>
            <span class="stat-value frozen">¥{{ stats.frozen || 0 }}</span>
          </div>
        </div>

        <div class="txn-list" v-loading="loading">
          <template v-if="transactions.length > 0">
            <div v-for="t in transactions" :key="t.id" class="txn-item">
              <div class="txn-icon" :class="getIconClass(t.type)">
                {{ getIcon(t.type) }}
              </div>
              <div class="txn-info">
                <div class="txn-title">{{ t.typeName || '交易' }}</div>
                <div class="txn-time">{{ formatTime(t.createTime) }}</div>
              </div>
              <div class="txn-amount" :class="getAmountClass(t.amount)">
                {{ t.amount >= 0 ? '+' : '' }}¥{{ Math.abs(t.amount) }}
              </div>
            </div>
          </template>
          <el-empty v-else description="暂无交易记录" />
        </div>

        <div class="pagination-wrap" v-if="total > 0">
          <el-pagination
            v-model:current-page="page"
            :page-size="20"
            :total="total"
            layout="prev, pager, next"
            @current-change="loadTransactions"
          />
        </div>
      </el-card>
    </div>
  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getWalletTransactions as getTransactions } from '@/api/wallet'
import PageLayout from '@/components/PageLayout.vue'
import { User, Wallet, List, Goods } from '@element-plus/icons-vue'

const route = useRoute()
const loading = ref(false)
const transactions = ref([])
const stats = ref({})
const page = ref(1)
const total = ref(0)
const filterType = ref('')

onMounted(() => {
  loadTransactions()

  document.title = '交易记录 - 龙虾道具交易平台'
})

const loadTransactions = async () => {
  loading.value = true
  try {
    const params = { page: page.value, pageSize: 20 }
    if (filterType.value) params.type = filterType.value
    const res = await getTransactions(params)
    if (res.data) {
      transactions.value = res.data.records || res.data.list || []
      total.value = res.data.total || 0
      if (res.data.stats) stats.value = res.data.stats
    }
  } catch (e) {
    console.error('加载交易记录失败', e)
  } finally {
    loading.value = false
  }
}

const getIconClass = (type) => {
  return ['', 'income', 'expense', 'frozen', 'frozen', 'refund'][type] || ''
}

const getIcon = (type) => {
  return ['💰', '📈', '📉', '🔒', '🔓', '↩️'][type] || '💰'
}

const getAmountClass = (amount) => {
  return amount >= 0 ? 'income' : 'expense'
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.txn-content {
  display: flex;
  flex-direction: column;
}

.menu-card {
  border-radius: 12px;
  border: none;
}

.menu-title {
  font-weight: 600;
  font-size: 14px;
}

.txn-card {
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

.stats-row {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.stat-box {
  flex: 1;
  background: #f9f8ff;
  border-radius: 10px;
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 12px;
  color: #999;
}

.stat-value {
  font-size: 18px;
  font-weight: 700;
}

.stat-value.income { color: #67c23a; }
.stat-value.expense { color: #f56c6c; }
.stat-value.frozen { color: #999; }

.txn-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.txn-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px;
  background: #f9f8ff;
  border-radius: 10px;
}

.txn-icon {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.txn-icon.income { background: #e8f5e9; }
.txn-icon.expense { background: #fce4ec; }
.txn-icon.frozen { background: #fff3e0; }
.txn-icon.refund { background: #e3f2fd; }

.txn-info {
  flex: 1;
}

.txn-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.txn-time {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.txn-amount {
  font-size: 16px;
  font-weight: 700;
}

.txn-amount.income { color: #67c23a; }
.txn-amount.expense { color: #333; }

.pagination-wrap {
  display: flex;
  justify-content: center;
  padding: 16px 0;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #667eea, #7B5FD4) !important;
  color: #fff !important;
}
</style>