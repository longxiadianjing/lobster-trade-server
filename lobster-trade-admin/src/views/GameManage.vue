<template>
  <div class="game-manage">
    <el-card shadow="never">
      <template #header>
        <div class="card-header-flex">
          <span class="card-title">游戏管理</span>
          <el-button type="primary" size="small" @click="showAddDialog">
            <el-icon><Plus /></el-icon>
            添加游戏
          </el-button>
        </div>
      </template>

      <el-table :data="games" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="gameName" label="游戏名称" min-width="180" />
        <el-table-column prop="gameCode" label="编码" width="140" />
        <el-table-column prop="gameType" label="类型" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.gameType === 'pc端游' ? 'PC端游' : row.gameType === '手游' ? '手游' : '其他' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 1" size="small" type="danger" @click="handleToggle(row, 0)">禁用</el-button>
            <el-button v-else size="small" type="success" @click="handleToggle(row, 1)">启用</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑弹窗 -->
    <el-dialog v-model="showDialog" :title="isEdit ? '编辑游戏' : '添加游戏'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="游戏名称" prop="gameName">
          <el-input v-model="form.gameName" placeholder="如：三角洲行动" />
        </el-form-item>
        <el-form-item label="游戏编码" prop="gameCode">
          <el-input v-model="form.gameCode" placeholder="如：delta_force" />
        </el-form-item>
        <el-form-item label="类型" prop="gameType">
          <el-select v-model="form.gameType" style="width: 100%;">
            <el-option label="PC端游" value="pc端游" />
            <el-option label="手游" value="手游" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="游戏简介" />
        </el-form-item>
        <el-form-item label="排序值">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { Plus } from '@element-plus/icons-vue'

const loading = ref(false)
const games = ref([])
const showDialog = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const currentId = ref(null)

const form = reactive({
  gameName: '', gameCode: '', gameType: 'pc端游', description: '', sortOrder: 100, status: 1
})

const rules = {
  gameName: [{ required: true, message: '请输入游戏名称', trigger: 'blur' }],
  gameCode: [{ required: true, message: '请输入游戏编码', trigger: 'blur' }],
  gameType: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

const loadGames = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/games')
    if (res.data) games.value = res.data
  } catch (e) {
    games.value = [
      { id: 1, gameName: '三角洲行动', gameCode: 'delta_force', gameType: 'pc端游', description: '腾讯旗舰级FPS游戏', sortOrder: 100, status: 1 },
      { id: 2, gameName: '王者荣耀', gameCode: 'lol', gameType: '手游', description: '5v5 MOBA竞技手游', sortOrder: 90, status: 1 },
      { id: 3, gameName: '暗区突围', gameCode: 'arena_breakout', gameType: 'pc端游', description: '摸金撤离FPS游戏', sortOrder: 88, status: 1 }
    ]
  } finally {
    loading.value = false
  }
}

const showAddDialog = () => {
  isEdit.value = false
  currentId.value = null
  Object.assign(form, { gameName: '', gameCode: '', gameType: 'pc端游', description: '', sortOrder: 100, status: 1 })
  showDialog.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  currentId.value = row.id
  Object.assign(form, { gameName: row.gameName, gameCode: row.gameCode, gameType: row.gameType, description: row.description || '', sortOrder: row.sortOrder || 100, status: row.status })
  showDialog.value = true
}

const handleSave = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (isEdit.value) {
        await request.put(`/admin/game/${currentId.value}`, form)
        ElMessage.success('更新成功')
      } else {
        await request.post('/admin/game', form)
        ElMessage.success('添加成功')
      }
      showDialog.value = false
      loadGames()
    } catch (e) {
      ElMessage.error('操作失败')
    } finally {
      submitting.value = false
    }
  })
}

const handleToggle = async (row, status) => {
  try {
    await request.post(`/admin/game/${row.id}/status`, { status })
    ElMessage.success(status === 1 ? '已启用' : '已禁用')
    loadGames()
  } catch (e) { ElMessage.error('操作失败') }
}

onMounted(() => { loadGames() })
</script>

<style scoped>
.card-header-flex { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-size: 15px; font-weight: 600; color: #333; }
</style>