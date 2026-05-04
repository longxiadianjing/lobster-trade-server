<template>
  <div class="game-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>游戏列表</span>
          <el-button type="primary" size="small" @click="showAddDialog">添加游戏</el-button>
        </div>
      </template>
      <el-table :data="games" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="gameName" label="游戏名称" />
        <el-table-column prop="gameCode" label="游戏代码" />
        <el-table-column prop="gameType" label="类型" width="100" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '上线' : '下线' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="success" v-if="row.status !== 1" @click="setStatus(row.id, 1)">上线</el-button>
            <el-button size="small" type="warning" v-else @click="setStatus(row.id, 0)">下线</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-dialog v-model="addDialogVisible" title="添加游戏" width="450px">
      <el-form :model="gameForm" label-width="100px">
        <el-form-item label="游戏名称"><el-input v-model="gameForm.gameName" /></el-form-item>
        <el-form-item label="游戏代码"><el-input v-model="gameForm.gameCode" /></el-form-item>
        <el-form-item label="游戏类型">
          <el-select v-model="gameForm.gameType">
            <el-option label="FPS" value="fps" />
            <el-option label="MOBA" value="moba" />
            <el-option label="RPG" value="rpg" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="图标URL"><el-input v-model="gameForm.icon" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="gameForm.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminGames, createGame, setGameStatus } from '@/api/admin'

const loading = ref(false)
const games = ref([])
const addDialogVisible = ref(false)
const gameForm = ref({ gameName: '', gameCode: '', gameType: 'fps', icon: '', sortOrder: 0 })

const loadGames = async () => {
  loading.value = true
  try {
    const res = await getAdminGames()
    games.value = res.data || []
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

const setStatus = async (id, status) => {
  try {
    await setGameStatus(id, status)
    ElMessage.success('已更新')
    loadGames()
  } catch (e) { ElMessage.error('操作失败') }
}

const showAddDialog = () => { addDialogVisible.value = true }

const handleCreate = async () => {
  try {
    await createGame(gameForm.value)
    ElMessage.success('添加成功')
    addDialogVisible.value = false
    loadGames()
  } catch (e) { ElMessage.error('添加失败') }
}

onMounted(() => {
  loadGames()
  document.title = '游戏管理 - 龙虾道具交易平台'
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>