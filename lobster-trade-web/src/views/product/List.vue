<template>
  <PageLayout>
    <template #sidebar>
      <div class="sidebar-wrapper" :class="{ collapsed: sidebarCollapsed }">
        <!-- 收起按钮（移动端） -->
        <div class="sidebar-toggle-btn" @click="sidebarCollapsed = !sidebarCollapsed">
          <span>{{ sidebarCollapsed ? '▶' : '◀' }}</span>
        </div>

        <div class="sidebar-inner">
          <!-- 标题 -->
          <div class="sidebar-head">
            <span class="sidebar-icon">🔍</span>
            <span class="sidebar-title">商品筛选</span>
          </div>

          <!-- 游戏筛选 -->
          <div class="filter-block">
            <div class="block-head">
              <span class="block-dot dot-game"></span>
              <span class="block-name">游戏</span>
            </div>
            <div class="game-wrap">
              <el-tag
                v-for="game in games"
                :key="game.id"
                class="game-tag"
                :type="activeGame === game.id ? 'primary' : 'info'"
                effect="plain"
                @click="selectGame(game.id)"
              >
                {{ game.gameName }}
              </el-tag>
            </div>
          </div>

          <!-- 商品类型 -->
          <div class="filter-block">
            <div class="block-head">
              <span class="block-dot dot-type"></span>
              <span class="block-name">类型</span>
            </div>
            <div class="type-wrap">
              <div
                v-for="t in productTypes"
                :key="t.value"
                class="type-btn"
                :class="{ active: activeType === t.value }"
                @click="selectType(t.value)"
              >
                {{ t.label }}
                <span v-if="activeType === t.value" class="chk">✓</span>
              </div>
            </div>
          </div>

          <!-- 价格区间（快捷筛选） -->
          <div class="filter-block">
            <div class="block-head">
              <span class="block-dot dot-price"></span>
              <span class="block-name">价格</span>
            </div>
            <div class="price-wrap">
              <div
                v-for="range in priceRanges"
                :key="range.label"
                class="price-btn"
                :class="{ active: activePriceRange === range.label }"
                @click="selectPriceRange(range.label)"
              >
                {{ range.label }}
              </div>
            </div>
          </div>

          <!-- 库存 -->
          <div class="filter-block">
            <div class="block-head">
              <span class="block-dot dot-stock"></span>
              <span class="block-name">库存</span>
            </div>
            <div class="stock-row">
              <span class="stock-label">只看有货</span>
              <el-switch v-model="onlyInStock" @change="fetchProducts" />
            </div>
          </div>

          <!-- 价格类型 -->
          <div class="filter-block">
            <div class="block-head">
              <span class="block-dot dot-ptype"></span>
              <span class="block-name">定价</span>
            </div>
            <div class="ptype-wrap">
              <div
                v-for="pt in priceTypes"
                :key="pt.value"
                class="ptype-btn"
                :class="{ active: priceType === pt.value }"
                @click="priceType = pt.value; fetchProducts()"
              >
                <span class="ptype-dot"></span>
                {{ pt.label }}
              </div>
            </div>
          </div>

          <!-- 重置按钮 -->
          <div class="reset-row">
            <el-button size="small" class="reset-btn" @click="resetFilters">
              ⟳ 重置筛选
            </el-button>
          </div>
        </div>
      </div>
    </template>

    <div class="product-content">
      <!-- 搜索工具栏 -->
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索商品..."
          clearable
          class="search-input"
          @keyup.enter="fetchProducts"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="fetchProducts">搜索</el-button>
        <!-- 排序下拉 -->
        <el-dropdown trigger="click" @command="handleSortChange" class="sort-dropdown">
          <el-button class="sort-btn">
            <span>{{ currentSortLabel }}</span>
            <el-icon><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item
                v-for="opt in sortOptions"
                :key="opt.label"
                :command="opt"
              >
                {{ opt.label }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button @click="router.push({ path: '/product/publish' })" class="publish-btn">
          <el-icon><Sell /></el-icon>
          发布商品
        </el-button>
      </div>

      <!-- 本周热销榜 -->
      <div class="hot-rank-section" v-if="hotProducts.length > 0">
        <div class="section-head">
          <span class="section-icon">🏆</span>
          <span class="section-title">本周热销榜</span>
        </div>
        <div class="hot-rank-list">
          <div
            v-for="(p, idx) in hotProducts"
            :key="p.id"
            class="hot-rank-item"
            @click="router.push('/product/detail/' + p.id)"
          >
            <span class="rank-num" :class="{ top3: idx < 3 }">{{ idx + 1 }}</span>
            <div class="rank-info">
              <span class="rank-title">{{ p.title }}</span>
              <span class="rank-sales">已售 {{ p.completedOrders || 0 }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 商品列表 -->
      <div class="product-grid" v-if="products.length > 0">
        <div
          v-for="p in products"
          :key="p.id"
          class="product-card"
          @click="router.push('/product/detail/' + p.id)"
        >
          <div class="product-image">
            <el-image
              :src="JSON.parse(p.images || '[]')[0] || 'https://ViaPython/L/200'"
              fit="cover"
              class="product-img"
            >
              <template #error>
                <div class="img-placeholder">🦞</div>
              </template>
            </el-image>
            <div v-if="p.completedOrders > 0" class="hot-badge">
              🔥 售{{ p.completedOrders }}
            </div>
            <div v-if="p.isOfficial" class="official-badge">官开自营</div>
            <div class="product-price-tag">¥{{ p.price }}</div>
          </div>
          <div class="product-info">
            <div class="product-title">{{ p.title }}</div>
            <div class="product-meta">
              <span class="meta-game">{{ getGameName(p.gameId) }}</span>
              <span class="meta-unit">{{ p.unit }}</span>
            </div>
            <div class="product-bottom">
              <div class="product-seller">
                <span class="seller-avatar">🦞</span>
              </div>
              <div class="product-stats">
                <span class="stat">🔥 {{ p.viewCount || 0 }}</span>
                <span class="stat">❤️ {{ p.favoriteCount || 0 }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <div class="empty-icon">🔍</div>
        <div class="empty-text">暂无商品</div>
        <el-button type="primary" @click="router.push({ path: '/product/publish' })">发布第一个商品</el-button>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrap" v-if="total > 0">
        <div class="pagination-info">共 <strong>{{ total }}</strong> 条记录，第 <strong>{{ page }}</strong>/{{ totalPages }} 页</div>
        <el-pagination
          v-model:current-page="page"
          :page-size="20"
          :total="total"
          layout="prev, pager, next, jumper"
          @current-change="fetchProducts"
        />
      </div>
    </div>

    <!-- 最近成交动态（右侧悬浮） -->
    <div class="recent-orders-flyer" v-if="recentOrders.length > 0">
      <div class="flyer-head">
        <span class="flyer-icon">🔥</span>
        <span>最新成交</span>
        <button class="flyer-close" @click="showRecentOrders = false">×</button>
      </div>
      <div class="flyer-list">
        <transition-group name="fade-slide" tag="div" class="flyer-inner">
          <div v-for="(o, idx) in visibleOrders" :key="o.id + '_' + o.ts" class="flyer-item">
            <span class="flyer-nick">{{ getNick(o) }}</span>
            <span class="flyer-text">刚买走了</span>
            <span class="flyer-title">{{ o.productTitle || '商品' }}</span>
          </div>
        </transition-group>
      </div>
    </div>
  </PageLayout>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getProductList } from '@/api/product'
import { getGames } from '@/api/games'
import { getHotProducts, getRecentOrders } from '@/api/stats'
import PageLayout from '@/components/PageLayout.vue'
import { Search, Sell, ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()

const products = ref([])
const games = ref([])
const hotProducts = ref([])
const recentOrders = ref([])
const showRecentOrders = ref(true)
const sidebarCollapsed = ref(false)
const activeGame = ref(null)
const activeType = ref('')
const activePriceRange = ref('')
const priceType = ref('')
const sortField = ref('')
const sortOrder = ref('')
const onlyInStock = ref(false)
const keyword = ref('')
const page = ref(1)
const total = ref(0)

let recentOrderTimer = null
let orderIndex = 0

const productTypes = [
  { label: '全部', value: '' },
  { label: '装备', value: 'equipment' },
  { label: '道具', value: 'goods' },
  { label: '代练', value: 'boosting' },
  { label: '游戏币', value: 'game_currency' },
]

const priceRanges = [
  { label: '不限', min: 0, max: 0 },
  { label: '1-50', min: 1, max: 50 },
  { label: '50-200', min: 50, max: 200 },
  { label: '200-500', min: 200, max: 500 },
  { label: '500-1000', min: 500, max: 1000 },
  { label: '1000+', min: 1000, max: null },
]

const priceTypes = [
  { label: '不限', value: '' },
  { label: '一口价', value: 'fixed' },
  { label: '可议价', value: 'negotiable' },
]

// 最近成交：每次显示2条
const visibleOrders = computed(() => {
  if (recentOrders.value.length === 0) return []
  const start = orderIndex % recentOrders.value.length
  const a = recentOrders.value[start]
  const b = recentOrders.value[(start + 1) % recentOrders.value.length]
  return [{ ...a, ts: Date.now() }, { ...b, ts: Date.now() + 1 }]
})

const nicknames = ['星星⭐', '传奇⭐', '战神⭐', '王者⭐', '萌新⭐', '欧皇⭐', '大佬⭐', '小虾米']

const getNick = (o) => {
  if (o.buyerNickname) return o.buyerNickname
  return nicknames[o.id % nicknames.length]
}

onMounted(() => {
  fetchGames()
  fetchProducts()
  fetchHotProducts()
  fetchRecentOrders()
  recentOrderTimer = setInterval(() => {
    orderIndex++
  }, 3500)

  document.title = '商品列表 - 龙虾道具交易平台'
})

onUnmounted(() => {
  if (recentOrderTimer) clearInterval(recentOrderTimer)
})

const fetchGames = async () => {
  try {
    const res = await getGames()
    if (res.data) {
      games.value = res.data
      if (games.value.length > 0 && !activeGame.value) {
        activeGame.value = games.value[0].id
      }
    }
  } catch (e) {
    console.error('获取游戏列表失败', e)
  }
}

const fetchHotProducts = async () => {
  try {
    const res = await getHotProducts()
    if (res.data && res.data.length > 0) {
      hotProducts.value = res.data.slice(0, 10)
    }
  } catch (e) {
    console.error('获取热销榜失败', e)
  }
}

const fetchRecentOrders = async () => {
  try {
    const res = await getRecentOrders({ limit: 20 })
    if (res.data && res.data.length > 0) {
      recentOrders.value = res.data
    }
  } catch (e) {
    console.error('获取最近成交失败', e)
  }
}

const fetchProducts = async (p = 1) => {
  p = Math.max(1, p)
  page.value = p
  try {
    const params = { page: page.value, pageSize: 20 }
    if (activeGame.value) params.gameId = activeGame.value
    if (activeType.value) params.productType = activeType.value
    if (priceType.value) params.priceType = priceType.value
    if (keyword.value) params.keyword = keyword.value

    // 排序参数（后端目前忽略，前端先传着）
    if (sortField.value) {
      params.sortField = sortField.value
      params.sortOrder = sortOrder.value
    }

    const res = await getProductList(params)
    if (res.data) {
      let list = res.data.records || res.data.list || []

      // 前端价格区间筛选
      const rangeMap = {
        '1-50':    { min: 1,    max: 50   },
        '50-200':  { min: 50,   max: 200  },
        '200-500': { min: 200,  max: 500  },
        '500-1000':{ min: 500,  max: 1000 },
        '1000+':   { min: 1000, max: null },
      }
      if (activePriceRange.value && rangeMap[activePriceRange.value]) {
        const r = rangeMap[activePriceRange.value]
        list = list.filter(p => {
          if (r.min != null && p.price < r.min) return false
          if (r.max != null && p.price > r.max) return false
          return true
        })
      }

      // 前端库存筛选
      if (onlyInStock.value) {
        list = list.filter(p => p.stock != null && p.stock > 0)
      }

      products.value = list
      total.value = list.length
    }
  } catch (e) {
    console.error('获取商品列表失败', e)
  }
}

const sortOptions = [
  { label: '综合排序', field: '', order: '' },
  { label: '最新上架', field: 'createTime', order: 'desc' },
  { label: '价格↑', field: 'price', order: 'asc' },
  { label: '价格↓', field: 'price', order: 'desc' },
  { label: '销量', field: 'sales', order: 'desc' },
  { label: '收藏', field: 'favoriteCount', order: 'desc' },
  { label: '浏览', field: 'viewCount', order: 'desc' },
]

const currentSortLabel = computed(() => {
  const opt = sortOptions.find(o => o.field === sortField.value && o.order === sortOrder.value)
  return opt ? opt.label : '综合排序'
})

const totalPages = computed(() => Math.ceil(total.value / 20) || 1)

const handleSortChange = (opt) => {
  sortField.value = opt.field
  sortOrder.value = opt.order
  page.value = 1
  fetchProducts()
}

const selectGame = (id) => {
  activeGame.value = id
  page.value = 1
  fetchProducts()
}

const selectType = (v) => {
  activeType.value = v
  page.value = 1
  fetchProducts()
}

const selectPriceRange = (label) => {
  activePriceRange.value = label
  page.value = 1
  fetchProducts()
}

const getGameName = (id) => {
  const g = games.value.find(x => x.id === id)
  return g ? g.gameName : ''
}

const resetFilters = () => {
  activeGame.value = games.value[0]?.id || null
  activeType.value = ''
  activePriceRange.value = ''
  priceType.value = ''
  onlyInStock.value = false
  keyword.value = ''
  sortField.value = ''
  sortOrder.value = ''
  page.value = 1
  fetchProducts()
}
</script>

<style scoped>
/* ====== 侧边栏 ====== */
.sidebar-wrapper {
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  overflow: hidden;
  position: sticky;
  top: 24px;
  transition: all 0.3s;
}
.sidebar-inner {
  transition: all 0.3s;
}

/* 移动端收起 */
@media (max-width: 768px) {
  .sidebar-wrapper {
    width: 48px !important;
    min-width: 48px;
    border-radius: 12px;
  }
  .sidebar-wrapper .sidebar-inner,
  .sidebar-wrapper .sidebar-head,
  .sidebar-wrapper .filter-block,
  .sidebar-wrapper .reset-row {
    display: none;
  }
  .sidebar-wrapper:not(.collapsed) {
    width: 260px !important;
    min-width: 260px;
  }
  .sidebar-wrapper:not(.collapsed) .sidebar-inner {
    display: block;
  }
  .sidebar-toggle-btn {
    display: flex;
  }
}
@media (min-width: 769px) {
  .sidebar-toggle-btn {
    display: none;
  }
}

.sidebar-toggle-btn {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 14px;
  color: #667eea;
  border-bottom: 1px solid #f0eeff;
}
.sidebar-toggle-btn:hover {
  background: #f5f0ff;
}

.sidebar-head {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 18px 14px;
  background: linear-gradient(135deg, var(--accent-color), #c73e54);
  color: #fff;
  border-bottom: 1px solid rgba(233,69,96,0.3);
}
.sidebar-head .sidebar-icon { font-size: 16px; }
.sidebar-title {
  font-size: 15px;
  font-weight: 800;
  letter-spacing: 0.5px;
}

.filter-block {
  padding: 14px 16px;
  border-bottom: 1px solid #f5f0ff;
  transition: background 0.2s;
}
.filter-block:last-of-type { border-bottom: none; }

.block-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}
.block-dot {
  width: 8px; height: 8px; border-radius: 50%;
  flex-shrink: 0;
}
.dot-game { background: var(--accent-color); box-shadow: 0 0 6px rgba(233,69,96,0.5); }
.dot-type { background: var(--danger); box-shadow: 0 0 6px rgba(239,68,68,0.5); }
.dot-price { background: var(--gold-color); box-shadow: 0 0 6px rgba(240,165,0,0.5); }
.dot-stock { background: var(--success); box-shadow: 0 0 6px rgba(16,185,129,0.5); }
.dot-ptype { background: #909399; box-shadow: 0 0 6px rgba(144,147,153,0.5); }
.block-name {
  font-size: 12px;
  font-weight: 700;
  color: #888;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.game-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
}
:deep(.game-tag) {
  cursor: pointer;
  font-size: 12px !important;
  padding: 5px 12px !important;
  border-radius: 20px !important;
  transition: all 0.2s;
  border: 1.5px solid !important;
}
:deep(.game-tag.el-tag--info) {
  background: rgba(233,69,96,0.08) !important;
  border-color: rgba(233,69,96,0.2) !important;
  color: var(--text-secondary) !important;
}
:deep(.game-tag.el-tag--info:hover) {
  background: rgba(233,69,96,0.15) !important;
  border-color: var(--accent-color) !important;
  color: var(--accent-color) !important;
}
:deep(.game-tag.el-tag--primary) {
  background: linear-gradient(135deg, var(--accent-color), #c73e54) !important;
  border-color: transparent !important;
  color: #fff !important;
  font-weight: 700 !important;
  box-shadow: 0 3px 10px rgba(233,69,96,0.35) !important;
}

.type-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
}
.type-btn {
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 12px;
  color: var(--text-secondary);
  background: rgba(233,69,96,0.05);
  border: 1.5px solid rgba(233,69,96,0.15);
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 4px;
  user-select: none;
}
.type-btn:hover {
  border-color: var(--accent-color);
  color: var(--accent-color);
  background: rgba(233,69,96,0.1);
}
.type-btn.active {
  background: linear-gradient(135deg, var(--accent-color), #c73e54);
  border-color: transparent;
  color: #fff;
  font-weight: 700;
  box-shadow: 0 3px 10px rgba(233,69,96,0.3);
}
.type-btn .chk { font-size: 11px; }

.price-wrap {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 7px;
}
.price-btn {
  padding: 8px 6px;
  border-radius: 10px;
  font-size: 12px;
  color: var(--text-secondary);
  background: rgba(240,165,0,0.05);
  border: 1.5px solid rgba(240,165,0,0.15);
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}
.price-btn:hover {
  border-color: var(--gold-color);
  color: var(--gold-color);
  background: rgba(240,165,0,0.1);
}
.price-btn.active {
  background: linear-gradient(135deg, var(--gold-color), var(--gold-hover));
  border-color: transparent;
  color: #1a1a2e;
  font-weight: 700;
  box-shadow: 0 3px 10px rgba(240,165,0,0.3);
}

.stock-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.stock-label {
  font-size: 13px;
  color: #333;
  font-weight: 500;
}
:deep(.el-switch) { --el-switch-on-color: #67c23a; }

.ptype-wrap {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.ptype-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 10px;
  font-size: 13px;
  color: var(--text-secondary);
  background: rgba(233,69,96,0.05);
  border: 1.5px solid rgba(233,69,96,0.15);
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}
.ptype-btn:hover {
  border-color: var(--accent-color);
  color: var(--accent-color);
  background: rgba(233,69,96,0.1);
}
.ptype-btn.active {
  background: linear-gradient(135deg, var(--accent-color), #c73e54);
  border-color: transparent;
  color: #fff;
  font-weight: 700;
  box-shadow: 0 3px 10px rgba(233,69,96,0.3);
}
.ptype-dot {
  width: 8px; height: 8px; border-radius: 50%;
  background: var(--border-color); flex-shrink: 0;
  transition: background 0.2s;
}
.ptype-btn:hover .ptype-dot { background: var(--accent-color); }
.ptype-btn.active .ptype-dot { background: rgba(255,255,255,0.7); }

.reset-row {
  padding: 12px 16px 16px;
  display: flex;
  justify-content: center;
}
.reset-btn {
  width: 100%;
  border-radius: 10px !important;
  border: 1.5px dashed var(--border-color) !important;
  color: #667eea !important;
  font-size: 13px !important;
  background: #f0f2ff !important;
  transition: all 0.2s !important;
}
.reset-btn:hover {
  border-color: var(--accent-color) !important;
  color: var(--accent-color) !important;
  background: rgba(233,69,96,0.05) !important;
}

/* ====== 主内容区 ====== */
.product-content { display: flex; flex-direction: column; gap: 16px; }

.search-bar {
  display: flex; gap: 10px; align-items: center;
  background: var(--bg-card); padding: 14px 16px; border-radius: 14px;
  box-shadow: var(--shadow-sm); border: 1px solid var(--border-color);
}
:deep(.search-input .el-input__wrapper) {
  border-radius: 10px; border: 1.5px solid var(--border-color);
  box-shadow: none !important;
}
:deep(.search-input .el-input__wrapper:focus-within) {
  border-color: var(--accent-color);
  box-shadow: 0 0 0 3px rgba(233,69,96,0.12) !important;
}
.publish-btn {
  background: linear-gradient(135deg, #e94560, #c73e54) !important;
  border: none !important; border-radius: 10px !important;
  color: #fff !important; font-weight: 700 !important;
  box-shadow: 0 4px 14px rgba(233,69,96,0.38) !important;
}
.publish-btn:hover { opacity: 0.92; transform: translateY(-1px); }

/* ====== 热销榜 ====== */
.hot-rank-section {
  background: var(--bg-card);
  border-radius: 14px;
  overflow: hidden;
  box-shadow: var(--shadow-sm); border: 1px solid var(--border-color);
}
.section-head {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: linear-gradient(135deg, var(--accent-color), #c73e54);
  color: #fff;
}
.section-icon { font-size: 16px; }
.section-title {
  font-size: 14px;
  font-weight: 800;
  letter-spacing: 0.5px;
}
.hot-rank-list {
  padding: 10px 14px 14px;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
}
.hot-rank-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 10px;
  background: #f9f8ff;
  border: 1px solid #e0deff;
  cursor: pointer;
  transition: all 0.2s;
}
.hot-rank-item:hover {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-color: transparent;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102,126,234,0.25);
}
.hot-rank-item:hover .rank-num,
.hot-rank-item:hover .rank-title,
.hot-rank-item:hover .rank-sales {
  color: #fff;
}
.rank-num {
  font-size: 13px;
  font-weight: 900;
  color: var(--accent-color);
  flex-shrink: 0;
  min-width: 18px;
}
.rank-num.top3 {
  color: var(--gold-color);
  font-size: 15px;
}
.rank-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.rank-title {
  font-size: 11px;
  color: var(--text-primary);
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 90px;
}
.rank-sales {
  font-size: 10px;
  color: var(--gold-color);
  font-weight: 600;
}
@media (max-width: 900px) {
  .hot-rank-list { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 600px) {
  .hot-rank-list { grid-template-columns: repeat(2, 1fr); }
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
@media (max-width: 1100px) {
  .product-grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 768px) {
  .product-grid { grid-template-columns: repeat(2, 1fr); gap: 12px; }
}
@media (max-width: 480px) {
  .product-grid { grid-template-columns: 1fr; }
}

.product-card {
  background: var(--bg-card); border-radius: 14px; overflow: hidden;
  cursor: pointer; transition: all 0.25s; border: 1px solid var(--border-color);
}
.product-card:hover {
  border-color: var(--accent-color); transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(233,69,96,0.2);
}
.product-image { position: relative; height: 160px; background: #12122a; }
.product-img { width: 100%; height: 100%; }
.img-placeholder {
  width: 100%; height: 100%; display: flex; align-items: center;
  justify-content: center; font-size: 40px; background: rgba(233,69,96,0.05);
}
.product-price-tag {
  position: absolute; bottom: 8px; right: 8px;
  background: linear-gradient(135deg, var(--gold-color), var(--gold-hover));
  color: #1a1a2e; padding: 4px 12px; border-radius: 14px;
  font-size: 13px; font-weight: 800;
  box-shadow: 0 2px 8px rgba(240,165,0,0.4);
}
.official-badge {
  position: absolute; top: 8px; right: 8px;
  background: linear-gradient(135deg, var(--accent-color), #c73e54);
  color: #fff; padding: 3px 10px; border-radius: 12px;
  font-size: 11px; font-weight: 800;
  box-shadow: 0 2px 8px rgba(233,69,96,0.4);
  z-index: 2;
}
.hot-badge {
  position: absolute; top: 8px; left: 8px;
  background: linear-gradient(135deg, var(--accent-color), var(--accent-hover));
  color: #fff; padding: 3px 10px; border-radius: 12px;
  font-size: 11px; font-weight: 800;
  box-shadow: 0 2px 8px rgba(233,69,96,0.4);
  z-index: 1;
}
.product-info { padding: 14px; }
.product-title {
  font-size: 14px; font-weight: 600; color: var(--text-primary);
  margin-bottom: 8px; overflow: hidden;
  text-overflow: ellipsis; white-space: nowrap;
}
.product-meta { display: flex; gap: 8px; align-items: center; margin-bottom: 10px; }
.meta-game {
  font-size: 11px; color: var(--accent-color); background: rgba(233,69,96,0.1);
  padding: 2px 8px; border-radius: 6px; font-weight: 600;
  border: 1px solid rgba(233,69,96,0.2);
}
.meta-unit { font-size: 11px; color: var(--text-muted); }
.product-bottom { display: flex; align-items: center; justify-content: space-between; }
.product-seller { display: flex; align-items: center; gap: 5px; }
.seller-avatar { font-size: 16px; }
.product-stats { display: flex; gap: 8px; }
.stat { font-size: 11px; color: var(--text-muted); }

.empty-state {
  display: flex; flex-direction: column; align-items: center;
  justify-content: center; padding: 60px 0; gap: 16px;
  background: var(--bg-card); border-radius: 14px;
  border: 1px solid var(--border-color);
}
.empty-icon { font-size: 48px; }
.empty-text { font-size: 15px; color: var(--text-secondary); }

.sort-dropdown { margin-left: 2px; }
.sort-btn {
  display: flex !important;
  align-items: center !important;
  gap: 4px !important;
  border-radius: 10px !important;
  border: 1.5px solid #667eea !important;
  background: var(--bg-card) !important;
  color: var(--text-primary) !important;
  font-size: 13px !important;
  font-weight: 600 !important;
  transition: all 0.2s !important;
}
.sort-btn:hover {
  border-color: var(--accent-color) !important;
  color: var(--accent-color) !important;
  background: rgba(233,69,96,0.05) !important;
}
.pagination-info {
  font-size: 13px;
  color: var(--text-secondary);
  margin-right: 16px;
  white-space: nowrap;
}
.pagination-info strong {
  color: var(--accent-color);
}
.pagination-wrap {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px 0;
}
:deep(.pagination-wrap .el-pagination) {
  background: var(--bg-card);
  padding: 10px 16px;
  border-radius: 12px;
  box-shadow: var(--shadow-sm); border: 1px solid var(--border-color);
}

/* ====== 最近成交动态（右侧悬浮） ====== */
.recent-orders-flyer {
  position: fixed;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  width: 220px;
  background: var(--bg-card);
  border-radius: 14px;
  box-shadow: 0 6px 28px rgba(0,0,0,0.5);
  border: 1px solid var(--border-color);
  z-index: 100;
  overflow: hidden;
}
.flyer-head {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 12px 8px;
  background: linear-gradient(135deg, var(--accent-color), var(--accent-hover));
  color: #fff;
  font-size: 12px;
  font-weight: 700;
}
.flyer-icon { font-size: 14px; }
.flyer-close {
  margin-left: auto;
  background: none;
  border: none;
  color: #fff;
  font-size: 16px;
  cursor: pointer;
  line-height: 1;
  padding: 0 2px;
  opacity: 0.8;
}
.flyer-close:hover { opacity: 1; }
.flyer-list {
  padding: 8px 10px;
  height: 80px;
  overflow: hidden;
  position: relative;
}
.flyer-inner {
  position: relative;
  height: 100%;
}
.flyer-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 6px 8px;
  border-radius: 8px;
  background: rgba(233,69,96,0.08);
  border: 1px solid rgba(233,69,96,0.15);
  margin-bottom: 6px;
  font-size: 11px;
  line-height: 1.4;
}
.flyer-item:last-child { margin-bottom: 0; }
.flyer-nick {
  color: var(--accent-color);
  font-weight: 700;
}
.flyer-text { color: var(--text-secondary); }
.flyer-title {
  color: var(--text-primary);
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 淡入淡出动画 */
.fade-slide-enter-active {
  transition: all 0.5s ease;
}
.fade-slide-leave-active {
  transition: all 0.5s ease;
  position: absolute;
  right: 0;
  opacity: 0;
  transform: translateY(-10px);
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(10px);
}
.fade-slide-leave-to {
  opacity: 0;
}

@media (max-width: 1200px) {
  .recent-orders-flyer { display: none; }
}
</style>
