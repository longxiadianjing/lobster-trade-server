<template>
  <div class="audit-log">
    <el-card>
      <template #header><span>审计日志</span></template>
      <el-table :data="list" v-loading="loading" size="small">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="operatorId" label="操作者ID" width="100" />
        <el-table-column prop="action" label="操作类型" width="120" />
        <el-table-column prop="targetType" label="目标类型" width="100" />
        <el-table-column prop="targetId" label="目标ID" width="80" />
        <el-table-column prop="detail" label="详情" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="时间" width="140" />
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
    const r = await fetch(`/api/admin/audit/list?page=${page.value}&size=20`, auth).then(r => r.json())
    list.value = r.data?.records || []
    total.value = r.data?.total || 0
  } catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

onMounted(() => {
  load()
  document.title = '审计日志 - 龙虾道具交易平台'
})
</script>