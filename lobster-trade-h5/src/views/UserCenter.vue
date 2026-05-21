<template>
  <div class="user-center">
    <div class="user-header">
      <van-image round width="60" height="60" :src="userInfo.avatar" />
      <div class="user-name">{{ userInfo.nickname || '未登录' }}</div>
      <van-button size="small" type="primary" plain @click="handleLogin" v-if="!isLoggedIn">登录</van-button>
    </div>

    <van-cell-group title="我的交易">
      <van-cell is-link to="/orders?type=buyer" title="我的订单" icon="orders-o" />
      <van-cell is-link to="/favorites" title="我的收藏" icon="star-o" />
    </van-cell-group>

    <van-cell-group title="资产管理">
      <van-cell is-link to="/wallet" title="我的钱包" icon="balance-o">
        <template #value>
          <span style="color:#ff6000">¥{{ walletInfo.balance || 0 }}</span>
        </template>
      </van-cell>
      <van-cell is-link to="/recharge" title="充值" icon="gold-coin-o" />
      <van-cell is-link to="/withdraw" title="提现" icon="cash-back-record" />
    </van-cell-group>

    <van-cell-group title="账号管理">
      <van-cell is-link to="/settings" title="设置" icon="setting-o" />
      <van-cell is-link to="/about" title="关于我们" icon="info-o" />
    </van-cell-group>

    <van-button v-if="isLoggedIn" type="danger" plain block style="margin-top:16px" @click="handleLogout">退出登录</van-button>

    <TabBar />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getWalletInfo } from '@/api/wallet'
import TabBar from '@/components/TabBar.vue'

document.title = '用户中心 - 龙虾道具交易平台'

const router = useRouter()
const userStore = useUserStore()
const walletInfo = ref({})
const isLoggedIn = computed(() => userStore.isLoggedIn)
const userInfo = computed(() => userStore.userInfo || {})

const handleLogin = () => router.push('/login')
const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

onMounted(async () => {
  if (isLoggedIn.value) {
    try {
      const res = await getWalletInfo()
      walletInfo.value = res.data || res
    } catch (e) {
      console.error(e)
    }
  }
})
</script>

<style scoped>
.user-header {
  background: linear-gradient(135deg, #ff6000, #ff8c00);
  padding: 30px 16px;
  color: #fff;
  text-align: center;
}
.user-name { margin-top: 8px; font-size: 18px; font-weight: bold; }
.user-center { padding-bottom: 60px; }
</style>