<template>
  <div class="cert-manage">
    <div class="page-header">
      <h2>服务商认证管理</h2>
      <el-tag type="warning">{{ pendingCount }} 个待审核申请</el-tag>
    </div>

    <el-card shadow="never">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="待审核" name="pending">
          <el-table :data="pendingList" v-loading="loading" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="userId" label="用户ID" width="100" />
            <el-table-column prop="certificationType" label="认证类型" width="120">
              <template #default="{ row }">
                <el-tag :type="row.certificationType === 'boost' ? 'success' : row.certificationType === 'accompany' ? 'warning' : 'primary'">
                  {{ row.certificationType === 'boost' ? '代练' : row.certificationType === 'accompany' ? '陪玩' : '工作室' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="hourlyRate" label="服务内容" width="150" show-overflow-tooltip />
            <el-table-column prop="serviceRegions" label="服务区服" width="150" show-overflow-tooltip />
            <el-table-column prop="serviceDescription" label="服务描述" min-width="200" show-overflow-tooltip />
            <el-table-column label="申请等级" width="100">
              <template #default="{ row }">
                <el-tag :type="levelTagType(row.providerLevel)" size="small">{{ levelName(row.providerLevel) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="submitTime" label="申请时间" width="170">
              <template #default="{ row }">{{ formatTime(row.submitTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button type="success" size="small" @click="openApproveDialog(row)">通过</el-button>
                <el-button type="danger" size="small" @click="handleReject(row)">拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="已通过" name="approved">
          <el-table :data="approvedList" v-loading="loading" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="userId" label="用户ID" width="100" />
            <el-table-column prop="certificationType" label="认证类型" width="120">
              <template #default="{ row }">
                <el-tag :type="row.certificationType === 'boost' ? 'success' : row.certificationType === 'accompany' ? 'warning' : 'primary'">
                  {{ row.certificationType === 'boost' ? '代练' : row.certificationType === 'accompany' ? '陪玩' : '工作室' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="服务商等级" width="120">
              <template #default="{ row }">
                <el-tag :type="levelTagType(row.providerLevel)" size="small">{{ levelName(row.providerLevel) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="serviceDescription" label="服务描述" min-width="200" show-overflow-tooltip />
            <el-table-column prop="expireTime" label="有效期至" width="170">
              <template #default="{ row }">{{ formatTime(row.expireTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button type="danger" size="small" @click="handleRevoke(row)">撤销认证</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 拒绝原因对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝认证申请" width="400px">
      <el-form>
        <el-form-item label="拒绝原因" required>
          <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入拒绝原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>

    <!-- 通过认证对话框（设置服务商等级） -->
    <el-dialog v-model="approveDialogVisible" title="通过认证申请" width="450px">
      <el-form label-width="100px">
        <el-form-item label="服务商等级">
          <el-select v-model="approveLevel" style="width: 200px;">
            <el-option :value="1" label="普通服务商" />
            <el-option :value="2" label="铜牌服务商" />
            <el-option :value="3" label="银牌服务商" />
            <el-option :value="4" label="金牌服务商" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户备注">
          <el-input v-model="adminRemark" type="textarea" :rows="2" placeholder="可选备注（内部使用）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="success" @click="confirmApprove">确认通过</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
document.title = '认证管理 - 龙虾道具交易平台'
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const activeTab = ref('pending')
const pendingList = ref([])
const approvedList = ref([])
const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const currentRejectRow = ref(null)
const approveDialogVisible = ref(false)
const approveLevel = ref(1)
const approveRow = ref(null)
const adminRemark = ref('')

const pendingCount = computed(() => pendingList.value.length)

const levelName = (level) => {
    const map = { 1: '普通', 2: '铜牌', 3: '银牌', 4: '金牌' }
    return map[level] || '普通'
}

const levelTagType = (level) => {
    const map = { 1: 'info', 2: 'warning', 3: 'success', 4: 'danger' }
    return map[level] || 'info'
}

const formatTime = (time) => {
    if (!time) return '-'
    return new Date(time).toLocaleString('zh-CN')
}

const loadPending = async () => {
    loading.value = true
    try {
        const resp = await request.get('/admin/certification/pending')
        pendingList.value = resp.data || []
    } catch (e) {
        ElMessage.error('加载失败')
    } finally {
        loading.value = false
    }
}

const loadApproved = async () => {
    loading.value = true
    try {
        // loadApproved 已有自己的 filter，status=1 表示已通过
        const resp = await request.get('/certification/list')
        approvedList.value = (resp.data || []).filter(c => c.status === 1)
    } catch (e) {
        ElMessage.error('加载失败')
    } finally {
        loading.value = false
    }
}

const openApproveDialog = (row) => {
    approveRow.value = row
    approveLevel.value = row.providerLevel || 1
    adminRemark.value = ''
    approveDialogVisible.value = true
}

const confirmApprove = async () => {
    try {
        await request.post('/admin/certification/review', {
            certId: approveRow.value.id,
            status: 1,
            rejectReason: null,
            providerLevel: approveLevel.value
        })
        ElMessage.success('审核完成，等级已设置')
        approveDialogVisible.value = false
        loadPending()
        loadApproved()
    } catch (e) {
        ElMessage.error(e.message || '操作失败')
    }
}

const handleReview = async (row, status) => {
    try {
        await ElMessageBox.confirm(
            status === 1 ? `确认通过用户 ${row.userId} 的认证申请？` : `确认拒绝该申请？`,
            '审核确认'
        )
        await request.post('/admin/certification/review', {
            certId: row.id,
            status,
            rejectReason: status === 2 ? rejectReason.value : null
        })
        ElMessage.success('审核完成')
        loadPending()
        loadApproved()
    } catch (e) {
        if (e !== 'cancel') ElMessage.error(e.message || '操作失败')
    }
}

const handleReject = (row) => {
    currentRejectRow.value = row
    rejectReason.value = ''
    rejectDialogVisible.value = true
}

const confirmReject = async () => {
    if (!rejectReason.value.trim()) {
        ElMessage.warning('请填写拒绝原因')
        return
    }
    rejectDialogVisible.value = false
    await handleReview(currentRejectRow.value, 2)
}

const handleRevoke = async (row) => {
    try {
        await ElMessageBox.confirm(`确认撤销用户 ${row.userId} 的认证资格？`, '撤销确认')
        await request.post('/admin/certification/review', {
            certId: row.id,
            status: 3, // 冻结
            rejectReason: '管理员撤销认证'
        })
        ElMessage.success('已撤销认证')
        loadApproved()
    } catch (e) {
        if (e !== 'cancel') ElMessage.error(e.message || '操作失败')
    }
}

onMounted(() => {
    loadPending()
    loadApproved()
})
</script>

<style scoped>
.cert-manage { padding: 20px; }
.page-header { display: flex; align-items: center; gap: 12px; margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 18px; }
</style>
