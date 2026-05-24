<template>
  <div class="seller-profile">
    <!-- 顶部导航 -->
    <div class="top-header">
      <div class="header-inner">
        <router-link to="/home" class="logo">🦞 龙虾道具交易平台</router-link>
        <div class="header-actions">
          <el-button @click="router.push({ path: '/user' })">个人中心</el-button>
        </div>
      </div>
    </div>

    <div class="seller-main">
      <!-- 左侧：卖家信息 -->
      <aside class="profile-sidebar">
        <el-card class="profile-card" shadow="never" v-loading="loading">
          <div v-if="sellerInfo" class="profile-inner">
            <div class="seller-avatar-lg">
              <el-avatar :size="80">{{ (sellerInfo.nickname || '匿名').charAt(0) }}</el-avatar>
            </div>
            <h2 class="seller-nickname">{{ sellerInfo.nickname || '匿名用户' }}</h2>
            <div class="seller-id">ID: {{ sellerInfo.id }}</div>

            <!-- 认证标识 -->
            <div class="auth-badges">
              <el-tag v-if="sellerInfo.realNameStatus === 1" type="success" size="small" effect="plain">
                <el-icon><Medal /></el-icon> 已实名认证
              </el-tag>
              <el-tag v-else-if="sellerInfo.realNameStatus" type="info" size="small" effect="plain">
                <el-icon><Warning /></el-icon> 未实名认证
              </el-tag>
              <el-tag v-if="sellerInfo.isVerified === 1" type="warning" size="small" effect="plain">
                🏅 官方认证
              </el-tag>
            </div>

            <!-- 店铺统计数据卡片 -->
            <div class="shop-stats">
              <div class="stats-card">
                <div class="stats-item">
                  <span class="stats-num">{{ sellerStats.totalTradeCount || 0 }}</span>
                  <span class="stats-label">已售订单</span>
                </div>
                <div class="stats-divider"></div>
                <div class="stats-item">
                  <span class="stats-num">¥{{ formatAmount(sellerStats.totalTradeAmount) }}</span>
                  <span class="stats-label">总交易额</span>
                </div>
                <div class="stats-divider"></div>
                <div class="stats-item">
                  <span class="stats-num">{{ computeGoodRate(sellerStats.reputationScore) }}%</span>
                  <span class="stats-label">好评率</span>
                </div>
                <div class="stats-divider"></div>
                <div class="stats-item">
                  <span class="stats-num">{{ sellerStats.productCount || 0 }}</span>
                  <span class="stats-label">在售商品</span>
                </div>
              </div>
            </div>

            <!-- 评分概览 -->
            <div class="rating-overview">
              <div class="rating-score">
                <span class="big-score">{{ Number(profileStats.avgRating || 0).toFixed(1) }}</span>
                <el-rate :model-value="Number(profileStats.avgRating || 0)" disabled show-score size="small" style="display:inline-flex" />
              </div>
              <div class="rating-count">共 {{ profileStats.totalReviews || 0 }} 条评价</div>
            </div>

            <!-- 详细统计 -->
            <div class="detail-stats">
              <div class="stat-row" v-if="sellerInfo.userLevel">
                <span class="stat-label">用户等级</span>
                <span class="stat-value">{{ userLevelText(sellerInfo.userLevel) }}</span>
              </div>
              <div class="stat-row">
                <span class="stat-label">注册时间</span>
                <span class="stat-value">{{ formatDate(sellerInfo.createTime) }}</span>
              </div>
            </div>

            <el-divider />

            <!-- 星级分布 -->
            <div class="star-distribution" v-if="profileStats.totalReviews > 0">
              <div class="dist-title">评分分布</div>
              <div class="star-row" v-for="star in [5,4,3,2,1]" :key="star">
                <span class="star-label">{{ star }}星</span>
                <div class="star-bar">
                  <div class="star-fill" :style="{ width: getStarPercent(star) + '%' }"></div>
                </div>
                <span class="star-count">{{ profileStats[starKey(star)] || 0 }}</span>
              </div>
            </div>
          </div>
          <el-empty v-else-if="!loading" description="该卖家信息不存在" />
        </el-card>
      </aside>

      <!-- 右侧：商品 + 评价 -->
      <main class="profile-content">
        <el-tabs v-model="activeTab" class="seller-tabs">
          <el-tab-pane label="在售商品" name="products">
            <div class="product-grid" v-loading="productsLoading">
              <template v-if="products.length > 0">
                <div class="product-card" v-for="p in products" :key="p.id" @click="router.push({ path: '/product/detail/' + p.id })">
                  <div class="p-cover">
                    <img v-if="p.coverImage" :src="p.coverImage" alt="封面" />
                    <div v-else class="cover-placeholder"><el-icon :size="32"><Picture /></el-icon></div>
                  </div>
                  <div class="p-info">
                    <div class="p-title">{{ p.title }}</div>
                    <div class="p-meta">
                      <span class="p-game">{{ p.gameName }}</span>
                      <span class="p-zone">{{ p.gameZone }}</span>
                    </div>
                    <div class="p-price">¥{{ p.price }}<span v-if="p.unit">/{{ p.unit }}</span></div>
                    <div class="p-sales">已售 {{ p.totalOrders || 0 }}</div>
                  </div>
                </div>
              </template>
              <el-empty v-else description="暂无在售商品" />
            </div>
            <el-pagination
              v-if="productTotal > pageSize"
              v-model:current-page="productPage"
              :page-size="pageSize"
              layout="prev, pager, next"
              :total="productTotal"
              @current-change="loadProducts"
              style="margin-top:16px;justify-content:center"
            />
          </el-tab-pane>

          <el-tab-pane :label="'评价 (' + (profileStats.totalReviews || 0) + ')'" name="reviews">
            <div class="review-list" v-loading="reviewsLoading">
              <template v-if="reviews.length > 0">
                <div class="review-item" v-for="r in reviews" :key="r.id">
                  <div class="review-header">
                    <div class="reviewer-avatar">{{ (r.fromUserNickname || '匿名').charAt(0) }}</div>
                    <div class="reviewer-info">
                      <span class="reviewer-name">{{ r.fromUserNickname || '匿名用户' }}</span>
                      <el-rate :model-value="r.rating" disabled size="small" />
                    </div>
                    <span class="review-time">{{ formatTime(r.createTime) }}</span>
                  </div>
                  <div class="review-body">{{ r.content }}</div>
                  <div v-if="r.replyContent" class="review-reply">
                    <span class="reply-label">卖家回复：</span>{{ r.replyContent }}
                  </div>
                </div>
              </template>
              <el-empty v-else description="暂无评价" />
            </div>
            <el-pagination
              v-if="reviewTotal > pageSize"
              v-model:current-page="reviewPage"
              :page-size="pageSize"
              layout="prev, pager, next"
              :total="reviewTotal"
              @current-change="loadReviews"
              style="margin-top:16px;justify-content:center"
            />
          </el-tab-pane>
        </el-tabs>
      </main>
    </div>
  </div>
</template>

<script setup>
document.title = '卖家资料 - 龙虾道具交易平台';

import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture, Medal, Warning } from '@element-plus/icons-vue'
import { getSellerReviews } from '@/api/review'
import { searchProduct } from '@/api/product'
import { getSellerStats, getSellerPublicInfo } from '@/api/stats'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const sellerInfo = ref(null)
const sellerStats = ref({})
const profileStats = ref({})
const products = ref([])
const productsLoading = ref(false)
const reviews = ref([])
const reviewsLoading = ref(false)
const activeTab = ref('products')
const pageSize = 12

const productPage = ref(1)
const productTotal = ref(0)
const reviewPage = ref(1)
const reviewTotal = ref(0)

const starKeys = ['fiveStar', 'fourStar', 'threeStar', 'twoStar', 'oneStar']

const starKey = (star) => starKeys[5 - star]

const formatDate = (time) => {
  if (!time) return '-'
  const d = new Date(time)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
}

const formatTime = (time) => {
  if (!time) return ''
  const d = new Date(time)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
}

const formatAmount = (val) => {
  if (!val && val !== 0) return '0.00'
  return Number(val).toFixed(2)
}

const computeGoodRate = (score) => {
  if (!score) return '100.0'
  // reputationScore is 1-5; treat 4+ as good
  const rate = (Number(score) / 5) * 100
  return rate.toFixed(1)
}

const userLevelText = (level) => {
  const map = { 1: '普通', 2: '铜牌', 3: '银牌', 4: '金牌' }
  return map[level] || '普通'
}

const getStarPercent = (star) => {
  const key = starKey(star)
  const count = profileStats.value[key] || 0
  const total = profileStats.value.totalReviews || 0
  return total > 0 ? Math.round(count / total * 100) : 0
}

const loadProfile = async () => {
  loading.value = true
  const sellerId = route.params.id
  try {
    // 并行加载卖家公开信息和统计数据
    const [pubRes, statsRes, reviewRes] = await Promise.all([
      getSellerPublicInfo(sellerId),
      getSellerStats(sellerId),
      getSellerReviews(sellerId).catch(() => null)
    ])
    if (pubRes.data) {
      sellerInfo.value = pubRes.data
    }
    if (statsRes.data) {
      sellerStats.value = statsRes.data
    }
    if (reviewRes?.data) {
      const d = reviewRes.data
      profileStats.value = {
        totalReviews: d.totalReviews || 0,
        avgRating: d.avgRating || 0,
        fiveStar: d.fiveStar || 0,
        fourStar: d.fourStar || 0,
        threeStar: d.threeStar || 0,
        twoStar: d.twoStar || 0,
        oneStar: d.oneStar || 0,
      }
      reviewTotal.value = d.total || 0
      reviews.value = d.records || []
    }
  } catch (e) {
    console.error('加载卖家信息失败:', e)
  } finally {
    loading.value = false
  }
}

const loadProducts = async () => {
  productsLoading.value = true
  try {
    const res = await searchProduct({ sellerId: route.params.id, page: productPage.value, size: pageSize })
    if (res.data) {
      const d = res.data
      products.value = d.records || []
      productTotal.value = d.total || 0
    }
  } catch (e) {
    // 降级：用 product/list
    try {
      const res = await searchProduct({ page: 1, size: 200 })
      if (res.data) {
        const sellerId = Number(route.params.id)
        const all = res.data.records || []
        const filtered = all.filter(p => p.sellerId === sellerId)
        const start = (productPage.value - 1) * pageSize
        products.value = filtered.slice(start, start + pageSize)
        productTotal.value = filtered.length
      }
    } catch (e2) {
      console.error('加载商品失败:', e2)
    }
  } finally {
    productsLoading.value = false
  }
}

const loadReviews = async () => {
  reviewsLoading.value = true
  try {
    const res = await getSellerReviews(route.params.id)
    if (res.data) {
      const d = res.data
      reviews.value = d.records || []
      reviewTotal.value = d.total || 0
      if (d.totalReviews !== undefined) profileStats.value.totalReviews = d.totalReviews
      if (d.avgRating !== undefined) profileStats.value.avgRating = d.avgRating
    }
  } catch (e) {
    console.error('加载评价失败:', e)
  } finally {
    reviewsLoading.value = false
  }
}

onMounted(() => {
  loadProfile()
  loadProducts()
})
</script>

<style scoped>
.seller-profile {
  min-height: 100vh;
  background: #f0f2f5;
}

.top-header {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 24px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  text-decoration: none;
}

.seller-main {
  max-width: 1280px;
  margin: 0 auto;
  padding: 24px;
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.profile-sidebar {
  width: 280px;
  flex-shrink: 0;
  position: sticky;
  top: 80px;
}

.profile-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.profile-card :deep(.el-card__body) {
  padding: 24px 20px;
}

.profile-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.seller-avatar-lg {
  margin-bottom: 12px;
}

.seller-nickname {
  margin: 0 0 4px;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.seller-id {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}

.auth-badges {
  margin-bottom: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  justify-content: center;
}

/* 店铺统计卡片 */
.shop-stats {
  width: 100%;
  margin: 8px 0;
  background: #f8f9fb;
  border-radius: 10px;
  padding: 12px 8px;
  border: 1px solid #f0f1f2;
}

.stats-card {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.stats-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.stats-num {
  font-size: 15px;
  font-weight: 700;
  color: #e94560;
  line-height: 1.2;
}

.stats-label {
  font-size: 11px;
  color: #999;
}

.stats-divider {
  display: none;
}

.rating-overview {
  margin: 12px 0;
}

.rating-score {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.big-score {
  font-size: 36px;
  font-weight: 700;
  color: #ff9800;
  line-height: 1;
}

.rating-count {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.detail-stats {
  width: 100%;
  margin-top: 8px;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  font-size: 13px;
  border-bottom: 1px solid #f5f5f5;
}

.stat-label { color: #999; }
.stat-value { color: #333; font-weight: 500; }

.star-distribution {
  width: 100%;
  margin-top: 8px;
}

.dist-title {
  font-size: 13px;
  color: #666;
  font-weight: 500;
  margin-bottom: 8px;
  text-align: left;
}

.star-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.star-label {
  font-size: 11px;
  color: #666;
  width: 24px;
  text-align: left;
}

.star-bar {
  flex: 1;
  height: 6px;
  background: #eee;
  border-radius: 3px;
  overflow: hidden;
}

.star-fill {
  height: 100%;
  background: #ff9800;
  border-radius: 3px;
}

.star-count {
  font-size: 11px;
  color: #999;
  width: 18px;
  text-align: right;
}

.profile-content {
  flex: 1;
  min-width: 0;
}

.seller-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
}

.seller-tabs :deep(.el-tabs__item) {
  font-size: 15px;
}

/* 商品网格 */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.product-card {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.2s;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.product-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.p-cover {
  height: 160px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.p-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  color: #ccc;
}

.p-info {
  padding: 10px 12px;
}

.p-title {
  font-size: 13px;
  color: #333;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}

.p-meta {
  display: flex;
  gap: 6px;
  font-size: 11px;
  color: #999;
  margin-bottom: 6px;
}

.p-meta span {
  background: rgba(102,126,234,0.08);
  padding: 2px 7px;
  border-radius: 4px;
  font-weight: 700;
  color: #667eea;
}

.p-price {
  font-size: 15px;
  font-weight: 700;
  color: #667eea;
  margin-bottom: 2px;
}

.p-price span {
  font-size: 11px;
  font-weight: 400;
  color: #999;
}

.p-sales {
  font-size: 11px;
  color: #999;
}

/* 评价列表 */
.review-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-item {
  background: #fff;
  border-radius: 10px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.review-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.reviewer-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #3b82f6;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  flex-shrink: 0;
}

.reviewer-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.reviewer-name {
  font-size: 13px;
  font-weight: 500;
  color: #333;
}

.review-time {
  font-size: 12px;
  color: #bbb;
}

.review-body {
  font-size: 14px;
  color: #555;
  line-height: 1.6;
}

.review-reply {
  margin-top: 8px;
  font-size: 13px;
  color: #666;
  background: #f9f9f9;
  padding: 8px 12px;
  border-radius: 6px;
  border-left: 3px solid #10b981;
}

.reply-label {
  font-weight: 500;
  color: #10b981;
  margin-right: 4px;
}
</style>