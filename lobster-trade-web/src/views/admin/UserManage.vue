<template>
  <div class="user-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户列表</span>
          <el-input v-model="keyword" placeholder="搜索用户名/手机号" style="width:200px" clearable @clear="loadUsers" @keyup.enter="loadUsers">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
      </template>
      <el-table :data="users" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="账号" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="reputationScore" label="信誉分" width="90">
          <template #default="{ row }">{{ row.reputationScore || '-' }}</template>
        </el-table-column>
        <el-table-column prop="totalTradeCount" label="交易笔数" width="100" />
        <el-table-column prop="balance" label="余额" width="100">
          <template #default="{ row }">¥{{ row.balance ?? 0 }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="danger" v-if="row.status === 1" @click="handleBan(row.id)">禁用</el-button>
            <el-button size="small" type="success" v-else @click="handleUnban(row.id)">解禁</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="page"
        :page-size="20"
        layout="total, prev, pager, next"
        :total="total"
        @current-change="loadUsers"
        style="margin-top:16px;justify-content:center" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminUsers, banUser, unbanUser } from '@/api/admin'
import { Search } from '@element-plus/icons-vue'

const loading = ref(false)
const users = ref([])
const total = ref(0)
const page = ref(1)
const keyword = ref('')

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await getAdminUsers({ page: page.value, size: 20, keyword: keyword.value || undefined })
    users.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

const handleBan = async (id) => {
  await ElMessageBox.confirm('确认禁用该用户？', '提示')
  try { await banUser(id); ElMessage.success('已禁用'); loadUsers() }
  catch (e) { ElMessage.error('操作失败') }
}

const handleUnban = async (id) => {
  try { await unbanUser(id); ElMessage.success('已解禁'); loadUsers() }
  catch (e) { ElMessage.error('操作失败') }
}

onMounted(() => {
  loadUsers()
  document.title = '用户管理 - 龙虾道具交易平台'
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>