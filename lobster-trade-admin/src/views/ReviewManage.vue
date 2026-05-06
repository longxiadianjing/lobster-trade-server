<template>
  <div class="review-manage">
    <el-card shadow="never">
      <template #header>
        <div class="card-header-flex">
          <span class="card-title">评价管理</span>
          <el-tag type="info">买卖互评，订单完成后可评价</el-tag>
        </div>
      </template>

      <div class="filter-row">
        <el-input v-model="filters.keyword" placeholder="搜索评价内容" style="width:200px" clearable @clear="loadData" @keyup.enter="loadData" />
        <el-select v-model="filters.rating" placeholder="评分" style="width:120px" clearable @change="loadData">
          <el-option label="5星" :value="5" />
          <el-option label="4星" :value="4" />
          <el-option label="3星" :value="3" />
          <el-option label="2星" :value="2" />
          <el-option label="1星" :value="1" />
        </el-select>
        <el-button type="primary" @click="loadData">搜索</el-button>
      </div>

      <el-table :data="list" v-loading="loading" stripe class="mt-16">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="orderId" label="订单ID" width="80" />
        <el-table-column prop="reviewerId" label="评价人" width="80" />
        <el-table-column prop="reviewedId" label="被评人" width="80" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.role === 1 ? 'primary' : 'success'">
              {{ row.role === 1 ? '买家评卖家' : '卖家评买家' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rating" label="评分" width="80">
          <template #default="{ row }">
            <el-rate v-model="row.rating" disabled size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评价内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="isAnonymous" label="匿名" width="70">
          <template #default="{ row }">
            <el-tag :type="row.isAnonymous === 1 ? 'warning' : 'info'" size="small">
              {{ row.isAnonymous === 1 ? '匿名' : '公开' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isHidden" label="状态" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isHidden === 1" type="danger" size="small">已隐藏</el-tag>
            <span v-else class="text-muted">正常</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="评价时间" width="160" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.isHidden === 0" size="small" type="danger" plain @click="hideRow(row, true)">隐藏</el-button>
            <el-button v-else size="small" type="success" @click="hideRow(row, false)">显示</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="loadData"
        class="mt-16"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const filters = reactive({ keyword: '', rating: '' })

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/review/list', {
      params: { page: currentPage.value, size: pageSize.value, keyword: filters.keyword || undefined, rating: filters.rating || undefined }
    })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) { /* handled */ }
  finally { loading.value = false }
}

const hideRow = async (row, hide) => {
  try {
    await request.put('/admin/review/hide', { id: row.id, isHidden: hide })
    ElMessage.success(hide ? '已隐藏' : '已显示')
    loadData()
  } catch (e) { /* handled */ }
}

onMounted(() => { loadData() })
</script>

<style scoped>
.review-manage { padding: 0; }
.card-header-flex { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-size: 15px; font-weight: 600; }
.filter-row { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
.mt-16 { margin-top: 16px; }
.text-muted { color: #999; font-size: 13px; }
</style>