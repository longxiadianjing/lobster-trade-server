<template>
  <div class="user-manage">
    <el-card shadow="never">
      <template #header>
        <div class="card-header-flex">
          <span class="card-title">用户管理</span>
          <div class="filter-row">
            <el-input v-model="keyword" placeholder="搜索用户ID/手机/昵称" style="width: 260px;" clearable @change="loadUsers">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-select v-model="filterStatus" placeholder="用户状态" style="width: 140px;" clearable @change="loadUsers">
              <el-option label="正常" value="1" />
              <el-option label="封禁" value="2" />
              <el-option label="冻结" value="3" />
            </el-select>
          </div>
        </div>
      </template>

      <el-table :data="users" v-loading="loading" stripe>
        <el-table-column prop="id" label="用户ID" width="100" />
        <el-table-column prop="nickname" label="昵称" width="140" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="realName" label="实名" width="100">
          <template #default="{ row }">
            <span v-if="row.realNameStatus === 1">{{ row.realName }}</span>
            <el-tag v-else-if="row.realNameStatus === 2" type="warning" size="small">审核中</el-tag>
            <el-tag v-else type="info" size="small">未实名</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="余额" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ row.balance || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="userLevel" label="等级" width="80">
          <template #default="{ row }">
            <el-tag :type="levelType(row.userLevel)" size="small">{{ levelText(row.userLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalTradeCount" label="交易次数" width="100" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="viewDetail(row)">详情</el-button>
            <el-button size="small" type="info" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 1" size="small" type="danger" @click="handleBan(row)">封禁</el-button>
            <el-button v-if="row.status === 2" size="small" type="success" @click="handleUnban(row)">解封</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, prev, pager, next"
          @current-change="loadUsers"
        />
      </div>
    </el-card>

    <!-- 用户详情弹窗 -->
    <el-dialog v-model="showDetail" title="用户详情" width="560px">
      <el-descriptions :column="2" border v-if="currentUser">
        <el-descriptions-item label="用户ID">{{ currentUser.id }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentUser.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ currentUser.nickname || '-' }}</el-descriptions-item>
        <el-descriptions-item label="用户等级">
          <el-tag :type="levelType(currentUser.userLevel)" size="small">{{ levelText(currentUser.userLevel) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="实名姓名" :span="2">
          <span v-if="currentUser.realNameStatus === 1">{{ currentUser.realName || '-' }}</span>
          <el-tag v-else-if="currentUser.realNameStatus === 2" type="warning" size="small">审核中</el-tag>
          <el-tag v-else type="info" size="small">未实名</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="余额">
          <span class="price-text">¥{{ currentUser.balance || '0.00' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="用户状态">
          <el-tag :type="statusType(currentUser.status)" size="small">{{ statusText(currentUser.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="交易次数">{{ currentUser.totalTradeCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="信誉分">{{ currentUser.reputationScore || '-' }}</el-descriptions-item>
        <el-descriptions-item label="注册时间" :span="2">{{ currentUser.createTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="showDetail = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 用户编辑弹窗 -->
    <el-dialog v-model="editVisible" title="编辑用户" width="460px" destroy-on-close>
      <el-form :model="editForm" label-width="85px" v-loading="editLoading">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" placeholder="用户昵称" maxlength="30" />
        </el-form-item>
        <el-form-item label="用户等级">
          <el-select v-model="editForm.userLevel" style="width:100%">
            <el-option label="普通用户" :value="1" />
            <el-option label="铜牌服务商" :value="2" />
            <el-option label="银牌服务商" :value="3" />
            <el-option label="金牌服务商" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户状态">
          <el-select v-model="editForm.status" style="width:100%">
            <el-option label="正常" :value="1" />
            <el-option label="封禁" :value="2" />
            <el-option label="冻结" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="余额">
          <el-input-number v-model="editForm.balance" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="doEdit" :loading="editLoading">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
document.title = '用户管理 - 龙虾道具交易平台'
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const users = ref([])
const keyword = ref('')
const filterStatus = ref('')
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })

const showDetail = ref(false)
const currentUser = ref(null)

const statusMap = { 1: '正常', 2: '封禁', 3: '冻结' }
const statusTypeMap = { 1: 'success', 2: 'danger', 3: 'warning' }
const statusText = (s) => statusMap[s] || s
const statusType = (s) => statusTypeMap[s] || ''
const levelText = (l) => ['普通', '铜牌', '银牌', '金牌'][l - 1] || '普通'
const levelType = (l) => ['', 'default', 'warning', 'success'][l - 1] || ''

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/users', {
      params: { page: pagination.page, pageSize: pagination.pageSize, keyword: keyword.value, status: filterStatus.value }
    })
    if (res.data) {
      users.value = res.data.records || res.data
      pagination.total = res.data.total || 0
    }
  } catch (e) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleBan = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要封禁用户【${row.nickname}】吗？封禁后该用户将无法登录平台。`,
      '⚠️ 确认封禁',
      { confirmButtonText: '确认封禁', cancelButtonText: '取消', type: 'warning' }
    )
    await request.post(`/admin/user/${row.id}/ban`)
    ElMessage.success('已封禁')
    loadUsers()
  } catch (e) {}
}

const handleUnban = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要解封用户【${row.nickname}】吗？解封后用户可恢复正常登录。`,
      '确认解封',
      { confirmButtonText: '确认', cancelButtonText: '取消', type: 'info' }
    )
    await request.post(`/admin/user/${row.id}/unban`)
    ElMessage.success('已解封')
    loadUsers()
  } catch (e) {}
}

const viewDetail = (row) => {
  currentUser.value = row
  showDetail.value = true
}

const editVisible = ref(false)
const editLoading = ref(false)
const editId = ref(null)
const editForm = reactive({
  nickname: '', userLevel: null, status: null, balance: null
})

const handleEdit = (row) => {
  editId.value = row.id
  editForm.nickname = row.nickname || ''
  editForm.userLevel = row.userLevel
  editForm.status = row.status
  editForm.balance = row.balance
  editVisible.value = true
}

const doEdit = async () => {
  if (!editId.value) return
  editLoading.value = true
  try {
    await request.put(`/admin/user/${editId.value}`, editForm)
    ElMessage.success('保存成功')
    editVisible.value = false
    loadUsers()
  } catch (e) { ElMessage.error('保存失败') }
  finally { editLoading.value = false }
}

onMounted(() => { loadUsers() })
</script>

<style scoped>
.card-header-flex { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 10px; }
.card-title { font-size: 15px; font-weight: 600; color: #333; }
.filter-row { display: flex; gap: 10px; align-items: center; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
.price { font-weight: 600; color: #667eea; }
.price-text { font-weight: 600; color: #667eea; }
</style>
