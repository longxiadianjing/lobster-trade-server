<template>
  <div class="user-container" data-theme="business">
    <!-- 顶部导航 -->
    <div class="top-header">
      <div class="header-inner">
        <router-link to="/home" class="logo">🦞 龙虾道具交易平台</router-link>
        <div class="header-actions">
          <el-button @click="router.push({ path: '/order/center' })">
            <el-icon><List /></el-icon>
            我的订单
          </el-button>
          <el-button @click="handleLogout">退出登录</el-button>
        </div>
      </div>
    </div>

    <!-- 主内容 -->
    <div class="user-main">
      <!-- 左侧边栏 -->
      <aside class="sidebar">
        <div class="profile-card" @click="showAvatarPicker = true">
          <div class="avatar-wrap">
            <el-avatar :size="72" class="avatar" :src="userInfo?.avatar || undefined">
              <img v-if="userInfo?.avatar" :src="userInfo.avatar" style="width:100%;height:100%;border-radius:50%;object-fit:cover;" />
              <span v-else>{{ userInfo?.nickname?.charAt(0) || 'U' }}</span>
            </el-avatar>
            <div class="avatar-edit-icon">✏️</div>
            <div class="level-badge">V{{ userInfo?.user_level || 1 }}</div>
          </div>
          <h3 class="nickname">{{ userInfo?.nickname || '用户' }}</h3>
          <p class="user-id">ID: {{ userInfo?.id || '-' }}</p>
          <div class="auth-status">
            <el-tag v-if="userInfo?.real_name_status === 1" type="success" size="small" effect="plain">
              <el-icon><CircleCheckFilled /></el-icon> 已实名
            </el-tag>
            <el-tag v-else-if="userInfo?.real_name_status === 2" type="warning" size="small" effect="plain">审核中</el-tag>
            <el-tag v-else type="info" size="small" effect="plain">未实名</el-tag>
          </div>
        </div>

        <!-- 快捷统计 -->
        <div class="stats-card">
          <div class="stats-title-row">
            <span class="stats-title">📊 核心指标</span>
          </div>
          <div class="stats-grid">
            <div class="stat-item highlight" @click="router.push({ path: '/order/center', query: { role: 'buyer' } })">
              <span class="stat-value">{{ userInfo?.total_trade_count || 0 }}</span>
              <span class="stat-label">交易订单</span>
            </div>
            <div class="stat-item highlight" @click="router.push({ path: '/wallet' })">
              <span class="stat-value green">¥{{ (walletStore.balance || '0.00') }}</span>
              <span class="stat-label">账户余额</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ userInfo?.product_count || 0 }}</span>
              <span class="stat-label">发布商品</span>
            </div>
            <div class="stat-item">
              <span class="stat-value orange">{{ userInfo?.publishing_count || 0 }}</span>
              <span class="stat-label">发布中</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ userInfo?.sold_count || 0 }}</span>
              <span class="stat-label">已售出</span>
            </div>
            <div class="stat-item">
              <span class="stat-value purple">¥{{ userInfo?.total_revenue || '0.00' }}</span>
              <span class="stat-label">累计收入</span>
            </div>
            <div class="stat-item" @click="router.push('/order/center?status=pending')">
              <span class="stat-value red">{{ userInfo?.pending_count || 0 }}</span>
              <span class="stat-label">进行中</span>
            </div>
            <div class="stat-item" @click="router.push('/user/message')">
              <span class="stat-value">{{ unreadCount }}</span>
              <span class="stat-label">未读消息</span>
            </div>
          </div>
        </div>

        <el-card class="menu-card" shadow="never">
          <template #header>
            <span class="menu-title">快捷服务</span>
          </template>
          <el-menu :default-active="activeMenu" router @select="activeMenu = $event">
            <el-menu-item index="/user">
              <el-icon><User /></el-icon>
              <span>个人信息</span>
            </el-menu-item>
            <el-menu-item index="/user/message">
              <el-icon><ChatDotRound /></el-icon>
              <span>消息中心</span>
            </el-menu-item>
            <el-menu-item index="/user/favorites">
              <el-icon><Star /></el-icon>
              <span>我的收藏</span>
            </el-menu-item>
            <el-menu-item index="/user/security-center">
              <el-icon><Lock /></el-icon>
              <span>安全中心</span>
            </el-menu-item>
            <el-sub-menu index="theme-switch">
              <template #title>
                <span>&#127912; 切换主题</span>
              </template>
              <el-menu-item v-for="t in themes" :key="t.id" :index="t.id" @click="switchTheme(t.id)">
                <span style="display:inline-block;width:8px;height:8px;border-radius:50%;background:var(--primary);margin-right:8px;"></span>
                <span style="font-size:13px;">{{ t.name }}</span>
                <span v-if="currentTheme === t.id" style="margin-left:auto;color:var(--primary);font-size:11px;">&#10003;</span>
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item index="/user/coupon-center">
              <el-icon><Ticket /></el-icon>
              <span>优惠券</span>
            </el-menu-item>
            <el-menu-item index="/wallet">
              <el-icon><Wallet /></el-icon>
              <span>我的钱包</span>
            </el-menu-item>
            <el-menu-item index="/product/list">
              <el-icon><Goods /></el-icon>
              <span>商品列表</span>
            </el-menu-item>
            <el-menu-item index="/order/center">
              <el-icon><List /></el-icon>
              <span>订单中心</span>
            </el-menu-item>
            <el-menu-item index="/product/publish">
              <el-icon><Sell /></el-icon>
              <span>发布商品</span>
            </el-menu-item>
            <el-menu-item index="/im">
              <el-icon><ChatLineSquare /></el-icon>
              <span>我的消息</span>
            </el-menu-item>
            <el-menu-item index="/user/real-name-verify" v-if="userInfo?.real_name_status !== 1">
              <el-icon><CircleCheck /></el-icon>
              <span>实名认证</span>
            </el-menu-item>
            <el-menu-item index="/certification/apply">
              <el-icon><Medal /></el-icon>
              <span>服务商认证</span>
            </el-menu-item>
            <el-menu-item index="/certification/providers">
              <el-icon><DataAnalysis /></el-icon>
              <span>认证服务商</span>
            </el-menu-item>
          </el-menu>
        </el-card>
      </aside>

      <!-- 右侧内容 -->
      <main class="content">
        <!-- 个人信息 -->
        <el-card class="info-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">个人信息</span>
              <el-button v-if="!editing" type="primary" plain size="small" @click="startEdit">
                编辑资料
              </el-button>
              <template v-else>
                <el-button type="primary" size="small" :loading="saveLoading" @click="handleSave">保存</el-button>
                <el-button size="small" @click="cancelEdit">取消</el-button>
              </template>
            </div>
          </template>

          <div class="info-grid">
            <div class="info-row">
              <span class="info-label">用户ID</span>
              <span class="info-value">{{ userInfo?.id || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">用户名</span>
              <span class="info-value">{{ userInfo?.username || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">昵称</span>
              <span class="info-value" v-if="!editing">{{ userInfo?.nickname || '-' }}</span>
              <el-input v-else v-model="form.nickname" size="small" style="width: 160px;" />
            </div>
            <div class="info-row">
              <span class="info-label">手机号</span>
              <span class="info-value">
                {{ userInfo?.phone ? userInfo.phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2') : '-' }}
              </span>
            </div>
            <div class="info-row">
              <span class="info-label">用户等级</span>
              <span class="info-value">
                <el-tag type="primary" size="small">V{{ userInfo?.user_level || 1 }}</el-tag>
              </span>
            </div>
            <div class="info-row">
              <span class="info-label">信誉评分</span>
              <span class="info-value">
                <el-rate :model-value="Number(userInfo?.reputation_score) || 5" disabled show-score size="small" />
              </span>
            </div>
            <div class="info-row">
              <span class="info-label">注册时间</span>
              <span class="info-value">{{ formatTime(userInfo?.create_time) || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">累计交易</span>
              <span class="info-value">{{ userInfo?.total_trade_count || 0 }} 笔</span>
            </div>
          </div>
        </el-card>

        <!-- 实名认证 -->
        <el-card class="info-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">实名认证</span>
              <el-tag v-if="userInfo?.real_name_status === 1" type="success" size="small" effect="plain">已认证</el-tag>
              <el-tag v-else-if="userInfo?.real_name_status === 2" type="warning" size="small" effect="plain">审核中</el-tag>
            </div>
          </template>

          <div v-if="userInfo?.real_name_status === 1" class="status-block success">
            <div class="status-icon-wrap success">
              <el-icon :size="28"><CircleCheckFilled /></el-icon>
            </div>
            <div class="status-text">
              <p class="status-title">已实名认证</p>
              <p class="status-desc">姓名：{{ maskedRealName }} · 认证时间：{{ formatDate(userInfo?.real_name_time) }}</p>
            </div>
          </div>

          <div v-else-if="userInfo?.real_name_status === 2" class="status-block warning">
            <div class="status-icon-wrap warning">
              <el-icon :size="28"><Clock /></el-icon>
            </div>
            <div class="status-text">
              <p class="status-title">实名认证审核中</p>
              <p class="status-desc">预计1-3个工作日内完成审核，请耐心等待</p>
            </div>
          </div>

          <div v-else class="status-block">
            <div class="status-icon-wrap info">
              <el-icon :size="28"><Warning /></el-icon>
            </div>
            <div class="status-text">
              <p class="status-title">未进行实名认证</p>
              <p class="status-desc">完成实名认证后可进行交易，提升账户安全性</p>
            </div>
            <el-button type="primary" @click="router.push({ path: '/user/real-name-verify' })">
              立即认证
            </el-button>
          </div>
        </el-card>

        <!-- 账号安全 -->
        <el-card class="info-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">账号安全</span>
              <span class="security-score">
                安全评分：
                <el-rate :model-value="securityScore" disabled size="small" style="display:inline-flex;vertical-align:middle;" />
                <span class="score-num">{{ securityScore * 20 }}分</span>
              </span>
            </div>
          </template>

          <div class="security-list">
            <div class="security-item">
              <div class="security-info">
                <div class="security-icon-wrap">
                  <el-icon><Lock /></el-icon>
                </div>
                <div>
                  <p class="security-title">登录密码</p>
                  <p class="security-desc">定期更换密码可提升账号安全，建议90天更换一次</p>
                </div>
              </div>
              <el-button size="small" type="primary" plain>修改</el-button>
            </div>

            <el-divider class="inner-divider" />

            <div class="security-item">
              <div class="security-info">
                <div class="security-icon-wrap">
                  <el-icon><Coin /></el-icon>
                </div>
                <div>
                  <p class="security-title">支付密码</p>
                  <p class="security-desc">
                    {{ userInfo?.pay_password_set ? '已设置 · 用于提现、大额交易' : '未设置 · 设置后可进行提现操作' }}
                  </p>
                </div>
              </div>
              <el-button size="small" type="primary" plain @click="router.push({ path: '/user/set-pay-password' })">
                {{ userInfo?.pay_password_set ? '修改' : '设置' }}
              </el-button>
            </div>

            <el-divider class="inner-divider" />

            <div class="security-item">
              <div class="security-info">
                <div class="security-icon-wrap">
                  <el-icon><Message /></el-icon>
                </div>
                <div>
                  <p class="security-title">绑定手机</p>
                  <p class="security-desc">{{ userInfo?.phone ? userInfo.phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2') : '未绑定' }}</p>
                </div>
              </div>
              <el-tag type="success" size="small" effect="plain" v-if="userInfo?.phone">已绑定</el-tag>
              <el-tag type="info" size="small" effect="plain" v-else>未绑定</el-tag>
            </div>

            <el-divider class="inner-divider" />

            <div class="security-item">
              <div class="security-info">
                <div class="security-icon-wrap">
                  <el-icon><CircleCheck /></el-icon>
                </div>
                <div>
                  <p class="security-title">实名认证</p>
                  <p class="security-desc">
                    {{ userInfo?.real_name_status === 1 ? '已认证 · 交易额度不受限制' : userInfo?.real_name_status === 2 ? '审核中' : '未认证 · 认证后解锁全部功能' }}
                  </p>
                </div>
              </div>
              <el-tag :type="userInfo?.real_name_status === 1 ? 'success' : userInfo?.real_name_status === 2 ? 'warning' : 'info'" size="small" effect="plain">
                {{ userInfo?.real_name_status === 1 ? '已认证' : userInfo?.real_name_status === 2 ? '审核中' : '未认证' }}
              </el-tag>
            </div>
          </div>
        </el-card>
        <!-- 消息通知 -->
        <el-card class="info-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">📬 消息通知</span>
              <span class="unread-badge" v-if="unreadCount > 0">{{ unreadCount }} 条未读</span>
            </div>
          </template>
          <div v-if="loadingNotifications" class="loading-wrap">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>加载中...</span>
          </div>
          <div v-else class="notification-list">
            <div
              class="notif-item"
              v-for="n in notifications"
              :key="n.id"
              :class="{ unread: n.status === 0 }"
              @click="handleNotifClick(n)"
            >
              <span class="notif-icon">{{ getNotifIcon(n.type) }}</span>
              <div class="notif-body">
                <div class="notif-title">{{ n.title }}</div>
                <div class="notif-desc">{{ n.content }}</div>
              </div>
              <div class="notif-right">
                <div class="notif-time">{{ formatNotifTime(n.createTime) }}</div>
                <div v-if="n.status === 0" class="unread-dot"></div>
              </div>
            </div>
            <el-empty v-if="notifications.length === 0" description="暂无消息" :image-size="80" />
          </div>
        </el-card>
      </main>
    </div>

    <!-- 头像选择弹窗 -->
    <el-dialog v-model="showAvatarPicker" title="选择头像" width="480px" :close-on-click-modal="true">
      <div class="avatar-picker-grid">
        <div
          v-for="av in presetAvatars"
          :key="av"
          class="avatar-option"
          :class="{ selected: selectedAvatar === av }"
          @click="selectedAvatar = av"
        >
          <img :src="av" />
        </div>
      </div>
      <div class="avatar-upload-row">
        <el-upload
          class="avatar-uploader"
          :show-file-list="false"
          :before-upload="beforeAvatarUpload"
          :http-request="uploadAvatar"
          accept="image/*"
        >
          <el-button size="small" type="primary">上传自定义头像</el-button>
        </el-upload>
      </div>
      <template #footer>
        <el-button @click="showAvatarPicker = false">取消</el-button>
        <el-button type="primary" :loading="avatarSaving" @click="saveAvatar">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted, markRaw } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useWalletStore } from '@/stores/wallet'
import { updateUserInfo } from '@/api/user'
import { getNotificationList, markNotificationRead } from '@/api/notification'
import {
  User, Wallet, List, Goods, Sell, CircleCheck, CircleCheckFilled,
  Clock, Lock, Coin, Message, Warning, ChatDotRound, Ticket, Medal, DataAnalysis,
  ChatLineSquare, Star, Loading, Bell, ShoppingCart, Headset
} from '@element-plus/icons-vue'
import { useTheme } from '@/composables/useTheme'
import ThemeSwitcher from '@/components/ThemeSwitcher/index.vue'

const router = useRouter()
const userStore = useUserStore()
const walletStore = useWalletStore()
const { themes, switchTheme, currentTheme } = useTheme()

const activeMenu = ref('/user')
const editing = ref(false)
const saveLoading = ref(false)
const loadingNotifications = ref(false)

const form = reactive({ nickname: '', email: '' })
const userInfo = computed(() => userStore.userInfo)
const unreadCount = computed(() => userStore.unreadNotificationCount || 0)

const notifications = ref([])

// 头像选择相关
const showAvatarPicker = ref(false)
const selectedAvatar = ref('')
const avatarSaving = ref(false)
const presetAvatars = [
  '/avatars/lobster.png',
  '/avatars/fox.png',
  '/avatars/cat.png',
  '/avatars/bear.png',
  '/avatars/panda.png',
  '/avatars/owl.png',
  '/avatars/rabbit.png'
]

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) { ElMessage.error('只能上传图片文件'); return false }
  if (!isLt2M) { ElMessage.error('图片大小不能超过 2MB'); return false }
  return true
}

const uploadAvatar = async ({ file }) => {
  const formData = new FormData()
  formData.append('avatar', file)
  try {
    const res = await fetch('/api/user/avatar', { method: 'POST', body: formData, headers: { 'Authorization': 'Bearer ' + userStore.token } })
    const data = await res.json()
    if (data.code === 0 || data.code === 200) {
      selectedAvatar.value = data.data
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error(data.message || '上传失败')
    }
  } catch (e) {
    ElMessage.error('上传失败')
  }
}

const saveAvatar = async () => {
  if (!selectedAvatar.value) { ElMessage.warning('请选择头像'); return }
  avatarSaving.value = true
  try {
    await updateUserInfo({ avatar: selectedAvatar.value })
    await userStore.fetchUserInfo()
    showAvatarPicker.value = false
    ElMessage.success('头像更新成功')
  } catch (e) {
    ElMessage.error('更新失败')
  } finally {
    avatarSaving.value = false
  }
}

const typeToIcon = (type) => {
  if (type === 1) return '🔔'
  if (type === 2) return '📦'
  if (type === 3) return '💬'
  if (type === 4) return '🎁'
  return '🔔'
}

const getNotifIcon = (type) => typeToIcon(type)

const formatNotifTime = (time) => {
  if (!time) return ''
  let d
  if (typeof time === 'string') {
    d = new Date(time.replace('T', ' ').substring(0, 19))
  } else {
    d = new Date(time)
  }
  const now = new Date()
  const diff = now - d
  if (diff < 60 * 1000) return '刚刚'
  if (diff < 60 * 60 * 1000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 24 * 60 * 60 * 1000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 2 * 24 * 60 * 60 * 1000) return '昨天'
  if (diff < 7 * 24 * 60 * 60 * 1000) return `${Math.floor(diff / 86400000)}天前`
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const fetchNotifications = async () => {
  loadingNotifications.value = true
  try {
    const res = await getNotificationList({ page: 1, size: 20 })
    notifications.value = res.data?.records || []
  } catch (e) {
    console.error('fetchNotifications error', e)
  } finally {
    loadingNotifications.value = false
  }
}

const handleNotifClick = async (n) => {
  if (n.status === 0) {
    try {
      await markNotificationRead(n.id)
      n.status = 1
      userStore.setUnreadCount(Math.max(0, userStore.unreadNotificationCount - 1))
    } catch (e) {
      console.error(e)
    }
  }
  if (n.linkUrl) {
    router.push(n.linkUrl)
  }
}

const maskedRealName = computed(() => {
  const name = userInfo.value?.real_name
  return name ? name.charAt(0) + '**' : ''
})

const securityScore = computed(() => {
  let score = 0
  if (userInfo.value?.phone) score += 1
  if (userInfo.value?.pay_password_set) score += 1
  if (userInfo.value?.real_name_status === 1) score += 1
  if (userInfo.value?.nickname) score += 1
  return Math.max(score, 1)
})

const formatTime = (time) => {
  if (!time) return ''
  const d = new Date(time)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const formatDate = (time) => {
  if (!time) return ''
  const d = new Date(time)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

onMounted(async () => {
  document.title = '个人中心 - 龙虾道具交易平台'
  try {
    await userStore.fetchUserInfo()
    await walletStore.fetchWalletInfo()
    form.nickname = userInfo.value?.nickname || ''
    form.email = userInfo.value?.email || ''
  } catch (e) {
    console.error('Failed to fetch user info:', e)
  }
  userStore.fetchUnreadCount()
  fetchNotifications()
})

const startEdit = () => {
  form.nickname = userInfo.value?.nickname || ''
  form.email = userInfo.value?.email || ''
  editing.value = true
}

const cancelEdit = () => { editing.value = false }

const handleSave = async () => {
  saveLoading.value = true
  try {
    await updateUserInfo({ nickname: form.nickname, email: form.email })
    await userStore.fetchUserInfo()
    ElMessage.success('更新成功')
    editing.value = false
  } catch (e) {
    ElMessage.error('更新失败')
  } finally {
    saveLoading.value = false
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push({ path: '/login' })
  } catch {}
}
</script>

<style scoped>
/* ========== 整体容器 ========== */
.user-container {
  min-height: 100vh;
  background: #ffffff;
}

/* ========== 顶部导航 ========== */
.top-header {
  background: linear-gradient(135deg, #1a1a2e, #16213e);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.25);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 24px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  text-decoration: none;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.header-actions .el-button {
  background: rgba(255,255,255,0.1);
  border: 1px solid rgba(255,255,255,0.2);
  color: #fff;
}

.header-actions .el-button:hover {
  background: rgba(255,255,255,0.2);
}

/* ========== 主布局 ========== */
.user-main {
  max-width: 1280px;
  margin: 0 auto;
  padding: 24px;
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

/* ========== 左侧边栏 ========== */
.sidebar {
  width: 240px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: sticky;
  top: 84px;
}

/* 头像卡片 */
.profile-card {
  background: #ffffff;
  border: 1px solid #e2e6ed;
  border-radius: 14px;
  padding: 28px 20px 20px;
  text-align: center;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
  color: #1a1a1a;
}

.profile-card .nickname,
.profile-card .user-id,
.profile-card .auth-status .el-tag {
  color: #1a1a1a;
}
.profile-card .user-id { color: #555; }
.profile-card .auth-status .el-tag { background: rgba(91,71,194,0.1); border-color: rgba(91,71,194,0.2); color: #667eea; }

.avatar-wrap {
  position: relative;
  display: inline-block;
}

.avatar {
  background: rgba(255,255,255,0.25);
  color: #fff;
  font-size: 28px;
  font-weight: 600;
  border: 3px solid rgba(255,255,255,0.5);
}

.level-badge {
  position: absolute;
  bottom: -4px;
  right: -4px;
  background: #f5a623;
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 10px;
  border: 2px solid #fff;
}

.nickname {
  margin: 14px 0 4px;
  font-size: 17px;
  font-weight: 600;
  color: #fff;
  word-break: break-all;
}

.user-id {
  margin: 0 0 10px;
  font-size: 12px;
  color: rgba(255,255,255,0.7);
}

.auth-status {
  display: flex;
  justify-content: center;
}

.auth-status .el-tag {
  background: rgba(255,255,255,0.2);
  border-color: rgba(255,255,255,0.3);
  color: #fff;
}

/* 统计卡片 */
.stats-card {
  background: #ffffff;
  border: 1px solid #e2e6ed;
  border-radius: 14px;
  padding: 16px 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.stat-item {
  text-align: center;
  padding: 10px 4px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.stat-item:hover {
  background: rgba(91,71,194,0.06);
}

.stat-value {
  display: block;
  font-size: 18px;
  font-weight: 700;
  color: var(--theme-text-primary);
  margin-bottom: 4px;
}

.stat-label {
  display: block;
  font-size: 11px;
  color: var(--theme-text-muted);
}

.stats-title-row {
  padding: 0 0 8px 0;
  border-bottom: 1px solid var(--theme-border-color);
  margin-bottom: 4px;
}
.stats-title { font-size: 12px; font-weight: 700; color: var(--theme-text-secondary); }

.stats-grid .stat-item:nth-child(2) .stat-value { color: #667eea; }
.stats-grid .stat-value.green { color: #27ae60 !important; }
.stats-grid .stat-value.orange { color: #e67e22 !important; }
.stats-grid .stat-value.purple { color: #667eea !important; }
.stats-grid .stat-value.red { color: #c0392b !important; }
.stat-item.highlight { background: rgba(91,71,194,0.06); border-radius: 8px; padding: 4px; }

.stat-value {
  display: block;
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.stat-label {
  display: block;
  font-size: 11px;
  color: #999999;
}

.stats-title-row {
  padding: 0 0 8px 0;
  border-bottom: 1px solid #e2e6ed;
  margin-bottom: 4px;
}
.stats-title { font-size: 12px; font-weight: 700; color: #555555; }

/* 菜单卡片 */
.menu-card {
  background: #ffffff;
  border: 1px solid #e2e6ed;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}

.menu-card :deep(.el-menu-item) {
  color: #1a1a1a !important;
}

.menu-card :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #667eea, #7B5FD4) !important;
  color: #fff !important;
}

.menu-card :deep(.el-menu-item:hover) {
  background: rgba(91,71,194,0.08) !important;
  color: #667eea !important;
}

.menu-card :deep(.el-card__header) {
  padding: 12px 16px;
  border-bottom: 1px solid var(--theme-border-color);
}

.menu-card :deep(.el-card__body) {
  padding: 0;
}

.menu-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--theme-text-primary);
}

.menu-card :deep(.el-menu) {
  border: none;
}

/* ========== 消息通知 ========== */
.unread-badge {
  background: var(--theme-danger);
  color: #fff;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 700;
}

.loading-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 40px;
  color: #999;
  font-size: 14px;
}

.notification-list { display: flex; flex-direction: column; }
.notif-item {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 4px; border-bottom: 1px solid #f5f5f5;
  transition: background 0.2s; cursor: pointer;
}
.notif-item:last-child { border-bottom: none; }
.notif-item:hover { background: var(--theme-bg-card-hover); border-radius: 8px; }
.notif-item.unread { background: rgba(239,68,68,0.06); }
.notif-icon { font-size: 22px; flex-shrink: 0; width: 32px; text-align: center; }
.notif-body { flex: 1; min-width: 0; }
.notif-title { font-size: 13px; font-weight: 700; color: var(--theme-text-primary); margin-bottom: 3px; }
.notif-desc { font-size: 12px; color: #888; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.notif-right { display: flex; flex-direction: column; align-items: flex-end; gap: 4px; flex-shrink: 0; }
.notif-time { font-size: 11px; color: #bbb; }
.unread-dot { width: 8px; height: 8px; border-radius: 50%; background: #f56c6c; }

/* ========== 右侧内容 ========== */
.content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 通用卡片 */
.info-card {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.info-card :deep(.el-card__header) {
  padding: 14px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.info-card :deep(.el-card__body) {
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

/* ========== 个人信息网格 ========== */
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 24px;
}

.info-row {
  display: flex;
  align-items: center;
  padding: 11px 0;
  border-bottom: 1px solid #f9f9f9;
}

.info-row:last-child,
.info-row:nth-last-child(2):nth-child(odd) {
  border-bottom: none;
}

.info-label {
  width: 80px;
  flex-shrink: 0;
  font-size: 13px;
  color: #999;
}

.info-value {
  flex: 1;
  font-size: 14px;
  color: #333;
  word-break: break-all;
}

/* ========== 实名认证状态块 ========== */
.status-block {
  display: flex;
  align-items: center;
  gap: 16px;
}

.status-icon-wrap {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.status-icon-wrap.success { background: #f0f9eb; color: #67c23a; }
.status-icon-wrap.warning { background: #fdf6ec; color: #e6a23c; }
.status-icon-wrap.info { background: #f4f4f5; color: #909399; }

.status-text {
  flex: 1;
}

.status-title {
  margin: 0 0 4px;
  font-size: 15px;
  color: #333;
  font-weight: 600;
}

.status-desc {
  margin: 0;
  font-size: 13px;
  color: #999;
}

/* ========== 安全中心 ========== */
.security-score {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #999;
}

.score-num {
  font-size: 14px;
  font-weight: 600;
  color: #667eea;
}

.security-list {
  display: flex;
  flex-direction: column;
}

.security-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4px 0;
}

.security-info {
  display: flex;
  align-items: center;
  gap: 14px;
  flex: 1;
}

.security-icon-wrap {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  background: #f5f7fa;
  color: #667eea;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.security-title {
  margin: 0 0 2px;
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.security-desc {
  margin: 0;
  font-size: 12px;
  color: #999;
}

.inner-divider {
  margin: 12px 0;
}

/* ========== 响应式 ========== */
@media (max-width: 900px) {
  .user-main {
    flex-direction: column;
    padding: 16px;
  }

  .sidebar {
    width: 100%;
    position: static;
    flex-direction: row;
    flex-wrap: wrap;
  }

  .profile-card {
    flex: 1;
    min-width: 160px;
  }

  .stats-card {
    flex: 1;
    min-width: 160px;
  }

  .menu-card {
    width: 100%;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }
}


/* ========== Theme Switcher ========== */
.theme-switcher-wrap {
  background: var(--theme-bg-card);
  border: 1px solid var(--theme-border-color);
  border-radius: var(--theme-radius-md);
  padding: 14px;
}

.theme-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--theme-text-secondary);
  margin-bottom: 10px;
  font-weight: 600;
}

.theme-label-icon { font-size: 14px; }

.theme-btns {
  display: flex;
  gap: 8px;
}

.theme-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px 4px;
  border-radius: var(--theme-radius-sm);
  background: var(--theme-bg-input);
  border: 1px solid var(--theme-border-color);
  cursor: pointer;
  transition: all 0.2s;
}

.theme-btn:hover {
  border-color: var(--theme-accent);
}

.theme-btn.active {
  border-color: var(--theme-accent);
  background: var(--theme-highlight-bg);
}

.theme-btn-preview {
  width: 32px;
  height: 22px;
  border-radius: 4px;
  border: 1px solid;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.theme-btn-name {
  font-size: 10px;
  color: var(--theme-text-secondary);
}

.theme-btn.active .theme-btn-name {
  color: var(--theme-accent);
  font-weight: 600;
}



/* ========== 头像选择器 ========== */
.avatar-picker-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  padding: 8px 0 16px;
}

.avatar-option {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  border: 3px solid transparent;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-option img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.avatar-option:hover {
  border-color: #667eea;
  transform: scale(1.05);
}

.avatar-option.selected {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.3);
}

.avatar-upload-row {
  padding-top: 12px;
  border-top: 1px solid #e2e6ed;
  display: flex;
  justify-content: center;
}

/* ========== 头像卡片 ========== */
.avatar-wrap {
  position: relative;
  display: inline-block;
}

.avatar-edit-icon {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 22px;
  height: 22px;
  background: #667eea;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  cursor: pointer;
  border: 2px solid #fff;
}

</style>