<template>
  <div class="login-page">
    <div class="logo-area">
      <div class="logo">🦞</div>
      <div class="app-name">龙虾道具交易平台</div>
    </div>

    <van-form @submit="handleLogin">
      <van-cell-group inset>
        <van-field
          v-model="form.phone"
          label="+86"
          type="tel"
          placeholder="请输入手机号"
          :rules="[{ required: true, message: '请输入手机号' }]"
        />
        <van-field
          v-model="form.password"
          type="password"
          placeholder="请输入密码"
          :rules="[{ required: true, message: '请输入密码' }]"
        />
      </van-cell-group>

      <div class="extra-links">
        <span @click="$router.push('/register')">没有账号？去注册</span>
      </div>

      <div style="padding: 16px">
        <van-button type="primary" size="large" block native-type="submit" :loading="loading">登录</van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { login as apiLogin } from '@/api/auth'
import { Toast } from 'vant'

document.title = '登录 - 龙虾道具交易平台'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const form = ref({ phone: '', password: '' })

const handleLogin = async () => {
  loading.value = true
  try {
    const res = await apiLogin(form.value)
    userStore.setToken(res.data?.token || res.token)
    await userStore.fetchUserInfo()
    Toast.success('登录成功')
    router.replace('/')
  } catch (e) {
    Toast.fail(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page { padding-top: 60px; }
.logo-area { text-align: center; padding: 40px 0; }
.logo { font-size: 64px; }
.app-name { font-size: 20px; color: #333; font-weight: bold; margin-top: 8px; }
.extra-links { text-align: right; padding: 0 16px; margin-top: 8px; }
.extra-links span { color: #1989fa; font-size: 14px; }
</style>