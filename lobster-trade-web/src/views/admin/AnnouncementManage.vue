<template>
  <div class="announcement-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>公告列表</span>
          <el-button type="primary" size="small" @click="showAddDialog">发布公告</el-button>
        </div>
      </template>
      <el-table :data="announcements" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">{{ typeMap[row.type] || row.type }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '显示' : '隐藏' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="160" />
      </el-table>
    </el-card>
    <el-dialog v-model="addDialogVisible" title="发布公告" width="500px">
      <el-form :model="annForm" label-width="80px">
        <el-form-item label="标题"><el-input v-model="annForm.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="annForm.content" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="annForm.type">
            <el-option label="系统公告" :value="1" />
            <el-option label="活动公告" :value="2" />
            <el-option label="维护公告" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminAnnouncements, createAnnouncement } from '@/api/admin'

const loading = ref(false)
const announcements = ref([])
const addDialogVisible = ref(false)
const typeMap = { 1:'系统公告', 2:'活动公告', 3:'维护公告' }
const annForm = ref({ title: '', content: '', type: 1 })

const loadAnnouncements = async () => {
  loading.value = true
  try {
    const res = await getAdminAnnouncements({ page: 1, size: 50 })
    announcements.value = res.data?.records || []
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

const showAddDialog = () => { addDialogVisible.value = true }

const handleCreate = async () => {
  if (!annForm.value.title || !annForm.value.content) { ElMessage.warning('请填写完整'); return }
  try {
    await createAnnouncement(annForm.value)
    ElMessage.success('发布成功')
    addDialogVisible.value = false
    loadAnnouncements()
  } catch (e) { ElMessage.error('发布失败') }
}

onMounted(() => {
  loadAnnouncements()
  document.title = '公告管理 - 龙虾道具交易平台'
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>