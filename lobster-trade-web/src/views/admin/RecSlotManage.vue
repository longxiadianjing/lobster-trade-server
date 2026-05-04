<template>
  <div class="rec-slot-manage">
    <el-card>
      <template #header>
        <div class="flex-between">
          <span>🏆 推荐位管理</span>
          <el-button type="primary" size="small" @click="openCreate">+ 添加推荐位</el-button>
        </div>
      </template>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="slotKey" label="位置标识" width="160">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.slotKey }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="商品信息" min-width="200">
          <template #default="{ row }">
            <div class="product-info">
              <span class="product-id">商品#{{ row.productId }}</span>
              <span class="product-remark">{{ row.remark || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="productId" label="商品ID" width="100" />
        <el-table-column prop="sortOrder" label="排序" width="80">
          <template #default="{ row }">{{ row.sortOrder ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="有效期" width="220">
          <template #default="{ row }">
            <span class="time-range">{{ row.startTime ? row.startTime.slice(0,16) : '-' }} ~ {{ row.endTime ? row.endTime.slice(0,16) : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleSlot(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" link type="danger" @click="deleteSlot(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑推荐位' : '添加推荐位'" width="480px">
      <el-form :model="form" label-width="100px" ref="formRef">
        <el-form-item label="位置标识" required>
          <el-select v-model="form.slotKey" placeholder="选择位置" style="width:100%">
            <el-option label="首页精选推荐" value="home_featured" />
            <el-option label="首页热门商品" value="home_hot" />
            <el-option label="游戏精选推荐" value="game_featured" />
            <el-option label="新品推荐" value="new_arrival" />
            <el-option label="今日特价" value="today_deal" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品ID" required>
          <el-input-number v-model="form.productId" :min="1" style="width:100%" placeholder="输入商品ID" />
        </el-form-item>
        <el-form-item label="商品备注">
          <el-input v-model="form.remark" placeholder="如：三角洲行动 哈夫币 100万" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="有效期">
          <el-date-picker v-model="dateRange" type="datetimerange" range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const list = ref([])
const dateRange = ref([])
const formRef = ref(null)
const form = reactive({ id: null, slotKey: 'home_featured', productId: null, remark: '', sortOrder: 0, startTime: null, endTime: null })
const auth = { headers: { Authorization: `Bearer ${localStorage.getItem('adminToken')}` } }

onMounted(() => {
  load()
  document.title = '推荐位管理 - 龙虾道具交易平台'
})
async function load() {
  loading.value = true
  try {
    const r = await fetch('/api/admin/recommend-slot/list?page=1&size=50', auth).then(r => r.json())
    list.value = r.data?.records || []
  } catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function openCreate() {
  isEdit.value = false
  Object.assign(form, { id: null, slotKey: 'home_featured', productId: null, remark: '', sortOrder: 0, startTime: null, endTime: null })
  dateRange.value = []
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    slotKey: row.slotKey,
    productId: row.productId,
    remark: row.remark || '',
    sortOrder: row.sortOrder || 0,
    startTime: row.startTime,
    endTime: row.endTime,
  })
  dateRange.value = row.startTime && row.endTime ? [row.startTime.slice(0, 19), row.endTime.slice(0, 19)] : []
  dialogVisible.value = true
}

async function save() {
  if (!form.productId) { ElMessage.warning('请填写商品ID'); return }
  if (!form.slotKey) { ElMessage.warning('请选择位置标识'); return }
  submitting.value = true
  try {
    const [startTime, endTime] = dateRange.value || []
    const body = { ...form, startTime: startTime || null, endTime: endTime || null }
    const url = isEdit.value ? `/api/admin/recommend-slot/${form.id}` : '/api/admin/recommend-slot/create'
    const method = isEdit.value ? 'PUT' : 'POST'
    const r = await fetch(url, { method, headers: { ...auth.headers, 'Content-Type': 'application/json' }, body: JSON.stringify(body) }).then(r => r.json())
    if (r.code === 0 || r.code === 200) {
      ElMessage.success('保存成功')
      dialogVisible.value = false
      load()
    } else {
      ElMessage.error(r.message || '保存失败')
    }
  } catch { ElMessage.error('保存失败') }
  finally { submitting.value = false }
}

async function toggleSlot(row) {
  try {
    await ElMessageBox.confirm(`确定要${row.status === 1 ? '禁用' : '启用'}该推荐位吗？`, '提示')
    const r = await fetch(`/api/admin/recommend-slot/${row.id}/toggle`, { method: 'PUT', headers: auth.headers }).then(r => r.json())
    if (r.code === 0 || r.code === 200) {
      ElMessage.success('更新成功')
      load()
    } else {
      ElMessage.error(r.message || '操作失败')
    }
  } catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}

async function deleteSlot(row) {
  try {
    await ElMessageBox.confirm(`确定删除该推荐位吗？`, '危险操作', { type: 'warning' })
    const r = await fetch(`/api/admin/recommend-slot/${row.id}`, { method: 'DELETE', headers: auth.headers }).then(r => r.json())
    if (r.code === 0 || r.code === 200) {
      ElMessage.success('删除成功')
      load()
    } else {
      ElMessage.error(r.message || '删除失败')
    }
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}
</script>

<style scoped>
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.product-info { display: flex; flex-direction: column; gap: 2px; }
.product-id { font-size: 12px; color: #999; }
.product-remark { font-size: 13px; color: #333; }
.time-range { font-size: 12px; color: #666; }
</style>
