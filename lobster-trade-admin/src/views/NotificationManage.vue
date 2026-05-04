<template>
  <div class="notification-manage">
    <el-card shadow="never">
      <template #header>
        <div class="card-header-flex">
          <span class="card-title">系统通知管理</span>
          <el-button type="primary" size="small" @click="openAdd">
            <el-icon><Plus /></el-icon>
            发送通知
          </el-button>
        </div>
      </template>

      <div class="filter-row">
        <el-input v-model="filters.keyword" placeholder="搜索标题/内容" style="width:200px" clearable @clear="loadData" @keyup.enter="loadData" />
        <el-select v-model="filters.type" placeholder="类型" style="width:140px" clearable @change="loadData">
          <el-option label="系统通知" :value="1" />
          <el-option label="订单提醒" :value="2" />
          <el-option label="账户变动" :value="3" />
          <el-option label="活动通知" :value="4" />
        </el-select>
        <el-select v-model="filters.status" placeholder="状态" style="width:120px" clearable @change="loadData">
          <el-option label="未读" :value="0" />
          <el-option label="已读" :value="1" />
        </el-select>
        <el-button type="primary" @click="loadData">搜索</el-button>
      </div>

      <el-table :data="list" v-loading="loading" stripe class="mt-16">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="userId" label="接收人" width="100">
          <template #default="{ row }">
            <span>{{ row.userId === 0 ? '全部用户' : row.userId }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="typeTag(row.type)">{{ typeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="level" label="级别" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.level === 1" type="danger" size="small">紧急</el-tag>
            <el-tag v-else-if="row.level === 2" type="warning" size="small">重要</el-tag>
            <span v-else class="text-muted">一般</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '已读' : '未读' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发送时间" width="160" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
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

    <!-- 发送通知弹窗 -->
    <el-dialog v-model="showDialog" title="发送通知" width="520px" destroy-on-close>
      <el-form :model="form" label-width="90px">
        <el-form-item label="接收用户">
          <el-input v-model.number="form.userId" placeholder="输入用户ID，0表示全部用户" type="number" />
          <div class="form-tip">输入用户ID或填0发送给全部用户</div>
        </el-form-item>
        <el-form-item label="通知标题">
          <el-input v-model="form.title" placeholder="通知标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="通知内容">
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="通知内容" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type" style="width:100%">
            <el-option label="系统通知" :value="1" />
            <el-option label="订单提醒" :value="2" />
            <el-option label="账户变动" :value="3" />
            <el-option label="活动通知" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="form.level">
            <el-radio :label="1">紧急</el-radio>
            <el-radio :label="2">重要</el-radio>
            <el-radio :label="3">一般</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="doCreate">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
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
const filters = reactive({ keyword: '', type: '', status: '' })
const form = reactive({ userId: 0, title: '', content: '', type: 1, level: 3 })

const typeText = (v) => { const m = { 1: '系统通知', 2: '订单提醒', 3: '账户变动', 4: '活动' }; return m[v] || '-' }
const typeTag = (v) => { const m = { 1: 'primary', 2: 'success', 3: 'warning', 4: 'info' }; return m[v] || 'info' }

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/notification/list', {
      params: { page: currentPage.value, size: pageSize.value, keyword: filters.keyword || undefined, type: filters.type || undefined, status: filters.status || undefined }
    })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) { /* handled */ }
  finally { loading.value = false }
}

const openAdd = () => {
  Object.assign(form, { userId: 0, title: '', content: '', type: 1, level: 3 })
  showDialog.value = true
}

const doCreate = async () => {
  if (!form.title?.trim()) { ElMessage.warning('请输入标题'); return }
  if (!form.content?.trim()) { ElMessage.warning('请输入内容'); return }
  submitting.value = true
  try {
    await request.post('/admin/notification/create', form)
    ElMessage.success('发送成功')
    showDialog.value = false
    loadData()
  } catch (e) { /* handled */ }
  finally { submitting.value = false }
}

const deleteRow = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除该通知？`, '提示', { type: 'warning' })
    await request.delete(`/admin/notification/${row.id}`)
    ElMessage.success('已删除')
    loadData()
  } catch (e) { /* handled */ }
}

onMounted(() => { loadData() })
</script>

<style scoped>
.notification-manage { padding: 0; }
.card-header-flex { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-size: 15px; font-weight: 600; }
.filter-row { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
.mt-16 { margin-top: 16px; }
.text-muted { color: #999; font-size: 13px; }
.form-tip { font-size: 12px; color: #999; margin-top: 4px; }
</style>