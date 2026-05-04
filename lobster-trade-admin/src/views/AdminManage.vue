<template>
  <div class="admin-manage">
    <el-card shadow="never">
      <template #header>
        <div class="card-header-flex">
          <span class="card-title">管理员权限管理</span>
          <div class="filter-row">
            <el-input v-model="keyword" placeholder="搜索管理员账号" style="width: 240px;" clearable @change="loadAdmins">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-button type="primary" @click="openAddDialog">
              <el-icon><Plus /></el-icon> 添加管理员
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="admins" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="账号" width="140" />
        <el-table-column prop="nickname" label="昵称" width="140" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="row.role === 'SUPER_ADMIN' ? 'danger' : row.role === 'ADMIN' ? 'warning' : 'default'" size="small">
              {{ roleText(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="permissions" label="权限" min-width="300">
          <template #default="{ row }">
            <template v-if="row.permissions === '*'">
              <el-tag type="danger" size="small">全部权限</el-tag>
            </template>
            <template v-else>
              <el-tag
                v-for="p in splitPerms(row.permissions)"
                :key="p"
                size="small"
                type="info"
                style="margin: 2px 4px 2px 0;"
              >{{ p }}</el-tag>
            </template>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openEditDialog(row)">权限编辑</el-button>
            <el-button v-if="row.status === 1" size="small" type="danger" @click="toggleStatus(row, 2)">禁用</el-button>
            <el-button v-else size="small" type="success" @click="toggleStatus(row, 1)">启用</el-button>
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
          @current-change="loadAdmins"
        />
      </div>
    </el-card>

    <!-- 添加管理员弹窗 -->
    <el-dialog v-model="addDialogVisible" title="添加管理员" width="500px" destroy-on-close>
      <el-form :model="addForm" :rules="addRules" ref="addFormRef" label-width="80px">
        <el-form-item label="账号" prop="username">
          <el-input v-model="addForm.username" placeholder="登录账号" maxlength="30" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="addForm.nickname" placeholder="显示名称" maxlength="30" />
        </el-form-item>
        <el-form-item label="初始密码" prop="password">
          <el-input v-model="addForm.password" type="password" placeholder="登录密码" show-password maxlength="50" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="addForm.role" style="width:100%">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="运营" value="OPERATOR" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="doAddAdmin" :loading="addLoading">创建</el-button>
      </template>
    </el-dialog>

    <!-- 权限编辑弹窗 -->
    <el-dialog v-model="permDialogVisible" title="编辑管理员权限" width="720px" destroy-on-close>
      <div v-loading="permLoading">
        <div class="dialog-admin-info" v-if="currentAdmin">
          <span class="dialog-admin-name">{{ currentAdmin.username }}</span>
          <el-tag :type="currentAdmin.role === 'SUPER_ADMIN' ? 'danger' : 'default'" size="small">
            {{ roleText(currentAdmin.role) }}
          </el-tag>
        </div>

        <div class="perm-tip">勾选该管理员拥有的权限（超级管理员拥有全部权限，无需编辑）</div>

        <el-checkbox v-if="currentAdmin && currentAdmin.role !== 'SUPER_ADMIN'" v-model="isAllPerms" @change="toggleAllPerms" class="select-all-check">全选</el-checkbox>

        <div class="perm-groups" v-if="currentAdmin && currentAdmin.role !== 'SUPER_ADMIN'">
          <div v-for="group in permGroups" :key="group.label" class="perm-group">
            <div class="perm-group-label">{{ group.label }}</div>
            <el-checkbox-group v-model="selectedPerms">
              <el-checkbox
                v-for="perm in group.perms"
                :key="perm.value"
                :value="perm.value"
                :label="perm.value"
                :disabled="currentAdmin.role === 'SUPER_ADMIN'"
              >{{ perm.label }}</el-checkbox>
            </el-checkbox-group>
          </div>
        </div>

        <div v-else-if="currentAdmin && currentAdmin.role === 'SUPER_ADMIN'" class="super-admin-note">
          <el-icon><Warning /></el-icon>
          超级管理员拥有全部权限，无需配置
        </div>
      </div>
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePermissions" :loading="permLoading" :disabled="currentAdmin?.role === 'SUPER_ADMIN'">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Warning, Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const admins = ref([])
const keyword = ref('')
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })

// 权限编辑
const permDialogVisible = ref(false)
const permLoading = ref(false)
const currentAdmin = ref(null)
const selectedPerms = ref([])
const isAllPerms = computed({
  get: () => selectedPerms.value.length === allPerms.length,
  set: (v) => { selectedPerms.value = v ? [...allPerms] : [] }
})

const allPerms = [
  'USER_VIEW','USER_EDIT','USER_BAN',
  'ORDER_VIEW','ORDER_EDIT',
  'PRODUCT_VIEW','PRODUCT_EDIT','PRODUCT_AUDIT',
  'DISPUTE_VIEW','DISPUTE_HANDLE',
  'GAME_VIEW','GAME_EDIT',
  'ANNOUNCEMENT_VIEW','ANNOUNCEMENT_EDIT',
  'COUPON_VIEW','COUPON_EDIT',
  'CS_VIEW','CS_HANDLE',
  'CERT_VIEW','CERT_HANDLE',
  'NOTIFICATION_VIEW','NOTIFICATION_EDIT',
  'TICKET_VIEW','TICKET_HANDLE',
  'REVIEW_VIEW','REVIEW_EDIT',
  'HOTSEARCH_VIEW','HOTSEARCH_EDIT',
  'RECSLOT_VIEW','RECSLOT_EDIT',
  'AUDIT_VIEW',
  'ADMIN_MANAGE'
]

const permGroups = [
  {
    label: '👤 用户管理',
    perms: [
      { value: 'USER_VIEW', label: '查看用户' },
      { value: 'USER_EDIT', label: '编辑用户' },
      { value: 'USER_BAN', label: '封禁用户' }
    ]
  },
  {
    label: '📋 订单管理',
    perms: [
      { value: 'ORDER_VIEW', label: '查看订单' },
      { value: 'ORDER_EDIT', label: '编辑订单' }
    ]
  },
  {
    label: '🎮 商品管理',
    perms: [
      { value: 'PRODUCT_VIEW', label: '查看商品' },
      { value: 'PRODUCT_EDIT', label: '编辑商品' },
      { value: 'PRODUCT_AUDIT', label: '审核商品' }
    ]
  },
  {
    label: '⚖️ 纠纷管理',
    perms: [
      { value: 'DISPUTE_VIEW', label: '查看纠纷' },
      { value: 'DISPUTE_HANDLE', label: '处理纠纷' }
    ]
  },
  {
    label: '🎮 游戏管理',
    perms: [
      { value: 'GAME_VIEW', label: '查看游戏' },
      { value: 'GAME_EDIT', label: '编辑游戏' }
    ]
  },
  {
    label: '📢 公告管理',
    perms: [
      { value: 'ANNOUNCEMENT_VIEW', label: '查看公告' },
      { value: 'ANNOUNCEMENT_EDIT', label: '编辑公告' }
    ]
  },
  {
    label: '🎫 优惠券',
    perms: [
      { value: 'COUPON_VIEW', label: '查看优惠券' },
      { value: 'COUPON_EDIT', label: '编辑优惠券' }
    ]
  },
  {
    label: '💬 客服',
    perms: [
      { value: 'CS_VIEW', label: '查看会话' },
      { value: 'CS_HANDLE', label: '处理会话' }
    ]
  },
  {
    label: '📛 实名认证',
    perms: [
      { value: 'CERT_VIEW', label: '查看认证' },
      { value: 'CERT_HANDLE', label: '审核认证' }
    ]
  },
  {
    label: '🔔 通知管理',
    perms: [
      { value: 'NOTIFICATION_VIEW', label: '查看通知' },
      { value: 'NOTIFICATION_EDIT', label: '编辑通知' }
    ]
  },
  {
    label: '🎫 工单管理',
    perms: [
      { value: 'TICKET_VIEW', label: '查看工单' },
      { value: 'TICKET_HANDLE', label: '处理工单' }
    ]
  },
  {
    label: '⭐ 评价管理',
    perms: [
      { value: 'REVIEW_VIEW', label: '查看评价' },
      { value: 'REVIEW_EDIT', label: '编辑评价' }
    ]
  },
  {
    label: '🔥 热搜词',
    perms: [
      { value: 'HOTSEARCH_VIEW', label: '查看热搜' },
      { value: 'HOTSEARCH_EDIT', label: '编辑热搜' }
    ]
  },
  {
    label: '📌 推荐位',
    perms: [
      { value: 'RECSLOT_VIEW', label: '查看推荐位' },
      { value: 'RECSLOT_EDIT', label: '编辑推荐位' }
    ]
  },
  {
    label: '📝 审计日志',
    perms: [
      { value: 'AUDIT_VIEW', label: '查看审计日志' }
    ]
  },
  {
    label: '🔐 管理员管理',
    perms: [
      { value: 'ADMIN_MANAGE', label: '管理其他管理员' }
    ]
  }
]

const roleText = (r) => ({ SUPER_ADMIN: '超级管理员', ADMIN: '管理员', OPERATOR: '运营' }[r] || r)

const splitPerms = (p) => p ? p.split(',').filter(Boolean) : []

const loadAdmins = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/admins', {
      params: { page: pagination.page, pageSize: pagination.pageSize, keyword: keyword.value }
    })
    if (res.data) {
      admins.value = res.data.records || res.data
      pagination.total = res.data.total || 0
    }
  } catch (e) {
    ElMessage.error('加载管理员列表失败')
  } finally {
    loading.value = false
  }
}

const openEditDialog = (row) => {
  currentAdmin.value = row
  selectedPerms.value = row.permissions === '*' ? [...allPerms] : splitPerms(row.permissions)
  permDialogVisible.value = true
}

const toggleAllPerms = (checked) => {
  selectedPerms.value = checked ? [...allPerms] : []
}

const savePermissions = async () => {
  if (!currentAdmin.value) return
  permLoading.value = true
  try {
    await request.post(`/admin/${currentAdmin.value.id}/permissions`, {
      permissions: selectedPerms.value.join(',')
    })
    ElMessage.success('权限保存成功')
    permDialogVisible.value = false
    loadAdmins()
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    permLoading.value = false
  }
}

const toggleStatus = async (row, status) => {
  const action = status === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确认${action}管理员「${row.username}」？`, `${action}管理员`, { type: 'warning' })
    await request.post(`/admin/${row.id}/status`, { status })
    ElMessage.success(`${action}成功`)
    loadAdmins()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(`${action}失败`)
  }
}

onMounted(() => { loadAdmins() })

// 添加管理员
const addDialogVisible = ref(false)
const addLoading = ref(false)
const addFormRef = ref(null)
const addForm = reactive({ username: '', nickname: '', password: '', role: 'ADMIN' })
const addRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }]
}

const openAddDialog = () => {
  addForm.username = ''
  addForm.nickname = ''
  addForm.password = ''
  addForm.role = 'ADMIN'
  addDialogVisible.value = true
}

const doAddAdmin = async () => {
  await addFormRef.value.validate()
  addLoading.value = true
  try {
    await request.post('/admin', {
      username: addForm.username,
      nickname: addForm.nickname,
      password: addForm.password,
      role: addForm.role
    })
    ElMessage.success('添加成功')
    addDialogVisible.value = false
    loadAdmins()
  } catch (e) {
    // error already shown by interceptor
  } finally {
    addLoading.value = false
  }
}
</script>

<style scoped>
.card-header-flex { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 10px; }
.card-title { font-size: 15px; font-weight: 600; color: #333; }
.filter-row { display: flex; gap: 10px; align-items: center; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
.dialog-admin-info { display: flex; align-items: center; gap: 10px; margin-bottom: 16px; padding-bottom: 16px; border-bottom: 1px solid #f0f0f0; }
.dialog-admin-name { font-size: 16px; font-weight: 600; }
.perm-tip { font-size: 13px; color: #999; margin-bottom: 12px; }
.select-all-check { margin-bottom: 12px; }
.perm-groups { display: flex; flex-direction: column; gap: 16px; max-height: 480px; overflow-y: auto; }
.perm-group { border: 1px solid #f0f0f0; border-radius: 8px; padding: 12px 16px; }
.perm-group-label { font-size: 13px; font-weight: 600; color: #333; margin-bottom: 10px; }
:deep(.el-checkbox) { margin-right: 16px; margin-bottom: 8px; width: 140px; }
.super-admin-note { display: flex; align-items: center; gap: 8px; color: #999; font-size: 14px; padding: 20px 0; justify-content: center; }
</style>
