<template>
  <div class="admin-login-page">
    <div class="login-card">
      <div class="login-header">
        <span class="logo">🦞</span>
        <h2>龙虾管理后台</h2>
        <p>超级管理员登录</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="管理员账号" size="large" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" class="login-btn" native-type="submit">
            登录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-tip">测试账号：admin / admin123</div>
    </div>
  </div>
</template>

<script setup>
onMounted(() => {
  document.title = '管理员登录 - 龙虾道具交易平台'
})

import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminLogin } from '@/api/admin'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const form = ref({ username: 'admin', password: 'admin123' })
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const handleLogin = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await adminLogin(form.value)
      if (res.data?.token) {
        localStorage.setItem('adminToken', res.data.token)
        ElMessage.success('登录成功，即将跳转...')
        await router.push({ path: '/admin' }).catch(err => {
          console.error('Router push error:', err)
          ElMessage.error('跳转失败：' + err.message)
        })
      } else {
        ElMessage.error(res.message || '登录失败')
      }
    } catch (e) {
      console.error('Login error:', e)
      ElMessage.error('登录失败：' + (e.message || '网络错误'))
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.admin-login-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #1a1a2e, #16213e); }
.login-card { background: #fff; border-radius: 16px; padding: 40px; width: 400px; box-shadow: 0 20px 60px rgba(0,0,0,0.3); }
.login-header { text-align: center; margin-bottom: 32px; }
.logo { font-size: 48px; }
.login-header h2 { margin: 8px 0 4px; font-size: 24px; color: #333; }
.login-header p { color: #999; font-size: 14px; }
.login-btn { width: 100%; background: linear-gradient(135deg, #667eea, #764ba2); border: none; font-size: 16px; }
.login-tip { text-align: center; color: #aaa; font-size: 12px; margin-top: 8px; }
</style>