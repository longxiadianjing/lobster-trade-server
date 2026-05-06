<template>
  <div class="real-name-manage">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>实名认证管理</span>
          <el-radio-group v-model="filterStatus" size="small" @change="loadData">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="0">审核中</el-radio-button>
            <el-radio-button label="1">已通过</el-radio-button>
            <el-radio-button label="2">未通过</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column label="用户信息" min-width="120">
          <template #default="{ row }">
            <div>{{ row.nickname || '-' }}</div>
            <div style="font-size:12px;color:#999">{{ row.phone || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="姓名" min-width="100">
          <template #default="{ row }">
            <span v-if="row.realName">{{ maskName(row.realName) }}</span>
            <span v-else style="color:#999">-</span>
          </template>
        </el-table-column>
        <el-table-column label="身份证" min-width="150">
          <template #default="{ row }">
            <span v-if="row.idCard">{{ maskIdCard(row.idCard) }}</span>
            <span v-else style="color:#999">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="warning" size="small">审核中</el-tag>
            <el-tag v-else-if="row.status === 1" type="success" size="small">已通过</el-tag>
            <el-tag v-else-if="row.status === 2" type="danger" size="small">未通过</el-tag>
            <el-tag v-else-if="row.status === 3" type="info" size="small">已撤回</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rejectReason" label="驳回原因" min-width="120" show-overflow-tooltip />
        <el-table-column prop="createTime" label="申请时间" width="160" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="showDetail(row)">详情</el-button>
            <template v-if="row.status === 0 || row.status === 2">
              <el-button type="success" size="small" link @click="showApprove(row)">通过</el-button>
              <el-button type="danger" size="small" link @click="showReject(row)">驳回</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        style="margin-top:16px"
        :current-page="page"
        :page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadData"
      />
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="实名认证详情" width="600px">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="用户ID">{{ detailData.userId }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ detailData.nickname || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detailData.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="认证状态">
          <el-tag v-if="detailData.status === 0" type="warning">审核中</el-tag>
          <el-tag v-else-if="detailData.status === 1" type="success">已通过</el-tag>
          <el-tag v-else-if="detailData.status === 2" type="danger">未通过</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="姓名" :span="2">{{ detailData.realName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="身份证号" :span="2">{{ detailData.idCard ? maskIdCard(detailData.idCard) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="身份证正面" :span="2">
          <el-image v-if="detailData.idCardFront" :src="detailData.idCardFront" style="width:200px;height:125px" fit="cover" :preview-src-list="[detailData.idCardFront]" />
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="身份证反面" :span="2">
          <el-image v-if="detailData.idCardBack" :src="detailData.idCardBack" style="width:200px;height:125px" fit="cover" :preview-src-list="[detailData.idCardBack]" />
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ detailData.verifyTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="驳回原因" :span="2">{{ detailData.rejectReason || '-' }}</el-descriptions-item>
        <el-descriptions-item label="阿里云认证结果" :span="2">
          <el-input type="textarea" v-if="detailData.aliyunResult" :model-value="formatJson(detailData.aliyunResult)" readonly :rows="3" />
          <span v-else>-</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 通过弹窗 -->
    <el-dialog v-model="approveVisible" title="通过认证" width="500px">
      <el-form label-width="90px">
        <el-form-item label="用户ID">
          <el-input v-model="currentRow.userId" disabled />
        </el-form-item>
        <el-form-item label="真实姓名" required>
          <el-input v-model="approveForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="身份证号" required>
          <el-input v-model="approveForm.idCard" placeholder="请输入身份证号" maxlength="18" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveVisible = false">取消</el-button>
        <el-button type="success" @click="doApprove" :loading="actionLoading">确认通过</el-button>
      </template>
    </el-dialog>

    <!-- 驳回弹窗 -->
    <el-dialog v-model="rejectVisible" title="驳回认证" width="500px">
      <el-form label-width="90px">
        <el-form-item label="用户ID">
          <el-input v-model="currentRow.userId" disabled />
        </el-form-item>
        <el-form-item label="驳回原因" required>
          <el-input v-model="rejectForm.reason" type="textarea" placeholder="请输入驳回原因" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="doReject" :loading="actionLoading">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
document.title = '实名管理 - 龙虾道具交易平台'
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const filterStatus = ref('')
const page = ref(1)
const size = ref(20)
const total = ref(0)
const tableData = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const approveVisible = ref(false)
const rejectVisible = ref(false)
const actionLoading = ref(false)
const detailData = ref({})
const currentRow = ref({})
const approveForm = reactive({ realName: '', idCard: '' })
const rejectForm = reactive({ reason: '' })

const loadData = async (p = 1) => {
  page.value = p
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (filterStatus.value !== '') params.status = filterStatus.value
    const res = await request.get('/admin/real-name/list', { params })
    if (res.code === 0 || res.code === 200) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const showDetail = async (row) => {
  try {
    const res = await request.get(`/admin/real-name/detail/${row.userId}`)
    detailData.value = res.data || {}
    detailVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

const showApprove = (row) => {
  currentRow.value = row
  approveForm.realName = row.realName || ''
  approveForm.idCard = row.idCard || ''
  approveVisible.value = true
}

const showReject = (row) => {
  currentRow.value = row
  rejectForm.reason = ''
  rejectVisible.value = true
}

const doApprove = async () => {
  if (!approveForm.realName || !approveForm.idCard) {
    ElMessage.warning('请填写姓名和身份证号')
    return
  }
  actionLoading.value = true
  try {
    await request.post(`/admin/real-name/approve/${currentRow.value.userId}`, null, {
      params: { realName: approveForm.realName, idCard: approveForm.idCard }
    })
    ElMessage.success('已通过认证')
    approveVisible.value = false
    loadData(page.value)
  } catch (e) {
    console.error(e)
  } finally {
    actionLoading.value = false
  }
}

const doReject = async () => {
  if (!rejectForm.reason) { ElMessage.warning('请填写驳回原因'); return }
  actionLoading.value = true
  try {
    await request.post(`/admin/real-name/reject/${currentRow.value.userId}`, null, {
      params: { reason: rejectForm.reason }
    })
    ElMessage.success('已驳回')
    rejectVisible.value = false
    loadData(page.value)
  } catch (e) {
    console.error(e)
  } finally {
    actionLoading.value = false
  }
}

const maskName = (name) => {
  if (!name) return '-'
  return name[0] + '*'.repeat(name.length - 1)
}

const maskIdCard = (id) => {
  if (!id) return '-'
  return id.substring(0, 4) + '**********' + id.substring(id.length - 4)
}

const formatJson = (str) => {
  try { return JSON.stringify(JSON.parse(str), null, 2) } catch { return str }
}

loadData()
</script>

<style scoped>
.real-name-manage { padding: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
