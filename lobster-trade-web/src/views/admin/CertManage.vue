<template>
  <div class="cert-manage">
    <el-card>
      <template #header><span>实名认证管理</span></template>
      <el-table :data="list" v-loading="loading" size="small">
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="realName" label="真实姓名" width="100" />
        <el-table-column prop="idNumber" label="身份证号" width="160" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : row.status === 2 ? 'danger' : 'warning'">
              {{ ['','已通过','已拒绝','待审核'][row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="140" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button size="small" type="success" @click="review(row.userId, 1)">通过</el-button>
              <el-button size="small" type="danger" @click="review(row.userId, 2)">拒绝</el-button>
            </template>
            <span v-else style="color:#999">已处理</span>
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
    const r = await fetch(`/api/admin/real-name/list?page=${page.value}&size=20`, auth).then(r => r.json())
    list.value = r.data?.records || []
    total.value = r.data?.total || 0
  } catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

const review = async (userId, status) => {
  const url = status === 1 ? `/api/admin/real-name/approve/${userId}` : `/api/admin/real-name/reject/${userId}`
  try {
    await fetch(url, { method: 'POST', headers: auth.headers })
    ElMessage.success(status === 1 ? '已通过' : '已拒绝')
    load()
  } catch { ElMessage.error('操作失败') }
}

onMounted(() => {
  load()
  document.title = '认证管理 - 龙虾道具交易平台'
})
</script>