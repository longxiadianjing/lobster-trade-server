<template>
  <div class="hot-search-manage">
    <el-card shadow="never">
      <template #header>
        <div class="card-header-flex">
          <span class="card-title">热搜词管理</span>
          <el-button type="primary" size="small" @click="openAdd">
            <el-icon><Plus /></el-icon>
            添加热搜词
          </el-button>
        </div>
      </template>

      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索热搜词"
          style="width: 240px"
          clearable
          @clear="loadData"
          @keyup.enter="loadData"
        >
          <template #prefix><span>🔍</span></template>
        </el-input>
        <el-button type="primary" @click="loadData">搜索</el-button>
      </div>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="word" label="热搜词" min-width="200" />
        <el-table-column prop="searchCount" label="搜索次数" width="100" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 1" size="small" type="danger" @click="toggleStatus(row, 0)">禁用</el-button>
            <el-button v-else size="small" type="success" @click="toggleStatus(row, 1)">启用</el-button>
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

    <!-- 添加/编辑弹窗 -->
    <el-dialog v-model="showDialog" :title="isEdit ? '编辑热搜词' : '添加热搜词'" width="480px">
      <el-form ref="formRef" :model="form" label-width="80px">
        <el-form-item label="热搜词" prop="word">
          <el-input v-model="form.word" placeholder="如：三角洲行动" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="排序值">
          <el-input-number v-model="form.sortOrder" :min="1" :max="999" />
          <span class="form-tip">越小越靠前</span>
        </el-form-item>
        <el-form-item label="状态" v-if="isEdit">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const keyword = ref('')
const showDialog = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const form = ref({ word: '', sortOrder: 100, status: 1 })

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/hot-search/list', {
      params: { page: currentPage.value, size: pageSize.value, keyword: keyword.value }
    })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

const openAdd = () => {
  isEdit.value = false
  form.value = { word: '', sortOrder: 100, status: 1 }
  showDialog.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  form.value = { word: row.word, sortOrder: row.sortOrder, status: row.status }
  form.value._id = row.id
  showDialog.value = true
}

const submitForm = async () => {
  if (!form.value.word || !form.value.word.trim()) {
    ElMessage.warning('请输入热搜词')
    return
  }
  submitting.value = true
  try {
    if (isEdit.value) {
      await request.put(`/admin/hot-search/${form.value._id}`, form.value)
      ElMessage.success('更新成功')
    } else {
      await request.post('/admin/hot-search/create', form.value)
      ElMessage.success('添加成功')
    }
    showDialog.value = false
    loadData()
  } catch (e) {
    // handled
  } finally {
    submitting.value = false
  }
}

const toggleStatus = async (row, status) => {
  try {
    await request.put(`/admin/hot-search/${row.id}/toggle`)
    ElMessage.success(status === 1 ? '已启用' : '已禁用')
    loadData()
  } catch (e) {
    // handled
  }
}

onMounted(() => { loadData() })
</script>

<style scoped>
.hot-search-manage { padding: 0; }
.card-header-flex { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-size: 15px; font-weight: 600; }
.mt-16 { margin-top: 16px; justify-content: center; }
.form-tip { margin-left: 8px; font-size: 12px; color: #999; }
.search-bar { display: flex; align-items: center; gap: 10px; margin-bottom: 16px; }
</style>