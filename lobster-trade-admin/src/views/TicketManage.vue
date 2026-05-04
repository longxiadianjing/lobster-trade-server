<template>
  <div class="ticket-manage">
    <el-card shadow="never">
      <template #header>
        <div class="card-header-flex">
          <span class="card-title">客服工单</span>
          <el-tag type="info">仅展示工单记录，实际处理请在后台人工介入</el-tag>
        </div>
      </template>

      <div class="filter-row">
        <el-input v-model="filters.keyword" placeholder="搜索工单号/主题" style="width:200px" clearable @clear="loadData" @keyup.enter="loadData" />
        <el-select v-model="filters.category" placeholder="类型" style="width:140px" clearable @change="loadData">
          <el-option label="交易纠纷" :value="1" />
          <el-option label="账户问题" :value="2" />
          <el-option label="商品咨询" :value="3" />
          <el-option label="功能建议" :value="4" />
          <el-option label="其他" :value="5" />
        </el-select>
        <el-select v-model="filters.status" placeholder="状态" style="width:140px" clearable @change="loadData">
          <el-option label="待处理" :value="1" />
          <el-option label="处理中" :value="2" />
          <el-option label="待确认" :value="3" />
          <el-option label="已解决" :value="4" />
          <el-option label="已关闭" :value="5" />
          <el-option label="已驳回" :value="6" />
        </el-select>
        <el-button type="primary" @click="loadData">搜索</el-button>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-row">
        <el-statistic v-for="s in statsData" :key="s.label" :title="s.label" :value="s.value" :value-style="{ fontSize: '20px' }" />
      </div>

      <el-table :data="list" v-loading="loading" stripe class="mt-16">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="ticketNo" label="工单号" width="130" />
        <el-table-column prop="subject" label="主题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="category" label="类型" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="catTag(row.category)">{{ catText(row.category) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.priority === 1" type="danger" size="small">紧急</el-tag>
            <el-tag v-else-if="row.priority === 2" type="warning" size="small">高</el-tag>
            <span v-else-if="row.priority === 3" class="text-muted">中</span>
            <span v-else class="text-muted">低</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column prop="handlerName" label="处理人" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row)">详情</el-button>
            <el-button v-if="row.status <= 2" size="small" type="primary" @click="openReply(row)">处理</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="loadData"
        class="mt-16"
      />
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="工单详情" width="640px">
      <div v-if="current" class="ticket-detail">
        <div class="detail-row"><label>工单号：</label><span>{{ current.ticketNo }}</span></div>
        <div class="detail-row"><label>主题：</label><span>{{ current.subject }}</span></div>
        <div class="detail-row"><label>类型：</label><el-tag size="small">{{ catText(current.category) }}</el-tag></div>
        <div class="detail-row"><label>状态：</label><el-tag :type="statusTag(current.status)">{{ statusText(current.status) }}</el-tag></div>
        <div class="detail-row"><label>优先级：</label>
          <el-tag v-if="current.priority === 1" type="danger" size="small">紧急</el-tag>
          <el-tag v-else-if="current.priority === 2" type="warning" size="small">高</el-tag>
          <span v-else>{{ current.priority === 3 ? '中' : '低' }}</span>
        </div>
        <div class="detail-row"><label>用户ID：</label><span>{{ current.userId }}</span></div>
        <div class="detail-row"><label>处理人：</label><span>{{ current.handlerName || '-' }}</span></div>
        <div class="detail-row"><label>创建时间：</label><span>{{ current.createTime }}</span></div>
        <div class="detail-row"><label>最后回复：</label><span>{{ current.lastReplyTime || '-' }}</span></div>
        <div class="detail-section"><label>问题描述：</label><div class="desc-box">{{ current.description }}</div></div>
        <div v-if="current.handlerReply" class="detail-section"><label>处理回复：</label><div class="desc-box reply-box">{{ current.handlerReply }}</div></div>
        <div v-if="current.satisfaction" class="detail-row"><label>满意度：</label><el-rate v-model="current.satisfaction" disabled text-color="#ff9900" /></div>
        <div v-if="current.userFeedback" class="detail-row"><label>用户反馈：</label><span>{{ current.userFeedback }}</span></div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button v-if="current && current.status <= 2" type="primary" @click="openReply(current); detailVisible = false">立即处理</el-button>
      </template>
    </el-dialog>

    <!-- 处理弹窗 -->
    <el-dialog v-model="replyVisible" title="处理工单" width="520px" destroy-on-close>
      <el-form :model="replyForm" label-width="90px">
        <el-form-item label="处理动作">
          <el-select v-model="replyForm.action" style="width:100%">
            <el-option label="回复并待确认" :value="3" />
            <el-option label="标记已解决" :value="4" />
            <el-option label="驳打工单" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item label="回复内容">
          <el-input v-model="replyForm.reply" type="textarea" :rows="5" placeholder="请输入处理说明或回复内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="doReply">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const detailVisible = ref(false)
const replyVisible = ref(false)
const current = ref(null)
const statsData = ref([])
const filters = reactive({ keyword: '', category: '', status: '' })
const replyForm = reactive({ action: 3, reply: '' })

const catText = (v) => { const m = { 1: '交易纠纷', 2: '账户问题', 3: '商品咨询', 4: '功能建议', 5: '其他' }; return m[v] || '-' }
const catTag = (v) => { const m = { 1: 'danger', 2: 'warning', 3: 'success', 4: 'info', 5: '' }; return m[v] || '' }
const statusText = (v) => { const m = { 1: '待处理', 2: '处理中', 3: '待确认', 4: '已解决', 5: '已关闭', 6: '已驳回' }; return m[v] || '-' }
const statusTag = (v) => { const m = { 1: 'info', 2: 'primary', 3: 'warning', 4: 'success', 5: '', 6: 'danger' }; return m[v] || '' }

const loadStats = async () => {
  try {
    const res = await request.get('/admin/ticket/stats')
    const d = res.data || {}
    statsData.value = [
      { label: '总数', value: d.total || 0 },
      { label: '待处理', value: d.pending || 0 },
      { label: '处理中', value: d.processing || 0 },
      { label: '已解决', value: d.resolved || 0 },
      { label: '已关闭', value: d.closed || 0 }
    ]
  } catch (e) { /* handled */ }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/ticket/list', {
      params: { page: currentPage.value, size: pageSize.value, keyword: filters.keyword || undefined, category: filters.category || undefined, status: filters.status || undefined }
    })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) { /* handled */ }
  finally { loading.value = false }
}

const openDetail = (row) => {
  current.value = row
  detailVisible.value = true
}

const openReply = (row) => {
  current.value = row
  replyForm.action = 3
  replyForm.reply = ''
  replyVisible.value = true
}

const doReply = async () => {
  if (!replyForm.reply?.trim()) { ElMessage.warning('请输入回复内容'); return }
  submitting.value = true
  try {
    await request.put('/admin/ticket/handle', null, { params: { ticketId: current.value.id, reply: replyForm.reply, status: replyForm.action } })
    ElMessage.success('处理成功')
    replyVisible.value = false
    loadData()
    loadStats()
  } catch (e) { /* handled */ }
  finally { submitting.value = false }
}

onMounted(() => { loadData(); loadStats() })
</script>

<style scoped>
.ticket-manage { padding: 0; }
.card-header-flex { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-size: 15px; font-weight: 600; }
.filter-row { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
.stats-row { display: flex; gap: 32px; margin-top: 16px; padding: 12px 0; border-top: 1px solid #f0f0f0; border-bottom: 1px solid #f0f0f0; }
.mt-16 { margin-top: 16px; }
.text-muted { color: #999; font-size: 13px; }
.ticket-detail { display: flex; flex-direction: column; gap: 10px; }
.detail-row { display: flex; align-items: center; gap: 8px; font-size: 14px; }
.detail-row label { color: #888; min-width: 80px; }
.detail-section { display: flex; flex-direction: column; gap: 6px; font-size: 14px; }
.detail-section label { color: #888; }
.desc-box { background: #f9fafb; padding: 10px 12px; border-radius: 6px; font-size: 13px; line-height: 1.6; white-space: pre-wrap; }
.reply-box { background: #f0f7ff; border-left: 3px solid #667eea; }
</style>