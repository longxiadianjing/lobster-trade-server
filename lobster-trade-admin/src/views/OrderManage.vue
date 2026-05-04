<template>
  <div class="order-manage">
    <el-card shadow="never">
      <template #header>
        <div class="card-header-flex">
          <span class="card-title">订单管理</span>
          <div class="filter-row">
            <el-input v-model="keyword" placeholder="订单号/商品名称" style="width: 220px;" clearable @change="loadOrders">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-select v-model="filterStatus" placeholder="订单状态" style="width: 140px;" clearable @change="loadOrders">
              <el-option label="待付款" value="pending_pay" />
              <el-option label="已付款" value="paid" />
              <el-option label="已发货" value="submitted" />
              <el-option label="已完成" value="completed" />
              <el-option label="已取消" value="cancelled" />
              <el-option label="仲裁中" value="disputed" />
            </el-select>
            <el-select v-model="filterType" placeholder="订单类型" style="width: 140px;" clearable @change="loadOrders">
              <el-option label="游戏币" value="game_currency" />
              <el-option label="装备" value="equipment" />
              <el-option label="代练" value="boosting" />
            </el-select>
          </div>
        </div>
      </template>

      <el-table :data="orders" v-loading="loading" stripe>
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="productTitle" label="商品" min-width="200" />
        <el-table-column prop="tradeType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ typeMap[row.tradeType] || row.tradeType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="buyerId" label="买家ID" width="100" />
        <el-table-column prop="sellerId" label="卖家ID" width="100" />
        <el-table-column prop="orderAmount" label="金额" width="100">
          <template #default="{ row }">
            <span class="price">¥{{ row.orderAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTypeMap[row.status]" size="small">{{ statusMap[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="disputeStatus" label="仲裁" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.disputeStatus === 1" type="danger" size="small">处理中</el-tag>
            <el-tag v-else-if="row.disputeStatus === 2" type="success" size="small">已处理</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="viewDetail(row)">详情</el-button>
            <el-button size="small" type="info" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.disputeStatus === 1" size="small" type="warning" @click="openDispute(row)">处理</el-button>
            <el-button size="small" type="danger" @click="openIntervene(row)">人工介入</el-button>
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
          @current-change="loadOrders"
        />
      </div>
    </el-card>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="showDetail" title="订单详情" width="680px">
      <el-descriptions :column="2" border v-if="currentOrder">
        <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单类型">
          <el-tag size="small">{{ typeMap[currentOrder.tradeType] || currentOrder.tradeType }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="商品名称" :span="2">{{ currentOrder.productTitle }}</el-descriptions-item>
        <el-descriptions-item label="买家ID">{{ currentOrder.buyerId }}</el-descriptions-item>
        <el-descriptions-item label="卖家ID">{{ currentOrder.sellerId }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">
          <span class="price-text">¥{{ currentOrder.orderAmount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="statusTypeMap[currentOrder.status]" size="small">{{ statusMap[currentOrder.status] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="保险柜">{{ currentOrder.escrowAmount ? `¥${currentOrder.escrowAmount}` : '-' }}</el-descriptions-item>
        <el-descriptions-item label="卖家实收">{{ currentOrder.sellerReceived ? `¥${currentOrder.sellerReceived}` : '-' }}</el-descriptions-item>
        <el-descriptions-item label="游戏ID">{{ currentOrder.gameId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="区服">{{ currentOrder.server || '-' }}</el-descriptions-item>
        <el-descriptions-item label="仲裁状态" :span="2">
          <el-tag v-if="currentOrder.disputeStatus === 1" type="danger" size="small">处理中</el-tag>
          <el-tag v-else-if="currentOrder.disputeStatus === 2" type="success" size="small">已处理</el-tag>
          <span v-else>无</span>
          <span v-if="currentOrder.disputeReason" style="margin-left:8px;color:#999;">原因：{{ currentOrder.disputeReason }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentOrder.createTime }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ currentOrder.paymentTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ currentOrder.confirmTime || currentOrder.actualCompleteTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发货备注" :span="2" v-if="currentOrder.deliveryRemark">{{ currentOrder.deliveryRemark }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="showDetail = false">关闭</el-button>
        <el-button type="primary" @click="openEditFromDetail">编辑</el-button>
      </template>
    </el-dialog>

    <!-- 订单编辑弹窗 -->
    <el-dialog v-model="editVisible" title="编辑订单" width="500px" destroy-on-close>
      <el-form :model="editForm" label-width="100px" v-loading="editLoading">
        <el-form-item label="订单类型">
          <el-select v-model="editForm.tradeType" style="width:100%">
            <el-option label="游戏币" value="game_currency" />
            <el-option label="装备" value="equipment" />
            <el-option label="代练(boost)" value="boosting" />
            <el-option label="代练(boost)" value="boost" />
            <el-option label="陪玩" value="accompanying" />
            <el-option label="护航" value="escort" />
            <el-option label="商品" value="goods" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input v-model="editForm.productTitle" placeholder="商品名称" maxlength="200" />
        </el-form-item>
        <el-form-item label="游戏ID">
          <el-input-number v-model="editForm.gameId" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="卖家ID">
          <el-input-number v-model="editForm.sellerId" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="买家ID">
          <el-input-number v-model="editForm.buyerId" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="editForm.status" style="width:100%">
            <el-option label="待付款" value="pending_pay" />
            <el-option label="已付款" value="paid" />
            <el-option label="进行中" value="in_progress" />
            <el-option label="已发货" value="submitted" />
            <el-option label="已确认" value="confirmed" />
            <el-option label="已完成" value="completed" />
            <el-option label="仲裁中" value="disputed" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单金额">
          <el-input-number v-model="editForm.orderAmount" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="定金(卖方)">
          <el-input-number v-model="editForm.depositSeller" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="定金(买方)">
          <el-input-number v-model="editForm.depositBuyer" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="保险柜金额">
          <el-input-number v-model="editForm.escrowAmount" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="卖家实收">
          <el-input-number v-model="editForm.sellerReceived" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="托管状态">
          <el-select v-model="editForm.escrowStatus" style="width:100%">
            <el-option label="未托管" :value="1" />
            <el-option label="已托管" :value="2" />
            <el-option label="已释放" :value="3" />
            <el-option label="已退款" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付状态">
          <el-select v-model="editForm.paymentStatus" style="width:100%">
            <el-option label="未支付" :value="0" />
            <el-option label="已支付" :value="1" />
            <el-option label="已退款" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="发货备注">
          <el-input v-model="editForm.deliveryRemark" type="textarea" :rows="2" placeholder="发货备注" />
        </el-form-item>
        <el-form-item label="代练要求">
          <el-input v-model="editForm.boostRequirement" type="textarea" :rows="2" placeholder="代练要求" />
        </el-form-item>
        <el-form-item label="退款原因">
          <el-input v-model="editForm.refundReason" placeholder="退款原因" />
        </el-form-item>
        <el-form-item label="仲裁状态">
          <el-select v-model="editForm.disputeStatus" style="width:100%">
            <el-option label="无" :value="0" />
            <el-option label="处理中" :value="1" />
            <el-option label="已处理" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="仲裁原因">
          <el-input v-model="editForm.disputeReason" placeholder="仲裁原因" />
        </el-form-item>
        <el-form-item label="仲裁结果">
          <el-input v-model="editForm.disputeResult" type="textarea" :rows="2" placeholder="仲裁结果" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="doEdit" :loading="editLoading">保存</el-button>
      </template>
    </el-dialog>
    <!-- 人工介入弹窗 -->
    <el-dialog v-model="interventionVisible" title="人工介入订单" width="480px" destroy-on-close>
      <div v-if="interventionOrder" class="intervene-info">
        <el-alert type="warning" :closable="false" show-icon>
          <template #title>
            <span>订单号：{{ interventionOrder.orderNo }}</span>
            <span style="margin-left:16px;">当前状态：{{ statusMap[interventionOrder.status] }}</span>
          </template>
        </el-alert>
      </div>
      <el-form :model="interventionForm" label-width="90px" style="margin-top:16px;">
        <el-form-item label="操作类型">
          <el-radio-group v-model="interventionForm.action">
            <el-radio label="complete">强制完成</el-radio>
            <el-radio label="cancel">强制取消</el-radio>
            <el-radio label="dispute">设为仲裁中</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="操作原因" required>
          <el-input v-model="interventionForm.reason" type="textarea" :rows="3" placeholder="请填写介入原因（必填），用于审计追踪" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="interventionVisible = false">取消</el-button>
        <el-button type="primary" :loading="interventionLoading" @click="doIntervene">确认介入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const orders = ref([])
const keyword = ref('')
const filterStatus = ref('')
const filterType = ref('')
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })

const showDetail = ref(false)
const currentOrder = ref(null)
const interventionVisible = ref(false)
const interventionLoading = ref(false)
const interventionOrder = ref(null)
const interventionForm = reactive({ action: 'complete', reason: '' })

const typeMap = { game_currency: '游戏币', equipment: '装备', boosting: '代练', accompanying: '陪玩', escort: '护航', goods: '商品' }
const statusMap = { pending_pay: '待付款', paid: '已付款', in_progress: '进行中', submitted: '已发货', confirmed: '已确认', completed: '已完成', disputed: '仲裁中', cancelled: '已取消' }
const statusTypeMap = { pending_pay: 'warning', paid: 'primary', in_progress: '', submitted: 'success', confirmed: 'warning', completed: 'info', disputed: 'danger', cancelled: 'info' }

const loadOrders = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/orders', {
      params: { page: pagination.page, pageSize: pagination.pageSize, status: filterStatus.value, tradeType: filterType.value, keyword: keyword.value }
    })
    if (res.data) {
      orders.value = res.data.records || res.data
      pagination.total = res.data.total || 0
    }
  } catch (e) {
    ElMessage.error('加载订单列表失败')
  } finally {
    loading.value = false
  }
}

const viewDetail = (row) => {
  currentOrder.value = row
  showDetail.value = true
}

const editId = ref(null)
const editVisible = ref(false)
const editLoading = ref(false)
const editForm = reactive({
  tradeType: '', productTitle: '', gameId: null, sellerId: null, buyerId: null,
  status: '', orderAmount: null, depositSeller: null, depositBuyer: null,
  escrowAmount: null, sellerReceived: null, escrowStatus: null, paymentStatus: null,
  deliveryRemark: '', boostRequirement: '', refundReason: '', disputeStatus: 0,
  disputeReason: '', disputeResult: '', disputeId: null, appealTime: ''
})

const handleEdit = (row) => {
  editId.value = row.id
  editForm.tradeType = row.tradeType || ''
  editForm.productTitle = row.productTitle || ''
  editForm.gameId = row.gameId || null
  editForm.sellerId = row.sellerId || null
  editForm.buyerId = row.buyerId || null
  editForm.status = row.status || ''
  editForm.orderAmount = row.orderAmount
  editForm.depositSeller = row.depositSeller || null
  editForm.depositBuyer = row.depositBuyer || null
  editForm.escrowAmount = row.escrowAmount
  editForm.sellerReceived = row.sellerReceived
  editForm.escrowStatus = row.escrowStatus || null
  editForm.paymentStatus = row.paymentStatus || null
  editForm.deliveryRemark = row.deliveryRemark || ''
  editForm.boostRequirement = row.boostRequirement || ''
  editForm.refundReason = row.refundReason || ''
  editForm.disputeStatus = row.disputeStatus || 0
  editForm.disputeReason = row.disputeReason || ''
  editForm.disputeResult = row.disputeResult || ''
  editForm.disputeId = row.disputeId || null
  editForm.appealTime = row.appealTime || ''
  editVisible.value = true
}

const doEdit = async () => {
  if (!editId.value) return
  editLoading.value = true
  try {
    await request.put(`/admin/order/${editId.value}`, editForm)
    ElMessage.success('保存成功')
    editVisible.value = false
    loadOrders()
  } catch (e) { ElMessage.error('保存失败') }
  finally { editLoading.value = false }
}

const openEditFromDetail = () => {
  if (!currentOrder.value) return
  handleEdit(currentOrder.value)
  showDetail.value = false
}

onMounted(() => { loadOrders() })

const openDispute = async (row) => {
  try {
    await request.put(`/admin/dispute/resolve/${row.id}`, { result: '管理员处理' })
    ElMessage.success('纠纷已处理')
    loadOrders()
  } catch (e) {
    ElMessage.error('处理失败')
  }
}

const openIntervene = (row) => {
  interventionOrder.value = row
  interventionForm.action = 'complete'
  interventionForm.reason = ''
  interventionVisible.value = true
}

const doIntervene = async () => {
  if (!interventionForm.reason.trim()) {
    ElMessage.warning('请填写介入原因')
    return
  }
  if (!interventionOrder.value) return
  interventionLoading.value = true
  try {
    const actionMap = { complete: 'completed', cancel: 'cancelled', dispute: 'disputed' }
    const payload = {
      status: actionMap[interventionForm.action],
      disputeStatus: interventionForm.action === 'dispute' ? 1 : interventionOrder.value.disputeStatus,
      disputeReason: interventionForm.reason
    }
    await request.put(`/admin/order/${interventionOrder.value.id}`, payload)
    ElMessage.success('人工介入成功，订单状态已更新')
    interventionVisible.value = false
    loadOrders()
  } catch (e) {
    ElMessage.error('介入失败：' + (e.message || ''))
  } finally {
    interventionLoading.value = false
  }
}
</script>

<style scoped>
.card-header-flex { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 10px; }
.card-title { font-size: 15px; font-weight: 600; color: #333; }
.filter-row { display: flex; gap: 10px; align-items: center; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
.price { font-weight: 600; color: #667eea; }
.price-text { font-weight: 600; color: #667eea; }
</style>
