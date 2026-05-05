<template>
  <div class="coupon-manage">
    <div class="page-header">
      <h2>优惠券管理</h2>
      <el-button type="primary" @click="openDialog('create')">新建优惠券</el-button>
      <el-button type="success" @click="handleDistributeAll">发放给所有用户</el-button>
    </div>

    <!-- 搜索栏 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="名称">
          <el-input v-model="filters.name" placeholder="优惠券名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadCoupons">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="coupons" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="名称" min-width="150">
          <template #default="{ row }">
            <div class="coupon-name">{{ row.name }}</div>
            <div class="coupon-desc">{{ row.description }}</div>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.type)" size="small">
              {{ typeMap[row.type] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="优惠内容" width="140">
          <template #default="{ row }">
            <span v-if="row.type === 1">满{{ row.minAmount }}减{{ row.discountValue }}</span>
            <span v-else-if="row.type === 2">{{ (row.discountRate * 10).toFixed(1) }}折</span>
            <span v-else-if="row.type === 3">充{{ row.rechargeAmount }}送{{ row.giftAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="发行/已领" width="110">
          <template #default="{ row }">
            {{ row.issuedCount }} / {{ row.totalCount }}
          </template>
        </el-table-column>
        <el-table-column prop="perUserLimit" label="限领" width="70" />
        <el-table-column label="有效期" width="200">
          <template #default="{ row }">
            <span v-if="row.startTime">{{ formatDate(row.startTime) }} ~ {{ formatDate(row.endTime) }}</span>
            <span v-else>{{ formatDate(row.endTime) }} 截止</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openDialog('edit', row)">编辑</el-button>
            <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="deleteCoupon(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        :current-page="pagination.page"
        :page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </el-card>

    <!-- 新建/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="如：新人专享满减券" maxlength="50" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="简要描述" maxlength="200" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type" @change="onTypeChange">
            <el-radio :value="1">满减券</el-radio>
            <el-radio :value="2">折扣券</el-radio>
            <el-radio :value="3">充值赠送</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 满减券 -->
        <template v-if="form.type === 1">
          <el-form-item label="消费门槛" prop="minAmount">
            <el-input-number v-model="form.minAmount" :min="0" :precision="2" /> 元
          </el-form-item>
          <el-form-item label="优惠金额" prop="discountValue">
            <el-input-number v-model="form.discountValue" :min="0.01" :precision="2" /> 元
          </el-form-item>
        </template>

        <!-- 折扣券 -->
        <template v-if="form.type === 2">
          <el-form-item label="消费门槛" prop="minAmount">
            <el-input-number v-model="form.minAmount" :min="0" :precision="2" /> 元
          </el-form-item>
          <el-form-item label="折扣率" prop="discountRate">
            <el-input-number v-model="form.discountRate" :min="0.01" :max="0.99" :precision="2" />
            <span class="form-tip">如 0.85 = 85折</span>
          </el-form-item>
        </template>

        <!-- 充值赠送 -->
        <template v-if="form.type === 3">
          <el-form-item label="充值金额" prop="rechargeAmount">
            <el-input-number v-model="form.rechargeAmount" :min="1" :precision="2" /> 元
          </el-form-item>
          <el-form-item label="赠送金额" prop="giftAmount">
            <el-input-number v-model="form.giftAmount" :min="0.01" :precision="2" /> 元
          </el-form-item>
        </template>

        <el-form-item label="发行总量" prop="totalCount">
          <el-input-number v-model="form.totalCount" :min="1" /> 张
        </el-form-item>
        <el-form-item label="限领数量" prop="perUserLimit">
          <el-input-number v-model="form.perUserLimit" :min="1" :max="100" /> 张/人
        </el-form-item>
        <el-form-item label="使用门槛" prop="useMinAmount">
          <el-input-number v-model="form.useMinAmount" :min="0" :precision="2" /> 元（0=无限制）
        </el-form-item>
        <el-form-item label="有效期" prop="endTime">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="适用场景" prop="scope">
          <el-radio-group v-model="form.scope">
            <el-radio :value="0">全场通用</el-radio>
            <el-radio :value="1">仅充值</el-radio>
            <el-radio :value="2">仅商品购买</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const coupons = ref([])
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const dateRange = ref([])

const filters = reactive({ name: '', status: null })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const form = reactive({
  id: null,
  name: '',
  description: '',
  type: 1,
  minAmount: 0,
  discountValue: 0,
  discountRate: 0.9,
  rechargeAmount: 0,
  giftAmount: 0,
  totalCount: 100,
  issuedCount: 0,
  perUserLimit: 1,
  useMinAmount: 0,
  startTime: null,
  endTime: null,
  scope: 0,
  status: 1
})

const typeMap = { 1: '满减', 2: '折扣', 3: '充值赠送' }
const typeTag = (t) => ({ 1: '', 2: 'success', 3: 'warning' }[t] || '')

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  totalCount: [{ required: true, message: '请输入总量', trigger: 'blur' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑优惠券' : '新建优惠券')

const formatDate = (d) => d ? d.replace('T', ' ').substring(0, 16) : '-'

const loadCoupons = async () => {
  loading.value = true
  try {
    const res = await request.get('/coupon/list', {
      params: { page: pagination.page, pageSize: pagination.pageSize, name: filters.name || null, status: filters.status }
    })
    if (res.code === 0) {
      coupons.value = res.data.list || []
      pagination.total = res.data.total || 0
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const resetFilters = () => { filters.name = ''; filters.status = null; loadCoupons() }

const handleSizeChange = (s) => { pagination.pageSize = s; pagination.page = 1; loadCoupons() }
const handlePageChange = (p) => { pagination.page = p; loadCoupons() }

const openDialog = (mode, row = null) => {
  isEdit.value = mode === 'edit'
  if (row) {
    Object.assign(form, {
      id: row.id, name: row.name, description: row.description || '', type: row.type,
      minAmount: row.minAmount || 0, discountValue: row.discountValue || 0,
      discountRate: row.discountRate || 0.9, rechargeAmount: row.rechargeAmount || 0,
      giftAmount: row.giftAmount || 0, totalCount: row.totalCount, issuedCount: row.issuedCount,
      perUserLimit: row.perUserLimit || 1, useMinAmount: row.useMinAmount || 0,
      startTime: row.startTime, endTime: row.endTime, scope: row.scope || 0, status: row.status
    })
    dateRange.value = row.startTime ? [row.startTime, row.endTime] : []
  } else {
    resetForm()
  }
  dialogVisible.value = true
}

const resetForm = () => {
  Object.assign(form, {
    id: null, name: '', description: '', type: 1, minAmount: 0, discountValue: 0,
    discountRate: 0.9, rechargeAmount: 0, giftAmount: 0, totalCount: 100, issuedCount: 0,
    perUserLimit: 1, useMinAmount: 0, startTime: null, endTime: null, scope: 0, status: 1
  })
  dateRange.value = []
  formRef.value?.resetFields()
}

const onTypeChange = () => {
  if (form.type === 1) { form.discountRate = 0.9; form.rechargeAmount = 0; form.giftAmount = 0 }
  else if (form.type === 2) { form.discountValue = 0; form.rechargeAmount = 0; form.giftAmount = 0 }
  else { form.minAmount = 0; form.discountValue = 0; form.discountRate = 0.9 }
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    if (dateRange.value && dateRange.value.length === 2) {
      form.startTime = dateRange.value[0]
      form.endTime = dateRange.value[1]
    }
    submitting.value = true
    try {
      const api = isEdit.value ? '/coupon' : '/coupon'
      const method = isEdit.value ? 'put' : 'post'
      await request({ url: api, method, data: form })
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadCoupons()
    } catch (e) {
      console.error(e)
    } finally {
      submitting.value = false
    }
  })
}

const toggleStatus = async (row) => {
  try {
    await ElMessageBox.confirm(`确认${row.status === 1 ? '禁用' : '启用'} "${row.name}"？`, '操作确认')
    await request.post(`/coupon/${row.id}/toggle`)
    ElMessage.success('操作成功')
    loadCoupons()
  } catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}

const deleteCoupon = async (row) => {
  try {
    await ElMessageBox.confirm(`删除 "${row.name}" 后不可恢复，确定删除？`, '危险操作', { type: 'warning' })
    await request.delete(`/coupon/${row.id}`)
    ElMessage.success('已删除')
    loadCoupons()
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

const handleDistributeAll = async () => {
  if (coupons.value.length === 0) { ElMessage.warning('暂无优惠券'); return }
  // Find first enabled coupon to distribute
  const target = coupons.value.find(c => c.status === 1)
  if (!target) { ElMessage.warning('没有启用的优惠券'); return }
  try {
    await ElMessageBox.confirm(
      `确定向所有用户发放优惠券「${target.name}」？`,
      '批量发放',
      { type: 'success', confirmButtonText: '确认发放' }
    )
    await request.post('/coupon/distribute-all', { couponId: target.id })
    ElMessage.success('发放成功')
  } catch (e) { if (e !== 'cancel') ElMessage.error('发放失败') }
}

loadCoupons()
</script>

<style scoped>
.coupon-manage { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; font-weight: 600; }
.filter-card { margin-bottom: 16px; }
.table-card { background: #fff; }
.coupon-name { font-weight: 600; color: #333; }
.coupon-desc { font-size: 12px; color: #999; margin-top: 2px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 200px; }
.pagination { margin-top: 16px; justify-content: flex-end; }
.form-tip { margin-left: 8px; color: #999; font-size: 12px; }
</style>
