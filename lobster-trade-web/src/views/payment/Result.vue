<template>
  <div class="result-page">
    <div class="result-container">
      <div class="result-card">
        <!-- 加载中 -->
        <div v-if="loading" class="result-loading">
          <el-icon class="is-loading" :size="48" color="#667eea"><Loading /></el-icon>
          <p>正在确认支付结果...</p>
        </div>

        <!-- 支付成功 -->
        <div v-else-if="status === 'success'" class="result-success">
          <div class="result-icon success-icon">✅</div>
          <h2>支付成功</h2>
          <p class="result-desc">您的充值已到账，欢迎使用龙虾道具交易平台</p>
          <div class="result-info">
            <div class="info-row">
              <span>支付单号</span>
              <span>{{ paymentNo }}</span>
            </div>
            <div class="info-row">
              <span>充值金额</span>
              <span class="amount">¥{{ amount }}</span>
            </div>
          </div>
          <div class="result-actions">
            <el-button type="primary" size="large" @click="goWallet">前往钱包</el-button>
            <el-button size="large" @click="goHome">返回首页</el-button>
          </div>
        </div>

        <!-- 支付失败 -->
        <div v-else-if="status === 'fail'" class="result-fail">
          <div class="result-icon fail-icon">❌</div>
          <h2>支付失败</h2>
          <p class="result-desc">支付未能完成，金额将原路退回，请稍后重试</p>
          <div class="result-actions">
            <el-button type="primary" size="large" @click="goRetry">重新充值</el-button>
            <el-button size="large" @click="goHome">返回首页</el-button>
          </div>
        </div>

        <!-- 查询失败 -->
        <div v-else class="result-error">
          <div class="result-icon">⚠️</div>
          <h2>查询失败</h2>
          <p class="result-desc">无法确认支付状态，请稍后刷新页面或前往钱包查看</p>
          <div class="result-actions">
            <el-button type="primary" size="large" @click="checkAgain">重新查询</el-button>
            <el-button size="large" @click="goWallet">前往钱包</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getPaymentStatus } from '@/api/payment'
import { useWalletStore } from '@/stores/wallet'
import { Loading } from '@element-plus/icons-vue'

document.title = '支付结果 - 龙虾道具交易平台'

const router = useRouter()
const route = useRoute()
const walletStore = useWalletStore()

const loading = ref(true)
const status = ref('') // success / fail / error
const paymentNo = ref('')
const amount = ref('')

const goWallet = () => {
  walletStore.refresh()
  router.push({ path: '/wallet' })
}

const goHome = () => router.push({ path: '/' })

const goRetry = () => router.push({ path: '/wallet/recharge' })

const checkAgain = () => {
  loading.value = true
  status.value = ''
  loadResult()
}

const loadResult = async () => {
  const no = route.query.paymentNo || route.query.paymentNo
  if (!no) {
    status.value = 'error'
    loading.value = false
    return
  }
  paymentNo.value = no

  try {
    const res = await getPaymentStatus(no)
    loading.value = false
    if (res.data) {
      amount.value = res.data.amount
      status.value = res.data.status === 1 ? 'success' : 'fail'
    } else {
      status.value = 'error'
    }
  } catch (e) {
    loading.value = false
    status.value = 'error'
  }
}

onMounted(() => {
  loadResult()
})
</script>

<style scoped>
.result-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.result-container {
  width: 100%;
  max-width: 440px;
}

.result-card {
  background: #fff;
  border-radius: 20px;
  padding: 40px 32px;
  text-align: center;
}

.result-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  color: #667eea;
}

.result-loading p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.result-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.result-success h2, .result-fail h2, .result-error h2 {
  margin: 0 0 8px;
  font-size: 22px;
  color: #333;
}

.result-desc {
  margin: 0 0 24px;
  color: #888;
  font-size: 14px;
}

.result-info {
  background: #f9f8ff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 28px;
  text-align: left;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
  color: #666;
}

.info-row .amount {
  font-weight: 700;
  color: #667eea;
  font-size: 18px;
}

.result-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.result-actions .el-button {
  border-radius: 10px;
  font-weight: 600;
}

.result-actions .el-button--primary {
  background: linear-gradient(135deg, #667eea, #764ba2) !important;
  border: none !important;
}
</style>
