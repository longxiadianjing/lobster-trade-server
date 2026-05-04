<template>
  <div class="hot-search-manage">
    <el-card>
      <template #header>
        <div class="flex-between">
          <span>热搜词管理</span>
          <el-button type="primary" size="small" @click="showDialog = true">添加热搜词</el-button>
        </div>
      </template>
      <el-table :data="list" v-loading="loading" size="small">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="word" label="热搜词" />
        <el-table-column prop="searchCount" label="搜索次数" width="100" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '显示' : '隐藏' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" @click="toggle(row.id, row.status === 1 ? 0 : 1)">{{ row.status === 1 ? '隐藏' : '显示' }}</el-button>
            <el-button size="small" type="danger" @click="del(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :page-size="20" layout="total, prev, pager, next" :total="total" @current-change="load" style="margin-top:12px;justify-content:center" />
    </el-card>
    <el-dialog v-model="showDialog" title="添加热搜词" width="400px">
      <el-form label-width="80px">
        <el-form-item label="热搜词"><el-input v-model="form.word" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="create">添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const loading = ref(false), list = ref([]), total = ref(0), page = ref(1), showDialog = ref(false)
const form = ref({ word: '', sortOrder: 0 })
const auth = { headers: { Authorization: `Bearer ${localStorage.getItem('adminToken')}` } }

const load = async () => {
  loading.value = true
  try {
    const r = await fetch(`/api/admin/hot-search/list?page=${page.value}&size=20`, auth).then(r => r.json())
    list.value = r.data?.records || []
    total.value = r.data?.total || 0
  } catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

const create = async () => {
  if (!form.value.word) { ElMessage.warning('请输入热搜词'); return }
  try {
    await fetch('/api/admin/hot-search/create', { method: 'POST', headers: { ...auth.headers, 'Content-Type': 'application/json' }, body: JSON.stringify(form.value) })
    ElMessage.success('添加成功'); showDialog.value = false; load()
  } catch { ElMessage.error('添加失败') }
}

const toggle = async (id, status) => {
  try {
    await fetch(`/api/admin/hot-search/${id}/toggle`, { method: 'PUT', headers: { ...auth.headers, 'Content-Type': 'application/json' }, body: JSON.stringify({ status }) })
    ElMessage.success('更新成功'); load()
  } catch { ElMessage.error('操作失败') }
}

const del = async (id) => {
  try {
    await fetch(`/api/admin/hot-search/${id}/toggle`, { method: 'PUT', headers: { ...auth.headers, 'Content-Type': 'application/json' }, body: JSON.stringify({ status: -1 }) })
    ElMessage.success('已删除'); load()
  } catch { ElMessage.error('删除失败') }
}

onMounted(() => {
  load()
  document.title = '热搜词管理 - 龙虾道具交易平台'
})
</script>

<style scoped>
.flex-between { display: flex; justify-content: space-between; align-items: center; }
</style>