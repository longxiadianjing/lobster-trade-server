<template>
  <div class="order-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单列表</span>
          <el-select v-model="statusFilter" placeholder="订单状态" style="width:140px" clearable @change="loadOrders">
            <el-option label="全部" value="" />
            <el-option label="待支付" :value="0" />
            <el-option label="已支付" :value="1" />
            <el-option label="进行中" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="4" />
            <el-option label="退款中" :value="5" />
            <el-option label="已退款" :value="6" />
          </el-select>
        </div>
      </template>
      <el-table :data="orders" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单号" min-width="180" show-overflow-tooltip />
        <el-table-column prop="productTitle" label="商品" min-width="160" show-overflow-tooltip />
        <el-table-column prop="buyerId" label="买家ID" width="90" />
        <el-table-column prop="sellerId" label="卖家ID" width="90" />
        <el-table-column prop="amount" label="金额" width="90">
          <template #default="{ row }">¥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTypeMap[row.status]" size="small">{{ statusMap[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="page"
        :page-size="20"
        layout="total, prev, pager, next"
        :total="total"
        @current-change="loadOrders"
        style="margin-top:16px;justify-content:center" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminOrders } from '@/api/admin'

const loading = ref(false)
const orders = ref([])
const total = ref(0)
const page = ref(1)
const statusFilter = ref('')

const statusMap = { 0:'待支付', 1:'已支付', 2:'进行中', 3:'已完成', 4:'已取消', 5:'退款中', 6:'已退款' }
const statusTypeMap = { 0:'warning', 1:'', 2:'info', 3:'success', 4:'danger', 5:'warning', 6:'info' }

const loadOrders = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: 20 }
    if (statusFilter.value !== '') params.status = statusFilter.value
    const res = await getAdminOrders(params)
    orders.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

const viewDetail = (row) => {
  ElMessage.info('订单详情：' + row.orderNo)
}

onMounted(() => {
  loadOrders()
  document.title = '订单管理 - 龙虾道具交易平台'
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>