<template>
  <div class="forget-container">
    <div class="forget-box">
      <div class="forget-header">
        <h1 class="logo">🦞 找回密码</h1>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="forget-form"
        @submit.prevent="handleSubmit"
      >
        <el-form-item prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="请输入注册手机号"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="smsCode">
          <el-input
            v-model="form.smsCode"
            placeholder="请输入短信验证码"
            size="large"
            prefix-icon="Message"
          >
            <template #append>
              <el-button @click="handleSendCode" :disabled="countdown > 0">
                {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入新密码（6-20位）"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="submit-btn"
            @click="handleSubmit"
          >
            重置密码
          </el-button>
        </el-form-item>
      </el-form>

      <div class="forget-footer">
        <router-link to="/login" class="back-login">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
onMounted(() => {
  document.title = '忘记密码 - 龙虾道具交易平台'
})

import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { sendSmsCode, resetPassword } from '@/api/auth'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const countdown = ref(0)
let countdownTimer = null

const form = reactive({
  phone: '',
  smsCode: '',
  password: ''
})

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  smsCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{4,6}$/, message: '请输入4-6位验证码', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ]
}

const handleSendCode = async () => {
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.error('请输入正确的手机号')
    return
  }
  try {
    await sendSmsCode(form.phone, 'reset_password')
    ElMessage.success('验证码已发送')
    countdown.value = 60
    countdownTimer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(countdownTimer)
    }, 1000)
  } catch (error) {
    console.error('Failed to send code:', error)
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await resetPassword({
        phone: form.phone,
        code: form.smsCode,
        password: form.password
      })
      ElMessage.success('密码重置成功')
      router.push({ path: '/login' })
    } catch (error) {
      console.error('Reset password failed:', error)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.forget-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.forget-box {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.forget-header {
  text-align: center;
  margin-bottom: 30px;
}

.logo {
  font-size: 24px;
  color: #333;
  margin: 0;
}

.submit-btn {
  width: 100%;
}

.forget-footer {
  text-align: center;
}

.back-login {
  color: #667eea;
  text-decoration: none;
  font-size: 14px;
}
</style>
