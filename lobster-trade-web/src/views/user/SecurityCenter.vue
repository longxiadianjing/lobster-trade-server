<template>
  <PageLayout>
    <template #sidebar>
      <el-card class="menu-card" shadow="never" :body-style="{ padding: '0' }">
        <template #header>
          <span class="menu-title">🔐 安全中心</span>
        </template>
        <el-menu :default-active="route.path" router @select="() => {}">
          <el-menu-item index="/user">
            <el-icon><User /></el-icon>
            <span>个人信息</span>
          </el-menu-item>
          <el-menu-item index="/user/message">
            <el-icon><ChatDotRound /></el-icon>
            <span>消息中心</span>
          </el-menu-item>
          <el-menu-item index="/user/security-center">
            <el-icon><Lock /></el-icon>
            <span>安全中心</span>
          </el-menu-item>
          <el-menu-item index="/wallet">
            <el-icon><Wallet /></el-icon>
            <span>我的钱包</span>
          </el-menu-item>
        </el-menu>
      </el-card>
    </template>

    <div class="page-header">
      <h2>安全中心</h2>
    </div>

    <!-- 安全评分 -->
    <div class="score-card" :class="scoreClass">
      <div class="score-left">
        <div class="score-circle">
          <svg viewBox="0 0 100 100">
            <circle cx="50" cy="50" r="45" class="bg" />
            <circle cx="50" cy="50" r="45" class="fg" :stroke-dasharray="scoreDasharray" />
          </svg>
          <div class="score-num">{{ securityData.score }}</div>
        </div>
      </div>
      <div class="score-right">
        <div class="score-title">账户安全评级：<span :class="'level-' + securityData.level">{{ securityData.level }}</span></div>
        <div class="score-tip">{{ scoreTip }}</div>
        <div class="event-list">
          <div v-for="(ev, i) in securityData.events" :key="i" class="event-item">{{ ev }}</div>
        </div>
      </div>
    </div>

    <!-- 设备管理 -->
    <el-card class="device-card" shadow="never">
      <template #header>
        <div class="card-header-flex">
          <span class="card-title">登录设备管理</span>
          <el-button text type="primary" @click="loadDevices">刷新</el-button>
        </div>
      </template>
      <el-table :data="devices" v-loading="deviceLoading" stripe>
        <el-table-column label="设备信息" min-width="200">
          <template #default="{ row }">
            <div class="device-name">{{ row.deviceName || '未知设备' }}</div>
            <div class="device-ip">IP：{{ row.ipAddress || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="loginLocation" label="登录地点" width="130" />
        <el-table-column label="可信" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isTrusted === 1 ? 'success' : 'info'" size="small">
              {{ row.isTrusted === 1 ? '可信' : '普通' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastActiveTime" label="最近活跃" width="160">
          <template #default="{ row }">{{ formatTime(row.lastActiveTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button v-if="row.isTrusted !== 1" size="small" type="success" @click="toggleTrust(row)">设为可信</el-button>
            <el-button v-else size="small" type="warning" @click="toggleTrust(row)">取消可信</el-button>
            <el-button size="small" type="danger" @click="removeDevice(row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 安全设置 -->
    <el-card class="settings-card" shadow="never">
      <template #header>
        <span class="card-title">安全设置</span>
      </template>
      <div class="setting-item">
        <div class="setting-info">
          <div class="setting-name">支付密码</div>
          <div class="setting-desc">设置支付密码保障账户资金安全</div>
        </div>
        <el-button size="small" @click="router.push({ path: '/user/set-pay-password' })">
          {{ hasPayPassword ? '修改密码' : '立即设置' }}
        </el-button>
      </div>
      <div class="setting-item">
        <div class="setting-info">
          <div class="setting-name">实名认证</div>
          <div class="setting-desc">完成实名认证提升账户安全等级</div>
        </div>
        <el-button size="small" @click="router.push({ path: '/user/real-name-verify' })">
          {{ hasRealName ? '已认证' : '立即认证' }}
        </el-button>
      </div>
    </el-card>
  </PageLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import PageLayout from '@/components/PageLayout.vue'
import { User, Lock, ChatDotRound, Wallet } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const securityData = ref({ score: 0, level: '', events: [] })
const devices = ref([])
const deviceLoading = ref(false)
const hasPayPassword = ref(false)
const hasRealName = ref(false)

const scoreClass = computed(() => {
  const s = securityData.value.level
  if (s === '高') return 'score-high'
  if (s === '中') return 'score-mid'
  return 'score-low'
})

const scoreDasharray = computed(() => {
  const s = securityData.value.score || 0
  const circumference = 2 * Math.PI * 45
  return `${(s / 100) * circumference} ${circumference}`
})

const scoreTip = computed(() => {
  const s = securityData.value.score
  if (s >= 90) return '账户安全状态优秀，请继续保持'
  if (s >= 60) return '账户存在一定安全隐患，请尽快修复'
  return '账户存在严重安全隐患，请立即处理'
})

const formatTime = (t) => {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 19)
}

const loadSecurity = async () => {
  try {
    const res = await request.get('/security/score')
    if (res.code === 200) {
      securityData.value = res.data || { score: 0, level: '', events: [] }
    }
  } catch (e) {
    console.error(e)
  }
}

const loadDevices = async () => {
  deviceLoading.value = true
  try {
    const res = await request.get('/security/devices')
    if (res.code === 200) {
      devices.value = res.data || []
    }
  } catch (e) {
    console.error(e)
  } finally {
    deviceLoading.value = false
  }
}

const toggleTrust = async (row) => {
  try {
    await ElMessageBox.confirm(
      row.isTrusted === 1 ? `取消 "${row.deviceName}" 的可信状态？` : `将该设备设为可信？`,
      '提示'
    )
    await request.put(`/security/devices/${row.id}/trust`)
    ElMessage.success('操作成功')
    loadDevices()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const removeDevice = async (row) => {
  try {
    await ElMessageBox.confirm(`确认移除设备 "${row.deviceName}"？移除后该设备需重新登录。`, '警告', { type: 'warning' })
    await request.delete(`/security/devices/${row.id}`)
    ElMessage.success('已移除')
    loadDevices()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('移除失败')
  }
}

const loadUserInfo = async () => {
  try {
    const res = await request.get('/user/info')
    if (res.data) {
      hasPayPassword.value = !!(res.data.payPassword)
      hasRealName.value = res.data.realNameStatus === 1
    }
  } catch (e) {}
}

onMounted(() => {
  loadSecurity()
  loadDevices()
  loadUserInfo()

  document.title = '安全中心 - 龙虾道具交易平台'
})
</script>

<style scoped>
.security-center { padding: 0; max-width: 100%; }

.menu-card { border-radius: 12px; border: none; }
.menu-title { font-weight: 600; font-size: 14px; }

.page-header { margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 18px; font-weight: 600; }

.score-card {
  display: flex;
  gap: 24px;
  align-items: center;
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.score-card.score-high { border-left: 4px solid #67c23a; }
.score-card.score-mid { border-left: 4px solid #f5a623; }
.score-card.score-low { border-left: 4px solid #f56c6c; }

.score-circle {
  width: 100px;
  height: 100px;
  position: relative;
}
.score-circle svg {
  transform: rotate(-90deg);
  width: 100%;
  height: 100%;
}
.score-circle circle.bg { fill: none; stroke: #f0f0f0; stroke-width: 8; }
.score-circle circle.fg {
  fill: none;
  stroke-width: 8;
  stroke-linecap: round;
  transition: stroke-dasharray 0.5s;
}
.score-high .fg { stroke: #67c23a; }
.score-mid .fg { stroke: #f5a623; }
.score-low .fg { stroke: #f56c6c; }
.score-num {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 700;
  color: #333;
}

.score-title { font-size: 15px; font-weight: 600; margin-bottom: 6px; }
.level-高 { color: #67c23a; }
.level-中 { color: #f5a623; }
.level-低 { color: #f56c6c; }
.score-tip { font-size: 13px; color: #999; margin-bottom: 8px; }

.event-list { display: flex; flex-direction: column; gap: 4px; }
.event-item { font-size: 12px; color: #666; }

.device-card { background: #fff; border-radius: 12px; margin-bottom: 20px; }
.device-name { font-weight: 600; color: #333; }
.device-ip { font-size: 12px; color: #999; margin-top: 2px; }

.card-header-flex { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-size: 15px; font-weight: 600; color: #333; }

.settings-card { background: #fff; border-radius: 12px; }
.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}
.setting-item:last-child { border-bottom: none; }
.setting-name { font-size: 14px; font-weight: 600; color: #333; margin-bottom: 2px; }
.setting-desc { font-size: 12px; color: #999; }
</style>
