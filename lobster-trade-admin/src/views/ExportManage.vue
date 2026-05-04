<template>
  <div class="export-manage">
    <el-card shadow="never">
      <template #header>
        <span class="card-title">数据导出</span>
      </template>

      <div class="export-grid">
        <el-card shadow="never" class="export-card">
          <template #header>
            <div class="export-card-header">
              <el-icon :size="32" color="#667eea"><Document /></el-icon>
              <div>
                <div class="export-card-title">订单数据导出</div>
                <div class="export-card-desc">导出订单记录为 CSV 文件，可用 Excel 打开</div>
              </div>
            </div>
          </template>
          <el-form :model="orderFilters" label-width="80px" size="small">
            <el-form-item label="订单状态">
              <el-select v-model="orderFilters.status" placeholder="全部" clearable style="width:100%">
                <el-option label="全部" value="" />
                <el-option label="待支付" value="pending_pay" />
                <el-option label="已支付" value="paid" />
                <el-option label="进行中" value="in_progress" />
                <el-option label="已提交" value="submitted" />
                <el-option label="已确认" value="confirmed" />
                <el-option label="已完成" value="completed" />
                <el-option label="申诉中" value="disputed" />
                <el-option label="已取消" value="cancelled" />
              </el-select>
            </el-form-item>
            <el-form-item label="交易类型">
              <el-select v-model="orderFilters.tradeType" placeholder="全部" clearable style="width:100%">
                <el-option label="全部" value="" />
                <el-option label="代练" value="boost" />
                <el-option label="陪玩" value="accompany" />
                <el-option label="护送" value="escort" />
                <el-option label="商品" value="goods" />
              </el-select>
            </el-form-item>
            <el-form-item label="关键词">
              <el-input v-model="orderFilters.keyword" placeholder="订单号/商品标题" clearable />
            </el-form-item>
          </el-form>
          <div class="export-action">
            <el-button type="primary" :loading="orderExporting" @click="exportOrders">
              <el-icon v-if="!orderExporting"><Download /></el-icon>
              {{ orderExporting ? '导出中...' : '导出 CSV' }}
            </el-button>
          </div>
        </el-card>

        <el-card shadow="never" class="export-card">
          <template #header>
            <div class="export-card-header">
              <el-icon :size="32" color="#52c41a"><User /></el-icon>
              <div>
                <div class="export-card-title">用户数据导出</div>
                <div class="export-card-desc">导出用户信息为 CSV 文件</div>
              </div>
            </div>
          </template>
          <el-form :model="userFilters" label-width="80px" size="small">
            <el-form-item label="账号状态">
              <el-select v-model="userFilters.status" placeholder="全部" clearable style="width:100%">
                <el-option label="全部" value="" />
                <el-option label="正常" value="1" />
                <el-option label="封禁" value="2" />
                <el-option label="冻结" value="3" />
              </el-select>
            </el-form-item>
            <el-form-item label="关键词">
              <el-input v-model="userFilters.keyword" placeholder="用户名/昵称/手机号" clearable />
            </el-form-item>
          </el-form>
          <div class="export-action">
            <el-button type="success" :loading="userExporting" @click="exportUsers">
              <el-icon v-if="!userExporting"><Download /></el-icon>
              {{ userExporting ? '导出中...' : '导出 CSV' }}
            </el-button>
          </div>
        </el-card>
      </div>

      <div class="export-tips">
        <el-alert type="info" :closable="false" show-icon>
          <template #title>
            <span>导出说明：每次最多导出 10,000 条记录，文件为 GBK 编码的 CSV 格式，用 Excel 打开时选择"自动检测编码"或手动选择 GB2312/GBK 编码</span>
          </template>
        </el-alert>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { Document, User, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const orderFilters = reactive({ status: '', tradeType: '', keyword: '' })
const userFilters = reactive({ status: '', keyword: '' })
const orderExporting = ref(false)
const userExporting = ref(false)

const buildQuery = (filters) => {
  const parts = []
  Object.entries(filters).forEach(([k, v]) => {
    if (v) parts.push(`${k}=${encodeURIComponent(v)}`)
  })
  return parts.join('&')
}

const exportOrders = () => {
  orderExporting.value = true
  const url = `/admin/export/orders?${buildQuery(orderFilters)}`
  window.open(url)
  setTimeout(() => { orderExporting.value = false; ElMessage.success('订单 CSV 已开始下载') }, 1500)
}

const exportUsers = () => {
  userExporting.value = true
  const url = `/admin/export/users?${buildQuery(userFilters)}`
  window.open(url)
  setTimeout(() => { userExporting.value = false; ElMessage.success('用户 CSV 已开始下载') }, 1500)
}
</script>

<style scoped>
.export-manage { padding: 0; height: 100%; display: flex; flex-direction: column; }
.card-title { font-size: 15px; font-weight: 600; }
.export-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 20px; flex: 1; }
.export-card { border: 1px solid #f0f0f0; display: flex; flex-direction: column; }
.export-card-header { display: flex; align-items: center; gap: 12px; }
.export-card-title { font-size: 15px; font-weight: 600; color: #333; }
.export-card-desc { font-size: 12px; color: #999; margin-top: 4px; }
.export-action { margin-top: auto; padding-top: 16px; }
.export-tips { margin-top: auto; }
</style>