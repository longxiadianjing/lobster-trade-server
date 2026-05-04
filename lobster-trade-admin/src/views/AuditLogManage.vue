<template>
  <div class="audit-manage">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>操作审计日志</span>
        </div>
      </template>

      <el-form :model="filters" inline size="small">
        <el-form-item label="操作类型">
          <el-input v-model="filters.action" placeholder="如：审核用户" clearable style="width:150px" />
        </el-form-item>
        <el-form-item label="实体类型">
          <el-select v-model="filters.entityType" placeholder="全部" clearable style="width:150px">
            <el-option label="用户" value="user" />
            <el-option label="订单" value="order" />
            <el-option label="商品" value="product" />
            <el-option label="认证" value="certification" />
            <el-option label="工单" value="ticket" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束"
            value-format="YYYY-MM-DD" style="width:240px" @change="onDateChange" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData(1)">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="adminUsername" label="管理员" width="120" />
        <el-table-column prop="action" label="操作类型" width="160" />
        <el-table-column prop="entityType" label="实体类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.entityType === 'user'" type="info" size="small">用户</el-tag>
            <el-tag v-else-if="row.entityType === 'order'" type="primary" size="small">订单</el-tag>
            <el-tag v-else-if="row.entityType === 'product'" type="success" size="small">商品</el-tag>
            <el-tag v-else-if="row.entityType === 'certification'" type="warning" size="small">认证</el-tag>
            <el-tag v-else type="info" size="small">{{ row.entityType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="entityId" label="实体ID" width="100" />
        <el-table-column prop="detail" label="操作详情" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="createTime" label="操作时间" width="170" />
      </el-table>

      <el-pagination
        style="margin-top:16px"
        :current-page="page"
        :page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadData"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

const filters = reactive({ action: '', entityType: '' })
const dateRange = ref([])
const page = ref(1)
const size = ref(20)
const total = ref(0)
const tableData = ref([])
const loading = ref(false)

const onDateChange = () => {}

const reset = () => {
  filters.action = ''
  filters.entityType = ''
  dateRange.value = []
  loadData(1)
}

const buildQuery = () => {
  const params = new URLSearchParams({ page: page.value, size: size.value })
  if (filters.action) params.set('action', filters.action)
  if (filters.entityType) params.set('entityType', filters.entityType)
  if (dateRange.value && dateRange.value.length === 2) {
    params.set('startTime', dateRange.value[0] + ' 00:00:00')
    params.set('endTime', dateRange.value[1] + ' 23:59:59')
  }
  return params.toString()
}

const loadData = (p = 1) => {
  page.value = p
  loading.value = true
  fetch(`/admin/audit/list?${buildQuery()}`)
    .then(r => r.json())
    .then(r => {
      if (r.code === 0) {
        tableData.value = r.data.records || []
        total.value = r.data.total || 0
      }
      loading.value = false
    })
    .catch(() => { loading.value = false })
}

loadData()
</script>

<style scoped>
.audit-manage { padding: 0; }
.card-header { font-size: 15px; font-weight: 600; }
</style>