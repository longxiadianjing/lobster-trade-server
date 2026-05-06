<template>
  <div class="slot-manage">
    <div class="page-header">
      <h2>商品推荐位管理</h2>
    </div>

    <!-- 搜索栏 -->
    <div class="filter-bar">
      <el-select v-model="querySlotKey" placeholder="选择推荐位" clearable style="width: 240px">
        <el-option label="首页精选" value="home_featured" />
        <el-option label="首页横幅" value="home_banner" />
        <el-option label="商品详情侧边" value="product_detail_side" />
      </el-select>
      <el-button @click="loadSlots">查询</el-button>
      <el-button type="primary" @click="showDialog('create')">新增推荐位</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="slots" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="slotKey" label="推荐位" width="160">
        <template #default="{ row }">
          <el-tag>{{ slotKeyMap[row.slotKey] || row.slotKey }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="productId" label="商品ID" width="80" />
      <el-table-column label="商品信息" min-width="200">
        <template #default="{ row }">
          <span>{{ row.remark || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="70" />
      <el-table-column prop="startTime" label="开始时间" width="160">
        <template #default="{ row }">
          {{ row.startTime ? formatTime(row.startTime) : '不限' }}
        </template>
      </el-table-column>
      <el-table-column prop="endTime" label="结束时间" width="160">
        <template #default="{ row }">
          {{ row.endTime ? formatTime(row.endTime) : '不限' }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="showDialog('edit', row)">编辑</el-button>
          <el-button link type="warning" size="small" @click="toggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="page"
      :page-size="20"
      layout="prev, pager, next"
      :total="total"
      @current-change="loadSlots"
    />

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="推荐位" required>
          <el-select v-model="form.slotKey" placeholder="请选择推荐位" :disabled="dialogMode === 'edit'">
            <el-option label="首页精选" value="home_featured" />
            <el-option label="首页横幅" value="home_banner" />
            <el-option label="商品详情侧边" value="product_detail_side" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品ID" required>
          <el-input-number v-model="form.productId" :min="1" />
          <span style="margin-left: 12px; color: #999; font-size: 12px">
            <el-link :href="`#/product/list`" type="primary" target="_blank">查看商品ID</el-link>
          </span>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
          <span style="margin-left: 8px; color: #999; font-size: 12px">越小越靠前</span>
        </el-form-item>
        <el-form-item label="展示时间">
          <el-date-picker v-model="timeRange" type="datetimerange" range-separator="至"
            start-placeholder="开始时间" end-placeholder="结束时间" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
          <span style="color: #999; font-size: 12px; margin-top: 4px; display: block">留空表示永久有效</span>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" placeholder="内部备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
document.title = '推荐位管理 - 龙虾道具交易平台'
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const slots = ref([])
const total = ref(0)
const page = ref(1)
const querySlotKey = ref('')
const dialogVisible = ref(false)
const dialogMode = ref('create')
const dialogTitle = ref('新增推荐位')
const submitting = ref(false)
const timeRange = ref([])

const form = reactive({
  id: null,
  slotKey: 'home_featured',
  productId: null,
  sortOrder: 0,
  startTime: null,
  endTime: null,
  status: 1,
  remark: ''
})

const slotKeyMap = {
  home_featured: '首页精选',
  home_banner: '首页横幅',
  product_detail_side: '商品详情侧边'
}

const formatTime = (t) => {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 19)
}

const loadSlots = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/recommend-slot/list', {
      params: { slotKey: querySlotKey.value || undefined, page: page.value, size: 20 }
    })
    if (res.code === 0) {
      slots.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } finally {
    loading.value = false
  }
}

const showDialog = (mode, row = null) => {
  dialogMode.value = mode
  dialogTitle.value = mode === 'create' ? '新增推荐位' : '编辑推荐位'
  if (mode === 'edit' && row) {
    Object.assign(form, {
      id: row.id,
      slotKey: row.slotKey,
      productId: row.productId,
      sortOrder: row.sortOrder,
      startTime: row.startTime,
      endTime: row.endTime,
      status: row.status,
      remark: row.remark
    })
    timeRange.value = [row.startTime, row.endTime].filter(Boolean)
  } else {
    Object.assign(form, { id: null, slotKey: 'home_featured', productId: null, sortOrder: 0, startTime: null, endTime: null, status: 1, remark: '' })
    timeRange.value = []
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.slotKey || !form.productId) {
    ElMessage.warning('请填写推荐位和商品ID')
    return
  }
  submitting.value = true
  try {
    form.startTime = timeRange.value?.[0] || null
    form.endTime = timeRange.value?.[1] || null
    const url = dialogMode.value === 'create'
      ? '/admin/recommend-slot/create'
      : `/admin/recommend-slot/${form.id}`
    const method = dialogMode.value === 'create' ? 'post' : 'put'
    const res = await request({ url, method, data: form })
    if (res.code === 0) {
      ElMessage.success(dialogMode.value === 'create' ? '创建成功' : '更新成功')
      dialogVisible.value = false
      loadSlots()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } finally {
    submitting.value = false
  }
}

const toggleStatus = async (row) => {
  const action = row.status === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确认${action}该推荐位？`, '提示')
    const res = await request.put(`/admin/recommend-slot/${row.id}/toggle`)
    if (res.code === 0) {
      ElMessage.success(`${action}成功`)
      loadSlots()
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(`${action}失败`)
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该推荐位？', '警告', { type: 'warning' })
    const res = await request.delete(`/admin/recommend-slot/${row.id}`)
    if (res.code === 0) {
      ElMessage.success('删除成功')
      loadSlots()
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(() => loadSlots())
</script>

<style scoped>
.slot-manage { padding: 20px; }
.page-header { margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 18px; font-weight: 600; }
.filter-bar { display: flex; gap: 12px; margin-bottom: 16px; align-items: center; }
</style>
