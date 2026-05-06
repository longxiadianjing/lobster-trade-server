<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="logo">🦞</div>
        <h2>龙虾道具交易平台</h2>
        <p>管理后台</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="管理员账号"
            prefix-icon="User"
            clearable
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="登录密码"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            class="login-btn"
            native-type="submit"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-tip">
        <p>测试账号：admin / admin123</p>
      </div>
    </div>
  </div>
</template>

<script setup>
document.title = '管理员登录 - 龙虾道具交易平台'
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/admin'
import { ElMessage } from 'element-plus'

const router = useRouter()
const adminStore = useAdminStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: 'admin',
  password: 'admin123'
})

const rules = {
  username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入登录密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await adminStore.login(form.username, form.password)
      ElMessage.success('登录成功')
      router.push('/dashboard')
    } catch (e) {
      // 错误已在拦截器处理
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-box {
  width: 400px;
  background: #fff;
  border-radius: 16px;
  padding: 40px 36px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo {
  font-size: 48px;
  margin-bottom: 12px;
}

.login-header h2 {
  margin: 0 0 4px;
  font-size: 22px;
  color: #333;
}

.login-header p {
  margin: 0;
  font-size: 14px;
  color: #999;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
}

.login-tip {
  margin-top: 20px;
  text-align: center;
  font-size: 12px;
  color: #aaa;
}

.login-tip p {
  margin: 0;
}
</style>