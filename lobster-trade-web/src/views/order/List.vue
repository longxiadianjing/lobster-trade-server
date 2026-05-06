<template>
  <div class="order-list">
    <div class="filter-bar">
      <el-select v-model="status" placeholder="订单状态" clearable @change="loadOrders">
        <el-option label="待付款" value="pending_pay" />
        <el-option label="已付款" value="paid" />
        <el-option label="进行中" value="in_progress" />
        <el-option label="已提交" value="submitted" />
        <el-option label="已完成" value="completed" />
        <el-option label="仲裁中" value="disputed" />
        <el-option label="已取消" value="cancelled" />
      </el-select>
    </div>

    <div class="status-tip" v-if="status">
      <el-alert type="info" :closable="false" show-icon>
        <template #title>
          <span v-if="status === 'pending_pay'">💡 请尽快完成付款，付款后订单将自动发货</span>
          <span v-else-if="status === 'paid'">⏳ 卖家正在准备商品，请耐心等待发货</span>
          <span v-else-if="status === 'in_progress'">🔧 卖家正在处理中，请稍候</span>
          <span v-else-if="status === 'submitted'">✅ 卖家已发货，请确认收货并核对商品</span>
          <span v-else-if="status === 'completed'">🎉 交易已完成，感谢您的信任</span>
          <span v-else-if="status === 'disputed'">🛡️ 平台正在处理争议，请保持联系畅通</span>
          <span v-else-if="status === 'cancelled'">📋 订单已取消，如有疑问请联系客服</span>
        </template>
      </el-alert>
    </div>

    <el-table :data="orders" v-loading="loading">
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column prop="productTitle" label="商品" />
      <el-table-column prop="orderAmount" label="金额" width="90">
        <template #default="{ row }">¥{{ row.orderAmount }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusMap[row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="160" />
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button link type="primary" @click="goDetail(row)">详情</el-button>
          <el-button v-if="row.status === 'pending_pay' && role === 'buyer'" link type="danger" @click="handleCancel(row)">取消</el-button>
          <el-button v-if="row.status === 'submitted' && role === 'buyer'" link type="success" @click="handleConfirm(row)">确认</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && orders.length === 0" description="暂无相关订单" />
    <el-pagination
      v-model:current-page="page"
      :page-size="20"
      layout="prev, pager, next"
      :total="total"
      @current-change="loadOrders"
    />
  </div>
</template>

<script setup>
  document.title = '订单列表 - 龙虾道具交易平台';

import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getBuyerOrders, getSellerOrders, cancelOrder, confirmOrder } from '@/api/order'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({ role: { type: String, default: 'buyer' } })
const router = useRouter()
const orders = ref([])
const loading = ref(false)
const status = ref(null)
const page = ref(1)
const total = ref(0)
const statusMap = {
  pending_pay: '待付款', paid: '已付款', in_progress: '进行中',
  submitted: '已提交', confirmed: '已确认', completed: '已完成',
  disputed: '仲裁中', arbitration: '仲裁中', cancelled: '已取消'
}

onMounted(() => loadOrders())

async function loadOrders() {
  loading.value = true
  try {
    const res = props.role === 'buyer'
      ? await getBuyerOrders({ status: status.value, page: page.value, pageSize: 20 })
      : await getSellerOrders({ status: status.value, page: page.value, pageSize: 20 })
    if (res.code === 200) {
      orders.value = res.data?.records || []
      total.value = res.data?.total || 0
    }
  } finally {
    loading.value = false
  }
}

function statusType(s) {
  return { pending_pay: 'warning', paid: '', in_progress: 'primary', submitted: 'info', confirmed: 'success', completed: 'success', disputed: 'danger', arbitration: 'danger', cancelled: 'info' }[s] || ''
}

function goDetail(row) {
  router.push(`/order/detail/${row.id}`)
}

async function handleCancel(row) {
  await ElMessageBox.confirm('确定取消该订单？')
  const res = await cancelOrder(row.id, '买家取消')
  if (res.code === 200) { ElMessage.success('已取消'); loadOrders() }
}

async function handleConfirm(row) {
  await ElMessageBox.confirm('确认收到代练成果？')
  const res = await confirmOrder(row.id)
  if (res.code === 200) { ElMessage.success('已确认'); loadOrders() }
}
</script>

<style scoped>
.order-list { padding: 0; }
.filter-bar { margin-bottom: 12px; }
.status-tip { margin-bottom: 12px; }
</style>
