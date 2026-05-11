<template>
  <div class="payment-config">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">支付设置</span>
        </div>
      </template>

      <!-- 支付宝配置 -->
      <div class="config-section">
        <div class="section-title">
          <span>支付宝</span>
          <el-switch
            v-model="form.alipay_enabled"
            active-value="true"
            inactive-value="false"
            @change="handleSwitchChange('alipay_enabled', $event)"
          />
        </div>

        <el-form :model="form" label-width="140px" class="config-form">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="AppID">
                <el-input v-model="form.alipay_app_id" placeholder="支付宝应用AppID" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="沙箱模式">
                <el-switch
                  v-model="form.alipay_sandbox"
                  active-value="true"
                  inactive-value="false"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="应用私钥(RSA2)">
            <div class="secret-input">
              <el-input
                v-model="form.alipay_private_key"
                :type="showPrivateKey ? 'text' : 'password'"
                placeholder="支付宝应用私钥(RSA2 PKCS8)"
                style="flex:1"
              />
              <el-button text @click="showPrivateKey = !showPrivateKey" style="margin-left:8px">
                <el-icon><component :is="showPrivateKey ? 'Hide' : 'View'" /></el-icon>
              </el-button>
            </div>
            <div class="field-tip">私钥仅显示后4位，完整私钥请重新填写后保存</div>
          </el-form-item>

          <el-form-item label="支付宝公钥">
            <div class="secret-input">
              <el-input
                v-model="form.alipay_public_key"
                :type="showAlipayPubKey ? 'text' : 'password'"
                placeholder="支付宝公钥"
                style="flex:1"
              />
              <el-button text @click="showAlipayPubKey = !showAlipayPubKey" style="margin-left:8px">
                <el-icon><component :is="showAlipayPubKey ? 'Hide' : 'View'" /></el-icon>
              </el-button>
            </div>
          </el-form-item>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="异步回调地址">
                <el-input v-model="form.alipay_notify_url" placeholder="https://..." />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="同步回调地址">
                <el-input v-model="form.alipay_return_url" placeholder="https://..." />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>

      <el-divider />

      <!-- 微信支付配置 -->
      <div class="config-section">
        <div class="section-title">
          <span>微信支付</span>
          <el-switch
            v-model="form.wechat_enabled"
            active-value="true"
            inactive-value="false"
            @change="handleSwitchChange('wechat_enabled', $event)"
          />
        </div>

        <el-form :model="form" label-width="140px" class="config-form">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="AppID">
                <el-input v-model="form.wechat_app_id" placeholder="微信AppID" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="商户号">
                <el-input v-model="form.wechat_mch_id" placeholder="微信商户号" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="APIv3密钥">
            <div class="secret-input">
              <el-input
                v-model="form.wechat_api_v3_key"
                :type="showWechatV3 ? 'text' : 'password'"
                placeholder="微信APIv3密钥"
                style="flex:1"
              />
              <el-button text @click="showWechatV3 = !showWechatV3" style="margin-left:8px">
                <el-icon><component :is="showWechatV3 ? 'Hide' : 'View'" /></el-icon>
              </el-button>
            </div>
          </el-form-item>

          <el-form-item label="APIv2密钥(MD5)">
            <div class="secret-input">
              <el-input
                v-model="form.wechat_api_v2_key"
                :type="showWechatV2 ? 'text' : 'password'"
                placeholder="微信APIv2密钥(32位MD5)"
                style="flex:1"
              />
              <el-button text @click="showWechatV2 = !showWechatV2" style="margin-left:8px">
                <el-icon><component :is="showWechatV2 ? 'Hide' : 'View'" /></el-icon>
              </el-button>
            </div>
          </el-form-item>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="商户证书序列号">
                <el-input v-model="form.wechat_cert_serial_no" placeholder="证书序列号" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="异步回调地址">
                <el-input v-model="form.wechat_notify_url" placeholder="https://..." />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="商户证书内容">
            <el-input
              v-model="form.wechat_cert_content"
              type="textarea"
              :rows="4"
              placeholder="-----BEGIN CERTIFICATE-----\n...\n-----END CERTIFICATE-----"
            />
            <div class="field-tip">apiclient_cert.pem 文件内容，请保留完整格式</div>
          </el-form-item>
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
document.title = '支付设置 - 龙虾道具交易平台'
import { ref, reactive, onMounted } from 'vue'
import { Hide, View } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import adminRequest from '@/utils/adminRequest'

const loading = ref(false)
const saving = ref(false)
const showPrivateKey = ref(false)
const showAlipayPubKey = ref(false)
const showWechatV3 = ref(false)
const showWechatV2 = ref(false)

const form = reactive({
  alipay_app_id: '',
  alipay_private_key: '',
  alipay_public_key: '',
  alipay_notify_url: '',
  alipay_return_url: '',
  alipay_enabled: 'false',
  alipay_sandbox: 'false',
  wechat_app_id: '',
  wechat_mch_id: '',
  wechat_api_v3_key: '',
  wechat_api_v2_key: '',
  wechat_cert_serial_no: '',
  wechat_cert_content: '',
  wechat_notify_url: '',
  wechat_enabled: 'false'
})

const handleSwitchChange = (key, val) => {
  form[key] = val
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await adminRequest.get('/admin/payment-config')
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
    await adminRequest.put('/admin/payment-config', { ...form })
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
.payment-config { padding: 0; }
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
:deep(.el-divider) { margin: 24px 0 20px; }
</style>