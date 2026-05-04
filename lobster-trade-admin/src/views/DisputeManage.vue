<template>
  <div class="dispute-manage">
    <el-card shadow="never">
      <template #header>
        <div class="card-header-flex">
          <span class="card-title">仲裁管理</span>
          <div class="header-right">
            <el-tag type="danger" effect="plain">待处理: {{ stats.pending }}</el-tag>
            <el-tag type="success" effect="plain">已处理: {{ stats.resolved }}</el-tag>
          </div>
        </div>
      </template>

      <el-table :data="disputes" v-loading="loading" stripe>
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="productTitle" label="商品" min-width="200" show-overflow-tooltip />
        <el-table-column prop="buyerId" label="买家ID" width="90" />
        <el-table-column prop="sellerId" label="卖家ID" width="90" />
        <el-table-column prop="orderAmount" label="订单金额" width="100">
          <template #default="{ row }">
            <span class="price">¥{{ row.orderAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="escrowAmount" label="托管金额" width="100">
          <template #default="{ row }">
            <span class="text-muted">¥{{ row.escrowAmount || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="disputeReason" label="申诉原因" min-width="160" show-overflow-tooltip />
        <el-table-column prop="disputeStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.disputeStatus === 1" type="danger" size="small">处理中</el-tag>
            <el-tag v-else-if="row.disputeStatus === 2" type="success" size="small">已处理</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申诉时间" width="160" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" plain @click="openDetail(row)">详情</el-button>
            <el-button size="small" type="info" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.disputeStatus === 1" type="danger" size="small" @click="openResolve(row)">处理</el-button>
            <el-button v-else size="small" @click="openResolve(row)">重审</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, prev, pager, next"
          @current-change="loadDisputes"
        />
      </div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="仲裁详情" width="640px" destroy-on-close>
      <div v-if="currentDispute" class="detail-content">
        <div class="detail-section">
          <h4 class="section-title">订单信息</h4>
          <div class="detail-grid">
            <div class="detail-item"><label>订单号</label><span class="mono">{{ currentDispute.orderNo }}</span></div>
            <div class="detail-item"><label>商品</label><span>{{ currentDispute.productTitle }}</span></div>
            <div class="detail-item"><label>交易类型</label><span>{{ currentDispute.tradeType || '-' }}</span></div>
            <div class="detail-item"><label>订单金额</label><span class="price">¥{{ currentDispute.orderAmount }}</span></div>
            <div class="detail-item"><label>买家ID</label><span>{{ currentDispute.buyerId }}</span></div>
            <div class="detail-item"><label>卖家ID</label><span>{{ currentDispute.sellerId }}</span></div>
          </div>
        </div>

        <div class="detail-section">
          <h4 class="section-title">资金信息</h4>
          <div class="detail-grid">
            <div class="detail-item"><label>托管金额</label><span>¥{{ currentDispute.escrowAmount || '0.00' }}</span></div>
            <div class="detail-item"><label>卖家保证金</label><span>¥{{ currentDispute.depositSeller || '0.00' }}</span></div>
            <div class="detail-item"><label>买家保证金</label><span>¥{{ currentDispute.depositBuyer || '0.00' }}</span></div>
            <div class="detail-item"><label>平台手续费</label><span>¥{{ currentDispute.platformFee || '0.00' }}</span></div>
            <div class="detail-item"><label>卖家实收</label><span>¥{{ currentDispute.sellerReceived || '0.00' }}</span></div>
            <div class="detail-item"><label>托管状态</label>
              <el-tag size="small" :type="escrowStatusType(currentDispute.escrowStatus)">
                {{ escrowStatusText(currentDispute.escrowStatus) }}
              </el-tag>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h4 class="section-title">仲裁信息</h4>
          <div class="detail-grid">
            <div class="detail-item"><label>申诉原因</label><span class="text-danger">{{ currentDispute.disputeReason || '-' }}</span></div>
            <div class="detail-item"><label>仲裁状态</label>
              <el-tag size="small" :type="currentDispute.disputeStatus === 1 ? 'danger' : 'success'">
                {{ currentDispute.disputeStatus === 1 ? '处理中' : '已处理' }}
              </el-tag>
            </div>
            <div class="detail-item full-width"><label>处理结果</label><span>{{ currentDispute.disputeResult || '待处理' }}</span></div>
            <div class="detail-item"><label>申诉时间</label><span>{{ currentDispute.createTime }}</span></div>
            <div class="detail-item"><label>处理时间</label><span>{{ currentDispute.disputeTime || '-' }}</span></div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button v-if="currentDispute?.disputeStatus === 1" type="danger" @click="openResolve(currentDispute); detailVisible = false">去处理</el-button>
        <el-button v-else type="primary" @click="handleEdit(currentDispute); detailVisible = false">编辑</el-button>
      </template>
    </el-dialog>

    <!-- 处理仲裁弹窗 -->
    <el-dialog v-model="showResolve" :title="resolveTitle" width="500px" destroy-on-close>
      <el-form :model="resolveForm" label-width="100px">
        <el-form-item label="订单号"><span class="mono">{{ currentDispute?.orderNo }}</span></el-form-item>
        <el-form-item label="订单金额"><span class="price">¥{{ currentDispute?.orderAmount }}</span></el-form-item>
        <el-form-item label="托管金额">¥{{ currentDispute?.escrowAmount || '0.00' }}</el-form-item>
        <el-form-item label="申诉原因"><span class="text-danger">{{ currentDispute?.disputeReason }}</span></el-form-item>
        <el-form-item label="处理结果" required>
          <el-radio-group v-model="resolveForm.result">
            <el-radio value="退款">全额退款给买家（订单取消）</el-radio>
            <el-radio value="放款">放款给卖家（订单完成）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="resolveForm.note" type="textarea" :rows="2" placeholder="可选备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showResolve = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleResolve">确认处理</el-button>
      </template>
    </el-dialog>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editVisible" title="编辑仲裁" width="460px" destroy-on-close>
      <el-form :model="editForm" label-width="90px" v-loading="editLoading">
        <el-form-item label="订单号">{{ currentDispute?.orderNo }}</el-form-item>
        <el-form-item label="仲裁状态">
          <el-select v-model="editForm.disputeStatus" style="width:100%">
            <el-option label="处理中" :value="1" />
            <el-option label="已处理" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理结果">
          <el-input v-model="editForm.disputeResult" type="textarea" :rows="2" placeholder="填写处理结果" />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="editForm.status" style="width:100%">
            <el-option label="待付款" value="pending_pay" />
            <el-option label="已付款" value="paid" />
            <el-option label="进行中" value="in_progress" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
            <el-option label="仲裁中" value="disputed" />
          </el-select>
        </el-form-item>
        <el-form-item label="托管状态">
          <el-select v-model="editForm.escrowStatus" style="width:100%">
            <el-option label="未托管" :value="0" />
            <el-option label="已托管" :value="1" />
            <el-option label="已释放" :value="2" />
            <el-option label="已退款" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="doEdit" :loading="editLoading">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const disputes = ref([])
const stats = ref({ pending: 0, resolved: 0 })
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })
const showResolve = ref(false)
const submitting = ref(false)
const currentDispute = ref(null)
const resolveForm = reactive({ result: '退款', note: '' })
const detailVisible = ref(false)

const escrowStatusText = (v) => {
  const m = { 0: '未托管', 1: '已托管', 2: '已释放', 3: '已退款' }
  return m[v] || '-'
}
const escrowStatusType = (v) => {
  const m = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }
  return m[v] || 'info'
}

const loadDisputes = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/disputes', { params: { page: pagination.page, size: pagination.pageSize } })
    if (res.data) {
      disputes.value = res.data.records || res.data
      pagination.total = res.data.total || 0
    }
    stats.value.pending = disputes.value.filter(d => d.disputeStatus === 1).length
    stats.value.resolved = disputes.value.filter(d => d.disputeStatus === 2).length
  } catch (e) {
    stats.value.pending = 0; stats.value.resolved = 0
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const openDetail = (row) => {
  currentDispute.value = row
  detailVisible.value = true
}

const openResolve = (row) => {
  currentDispute.value = row
  resolveForm.result = '退款'
  resolveForm.note = ''
  showResolve.value = true
}

const handleResolve = async () => {
  if (!resolveForm.result) { ElMessage.warning('请选择处理结果'); return }
  submitting.value = true
  try {
    const resultText = resolveForm.result + (resolveForm.note ? ' - ' + resolveForm.note : '')
    await request.post(`/admin/dispute/resolve/${currentDispute.value.id}`, { result: resultText })
    ElMessage.success('处理完成')
    showResolve.value = false
    loadDisputes()
  } catch (e) { ElMessage.error('处理失败') }
  finally { submitting.value = false }
}

const resolveTitle = computed(() => currentDispute.value?.disputeStatus === 1 ? '处理仲裁' : '重审仲裁')

const editVisible = ref(false)
const editLoading = ref(false)
const editForm = reactive({ disputeStatus: null, disputeResult: '', status: '', escrowStatus: null })

const handleEdit = (row) => {
  currentDispute.value = row
  editForm.disputeStatus = row.disputeStatus
  editForm.disputeResult = row.disputeResult || ''
  editForm.status = row.status || ''
  editForm.escrowStatus = row.escrowStatus ?? null
  editVisible.value = true
}

const doEdit = async () => {
  editLoading.value = true
  try {
    await request.put(`/admin/dispute/${currentDispute.value.id}`, editForm)
    ElMessage.success('保存成功')
    editVisible.value = false
    loadDisputes()
  } catch (e) { ElMessage.error('保存失败') }
  finally { editLoading.value = false }
}

onMounted(() => { loadDisputes() })
</script>

<style scoped>
.dispute-manage { padding: 0; }
.card-header-flex { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-size: 15px; font-weight: 600; color: #333; }
.header-right { display: flex; gap: 8px; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
.price { font-weight: 600; color: #667eea; }
.text-muted { color: #888; }
.text-danger { color: #f56c6c; }
.mono { font-family: monospace; font-size: 13px; }

.detail-content { padding: 0 4px; }
.detail-section { margin-bottom: 20px; }
.section-title { font-size: 13px; font-weight: 600; color: #667eea; margin: 0 0 12px; padding-bottom: 6px; border-bottom: 1px solid #f0f0f0; }
.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.detail-item { display: flex; flex-direction: column; gap: 4px; }
.detail-item label { font-size: 12px; color: #999; }
.detail-item span { font-size: 14px; color: #333; }
.full-width { grid-column: 1 / -1; }
</style>