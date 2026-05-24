<template>
  <div class="sms-config">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">短信设置</span>
        </div>
      </template>

      <!-- 阿里云短信配置 -->
      <div class="config-section">
        <div class="section-title">
          <span>阿里云短信</span>
          <el-switch
            v-model="form.aliyun_sms_enabled"
            active-value="true"
            inactive-value="false"
            @change="handleSwitchChange('aliyun_sms_enabled', $event)"
          />
        </div>

        <el-form :model="form" label-width="160px" class="config-form">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="AccessKeyId">
                <el-input v-model="form.aliyun_sms_access_key_id" placeholder="阿里云AccessKeyId" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="AccessKeySecret">
                <div class="secret-input">
                  <el-input
                    v-model="form.aliyun_sms_access_key_secret"
                    :type="showSecret ? 'text' : 'password'"
                    placeholder="阿里云AccessKeySecret"
                    style="flex:1"
                  />
                  <el-button text @click="showSecret = !showSecret" style="margin-left:8px">
                    <el-icon><component :is="showSecret ? 'Hide' : 'View'" /></el-icon>
                  </el-button>
                </div>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="短信签名">
            <el-input v-model="form.aliyun_sms_sign_name" placeholder="例如：龙虾道具交易平台" />
            <div class="field-tip">必须与阿里云控制台审核通过的签名一致</div>
          </el-form-item>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="模板CODE">
                <el-input v-model="form.aliyun_sms_template_code" placeholder="例如：SMS_xxxxxxx" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="模板参数JSON">
                <el-input
                  v-model="form.aliyun_sms_template_param_json"
                  placeholder='例如：{"code":"${code}"}'
                />
                <div class="field-tip">变量用 JSON 格式填写，变量名对应模板中的占位符</div>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>

      <div class="form-footer">
        <el-button @click="loadData">重置</el-button>
        <el-button type="primary" :loading="saving" @click="doSave">保存配置</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
document.title = '短信设置 - 龙虾道具交易平台'
import { ref, reactive, onMounted } from 'vue'
import { Hide, View } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import adminRequest from '@/utils/adminRequest'

const loading = ref(false)
const saving = ref(false)
const showSecret = ref(false)

const form = reactive({
  aliyun_sms_access_key_id: '',
  aliyun_sms_access_key_secret: '',
  aliyun_sms_sign_name: '',
  aliyun_sms_template_code: '',
  aliyun_sms_template_param_json: '',
  aliyun_sms_enabled: 'false'
})

const handleSwitchChange = (key, val) => {
  form[key] = val
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await adminRequest.get('/admin/sms-config')
    const data = res.data || {}
    Object.keys(form).forEach(key => {
      if (data[key] !== undefined && data[key] !== null) {
        form[key] = data[key]
      }
    })
  } catch (e) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

const doSave = async () => {
  saving.value = true
  try {
    await adminRequest.put('/admin/sms-config', { ...form })
    ElMessage.success('保存成功')
    loadData()
  } catch (e) {
    // handled by interceptor
  } finally {
    saving.value = false
  }
}

onMounted(() => { loadData() })
</script>

<style scoped>
.sms-config { padding: 0; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-size: 15px; font-weight: 600; }

.config-section { padding: 8px 0; }
.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #333;
}

.config-form { max-width: 900px; }

.secret-input { display: flex; align-items: center; width: 100%; }

.field-tip { font-size: 12px; color: #999; margin-top: 4px; line-height: 1.4; }

.form-footer {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-form-item) { margin-bottom: 18px; }
</style>