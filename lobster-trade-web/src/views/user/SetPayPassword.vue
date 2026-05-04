<template>
  <div class="set-pwd-container">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <h1 class="logo">🦞 龙虾道具交易平台</h1>
          <div class="user-actions">
            <router-link to="/user">
              <el-button>返回个人中心</el-button>
            </router-link>
          </div>
        </div>
      </el-header>

      <el-main class="main">
        <el-card class="pwd-card">
          <template #header>
            <span>设置支付密码</span>
          </template>

          <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
            <el-form-item label="支付密码" prop="payPassword">
              <el-input
                v-model="form.payPassword"
                type="password"
                placeholder="请输入6位数字支付密码"
                size="large"
                show-password
                maxlength="6"
              />
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="form.confirmPassword"
                type="password"
                placeholder="请再次输入支付密码"
                size="large"
                show-password
                maxlength="6"
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" size="large" :loading="loading" @click="handleSubmit">
                确认设置
              </el-button>
            </el-form-item>
          </el-form>

          <div class="tips">
            <p>支付密码用于：提现、充值等资金操作</p>
          </div>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
onMounted(() => {
  document.title = '设置支付密码 - 龙虾道具交易平台'
})

import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { setPayPassword } from '@/api/user'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  payPassword: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== form.payPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  payPassword: [
    { required: true, message: '请输入支付密码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '支付密码必须为6位数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认支付密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await setPayPassword(form.payPassword)
      ElMessage.success('支付密码设置成功')
      router.push({ path: '/user' })
    } catch (error) {
      console.error('Failed to set pay password:', error)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.set-pwd-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

.logo {
  font-size: 20px;
  color: #333;
  margin: 0;
}

.main {
  max-width: 600px;
  margin: 20px auto;
  padding: 0 20px;
}

.tips {
  color: #999;
  font-size: 14px;
  line-height: 1.8;
  margin-top: 20px;
}

.tips p {
  margin: 0;
}
</style>
