<template>
  <PageLayout>
    <template #sidebar>
      <el-card class="menu-card" shadow="never" :body-style="{ padding: '0' }">
        <template #header><span class="menu-title">🔐 账户安全</span></template>
        <el-menu :default-active="activeMenu" @select="onMenuSelect">
          <el-menu-item index="overview">
            <el-icon><Lock /></el-icon><span>安全概览</span>
          </el-menu-item>
          <el-menu-item index="devices">
            <el-icon><Monitor /></el-icon><span>可信设备</span>
          </el-menu-item>
          <el-menu-item index="login-history">
            <el-icon><Clock /></el-icon><span>登录记录</span>
          </el-menu-item>
        </el-menu>
      </el-card>
    </template>

    <div class="security-container">
      <!-- ========== 安全概览 ========== -->
      <div v-show="activeMenu === 'overview'">
        <h2 class="page-title">🔐 账户安全概览</h2>

        <!-- 安全评分卡片 -->
        <div class="score-card" :class="'score-' + securityData.level">
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
            <div class="score-title">
              安全等级：
              <span :class="'level-' + securityData.level">{{ securityData.level }}</span>
            </div>
            <div class="score-tip">{{ scoreTip }}</div>
            <div class="event-list">
              <div v-for="(ev, i) in securityData.events" :key="i" class="event-item">{{ ev }}</div>
            </div>
          </div>
        </div>

        <!-- 安全操作快捷入口 -->
        <el-card class="action-card" shadow="never">
          <template #header><span class="card-title">⚡ 安全操作</span></template>
          <div class="action-grid">
            <div class="action-item" @click="router.push({ path: '/user/set-pay-password' })">
              <div class="action-icon">🔑</div>
              <div class="action-name">{{ hasPayPassword ? '修改支付密码' : '设置支付密码' }}</div>
              <div class="action-desc">保障账户资金安全</div>
            </div>
            <div class="action-item" @click="router.push({ path: '/user/real-name-verify' })">
              <div class="action-icon">🪪</div>
              <div class="action-name">{{ hasRealName ? '已实名认证' : '立即实名认证' }}</div>
              <div class="action-desc">提升账户安全等级</div>
            </div>
            <div class="action-item" @click="switchTab('devices')">
              <div class="action-icon">📱</div>
              <div class="action-name">可信设备管理</div>
              <div class="action-desc">管理已登录设备</div>
            </div>
            <div class="action-item" @click="switchTab('login-history')">
              <div class="action-icon">📋</div>
              <div class="action-name">登录记录</div>
              <div class="action-desc">查看账号登录历史</div>
            </div>
          </div>
        </el-card>

        <!-- 异地登录提醒 -->
        <el-card class="alert-card" shadow="never">
          <template #header>
            <div class="card-header-flex">
              <span class="card-title">🔔 异地登录提醒</span>
              <el-switch v-model="remoteAlertEnabled" @change="saveAlertSetting" />
            </div>
          </template>
          <div class="alert-desc">
            开启后，当您的账号在新设备或新地区登录时，系统将通过站内信通知您。
          </div>
        </el-card>
      </div>

      <!-- ========== 可信设备 ========== -->
      <div v-show="activeMenu === 'devices'">
        <h2 class="page-title">📱 可信设备管理</h2>
        <el-card class="device-card" shadow="never">
          <template #header>
            <div class="card-header-flex">
              <span class="card-title">已登录设备列表</span>
              <el-button text type="primary" :icon="Refresh" @click="loadDevices">刷新</el-button>
            </div>
          </template>
          <el-table :data="devices" v-loading="deviceLoading" stripe>
            <el-table-column label="设备信息" min-width="200">
              <template #default="{ row }">
                <div class="device-name">
                  {{ row.deviceName || '未知设备' }}
                  <el-tag v-if="row.isCurrent === 1" type="primary" size="small" style="margin-left:6px">当前</el-tag>
                  <el-tag v-if="row.isTrusted === 1" type="success" size="small" style="margin-left:4px">可信</el-tag>
                </div>
                <div class="device-meta">
                  <span>{{ row.deviceType || 'PC' }}</span>
                  <span v-if="row.osVersion">· {{ row.osVersion }}</span>
                </div>
                <div class="device-ip">IP：{{ row.ipAddress || '-' }}</div>
              </template>
            </el-table-column>
            <el-table-column prop="loginLocation" label="登录地点" width="130" />
            <el-table-column label="最近活跃" width="160">
              <template #default="{ row }">{{ formatTime(row.lastActiveTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button
                  size="small"
                  :type="row.isTrusted === 1 ? 'warning' : 'success'"
                  :disabled="row.isCurrent === 1"
                  @click="toggleTrust(row)"
                >
                  {{ row.isTrusted === 1 ? '取消可信' : '设为可信' }}
                </el-button>
                <el-button
                  size="small"
                  type="danger"
                  :disabled="row.isCurrent === 1"
                  @click="removeDevice(row)"
                >
                  {{ row.isCurrent === 1 ? '当前设备' : '移除' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>

      <!-- ========== 登录记录 ========== -->
      <div v-show="activeMenu === 'login-history'">
        <h2 class="page-title">📋 登录记录</h2>
        <el-card class="history-card" shadow="never">
          <template #header>
            <div class="card-header-flex">
              <span class="card-title">最近50条登录记录</span>
              <el-button text type="primary" :icon="Refresh" @click="loadHistory">刷新</el-button>
            </div>
          </template>
          <el-table :data="loginHistory" v-loading="historyLoading" stripe>
            <el-table-column label="设备信息" min-width="200">
              <template #default="{ row }">
                <div class="device-name">{{ row.deviceName || '未知设备' }}</div>
                <div class="device-meta">
                  <span>{{ row.browserVersion || '' }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="ipAddress" label="IP地址" width="140" />
            <el-table-column prop="loginLocation" label="登录地点" width="130" />
            <el-table-column label="首次登录" width="160">
              <template #default="{ row }">{{ formatTime(row.firstLoginTime) }}</template>
            </el-table-column>
            <el-table-column label="最近活跃" width="160">
              <template #default="{ row }">{{ formatTime(row.lastActiveTime) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>
    </div>
  </PageLayout>
</template>

<script setup>
  document.title = '账户安全 - 龙虾道具交易平台'

  import { ref, computed, onMounted } from 'vue'
  import { useRouter } from 'vue-router'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { Refresh, Lock, Monitor, Clock } from '@element-plus/icons-vue'
  import request from '@/utils/request'
  import PageLayout from '@/components/PageLayout.vue'

  const router = useRouter()
  const activeMenu = ref('overview')
  const securityData = ref({ score: 0, level: '', events: [] })
  const devices = ref([])
  const loginHistory = ref([])
  const deviceLoading = ref(false)
  const historyLoading = ref(false)
  const hasPayPassword = ref(false)
  const hasRealName = ref(false)
  const remoteAlertEnabled = ref(true)

  // ---- 计算属性 ----
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

  // ---- 数据加载 ----
  const loadSecurity = async () => {
    try {
      const res = await request.get('/security/score')
      if (res.code === 200) {
        securityData.value = res.data || { score: 0, level: '', events: [] }
      }
    } catch (e) { console.error(e) }
  }

  const loadDevices = async () => {
    deviceLoading.value = true
    try {
      const res = await request.get('/security/devices')
      if (res.code === 200) devices.value = res.data || []
    } catch (e) { console.error(e) } finally { deviceLoading.value = false }
  }

  const loadHistory = async () => {
    historyLoading.value = true
    try {
      const res = await request.get('/security/login-history')
      if (res.code === 200) loginHistory.value = res.data || []
    } catch (e) { console.error(e) } finally { historyLoading.value = false }
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

  // ---- 操作 ----
  const toggleTrust = async (row) => {
    try {
      await ElMessageBox.confirm(
        row.isTrusted === 1 ? `取消"${row.deviceName}"的可信状态？` : `将该设备设为可信？`,
        '提示'
      )
      await request.post(`/security/devices/${row.id}/toggle-trust`)
      ElMessage.success('操作成功')
      loadDevices()
      loadSecurity()
    } catch (e) {
      if (e !== 'cancel') ElMessage.error('操作失败')
    }
  }

  const removeDevice = async (row) => {
    if (row.isCurrent === 1) {
      ElMessage.warning('当前设备无法移除')
      return
    }
    try {
      await ElMessageBox.confirm(
        `确认移除设备"${row.deviceName}"？移除后该设备需重新登录。`,
        '警告', { type: 'warning' }
      )
      await request.delete(`/security/devices/${row.id}`)
      ElMessage.success('设备已移除')
      loadDevices()
    } catch (e) {
      if (e !== 'cancel') ElMessage.error('移除失败')
    }
  }

  const saveAlertSetting = () => {
    ElMessage.success('设置已保存')
  }

  const formatTime = (t) => {
    if (!t) return '-'
    return t.replace('T', ' ').substring(0, 19)
  }

  const switchTab = (tab) => {
    activeMenu.value = tab
    if (tab === 'devices') loadDevices()
    if (tab === 'login-history') loadHistory()
  }

  const onMenuSelect = (index) => {
    switchTab(index)
  }

  onMounted(() => {
    loadSecurity()
    loadUserInfo()
  })
</script>

<style scoped>
.security-container { max-width: 900px; }

.page-title {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 20px;
}

/* 安全评分卡片 */
.score-card {
  display: flex;
  gap: 28px;
  align-items: center;
  background: #fff;
  border-radius: 14px;
  padding: 28px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  border-left: 5px solid transparent;
  transition: all 0.3s;
}
.score-card.score-高 { border-left-color: #67c23a; }
.score-card.score-中 { border-left-color: #f5a623; }
.score-card.score-低 { border-left-color: #f56c6c; }

.score-circle {
  width: 110px;
  height: 110px;
  position: relative;
  flex-shrink: 0;
}
.score-circle svg { transform: rotate(-90deg); width: 100%; height: 100%; }
.score-circle circle.bg { fill: none; stroke: #f0f0f0; stroke-width: 9; }
.score-circle circle.fg {
  fill: none;
  stroke-width: 9;
  stroke-linecap: round;
  transition: stroke-dasharray 0.6s ease;
}
.score-高 .fg { stroke: #67c23a; }
.score-中 .fg { stroke: #f5a623; }
.score-低 .fg { stroke: #f56c6c; }

.score-num {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  font-weight: 800;
  color: #333;
}

.score-title { font-size: 16px; font-weight: 700; margin-bottom: 6px; }
.level-高 { color: #67c23a; }
.level-中 { color: #f5a623; }
.level-低 { color: #f56c6c; }
.score-tip { font-size: 13px; color: #999; margin-bottom: 10px; }
.event-list { display: flex; flex-direction: column; gap: 4px; }
.event-item { font-size: 13px; color: #555; }

/* 安全操作卡片 */
.action-card, .alert-card, .device-card, .history-card {
  background: #fff;
  border-radius: 14px;
  border: none;
  margin-bottom: 20px;
}
.card-title { font-size: 15px; font-weight: 600; color: #333; }
.card-header-flex { display: flex; align-items: center; justify-content: space-between; }

.action-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.action-item {
  padding: 20px 16px;
  border-radius: 10px;
  background: #f9f8ff;
  cursor: pointer;
  text-align: center;
  transition: all 0.2s;
  border: 1px solid transparent;
}
.action-item:hover {
  border-color: #7c3aed;
  background: #f3eeff;
  transform: translateY(-2px);
}
.action-icon { font-size: 28px; margin-bottom: 8px; }
.action-name { font-size: 13px; font-weight: 700; color: #333; margin-bottom: 4px; }
.action-desc { font-size: 11px; color: #999; }

.alert-desc { font-size: 13px; color: #666; }

/* 设备列表 */
.device-name { font-weight: 600; color: #333; display: flex; align-items: center; flex-wrap: wrap; gap: 4px; }
.device-meta { font-size: 12px; color: #999; margin-top: 2px; }
.device-ip { font-size: 12px; color: #999; margin-top: 2px; }

/* 菜单卡片 */
.menu-card { border-radius: 14px; border: none; }
.menu-title { font-weight: 600; font-size: 14px; }

@media (max-width: 768px) {
  .score-card { flex-direction: column; text-align: center; }
  .action-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>