<template>
  <div class="home-container">
    <!-- 顶部公告栏 -->
    <div class="announcement-bar">
      <div class="announcement-inner">
        <el-icon><Bell /></el-icon>
        <span>平台托管交易保障 · 资金秒到账 · 全程客服支持</span>
      </div>
    </div>

    <!-- 顶部导航（深色主题） -->
    <div class="top-header">
      <div class="header-inner">
        <router-link to="/home" class="logo">
          <span class="logo-icon">🦞</span>
          <span class="logo-text">龙虾交易</span>
        </router-link>

        <div class="header-center">
          <div class="search-bar">
            <el-select v-model="searchGame" placeholder="选择游戏" size="default" class="search-select" clearable>
              <el-option v-for="g in games" :key="g.id" :label="g.gameName" :value="g.id" />
            </el-select>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索商品..."
              size="default"
              class="search-input"
              @keyup.enter="handleSearch"
            />
            <el-button type="primary" class="search-btn" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
          </div>
          <!-- 热搜词 -->
          <div class="hot-search-bar" v-if="hotSearchWords.length > 0">
            <span class="hot-label">热搜:</span>
            <span
              v-for="w in hotSearchWords"
              :key="w.id"
              class="hot-word"
              @click="searchKeyword = w.word; handleSearch()"
            >{{ w.word }}</span>
          </div>
        </div>

        <div class="header-actions">
          <template v-if="userStore.isLoggedIn">
            <el-button @click="router.push({ path: '/product/publish' })" class="sell-btn">
              <el-icon><Sell /></el-icon>
              我要卖
            </el-button>
            <el-button @click="router.push({ path: '/wallet' })" text class="action-text-btn">我的钱包</el-button>
            <router-link to="/user" class="user-avatar-link">
              <el-avatar :size="32">{{ userStore.nickname?.charAt(0) || '我' }}</el-avatar>
            </router-link>
          </template>
          <template v-else>
            <el-button @click="router.push({ path: '/login' })" text class="action-text-btn">登录</el-button>
            <el-button @click="router.push({ path: '/register' })" type="primary" size="small" class="sell-btn">免费入驻</el-button>
          </template>
        </div>
      </div>
    </div>

    <!-- 游戏选择横栏 -->
    <div class="game-bar">
      <div class="game-bar-inner">
        <div
          v-for="game in games"
          :key="game.id"
          class="game-tab"
          :class="{ active: activeGame === game.id }"
          @click="selectGame(game.id)"
        >
          <span class="game-tab-name">{{ game.gameName }}</span>
        </div>
      </div>
    </div>

    <!-- 主体内容 -->
    <div class="home-main">

      <!-- 英雄横幅（深色） -->
      <div class="hero-banner">
        <div class="hero-deco">
          <div class="deco-circle dc1"></div>
          <div class="deco-circle dc2"></div>
          <div class="deco-circle dc3"></div>
        </div>

        <div class="hero-content">
          <div class="hero-left">
            <div class="hero-badge">
              <el-icon><CircleCheckFilled /></el-icon>
              平台严选 · 资金托管 · 0风险
            </div>
            <h1 class="hero-title">
              游戏道具<span class="highlight">安全交易</span>首选平台
            </h1>
            <p class="hero-sub">游戏币 · 装备道具 · 代练陪玩 — 全场平台托管，交易更安心</p>
            <div class="hero-ctas">
              <el-button type="primary" size="large" class="hero-btn-primary" @click="router.push({ path: '/product/list' })">
                <el-icon><ShoppingCart /></el-icon>
                我要买
              </el-button>
              <el-button size="large" class="hero-btn-sell" @click="router.push({ path: '/product/publish' })">
                <el-icon><Sell /></el-icon>
                我要卖
              </el-button>
            </div>
          </div>

          <!-- 平台保障展示 -->
          <div class="hero-right">
            <div class="hero-guarantees">
              <div class="hero-guarantee-item">
                <div class="hg-icon"><el-icon :size="22"><Lock /></el-icon></div>
                <div class="hg-text"><p class="hg-title">资金托管</p><p class="hg-sub">安全交易保障</p></div>
              </div>
              <div class="hero-guarantee-item">
                <div class="hg-icon"><el-icon :size="22"><CircleCheckFilled /></el-icon></div>
                <div class="hg-text"><p class="hg-title">正品保障</p><p class="hg-sub">拒绝骗子</p></div>
              </div>
              <div class="hero-guarantee-item">
                <div class="hg-icon"><el-icon :size="22"><Timer /></el-icon></div>
                <div class="hg-text"><p class="hg-title">极速发货</p><p class="hg-sub">订单秒到账</p></div>
              </div>
              <div class="hero-guarantee-item">
                <div class="hg-icon"><el-icon :size="22"><Headset /></el-icon></div>
                <div class="hg-text"><p class="hg-title">7x24客服</p><p class="hg-sub">随时咨询</p></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 精选推荐 -->
      <div class="section" v-if="recommendedProducts.length > 0">
        <div class="section-header">
          <h3 class="section-title">
            <el-icon><Goods /></el-icon>
            精选推荐
          </h3>
          <el-tag type="warning" effect="plain" size="small">官方精选</el-tag>
        </div>
        <div class="product-grid" v-loading="recommendLoading">
          <div
            v-for="item in recommendedProducts"
            :key="item.id"
            class="product-card"
            @click="router.push(`/product/detail/${item.id}`)"
          >
            <div class="card-image">
              <img v-if="item.coverImage" :src="item.coverImage" alt="商品图片" />
              <div v-else class="image-placeholder">
                <el-icon :size="36"><Picture /></el-icon>
              </div>
              <div class="card-badges">
                <span class="type-tag recommended-tag">精选</span>
              </div>
            </div>
            <div class="card-body">
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
                <span class="seller-name clickable" @click.stop="router.push(`/seller/${item.sellerId}`)">{{ item.sellerNickname || '匿名' }}</span>
                <div class="rating-wrap">
                  <el-icon class="star-icon"><Star /></el-icon>
                  <span>{{ item.reputationScore || '5.0' }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 热门商品 -->
      <div class="section">
        <div class="section-header">
          <h3 class="section-title">
            <el-icon><Goods /></el-icon>
            热门商品
          </h3>
          <el-button text class="see-more-btn" @click="router.push({ path: '/product/list' })">
            查看更多
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
        <div class="product-grid" v-loading="hotLoading">
          <div
            v-for="item in hotProducts"
            :key="item.id"
            class="product-card"
            @click="router.push(`/product/detail/${item.id}`)"
          >
            <div class="card-image">
              <img v-if="item.images" :src="JSON.parse(item.images)[0]" alt="商品图片" />
              <div v-else-if="item.coverImage" class="cover-img" :style="{ backgroundImage: `url(${item.coverImage})` }"></div>
              <div v-else class="image-placeholder">
                <el-icon :size="36"><Picture /></el-icon>
              </div>
              <div class="card-badges">
                <span class="type-tag" :class="item.productType">{{ typeMap[item.productType] }}</span>
              </div>
            </div>
            <div class="card-body">
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
                <span class="seller-name clickable" @click.stop="router.push(`/seller/${item.sellerId}`)">{{ item.sellerNickname || '匿名' }}</span>
                <div class="rating-wrap">
                  <el-icon class="star-icon"><Star /></el-icon>
                  <span>{{ item.reputationScore || '5.0' }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 为什么选择龙虾交易平台 -->
      <div class="section">
        <div class="why-section">
          <div class="why-grid">
            <div v-for="w in whyItems" :key="w.title" class="why-card">
              <div class="why-icon-wrap" :style="{ color: w.color }">
                <el-icon :size="28"><component :is="w.icon" /></el-icon>
              </div>
              <h4>{{ w.title }}</h4>
              <p>{{ w.desc }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 认证服务商 -->
      <div class="section">
        <div class="section-header">
          <h3 class="section-title">
            <el-icon><UserFilled /></el-icon>
            认证服务商
          </h3>
        </div>
        <div class="seller-grid">
          <div v-for="seller in topSellers" :key="seller.id" class="seller-card">
            <div class="seller-avatar-wrap">{{ seller.nickname.charAt(0) }}</div>
            <div class="seller-info-block">
              <div class="seller-name-row">
                <span class="seller-name">{{ seller.nickname }}</span>
                <el-tag type="success" size="small" effect="plain">已认证</el-tag>
              </div>
              <div class="seller-stats-row">
                <span><el-icon><Goods /></el-icon> 已售 {{ seller.totalOrders }}</span>
                <span class="rating-stars">
                  <el-icon v-for="n in 5" :key="n" :color="n <= 4 ? '#FFD700' : '#ddd'"><Star /></el-icon>
                  {{ seller.好评率 }}
                </span>
              </div>
              <div class="seller-game-tags">
                <span>代练</span>
                <span>游戏币</span>
              </div>
            </div>
            <el-button size="small" class="seller-btn" @click.stop="router.push(`/product/list?sellerId=${seller.id}`)">
              进店
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 悬浮客服组件 -->
    <CustomerService />

    <!-- 页脚 -->
    <footer class="home-footer">
      <div class="footer-main">
        <div class="footer-content">
          <div class="footer-brand">
            <div class="footer-logo">🦞 龙虾道具交易平台</div>
            <p>安全 · 便捷 · 专业 — 游戏虚拟资产交易首选平台</p>
            <div class="footer-contact-row">
              <span class="contact-item">📞 7×24客服热线：400-888-8888</span>
              <span class="contact-divider">|</span>
              <span class="contact-item">💬 在线客服随时咨询</span>
            </div>
          </div>
          <div class="footer-links">
            <div class="link-col">
              <h4>关于我们</h4>
              <span>平台简介</span>
              <span>联系我们</span>
              <span>加入我们</span>
            </div>
            <div class="link-col">
              <h4>帮助中心</h4>
              <span>新手指南</span>
              <span>交易规则</span>
              <span>防骗指南</span>
            </div>
            <div class="link-col">
              <h4>法律信息</h4>
              <span>用户协议</span>
              <span>隐私政策</span>
              <span>退款政策</span>
            </div>
          </div>
        </div>
        <div class="footer-bottom">
          <div class="footer-cert">
            <span class="cert-item">© 2026 龙虾道具交易平台 版权所有</span>
            <span class="cert-divider">|</span>
            <a href="https://beian.miit.gov.cn/" target="_blank" class="cert-item cert-link">ICP备案：京ICP备XXXXXXXX号</a>
            <span class="cert-divider">|</span>
            <span class="cert-item">公安网备：XXXXXXXX</span>
          </div>
          <p class="footer-copy">小龙虾工作室出品 · 让交易更安心</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  Search, Sell, Bell, Goods, Menu, ArrowRight,
  CircleCheckFilled, Lock, Timer, Headset,
  Trophy, ShoppingCart
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getGameList } from '@/api/game'
import { getProductList as searchProduct } from '@/api/product'
import { getHotSearchWords } from '@/api/hotsearch'
import { getRecommendedSlots } from '@/api/recommend'
import CustomerService from '@/components/CustomerService.vue'

const router = useRouter()
const userStore = useUserStore()

const searchKeyword = ref('')
const searchGame = ref(null)
const activeGame = ref(null)
const hotLoading = ref(false)
const hotProducts = ref([])
const hotSearchWords = ref([])
const recommendedProducts = ref([])
const recommendLoading = ref(false)
const recentListings = ref([])
const recentDeals = ref([
  { id: 1, city: '厦门', nickname: '张同学', productTitle: '三角洲行动-哈夫币100万', price: 80 },
  { id: 2, city: '深圳', nickname: '王先森', productTitle: '王者荣耀-荣耀水晶×2', price: 350 },
  { id: 3, city: '广州', nickname: '李小姐', productTitle: '和平精英-稀有皮肤套装', price: 500 },
  { id: 4, city: '杭州', nickname: '赵玩家', productTitle: '英雄联盟-紫色方块5000', price: 200 },
  { id: 5, city: '成都', nickname: '孙老板', productTitle: '穿越火线-雷神武器', price: 800 },
  { id: 6, city: '武汉', nickname: '周大神', productTitle: '三角洲行动-战场代练', price: 200 },
])
const games = ref([])

const whyItems = [
  { icon: Lock, title: '资金托管', desc: '买家付款后资金由平台托管，卖家发货确认后才会打款，保障双方权益', color: '#764ba2' },
  { icon: CircleCheckFilled, title: '正品商品', desc: '所有商品经过平台审核，拒绝骗子，还玩家一个纯净交易环境', color: '#67c23a' },
  { icon: Timer, title: '极速发货', desc: '自动发货系统，订单支付后系统自动发货，秒到账无需等待', color: '#f5a623' },
  { icon: Headset, title: '专属客服', desc: '7x24小时在线客服，任何问题随时咨询，交易过程全程陪伴', color: '#f56c6c' }
]

const topSellers = ref([
  { id: 1, nickname: '专业搬砖商', totalOrders: 586, 好评率: '98%' },
  { id: 2, nickname: '靠谱代练', totalOrders: 234, 好评率: '99%' },
  { id: 3, nickname: '游戏达人', totalOrders: 189, 好评率: '97%' },
  { id: 4, nickname: '三角洲专家', totalOrders: 421, 好评率: '96%' }
])

const typeMap = {
  goods: '游戏币/道具',
  boost: '代练',
  accompany: '陪玩陪练'
}

const mockHotProducts = [
  { id: 1, title: '三角洲行动 哈夫币 100万', price: 80, unit: '万', gameName: '三角洲行动', productType: 'goods', coverImage: '/images/delta_hafu_1.png', sellerNickname: '专业搬砖商', reputationScore: '4.9' },
  { id: 2, title: '战场段位代练 全程手打不坐牢', price: 200, unit: '局', gameName: '三角洲行动', productType: 'boost', coverImage: '/images/gaming_gear_2.png', sellerNickname: '靠谱代练', reputationScore: '5.0' },
  { id: 3, title: '烽火保险箱任务代肝', price: 150, unit: '个', gameName: '三角洲行动', productType: 'boost', coverImage: '/images/gaming_gear_1.png', sellerNickname: '三角洲专家', reputationScore: '4.8' },
  { id: 4, title: '王者荣耀 点券 5000', price: 350, unit: '5000点', gameName: '王者荣耀', productType: 'goods', coverImage: '/images/gaming_gear_4.png', sellerNickname: '游戏商人', reputationScore: '4.7' },
  { id: 5, title: '原神 原石 5000个', price: 200, unit: '5000个', gameName: '原神', productType: 'goods', coverImage: '/images/delta_hafu_3.png', sellerNickname: '原神玩家', reputationScore: '5.0' },
  { id: 6, title: '史诗级装备礼包', price: 500, unit: '套', gameName: '王者荣耀', productType: 'goods', coverImage: '/images/delta_hafu_4.png', sellerNickname: '装备专家', reputationScore: '4.6' },
  { id: 7, title: '和平精英 套装皮肤全套', price: 180, unit: '套', gameName: '和平精英', productType: 'goods', coverImage: '/images/gaming_gear_3.png', sellerNickname: '皮肤商', reputationScore: '4.5' },
  { id: 8, title: '等级/通行证代肝 全程手打', price: 120, unit: '项', gameName: '三角洲行动', productType: 'boost', coverImage: '/images/delta_hafu_2.png', sellerNickname: '靠谱代练', reputationScore: '5.0' },
  { id: 9, title: '英雄联盟 点券 2000', price: 150, unit: '2000点', gameName: '英雄联盟', productType: 'goods', coverImage: '/images/gaming_gear_1.png', sellerNickname: 'LOL专家', reputationScore: '4.8' },
  { id: 10, title: '魔兽世界 金币 10万', price: 250, unit: '10万', gameName: '魔兽世界', productType: 'goods', coverImage: '/images/delta_hafu_2.png', sellerNickname: '魔兽工作室', reputationScore: '4.9' },
  { id: 11, title: '炉石传说 卡牌包合集', price: 88, unit: '套', gameName: '炉石传说', productType: 'goods', coverImage: '/images/delta_hafu_3.png', sellerNickname: '炉石玩家', reputationScore: '4.7' },
  { id: 12, title: 'DNF 深渊派对门票', price: 30, unit: '张', gameName: 'DNF', productType: 'goods', coverImage: '/images/gaming_gear_2.png', sellerNickname: 'DNF专业户', reputationScore: '4.8' },
  { id: 13, title: '暗区突围 装备礼包', price: 320, unit: '套', gameName: '暗区突围', productType: 'goods', coverImage: '/images/gaming_gear_4.png', sellerNickname: '暗区商人', reputationScore: '4.6' },
  { id: 14, title: '穿越火线 英雄级武器', price: 680, unit: '把', gameName: '穿越火线', productType: 'goods', coverImage: '/images/delta_hafu_1.png', sellerNickname: 'CF商人', reputationScore: '4.5' },
  { id: 15, title: '战场排位代练 从零上战神', price: 800, unit: '单', gameName: '三角洲行动', productType: 'boost', coverImage: '/images/delta_hafu_4.png', sellerNickname: '战神代练', reputationScore: '4.9' },
  { id: 16, title: '原神 深渊代打 满星通关', price: 300, unit: '次', gameName: '原神', productType: 'boost', coverImage: '/images/gaming_gear_3.png', sellerNickname: '原神代肝', reputationScore: '5.0' }
]

const selectGame = (id) => {
  activeGame.value = id
  router.push({ path: '/product/list', query: { gameId: id } })
}

const handleSearch = () => {
  const query = {}
  if (searchKeyword.value) query.keyword = searchKeyword.value
  if (searchGame.value) query.gameId = searchGame.value
  router.push({ path: '/product/list', query })
}

const loadGames = async () => {
  try {
    const res = await getGameList()
    if (res.data && res.data.length > 0) {
      games.value = res.data.map(g => ({ id: g.id, gameName: g.gameName, productCount: g.productCount || Math.floor(Math.random() * 300) + 10 }))
    }
  } catch (e) {
    console.error('加载游戏列表失败:', e)
  }
}

const loadHotProducts = async () => {
  hotLoading.value = true
  try {
    const res = await searchProduct({ page: 1, pageSize: 40 })
    if (res.data && res.data.records && res.data.records.length > 0) {
      hotProducts.value = res.data.records
      // 截取最新上架快报（取前6条）
      recentListings.value = res.data.records.slice(0, 6)
    } else {
      hotProducts.value = mockHotProducts
      recentListings.value = mockHotProducts.slice(0, 6)
    }
  } catch (e) {
    console.error('加载热门商品失败:', e)
    ElMessage.error('热门商品加载失败，显示推荐数据')
    hotProducts.value = mockHotProducts
    recentListings.value = mockHotProducts.slice(0, 6)
  } finally {
    hotLoading.value = false
  }
}

const loadHotSearchWords = async () => {
  try {
    const res = await getHotSearchWords()
    if (res.data && res.data.length > 0) {
      hotSearchWords.value = res.data.slice(0, 8)
    }
  } catch (e) {
    console.error('加载热搜词失败:', e)
  }
}

const loadRecommendedProducts = async () => {
  recommendLoading.value = true
  try {
    const res = await getRecommendedSlots('home_featured')
    if (res.data && res.data.length > 0) {
      recommendedProducts.value = res.data.slice(0, 4)
    }
  } catch (e) {
    console.error('加载推荐商品失败:', e)
  } finally {
    recommendLoading.value = false
  }
}

onMounted(() => {
  document.title = '首页 - 龙虾道具交易平台'
  loadGames()
  loadHotProducts()
  loadHotSearchWords()
  loadRecommendedProducts()
})
</script>

<style scoped>
.home-container { min-height: 100vh; background: #ffffff; }

/* 公告栏 */
.announcement-bar { background: #e94560; color: #fff; font-size: 12px; padding: 5px 0; }
.announcement-inner { max-width: 1280px; margin: 0 auto; padding: 0 24px; display: flex; align-items: center; justify-content: center; gap: 8px; }

/* 顶部导航（深色） */
.top-header { background: #ffffff; box-shadow: 0 2px 8px rgba(0,0,0,0.08); position: sticky; top: 0; z-index: 100; border-bottom: 1px solid #eee; }
.header-inner { max-width: 1280px; margin: 0 auto; padding: 0 24px; height: 64px; display: flex; align-items: center; gap: 0; }
.logo { width: 160px; flex-shrink: 0; display: flex; align-items: center; gap: 8px; text-decoration: none; }
.logo-icon { font-size: 28px; }
.logo-text { font-size: 18px; font-weight: 700; color: #1a1a2e; letter-spacing: 0.5px; }

.header-center { flex: 1; display: flex; justify-content: center; padding: 0 20px; }
.search-bar { display: flex; align-items: center; background: #f0f0f5; border-radius: 8px; overflow: hidden; width: 100%; max-width: 560px; height: 40px; box-shadow: none; border: 1px solid #e0e0e8; }
.search-select { width: 130px; border: none; }
.search-select :deep(.el-input__wrapper) { border-radius: 0; box-shadow: none !important; background: transparent !important; border: none !important; }
.search-input { flex: 1; border: none; }
.search-input :deep(.el-input__wrapper) { box-shadow: none !important; border-radius: 0; background: transparent !important; }
.search-btn { background: #667eea; border: none; color: #fff; border-radius: 0; height: 40px; width: 70px; font-weight: 700; }
.search-btn:hover { background: #5a70d4; }

.hot-search-bar { display: flex; align-items: center; gap: 6px; margin-top: 6px; padding: 0 4px; flex-wrap: wrap; }
.hot-label { font-size: 11px; color: #999; flex-shrink: 0; font-weight: 600; }
.hot-word { font-size: 12px; color: #666; cursor: pointer; padding: 3px 10px; border-radius: 20px; background: #f0f0f5; border: 1px solid #e0e0e0; transition: all 0.2s; }
.hot-word:hover { color: #667eea; background: rgba(102,126,234,0.08); border-color: rgba(102,126,234,0.2); }

.header-actions { width: 260px; flex-shrink: 0; display: flex; align-items: center; justify-content: flex-end; gap: 8px; }
.sell-btn { background: #667eea; border: none; color: #fff; font-weight: 700; }
.sell-btn:hover { background: #5a70d4; box-shadow: 0 4px 12px rgba(102,126,234,0.3); }
.action-text-btn { color: #666; font-size: 13px; }
.action-text-btn:hover { color: #667eea; }
.user-avatar-link { display: flex; align-items: center; }

/* 游戏选择栏 */
.game-bar { background: #ffffff; position: sticky; top: 64px; z-index: 99; border-bottom: 1px solid #eee; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.game-bar-inner { max-width: 1280px; margin: 0 auto; padding: 0 24px; display: flex; }
.game-tab { padding: 12px 24px; cursor: pointer; border-bottom: 3px solid transparent; transition: all 0.2s; color: #666; display: flex; align-items: center; gap: 6px; }
.game-tab:hover { color: #667eea; background: rgba(102,126,234,0.08); }
.game-tab.active { border-bottom-color: #667eea; color: #667eea; }
.game-tab-name { font-size: 14px; font-weight: 500; }
.game-tab-icon { font-size: 18px; }

/* 主体内容 */
.home-main { max-width: 1280px; margin: 0 auto; padding: 24px 24px 40px; }

/* 英雄横幅 */
.hero-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px; padding: 48px 52px; color: #fff; margin-bottom: 24px;
  position: relative; overflow: hidden; min-height: 280px; display: flex; align-items: center;
  border: none;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.35);
}
.hero-deco { position: absolute; inset: 0; pointer-events: none; }
.deco-circle { position: absolute; border-radius: 50%; background: rgba(255, 255, 255, 0.12); border: 1px solid rgba(255,255,255,0.18); }
.dc1 { width: 300px; height: 300px; top: -100px; right: -60px; }
.dc2 { width: 200px; height: 200px; bottom: -80px; right: 200px; }
.dc3 { width: 120px; height: 120px; top: 20px; right: 280px; }

.hero-content { position: relative; z-index: 1; display: flex; align-items: center; justify-content: space-between; width: 100%; gap: 40px; }
.hero-left { flex: 1; }
.hero-badge { display: inline-flex; align-items: center; gap: 6px; background: rgba(233, 69, 96, 0.35); border: 1.5px solid #e94560; border-radius: 20px; padding: 6px 16px; font-size: 14px; margin-bottom: 18px; color: #ffb3c1; font-weight: 600; letter-spacing: 0.5px; box-shadow: 0 0 16px rgba(233, 69, 96, 0.25); }
.hero-title { font-size: 34px; font-weight: 900; margin: 0 0 12px; line-height: 1.25; color: #ffffff; letter-spacing: -0.5px; text-shadow: 0 2px 12px rgba(0,0,0,0.25); }
.hero-title .highlight { color: #ffd700; text-shadow: 0 0 20px rgba(255, 215, 0, 0.5); font-weight: 900; }
.hero-sub { font-size: 16px; margin: 0 0 28px; line-height: 1.7; color: rgba(255,255,255,0.9); font-weight: 500; letter-spacing: 0.3px; }
.hero-ctas { display: flex; gap: 14px; }
.hero-btn-primary { background: #f0a500; border: none; color: #fff; font-weight: 800; box-shadow: 0 4px 20px rgba(240, 165, 0, 0.5); letter-spacing: 1px; }
.hero-btn-primary:hover { background: #ffb800; box-shadow: 0 0 28px rgba(255, 184, 0, 0.6); transform: translateY(-2px); }
.hero-btn-sell { background: rgba(255,255,255,0.12); border: 1.5px solid rgba(255,255,255,0.5); color: #fff; font-weight: 700; backdrop-filter: blur(4px); }
.hero-btn-sell:hover { background: rgba(255,255,255,0.22); border-color: rgba(255,255,255,0.8); transform: translateY(-2px); }

.hero-right { flex-shrink: 0; min-width: 200px; }
.hero-guarantees { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; width: 360px; }
.hero-guarantee-item { display: flex; align-items: center; gap: 12px; padding: 14px 16px; background: rgba(233, 69, 96, 0.15); border: 1.5px solid rgba(233, 69, 96, 0.35); border-radius: 14px; backdrop-filter: blur(6px); transition: all 0.3s; box-shadow: 0 2px 12px rgba(233, 69, 96, 0.15); }
.hero-guarantee-item:hover { border-color: rgba(233,69,96,0.6); background: rgba(233,69,96,0.22); box-shadow: 0 4px 20px rgba(233, 69, 96, 0.25); transform: translateY(-1px); }
.hg-icon { width: 44px; height: 44px; border-radius: 12px; background: rgba(233, 69, 96, 0.30); border: 1.5px solid rgba(233, 69, 96, 0.5); display: flex; align-items: center; justify-content: center; color: #ffd700; flex-shrink: 0; box-shadow: 0 0 12px rgba(233, 69, 96, 0.2); }
.hg-text { flex: 1; }
.hg-title { margin: 0 0 3px; font-size: 14px; font-weight: 800; color: #ffffff; letter-spacing: 0.5px; }
.hg-sub { margin: 0; font-size: 12px; color: rgba(255, 255, 255, 0.80); font-weight: 500; }

/* 区块通用 */
.section { margin-bottom: 32px; }
.section-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.see-more-btn { color: #e94560 !important; font-size: 13px; }
.see-more-btn:hover { color: #d63050 !important; }

/* 商品网格 */
.product-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 16px; }
.hot-product-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 16px; }
.product-card {
  background: #fff;
  border-radius: 10px; overflow: hidden;
  cursor: pointer; transition: all 0.25s;
  border: 1px solid #e8e8e8;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}
.product-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(0,0,0,0.12);
  border-color: #ddd;
}
.card-image { height: 160px; background: #f5f5f5; position: relative; overflow: hidden; }
.card-image img, .cover-img { width: 100%; height: 100%; object-fit: cover; background-size: cover; background-position: center; transition: transform 0.3s ease; }
.product-card:hover .card-image img { transform: scale(1.04); }
.image-placeholder { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; color: rgba(0,0,0,0.15); }
.card-badges { position: absolute; top: 8px; left: 8px; }
.type-tag { padding: 2px 8px; border-radius: 4px; font-size: 11px; color: #fff; background: #e94560; }
.type-tag.boost { background: #10b981; }
.type-tag.accompany { background: #3b82f6; }
.type-tag.recommended-tag { background: linear-gradient(135deg, #f0a500, #e6762a); }
.type-tag.goods { background: #f0a500; }
.card-body { padding: 12px; }
.product-title { margin: 0 0 8px; font-size: 13px; color: #1a1a1a; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-weight: 600; }
.product-tags { margin-bottom: 8px; }
.game-tag { display: inline-flex; align-items: center; gap: 3px; font-size: 11px; color: #667eea; background: rgba(102,126,234,0.1); padding: 2px 6px; border-radius: 3px; }
.card-bottom { margin-bottom: 8px; }
.price-wrap { display: flex; align-items: baseline; }
.price-sign { font-size: 13px; font-weight: 700; color: #e94560; }
.price { font-size: 20px; font-weight: 800; color: #e94560; }
.unit { font-size: 12px; color: #999; margin-left: 2px; }
.seller-row { display: flex; align-items: center; justify-content: space-between; }
.seller-name { font-size: 12px; color: #666; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 100px; }
.seller-name.clickable { cursor: pointer; color: #667eea; }
.seller-name.clickable:hover { text-decoration: underline; }
.rating-wrap { display: flex; align-items: center; gap: 2px; font-size: 12px; color: #f0a500; }
.star-icon { font-size: 12px; }

/* 为什么选我们 */
.why-section { background: #f9f9ff; border-radius: 14px; padding: 24px; border: 1px solid #e8e8f8; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.why-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-top: 4px; }
.why-card { text-align: center; padding: 20px 12px; border-radius: 10px; transition: all 0.3s; }
.why-card:hover { background: #fff; box-shadow: 0 4px 16px rgba(102,126,234,0.12); }
.why-icon-wrap { width: 52px; height: 52px; border-radius: 14px; display: flex; align-items: center; justify-content: center; margin: 0 auto 14px; border: 1px solid rgba(102,126,234,0.2); }
.why-card h4 { margin: 0 0 8px; font-size: 15px; font-weight: 700; color: #1a1a1a; }
.why-card p { margin: 0; font-size: 13px; color: #666; line-height: 1.6; }

/* 商家网格 */
.seller-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; }
.seller-card {
  background: #f5f5f5; border-radius: 12px; padding: 18px 14px;
  border: 1px solid #e0e0e0; box-shadow: none;
  transition: all 0.25s; display: flex; flex-direction: column; align-items: center; gap: 10px; text-align: center;
}
.seller-card:hover { box-shadow: 0 4px 20px rgba(102,126,234,0.15); border-color: #667eea; transform: translateY(-2px); }
.seller-avatar-wrap { width: 52px; height: 52px; border-radius: 50%; background: linear-gradient(135deg, #667eea, #764ba2); display: flex; align-items: center; justify-content: center; font-size: 20px; font-weight: 700; color: #fff; box-shadow: 0 4px 14px rgba(102,126,234,0.3); }
.seller-info-block { flex: 1; }
.seller-name-row { display: flex; align-items: center; justify-content: center; gap: 8px; margin-bottom: 8px; }
.seller-name { font-size: 14px; font-weight: 700; color: #1a1a1a; }
.seller-stats-row { display: flex; justify-content: center; align-items: center; gap: 12px; font-size: 12px; color: var(--text-secondary); margin-bottom: 8px; }
.seller-stats-row span { display: flex; align-items: center; gap: 3px; }
.rating-stars { color: var(--gold-color); font-size: 12px; }
.seller-game-tags { display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 12px; justify-content: center; }
.seller-game-tags span { font-size: 11px; padding: 2px 8px; background: rgba(233,69,96,0.08); color: var(--accent-color); border-radius: 10px; border: 1px solid rgba(233,69,96,0.2); }
.seller-btn { width: 100%; background: rgba(102,126,234,0.1); border: 1px solid #667eea; color: #667eea; font-weight: 700; }
.seller-btn:hover { background: #667eea; color: #fff; box-shadow: 0 4px 12px rgba(102,126,234,0.3); }

/* 页脚 */
.home-footer { background: #0f0f1a; color: #fff; padding: 0; border-top: 1px solid var(--border-color); }
.footer-main { max-width: 1280px; margin: 0 auto; }
.footer-content { display: flex; gap: 40px; padding: 40px 0 28px; }
.footer-brand { flex-shrink: 0; }
.footer-logo { font-size: 20px; font-weight: 700; margin-bottom: 8px; }
.footer-brand p { margin: 0 0 12px; font-size: 13px; color: var(--text-secondary); }
.footer-contact-row { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.contact-item { font-size: 12px; color: var(--text-secondary); }
.contact-divider { color: rgba(255,255,255,0.15); font-size: 12px; }
.footer-links { display: flex; gap: 60px; flex: 1; justify-content: center; }
.link-col { display: flex; flex-direction: column; gap: 10px; text-align: left; }
.link-col h4 { margin: 0 0 4px; font-size: 14px; color: var(--text-primary); }
.link-col span { font-size: 13px; color: var(--text-secondary); cursor: pointer; transition: color 0.2s; }
.link-col span:hover { color: var(--accent-color); }
.footer-bottom { border-top: 1px solid var(--border-color); padding: 16px 0 24px; }
.footer-cert { display: flex; align-items: center; justify-content: center; gap: 10px; margin-bottom: 8px; }
.cert-item { font-size: 12px; color: var(--text-muted); }
.cert-link { color: var(--text-secondary); text-decoration: none; }
.cert-link:hover { color: var(--accent-color); }
.cert-divider { color: rgba(255,255,255,0.15); font-size: 12px; }
.footer-copy { text-align: center; font-size: 12px; color: var(--text-muted); margin: 0; }

@media (max-width: 900px) {
  .header-inner { padding: 0 16px; }
  .header-center { display: none; }
  .home-main { padding: 16px; }
  .hero-banner { padding: 32px 24px; min-height: auto; }
  .hero-title { font-size: 22px; }
  .hero-right { display: none; }
  .why-grid { grid-template-columns: repeat(2, 1fr); }
  .seller-grid { grid-template-columns: repeat(2, 1fr); }
  .footer-links { gap: 24px; flex-wrap: wrap; }
}
</style>
