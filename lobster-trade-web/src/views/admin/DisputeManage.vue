<template>
  <div class="dispute-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>纠纷列表</span>
          <el-select v-model="statusFilter" placeholder="纠纷状态" style="width:140px" clearable @change="loadDisputes">
            <el-option label="全部" value="" />
            <el-option label="处理中" value="0" />
            <el-option label="已解决" value="1" />
          </el-select>
        </div>
      </template>
      <el-table :data="disputes" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderId" label="订单ID" width="90" />
        <el-table-column prop="disputeType" label="纠纷类型" width="120">
          <template #default="{ row }">{{ disputeTypeMap[row.disputeType] || row.disputeType }}</template>
        </el-table-column>
        <el-table-column prop="disputeReason" label="原因" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small">
              {{ row.status === 1 ? '已解决' : '处理中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" v-if="row.status !== 1" @click="handleResolve(row.orderId)">处理</el-button>
            <el-button size="small" v-else disabled>已解决</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="page"
        :page-size="20"
        layout="total, prev, pager, next"
        :total="total"
        @current-change="loadDisputes"
        style="margin-top:16px;justify-content:center" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDisputes, resolveDispute } from '@/api/admin'

const loading = ref(false)
const disputes = ref([])
const total = ref(0)
const page = ref(1)
const statusFilter = ref('')
const disputeTypeMap = { 0:'交易纠纷', 1:'发货纠纷', 2:'售后纠纷', 3:'其他' }

const loadDisputes = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: 20 }
    if (statusFilter.value !== '') params.status = statusFilter.value
    const res = await getDisputes(params)
    disputes.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

const handleResolve = async (orderId) => {
  await ElMessageBox.confirm('确认解决该纠纷？', '提示')
  try {
    await resolveDispute(orderId)
    ElMessage.success('已处理')
    loadDisputes()
  } catch (e) { ElMessage.error('操作失败') }
}

onMounted(() => {
  loadDisputes()
  document.title = '争议管理 - 龙虾道具交易平台'
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>