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
        <!-- 买卖双方信息 -->
        <div class="parties-section">
          <div class="party-card buyer">
            <div class="party-avatar">买</div>
            <div class="party-info">
              <div class="party-label">买家</div>
              <div class="party-id">ID: {{ currentDispute.buyerId }}</div>
              <div class="party-badge">申诉发起方</div>
            </div>
          </div>
          <div class="vs-badge">VS</div>
          <div class="party-card seller">
            <div class="party-avatar">卖</div>
            <div class="party-info">
              <div class="party-label">卖家</div>
              <div class="party-id">ID: {{ currentDispute.sellerId }}</div>
            </div>
          </div>
        </div>

        <!-- 订单核心信息 -->
        <div class="detail-section">
          <h4 class="section-title">订单信息</h4>
          <div class="detail-grid">
            <div class="detail-item"><label>订单号</label><span class="mono">{{ currentDispute.orderNo }}</span></div>
            <div class="detail-item"><label>商品</label><span>{{ currentDispute.productTitle }}</span></div>
            <div class="detail-item"><label>交易类型</label><span>{{ currentDispute.tradeType || '-' }}</span></div>
            <div class="detail-item"><label>订单金额</label><span class="price-highlight">¥{{ currentDispute.orderAmount }}</span></div>
          </div>
        </div>

        <!-- 资金流向卡片 -->
        <div class="detail-section">
          <h4 class="section-title">资金流向</h4>
          <div class="fund-flow">
            <div class="fund-node buyer-fund">
              <div class="fund-label">买家已付</div>
              <div class="fund-amount">¥{{ currentDispute.escrowAmount || currentDispute.orderAmount }}</div>
              <div class="fund-sub">已冻结</div>
            </div>
            <div class="fund-arrow">→</div>
            <div class="fund-node platform-fund">
              <div class="fund-label">平台托管</div>
              <div class="fund-amount">¥{{ currentDispute.escrowAmount || '0.00' }}</div>
              <div class="fund-sub">仲裁中</div>
            </div>
            <div class="fund-arrow resolve-arrow">→</div>
            <div class="fund-node result-fund" :class="currentDispute.disputeStatus === 2 ? 'resolved' : 'pending'">
              <div class="fund-label">仲裁结果</div>
              <div class="fund-amount">{{ currentDispute.disputeResult || '待裁定' }}</div>
              <div class="fund-sub">{{ currentDispute.disputeStatus === 2 ? '已处理' : '处理中' }}</div>
            </div>
          </div>
          <div class="fund-detail-row">
            <div class="fund-chip"><span class="chip-label">卖家保证金</span><span class="chip-val">¥{{ currentDispute.depositSeller || '0.00' }}</span></div>
            <div class="fund-chip"><span class="chip-label">买家保证金</span><span class="chip-val">¥{{ currentDispute.depositBuyer || '0.00' }}</span></div>
            <div class="fund-chip"><span class="chip-label">平台手续费</span><span class="chip-val">¥{{ currentDispute.platformFee || '0.00' }}</span></div>
            <div class="fund-chip"><span class="chip-label">卖家实收</span><span class="chip-val">¥{{ currentDispute.sellerReceived || '0.00' }}</span></div>
          </div>
        </div>

        <!-- 仲裁信息 -->
        <div class="detail-section">
          <h4 class="section-title">仲裁信息</h4>
          <div class="dispute-reason-box">
            <span class="dispute-reason-label">申诉原因：</span>
            <span class="dispute-reason-text">{{ currentDispute.disputeReason || '未填写' }}</span>
          </div>
          <div class="timeline-mini">
            <div class="timeline-item" :class="{active: true}">
              <div class="timeline-dot"></div>
              <div class="timeline-content">申诉发起 {{ currentDispute.createTime }}</div>
            </div>
            <div class="timeline-item" :class="{active: currentDispute.disputeStatus === 2}">
              <div class="timeline-dot"></div>
              <div class="timeline-content"> {{ currentDispute.disputeStatus === 2 ? '仲裁处理 ' + currentDispute.disputeTime : '等待处理' }}</div>
            </div>
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
    <el-dialog v-model="showResolve" :title="resolveTitle" width="520px" destroy-on-close>
      <div class="resolve-content">
        <div class="resolve-order-info">
          <el-tag type="info" effect="plain">订单 {{ currentDispute?.orderNo }}</el-tag>
          <span class="resolve-amount">托管金额 <strong>¥{{ currentDispute?.escrowAmount || '0.00' }}</strong></span>
        </div>

        <div class="resolve-type-label">选择仲裁结果</div>
        <div class="resolve-cards">
          <div class="resolve-card refund" :class="{selected: resolveForm.result === '退款'}" @click="resolveForm.result = '退款'">
            <div class="resolve-card-icon">↩</div>
            <div class="resolve-card-title">退款给买家</div>
            <div class="resolve-card-desc">订单取消，全额退还买家已付金额</div>
            <div class="resolve-card-amount">¥{{ currentDispute?.escrowAmount || '0.00' }}</div>
          </div>
          <div class="resolve-card release" :class="{selected: resolveForm.result === '放款'}" @click="resolveForm.result = '放款'">
            <div class="resolve-card-icon">↗</div>
            <div class="resolve-card-title">放款给卖家</div>
            <div class="resolve-card-desc">订单完成，金额转给卖家</div>
            <div class="resolve-card-amount">¥{{ currentDispute?.escrowAmount || '0.00' }}</div>
          </div>
        </div>

        <!-- 金额预览 -->
        <div class="amount-preview" v-if="resolveForm.result">
          <div class="preview-title">金额变动预览</div>
          <div class="preview-row">
            <span class="preview-label">买家获得退款</span>
            <span class="preview-val refund-text">{{ resolveForm.result === '退款' ? '¥' + (currentDispute?.escrowAmount || '0.00') : '—' }}</span>
          </div>
          <div class="preview-row">
            <span class="preview-label">卖家实收</span>
            <span class="preview-val release-text">{{ resolveForm.result === '放款' ? '¥' + (currentDispute?.escrowAmount || '0.00') : '—' }}</span>
          </div>
        </div>

        <div class="resolve-note">
          <div class="note-label">处理备注 <span style="color:#999;font-weight:400">（可选）</span></div>
          <el-input v-model="resolveForm.note" type="textarea" :rows="2" placeholder="填写处理备注，说明仲裁依据..." />
        </div>
      </div>
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
document.title = '纠纷管理 - 龙虾道具交易平台'
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

/* 买卖双方卡片 */
.parties-section { display: flex; align-items: center; gap: 16px; margin-bottom: 20px; }
.party-card { display: flex; align-items: center; gap: 12px; flex: 1; padding: 14px 16px; border-radius: 12px; border: 1px solid #eee; }
.party-card.buyer { background: linear-gradient(135deg, #fff1f0, #ffcec9); border-color: #ffccc7; }
.party-card.seller { background: linear-gradient(135deg, #f0f5ff, #adc6ff); border-color: #adc6ff; }
.party-avatar { width: 44px; height: 44px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 18px; font-weight: 700; color: #fff; background: linear-gradient(135deg, #667eea, #764ba2); }
.party-info { flex: 1; }
.party-label { font-size: 13px; color: #666; }
.party-id { font-size: 15px; font-weight: 700; color: #333; margin-top: 2px; font-family: monospace; }
.party-badge { font-size: 11px; background: #ff4d4f; color: #fff; padding: 2px 8px; border-radius: 10px; display: inline-block; margin-top: 4px; }
.vs-badge { font-size: 13px; font-weight: 800; color: #ccc; }

/* 资金流向 */
.fund-flow { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; }
.fund-node { flex: 1; text-align: center; padding: 12px 8px; border-radius: 10px; background: #f5f5f5; }
.fund-node.buyer-fund { background: linear-gradient(135deg, #fff1f0, #ffe7e6); border: 1px solid #ffccc7; }
.fund-node.platform-fund { background: linear-gradient(135deg, #f0f5ff, #e6eaff); border: 1px solid #adc6ff; }
.fund-node.result-fund.resolved { background: linear-gradient(135deg, #f6ffed, #d9f7be); border: 1px solid #b7eb8f; }
.fund-node.result-fund.pending { background: linear-gradient(135deg, #fff7e6, #ffd591); border: 1px solid #ffe7ba; }
.fund-label { font-size: 11px; color: #888; margin-bottom: 4px; }
.fund-amount { font-size: 16px; font-weight: 700; color: #333; }
.fund-sub { font-size: 11px; color: #999; margin-top: 2px; }
.fund-arrow { font-size: 20px; color: #ccc; }
.fund-detail-row { display: flex; gap: 8px; flex-wrap: wrap; }
.fund-chip { display: flex; align-items: center; gap: 6px; padding: 6px 12px; background: #f9f9f9; border-radius: 6px; border: 1px solid #eee; }
.chip-label { font-size: 12px; color: #888; }
.chip-val { font-size: 13px; font-weight: 600; color: #333; }

/* 详情弹窗 */
.detail-content { padding: 0 4px; }
.section-title { font-size: 13px; font-weight: 600; color: #667eea; margin: 0 0 12px; padding-bottom: 6px; border-bottom: 1px solid #f0f0f0; }
.detail-section { margin-bottom: 20px; }
.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.detail-item { display: flex; flex-direction: column; gap: 4px; }
.detail-item label { font-size: 12px; color: #999; }
.detail-item span { font-size: 14px; color: #333; }
.full-width { grid-column: 1 / -1; }
.price-highlight { font-size: 18px !important; font-weight: 700; color: #667eea !important; }

/* 申诉原因 */
.dispute-reason-box { display: flex; gap: 8px; align-items: flex-start; background: #fff2e6; border: 1px solid #ffd591; border-radius: 8px; padding: 12px; margin-bottom: 12px; }
.dispute-reason-label { font-size: 12px; color: #ad6800; font-weight: 600; white-space: nowrap; }
.dispute-reason-text { font-size: 14px; color: #ad6800; line-height: 1.5; }

/* 迷你时间线 */
.timeline-mini { display: flex; flex-direction: column; gap: 0; }
.timeline-item { display: flex; align-items: center; gap: 10px; padding: 6px 0; }
.timeline-dot { width: 8px; height: 8px; border-radius: 50%; background: #ddd; flex-shrink: 0; }
.timeline-item.active .timeline-dot { background: #667eea; }
.timeline-content { font-size: 13px; color: #999; }
.timeline-item.active .timeline-content { color: #333; font-weight: 600; }

/* 处理弹窗 */
.resolve-content { padding: 4px 0; }
.resolve-order-info { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; padding: 10px 14px; background: #f5f5f5; border-radius: 8px; }
.resolve-amount { font-size: 14px; color: #666; }
.resolve-amount strong { font-size: 18px; color: #667eea; font-weight: 700; }
.resolve-type-label { font-size: 13px; font-weight: 600; color: #333; margin-bottom: 10px; }
.resolve-cards { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 16px; }
.resolve-card { border: 2px solid #eee; border-radius: 12px; padding: 16px 12px; cursor: pointer; transition: all 0.2s; text-align: center; }
.resolve-card.refund:hover, .resolve-card.refund.selected { border-color: #ff4d4f; background: #fff1f0; }
.resolve-card.release:hover, .resolve-card.release.selected { border-color: #52c41a; background: #f6ffed; }
.resolve-card.selected { box-shadow: 0 0 0 3px rgba(102,126,234,0.15); }
.resolve-card-icon { font-size: 28px; margin-bottom: 6px; }
.resolve-card.refund .resolve-card-icon { color: #ff4d4f; }
.resolve-card.release .resolve-card-icon { color: #52c41a; }
.resolve-card-title { font-size: 14px; font-weight: 700; color: #333; margin-bottom: 4px; }
.resolve-card-desc { font-size: 12px; color: #888; margin-bottom: 8px; line-height: 1.4; }
.resolve-card-amount { font-size: 16px; font-weight: 700; color: #667eea; }
.resolve-card.refund.selected .resolve-card-amount { color: #ff4d4f; }
.resolve-card.release.selected .resolve-card-amount { color: #52c41a; }

/* 金额预览 */
.amount-preview { background: #f9f9f9; border-radius: 10px; padding: 12px 14px; margin-bottom: 14px; }
.preview-title { font-size: 12px; color: #888; margin-bottom: 8px; }
.preview-row { display: flex; justify-content: space-between; font-size: 14px; padding: 4px 0; }
.preview-label { color: #666; }
.preview-val { font-weight: 700; }
.refund-text { color: #ff4d4f; }
.release-text { color: #52c41a; }

/* 备注 */
.resolve-note { margin-top: 4px; }
.note-label { font-size: 13px; font-weight: 600; color: #333; margin-bottom: 8px; }
</style>