<template>
  <div class="providers-container">
    <!-- 顶部导航 -->
    <div class="top-header">
      <div class="header-inner">
        <router-link to="/home" class="logo">🦞 龙虾道具交易平台</router-link>
        <div class="header-actions">
          <el-button @click="router.push({ path: '/home' })">返回首页</el-button>
        </div>
      </div>
    </div>

    <!-- 主内容 -->
    <div class="providers-main">
      <!-- 筛选栏 -->
      <div class="filter-bar">
        <el-tabs v-model="filterType" @tab-change="loadProviders">
          <el-tab-pane label="全部" name="" />
          <el-tab-pane label="🎮 代练服务商" name="boost" />
          <el-tab-pane label="🎯 陪玩服务商" name="accompany" />
          <el-tab-pane label="🏢 工作室" name="studio" />
        </el-tabs>
        <el-select v-model="filterGame" placeholder="筛选游戏" clearable style="width: 160px;" @change="loadProviders">
          <el-option v-for="g in games" :key="g.id" :label="g.gameName" :value="g.id" />
        </el-select>
      </div>

      <!-- 服务商列表 -->
      <div v-if="providers.length" class="provider-grid">
        <el-card v-for="p in providers" :key="p.id" class="provider-card" shadow="hover">
          <div class="provider-header">
            <div class="provider-avatar">
              <img v-if="p.userAvatar" :src="p.userAvatar" alt="avatar" />
              <span v-else class="avatar-placeholder">🙎‍♂️</span>
            </div>
            <div class="provider-info">
              <div class="provider-name">
                {{ p.userNickname || '用户' + p.userId }}
                <el-tag v-if="p.certificationType === 'boost'" type="success" size="small">代练</el-tag>
                <el-tag v-else-if="p.certificationType === 'accompany'" type="warning" size="small">陪玩</el-tag>
                <el-tag v-else type="primary" size="small">工作室</el-tag>
              </div>
              <div class="provider-meta">
                <span>服务内容：<strong>{{ p.hourlyRate || '-' }}</strong></span>
                <span v-if="p.gameName">｜{{ p.gameName }}</span>
                <el-tag v-if="p.providerLevel" :type="levelTagType(p.providerLevel)" size="small" style="margin-left: 6px;">{{ levelName(p.providerLevel) }}</el-tag>
              </div>
            </div>
          </div>
          <div class="provider-desc">{{ p.serviceDescription || '暂无服务描述' }}</div>
          <div class="provider-footer">
            <span class="regions" v-if="p.serviceRegions">📍 {{ p.serviceRegions }}</span>
            <span class="expire" v-if="p.expireTime">有效期至：{{ formatDate(p.expireTime) }}</span>
          </div>
        </el-card>
      </div>

      <el-empty v-else description="暂无认证服务商" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCertifiedProviders } from '@/api/certification'
import { getGameList } from '@/api/game'

const router = useRouter()
const providers = ref([])
const games = ref([])
const filterType = ref('')
const filterGame = ref(null)

const loadProviders = async () => {
    try {
        const params = {}
        if (filterType.value) params.certType = filterType.value
        if (filterGame.value) params.gameId = filterGame.value
        const resp = await getCertifiedProviders(params)
        providers.value = resp.data || []
    } catch (e) {
        console.error(e)
    }
}

const formatDate = (date) => {
    if (!date) return '-'
    return new Date(date).toLocaleDateString('zh-CN')
}

const levelName = (level) => {
    const map = { 1: '普通', 2: '铜牌', 3: '银牌', 4: '金牌' }
    return map[level] || '普通'
}

const levelTagType = (level) => {
    const map = { 1: 'info', 2: 'warning', 3: 'success', 4: 'danger' }
    return map[level] || 'info'
}

onMounted(async () => {
    try {
        const gamesResp = await getGameList()
        games.value = gamesResp.data || []
    } catch (e) {}
    await loadProviders()

  document.title = '认证服务商 - 龙虾道具交易平台'
})
</script>

<style scoped>
.providers-container { min-height: 100vh; background: #f5f7fa; }
.top-header { background: #fff; border-bottom: 1px solid #eee; position: sticky; top: 0; z-index: 100; }
.header-inner { max-width: 1200px; margin: 0 auto; padding: 0 20px; height: 60px; display: flex; align-items: center; justify-content: space-between; }
.logo { font-size: 18px; font-weight: bold; color: #e54545; text-decoration: none; }
.providers-main { max-width: 1200px; margin: 0 auto; padding: 20px; }
.filter-bar { background: #fff; border-radius: 12px; padding: 16px 20px; margin-bottom: 20px; display: flex; gap: 16px; align-items: center; }
.provider-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(340px, 1fr)); gap: 16px; }
.provider-card { border-radius: 12px; cursor: pointer; transition: transform 0.2s; }
.provider-card:hover { transform: translateY(-2px); }
.provider-header { display: flex; gap: 12px; margin-bottom: 12px; }
.provider-avatar { width: 48px; height: 48px; border-radius: 50%; overflow: hidden; flex-shrink: 0; }
.provider-avatar img { width: 100%; height: 100%; object-fit: cover; }
.avatar-placeholder { width: 48px; height: 48px; display: flex; align-items: center; justify-content: center; font-size: 24px; background: #f0f0f0; border-radius: 50%; }
.provider-info { flex: 1; }
.provider-name { font-weight: bold; font-size: 15px; display: flex; align-items: center; gap: 6px; margin-bottom: 4px; }
.provider-meta { font-size: 13px; color: #666; display: flex; gap: 12px; }
.provider-desc { font-size: 13px; color: #666; line-height: 1.5; margin-bottom: 10px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.provider-footer { display: flex; justify-content: space-between; font-size: 12px; color: #999; }
.regions { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 200px; }
</style>
