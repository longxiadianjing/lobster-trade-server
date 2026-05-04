<template>
  <div class="deliver-container">
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
    <div class="deliver-main">
      <main class="content">
        <el-card class="deliver-card" shadow="never" v-loading="loading">
          <template #header>
            <div class="card-header">
              <span class="card-title">代练服务 - 订单发货</span>
            </div>
          </template>

          <template v-if="order">
            <!-- 订单信息摘要 -->
            <div class="order-summary">
              <div class="summary-row">
                <span class="label">订单编号</span>
                <span class="value">{{ order.orderNo || order.id }}</span>
              </div>
              <div class="summary-row">
                <span class="label">商品名称</span>
                <span class="value">{{ order.productTitle }}</span>
              </div>
              <div class="summary-row">
                <span class="label">买家</span>
                <span class="value">{{ order.buyerNickname }}</span>
              </div>
              <div class="summary-row">
                <span class="label">订单金额</span>
                <span class="value price">¥{{ order.orderAmount || order.amount }}</span>
              </div>
              <div class="summary-row">
                <span class="label">当前状态</span>
                <span class="value">
                  <el-tag :type="statusTagType(order.status)">{{ statusLabel(order.status) }}</el-tag>
                </span>
              </div>
            </div>

            <el-divider />

            <!-- 代练进度面板（boost订单） -->
            <div class="progress-panel" v-if="order.tradeType === 'boost' || order.tradeType === 'goods'">
              <h4 class="section-title">代练进度</h4>

              <!-- 当前进度展示 -->
              <div class="current-progress" v-if="progressData">
                <el-progress :percentage="progressData.progressPercent || 0" :color="progressColor(progressData.progressPercent)" />
                <p class="progress-note" v-if="progressData.progressNote">{{ progressData.progressNote }}</p>
                <p class="progress-time" v-if="progressData.sellerSubmitTime">
                  更新时间：{{ formatTime(progressData.sellerSubmitTime) }}
                </p>
              </div>
              <div class="no-progress" v-else>
                <el-empty description="暂无进度记录，点击下方更新进度" :image-size="80" />
              </div>
            </div>

            <el-divider />

            <!-- 发货表单 -->
            <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" size="large">
              <!-- 代练服务（boost订单显示进度选项） -->
              <template v-if="order.tradeType === 'boost'">
                <el-form-item label="代练进度" prop="progressPercent">
                  <el-select v-model="form.progressPercent" placeholder="请选择当前进度" style="width: 300px;">
                    <el-option label="🆕 已开始（10%）" value="10" />
                    <el-option label="🔄 进行中（50%）" value="50" />
                    <el-option label="⏳ 即将完成（90%）" value="90" />
                    <el-option label="✅ 已完成，待确认（100%）" value="100" />
                  </el-select>
                  <span class="form-hint">选择后点击「更新进度」同步给买家</span>
                </el-form-item>

                <el-form-item label="当前段位" prop="currentRank">
                  <el-input v-model="form.currentRank" placeholder="如：钻石III、专家等" style="width: 200px;" />
                </el-form-item>

                <el-form-item label="目标段位" prop="targetRank">
                  <el-input v-model="form.targetRank" placeholder="如：大师、大地球等" style="width: 200px;" />
                </el-form-item>

                <el-form-item label="进度说明" prop="progressNote">
                  <el-input
                    v-model="form.progressNote"
                    type="textarea"
                    :rows="3"
                    placeholder="描述当前代练进度情况，如：已完成钻石到大师段位，当前在大师晋级赛"
                    maxlength="200"
                    show-word-limit
                    style="width: 480px;"
                  />
                </el-form-item>

                <el-form-item label="进度截图">
                  <div class="upload-area">
                    <el-upload
                      v-model:file-list="imageList"
                      action="#"
                      :auto-upload="false"
                      :limit="6"
                      list-type="picture-card"
                      :on-preview="handlePictureCardPreview"
                      :on-remove="handleRemove"
                    >
                      <el-icon><Plus /></el-icon>
                    </el-upload>
                    <p class="upload-tip">最多上传6张图片，建议截图游戏内完成画面</p>
                  </div>
                </el-form-item>

                <!-- 更新进度按钮 -->
                <el-form-item>
                  <el-button type="info" size="large" :loading="progressLoading" @click="handleUpdateProgress">
                    更新进度
                  </el-button>
                </el-form-item>

                <el-divider />
              </template>

              <!-- 交付说明 -->
              <el-form-item label="交付说明" prop="deliveryRemark">
                <el-input
                  v-model="form.deliveryRemark"
                  type="textarea"
                  :rows="4"
                  placeholder="请详细描述交付内容（如：已完成代练，账号XXX，等级已到指定段位）"
                  maxlength="300"
                  show-word-limit
                  style="width: 500px;"
                />
              </el-form-item>

              <!-- 交付截图 -->
              <el-form-item label="交付凭证">
                <div class="upload-area">
                  <el-upload
                    v-model:file-list="deliveryImageList"
                    action="#"
                    :auto-upload="false"
                    :limit="6"
                    list-type="picture-card"
                    :on-preview="handlePictureCardPreview"
                    :on-remove="handleRemove"
                  >
                    <el-icon><Plus /></el-icon>
                  </el-upload>
                  <p class="upload-tip">最多上传6张图片，建议截图游戏内完成画面</p>
                </div>
              </el-form-item>

              <!-- 确认信息 -->
              <el-form-item>
                <div class="confirm-tip">
                  <el-icon><Warning /></el-icon>
                  <span>确认提交后将通知买家验收，请确保交付内容与描述一致</span>
                </div>
              </el-form-item>

              <!-- 提交按钮 -->
              <el-form-item>
                <el-button
                  type="primary"
                  size="large"
                  :loading="submitLoading"
                  :disabled="!canSubmit"
                  @click="handleSubmit"
                >
                  确认发货
                </el-button>
                <el-button size="large" @click="router.push({ path: '/order/detail/' + orderId })">取消</el-button>
              </el-form-item>
            </el-form>
          </template>

          <template v-else-if="!loading">
            <el-empty description="订单不存在" />
          </template>
        </el-card>
      </main>
    </div>

    <!-- 图片预览 -->
    <el-dialog v-model="previewVisible">
      <img :src="previewUrl" alt="预览" style="width: 100%;" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Warning } from '@element-plus/icons-vue'
import { getOrderDetail, submitDelivery } from '@/api/order'
import { getOrderProgress, updateOrderProgress } from '@/api/progress'

const router = useRouter()
const route = useRoute()
const orderId = route.params.id

const loading = ref(false)
const submitLoading = ref(false)
const progressLoading = ref(false)
const order = ref(null)
const progressData = ref(null)
const formRef = ref(null)
const imageList = ref([])
const deliveryImageList = ref([])
const previewVisible = ref(false)
const previewUrl = ref('')

const form = reactive({
  progressPercent: '',
  currentRank: '',
  targetRank: '',
  progressNote: '',
  deliveryRemark: ''
})

const rules = {
  deliveryRemark: [{ required: true, message: '请输入交付说明', trigger: 'blur' }]
}

// 可发货状态：paid 或 in_progress
const canSubmit = computed(() => {
  return order.value && ('paid' === order.value.status || 'in_progress' === order.value.status)
})

const statusLabel = (s) => ({
  paid: '已付款', in_progress: '进行中', submitted: '已提交', confirmed: '已确认', completed: '已完成'
})[s] || s

const statusTagType = (s) => ({
  paid: '', in_progress: 'primary', submitted: 'info', confirmed: 'success', completed: 'success'
})[s] || ''

const progressColor = (pct) => {
  if (pct >= 100) return '#67c23a'
  if (pct >= 50) return '#e6a23c'
  return '#409eff'
}

const handlePictureCardPreview = (file) => {
  previewUrl.value = file.url
  previewVisible.value = true
}

const handleRemove = (file, fileList) => {
  imageList.value = fileList
  deliveryImageList.value = fileList
}

const handleUpdateProgress = async () => {
  if (!form.progressPercent) {
    ElMessage.warning('请选择代练进度')
    return
  }
  progressLoading.value = true
  try {
    const screenshots = imageList.value.map(f => f.url || '').filter(Boolean).join(',')
    await updateOrderProgress(orderId, {
      percent: parseInt(form.progressPercent),
      note: form.progressNote,
      screenshots
    })
    ElMessage.success('进度已更新')
    // 刷新进度
    const r = await getOrderProgress(orderId)
    progressData.value = r.data
    // 如果是从paid进入进行中，刷新订单状态
    const res = await getOrderDetail(orderId)
    if (res.data) order.value = res.data
  } catch (e) {
    ElMessage.error(e.message || '更新进度失败')
  } finally {
    progressLoading.value = false
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      await ElMessageBox.confirm(
        '确认提交发货信息？买家将收到通知。',
        '确认发货',
        { confirmButtonText: '确认提交', cancelButtonText: '取消', type: 'info' }
      )
    } catch {
      return
    }

    submitLoading.value = true
    try {
      const deliveryImages = deliveryImageList.value.map(f => f.url || '').filter(Boolean).join(',')
      await submitDelivery(orderId, deliveryImages, form.deliveryRemark)
      ElMessage.success('发货成功！等待买家确认')
      router.push({ path: '/order/center' })
    } catch (e) {
      ElMessage.error(e.message || '发货失败，请重试')
    } finally {
      submitLoading.value = false
    }
  })
}

const loadOrder = async () => {
  loading.value = true
  try {
    const res = await getOrderDetail(orderId)
    if (res.data) {
      order.value = res.data
    } else {
      order.value = {
        id: orderId,
        orderNo: 'ORD' + orderId,
        productTitle: '代练服务',
        tradeType: 'boost',
        buyerNickname: 'user_138****0001',
        amount: 200.00,
        status: 'paid'
      }
    }
    // 加载进度
    try {
      const pr = await getOrderProgress(orderId)
      progressData.value = pr.data
    } catch (e) { /* no progress yet */ }
  } catch (e) {
    console.error('加载订单失败:', e)
  } finally {
    loading.value = false
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const d = new Date(time)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

onMounted(() => {
  loadOrder()

  document.title = '发货 - 龙虾道具交易平台'
})
</script>

<style scoped>
.deliver-container {
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
.deliver-main {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px;
}
.content { min-width: 0; }
.deliver-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}
.deliver-card :deep(.el-card__header) { padding: 16px 20px; border-bottom: 1px solid #f0f0f0; }
.deliver-card :deep(.el-card__body) { padding: 24px 20px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-size: 15px; font-weight: 600; color: #333; }
.section-title { font-size: 14px; font-weight: 600; color: #333; margin: 0 0 12px; }

.order-summary {
  background: #f9fafb;
  border-radius: 10px;
  padding: 16px 20px;
}
.summary-row { display: flex; align-items: center; gap: 16px; padding: 8px 0; }
.summary-row .label { width: 80px; font-size: 13px; color: #999; flex-shrink: 0; }
.summary-row .value { font-size: 14px; color: #333; }
.summary-row .price { font-size: 18px; font-weight: 700; color: #667eea; }

.progress-panel { margin-bottom: 16px; }
.current-progress { padding: 12px 0; }
.progress-note { margin: 8px 0 4px; font-size: 13px; color: #666; }
.progress-time { margin: 4px 0; font-size: 12px; color: #aaa; }
.no-progress { padding: 20px 0; }

.form-hint { margin-left: 12px; font-size: 12px; color: #999; }

.upload-area { display: flex; flex-direction: column; gap: 8px; }
.upload-tip { margin: 0; font-size: 12px; color: #999; }
.confirm-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f0f9eb;
  border: 1px solid #67c23a30;
  border-radius: 8px;
  padding: 10px 14px;
  font-size: 13px;
  color: #67c23a;
}

@media (max-width: 768px) {
  .deliver-main { padding: 16px; }
}
</style>