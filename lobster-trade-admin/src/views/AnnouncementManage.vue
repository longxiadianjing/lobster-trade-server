<template>
  <div class="announcement-manage">
    <el-card shadow="never">
      <template #header>
        <div class="card-header-flex">
          <span class="card-title">公告管理</span>
          <el-button type="primary" size="small" @click="openAdd">
            <el-icon><Plus /></el-icon>
            发布公告
          </el-button>
        </div>
      </template>

      <!-- 筛选 -->
      <div class="filter-row">
        <el-input v-model="filters.keyword" placeholder="搜索标题/内容" style="width:200px" clearable @clear="loadData" @keyup.enter="loadData" />
        <el-select v-model="filters.type" placeholder="类型" style="width:140px" clearable @change="loadData">
          <el-option label="系统公告" :value="1" />
          <el-option label="活动通知" :value="2" />
          <el-option label="维护通知" :value="3" />
          <el-option label="版本更新" :value="4" />
        </el-select>
        <el-select v-model="filters.status" placeholder="状态" style="width:120px" clearable @change="loadData">
          <el-option label="草稿" :value="0" />
          <el-option label="已发布" :value="1" />
        </el-select>
        <el-button type="primary" @click="loadData">搜索</el-button>
      </div>

      <el-table :data="list" v-loading="loading" stripe class="mt-16">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="typeTag(row.type)">{{ typeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.priority === 1" type="danger" size="small">紧急</el-tag>
            <el-tag v-else-if="row.priority === 2" type="warning" size="small">重要</el-tag>
            <span v-else class="text-muted">一般</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '已发布' : '草稿' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="阅读量" width="80" />
        <el-table-column prop="publishTime" label="发布时间" width="160" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row)">详情</el-button>
            <el-button size="small" type="primary" plain @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 0" size="small" type="success" @click="publish(row)">发布</el-button>
            <el-button v-else size="small" type="warning" @click="unpublish(row)">撤回</el-button>
            <el-button size="small" type="danger" @click="deleteRow(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="loadData"
        class="mt-16"
      />
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="公告详情" width="600px">
      <div v-if="current" class="ann-detail">
        <div class="ann-title">{{ current.title }}</div>
        <div class="ann-meta">
          <el-tag :type="typeTag(current.type)" size="small">{{ typeText(current.type) }}</el-tag>
          <el-tag v-if="current.priority === 1" type="danger" size="small">紧急</el-tag>
          <span class="text-muted">阅读：{{ current.viewCount }}</span>
          <span class="text-muted">发布时间：{{ current.publishTime || '-' }}</span>
        </div>
        <div class="ann-content">{{ current.content }}</div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="openEdit(current); detailVisible = false">编辑</el-button>
      </template>
    </el-dialog>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="showDialog" :title="isEdit ? '编辑公告' : '发布公告'" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="公告标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" style="width:100%">
            <el-option label="系统公告" :value="1" />
            <el-option label="活动通知" :value="2" />
            <el-option label="维护通知" :value="3" />
            <el-option label="版本更新" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-radio-group v-model="form.priority">
            <el-radio :label="1">紧急</el-radio>
            <el-radio :label="2">重要</el-radio>
            <el-radio :label="3">一般</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="公告内容，支持富文本" maxlength="2000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button v-if="!isEdit" type="primary" :loading="submitting" @click="doCreate">创建并发布</el-button>
        <el-button v-else type="primary" :loading="submitting" @click="doUpdate">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
document.title = '公告管理 - 龙虾道具交易平台'
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const showDialog = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const current = ref(null)
const formRef = ref(null)
const filters = reactive({ keyword: '', type: '', status: '' })
const form = reactive({ title: '', content: '', type: 1, priority: 3 })

const typeText = (v) => { const m = { 1: '系统公告', 2: '活动通知', 3: '维护通知', 4: '版本更新' }; return m[v] || '-' }
const typeTag = (v) => { const m = { 1: 'primary', 2: 'success', 3: 'warning', 4: 'info' }; return m[v] || 'info' }

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/announcement/list', {
      params: { page: currentPage.value, size: pageSize.value, keyword: filters.keyword || undefined, type: filters.type || undefined, status: filters.status || undefined }
    })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) { /* handled */ }
  finally { loading.value = false }
}

const openAdd = () => {
  isEdit.value = false
  Object.assign(form, { title: '', content: '', type: 1, priority: 3 })
  showDialog.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  Object.assign(form, { title: row.title, content: row.content, type: row.type, priority: row.priority })
  form._id = row.id
  showDialog.value = true
}

const openDetail = (row) => {
  current.value = row
  detailVisible.value = true
}

const doCreate = async () => {
  if (!form.title?.trim()) { ElMessage.warning('请输入标题'); return }
  submitting.value = true
  try {
    await request.post('/admin/announcement/create', { ...form, status: 1 })
    ElMessage.success('发布成功')
    showDialog.value = false
    loadData()
  } catch (e) { /* handled */ }
  finally { submitting.value = false }
}

const doUpdate = async () => {
  if (!form.title?.trim()) { ElMessage.warning('请输入标题'); return }
  submitting.value = true
  try {
    await request.put(`/admin/announcement/${form._id}`, form)
    ElMessage.success('保存成功')
    showDialog.value = false
    loadData()
  } catch (e) { /* handled */ }
  finally { submitting.value = false }
}

const publish = async (row) => {
  try {
    await request.put(`/admin/announcement/${row.id}/publish`)
    ElMessage.success('已发布')
    loadData()
  } catch (e) { /* handled */ }
}

const unpublish = async (row) => {
  try {
    await request.put(`/admin/announcement/${row.id}/unpublish`)
    ElMessage.success('已撤回')
    loadData()
  } catch (e) { /* handled */ }
}

const deleteRow = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除公告「${row.title}」？`, '提示', { type: 'warning' })
    await request.delete(`/admin/announcement/${row.id}`)
    ElMessage.success('已删除')
    loadData()
  } catch (e) { /* handled */ }
}

onMounted(() => { loadData() })
</script>

<style scoped>
.announcement-manage { padding: 0; }
.card-header-flex { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-size: 15px; font-weight: 600; }
.filter-row { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
.mt-16 { margin-top: 16px; }
.text-muted { color: #999; font-size: 13px; }

.ann-detail { padding: 4px 0; }
.ann-title { font-size: 18px; font-weight: 700; margin-bottom: 12px; color: #333; }
.ann-meta { display: flex; gap: 10px; align-items: center; margin-bottom: 16px; flex-wrap: wrap; }
.ann-content { font-size: 14px; line-height: 1.8; color: #555; white-space: pre-wrap; background: #f9fafb; padding: 16px; border-radius: 8px; }
</style>