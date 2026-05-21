<template>
  <div class="register-page">
    <div class="logo-area">
      <div class="logo">🦞</div>
      <div class="app-name">龙虾道具交易平台</div>
    </div>

    <van-form @submit="handleRegister">
      <van-cell-group inset>
        <van-field
          v-model="form.phone"
          type="tel"
          placeholder="请输入手机号"
          :rules="[{ required: true, message: '请输入手机号' }]"
        />
        <van-field
          v-model="form.code"
          type="digit"
          placeholder="请输入验证码"
          :rules="[{ required: true, message: '请输入验证码' }]"
        >
          <template #button>
            <van-button size="small" type="primary" plain :disabled="countdown > 0" @click="sendCode">
              {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
            </van-button>
          </template>
        </van-field>
        <van-field
          v-model="form.password"
          type="password"
          placeholder="请设置密码"
          :rules="[{ required: true, message: '请设置密码' }]"
        />
      </van-cell-group>

      <div class="extra-links">
        <span @click="$router.push('/login')">已有账号？去登录</span>
      </div>

      <div style="padding: 16px">
        <van-button type="primary" size="large" block native-type="submit" :loading="loading">注册</van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { register as apiRegister, sendSmsCode as apiSendCode } from '@/api/auth'
import { Toast } from 'vant'

document.title = '注册 - 龙虾道具交易平台'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const countdown = ref(0)
const form = ref({ phone: '', code: '', password: '' })

let timer
const sendCode = async () => {
  try {
    await apiSendCode(form.value.phone, 'register')
    Toast.success('验证码已发送')
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (e) {
    Toast.fail(e.message || '发送失败')
  }
}

const handleRegister = async () => {
  loading.value = true
  try {
    await apiRegister(form.value)
    Toast.success('注册成功，请登录')
    router.replace('/login')
  } catch (e) {
    Toast.fail(e.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page { padding-top: 60px; }
.logo-area { text-align: center; padding: 40px 0; }
.logo { font-size: 64px; }
.app-name { font-size: 20px; color: #333; font-weight: bold; margin-top: 8px; }
.extra-links { text-align: right; padding: 0 16px; margin-top: 8px; }
.extra-links span { color: #1989fa; font-size: 14px; }
</style>