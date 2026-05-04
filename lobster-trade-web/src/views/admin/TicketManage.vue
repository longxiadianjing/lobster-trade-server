<template>
  <div class="ticket-manage">
    <el-card>
      <template #header>
        <div class="flex-between">
          <span>工单管理</span>
          <el-tag type="warning">{{ stats.pending || 0 }} 待处理</el-tag>
        </div>
      </template>
      <el-table :data="list" v-loading="loading" size="small">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="['','warning','primary','success','danger'][row.status]">
              {{ ['','待处理','处理中','已完成','已关闭'][row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="140" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" size="small" type="primary" @click="assign(row.id)">分配</el-button>
            <el-button v-if="row.status === 2" size="small" type="success" @click="resolve(row.id)">完成</el-button>
            <el-button v-if="row.status !== 4" size="small" type="danger" @click="close(row.id)">关闭</el-button>
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

const loading = ref(false), list = ref([]), total = ref(0), page = ref(1), stats = ref({})
const auth = { headers: { Authorization: `Bearer ${localStorage.getItem('adminToken')}` } }

const load = async () => {
  loading.value = true
  try {
    const r = await Promise.all([
      fetch(`/api/admin/ticket/list?page=${page.value}&size=20`, auth).then(r => r.json()),
      fetch('/api/admin/ticket/stats', auth).then(r => r.json()),
    ])
    list.value = r[0].data?.records || []
    total.value = r[0].data?.total || 0
    stats.value = r[1].data || {}
  } catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

const assign = async (id) => {
  try {
    await fetch('/api/admin/ticket/assign', { method: 'POST', headers: { ...auth.headers, 'Content-Type': 'application/json' }, body: JSON.stringify({ ticketId: id }) })
    ElMessage.success('已分配'); load()
  } catch { ElMessage.error('操作失败') }
}

const resolve = async (id) => {
  try {
    await fetch(`/api/admin/ticket/handle`, { method: 'PUT', headers: { ...auth.headers, 'Content-Type': 'application/json' }, body: JSON.stringify({ ticketId: id }) })
    ElMessage.success('已标记完成'); load()
  } catch { ElMessage.error('操作失败') }
}

const close = async (id) => {
  try {
    await fetch(`/api/admin/ticket/close/${id}`, { method: 'PUT', headers: auth.headers })
    ElMessage.success('已关闭'); load()
  } catch { ElMessage.error('操作失败') }
}

onMounted(() => {
  load()
  document.title = '工单管理 - 龙虾道具交易平台'
})
</script>