<template>
  <div class="realname-page">
    <!-- 顶部导航 -->
    <div class="page-header">
      <div class="header-inner">
        <router-link to="/user" class="back-link">
          <span class="back-icon">←</span>
          返回个人中心
        </router-link>
        <h1 class="page-title">实名认证</h1>
      </div>
    </div>

    <!-- 主内容卡片 -->
    <div class="page-content">
      <el-card class="main-card" shadow="hover">

        <!-- ✅ 已认证状态 -->
        <div v-if="status === 2" class="status-box verified">
          <div class="status-icon success-icon">✓</div>
          <h2 class="status-title">已实名认证</h2>
          <div class="info-list">
            <div class="info-item">
              <span class="info-label">真实姓名</span>
              <span class="info-value">{{ maskedName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">身份证号</span>
              <span class="info-value">{{ maskedIdCard }}</span>
            </div>
          </div>
          <el-tag type="success" size="large" effect="dark" class="status-tag">认证成功</el-tag>
        </div>

        <!-- ⏳ 认证中状态 -->
        <div v-else-if="status === 1" class="status-box pending">
          <div class="status-icon pending-icon">⏳</div>
          <h2 class="status-title">认证审核中</h2>
          <p class="status-desc">您的实名认证申请正在审核，请耐心等待。</p>
          <p class="status-desc">审核结果将在 1-3 个工作日内通知，请保持手机/邮箱畅通。</p>
          <el-button type="primary" plain @click="checkResult">刷新状态</el-button>
        </div>

        <!-- ❌ 认证失败状态 -->
        <div v-else-if="status === 3" class="status-box failed">
          <div class="status-icon failed-icon">✗</div>
          <h2 class="status-title">认证失败</h2>
          <p class="status-desc fail-reason">失败原因：{{ failReason || '信息审核不符，请重新提交真实有效的信息。' }}</p>
          <el-button type="danger" size="large" @click="status = 0">重新申请认证</el-button>
        </div>

        <!-- 📝 未认证 / 申请表单 -->
        <div v-else class="apply-form">
          <el-alert
            title="实名认证后可发布商品、发起提现等操作"
            type="info"
            :closable="false"
            class="form-tip"
          />

          <!-- 申请方式选择 -->
          <div class="apply-tabs">
            <div
              class="apply-tab"
              :class="{ active: applyMethod === 'aliyun' }"
              @click="applyMethod = 'aliyun'"
            >
              <span class="tab-icon">🅰️</span>
              <span class="tab-text">阿里云认证（自动）</span>
            </div>
            <div
              class="apply-tab"
              :class="{ active: applyMethod === 'manual' }"
              @click="applyMethod = 'manual'"
            >
              <span class="tab-icon">📝</span>
              <span class="tab-text">手动申请</span>
            </div>
          </div>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-width="100px"
            class="realname-form"
          >
            <el-form-item label="真实姓名" prop="realName">
              <el-input
                v-model="form.realName"
                placeholder="请输入真实姓名"
                size="large"
                maxlength="20"
              />
            </el-form-item>

            <el-form-item label="身份证号" prop="idCard">
              <el-input
                v-model="form.idCard"
                placeholder="请输入身份证号"
                size="large"
                maxlength="18"
                show-word-limit
              />
            </el-form-item>

            <el-form-item>
              <el-button
                v-if="applyMethod === 'aliyun'"
                type="primary"
                size="large"
                :loading="loading"
                class="submit-btn"
                @click="handleAliyun"
              >
                前往认证
              </el-button>
              <el-button
                v-else
                type="primary"
                size="large"
                :loading="loading"
                class="submit-btn"
                @click="handleManual"
              >
                提交申请
              </el-button>
            </el-form-item>
          </el-form>

          <div class="tips-box">
            <p class="tip-title">📌 温馨提示</p>
            <ul class="tip-list">
              <li>请填写真实信息，认证通过后不可修改</li>
              <li>平台将严格保护您的个人信息安全</li>
              <li>审核结果将在 1-3 个工作日内通知</li>
              <li>阿里云认证为自动审核，提交后即时生效</li>
            </ul>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

// 状态: 0=未认证, 1=认证中, 2=已认证, 3=认证失败
const status = ref(0)
const loading = ref(false)
const applyMethod = ref('aliyun')
const failReason = ref('')
const formRef = ref(null)

// 真实数据（已认证时从接口获取）
const realName = ref('')
const idCard = ref('')

// 脱敏姓名：张三 → 张*
const maskedName = computed(() => {
  if (!realName.value) return '***'
  if (realName.value.length === 1) return realName.value + '*'
  return realName.value[0] + '*'.repeat(realName.value.length - 1)
})

// 脱敏身份证：110101199001011234 → 110101***********234
const maskedIdCard = computed(() => {
  if (!idCard.value || idCard.value.length < 10) return '*******************'
  return idCard.value.slice(0, 6) + '*'.repeat(7) + idCard.value.slice(-4)
})

const form = reactive({
  realName: '',
  idCard: ''
})

const rules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度 2-20 个字符', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    {
      pattern: /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/,
      message: '身份证号格式不正确',
      trigger: 'blur'
    }
  ]
}

// 获取认证状态
const fetchStatus = async () => {
  try {
    const res = await request.get('/real-name/status')
    const data = res.data || {}
    // user.real_name_status: 0=未认证, 1=认证中, 2=已认证, 3=认证失败
    status.value = data.realNameStatus ?? 0
    failReason.value = data.failReason || data.rejectReason || ''
    if (data.realName) realName.value = data.realName
    if (data.idCardNumber) idCard.value = data.idCardNumber
    if (data.idCard) idCard.value = data.idCard
  } catch (error) {
    console.error('获取实名认证状态失败:', error)
  }
}

// 刷新状态（查询阿里云认证结果）
const checkResult = async () => {
  try {
    loading.value = true
    const res = await request.post('/real-name/verify-result')
    if (res.data?.realNameStatus) {
      status.value = res.data.realNameStatus
      failReason.value = res.data.failReason || ''
      if (res.data.realName) realName.value = res.data.realName
      if (res.data.idCardNumber) idCard.value = res.data.idCardNumber
      if (res.data.idCard) idCard.value = res.data.idCard
      if (status.value === 2) {
        ElMessage.success('认证成功！')
      } else if (status.value === 3) {
        ElMessage.warning('认证失败，请重新申请。')
      }
    }
  } catch (error) {
    console.error('查询认证结果失败:', error)
  } finally {
    loading.value = false
  }
}

// 阿里云认证
const handleAliyun = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await request.post('/real-name/init', {
        realName: form.realName,
        idCard: form.idCard
      })
      // init 返回阿里云认证跳转参数（HTML表单或URL）
      const html = res.data?.html || res.data
      if (html) {
        // 渲染阿里云认证跳转表单并自动提交
        const win = window.open('', '_blank', 'width=600,height=700')
        win.document.write(html)
        win.document.close()
        // 监听窗口关闭，轮询结果
        const pollTimer = setInterval(async () => {
          if (win.closed) {
            clearInterval(pollTimer)
            await fetchStatus()
            if (status.value === 1) {
              ElMessage.info('认证审核中，请稍后刷新查看结果。')
            }
          }
        }, 1000)
      } else {
        // 无HTML则直接轮询状态
        await fetchStatus()
        if (status.value === 1) {
          ElMessage.success('认证已提交，请等待审核结果。')
        }
      }
    } catch (error) {
      console.error('阿里云认证失败:', error)
    } finally {
      loading.value = false
    }
  })
}

// 手动申请
const handleManual = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await request.post('/real-name/apply', {
        realName: form.realName,
        idCard: form.idCard
      })
      ElMessage.success('实名认证申请已提交，请等待审核结果。')
      status.value = 1 // 转为认证中
    } catch (error) {
      console.error('手动申请失败:', error)
    } finally {
      loading.value = false
    }
  })
}

onMounted(() => {
  document.title = '实名认证 - 龙虾道具交易平台'
  fetchStatus()
})
</script>

<style scoped>
.realname-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f4ff 0%, #f5f7fa 100%);
}

.page-header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.header-inner {
  max-width: 700px;
  margin: 0 auto;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-link {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
  transition: opacity 0.2s;
}

.back-link:hover {
  opacity: 0.7;
}

.back-icon {
  font-size: 16px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.page-content {
  max-width: 600px;
  margin: 0 auto;
  padding: 24px 20px;
}

.main-card {
  border-radius: 16px;
  overflow: hidden;
}

/* 状态通用 */
.status-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32px 16px;
  text-align: center;
}

.status-icon {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  margin-bottom: 16px;
}

.success-icon {
  background: #f0f9eb;
  color: #67c23a;
}

.pending-icon {
  background: #fdf6ec;
  color: #e6a23c;
}

.failed-icon {
  background: #fef0f0;
  color: #f56c6c;
}

.status-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px;
}

.status-desc {
  color: #666;
  font-size: 14px;
  line-height: 1.8;
  margin: 0 0 8px;
}

.fail-reason {
  color: #f56c6c;
  font-weight: 500;
  margin-bottom: 20px;
}

/* 已认证信息列表 */
.info-list {
  width: 100%;
  max-width: 320px;
  background: #f9fafb;
  border-radius: 12px;
  padding: 20px;
  margin: 16px 0;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  color: #999;
  font-size: 14px;
}

.info-value {
  color: #333;
  font-size: 15px;
  font-weight: 500;
  letter-spacing: 1px;
}

.status-tag {
  margin-top: 8px;
  font-size: 15px;
  padding: 6px 20px;
  border-radius: 20px;
}

/* 申请表单 */
.form-tip {
  margin-bottom: 20px;
  border-radius: 10px;
}

/* 申请方式切换 */
.apply-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.apply-tab {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  border: 2px solid #e4e7ed;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s;
  color: #666;
  font-size: 14px;
  user-select: none;
}

.apply-tab:hover {
  border-color: #409eff;
  color: #409eff;
}

.apply-tab.active {
  border-color: #409eff;
  background: #ecf5ff;
  color: #409eff;
  font-weight: 600;
}

.tab-icon {
  font-size: 18px;
}

.realname-form {
  max-width: 440px;
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  border-radius: 8px;
}

/* 温馨提示 */
.tips-box {
  background: #f9fafb;
  border-radius: 10px;
  padding: 16px 20px;
  margin-top: 16px;
}

.tip-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin: 0 0 10px;
}

.tip-list {
  margin: 0;
  padding-left: 20px;
  color: #888;
  font-size: 13px;
  line-height: 2;
}

.tip-list li {
  margin: 0;
}
</style>
