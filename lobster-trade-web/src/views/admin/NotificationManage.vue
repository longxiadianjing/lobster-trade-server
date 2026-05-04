<template>
  <div class="notif-manage">
    <el-card>
      <template #header>
        <div class="flex-between">
          <span>系统通知</span>
          <el-button type="primary" size="small" @click="showDialog = true">发送通知</el-button>
        </div>
      </template>
      <el-table :data="list" v-loading="loading" size="small">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="90">
          <template #default="{ row }">{{ ['','系统','账户','订单','其他'][row.type] }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="发送时间" width="140" />
      </el-table>
      <el-pagination v-model:current-page="page" :page-size="20" layout="total, prev, pager, next" :total="total" @current-change="load" style="margin-top:12px;justify-content:center" />
    </el-card>
    <el-dialog v-model="showDialog" title="发送系统通知" width="450px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="类型"><el-select v-model="form.type" style="width:100%"><el-option label="系统通知" :value="1" /><el-option label="账户通知" :value="2" /><el-option label="订单通知" :value="3" /><el-option label="其他" :value="4" /></el-select></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="create">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const loading = ref(false), list = ref([]), total = ref(0), page = ref(1), showDialog = ref(false)
const form = ref({ title: '', content: '', type: 1 })
const auth = { headers: { Authorization: `Bearer ${localStorage.getItem('adminToken')}` } }

const load = async () => {
  loading.value = true
  try {
    const r = await fetch(`/api/admin/notification/list?page=${page.value}&size=20`, auth).then(r => r.json())
    list.value = r.data?.records || []
    total.value = r.data?.total || 0
  } catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

const create = async () => {
  if (!form.value.title) { ElMessage.warning('请输入标题'); return }
  try {
    await fetch('/api/admin/notification/create', { method: 'POST', headers: { ...auth.headers, 'Content-Type': 'application/json' }, body: JSON.stringify(form.value) })
    ElMessage.success('发送成功'); showDialog.value = false; load()
  } catch { ElMessage.error('发送失败') }
}

onMounted(() => {
  load()
  document.title = '通知管理 - 龙虾道具交易平台'
})
</script>

<style scoped>
.flex-between { display: flex; justify-content: space-between; align-items: center; }
</style>