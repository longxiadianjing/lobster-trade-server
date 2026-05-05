<template>
  <div class="progress-container">
    <!-- 顶部导航 -->
    <div class="top-header">
      <div class="header-inner">
        <router-link to="/home" class="logo">🦞 龙虾道具交易平台</router-link>
        <div class="header-actions">
          <el-button @click="router.push({ path: `/order/detail/${orderId}` })">返回订单详情</el-button>
        </div>
      </div>
    </div>

    <div class="progress-main">
      <main class="content">
        <el-card class="progress-card" shadow="never" v-loading="loading">
          <template #header>
            <div class="card-header">
              <span>代练进度追踪</span>
              <el-tag v-if="progress" :type="progressTagType">{{ progressTagText }}</el-tag>
            </div>
          </template>

          <!-- 订单基础信息 -->
          <div v-if="order" class="order-info">
            <div class="order-info-row">
              <span class="label">商品：</span>
              <span class="value">{{ order.productTitle }}</span>
            </div>
            <div class="order-info-row">
              <span class="label">订单号：</span>
              <span class="value">{{ order.orderNo }}</span>
            </div>
            <div class="order-info-row">
              <span class="label">金额：</span>
              <span class="value price">¥{{ order.orderAmount }}</span>
            </div>
          </div>

          <!-- 进度步骤条 -->
          <div class="progress-steps">
            <div class="step" :class="{ done: step >= 1, active: step === 1 }">
              <div class="step-icon">1</div>
              <div class="step-label">已下单</div>
            </div>
            <div class="step-line" :class="{ done: step >= 2 }"></div>
            <div class="step" :class="{ done: step >= 2, active: step === 2 }">
              <div class="step-icon">2</div>
              <div class="step-label">进行中</div>
            </div>
            <div class="step-line" :class="{ done: step >= 3 }"></div>
            <div class="step" :class="{ done: step >= 3, active: step === 3 }">
              <div class="step-icon">3</div>
              <div class="step-label">待验收</div>
            </div>
            <div class="step-line" :class="{ done: step >= 4 }"></div>
            <div class="step" :class="{ done: step >= 4, active: step === 4 }">
              <div class="step-icon">4</div>
              <div class="step-label">已完成</div>
            </div>
          </div>

          <!-- 进度百分比 -->
          <div v-if="progress" class="progress-percent">
            <div class="percent-bar">
              <div class="percent-fill" :style="{ width: progress.progressPercent + '%' }"></div>
            </div>
            <span class="percent-text">{{ progress.progressPercent }}%</span>
          </div>

          <!-- 进度记录时间线 -->
          <div v-if="progress" class="timeline">
            <div class="timeline-item">
              <div class="timeline-dot"></div>
              <div class="timeline-content">
                <div class="timeline-title">卖家提交进度</div>
                <div v-if="progress.sellerSubmitTime" class="timeline-time">{{ formatTime(progress.sellerSubmitTime) }}</div>
                <div v-if="progress.progressNote" class="timeline-note">{{ progress.progressNote }}</div>
                <div v-if="screenshots.length > 0" class="screenshots">
                  <el-image
                    v-for="(img, i) in screenshots"
                    :key="i"
                    :src="img"
                    fit="cover"
                    class="screenshot-thumb"
                    :preview-src-list="screenshots"
                    preview-teleported
                  />
                </div>
              </div>
            </div>

            <div v-if="progress.buyerAck === 1" class="timeline-item success">
              <div class="timeline-dot done"></div>
              <div class="timeline-content">
                <div class="timeline-title">✅ 买家已验收</div>
                <div v-if="progress.buyerAckTime" class="timeline-time">{{ formatTime(progress.buyerAckTime) }}</div>
              </div>
            </div>
          </div>

          <el-empty v-else description="暂无进度记录" />

          <!-- 卖家操作区 -->
          <div v-if="isSeller && progress && order && ['paid','in_progress'].includes(order.status)" class="action-area">
            <el-divider content-position="left">更新进度</el-divider>
            <el-form label-width="100px">
              <el-form-item label="完成百分比">
                <el-slider v-model="updateForm.percent" :min="0" :max="100" show-input @change="updateForm.percent = $event" />
              </el-form-item>
              <el-form-item label="进度说明">
                <el-input v-model="updateForm.note" type="textarea" :rows="3" placeholder="请描述当前进度情况..." />
              </el-form-item>
              <el-form-item label="截图凭证">
                <el-upload
                  ref="uploadRef"
                  action=""
                  :auto-upload="false"
                  :limit="9"
                  list-type="picture-card"
                  :on-change="handleScreenshotChange"
                  :file-list="fileList"
                >
                  <el-icon><Plus /></el-icon>
                </el-upload>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="submitting" @click="handleSubmitProgress">
                  提交进度
                </el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 买家验收区 -->
          <div v-if="isBuyer && progress && progress.progressPercent === 100 && progress.buyerAck !== 1" class="action-area">
            <el-divider content-position="left">验收确认</el-divider>
            <el-alert type="success" :closable="false" style="margin-bottom: 16px;">
              卖家已完成代练服务，请您验收确认。如有问题可先联系客服或发起申诉。
            </el-alert>
            <div class="buyer-actions">
              <el-button type="success" :loading="acking" @click="handleAck(true)">确认验收</el-button>
              <el-button type="warning" @click="router.push({ path: `/order/detail/${orderId}` })">发起申诉</el-button>
            </div>
          </div>

          <!-- 订单已完成提示 -->
          <div v-if="progress && progress.buyerAck === 1" class="action-area">
            <el-alert type="success" :closable="false">
              订单已完成验收，感谢您使用龙虾道具交易平台！
            </el-alert>
          </div>
        </el-card>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getOrderProgress, updateOrderProgress, ackOrderProgress } from '@/api/progress'
import { getOrderDetail } from '@/api/order'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const orderId = computed(() => route.params.id)
const isSeller = computed(() => userStore.isLoggedIn && userStore.userId === order.value?.sellerId)
const isBuyer = computed(() => userStore.isLoggedIn && userStore.userId === order.value?.buyerId)

const loading = ref(true)
const submitting = ref(false)
const acking = ref(false)
const progress = ref(null)
const order = ref(null)
const fileList = ref([])
const uploadRef = ref(null)

const updateForm = ref({
  percent: 0,
  note: '',
  screenshots: []
})

const screenshots = computed(() => {
  if (!progress.value?.screenshots) return []
  try {
    return JSON.parse(progress.value.screenshots)
  } catch {
    return []
  }
})

const step = computed(() => {
  if (!progress.value) return 1
  const p = progress.value.progressPercent
  if (progress.value.buyerAck === 1) return 4
  if (p === 0) return 1
  if (p < 100) return 2
  return 3
})

const progressTagType = computed(() => {
  if (!progress.value) return 'info'
  if (progress.value.buyerAck === 1) return 'success'
  if (progress.value.progressPercent === 100) return 'warning'
  return 'primary'
})

const progressTagText = computed(() => {
  if (!progress.value) return '暂无进度'
  if (progress.value.buyerAck === 1) return '✅ 已完成验收'
  if (progress.value.progressPercent === 100) return '⏳ 待验收'
  return `进行中 ${progress.value.progressPercent}%`
})

const handleScreenshotChange = (file) => {
  // Preview locally, actual upload happens on submit
  fileList.value.push(file)
}

const handleSubmitProgress = async () => {
  if (updateForm.value.percent < (progress.value?.progressPercent || 0)) {
    ElMessage.warning('进度百分比不能倒退')
    return
  }
  submitting.value = true
  try {
    // Upload screenshots first if any new files
    const uploadedUrls = [...screenshots.value]
    for (const file of fileList.value) {
      // Use existing URL if already uploaded
      if (typeof file === 'string') {
        uploadedUrls.push(file)
      }
    }

    await updateOrderProgress(orderId.value, {
      percent: updateForm.value.percent,
      note: updateForm.value.note,
      screenshots: JSON.stringify(uploadedUrls)
    })
    ElMessage.success('进度已更新')
    await loadProgress()
    fileList.value = []
    updateForm.value.note = ''
  } catch (e) {
    ElMessage.error('更新失败')
  } finally {
    submitting.value = false
  }
}

const handleAck = async (ack) => {
  acking.value = true
  try {
    await ackOrderProgress(orderId.value, ack)
    ElMessage.success('验收确认成功')
    await loadProgress()
  } catch (e) {
    ElMessage.error('验收失败')
  } finally {
    acking.value = false
  }
}

const loadProgress = async () => {
  try {
    const res = await getOrderProgress(orderId.value)
    progress.value = res.data
    updateForm.value.percent = res.data?.progressPercent || 0
  } catch (e) {
    // Progress record might not exist yet
    progress.value = null
  }
}

const loadOrder = async () => {
  try {
    const res = await getOrderDetail(orderId.value)
    order.value = res.data
  } catch (e) {
    ElMessage.error('加载订单失败')
  }
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

onMounted(async () => {
  loading.value = true
  if (userStore.isLoggedIn && !userStore.userId) {
    await userStore.fetchUserInfo().catch(() => {})
  }
  await Promise.all([loadOrder(), loadProgress()])
  loading.value = false
  document.title = '代练进度追踪 - 龙虾道具交易平台'
})
</script>

<style scoped>
.progress-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.top-header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.header-inner {
  max-width: 900px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 56px;
  padding: 0 20px;
}

.logo {
  font-size: 18px;
  font-weight: 700;
  text-decoration: none;
  color: #333;
}

.progress-main {
  max-width: 900px;
  margin: 20px auto;
  padding: 0 20px;
}

.order-info {
  background: #f8f8ff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 24px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.order-info-row {
  display: flex;
  gap: 8px;
  font-size: 14px;
}

.order-info-row .label {
  color: #888;
  flex-shrink: 0;
}

.order-info-row .value {
  color: #333;
  font-weight: 500;
}

.order-info-row .price {
  color: #f56c6c;
  font-weight: 700;
}

/* 步骤条 */
.progress-steps {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  padding: 0 20px;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.step-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #ddd;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
  transition: all 0.3s;
}

.step.done .step-icon {
  background: #67c23a;
}

.step.active .step-icon {
  background: #8b6fd4;
  box-shadow: 0 0 0 4px rgba(139, 111, 212, 0.2);
}

.step-label {
  font-size: 12px;
  color: #888;
}

.step.done .step-label,
.step.active .step-label {
  color: #333;
  font-weight: 600;
}

.step-line {
  flex: 1;
  height: 2px;
  background: #ddd;
  margin: 0 8px;
  margin-bottom: 20px;
  transition: background 0.3s;
}

.step-line.done {
  background: #67c23a;
}

/* 进度百分比 */
.progress-percent {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.percent-bar {
  flex: 1;
  height: 8px;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
}

.percent-fill {
  height: 100%;
  background: linear-gradient(90deg, #764ba2, #8b6fd4);
  border-radius: 4px;
  transition: width 0.5s ease;
}

.percent-text {
  font-size: 16px;
  font-weight: 700;
  color: #764ba2;
  min-width: 50px;
}

/* 时间线 */
.timeline {
  margin: 24px 0;
  padding-left: 20px;
  border-left: 2px solid #e8e8e8;
}

.timeline-item {
  position: relative;
  padding-bottom: 20px;
}

.timeline-dot {
  position: absolute;
  left: -26px;
  top: 4px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #8b6fd4;
  border: 2px solid #fff;
}

.timeline-dot.done {
  background: #67c23a;
}

.timeline-content {
  background: #f8f8ff;
  border-radius: 8px;
  padding: 12px 16px;
}

.timeline-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.timeline-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.timeline-note {
  font-size: 13px;
  color: #555;
  margin-top: 6px;
  line-height: 1.6;
}

.screenshots {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.screenshot-thumb {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  cursor: pointer;
}

/* 操作区 */
.action-area {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}

.buyer-actions {
  display: flex;
  gap: 12px;
}
</style>