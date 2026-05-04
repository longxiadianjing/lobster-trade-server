<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <h1 class="logo">🦞 用户注册</h1>
        <p class="subtitle">加入龙虾道具交易平台</p>
      </div>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="register-form"
        @submit.prevent="handleRegister"
      >
        <el-form-item prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入手机号"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="smsCode">
          <el-input
            v-model="registerForm.smsCode"
            placeholder="请输入短信验证码"
            size="large"
            prefix-icon="Message"
          >
            <template #append>
              <el-button
                @click="handleSendCode"
                :disabled="countdown > 0"
                class="send-code-btn"
              >
                {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请设置密码（6-20位）"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请确认密码"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item prop="agreement">
          <el-checkbox v-model="registerForm.agreement">
            我已阅读并同意
            <a href="#" class="agreement-link">《用户协议》</a>
            和
            <a href="#" class="agreement-link">《隐私政策》</a>
          </el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="register-btn"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-footer">
        已有账号？
        <router-link to="/login" class="login-link">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
onMounted(() => {
  document.title = '用户注册 - 龙虾道具交易平台'
})

import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register, sendSmsCode } from '@/api/auth'

const router = useRouter()

const registerFormRef = ref(null)
const loading = ref(false)
const countdown = ref(0)
let countdownTimer = null

const registerForm = reactive({
  phone: '',
  smsCode: '',
  password: '',
  confirmPassword: '',
  agreement: false
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  smsCode: [
    { required: true, message: '请输入短信验证码', trigger: 'blur' },
    { pattern: /^\d{4,6}$/, message: '请输入4-6位验证码', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  agreement: [
    { validator: (rule, value, callback) => {
      if (!value) {
        callback(new Error('请阅读并同意用户协议'))
      } else {
        callback()
      }
    }, trigger: 'change' }
  ]
}

const handleSendCode = async () => {
  if (!/^1[3-9]\d{9}$/.test(registerForm.phone)) {
    ElMessage.error('请输入正确的手机号')
    return
  }

  try {
    await sendSmsCode(registerForm.phone, 'register')
    ElMessage.success('验证码已发送')
    countdown.value = 60
    countdownTimer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(countdownTimer)
      }
    }, 1000)
  } catch (error) {
    console.error('Failed to send code:', error)
  }
}

const handleRegister = async () => {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await register({
        phone: registerForm.phone,
        code: registerForm.smsCode,
        password: registerForm.password
      })

      ElMessage.success('注册成功，请登录')
      router.push({ path: '/login' })
    } catch (error) {
      console.error('Register failed:', error)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-box {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.logo {
  font-size: 24px;
  color: #333;
  margin: 0 0 8px 0;
}

.subtitle {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.send-code-btn {
  width: 90px;
}

.register-btn {
  width: 100%;
}

.register-footer {
  text-align: center;
  color: #666;
  font-size: 14px;
}

.login-link {
  color: #667eea;
  text-decoration: none;
  margin-left: 4px;
}

.agreement-link {
  color: #667eea;
  text-decoration: none;
}
</style>
