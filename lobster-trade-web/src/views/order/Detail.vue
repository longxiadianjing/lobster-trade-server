<template>
  <div class="detail-container">
    <!-- 顶部导航 -->
    <div class="top-header">
      <div class="header-inner">
        <router-link to="/home" class="logo">🦞 龙虾道具交易平台</router-link>
        <div class="header-actions">
          <el-button @click="router.push({ path: '/order/center' })">返回订单中心</el-button>
        </div>
      </div>
    </div>

    <!-- 主内容 -->
    <div class="detail-main">
      <main class="content">
        <el-card class="order-detail-card" shadow="never" v-loading="loading">
          <template v-if="order">
            <!-- 订单状态卡片 -->
            <div class="status-banner" :class="order.status">
              <div class="status-icon">
                <el-icon :size="40"><CircleCheckFilled v-if="order.status === 'completed'" /><Clock v-else-if="order.status === 'pending_payment'" /><Van v-else /><Warning /></el-icon>
              </div>
              <div class="status-text">
                <h3>{{ getStatusText(order.status) }}</h3>
                <p>{{ getStatusDesc(order.status) }}</p>
              </div>
            </div>

            <!-- 订单信息 -->
            <div class="info-section">
              <h3 class="section-title">订单信息</h3>
              <div class="info-grid">
                <div class="info-row">
                  <span class="info-label">订单编号</span>
                  <span class="info-value">{{ order.id }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">创建时间</span>
                  <span class="info-value">{{ formatTime(order.createTime) }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">订单类型</span>
                  <span class="info-value">{{ typeMap[order.tradeType] || order.productType }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">游戏/区服</span>
                  <span class="info-value">{{ order.gameName }} · {{ order.gameZone || '全部区服' }}</span>
                </div>
              </div>
            </div>

            <!-- 商品信息 -->
            <div class="info-section">
              <h3 class="section-title">商品信息</h3>
              <div class="product-block" @click="router.push(`/product/detail/${order.productId}`)">
                <div class="product-image">
                  <img v-if="order.coverImage" :src="order.coverImage" alt="商品图片" />
                  <div v-else class="image-placeholder">
                    <el-icon :size="32"><Picture /></el-icon>
                  </div>
                </div>
                <div class="product-info">
                  <p class="product-title">{{ order.productTitle }}</p>
                  <p class="product-price">¥{{ order.amount }}</p>
                </div>
              </div>
            </div>

            <!-- 交易双方 -->
            <div class="info-section">
              <h3 class="section-title">交易双方</h3>
              <div class="party-row">
                <div class="party-item">
                  <p class="party-role">买家</p>
                  <p class="party-name">{{ order.buyerNickname }}</p>
                </div>
                <div class="party-arrow">
                  <el-icon :size="20"><Right /></el-icon>
                </div>
                <div class="party-item">
                  <p class="party-role">卖家</p>
                  <p class="party-name">{{ order.sellerNickname }}</p>
                </div>
              </div>
            </div>

            <!-- 金额信息 -->
            <div class="info-section amount-section">
              <h3 class="section-title">支付金额</h3>
              <div class="amount-block">
                <div class="amount-row">
                  <span>商品金额</span>
                  <span>¥{{ order.amount }}</span>
                </div>
                <div class="amount-row" v-if="order.couponDiscount && order.couponDiscount > 0">
                  <span>优惠券</span>
                  <span class="discount-value">-¥{{ order.couponDiscount }}</span>
                </div>
                <div class="amount-row">
                  <span>手续费</span>
                  <span>¥{{ order.serviceFee || '0.00' }}</span>
                </div>
                <div class="amount-row total">
                  <span>合计</span>
                  <span class="total-price">¥{{ order.totalAmount || order.amount }}</span>
                </div>
              </div>
            </div>

            <!-- 交易安全保障 -->
            <div class="info-section security-insurance-section">
              <h3 class="section-title">交易安全保障</h3>
              <div class="insurance-banner">
                <div class="insurance-icon">🛡️</div>
                <div class="insurance-text">
                  <p class="insurance-title">平台交易安全险</p>
                  <p class="insurance-desc">本订单已纳入平台交易安全保障体系，如遇交易纠纷可申请平台介入并申请赔付，全程保护您的资金安全。</p>
                </div>
              </div>
            </div>

            <!-- 操作日志 -->
            <div class="info-section" v-if="order.progressList && order.progressList.length > 0">
              <h3 class="section-title">订单日志</h3>
              <div class="progress-list">
                <div
                  v-for="(item, idx) in order.progressList"
                  :key="idx"
                  class="progress-item"
                >
                  <div class="progress-dot" :class="{ done: idx < order.progressList.length - 1 }"></div>
                  <div class="progress-content">
                    <p class="progress-title">{{ item.title }}</p>
                    <p class="progress-time">{{ formatTime(item.createTime) }}</p>
                    <p v-if="item.description" class="progress-desc">{{ item.description }}</p>
                  </div>
                </div>
              </div>
            </div>

            <!-- 代练进度（boost订单显示） -->
            <div class="info-section" v-if="order.tradeType === 'boost' && progressData">
              <h3 class="section-title">代练进度</h3>
              <div class="progress-panel">
                <el-progress :percentage="progressData.progressPercent || 0" :color="progressColor(progressData.progressPercent)" />
                <p class="progress-note" v-if="progressData.progressNote">{{ progressData.progressNote }}</p>
                <p class="progress-time" v-if="progressData.sellerSubmitTime">更新时间：{{ formatTime(progressData.sellerSubmitTime) }}</p>
                <div class="progress-actions">
                  <el-button size="small" type="success" v-if="isBuyer && progressData.sellerSubmit && !progressData.buyerAck" @click="handleAckProgress">确认进度</el-button>
                  <el-tag v-if="progressData.buyerAck" type="success" size="small">买家已确认</el-tag>
                </div>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="action-bar">
              <template v-if="order.status === 'pending_pay'">
                <el-button type="primary" size="large" @click="handlePay">去付款</el-button>
                <el-button size="large" type="danger" @click="handleCancel">取消订单</el-button>
              </template>
              <template v-if="order.status === 'paid' && isSeller">
                <el-button type="success" size="large" @click="handleDeliver">发货</el-button>
              </template>
              <!-- 联系对方 -->
              <el-button size="large" @click="contactCounterpart" v-if="order && ['pending_pay','paid','submitted'].includes(order.status)">
                {{ isBuyer ? '联系卖家' : '联系买家' }}
              </el-button>
              <template v-if="(order.status === 'submitted') && isBuyer">
                <el-button type="primary" size="large" @click="handleConfirm">确认收货</el-button>
              </template>
              <template v-if="(order.status === 'paid' || order.status === 'submitted') && isBuyer && !hasDisputed">
                <el-button size="large" type="warning" @click="showDisputeDialog = true">申请仲裁</el-button>
              </template>
              <template v-if="(order.status === 'confirmed' || order.status === 'completed') && !hasReviewed">
                <el-button type="warning" size="large" @click="showReviewDialog = true">发表评价</el-button>
              </template>
              <template v-if="order.status === 'completed'">
                <el-button size="large" @click="router.push({ path: '/order/center' })">返回订单中心</el-button>
              </template>
              <template v-if="order.status === 'cancelled'">
                <el-button size="large" @click="router.push({ path: '/order/center' })">返回订单中心</el-button>
              </template>
            </div>

            <!-- 评价弹窗 -->
            <el-dialog v-model="showReviewDialog" title="发表评价" width="500px">
              <el-form :model="reviewForm" label-width="80px">
                <el-form-item label="综合评分">
                  <el-rate v-model="reviewForm.rating" allow-half />
                </el-form-item>
                <el-form-item label="评价内容">
                  <el-input v-model="reviewForm.content" type="textarea" :rows="4" placeholder="请分享您的交易体验.." />
                </el-form-item>
              </el-form>
              <template #footer>
                <el-button @click="showReviewDialog = false">取消</el-button>
                <el-button type="primary" @click="handleReview">提交评价</el-button>
              </template>
            </el-dialog>

            <!-- 仲裁弹窗 -->
            <el-dialog v-model="showDisputeDialog" title="申请仲裁" width="500px">
              <el-form :model="disputeForm" label-width="100px">
                <el-form-item label="仲裁原因">
                  <el-select v-model="disputeForm.reason" placeholder="请选择原因">
                    <el-option label="未收到货物/服务" value="未收到货物" />
                    <el-option label="货物与描述不符" value="货物与描述不符" />
                    <el-option label="卖家态度问题" value="卖家态度问题" />
                    <el-option label="其他问题" value="其他问题" />
                  </el-select>
                </el-form-item>
                <el-form-item label="详细描述">
                  <el-input v-model="disputeForm.description" type="textarea" :rows="4" placeholder="请详细描述您遇到的问题.." />
                </el-form-item>
                <el-form-item label="证据截图">
                  <el-input v-model="disputeForm.images" placeholder="可上传截图，多张用逗号分隔" />
                </el-form-item>
              </el-form>
              <template #footer>
                <el-button @click="showDisputeDialog = false">取消</el-button>
                <el-button type="primary" @click="handleDispute">提交仲裁</el-button>
              </template>
            </el-dialog>
          </template>

          <template v-else-if="!loading">
            <el-empty description="订单不存在" />
          </template>
        

            <!-- 评价展示 -->
            <div class="review-section" v-if="order && order.status === 'completed' && (order.buyerReviewContent || order.sellerReviewContent || order.myReviewContent)">
              <h3 class="section-title">交易评价</h3>
              <!-- 买家对卖家的评价 -->
              <div class="review-card" v-if="order.buyerReviewContent">
                <div class="review-card-header">
                  <span class="review-role buyer-role">买家</span>
                  <el-rate :model-value="order.buyerReviewRating" disabled size="small" style="display:inline-flex" />
                  <span class="review-time">{{ formatTime(order.buyerReviewTime) }}</span>
                </div>
                <div class="review-card-body">{{ order.buyerReviewContent }}</div>
              </div>
              <!-- 卖家对买家的评价 -->
              <div class="review-card" v-if="order.sellerReviewContent">
                <div class="review-card-header">
                  <span class="review-role seller-role">卖家</span>
                  <el-rate :model-value="order.sellerReviewRating" disabled size="small" style="display:inline-flex" />
                  <span class="review-time">{{ formatTime(order.sellerReviewTime) }}</span>
                </div>
                <div class="review-card-body">{{ order.sellerReviewContent }}</div>
              </div>
              <!-- 我发出的评价 -->
              <div class="review-card my-review" v-if="order.myReviewContent">
                <div class="review-card-header">
                  <span class="review-role my-role">我的评价</span>
                  <el-rate :model-value="order.myReviewRating" disabled size="small" style="display:inline-flex" />
                </div>
                <div class="review-card-body">{{ order.myReviewContent }}</div>
              </div>
            </div>

</el-card>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Right, Picture, Warning, Clock, Van, CircleCheckFilled } from '@element-plus/icons-vue'
import { getOrderDetail, payOrder, confirmOrder, cancelOrder, submitDelivery, submitReview, submitDispute } from '@/api/order'
import { getOrCreateSessionByOrder } from '@/api/im'
import { getOrderProgress, ackOrderProgress } from '@/api/progress'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const order = ref(null)
const progressData = ref(null)
const isBuyer = ref(false)
const isSeller = ref(false)
const hasReviewed = ref(false)
const hasDisputed = ref(false)
const showReviewDialog = ref(false)
const showDisputeDialog = ref(false)

const reviewForm = reactive({ rating: 5, content: '' })
const disputeForm = reactive({ reason: '', description: '', images: '' })

const statusMap = {
  pending_pay: '待付款',
  paid: '已付款',
  submitted: '待收货',
  confirmed: '已确认',
  completed: '已完成',
  cancelled: '已取消',
  disputed: '仲裁中'
}

const statusDescMap = {
  pending_pay: '请尽快完成支付，订单将在24小时后自动取消',
  paid: '卖家已收到货款，请等待发货',
  submitted: '卖家已发货，请确认收到商品/服务后确认收货',
  confirmed: '交易已确认，请对卖家的服务进行评价',
  completed: '交易已完成，感谢您的信任',
  cancelled: '订单已取消',
  disputed: '平台正在处理中，请耐心等待'
}

const typeMap = {
  goods: '游戏道具',
  boost: '代练服务',
  accompany: '陪玩服务',
  escort: '护送服务'
}

const getStatusText = (s) => statusMap[s] || s
const getStatusDesc = (s) => statusDescMap[s] || ''

const progressColor = (pct) => {
  if (!pct) return '#409eff'
  if (pct >= 100) return '#67c23a'
  if (pct >= 50) return '#e6a23c'
  return '#409eff'
}

const formatTime = (time) => {
  if (!time) return '—'
  const d = new Date(time)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const loadOrder = async () => {
  loading.value = true
  try {
    const id = route.params.id
    const res = await getOrderDetail(id)
    if (res.data) {
      order.value = res.data
      isBuyer.value = res.data.buyerId === userStore.userId
      isSeller.value = res.data.sellerId === userStore.userId
      hasDisputed.value = !!(res.data.disputeStatus === 1)
      hasReviewed.value = !!(res.data.hasReviewed)
      // Load progress for boost orders
      if (res.data.tradeType === 'boost') {
        try {
          const pr = await getOrderProgress(id)
          if (pr.data) progressData.value = pr.data
        } catch(e) {}
      }
    } else {
      order.value = { ...mockOrder, id }
    }
  } catch (e) {
    console.error('加载订单失败:', e)
    order.value = { ...mockOrder, id: route.params.id }
  } finally {
    loading.value = false
  }
}

const handlePay = async () => {
  try {
    await ElMessageBox.confirm('确认使用钱包余额支付该订单？', '提示', {
      confirmButtonText: '确认支付',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await payOrder(order.value.id, 'wallet')
    ElMessage.success('支付成功')
    loadOrder()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('支付失败')
    }
  }
}

const handleConfirm = async () => {
  try {
    await ElMessageBox.confirm('确认收到商品/服务完成？', '确认收货', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'success'
    })
    await confirmOrder(order.value.id)
    ElMessage.success('确认收货成功')
    loadOrder()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('确认收货失败')
    }
  }
}

const contactCounterpart = async () => {
  try {
    const res = await getOrCreateSessionByOrder(order.value.id)
    if (res.code === 200 && res.data) {
      router.push({ path: '/im' })
    } else {
      ElMessage.error('无法创建会话')
    }
  } catch (e) {
    ElMessage.error('无法创建会话')
  }
}

const handleAckProgress = async () => {
  try {
    await ElMessageBox.confirm('确认卖家当前代练进度？确认后金额将进入可提现状态。', '确认进度', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    })
    await ackOrderProgress(order.value.id, 1)
    ElMessage.success('进度已确认')
    const pr = await getOrderProgress(order.value.id)
    progressData.value = pr.data
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('确认失败')
  }
}

const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确定取消该订单？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cancelOrder(order.value.id)
    ElMessage.success('订单已取消')
    loadOrder()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('取消失败')
    }
  }
}

const handleDeliver = () => {
  router.push(`/order/deliver/${order.value.id}`)
}

const handleReview = async () => {
  try {
    await submitReview(order.value.id, isBuyer.value ? 1 : 2, reviewForm.rating, reviewForm.content)
    ElMessage.success('评价已提交')
    showReviewDialog.value = false
    hasReviewed.value = true
    loadOrder()
  } catch (e) {
    ElMessage.error('评价提交失败')
  }
}

const handleDispute = async () => {
  try {
    await submitDispute(order.value.id, disputeForm.reason, disputeForm.description, disputeForm.images)
    ElMessage.success('仲裁申请已提交，平台将尽快处理')
    showDisputeDialog.value = false
    hasDisputed.value = true
  } catch (e) {
    ElMessage.error('仲裁申请失败')
  }
}

const mockOrder = {
  id: 1,
  productTitle: '三角洲行动 哈夫币 100万',
  productType: 'game_currency',
  gameName: '三角洲行动',
  gameZone: '烽火区',
  coverImage: '',
  amount: 80.00,
  quantity: 1,
  serviceFee: 4.00,
  totalAmount: 84.00,
  status: 'pending_pay',
  createTime: '2026-04-22T10:30:00',
  buyerNickname: 'user_138****0001',
  buyerId: 1,
  sellerNickname: '专业搬砖商',
  sellerId: 2,
  progressList: [
    { title: '创建订单', createTime: '2026-04-22T10:30:00', description: '' },
    { title: '完成付款', createTime: null, description: '等待买家付款' }
  ]
}

onMounted(() => {
  document.title = '订单详情 - 龙虾道具交易平台'
  loadOrder()
})
</script>

<style scoped>
/* ========== 主布局 ========== */
.detail-container {
  min-height: 100vh;
  background: #f0f2f5;
}
.top-header {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-inner {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 24px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.logo {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  text-decoration: none;
}
.detail-main {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px;
}
.content { min-width: 0; }

/* ========== 状态横条 ========== */
.status-banner {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  border-radius: 10px;
  margin-bottom: 20px;
}
.status-banner.pending_pay { background: linear-gradient(135deg, #fdf6ec, #fef0e6); }
.status-banner.paid { background: linear-gradient(135deg, #e6f4ff, #f0f9ff); }
.status-banner.submitted { background: linear-gradient(135deg, #fff7e6, #fffbf0); }
.status-banner.confirmed { background: linear-gradient(135deg, #f0f9eb, #f6ffed); }
.status-banner.completed { background: linear-gradient(135deg, #e6fffb, #f6ffed); }
.status-banner.cancelled { background: linear-gradient(135deg, #f5f5f5, #fafafa); }
.status-banner.arbitration, .status-banner.disputed { background: linear-gradient(135deg, #fff0f0, #fff5f5); }
.status-icon { color: #666; }
.status-text h3 { margin: 0 0 4px; font-size: 16px; color: #333; }
.status-text p { margin: 0; font-size: 13px; color: #666; }

/* ========== 信息区块 ========== */
.info-section { margin-bottom: 20px; }
.section-title { font-size: 14px; font-weight: 600; color: #333; margin: 0 0 12px; }
.info-grid { display: flex; flex-direction: column; gap: 8px; }
.info-row { display: flex; align-items: center; gap: 16px; }
.info-label { width: 80px; font-size: 13px; color: #999; flex-shrink: 0; }
.info-value { font-size: 14px; color: #333; }

/* ========== 交易安全保障险 ========== */
.security-insurance-section .insurance-banner {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  background: linear-gradient(135deg, #f0f7ff, #f5f0ff);
  border: 1px solid #d0e3ff;
  border-radius: 10px;
}
.insurance-icon { font-size: 28px; line-height: 1; flex-shrink: 0; }
.insurance-text { flex: 1; }
.insurance-title { font-size: 14px; font-weight: 600; color: #333; margin-bottom: 4px; }
.insurance-desc { font-size: 12px; color: #666; line-height: 1.5; margin: 0; }

/* ========== 商品卡片 ========== */
.product-block {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f9f9f9;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s;
}
.product-block:hover { background: #f0f0f0; }
.product-image {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  background: #eee;
  display: flex;
  align-items: center;
  justify-content: center;
}
.product-image img { width: 100%; height: 100%; object-fit: cover; }
.image-placeholder { color: #ccc; }
.product-info { flex: 1; min-width: 0; }
.product-title { margin: 0 0 4px; font-size: 14px; color: #333; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.product-price { margin: 0; font-size: 15px; font-weight: 600; color: #667eea; }

/* ========== 交易双方 ========== */
.party-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: #f9f9f9;
  border-radius: 10px;
}
.party-item { text-align: center; }
.party-role { margin: 0 0 4px; font-size: 12px; color: #999; }
.party-name { margin: 0; font-size: 14px; color: #333; }
.party-arrow { color: #ccc; }

/* ========== 金额 ========== */
.amount-section { }
.amount-block { background: #f9f9f9; border-radius: 10px; padding: 16px; }
.amount-row { display: flex; justify-content: space-between; padding: 6px 0; font-size: 14px; color: #666; }
.amount-row.total { border-top: 1px solid #eee; margin-top: 8px; padding-top: 12px; font-size: 15px; font-weight: 600; color: #333; }
.discount-value { color: #67c23a; font-weight: 600; }
.total-price { font-size: 18px; font-weight: 700; color: #667eea; }

/* ========== 进度 ========== */
.progress-panel { padding: 8px 0; }
.progress-note { margin: 8px 0 4px; font-size: 13px; color: #666; }
.progress-time { margin: 0; font-size: 12px; color: #aaa; }
.progress-actions { margin-top: 8px; }

/* ========== 操作日志 ========== */
.progress-list { display: flex; flex-direction: column; gap: 0; }
.progress-item { display: flex; gap: 12px; position: relative; padding-bottom: 16px; }
.progress-item:last-child { padding-bottom: 0; }
.progress-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #ddd;
  flex-shrink: 0;
  margin-top: 4px;
}
.progress-dot.done { background: #67c23a; }
.progress-content { flex: 1; }
.progress-title { margin: 0 0 2px; font-size: 13px; color: #333; }
.progress-time { margin: 0; font-size: 12px; color: #aaa; }
.progress-desc { margin: 4px 0 0; font-size: 12px; color: #999; }

/* ========== 操作栏 ========== */
.action-bar {
  display: flex;
  gap: 12px;
  padding: 20px 0 0;
  border-top: 1px solid #f0f0f0;
  flex-wrap: wrap;
}


/* ========== 评价展示 ========== */
.review-section {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.review-section .section-title {
  margin: 0 0 16px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.review-card {
  background: #f9f9f9;
  border-radius: 8px;
  padding: 14px 16px;
  margin-bottom: 12px;
}

.review-card.my-review {
  background: #f0f7ff;
  border-left: 3px solid #3b82f6;
}

.review-card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.review-role {
  font-size: 12px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
}

.buyer-role {
  background: #e8f0ff;
  color: #3b82f6;
}

.seller-role {
  background: #f0fdf4;
  color: #10b981;
}

.my-role {
  background: #e0e7ff;
  color: #6366f1;
}

.review-time {
  font-size: 12px;
  color: #bbb;
  margin-left: auto;
}

.review-card-body {
  font-size: 14px;
  color: #555;
  line-height: 1.6;
}

</style>