<template>
  <div class="audit-manage">
    <el-card shadow="never">
      <template #header>
        <div class="card-header-flex">
          <span class="card-title">操作审计日志</span>
          <div class="header-stats">
            <span class="stat-tag">总操作数 <strong>{{ pagination.total }}</strong></span>
          </div>
        </div>
      </template>

      <el-form :model="filters" inline size="default">
        <el-form-item label="操作类型">
          <el-select v-model="filters.action" placeholder="全部" clearable style="width:160px" @change="loadData(1)">
            <el-option label="封禁用户" value="封禁用户" />
            <el-option label="解封用户" value="解封用户" />
            <el-option label="修改用户" value="修改用户" />
            <el-option label="修改订单" value="修改订单" />
            <el-option label="下架商品" value="下架商品" />
            <el-option label="上架商品" value="上架商品" />
            <el-option label="封禁商品" value="封禁商品" />
            <el-option label="修改商品" value="修改商品" />
            <el-option label="处理仲裁" value="处理仲裁" />
            <el-option label="编辑仲裁" value="编辑仲裁" />
            <el-option label="新增游戏" value="新增游戏" />
            <el-option label="修改游戏" value="修改游戏" />
            <el-option label="修改游戏状态" value="修改游戏状态" />
            <el-option label="新增管理员" value="新增管理员" />
            <el-option label="修改管理员" value="修改管理员" />
            <el-option label="删除管理员" value="删除管理员" />
            <el-option label="启用/禁用管理员" value="启用/禁用管理员" />
            <el-option label="修改管理员权限" value="修改管理员权限" />
            <el-option label="审核实名认证-通过" value="审核实名认证-通过" />
            <el-option label="审核实名认证-拒绝" value="审核实名认证-拒绝" />
            <el-option label="新增优惠券" value="新增优惠券" />
            <el-option label="修改优惠券" value="修改优惠券" />
            <el-option label="删除优惠券" value="删除优惠券" />
            <el-option label="新增热词" value="新增热词" />
            <el-option label="修改热词" value="修改热词" />
            <el-option label="删除热词" value="删除热词" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标类型">
          <el-select v-model="filters.targetType" placeholder="全部" clearable style="width:140px" @change="loadData(1)">
            <el-option label="用户 (User)" value="User" />
            <el-option label="订单 (Order)" value="Order" />
            <el-option label="商品 (Product)" value="Product" />
            <el-option label="仲裁 (Dispute)" value="Dispute" />
            <el-option label="游戏 (Game)" value="Game" />
            <el-option label="管理员 (Admin)" value="Admin" />
            <el-option label="实名认证 (Certification)" value="Certification" />
            <el-option label="优惠券 (Coupon)" value="Coupon" />
            <el-option label="热词 (HotSearch)" value="HotSearch" />
          </el-select>
        </el-form-item>
        <el-form-item label="管理员ID">
          <el-input v-model="filters.adminId" placeholder="管理员ID" clearable style="width:120px" @change="loadData(1)" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData(1)">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="adminUsername" label="管理员" width="130" />
        <el-table-column prop="action" label="操作类型" width="160">
          <template #default="{ row }">
            <el-tag size="small" type="primary">{{ row.action }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetType" label="目标类型" width="140">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.targetType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetId" label="目标ID" width="100" />
        <el-table-column prop="ipAddress" label="IP地址" width="140" />
        <el-table-column prop="createTime" label="操作时间" width="170">
          <template #default="{ row }">
            <span class="time-text">{{ formatTime(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="detail" label="操作详情" min-width="200" show-overflow-tooltip />
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadData"
          @size-change="loadData(1)"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const filters = reactive({ action: '', targetType: '', adminId: '' })
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })
const tableData = ref([])
const loading = ref(false)

const formatTime = (val) => {
  if (!val) return '-'
  const d = new Date(val)
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

const reset = () => {
  filters.action = ''
  filters.targetType = ''
  filters.adminId = ''
  loadData(1)
}

const loadData = (page = 1) => {
  if (page === 1) pagination.page = 1
  loading.value = true
  const params = {
    page: pagination.page,
    size: pagination.pageSize
  }
  if (filters.action) params.action = filters.action
  if (filters.targetType) params.targetType = filters.targetType
  if (filters.adminId) params.adminId = filters.adminId

  request.get('/admin/audit-logs', { params }).then(res => {
    if (res && res.records) {
      tableData.value = res.records
      pagination.total = res.total || 0
    }
  }).catch(() => {
    ElMessage.error('加载审计日志失败')
  }).finally(() => { loading.value = false })
}

onMounted(() => { loadData() })
</script>

<style scoped>
.audit-manage { padding: 0; }
.card-header-flex { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-size: 15px; font-weight: 600; }
.header-stats { display: flex; gap: 16px; }
.stat-tag { font-size: 13px; color: #666; }
.stat-tag strong { color: #667eea; margin-left: 4px; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
.time-text { font-size: 13px; color: #666; }
</style>