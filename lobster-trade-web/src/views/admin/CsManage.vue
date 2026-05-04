<template>
  <div class="cs-manage">
    <el-card>
      <template #header>
        <div class="flex-between">
          <span>客服会话</span>
          <el-tag type="success">{{ stats.active || 0 }} 进行中</el-tag>
        </div>
      </template>
      <el-table :data="list" v-loading="loading" size="small">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="adminId" label="客服ID" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="['','warning','primary','success','danger'][row.status]">
              {{ ['','待接待','进行中','已完成','已关闭'][row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="140" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="viewSession(row.id)">查看</el-button>
            <el-button v-if="row.status !== 4" size="small" type="danger" @click="close(row.id)">关闭</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :page-size="20" layout="total, prev, pager, next" :total="total" @current-change="load" style="margin-top:12px;justify-content:center" />
    </el-card>
    <el-dialog v-model="showDialog" title="会话详情" width="700px">
      <div v-if="currentMessages.length" class="msg-list">
        <div v-for="m in currentMessages" :key="m.id" :class="['msg-item', m.isAdmin ? 'admin-msg' : 'user-msg']">
          <span class="msg-role">{{ m.isAdmin ? '客服' : '用户' }}</span>
          <span class="msg-content">{{ m.content }}</span>
          <span class="msg-time">{{ m.createTime }}</span>
        </div>
      </div>
      <div v-else style="text-align:center;color:#999;padding:40px">暂无消息</div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const loading = ref(false), list = ref([]), total = ref(0), page = ref(1), stats = ref({})
const showDialog = ref(false), currentMessages = ref([])
const auth = { headers: { Authorization: `Bearer ${localStorage.getItem('adminToken')}` } }

const load = async () => {
  loading.value = true
  try {
    const r = await Promise.all([
      fetch(`/api/admin/cs/sessions?page=${page.value}&size=20`, auth).then(r => r.json()),
      fetch('/api/admin/cs/stats', auth).then(r => r.json()),
    ])
    list.value = r[0].data?.records || []
    total.value = r[0].data?.total || 0
    stats.value = r[1].data || {}
  } catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

const viewSession = async (id) => {
  showDialog.value = true
  try {
    const r = await fetch(`/api/admin/cs/session/${id}`, auth).then(r => r.json())
    currentMessages.value = r.data || []
  } catch { ElMessage.error('加载失败') }
}

const close = async (id) => {
  try {
    await fetch(`/api/admin/cs/close/${id}`, { method: 'POST', headers: auth.headers })
    ElMessage.success('已关闭'); load()
  } catch { ElMessage.error('操作失败') }
}

onMounted(() => {
  load()
  document.title = '客服管理 - 龙虾道具交易平台'
})
</script>

<style scoped>
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.msg-list { max-height: 400px; overflow-y: auto; display: flex; flex-direction: column; gap: 10px; }
.msg-item { padding: 10px; border-radius: 8px; display: flex; flex-direction: column; gap: 4px; }
.user-msg { background: #f0f0f0; align-self: flex-end; }
.admin-msg { background: #e8f0ff; align-self: flex-start; }
.msg-role { font-size: 11px; color: #999; font-weight: 600; }
.msg-content { font-size: 14px; }
.msg-time { font-size: 11px; color: #bbb; align-self: flex-end; }
</style>