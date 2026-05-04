<template>
  <div class="order-input">
    <el-card shadow="never">
      <template #header>
        <div class="card-header-flex">
          <span class="card-title">手动录单</span>
          <el-tag type="info">粘贴订单信息，自动识别生成订单</el-tag>
        </div>
      </template>

      <el-row :gutter="24">
        <!-- 左侧：文本输入 -->
        <el-col :span="12">
          <el-form label-position="top">
            <el-form-item label="订单文本（粘贴即可）">
              <el-input
                v-model="orderText"
                type="textarea"
                :rows="12"
                placeholder="复制以下格式粘贴：

订单号：260423-482449877411343
订单内容：炫彩足球加巨兽机甲
服务端口：Q
角色名字：向来运气好
几格保险：9
联系方式：13812345678
订单来源：淘宝
段位多少：钻石"
                @input="onTextChange"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="recognizing" @click="handleRecognize">
                <el-icon v-if="!recognizing"><Operation /></el-icon>
                智能识别
              </el-button>
              <el-button @click="handleClear">清空</el-button>
            </el-form-item>
          </el-form>

          <el-divider />
          <el-alert v-if="recognized && recognized.confidence === 'low'" type="warning" show-icon :closable="false">
            识别置信度较低，请检查以下未能识别的字段并手动补充
          </el-alert>
          <el-alert v-if="recognized && recognized.confidence === 'high'" type="success" show-icon :closable="false">
            识别成功！请核对以下信息，确认无误后提交创建订单
          </el-alert>
        </el-col>

        <!-- 右侧：识别结果预览 -->
        <el-col :span="12">
          <div v-if="!recognized" class="preview-placeholder">
            <el-empty description="识别结果将显示在这里" />
          </div>

          <div v-else class="preview-result">
            <el-descriptions title="识别结果" :column="1" border>
              <el-descriptions-item label="订单号">
                <el-input v-model="form.orderNo" size="small" />
              </el-descriptions-item>
              <el-descriptions-item label="订单来源">
                <el-select v-model="form.orderSource" size="small" placeholder="选择来源" style="width: 100%;">
                  <el-option label="淘宝" value="淘宝" />
                  <el-option label="拼多多" value="拼多多" />
                  <el-option label="抖店" value="抖店" />
                  <el-option label="闲鱼" value="闲鱼" />
                  <el-option label="快手" value="快手" />
                  <el-option label="其他/个人" value="其他" />
                </el-select>
              </el-descriptions-item>
              <el-descriptions-item label="联系方式">
                <el-input v-model="form.contact" size="small" placeholder="手机号/微信/QQ" />
              </el-descriptions-item>
              <el-descriptions-item label="游戏">
                <el-select v-model="form.gameId" size="small" placeholder="选择游戏" style="width: 100%;">
                  <el-option v-for="g in games" :key="g.id" :label="g.gameName" :value="g.id" />
                </el-select>
              </el-descriptions-item>
              <el-descriptions-item label="交易类型">
                <el-select v-model="form.tradeType" size="small" style="width: 100%;">
                  <el-option label="装备/道具" value="goods" />
                  <el-option label="段位代练" value="boost" />
                  <el-option label="陪玩服务" value="accompany" />
                  <el-option label="护航服务" value="escort" />
                </el-select>
              </el-descriptions-item>
              <el-descriptions-item label="商品标题">
                <el-input v-model="form.productTitle" size="small" />
              </el-descriptions-item>
              <el-descriptions-item label="区服">
                <el-input v-model="form.gameZone" size="small" placeholder="如：Q(烽火)" />
              </el-descriptions-item>
              <el-descriptions-item label="角色名字">
                <el-input v-model="form.roleName" size="small" />
              </el-descriptions-item>
              <el-descriptions-item label="保险柜">
                <el-input v-model="form.safeBox" size="small" placeholder="如：9格" />
              </el-descriptions-item>
              <el-descriptions-item label="段位">
                <el-input v-model="form.rank" size="small" placeholder="如：钻石" />
              </el-descriptions-item>
              <el-descriptions-item label="订单金额(元)">
                <el-input-number v-model="form.orderAmount" size="small" :min="0" :precision="2" />
              </el-descriptions-item>
              <el-descriptions-item label="备注">
                <el-input v-model="form.remark" size="small" type="textarea" :rows="2" />
              </el-descriptions-item>
            </el-descriptions>

            <div style="margin-top: 20px; text-align: right;">
              <el-button type="success" size="large" :loading="submitting" @click="handleCreateOrder">
                确认创建订单
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 近期录单记录 -->
    <el-card shadow="never" style="margin-top: 16px;">
      <template #header>
        <div class="card-header-flex">
          <span class="card-title">近期录单记录</span>
        </div>
      </template>
      <el-table :data="recentRecords" stripe>
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column prop="productTitle" label="商品" min-width="200" />
        <el-table-column prop="gameName" label="游戏" width="120" />
        <el-table-column prop="roleName" label="角色" width="120" />
        <el-table-column prop="orderAmount" label="金额" width="100">
          <template #default="{ row }">
            <span class="price">¥{{ row.orderAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="录单时间" width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Operation } from '@element-plus/icons-vue'
import request from '@/utils/request'

const orderText = ref('')
const recognizing = ref(false)
const submitting = ref(false)
const recognized = ref(null)
const games = ref([])
const recentRecords = ref([])

const form = reactive({
  orderNo: '',
  gameId: null,
  tradeType: 'goods',
  productTitle: '',
  gameZone: '',
  roleName: '',
  safeBox: '',
  rank: '',
  contact: '',
  orderSource: '',
  orderAmount: 0,
  remark: ''
})

let recognizeTimer = null
const onTextChange = () => {
  if (recognizeTimer) clearTimeout(recognizeTimer)
  recognizeTimer = setTimeout(() => {
    if (orderText.value.trim().length > 10) handleRecognize()
  }, 1500)
}

const handleRecognize = async () => {
  if (!orderText.value.trim()) {
    ElMessage.warning('请先输入订单文本')
    return
  }
  recognizing.value = true
  try {
    const res = await request.post('/admin/order/recognize', { text: orderText.value })
    recognized.value = res.data?.recognized || null
    if (recognized.value) {
      const r = recognized.value
      form.orderNo = r.orderNo || ''
      form.productTitle = r.productTitle || ''
      form.gameZone = r.server || r.gameZone || ''
      form.roleName = r.roleName || ''
      form.safeBox = r.safeBox || ''
      form.rank = r.rank || ''
      form.contact = r.contact || ''
      form.orderSource = r.orderSource || ''
      form.tradeType = r.tradeType || 'goods'
      if (r.gameId) form.gameId = r.gameId
    }
  } catch (e) {
    ElMessage.error('识别失败：' + (e.message || '未知错误'))
  } finally {
    recognizing.value = false
  }
}

const handleClear = () => {
  orderText.value = ''
  recognized.value = null
  Object.assign(form, {
    orderNo: '', gameId: null, tradeType: 'goods', productTitle: '',
    gameZone: '', roleName: '', safeBox: '', rank: '', contact: '',
    orderSource: '', orderAmount: 0, remark: ''
  })
}

const handleCreateOrder = async () => {
  if (!form.orderNo) {
    ElMessage.warning('订单号不能为空')
    return
  }
  if (!form.orderAmount || form.orderAmount <= 0) {
    ElMessage.warning('请填写订单金额')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确认创建订单？\n\n订单号：${form.orderNo}\n商品：${form.productTitle}\n金额：¥${form.orderAmount}\n来源：${form.orderSource || '未填写'}`,
      '确认创建订单',
      { type: 'success', confirmButtonText: '确认创建' }
    )
  } catch {
    return
  }
  submitting.value = true
  try {
    await request.post('/admin/order/create', {
      orderNo: form.orderNo,
      productTitle: form.productTitle,
      tradeType: form.tradeType,
      gameId: form.gameId,
      gameZone: form.gameZone,
      roleName: form.roleName,
      safeBox: form.safeBox,
      rank: form.rank,
      orderAmount: form.orderAmount,
      contact: form.contact,
      orderSource: form.orderSource,
      remark: form.remark
    })
    ElMessage.success('订单创建成功')
    recentRecords.value.unshift({
      orderNo: form.orderNo,
      productTitle: form.productTitle,
      gameName: recognized.value?.gameName || '',
      roleName: form.roleName,
      orderAmount: form.orderAmount,
      createTime: new Date().toLocaleString('zh-CN')
    })
    handleClear()
  } catch (e) {
    ElMessage.error('创建失败：' + (e.message || '未知错误'))
  } finally {
    submitting.value = false
  }
}

// 加载游戏列表
const loadGames = async () => {
  try {
    const res = await request.get('/admin/games')
    games.value = res.data || []
  } catch (e) {
    games.value = []
  }
}

loadGames()
</script>

<style scoped>
.card-header-flex { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 10px; }
.card-title { font-size: 15px; font-weight: 600; color: #333; }
.preview-placeholder { padding: 40px 0; text-align: center; }
.preview-result { background: #f8f9fa; padding: 16px; border-radius: 8px; }
.price { font-weight: 600; color: #667eea; }
</style>
