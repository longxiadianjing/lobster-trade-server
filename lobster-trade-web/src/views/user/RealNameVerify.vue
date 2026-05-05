<template>
  <div class="realname-container">
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
        <el-card class="realname-card">
          <template #header>
            <span>实名认证</span>
          </template>

          <!-- 已认证 (status=1) -->
          <div v-if="realNameStatus === 1" class="verified">
            <el-result icon="success" title="已实名认证">
              <template #sub-title>
                <p class="real-name-text">{{ maskedName }}</p>
                <p class="id-card-text">身份证：{{ maskedIdCard }}</p>
              </template>
            </el-result>
          </div>

          <!-- 审核中 (status=0) -->
          <div v-else-if="realNameStatus === 0" class="pending">
            <el-result icon="info" title="实名认证审核中">
              <template #sub-title>
                <p class="pending-tip">您的实名认证申请正在审核中，请耐心等待。</p>
                <p class="pending-tip">审核结果将在1-3个工作日内通知。</p>
              </template>
            </el-result>
          </div>

          <!-- 未通过 (status=2) -->
          <div v-else-if="realNameStatus === 2" class="rejected">
            <el-result icon="error" title="实名认证未通过">
              <template #sub-title>
                <p class="rejected-tip">未通过原因：{{ rejectReason || '信息审核不符，请重新提交' }}</p>
              </template>
              <template #extra>
                <el-button type="primary" size="large" @click="realNameStatus = -1">重新认证</el-button>
              </template>
            </el-result>
          </div>

          <!-- 已撤回 (status=3) 或未认证 -->
          <div v-else>
            <el-alert
              title="实名认证后可发布商品、发起提现等操作"
              type="info"
              :closable="false"
              style="margin-bottom: 20px;"
            />

            <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
              <el-form-item label="真实姓名" prop="realName">
                <el-input v-model="form.realName" placeholder="请输入真实姓名" size="large" />
              </el-form-item>

              <el-form-item label="身份证号" prop="idCard">
                <el-input v-model="form.idCard" placeholder="请输入身份证号" size="large" maxlength="18" />
              </el-form-item>

              <el-form-item>
                <el-button type="primary" size="large" :loading="loading" @click="handleApply">
                  提交认证
                </el-button>
              </el-form-item>
            </el-form>

            <div class="tips">
              <p>1. 请填写真实信息，审核通过后不可修改</p>
              <p>2. 平台将保护您的个人信息安全</p>
              <p>3. 审核结果将在1-3个工作日内通知</p>
            </div>
          </div>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { applyRealName, getRealNameStatus } from '@/api/user'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const realNameStatus = ref(-1) // -1=未认证(可申请)
const maskedName = ref('')
const maskedIdCard = ref('')
const rejectReason = ref('')

const form = reactive({
  realName: '',
  idCard: ''
})

const rules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度2-20位', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/, message: '身份证号格式不正确', trigger: 'blur' }
  ]
}

onMounted(async () => {
  try {
    const res = await getRealNameStatus()
    // status: 0=审核中, 1=已认证, 2=未通过, 3=已撤回
    const status = res.data?.status ?? -1
    const createTime = res.data?.createTime
    // status=0 且无 createTime = 从未申请（显示表单）
    // status=0 且有 createTime = 审核中
    // status=1 = 已认证, status=2 = 未通过, status=3 = 已撤回
    if (status === 0 && createTime) {
      realNameStatus.value = 0 // 审核中
    } else {
      realNameStatus.value = status === 1 || status === 2 ? status : -1
    }
    if (res.data?.realName) maskedName.value = res.data.realName
    if (res.data?.idCard) maskedIdCard.value = res.data.idCard
    if (res.data?.rejectReason) rejectReason.value = res.data.rejectReason
  } catch (error) {
    console.error('Failed to get real name status:', error)
  }

  document.title = '实名认证 - 龙虾道具交易平台'
})

const handleApply = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await applyRealName({
        realName: form.realName,
        idCard: form.idCard
      })
      ElMessage.success('实名认证提交成功')
      router.push({ path: '/user' })
    } catch (error) {
      console.error('Failed to apply real name:', error)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.realname-container {
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

.verified {
  text-align: center;
  padding: 20px 0;
}

.real-name-text {
  font-size: 18px;
  font-weight: bold;
  margin: 8px 0;
}

.id-card-text {
  color: #666;
  margin: 0;
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
