<template>
  <div class="admin-manage-page">
    <div class="page-header">
      <h2>管理员管理</h2>
      <el-button type="primary" @click="openCreateDialog">
        <el-icon><Plus /></el-icon> 新增管理员
      </el-button>
    </div>

    <!-- 管理员列表 -->
    <el-table :data="admins" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="账号" width="140" />
      <el-table-column prop="nickname" label="昵称" width="140" />
      <el-table-column prop="role" label="角色" width="140">
        <template #default="{ row }">
          <el-tag :type="row.role === 'SUPER_ADMIN' ? 'danger' : row.role === 'OPERATOR' ? 'warning' : 'info'" size="small">
            {{ roleNameMap[row.role] || row.role }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="permissions" label="权限范围" min-width="200">
        <template #default="{ row }">
          <span v-if="row.permissions === '*'" class="perm-all">全部权限</span>
          <span v-else-if="!row.permissions">未分配</span>
          <el-tooltip v-else :content="row.permissions" placement="top" :show-after="300">
            <span class="perm-preview">{{ row.permissions.slice(0, 40) }}{{ row.permissions.length > 40 ? '...' : '' }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="lastLoginTime" label="最后登录" width="160">
        <template #default="{ row }">
          {{ row.lastLoginTime ? row.lastLoginTime.slice(0, 16) : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button size="small" link type="primary" @click="openEditDialog(row)">编辑</el-button>
          <el-button size="small" link type="warning" @click="openPermDialog(row)">权限</el-button>
          <el-button size="small" link :type="row.status === 1 ? 'danger' : 'success'" @click="toggleStatus(row)" :disabled="row.role === 'SUPER_ADMIN'">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button size="small" link type="danger" @click="handleDeleteAdmin(row)" :disabled="row.role === 'SUPER_ADMIN'">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑管理员' : '新增管理员'" width="500px">
      <el-form :model="form" label-width="80" ref="formRef">
        <el-form-item label="账号" required>
          <el-input v-model="form.username" placeholder="登录账号" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="密码" :required="!isEdit">
          <el-input v-model="form.password" type="password" show-password :placeholder="isEdit ? '留空则不修改' : '登录密码'" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="显示名称" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" placeholder="选择角色">
            <el-option label="运营管理员" value="OPERATOR" />
            <el-option label="客服" value="CS_AGENT" />
            <el-option label="普通管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 权限分配对话框 -->
    <el-dialog v-model="permDialogVisible" title="分配权限" width="600px">
      <div v-if="permForm.id">
        <p style="margin-bottom:16px;color:#666;">
          管理员：<strong>{{ permForm.username }}</strong>
          <el-tag size="small" style="margin-left:8px">{{ roleNameMap[permForm.role] || permForm.role }}</el-tag>
        </p>
        <el-checkbox v-model="permAll" @change="toggleAllPerms" :indeterminate="permIndeterminate" style="margin-bottom:12px">
          全部权限（*）
        </el-checkbox>
        <el-divider style="margin:12px 0" />
        <div class="perm-grid">
          <el-checkbox-group v-model="permForm.permissions">
            <el-checkbox v-for="(label, key) in permissionMap" :key="key" :label="key" :value="key">
              {{ label }}
            </el-checkbox>
          </el-checkbox-group>
        </div>
      </div>
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPerms" :loading="submitting">保存权限</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getAdminList, createAdmin, updateAdmin, deleteAdmin, updateAdminStatus, grantPermissions, getPermissions, getRoles } from '@/api/admin'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const permDialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const permAll = ref(false)
const permIndeterminate = ref(false)
const permissionMap = ref({})
const roleNameMap = { SUPER_ADMIN: '超级管理员', OPERATOR: '运营管理员', CS_AGENT: '客服', ADMIN: '普通管理员' }

const admins = ref([])
const form = reactive({ id: null, username: '', password: '', nickname: '', role: 'OPERATOR' })
const permForm = reactive({ id: null, username: '', role: '', permissions: [] })

onMounted(async () => {
  await Promise.all([loadAdmins(), loadPermissions()])

  document.title = '管理员管理 - 龙虾道具交易平台'
})

async function loadAdmins() {
  loading.value = true
  try {
    const res = await getAdminList({ page: 1, size: 100 })
    admins.value = res.data?.records || []
  } catch (e) {
    ElMessage.error('加载管理员列表失败')
  } finally {
    loading.value = false
  }
}

async function loadPermissions() {
  try {
    const res = await getPermissions()
    permissionMap.value = res.data || {}
  } catch (e) {
    console.error('load permissions failed', e)
  }
}

function openCreateDialog() {
  isEdit.value = false
  Object.assign(form, { id: null, username: '', password: '', nickname: '', role: 'OPERATOR' })
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  Object.assign(form, { id: row.id, username: row.username, password: '', nickname: row.nickname || '', role: row.role })
  dialogVisible.value = true
}

async function submitForm() {
  if (!form.username) { ElMessage.warning('请输入账号'); return }
  if (!isEdit.value && !form.password) { ElMessage.warning('请输入密码'); return }
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateAdmin(form.id, { nickname: form.nickname, role: form.role, password: form.password || undefined })
      ElMessage.success('更新成功')
    } else {
      await createAdmin({ ...form })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await loadAdmins()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

async function toggleStatus(row) {
  const action = row.status === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}管理员 [${row.username}] 吗？`, '提示')
    await updateAdminStatus(row.id, row.status === 1 ? 2 : 1)
    ElMessage.success(`${action}成功`)
    await loadAdmins()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

async function handleDeleteAdmin(row) {
  try {
    await ElMessageBox.confirm(`确定要删除管理员 [${row.username}] 吗？此操作不可恢复！`, '危险操作', { type: 'warning' })
    await deleteAdmin(row.id)
    ElMessage.success('删除成功')
    await loadAdmins()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

function openPermDialog(row) {
  Object.assign(permForm, {
    id: row.id,
    username: row.username,
    role: row.role,
    permissions: row.permissions && row.permissions !== '*' ? row.permissions.split(',').filter(Boolean) : []
  })
  permAll.value = row.permissions === '*'
  permIndeterminate.value = !permAll.value && permForm.permissions.length > 0
  permDialogVisible.value = true
}

function toggleAllPerms(val) {
  permForm.permissions = val ? Object.keys(permissionMap.value) : []
  permIndeterminate.value = false
}

async function submitPerms() {
  submitting.value = true
  try {
    const permissions = permAll.value ? '*' : permForm.permissions.join(',')
    await grantPermissions(permForm.id, permissions)
    ElMessage.success('权限保存成功')
    permDialogVisible.value = false
    await loadAdmins()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.admin-manage-page { padding: 24px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 18px; color: #303133; }
.perm-all { color: #f56c6c; font-weight: 500; }
.perm-preview { color: #909399; font-size: 12px; cursor: pointer; }
.perm-grid { max-height: 350px; overflow-y: auto; }
.perm-grid :deep(.el-checkbox) { width: 180px; margin-right: 0; margin-bottom: 8px; }
</style>
