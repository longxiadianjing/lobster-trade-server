<template>
  <div class="certification-container">
    <!-- 顶部导航 -->
    <div class="top-header">
      <div class="header-inner">
        <router-link to="/home" class="logo">🦞 龙虾道具交易平台</router-link>
        <div class="header-actions">
          <el-button @click="router.push({ path: '/user' })">返回个人中心</el-button>
        </div>
      </div>
    </div>

    <!-- 主内容 -->
    <div class="certification-main">
      <main class="content">
        <el-card class="cert-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">申请认证服务商</span>
            </div>
          </template>

          <el-alert
            v-if="currentCert"
            :title="certStatusText"
            :type="certStatusType"
            style="margin-bottom: 20px;"
            :closable="false"
          >
            <template v-if="currentCert.status === 2">
              <div>拒绝原因：{{ currentCert.rejectReason }}</div>
              <el-button type="danger" size="small" style="margin-top: 10px;" @click="reapply = true">重新申请</el-button>
            </template>
          </el-alert>

          <el-form v-if="showForm" ref="formRef" :model="form" :rules="rules" label-width="120px" size="large">
            <el-form-item label="认证类型" prop="certType">
              <el-radio-group v-model="form.certType">
                <el-radio value="boost">🎮 代练服务商</el-radio>
                <el-radio value="accompany">🎯 陪玩服务商</el-radio>
                <el-radio value="studio">🏢 工作室认证</el-radio>
              </el-radio-group>
              <div class="form-tip">认证通过后将在对应服务类型下展示认证标识</div>
            </el-form-item>

            <el-form-item label="主要游戏" prop="gameId">
              <el-select v-model="form.gameId" placeholder="请选择主要服务游戏" style="width: 300px;">
                <el-option v-for="game in games" :key="game.id" :label="game.gameName" :value="game.id" />
              </el-select>
            </el-form-item>

            <el-form-item label="服务区服" prop="regions">
              <el-input v-model="form.regions" placeholder="如：QQ区、微信区、渠道服" style="width: 400px;" />
              <div class="form-tip">多个区服用逗号分隔</div>
            </el-form-item>

            <el-form-item label="参考时价" prop="hourlyRate">
              <el-input v-model="form.hourlyRate" placeholder="如：100（表示100元/小时），或填"面议"" style="width: 400px;" />
              <div class="form-tip">填数字表示参考时价，或填"面议"由买家咨询后确认</div>
            </el-form-item>

            <el-form-item label="服务商等级">
              <el-radio-group v-model="form.providerLevel">
                <el-radio :value="1">普通服务商</el-radio>
                <el-radio :value="2">铜牌服务商</el-radio>
                <el-radio :value="3">银牌服务商</el-radio>
                <el-radio :value="4">金牌服务商</el-radio>
              </el-radio-group>
              <div class="form-tip">等级越高展示越靠前，审核通过后可升级</div>
            </el-form-item>

            <el-form-item label="服务描述" prop="description">
              <el-input v-model="form.description" type="textarea" :rows="4" placeholder="介绍你的代练/陪玩服务：擅长项目、经验、战绩、服务承诺等" style="width: 500px;" maxlength="500" show-word-limit />
            </el-form-item>

            <el-form-item label="资质证明">
              <el-upload
                ref="uploadRef"
                action=""
                :auto-upload="false"
                :show-file-list="true"
                :on-change="handleCredentialsChange"
                :file-list="credentialsFiles"
                list-type="picture-card"
                :on-remove="handleCredentialsRemove"
                accept="image/*"
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
              <div class="form-tip">上传资质证书、战绩截图等证明材料，支持多张图片</div>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="submitting" @click="handleSubmit">提交认证申请</el-button>
              <el-button @click="router.push({ path: '/user' })">取消</el-button>
            </el-form-item>
          </el-form>

          <div v-else-if="currentCert && currentCert.status === 0" class="pending-status">
            <el-icon :size="48" color="#E6A23C"><Clock /></el-icon>
            <h3>认证申请已提交</h3>
            <p>我们将在 1-3 个工作日内完成审核，请耐心等待</p>
            <p class="submit-time">提交时间：{{ formatTime(currentCert.submitTime) }}</p>
            <el-button type="primary" style="margin-top: 20px;" @click="router.push({ path: '/user' })">返回个人中心</el-button>
          </div>

          <div v-else-if="currentCert && currentCert.status === 1" class="certified-status">
            <el-icon :size="48" color="#67C23A"><CircleCheckFilled /></el-icon>
            <h3>您已是认证服务商</h3>
            <p>认证类型：{{ certTypeName }}</p>
            <p>有效期至：{{ formatTime(currentCert.expireTime) }}</p>
            <el-button type="primary" style="margin-top: 20px;" @click="router.push({ path: '/certification/providers' })">查看认证服务商列表</el-button>
          </div>
        </el-card>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Clock, CircleCheckFilled, Plus } from '@element-plus/icons-vue'
import { applyCertification, getMyCertification, getCertifiedProviders } from '@/api/certification'
import { getGameList } from '@/api/game'
import { uploadImage } from '@/api/product'

const router = useRouter()
const formRef = ref()
const uploadRef = ref()
const submitting = ref(false)
const reapply = ref(false)
const currentCert = ref(null)
const games = ref([])
const credentialsFiles = ref([])
const credentialsUrls = ref([])

const form = ref({
    certType: 'boost',
    gameId: null,
    regions: '',
    hourlyRate: '',
    providerLevel: 1,
    description: ''
})

const rules = {
    certType: [{ required: true, message: '请选择认证类型', trigger: 'change' }],
    gameId: [{ required: true, message: '请选择主要游戏', trigger: 'change' }],
    regions: [{ required: true, message: '请填写服务区服', trigger: 'blur' }],
    hourlyRate: [{ required: true, message: '请填写服务内容', trigger: 'blur' }],
    description: [{ required: true, message: '请填写服务描述', trigger: 'blur' }]
}

const showForm = computed(() => {
    if (reapply.value) return true
    if (!currentCert.value) return true
    if (currentCert.value && currentCert.value.status === 2) return true
    return false
})

const certStatusText = computed(() => {
    if (!currentCert.value) return ''
    const map = { 0: '待审核', 1: '已认证', 2: '未通过', 3: '已冻结' }
    return `当前认证状态：${map[currentCert.value.status] || ''}`
})

const certStatusType = computed(() => {
    if (!currentCert.value) return 'info'
    const map = { 0: 'warning', 1: 'success', 2: 'error', 3: 'warning' }
    return map[currentCert.value.status] || 'info'
})

const certTypeName = computed(() => {
    if (!currentCert.value) return ''
    const map = { boost: '代练服务商', accompany: '陪玩服务商', studio: '工作室' }
    return map[currentCert.value.certificationType] || currentCert.value.certificationType
})

const formatTime = (time) => {
    if (!time) return '-'
    return new Date(time).toLocaleString('zh-CN')
}

const handleCredentialsChange = async (file, fileList) => {
    credentialsFiles.value = fileList
}

const handleCredentialsRemove = (file, fileList) => {
    credentialsFiles.value = fileList
    credentialsUrls.value = credentialsUrls.value.filter(u => u !== file.url)
}

const handleSubmit = async () => {
    const valid = await formRef.value?.validate().catch(() => false)
    if (!valid) return

    submitting.value = true
    try {
        // 先上传图片
        const uploadedUrls = []
        for (const file of credentialsFiles.value) {
            if (file.url && file.url.startsWith('http')) {
                uploadedUrls.push(file.url)
            } else if (file.raw) {
                try {
                    const res = await uploadImage(file.raw)
                    if (res.data) {
                        uploadedUrls.push(res.data)
                    }
                } catch (e) {
                    console.error('图片上传失败', file.name, e)
                }
            }
        }

        await applyCertification({
            certType: form.value.certType,
            gameId: form.value.gameId,
            regions: form.value.regions,
            hourlyRate: form.value.hourlyRate,
            providerLevel: form.value.providerLevel,
            description: form.value.description,
            credentials: JSON.stringify(uploadedUrls)
        })
        ElMessage.success('认证申请已提交，请等待审核')
        const resp = await getMyCertification()
        currentCert.value = resp.data
        reapply.value = false
        credentialsFiles.value = []
        credentialsUrls.value = []
    } catch (e) {
        ElMessage.error(e.message || '提交失败')
    } finally {
        submitting.value = false
    }
}

onMounted(async () => {
    try {
        const [certResp, gamesResp] = await Promise.all([
            getMyCertification(),
            getGameList()
        ])
        currentCert.value = certResp.data
        games.value = gamesResp.data || []
    } catch (e) {
        console.error(e)
    }

  document.title = '申请认证 - 龙虾道具交易平台'
})
</script>

<style scoped>
.certification-container { min-height: 100vh; background: #f5f7fa; }
.top-header { background: #fff; border-bottom: 1px solid #eee; padding: 0; position: sticky; top: 0; z-index: 100; }
.header-inner { max-width: 1200px; margin: 0 auto; padding: 0 20px; height: 60px; display: flex; align-items: center; justify-content: space-between; }
.logo { font-size: 18px; font-weight: bold; color: #e54545; text-decoration: none; }
.certification-main { max-width: 900px; margin: 30px auto; padding: 0 20px; }
.cert-card { border-radius: 12px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-size: 18px; font-weight: bold; }
.form-tip { font-size: 12px; color: #999; margin-top: 4px; }
.pending-status, .certified-status { text-align: center; padding: 40px 0; }
.pending-status h3, .certified-status h3 { margin: 16px 0 8px; }
.pending-status p, .certified-status p { color: #666; margin: 4px 0; }
.submit-time { font-size: 13px; color: #999; }
:deep(.el-upload-list--picture-card) { display: flex; flex-wrap: wrap; gap: 8px; }
:deep(.el-upload--picture-card) { width: 80px; height: 80px; line-height: 80px; }
:deep(.el-upload-list__item) { width: 80px; height: 80px; }
</style>
