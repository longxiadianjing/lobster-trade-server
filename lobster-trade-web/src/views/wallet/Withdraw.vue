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

    <div class="withdraw-content">
      <el-card class="withdraw-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">💸 提现申请</span>
          </div>
        </template>

        <div class="balance-info">
          <div class="balance-item">
            <span class="balance-label">可用余额</span>
            <span class="balance-value">¥{{ walletStore.availableBalance }}</span>
          </div>
          <div class="balance-split"></div>
          <div class="balance-item">
            <span class="balance-label">冻结金额</span>
            <span class="balance-value frozen">¥{{ walletStore.frozenBalance }}</span>
          </div>
        </div>

        <el-form :model="form" label-width="80px" class="withdraw-form">
          <el-form-item label="提现金额">
            <el-input v-model="form.amount" type="number" placeholder="请输入提现金额">
              <template #prefix>¥</template>
            </el-input>
          </el-form-item>
          <el-form-item label="到账账户">
            <el-select v-model="form.accountType" placeholder="请选择账户类型" class="full-select">
              <el-option label="银行卡" value="bank" />
              <el-option label="支付宝" value="alipay" />
              <el-option label="微信" value="wechat" />
            </el-select>
          </el-form-item>
          <el-form-item label="账户信息">
            <el-input v-model="form.accountInfo" placeholder="请输入账户信息" />
          </el-form-item>
          <el-form-item label="支付密码">
            <el-input v-model="form.payPassword" type="password" placeholder="请输入支付密码" show-password />
          </el-form-item>
        </el-form>

        <div class="mock-notice">
          <el-icon><InfoFilled /></el-icon>
          <span><strong>Mock 阶段：</strong>支付渠道暂未对接，提现申请提交后直接进入模拟到账流程。正式版将支持支付宝、微信提现、银行转账</span>
        </div>

        <div class="withdraw-tips">
          <p class="tip-title">💡 提现说明</p>
          <ul class="tip-list">
            <li>提现申请提交后，预计1-3个工作日到账</li>
            <li>每笔提现收取 ¥1.00 手续费</li>
            <li>最低提现金额 ¥10.00，最高 ¥50000.00</li>
          </ul>
        </div>

        <div class="action-row">
          <el-button type="primary" size="large" class="withdraw-btn" @click="handleSubmit" :loading="loading">
            提交提现申请
          </el-button>
        </div>
      </el-card>
    </div>
  </PageLayout>
</template>

<script setup>
  document.title = '提现 - 龙虾道具交易平台';

import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { withdraw } from '@/api/wallet'
import { useWalletStore } from '@/stores/wallet'
import { ElMessage } from 'element-plus'
import PageLayout from '@/components/PageLayout.vue'
import { User, Wallet, List, Goods, InfoFilled } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const walletStore = useWalletStore()

const form = reactive({
  amount: '',
  accountType: '',
  accountInfo: '',
  payPassword: ''
})
const loading = ref(false)

const handleSubmit = async () => {
  if (!form.amount || parseFloat(form.amount) <= 0) {
    ElMessage.warning('请输入正确的提现金额')
    return
  }
  if (!form.accountType || !form.accountInfo) {
    ElMessage.warning('请填写完整的账户信息')
    return
  }
  if (!form.payPassword) {
    ElMessage.warning('请输入支付密码')
    return
  }
  const amt = parseFloat(form.amount)
  if (amt < 10) {
    ElMessage.warning('最低提现 10 元')
    return
  }
  if (amt > 50000) {
    ElMessage.warning('最高提现 50000 元')
    return
  }
  if (amt > parseFloat(walletStore.availableBalance)) {
    ElMessage.warning('可用余额不足')
    return
  }
  loading.value = true
  try {
    await withdraw({
      amount: amt,
      channel: form.accountType,
      accountInfo: form.accountInfo,
      payPassword: form.payPassword
    })
    walletStore.refresh()
    ElMessage.success('提现申请已提交，预计1-3个工作日到账')
    router.push({ path: '/wallet' })
  } catch (e) {
    ElMessage.error(e.message || '提现失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.withdraw-content {
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

.withdraw-card {
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

.balance-info {
  display: flex;
  align-items: center;
  padding: 16px;
  background: linear-gradient(135deg, #667eea, #7B5FD4);
  border-radius: 10px;
  margin-bottom: 24px;
  gap: 24px;
}

.balance-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.balance-label {
  font-size: 12px;
  color: rgba(255,255,255,0.7);
}

.balance-value {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
}

.balance-value.frozen {
  color: rgba(255,255,255,0.6);
}

.balance-split {
  width: 1px;
  height: 40px;
  background: rgba(255,255,255,0.2);
}

.withdraw-form {
  margin-bottom: 20px;
}

.full-select {
  width: 100%;
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
  margin-bottom: 20px;
  line-height: 1.5;
}

.mock-notice .el-icon {
  color: #faad14;
  flex-shrink: 0;
  margin-top: 1px;
}

.withdraw-tips {
  background: #f9f8ff;
  padding: 14px 16px;
  border-radius: 10px;
  margin-bottom: 24px;
}

.tip-title {
  font-size: 13px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
}

.tip-list {
  margin: 0;
  padding-left: 18px;
}

.tip-list li {
  font-size: 12px;
  color: #666;
  line-height: 1.8;
}

.action-row {
  display: flex;
  justify-content: center;
}

.withdraw-btn {
  width: 100%;
  max-width: 300px;
  height: 48px;
  background: linear-gradient(135deg, #667eea, #7B5FD4) !important;
  border: none !important;
  font-size: 16px !important;
  font-weight: 700 !important;
  border-radius: 10px !important;
}

:deep(.el-form-item__label) {
  font-weight: 600;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #667eea, #7B5FD4) !important;
  color: #fff !important;
}
</style>