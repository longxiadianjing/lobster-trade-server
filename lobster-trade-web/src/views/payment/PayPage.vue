<template>
  <div class="pay-page">
    <div class="pay-container">
      <!-- 顶部 -->
      <div class="pay-header">
        <router-link to="/" class="logo">🦞 龙虾道具交易平台</router-link>
        <span class="pay-title">订单支付</span>
      </div>

      <!-- 支付单卡片 -->
      <el-card class="pay-card" v-loading="loading">
        <template v-if="paymentInfo">
          <!-- 状态区 -->
          <div class="pay-status-banner" :class="statusClass">
            <div class="status-icon-area">
              <el-icon v-if="payStatus === 1" :size="48" color="#67c23a"><CircleCheckFilled /></el-icon>
              <el-icon v-else-if="payStatus === 2" :size="48" color="#999"><WarningFilled /></el-icon>
              <el-icon v-else :size="48" color="#faad14"><Clock /></el-icon>
            </div>
            <div class="status-text-area">
              <h3>{{ statusText }}</h3>
              <p v-if="payStatus === 0">请在 {{ expireText }} 前完成支付</p>
              <p v-else-if="payStatus === 1">支付成功，页面跳转中...</p>
              <p v-else-if="payStatus === 2">支付单已过期，请重新发起支付</p>
            </div>
          </div>

          <!-- 支付信息 -->
          <div class="pay-info-grid">
            <div class="info-row">
              <span class="info-label">支付单号</span>
              <span class="info-value">{{ paymentInfo.paymentNo }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">支付金额</span>
              <span class="info-value amount-highlight">¥{{ paymentInfo.amount }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">支付渠道</span>
              <span class="info-value">{{ channelText }}</span>
            </div>
            <div class="info-row" v-if="orderId">
              <span class="info-label">关联订单</span>
              <span class="info-value order-link" @click="router.push(`/order/detail/${orderId}`)">查看订单 {{ orderId }}</span>
            </div>
          </div>

          <!-- 二维码展示区（待支付状态） -->
          <div class="qr-section" v-if="payStatus === 0">
            <div class="qr-tip">请使用{{ channelText }}扫描下方二维码完成支付</div>
            <div class="qr-box">
              <div class="qr-code">
                <!-- 模拟二维码：用支付单号生成方块图案 -->
                <div class="mock-qr">
                  <div class="qr-grid">
                    <canvas ref="qrCanvas" width="200" height="200"></canvas>
                  </div>
                  <div class="qr-fallback">
                    <div class="qr-blocks">
                      <div v-for="i in 16" :key="i" class="qr-block" :class="{ active: isBlockActive(i) }"></div>
                    </div>
                  </div>
                </div>
              </div>
              <p class="qr-note">支付单号作为模拟二维码内容</p>
            </div>

            <!-- 模拟倒计时 -->
            <div class="countdown-bar">
              <el-icon><Clock /></el-icon>
              <span>支付剩余时间：<strong>{{ countdownText }}</strong></span>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="pay-actions">
            <template v-if="payStatus === 0">
              <el-button type="primary" size="large" class="pay-btn" :loading="mockLoading" @click="handleMockPay">
                我已支付
              </el-button>
              <el-button size="large" class="cancel-btn" @click="handleCancelPay">
                取消支付
              </el-button>
            </template>
            <template v-else-if="payStatus === 1">
              <el-button type="primary" size="large" class="pay-btn" @click="goToTarget">
                {{ paymentType === 'recharge' ? '前往钱包' : '查看订单' }}
              </el-button>
            </template>
            <template v-else-if="payStatus === 2">
              <el-button type="primary" size="large" class="pay-btn" @click="handleRetry">
                重新发起支付
              </el-button>
            </template>
          </div>
        </template>

        <template v-else-if="!loading">
          <el-empty description="支付单不存在" />
        </template>
      </el-card>
    </div>
  </div>
</template>

<script setup>
  document.title = '订单支付 - 龙虾道具交易平台'

import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Clock, CircleCheckFilled, WarningFilled } from '@element-plus/icons-vue'
import { createRechargePayment, createOrderPayment, getPaymentStatus, mockPaymentCallback } from '@/api/payment'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const mockLoading = ref(false)
const paymentInfo = ref(null)
const payStatus = ref(0) // 0=待支付 1=已支付 2=已过期
const paymentType = ref('recharge') // recharge / order
const orderId = ref(null)
const expireTime = ref(null)
const countdown = ref(0)
let countdownTimer = null
const qrCanvas = ref(null)

const statusText = computed(() => {
  if (payStatus.value === 1) return '支付成功'
  if (payStatus.value === 2) return '支付单已过期'
  return '等待支付'
})

const statusClass = computed(() => {
  if (payStatus.value === 1) return 'status-success'
  if (payStatus.value === 2) return 'status-expired'
  return 'status-pending'
})

const expireText = computed(() => {
  if (!expireTime.value) return ''
  const d = new Date(expireTime.value)
  return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}:${d.getSeconds().toString().padStart(2, '0')}`
})

const countdownText = computed(() => {
  const m = Math.floor(countdown.value / 60)
  const s = countdown.value % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
})

const channelText = computed(() => {
  const map = { alipay: '支付宝', wechat: '微信支付', bankcard: '银行卡', wallet: '钱包余额' }
  return map[paymentInfo.value?.channel] || paymentInfo.value?.channel || '—'
})

// 二维码方块模拟
const blockPattern = [
  1,0,1,1,0,1,0,1,1,0,1,1,1,0,0,1,
  1,1,0,0,1,1,1,0,0,1,0,1,0,1,1,0,
  0,0,1,1,0,0,1,1,1,0,1,0,1,0,0,1,
  1,1,1,0,1,1,0,0,0,1,1,1,0,1,1,0,
  0,1,0,1,1,0,1,1,0,0,1,0,1,0,1,1,
  1,0,1,0,0,1,1,0,1,1,0,1,1,0,0,1,
  0,1,1,1,0,0,0,1,1,0,1,0,0,1,1,0,
  1,1,0,0,1,1,1,0,0,1,0,1,0,1,1,0,
  1,0,1,1,0,1,0,1,1,0,1,1,1,0,0,1,
  0,1,0,1,1,0,1,1,0,0,1,0,1,0,1,1,
  1,1,0,0,1,1,1,0,0,1,0,1,0,1,1,0,
  0,0,1,1,0,0,1,1,1,0,1,0,1,0,0,1,
  1,1,1,0,1,1,0,0,0,1,1,1,0,1,1,0,
  0,1,0,1,1,0,1,1,0,0,1,0,1,0,1,1,
  0,1,1,1,0,0,0,1,1,0,1,0,0,1,1,0,
  1,0,1,0,0,1,1,0,1,1,0,1,1,0,0,1
]
const isBlockActive = (i) => {
  const paymentNo = paymentInfo.value?.paymentNo || ''
  const idx = (i * 7 + paymentNo.length) % 16
  return blockPattern[idx] === 1
}

let pollTimer = null

const loadPayment = async () => {
  loading.value = true
  try {
    const no = route.query.no
    const type = route.query.type || 'recharge'
    paymentType.value = type
    if (type === 'order') {
      orderId.value = route.query.orderId || null
    }
    if (!no) {
      ElMessage.error('支付单号无效')
      return
    }
    const res = await getPaymentStatus(no)
    if (res.data) {
      paymentInfo.value = res.data
      payStatus.value = res.data.status
      if (res.data.expireTime) {
        expireTime.value = res.data.expireTime
        startCountdown()
      }
    }
  } catch (e) {
    console.error('加载支付单失败:', e)
  } finally {
    loading.value = false
  }
}

const startCountdown = () => {
  if (countdownTimer) clearInterval(countdownTimer)
  if (!expireTime.value) return
  const update = () => {
    const left = Math.max(0, Math.floor((new Date(expireTime.value) - Date.now()) / 1000))
    countdown.value = left
    if (left === 0 && payStatus.value === 0) {
      payStatus.value = 2
    }
  }
  update()
  countdownTimer = setInterval(update, 1000)
}

const handleMockPay = async () => {
  if (!paymentInfo.value) return
  mockLoading.value = true
  try {
    await mockPaymentCallback(paymentInfo.value.paymentNo)
    ElMessage.success('模拟支付成功')
    // 开始轮询直到状态变为已支付
    startPolling()
  } catch (e) {
    ElMessage.error('支付失败')
  } finally {
    mockLoading.value = false
  }
}

const startPolling = () => {
  if (pollTimer) clearInterval(pollTimer)
  pollTimer = setInterval(async () => {
    try {
      const res = await getPaymentStatus(paymentInfo.value.paymentNo)
      if (res.data) {
        if (res.data.status === 1) {
          payStatus.value = 1
          clearInterval(pollTimer)
          ElMessage.success('支付成功')
          setTimeout(() => {
            goToTarget()
          }, 1500)
        } else if (res.data.status === 2) {
          payStatus.value = 2
          clearInterval(pollTimer)
        }
      }
    } catch (e) {}
  }, 2000)
}

const goToTarget = () => {
  if (paymentType.value === 'recharge') {
    router.push({ path: '/wallet' })
  } else {
    if (orderId.value) {
      router.push({ path: `/order/detail/${orderId.value}` })
    } else {
      router.push({ path: '/order/center' })
    }
  }
}

const handleCancelPay = () => {
  if (paymentType.value === 'recharge') {
    router.push({ path: '/wallet/recharge' })
  } else {
    router.push({ path: '/order/center' })
  }
}

const handleRetry = () => {
  if (paymentType.value === 'recharge') {
    router.push({ path: '/wallet/recharge' })
  } else {
    router.push({ path: '/order/center' })
  }
}

onMounted(() => {
  loadPayment()
})

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer)
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<style scoped>
.pay-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.pay-container {
  width: 100%;
  max-width: 480px;
}

.pay-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.logo {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  text-decoration: none;
}

.pay-title {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.pay-card {
  border-radius: 16px;
  border: none;
}

/* 状态横幅 */
.pay-status-banner {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  border-radius: 12px;
  margin-bottom: 20px;
}

.status-pending {
  background: linear-gradient(135deg, #fff7e6, #fff0d9);
}

.status-success {
  background: linear-gradient(135deg, #e6fffb, #d9f7ef);
}

.status-expired {
  background: linear-gradient(135deg, #f5f5f5, #ebebeb);
}

.status-icon-area {
  flex-shrink: 0;
}

.status-text-area h3 {
  margin: 0 0 4px;
  font-size: 16px;
  color: #333;
}

.status-text-area p {
  margin: 0;
  font-size: 13px;
  color: #666;
}

/* 信息网格 */
.pay-info-grid {
  background: #f9f9f9;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 20px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 13px;
  color: #999;
}

.info-value {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.amount-highlight {
  font-size: 20px;
  font-weight: 700;
  color: #667eea;
}

.order-link {
  color: #667eea;
  cursor: pointer;
  text-decoration: underline;
}

/* 二维码区 */
.qr-section {
  text-align: center;
}

.qr-tip {
  font-size: 14px;
  color: #666;
  margin-bottom: 16px;
}

.qr-box {
  display: inline-block;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  border: 2px dashed #d0d0d0;
  margin-bottom: 16px;
}

.qr-code {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 12px;
}

.mock-qr {
  width: 200px;
  height: 200px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.qr-blocks {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 4px;
  width: 160px;
  height: 160px;
}

.qr-block {
  background: #ddd;
  border-radius: 2px;
  transition: background 0.2s;
}

.qr-block.active {
  background: #333;
}

.qr-note {
  font-size: 12px;
  color: #bbb;
  margin: 0;
}

/* 倒计时 */
.countdown-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  background: #fff7e6;
  border-radius: 8px;
  font-size: 14px;
  color: #ad6800;
  margin-bottom: 20px;
}

/* 按钮 */
.pay-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 20px;
}

.pay-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea, #764ba2) !important;
  border: none !important;
  border-radius: 10px !important;
}

.cancel-btn {
  width: 100%;
  height: 48px;
  font-size: 15px;
  border-radius: 10px !important;
}
</style>