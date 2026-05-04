<template>
  <div class="review-manage">
    <el-card>
      <template #header><span>评价管理</span></template>
      <el-table :data="list" v-loading="loading" size="small">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="商品" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ row.productTitle || ('商品#' + row.productId) }}</template>
        </el-table-column>
        <el-table-column label="评分" width="90">
          <template #default="{ row }">
            <el-rate :model-value="Number(row.rating)" disabled text-color="#ff9900" />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评价内容" min-width="180" show-overflow-tooltip />
        <el-table-column prop="isHidden" label="状态" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="row.isHidden === 0 ? 'success' : 'info'">{{ row.isHidden === 0 ? '显示' : '隐藏' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="140" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button size="small" @click="toggle(row.id, row.isHidden === 0 ? 1 : 0)">{{ row.isHidden === 0 ? '隐藏' : '显示' }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :page-size="20" layout="total, prev, pager, next" :total="total" @current-change="load" style="margin-top:12px;justify-content:center" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const loading = ref(false), list = ref([]), total = ref(0), page = ref(1)
const auth = { headers: { Authorization: `Bearer ${localStorage.getItem('adminToken')}` } }

const load = async () => {
  loading.value = true
  try {
    const r = await fetch(`/api/admin/review/list?page=${page.value}&size=20`, auth).then(r => r.json())
    list.value = r.data?.records || []
    total.value = r.data?.total || 0
  } catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

const toggle = async (id, isHidden) => {
  try {
    await fetch('/api/admin/review/hide', { method: 'PUT', headers: { ...auth.headers, 'Content-Type': 'application/json' }, body: JSON.stringify({ id, isHidden }) })
    ElMessage.success('更新成功')
    load()
  } catch { ElMessage.error('操作失败') }
}

onMounted(() => {
  load()
  document.title = '评价管理 - 龙虾道具交易平台'
})
</script>