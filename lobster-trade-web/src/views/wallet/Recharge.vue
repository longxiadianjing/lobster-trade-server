<template>
  <PageLayout>
    <template #sidebar>
      <el-card class="menu-card" shadow="never" :body-style="{ padding: '0' }">
        <template #header>
          <span class="menu-title">💳 资金管理</span>
        </template>
        <el-menu :default-active="route.path" router>
          <el-menu-item index="/user">
            <el-icon><User /></el-icon>
            <span>个人信息</span>
          </el-menu-item>
          <el-menu-item index="/wallet">
            <el-icon><Wallet /></el-icon>
            <span>我的钱包</span>
          </el-menu-item>
          <el-menu-item index="/order/center">
            <el-icon><List /></el-icon>
            <span>订单中心</span>
          </el-menu-item>
          <el-menu-item index="/product/list">
            <el-icon><Goods /></el-icon>
            <span>商品列表</span>
          </el-menu-item>
        </el-menu>
      </el-card>
    </template>

    <div class="recharge-content">
      <el-card class="recharge-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">💳 账户充值</span>
          </div>
        </template>

        <div class="balance-tip">
          <el-icon><Wallet /></el-icon>
          当前可用余额：<strong>¥{{ walletStore.availableBalance }}</strong>
        </div>

        <div class="amount-section">
          <p class="section-label">选择充值金额</p>
          <div class="amount-grid">
            <div
              v-for="a in amounts"
              :key="a"
              class="amount-item"
              :class="{ active: selectedAmount === a }"
              @click="selectedAmount = a"
            >
              <span class="amount-value">¥{{ a }}</span>
            </div>
          </div>
        </div>

        <div class="custom-amount">
          <p class="section-label">自定义金额</p>
          <el-input
            v-model="customAmount"
            placeholder="请输入充值金额"
            type="number"
            class="custom-input"
            @input="selectedAmount = null"
          >
            <template #prefix>¥</template>
          </el-input>
        </div>

        <div class="mock-notice">
          <el-icon><InfoFilled /></el-icon>
          <span><strong>Mock 阶段：</strong>支付渠道暂未对接，充值成功后余额直接到账。正式版将支持支付宝、微信支付、银行转账</span>
        </div>

        <div class="action-row">
          <el-button type="primary" size="large" class="recharge-btn" @click="handleRecharge" :loading="loading">
            立即充值 ¥{{ selectedAmount || customAmount || 0 }}
          </el-button>
        </div>

        <!-- 支付渠道选择弹窗 -->
        <el-dialog v-model="showChannelDialog" title="选择支付渠道" width="400px" :close-on-click-modal="false">
          <div class="channel-list">
            <div
              v-for="ch in channelOptions"
              :key="ch.value"
              class="channel-item"
              @click="doRechargeWithChannel(ch.value)"
            >
              <span class="channel-icon">{{ ch.icon }}</span>
              <span class="channel-name">{{ ch.label }}</span>
              <el-icon class="channel-arrow"><Right /></el-icon>
            </div>
          </div>
        </el-dialog>
      </el-card>
    </div>
  </PageLayout>
</template>

<script setup>
  document.title = '充值 - 龙虾道具交易平台';

import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useWalletStore } from '@/stores/wallet'
import { ElMessage } from 'element-plus'
import PageLayout from '@/components/PageLayout.vue'
import { User, Wallet, List, Goods, InfoFilled, Right } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const walletStore = useWalletStore()

const amounts = [10, 50, 100, 200, 500, 1000]
const selectedAmount = ref(100)
const customAmount = ref('')
const loading = ref(false)

<script setup>
  document.title = '充值 - 龙虾道具交易平台';

import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useWalletStore } from '@/stores/wallet'
import { ElMessage } from 'element-plus'
import PageLayout from '@/components/PageLayout.vue'
import { User, Wallet, List, Goods, InfoFilled } from '@element-plus/icons-vue'
import { createRechargePayment } from '@/api/payment'

const router = useRouter()
const route = useRoute()
const walletStore = useWalletStore()

const amounts = [10, 50, 100, 200, 500, 1000]
const selectedAmount = ref(100)
const customAmount = ref('')
const loading = ref(false)
const showChannelDialog = ref(false)
const selectedChannel = ref('')

const channelOptions = [
  { label: '支付宝', value: 'alipay', icon: '💙' },
  { label: '微信支付', value: 'wechat', icon: '💚' },
  { label: '银行卡', value: 'bankcard', icon: '💳' }
]

const handleRecharge = () => {
  const amount = selectedAmount.value || parseFloat(customAmount.value)
  if (!amount || amount <= 0) {
    ElMessage.warning('请选择或输入充值金额')
    return
  }
  showChannelDialog.value = true
}

const doRechargeWithChannel = async (channel) => {
  const amount = selectedAmount.value || parseFloat(customAmount.value)
  selectedChannel.value = channel
  showChannelDialog.value = false
  loading.value = true
  try {
    const res = await createRechargePayment(amount, channel)
    if (res.data && res.data.paymentNo) {
      ElMessage.info('正在跳转支付页面...')
      router.push({ path: '/payment/pay', query: { no: res.data.paymentNo, type: 'recharge' } })
    } else {
      ElMessage.error('创建支付单失败')
    }
  } catch (e) {
    ElMessage.error('充值发起失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.recharge-content {
  max-width: 600px;
}

.menu-card {
  border-radius: 12px;
  border: none;
}

.menu-title {
  font-weight: 600;
  font-size: 14px;
}

.recharge-card {
  border-radius: 12px;
  border: none;
}

.card-header {
  display: flex;
  align-items: center;
}

.card-title {
  font-weight: 600;
  font-size: 15px;
}

.balance-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 16px;
  background: linear-gradient(135deg, #667eea, #7B5FD4);
  color: #fff;
  border-radius: 10px;
  margin-bottom: 24px;
  font-size: 14px;
}

.amount-section, .custom-amount {
  margin-bottom: 24px;
}

.section-label {
  font-size: 13px;
  color: #666;
  font-weight: 600;
  margin-bottom: 12px;
}

.amount-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.amount-item {
  padding: 20px;
  background: #f9f8ff;
  border-radius: 10px;
  text-align: center;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s;
}

.amount-item:hover {
  border-color: #7B5FD4;
}

.amount-item.active {
  background: linear-gradient(135deg, #667eea, #7B5FD4);
  border-color: transparent;
  color: #fff;
}

.amount-value {
  font-size: 20px;
  font-weight: 700;
}

.custom-input {
  max-width: 300px;
}

.mock-notice {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px 16px;
  background: #fff7e6;
  border: 1px solid #ffd591;
  border-radius: 8px;
  font-size: 13px;
  color: #ad6800;
  margin-bottom: 24px;
  line-height: 1.5;
}

.mock-notice .el-icon {
  color: #faad14;
  flex-shrink: 0;
  margin-top: 1px;
}

.action-row {
  margin-top: 24px;
}

.recharge-btn {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, #667eea, #7B5FD4) !important;
  border: none !important;
  font-size: 16px !important;
  font-weight: 700 !important;
  border-radius: 10px !important;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #667eea, #7B5FD4) !important;
  color: #fff !important;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
}

/* 支付渠道弹窗 */
.channel-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.channel-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border: 1px solid #eee;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.channel-item:hover {
  border-color: #667eea;
  background: #f9f8ff;
}

.channel-icon {
  font-size: 28px;
}

.channel-name {
  flex: 1;
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.channel-arrow {
  color: #ccc;
}
</style>