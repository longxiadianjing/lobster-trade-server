<template>
  <PageLayout>
    <template #sidebar>
      <el-card class="menu-card" shadow="never" :body-style="{ padding: '0' }">
        <template #header>
          <span class="menu-title">❤️ 我的收藏</span>
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
          <el-menu-item index="/wallet">
            <el-icon><Wallet /></el-icon>
            <span>我的钱包</span>
          </el-menu-item>
          <el-menu-item index="/order/center">
            <el-icon><List /></el-icon>
            <span>订单中心</span>
          </el-menu-item>
          <el-menu-item index="/user/favorites">
            <el-icon><Star /></el-icon>
            <span>我的收藏</span>
          </el-menu-item>
        </el-menu>
      </el-card>
    </template>

    <div class="favorites-content">
      <el-card class="main-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">❤️ 我的收藏夹</span>
            <span class="fav-count">共 {{ favorites.length }} 件收藏商品</span>
          </div>
        </template>

        <!-- 工具栏 -->
        <div class="toolbar">
          <el-button
            v-if="selectedItems.length > 0"
            type="danger"
            plain
            size="small"
            @click="handleBatchRemove"
          >
            <el-icon><Delete /></el-icon>
            取消收藏 ({{ selectedItems.length }})
          </el-button>
          <div class="toolbar-right">
            <el-select v-model="filterGame" placeholder="按游戏筛选" size="small" clearable style="width: 140px; margin-right: 8px;">
              <el-option v-for="g in gameOptions" :key="g" :label="g" :value="g" />
            </el-select>
            <el-select v-model="sortBy" size="small" style="width: 120px;">
              <el-option label="默认排序" value="default" />
              <el-option label="价格从低到高" value="price_asc" />
              <el-option label="价格从高到低" value="price_desc" />
              <el-option label="最近收藏" value="time_desc" />
            </el-select>
          </div>
        </div>

        <!-- 全选提示 -->
        <div class="select-tip" v-if="selectedItems.length > 0">
          <el-icon><InfoFilled /></el-icon>
          已选择 {{ selectedItems.length }} 件商品
          <el-button type="primary" link size="small" @click="selectedItems = []">清空选择</el-button>
        </div>

        <!-- 收藏列表 -->
        <div class="fav-grid" v-if="filteredFavorites.length > 0">
          <div
            v-for="item in filteredFavorites"
            :key="item.id"
            class="fav-card"
            :class="{ selected: selectedItems.includes(item.id) }"
          >
            <!-- 选择框 -->
            <div class="card-check" @click.stop="toggleSelect(item.id)">
              <el-checkbox
                :model-value="selectedItems.includes(item.id)"
                @change="toggleSelect(item.id)"
              />
            </div>

            <!-- 商品图片 -->
            <div class="fav-image" @click="router.push(`/product/detail/${item.id}`)">
              <img v-if="item.coverImage" :src="item.coverImage" alt="商品图片" />
              <div v-else class="image-placeholder">
                <el-icon :size="32"><Picture /></el-icon>
              </div>
              <div class="card-badges">
                <span class="type-tag" :class="item.productType">{{ typeMap[item.productType] }}</span>
              </div>
            </div>

            <!-- 商品信息 -->
            <div class="fav-body" @click="router.push(`/product/detail/${item.id}`)">
              <h4 class="product-title">{{ item.title }}</h4>
              <div class="product-tags">
                <span class="game-tag">
                  <el-icon><Location /></el-icon>
                  {{ item.gameName }}
                </span>
              </div>
              <div class="card-bottom">
                <div class="price-wrap">
                  <span class="price-sign">¥</span>
                  <span class="price">{{ item.price }}</span>
                  <span v-if="item.unit" class="unit">/{{ item.unit }}</span>
                </div>
              </div>
              <div class="seller-row">
                <span class="seller-name">{{ item.sellerNickname || '匿名' }}</span>
                <div class="rating-wrap">
                  <el-icon class="star-icon"><Star /></el-icon>
                  <span>{{ item.reputationScore || '5.0' }}</span>
                </div>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="fav-actions">
              <el-button type="primary" size="small" @click.stop="handleBuy(item)">立即购买</el-button>
              <el-button size="small" @click.stop="handleRemove(item)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>

            <!-- 收藏时间 -->
            <div class="fav-time">
              <el-icon><Clock /></el-icon>
              {{ formatTime(item.favoriteTime) }}
            </div>
          </div>
        </div>

        <el-empty v-else description="暂无收藏商品，快去逛逛吧！" :image-size="100">
          <el-button type="primary" @click="router.push({ path: '/product/list' })">
            去逛逛
          </el-button>
        </el-empty>
      </el-card>
    </div>
  </PageLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageLayout from '@/components/PageLayout.vue'
import { User, Wallet, List, Star, ChatDotRound, Delete, Picture, Location, Clock, InfoFilled } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const selectedItems = ref([])
const filterGame = ref('')
const sortBy = ref('default')

const typeMap = {
  game_currency: '游戏币',
  equipment: '装备',
  boosting: '代练'
}

// Mock收藏数据
const favorites = ref([
  {
    id: 101, title: '三角洲行动 哈夫币 100万', price: 80, unit: '万', gameName: '三角洲行动',
    productType: 'game_currency', coverImage: '', sellerNickname: '专业搬砖商',
    reputationScore: '4.9', favoriteTime: new Date(Date.now() - 1 * 60 * 60 * 1000).toISOString()
  },
  {
    id: 102, title: '战场段位代练 全程手打不坐牢', price: 200, unit: '局', gameName: '三角洲行动',
    productType: 'boosting', coverImage: '', sellerNickname: '靠谱代练',
    reputationScore: '5.0', favoriteTime: new Date(Date.now() - 3 * 60 * 60 * 1000).toISOString()
  },
  {
    id: 103, title: '烽火保险箱任务代肝', price: 150, unit: '个', gameName: '三角洲行动',
    productType: 'boosting', coverImage: '', sellerNickname: '三角洲专家',
    reputationScore: '4.8', favoriteTime: new Date(Date.now() - 5 * 60 * 60 * 1000).toISOString()
  },
  {
    id: 104, title: '王者荣耀 点券 5000', price: 350, unit: '5000点', gameName: '王者荣耀',
    productType: 'game_currency', coverImage: '', sellerNickname: '游戏商人',
    reputationScore: '4.7', favoriteTime: new Date(Date.now() - 12 * 60 * 60 * 1000).toISOString()
  },
  {
    id: 105, title: '原神 原石 5000个', price: 200, unit: '5000个', gameName: '原神',
    productType: 'game_currency', coverImage: '', sellerNickname: '原神玩家',
    reputationScore: '5.0', favoriteTime: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000).toISOString()
  },
  {
    id: 106, title: '史诗级装备礼包', price: 500, unit: '套', gameName: '王者荣耀',
    productType: 'equipment', coverImage: '', sellerNickname: '装备专家',
    reputationScore: '4.6', favoriteTime: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString()
  },
])

const gameOptions = computed(() => [...new Set(favorites.value.map(f => f.gameName))])

const filteredFavorites = computed(() => {
  let list = [...favorites.value]
  if (filterGame.value) {
    list = list.filter(f => f.gameName === filterGame.value)
  }
  if (sortBy.value === 'price_asc') {
    list.sort((a, b) => a.price - b.price)
  } else if (sortBy.value === 'price_desc') {
    list.sort((a, b) => b.price - a.price)
  } else if (sortBy.value === 'time_desc') {
    list.sort((a, b) => new Date(b.favoriteTime) - new Date(a.favoriteTime))
  }
  return list
})

const formatTime = (time) => {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const diff = now - d
  if (diff < 60 * 1000) return '刚刚收藏'
  if (diff < 60 * 60 * 1000) return `${Math.floor(diff / 60000)}分钟前收藏`
  if (diff < 24 * 60 * 60 * 1000) return `${Math.floor(diff / 3600000)}小时前收藏`
  if (diff < 7 * 24 * 60 * 60 * 1000) return `${Math.floor(diff / 86400000)}天前收藏`
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}收藏`
}

const toggleSelect = (id) => {
  const idx = selectedItems.value.indexOf(id)
  if (idx > -1) {
    selectedItems.value.splice(idx, 1)
  } else {
    selectedItems.value.push(id)
  }
}

const handleRemove = async (item) => {
  try {
    await ElMessageBox.confirm(`确定取消收藏「${item.title}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const idx = favorites.value.findIndex(f => f.id === item.id)
    if (idx > -1) favorites.value.splice(idx, 1)
    ElMessage.success('已取消收藏')
  } catch {}
}

const handleBatchRemove = async () => {
  try {
    await ElMessageBox.confirm(`确定取消收藏选中的 ${selectedItems.value.length} 件商品吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    favorites.value = favorites.value.filter(f => !selectedItems.value.includes(f.id))
    selectedItems.value = []
    ElMessage.success('已取消收藏')
  } catch {}
}

const handleBuy = (item) => {
  router.push(`/product/detail/${item.id}`)
}

onMounted(() => {
  document.title = '我的收藏 - 龙虾道具交易平台'
})
</script>

<style scoped>
.favorites-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.menu-card {
  border-radius: 12px;
  border: none;
}

.menu-title {
  font-weight: 600;
  font-size: 14px;
}

.main-card {
  border-radius: 12px;
  border: none;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-weight: 600;
  font-size: 15px;
}

.fav-count {
  font-size: 13px;
  color: #999;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.toolbar-right {
  display: flex;
  align-items: center;
}

.select-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f0f7ff;
  border: 1px solid #cce0ff;
  border-radius: 6px;
  font-size: 13px;
  color: #666;
  margin-bottom: 12px;
}

.fav-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.fav-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  border: 2px solid transparent;
  transition: all 0.2s;
  position: relative;
}

.fav-card:hover {
  border-color: #764ba2;
  box-shadow: 0 4px 16px rgba(103, 71, 199, 0.12);
}

.fav-card.selected {
  border-color: #764ba2;
  background: #f9f8ff;
}

.card-check {
  position: absolute;
  top: 8px;
  left: 8px;
  z-index: 2;
  background: rgba(255,255,255,0.9);
  border-radius: 50%;
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.fav-image {
  height: 130px;
  background: #f5f7fa;
  position: relative;
  cursor: pointer;
  overflow: hidden;
}

.fav-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ccc;
}

.card-badges {
  position: absolute;
  top: 8px;
  right: 8px;
}

.type-tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  color: #fff;
  background: #764ba2;
}

.type-tag.boosting { background: #67c23a; }
.type-tag.equipment { background: #f5a623; }

.fav-body {
  padding: 12px;
  cursor: pointer;
}

.product-title {
  margin: 0 0 8px;
  font-size: 13px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 600;
}

.product-tags { margin-bottom: 8px; }

.game-tag {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 11px;
  color: #764ba2;
  background: rgba(103, 71, 199, 0.08);
  padding: 2px 6px;
  border-radius: 3px;
}

.card-bottom { margin-bottom: 8px; }

.price-wrap { display: flex; align-items: baseline; }

.price-sign { font-size: 13px; font-weight: 700; color: #f53f3f; }
.price { font-size: 20px; font-weight: 800; color: #f53f3f; }
.unit { font-size: 12px; color: #999; margin-left: 2px; }

.seller-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.seller-name { font-size: 12px; color: #999; }

.rating-wrap {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 12px;
  color: #f5a623;
}

.star-icon { font-size: 12px; }

.fav-actions {
  display: flex;
  gap: 6px;
  padding: 0 12px 12px;
}

.fav-actions .el-button:first-child {
  flex: 1;
  background: #764ba2;
  border-color: #764ba2;
  color: #fff;
}

.fav-actions .el-button:first-child:hover {
  background: #5a3db8;
  border-color: #5a3db8;
}

.fav-time {
  position: absolute;
  bottom: 60px;
  right: 8px;
  font-size: 10px;
  color: #bbb;
  display: flex;
  align-items: center;
  gap: 2px;
  background: rgba(255,255,255,0.8);
  padding: 2px 6px;
  border-radius: 4px;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #667eea, #7B5FD4) !important;
  color: #fff !important;
}

@media (max-width: 1100px) {
  .fav-grid { grid-template-columns: repeat(3, 1fr); }
}

@media (max-width: 800px) {
  .fav-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
