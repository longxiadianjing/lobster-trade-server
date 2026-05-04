<template>
  <div class="coupon-manage">
    <el-card>
      <template #header>
        <div class="flex-between">
          <span>优惠券管理</span>
          <el-button type="primary" size="small" @click="showDialog = true">创建优惠券</el-button>
        </div>
      </template>
      <el-table :data="list" v-loading="loading" size="small">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="名称" min-width="120" />
        <el-table-column prop="type" label="类型" width="90">
          <template #default="{ row }">{{ row.type == 1 ? '满减' : '折扣' }}</template>
        </el-table-column>
        <el-table-column label="面值/折扣" width="100">
          <template #default="{ row }">{{ row.discountValue }}{{ row.type == 1 ? '元' : '折' }}</template>
        </el-table-column>
        <el-table-column prop="minAmount" label="门槛" width="90">
          <template #default="{ row }">满{{ row.minAmount }}元</template>
        </el-table-column>
        <el-table-column prop="totalCount" label="总数量" width="90" />
        <el-table-column prop="usedCount" label="已使用" width="90" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '生效中' : '已禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" @click="toggle(row.id, row.status === 1 ? 0 : 1)">{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :page-size="20" layout="total, prev, pager, next" :total="total" @current-change="load" style="margin-top:12px;justify-content:center" />
    </el-card>
    <el-dialog v-model="showDialog" title="创建优惠券" width="450px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="类型"><el-select v-model="form.type" style="width:100%"><el-option label="满减券" :value="1" /><el-option label="折扣券" :value="2" /></el-select></el-form-item>
        <el-form-item label="面值/折扣"><el-input-number v-model="form.discountValue" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="使用门槛"><el-input-number v-model="form.minAmount" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="总数量"><el-input-number v-model="form.totalCount" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="有效期"><el-date-picker v-model="form.expireTime" type="datetime" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="create" :loading="creating">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const loading = ref(false), list = ref([]), total = ref(0), page = ref(1)
const showDialog = ref(false), creating = ref(false)
const form = ref({ name: '', type: 1, discountValue: 10, minAmount: 100, totalCount: 100, expireTime: null })

const load = async () => {
  loading.value = true
  try {
    const r = await fetch(`/api/admin/coupon/list?page=${page.value}&size=20`, { headers: { Authorization: `Bearer ${localStorage.getItem('adminToken')}` } }).then(r => r.json())
    list.value = r.data?.records || []
    total.value = r.data?.total || 0
  } catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

const toggle = async (id, status) => {
  try {
    await fetch(`/api/admin/coupon/${id}/toggle`, { method: 'POST', headers: { Authorization: `Bearer ${localStorage.getItem('adminToken')}`, 'Content-Type': 'application/json' }, body: JSON.stringify({ status }) })
    ElMessage.success('更新成功')
    load()
  } catch { ElMessage.error('操作失败') }
}

const create = async () => {
  creating.value = true
  try {
    await fetch('/api/admin/coupon', { method: 'POST', headers: { Authorization: `Bearer ${localStorage.getItem('adminToken')}`, 'Content-Type': 'application/json' }, body: JSON.stringify(form.value) })
    ElMessage.success('创建成功')
    showDialog.value = false
    load()
  } catch { ElMessage.error('创建失败') }
  finally { creating.value = false }
}

onMounted(() => {
  load()
  document.title = '优惠券管理 - 龙虾道具交易平台'
})
</script>

<style scoped>
.flex-between { display: flex; justify-content: space-between; align-items: center; }
</style>