<template>
  <div class="detail-container">
    <!-- 顶部导航 -->
    <div class="top-header">
      <div class="header-inner">
        <router-link to="/home" class="logo">🦞 龙虾道具交易平台</router-link>
        <div class="header-actions">
          <el-button @click="router.push({ path: '/user' })">个人中心</el-button>
        </div>
      </div>
    </div>

    <!-- 锚点导航 -->
    <div class="anchor-nav">
      <div class="anchor-nav-inner">
        <a class="anchor-item" @click="scrollTo('product-info')">商品信息</a>
        <span class="anchor-sep">|</span>
        <a class="anchor-item" @click="scrollTo('seller-info')">卖家信息</a>
        <span class="anchor-sep">|</span>
        <a class="anchor-item" @click="scrollTo('user-reviews')">用户评价</a>
        <span class="anchor-sep">|</span>
        <a class="anchor-item" @click="scrollTo('platform-guarantee')">平台保障</a>
      </div>
    </div>

    <!-- 主内容 -->
    <div class="detail-main">
      <!-- 左侧边栏 -->
      <aside class="sidebar">
        <el-card class="menu-card" shadow="never">
          <template #header>
            <span class="menu-title">快捷导航</span>
          </template>
          <el-menu :default-active="activeMenu" router @select="activeMenu = $event">
            <el-menu-item index="/user">
              <el-icon><User /></el-icon>
              <span>个人信息</span>
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
          </el-menu>
        </el-card>
      </aside>

      <!-- 右侧内容 -->
      <main class="content">
        <el-card class="detail-card" shadow="never" v-loading="loading">
          <!-- 骨架屏加载态 -->
          <template v-if="loading">
            <div class="detail-inner">
              <div class="detail-left">
                <el-skeleton animated style="width:400px">
                  <template #template><el-skeleton-item variant="image" style="width:400px;height:400px;border-radius:12px" /></template>
                </el-skeleton>
              </div>
              <div class="detail-right">
                <el-skeleton animated style="width:100%" :rows="10" />
              </div>
            </div>
          </template>

          <template v-else-if="product">
            <div class="detail-inner">
              <!-- 左侧：图片展示 -->
              <div class="detail-left">
                <div class="main-image" @click="openViewer">
                  <img v-if="currentImage" :src="currentImage" alt="商品图片" />
                  <div v-else class="image-placeholder">
                    <el-icon :size="64"><Picture /></el-icon>
                  </div>
                  <div class="zoom-hint"><el-icon><ZoomIn /></el-icon> 点击放大</div>
                </div>
                <div class="image-list" v-if="product.imagesArray && product.imagesArray.length > 1">
                  <div
                    v-for="(img, idx) in product.imagesArray"
                    :key="idx"
                    class="thumbnail"
                    :class="{ active: currentImage === img }"
                    @click="currentImage = img"
                  >
                    <img :src="img" alt="商品图片" />
                  </div>
                </div>
                <!-- 图片查看器 -->
                <el-image-viewer v-if="showViewer" :url-list="product.imagesArray" @close="showViewer = false" />
              </div>

              <!-- 右侧：商品信息 -->
              <div class="detail-right" id="product-info">
                <div class="product-header">
                  <el-tag v-if="product.productType" :type="getTypeColor(product.productType)" size="small" effect="dark">
                    {{ typeMap[product.productType] || product.productType }}
                  </el-tag>
                  <h1 class="product-title">{{ product.title }}</h1>
                </div>

                <div class="price-section">
                  <div class="price-main">
                    <span class="price-yen">¥</span><span class="price">{{ product.price }}</span>
                    <span v-if="product.unit" class="price-unit">/{{ product.unit }}</span>
                  </div>
                  <div class="price-badges">
                    <span class="price-badge" v-if="product.minDeposit > 0">担保价 ¥{{ product.minDeposit }}</span>
                    <span class="stock-badge" :class="getStockClass(product.stock)">
                      {{ product.stock > 10 ? '库存充足' : product.stock > 0 ? '仅剩 ' + product.stock + ' 件' : '已售罄' }}
                    </span>
                  </div>
                  <!-- 限时折扣倒计时 -->
                  <div class="countdown-bar" v-if="product.discountEndTime && countdownTime !== '已结束'">
                    <el-icon><Timer /></el-icon>
                    <span>限时折扣剩余：</span>
                    <span class="countdown-time">{{ countdownTime }}</span>
                  </div>
                  <div class="countdown-ended" v-else-if="product.discountEndTime && countdownTime === '已结束'">
                    <span class="countdown-ended-text">⚠ 限时折扣已结束</span>
                  </div>
                </div>

                <div class="product-meta-grid">
                  <div class="meta-row">
                    <span class="meta-icon">🎮</span>
                    <span class="meta-label">游戏</span>
                    <span class="meta-value">{{ product.gameName || '-' }}</span>
                  </div>
                  <div class="meta-row" v-if="product.gameZone || product.server">
                    <span class="meta-icon">🌐</span>
                    <span class="meta-label">区服</span>
                    <span class="meta-value">{{ [product.gameZone, product.server].filter(Boolean).join(' / ') }}</span>
                  </div>
                  <div class="meta-row">
                    <span class="meta-icon">📦</span>
                    <span class="meta-label">销量</span>
                    <span class="meta-value success">{{ product.totalOrders || 0 }} 笔</span>
                  </div>
                  <div class="meta-row" v-if="product.platform">
                    <span class="meta-icon">💻</span>
                    <span class="meta-label">平台</span>
                    <span class="meta-value">{{ product.platform }}</span>
                  </div>
                </div>

                <div class="guarantee-bar">
                  <span class="guarantee-item">🔒 平台托管</span>
                  <span class="guarantee-item">🛡️ 资金保障</span>
                  <span class="guarantee-item">⚡ 快速发货</span>
                  <span class="guarantee-item">📞 7x24客服</span>
                </div>
                <div class="product-id-row">
                  <span class="id-label">商品编号：</span>
                  <span class="id-value">{{ product.id }}</span>
                  <span class="id-tip">（联系客服时请提供此编号）</span>
                </div>

                <el-divider />

                <!-- 卖家信息 -->
                <div class="seller-block" id="seller-info" @click="router.push({ path: '/seller/' + product.sellerId })" style="cursor:pointer">
                  <div class="seller-avatar-wrap">
                    <div class="seller-avatar">
                      <el-avatar :size="48">{{ (product.sellerNickname || '匿名').charAt(0) }}</el-avatar>
                    </div>
                    <div class="seller-level-badge">
                      <el-tag size="small" type="warning">Lv.5</el-tag>
                    </div>
                  </div>
                  <div class="seller-info">
                    <div class="seller-name-row">
                      <span class="seller-name">{{ product.sellerNickname || '匿名用户' }}</span>
                      <span class="seller-tag">认证服务商</span>
                      <span v-if="product.sellerIsVerified === 1" class="verified-badge">🏅 官方认证</span>
                      <span v-if="product.sellerRealNameVerified === 1" class="realname-badge">✅ 已实名</span>
                    </div>
                    <div class="seller-rep-row">
                      <span class="rep-label">信誉</span>
                      <div class="rep-bar-wrap">
                        <div class="rep-bar">
                          <div class="rep-fill" :style="{ width: ((product.sellerReputationScore || 5) / 5 * 100) + '%' }"></div>
                        </div>
                        <span class="rep-score">{{ product.sellerReputationScore || '5.0' }}</span>
                      </div>
                    </div>
                    <div class="seller-stats-row">
                      <span class="stat-item">商品 {{ product.totalOrders || 0 }}</span>
                      <span class="stat-divider">|</span>
                      <span class="stat-item" v-if="reviewStats.totalReviews > 0">{{ reviewStats.totalReviews }} 评价</span>
                      <span class="stat-item" v-else>暂无评价</span>
                    </div>
                  </div>
                  <el-icon class="seller-arrow"><ArrowRight /></el-icon>
                </div>

                <el-divider />

                <!-- 操作按钮 -->
                <div class="action-buttons">
                  <el-button type="primary" size="large" class="buy-btn" @click="handleBuy" :disabled="product.stock === 0">
                    <el-icon><ShoppingCart /></el-icon>
                    {{ product.stock === 0 ? '已售罄' : '立即购买' }}
                  </el-button>
                  <el-button size="large" @click="handleFavorite" class="fav-btn">
                    <el-icon><Star /></el-icon>
                    收藏
                  </el-button>
                  <el-button size="large" @click="handleContact" class="contact-btn">
                    <el-icon><ChatDotRound /></el-icon>
                    咨询客服
                  </el-button>
                  <el-button size="large" @click="handleShare" class="share-btn">
                    <el-icon><Share /></el-icon>
                    分享
                  </el-button>
                </div>

                <div class="seller-more-link" @click="router.push({ path: '/product/list', query: { sellerId: product.sellerId } })">
                  <span>查看该卖家所有商品 ({{ product.totalOrders || 0 }} 件)</span>
                  <el-icon><ArrowRight /></el-icon>
                </div>
              </div>

              <!-- 侧边推荐位 -->
              <div class="detail-sidebar" v-if="sideRecommendations.length > 0">
                <div class="sidebar-title">
                  <el-icon><Goods /></el-icon> 相关推荐
                </div>
                <div class="sidebar-item"
                  v-for="item in sideRecommendations"
                  :key="item.id"
                  @click="router.push({ path: '/product/detail/' + item.id })">
                  <img v-if="item.coverImage" :src="item.coverImage" class="sidebar-img" />
                  <div v-else class="sidebar-img-placeholder"><el-icon :size="20"><Picture /></el-icon></div>
                  <div class="sidebar-info">
                    <div class="sidebar-product-title">{{ item.title }}</div>
                    <div class="sidebar-price">¥{{ item.price }}<span v-if="item.unit">/{{ item.unit }}</span></div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 优惠券选择弹窗 -->
            <el-dialog v-model="showCouponDialog" title="选择优惠券" width="480px" :close-on-click-modal="true">
              <div v-if="couponLoading" style="text-align:center;padding:20px;color:#999;">加载中...</div>
              <div v-else-if="availableCoupons.length === 0" style="text-align:center;padding:20px;color:#999;">
                暂无符合条件的优惠券
              </div>
              <div v-else class="coupon-select-list">
                <div
                  v-for="c in availableCoupons"
                  :key="c.id"
                  class="coupon-select-item"
                  :class="{ selected: selectedCouponId === c.id }"
                  @click="selectedCouponId = c.id"
                >
                  <div class="csel-left">
                    <span class="csel-value" v-if="c.discountValue">¥{{ c.discountValue }}</span>
                    <span class="csel-value" v-else-if="c.discountRate">{{ (c.discountRate * 10).toFixed(1) }}折</span>
                    <span class="csel-name">{{ c.couponName }}</span>
                  </div>
                  <div class="csel-right">
                    <div class="csel-rule" v-if="c.minAmount">满{{ c.minAmount }}元可用</div>
                    <div class="csel-exp">有效期至：{{ formatTime(c.endTime) }}</div>
                  </div>
                  <el-icon v-if="selectedCouponId === c.id" class="csel-check"><Check /></el-icon>
                </div>
              </div>
              <template #footer>
                <el-button @click="showCouponDialog = false">不使用优惠券</el-button>
                <el-button type="primary" @click="confirmBuy">确认下单</el-button>
              </template>
            </el-dialog>

            <!-- 平台保障声明 -->
            <div class="platform-guarantee" id="platform-guarantee">
              <div class="guarantee-title">平台保障</div>
              <div class="guarantee-items">
                <div class="guarantee-item-large">
                  <div class="gi-icon">🔒</div>
                  <div class="gi-text"><p class="gi-title">资金托管</p><p class="gi-sub">买家付款后资金由平台托管，卖家发货后才会打款</p></div>
                </div>
                <div class="guarantee-item-large">
                  <div class="gi-icon">⚡</div>
                  <div class="gi-text"><p class="gi-title">极速发货</p><p class="gi-sub">自动发货系统，订单支付后秒到账，无需等待</p></div>
                </div>
                <div class="guarantee-item-large">
                  <div class="gi-icon">🛡️</div>
                  <div class="gi-text"><p class="gi-title">安全保障</p><p class="gi-sub">专业团队监控，交易过程全程保护</p></div>
                </div>
                <div class="guarantee-item-large">
                  <div class="gi-icon">📞</div>
                  <div class="gi-text"><p class="gi-title">7×24客服</p><p class="gi-sub">任何问题随时咨询，交易过程全程陪伴</p></div>
                </div>
                <div class="guarantee-item-large">
                  <div class="gi-icon">🛡️</div>
                  <div class="gi-text"><p class="gi-title">交易安全保障险</p><p class="gi-sub">每笔交易平台免费赠送安全保障，投诉可申请赔付</p></div>
                </div>
              </div>
            </div>

            <!-- 购买须知 -->
            <div class="purchase-notice">
              <div class="notice-title">
                <el-icon><InfoFilled /></el-icon>
                购买须知
              </div>
              <el-descriptions :column="2" border size="small" class="notice-descriptions">
                <el-descriptions-item label="发货时间">
                  <span class="notice-value">订单支付后 1-3 小时内完成发货，夜间订单顺延至次日</span>
                </el-descriptions-item>
                <el-descriptions-item label="库存说明">
                  <span class="notice-value">以上库存为实时数据，下单前请确认库存充足</span>
                </el-descriptions-item>
                <el-descriptions-item label="售后政策">
                  <span class="notice-value">虚拟商品发货后不支持退款；如商品存在问题请联系客服处理</span>
                </el-descriptions-item>
                <el-descriptions-item label="交易安全">
                  <span class="notice-value">全程平台托管，资金有保障；拒绝私下交易，谨防诈骗</span>
                </el-descriptions-item>
                <el-descriptions-item label="商品来源">
                  <span class="notice-value">所有商品由认证服务商提供，平台全程监管</span>
                </el-descriptions-item>
                <el-descriptions-item label="异常处理">
                  <span class="notice-value">遇到问题请保留凭证，联系 7×24 在线客服协助解决</span>
                </el-descriptions-item>
              </el-descriptions>
            </div>

            <!-- 商品描述 -->
            <div class="description-section">
              <h3 class="section-title" id="user-reviews">商品详情</h3>
              <div class="description-content">
                <p v-if="product.description">{{ product.description }}</p>
                <p v-else class="no-desc">暂无详细描述</p>
              </div>
            </div>

            <!-- 商品问答 -->
            <div class="qa-section">
              <div class="qa-title">
                <el-icon><QuestionFilled /></el-icon>
                商品问答 <span class="qa-count">({{ qaList.length }})</span>
              </div>

              <!-- 已有问答列表 -->
              <div class="qa-list">
                <div class="qa-item" v-for="qa in qaList" :key="qa.id">
                  <div class="qa-q">
                    <el-icon class="qa-icon"><ChatDotRound /></el-icon>
                    <span class="qa-q-text">{{ qa.question }}</span>
                    <span class="qa-time">{{ qa.time }}</span>
                  </div>
                  <div class="qa-a" v-if="qa.answer">
                    <el-icon class="qa-icon qa-icon-a"><ChatLineRound /></el-icon>
                    <span class="qa-a-text">{{ qa.answer }}</span>
                  </div>
                </div>
              </div>

              <!-- 提问入口 -->
              <div class="qa-form" v-if="showQaForm">
                <el-input
                  v-model="newQuestion"
                  type="textarea"
                  :rows="2"
                  placeholder="请输入您的问题（如：烽火区有货吗？支持退款吗？）"
                  maxlength="200"
                  show-word-limit
                />
                <div class="qa-form-actions">
                  <el-button size="small" @click="showQaForm = false; newQuestion = ''">取消</el-button>
                  <el-button size="small" type="primary" @click="submitQuestion" :disabled="!newQuestion.trim()">提交问题</el-button>
                </div>
              </div>

              <div class="qa-ask-btn" v-else @click="showQaForm = true">
                <el-icon><QuestionFilled /></el-icon>
                我要提问
              </div>
            </div>

            <!-- 相似商品推荐 -->
            <div class="similar-section" v-if="similarProducts.length > 0">
              <div class="similar-title">
                <el-icon><Goods /></el-icon>
                <span>相似商品</span>
                <span class="similar-sub">同游戏更多好货</span>
              </div>
              <div class="similar-grid">
                <div
                  class="similar-card"
                  v-for="p in similarProducts"
                  :key="p.id"
                  @click="router.push('/product/detail/' + p.id)"
                >
                  <div class="similar-img-wrap">
                    <img
                      :src="JSON.parse(p.images || '[]')[0] || 'https://ViaPython/L/200'"
                      class="similar-img"
                      alt=""
                    />
                    <div class="similar-price">¥{{ p.price }}</div>
                  </div>
                  <div class="similar-info">
                    <div class="similar-product-title">{{ p.title }}</div>
                    <div class="similar-game">{{ p.gameName }}</div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 评价列表 -->
            <div class="description-section">
              <h3 class="section-title">商品评价 <span v-if="reviews.length > 0" class="review-count">({{ reviews.length }})</span></h3>
              <div class="review-summary" v-if="reviewStats.totalReviews > 0">
                <div class="summary-left">
                  <div class="avg-rating">{{ Number(reviewStats.avgRating).toFixed(1) }}</div>
                  <el-rate :model-value="Number(reviewStats.avgRating)" disabled show-score size="small" />
                  <div class="total-label">共 {{ reviewStats.totalReviews }} 条评价</div>
                </div>
                <div class="summary-right">
                  <div class="star-row" v-for="star in [5,4,3,2,1]" :key="star">
                    <span class="star-label">{{ star }}星</span>
                    <div class="star-bar">
                      <div class="star-fill" :style="{ width: reviewStats.totalReviews > 0 ? (reviewStats[['fiveStar','fourStar','threeStar','twoStar','oneStar'][5-star]] / reviewStats.totalReviews * 100) + '%' : '0%' }"></div>
                    </div>
                    <span class="star-count">{{ reviewStats[['fiveStar','fourStar','threeStar','twoStar','oneStar'][5-star]] || 0 }}</span>
                  </div>
                </div>
              </div>
              <!-- 评价列表 -->
              <div class="review-list" v-if="reviews.length > 0">
                <div class="review-item" v-for="r in reviews" :key="r.id">
                  <div class="review-header">
                    <div class="reviewer-avatar">{{ (r.anonymousName || r.reviewerNickname || '匿名').charAt(0) }}</div>
                    <div class="reviewer-info">
                      <span class="reviewer-name">{{ r.anonymousName || r.reviewerNickname || '匿名用户' }}</span>
                      <el-rate :model-value="r.rating" disabled size="small" style="display:inline-flex" />
                    </div>
                    <span class="review-time">{{ formatReviewTime(r.createTime) }}</span>
                  </div>
                  <div class="review-body">{{ r.content }}</div>
                  <div class="review-reply" v-if="r.replyContent">
                    <span class="reply-label">卖家回复：</span>{{ r.replyContent }}
                  </div>
                </div>
              </div>
              <div v-else-if="!reviewLoading" class="no-reviews">暂无评价，快来购买并发表评价吧～</div>
            </div>
          </template>

          <template v-else-if="!loading">
            <el-empty description="商品不存在或已下架" />
          </template>
        </el-card>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture, Star, ChatDotRound, ArrowRight, ShoppingCart, Share, ZoomIn, Timer, QuestionFilled, InfoFilled, ChatLineRound, Check } from '@element-plus/icons-vue'
import { getOrCreateSessionByProduct } from '@/api/im'
import { getProductDetail, getProductList } from '@/api/product'
import { getProductReviews, getSellerReviews } from '@/api/review'
import { getRecommendedSlots } from '@/api/recommend'
import { createOrder } from '@/api/order'
import { getUsableCouponsForOrder } from '@/api/coupon'
import { useUserStore } from '@/stores/user'
import { User, Wallet, Goods, List } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const activeMenu = ref('/product/list')
const loading = ref(false)
const product = ref(null)
const currentImage = ref('')
const reviewStats = ref({ totalReviews: 0, avgRating: 0, fiveStar: 0, fourStar: 0, threeStar: 0, twoStar: 0, oneStar: 0 })
const reviews = ref([])
const reviewLoading = ref(false)
const sideRecommendations = ref([])
const showViewer = ref(false)
const similarProducts = ref([])
const similarLoading = ref(false)
const countdownEndTime = ref(null)
const countdownTime = ref('')
let countdownTimer = null

// 购买优惠券选择
const showCouponDialog = ref(false)
const availableCoupons = ref([])
const selectedCouponId = ref(null)
const couponLoading = ref(false)

// 商品问答
const qaList = ref([
  { id: 1, question: '这个区有没有货？', answer: '有的，烽火区货源充足，下单后1小时内发货。', time: '2026-05-02 14:30' },
  { id: 2, question: '支持退款吗？', answer: '若未发货可申请退款，发货后因虚拟商品特殊性不支持退款，请知悉。', time: '2026-05-01 10:15' }
])
const qaLoading = ref(false)
const showQaForm = ref(false)
const newQuestion = ref('')

const submitQuestion = () => {
  if (!newQuestion.value.trim()) return
  ElMessage.success('您的问题已提交，客服将尽快回复！')
  showQaForm.value = false
  newQuestion.value = ''
}

// 锚点滚动导航
const scrollTo = (id) => {
  const el = document.getElementById(id)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

// 限时折扣倒计时
const initCountdown = (endTime) => {
  if (!endTime) return
  countdownEndTime.value = new Date(endTime).getTime()
  clearInterval(countdownTimer)
  const update = () => {
    const now = Date.now()
    const diff = countdownEndTime.value - now
    if (diff <= 0) {
      countdownTime.value = '已结束'
      clearInterval(countdownTimer)
      return
    }
    const h = Math.floor(diff / 3600000)
    const m = Math.floor((diff % 3600000) / 60000)
    const s = Math.floor((diff % 60000) / 1000)
    countdownTime.value = `${String(h).padStart(2, '0')}时${String(m).padStart(2, '0')}分${String(s).padStart(2, '0')}秒`
  }
  update()
  countdownTimer = setInterval(update, 1000)
}

// 启动倒计时（如果有折扣信息）
const startCountdownIfNeeded = () => {
  if (product.value?.discountEndTime) {
    initCountdown(product.value.discountEndTime)
  }
}

const typeMap = {
  goods: '游戏币',
  goods: '装备道具',
  boost: '代练服务'
}

const mockProduct = {
  id: 1,
  title: '三角洲行动 哈夫币 100万',
  price: 80.00,
  unit: '万',
  gameName: '三角洲行动',
  gameZone: '烽火区',
  productType: 'goods',
  description: '正宗哈夫币代肝服务，专业团队操作，包拿到货。可跑刀可钢枪，按需求定制。\n交易方式：全程平台托管，安全有保障。\n交付时间：一般1-3小时内完成。',
  stock: 99,
  totalOrders: 58,
  sellerNickname: '专业搬砖商',
  reputationScore: '4.9',
  images: []
}

const getTypeColor = (type) => {
  const map = { goods: '', boost: 'success', accompany: 'primary' }
  return map[type] || ''
}

const openViewer = () => {
  if (currentImage.value) showViewer.value = true
}

const loadSimilarProducts = async () => {
  if (!product.value?.gameId) return
  similarLoading.value = true
  try {
    const res = await getProductList({ page: 1, pageSize: 50, gameId: product.value.gameId })
    if (res.data) {
      const all = res.data.records || res.data.list || []
      similarProducts.value = all.filter(p => p.id !== product.value.id).slice(0, 4)
    }
  } catch (e) {
    console.error('加载相似商品失败', e)
  } finally {
    similarLoading.value = false
  }
}

const handleBuy = async () => {
  const userStore = useUserStore()
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login' })
    return
  }
  if (!product.value) {
    ElMessage.error('商品信息加载中，请稍后')
    return
  }
  if (product.value.stock <= 0) {
    ElMessage.warning('库存不足')
    return
  }
  // 加载可用优惠券
  couponLoading.value = true
  availableCoupons.value = []
  selectedCouponId.value = null
  try {
    const res = await getUsableCouponsForOrder(product.value.price)
    if (res.code === 200 && res.data) {
      availableCoupons.value = res.data
    }
  } catch (e) {
    console.error('加载优惠券失败', e)
  } finally {
    couponLoading.value = false
  }
  showCouponDialog.value = true
}

const confirmBuy = async () => {
  showCouponDialog.value = false
  try {
    const res = await createOrder({ productId: product.value.id, quantity: 1, couponId: selectedCouponId.value || null })
    if (res.data && res.data.id) {
      ElMessage.success('订单创建成功，正在跳转...')
      router.push({ path: '/order/detail/' + res.data.id })
    } else {
      ElMessage.error(res.message || '创建订单失败')
    }
  } catch (e) {
    console.error('创建订单失败:', e)
    ElMessage.error(e.message || '创建订单失败，请稍后重试')
  }
}

const handleContact = async () => {
  const userStore = useUserStore()
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录', () => { router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } }) })
    return
  }
  if (userStore.userId === product.value?.sellerId) {
    ElMessage.info('这是您自己的商品')
    return
  }
  try {
    const res = await getOrCreateSessionByProduct(product.value.id)
    if (res.code === 200 && res.data?.id) {
      router.push({ path: '/im', query: { sessionId: res.data.id } })
    } else {
      ElMessage.error(res.message || '发起聊天失败')
    }
  } catch (e) {
    ElMessage.warning('功能升级中，请通过订单与卖家沟通')
  }
}

const handleShare = () => {
  const url = window.location.href
  const title = product.value?.title || '商品分享'
  const text = `【${title}】¥${product.value?.price} - 龙虾道具交易平台`
  if (navigator.clipboard) {
    navigator.clipboard.writeText(url)
    ElMessage.success('商品链接已复制到剪贴板')
  } else {
    ElMessage.warning('请手动复制链接：' + url)
  }
  // 尝试调起微信分享（兼容模式）
  if (window.WeChatJSBridge) {
    try {
      window.WeChatJSBridge.invoke('shareTimeline', {
        title: title,
        desc: text,
        link: url,
        imgUrl: product.value?.coverImage || ''
      })
    } catch (e) { /* ignore */ }
  }
}

const getStockClass = (stock) => {
  if (stock === 0) return 'no-stock'
  if (stock <= 10) return 'low-stock'
  return 'in-stock'
}

const handleFavorite = () => {
  ElMessage.success('已添加到收藏')
}

const formatTime = (time) => {
  if (!time) return ''
  const d = new Date(time)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const formatReviewTime = (time) => {
  return formatTime(time)
}

const loadProduct = async () => {
  loading.value = true
  try {
    const id = route.params.id
    const res = await getProductDetail(id)
    if (res.data) {
      product.value = res.data
      // images 是 JSON 字符串，需解析为数组
      if (product.value.images) {
        try { product.value.imagesArray = JSON.parse(product.value.images) } catch { product.value.imagesArray = [] }
      } else { product.value.imagesArray = [] }
      if (!product.value.imagesArray.length && product.value.coverImage) {
        product.value.imagesArray = [product.value.coverImage]
      }
      currentImage.value = product.value.coverImage || (product.value.imagesArray[0]) || ''
      // SEO：设置页面标题为商品名称
      document.title = `${product.value.title} - 龙虾道具交易平台`
      startCountdownIfNeeded()
    loadReviewStats(product.value.sellerId)
    } else {
      product.value = mockProduct
      currentImage.value = ''
    }
  } catch (e) {
    console.error('加载商品详情失败:', e)
    product.value = mockProduct
    currentImage.value = ''
  } finally {
    loading.value = false
  }
  loadReviews(route.params.id)
}

const loadReviewStats = async (sellerId) => {
  if (!sellerId) return
  try {
    const res = await getSellerReviews(sellerId)
    if (res.data) {
      const d = res.data
      reviewStats.value = {
        totalReviews: d.totalReviews || 0,
        avgRating: d.avgRating || 0,
        fiveStar: d.fiveStar || 0,
        fourStar: d.fourStar || 0,
        threeStar: d.threeStar || 0,
        twoStar: d.twoStar || 0,
        oneStar: d.oneStar || 0,
      }
    }
  } catch (e) {
    console.error('加载评分统计失败:', e)
  }
}

const loadReviews = async (productId) => {
  reviewLoading.value = true
  try {
    const res = await getProductReviews(productId)
    if (res.data) {
      reviews.value = res.data?.records || res.data || []
    }
  } catch (e) {
    console.error('加载评价失败:', e)
  } finally {
    reviewLoading.value = false
  }
}

const loadSideRecommendations = async () => {
  try {
    const res = await getRecommendedSlots('product_detail_side')
    if (res.data && Array.isArray(res.data)) {
      sideRecommendations.value = res.data.slice(0, 5)
    }
  } catch (e) {
    console.error('加载推荐失败:', e)
  }
}

onMounted(() => {
  loadProduct()
  loadSideRecommendations()
  loadSimilarProducts()

  // 默认标题，加载后会更新为商品名称
  document.title = '商品详情 - 龙虾道具交易平台'
})

onUnmounted(() => {
  clearInterval(countdownTimer)
})
</script>

<style scoped>
.detail-container {
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

.detail-main {
  max-width: 1280px;
  margin: 0 auto;
  padding: 24px;
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.sidebar {
  width: 220px;
  flex-shrink: 0;
  position: sticky;
  top: 80px;
}

.menu-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.menu-card :deep(.el-card__header) {
  padding: 14px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.menu-card :deep(.el-card__body) {
  padding: 0;
}

.menu-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.menu-card :deep(.el-menu) {
  border: none;
}

.content {
  flex: 1;
  min-width: 0;
}

.detail-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.detail-card :deep(.el-card__body) {
  padding: 24px;
}

.detail-inner {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

/* ========== 左侧图片 ========== */
.detail-left {
  width: 400px;
  flex-shrink: 0;
}

.main-image {
  width: 400px;
  height: 400px;
  background: #f5f7fa;
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: zoom-in;
  position: relative;
}

.main-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.main-image:hover img { transform: scale(1.03); }

.zoom-hint {
  position: absolute; bottom: 10px; right: 10px;
  background: rgba(0,0,0,0.55); color: #fff;
  padding: 4px 10px; border-radius: 12px;
  font-size: 11px; display: flex; align-items: center; gap: 4px;
  opacity: 0; transition: opacity 0.2s; pointer-events: none;
}
.main-image:hover .zoom-hint { opacity: 1; }

.image-placeholder {
  color: #ccc;
}

.image-list {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  flex-wrap: wrap;
}

.thumbnail {
  width: 72px;
  height: 72px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: border-color 0.2s;
}

.thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumbnail.active {
  border-color: #667eea;
}

/* ========== 右侧信息 ========== */
.detail-right {
  flex: 1;
  min-width: 0;
}

.product-header {
  margin-bottom: 16px;
}

.product-header .el-tag {
  margin-bottom: 8px;
}

.product-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
}

.price-block {
  display: flex;
  align-items: baseline;
  margin-bottom: 20px;
}

.price-section {
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f2ff 100%);
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 20px;
}

.price-main {
  display: flex;
  align-items: baseline;
  margin-bottom: 8px;
}

.price-yen {
  font-size: 20px;
  font-weight: 700;
  color: #667eea;
}

.price {
  font-size: 40px;
  font-weight: 800;
  color: #667eea;
  line-height: 1;
}

.price-unit {
  font-size: 16px;
  color: #999;
  margin-left: 4px;
}

.price-badges {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.price-badge {
  background: #fff;
  border: 1px solid #667eea;
  color: #667eea;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}

.stock-badge {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}

.stock-badge.in-stock { background: #e8f5e9; color: #4caf50; }
.stock-badge.low-stock { background: #fff3e0; color: #ff9800; }
.stock-badge.no-stock { background: #ffebee; color: #f44336; }

/* ========== 锚点导航 ========== */
.anchor-nav {
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  position: sticky;
  top: 56px;
  z-index: 99;
}

.anchor-nav-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 24px;
  height: 42px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.anchor-item {
  color: #666;
  text-decoration: none;
  cursor: pointer;
  padding: 6px 4px;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}

.anchor-item:hover {
  color: #667eea;
  border-bottom-color: #667eea;
}

.anchor-sep {
  color: #ddd;
  user-select: none;
}

/* ========== 限时折扣倒计时 ========== */
.countdown-bar {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 10px;
  padding: 6px 12px;
  background: linear-gradient(135deg, #fff3e0, #ffe0b2);
  border: 1px solid #ffb74d;
  border-radius: 8px;
  font-size: 13px;
  color: #e65100;
}

.countdown-time {
  font-weight: 700;
  font-size: 15px;
  font-family: 'Courier New', monospace;
  letter-spacing: 1px;
}

.countdown-ended {
  margin-top: 10px;
  padding: 6px 12px;
  background: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 13px;
}

.countdown-ended-text {
  color: #999;
}

.product-meta-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 16px;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.meta-icon {
  font-size: 14px;
  width: 20px;
  text-align: center;
}

.meta-label {
  color: #999;
  min-width: 36px;
}

.meta-value {
  color: #333;
}

.meta-value.success {
  color: #4caf50;
  font-weight: 500;
}

.guarantee-bar {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  padding: 10px 0;
}

.guarantee-item {
  font-size: 12px;
  color: #666;
  display: flex;
  align-items: center;
  gap: 4px;
}

.action-buttons {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.seller-block {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 8px;
  padding: 14px;
  background: #fafafa;
  border-radius: 10px;
  border: 1px solid #f0f0f0;
  transition: all 0.2s;
}

.seller-block:hover {
  background: #f0f2ff;
  border-color: #667eea;
}

.seller-avatar-wrap {
  position: relative;
  display: inline-block;
}

.seller-level-badge {
  position: absolute;
  bottom: -4px;
  right: -4px;
}

.seller-info {
  flex: 1;
}

.seller-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.seller-name {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.seller-tag {
  font-size: 11px;
  background: #fff8e1;
  color: #f5a623;
  padding: 1px 6px;
  border-radius: 3px;
  border: 1px solid #ffe082;
}

.verified-badge {
  font-size: 11px;
  background: #e8f5e9;
  color: #4caf50;
  padding: 1px 6px;
  border-radius: 3px;
  border: 1px solid #c8e6c9;
}

.realname-badge {
  font-size: 11px;
  background: #e3f2fd;
  color: #2196f3;
  padding: 1px 6px;
  border-radius: 3px;
  border: 1px solid #bbdefb;
}

.seller-rep-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.rep-label {
  font-size: 12px;
  color: #999;
}

.rep-bar-wrap {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
}

.rep-bar {
  flex: 1;
  height: 6px;
  background: #e0e0e0;
  border-radius: 3px;
  overflow: hidden;
}

.rep-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea, #764ba2);
  border-radius: 3px;
  transition: width 0.3s;
}

.rep-score {
  font-size: 13px;
  font-weight: 600;
  color: #667eea;
  min-width: 28px;
}

.seller-stats-row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #999;
}

.stat-divider {
  color: #ddd;
}

.seller-arrow {
  color: #ccc;
  transition: color 0.2s;
}

.seller-block:hover .seller-arrow {
  color: #667eea;
}

/* ========== 相似商品 ========== */
.similar-section {
  margin-top: 20px;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #f0f0f0;
}
.similar-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 700;
  color: #333;
  margin-bottom: 14px;
}
.similar-sub { font-size: 12px; color: #999; font-weight: 400; }
.similar-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.similar-card {
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.2s;
}
.similar-card:hover {
  border-color: #667eea;
  transform: translateY(-3px);
  box-shadow: 0 4px 16px rgba(102,126,234,0.15);
}
.similar-img-wrap { position: relative; height: 110px; background: #f5f0ff; }
.similar-img { width: 100%; height: 100%; object-fit: cover; }
.similar-price {
  position: absolute; bottom: 6px; right: 6px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff; padding: 2px 8px; border-radius: 10px;
  font-size: 11px; font-weight: 800;
}
.similar-info { padding: 10px; }
.similar-product-title {
  font-size: 12px; font-weight: 600; color: #333;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-bottom: 4px;
}
.similar-game { font-size: 11px; color: #999; }

/* ========== 商品描述 ========== */
.description-section {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}

.section-title {
  margin: 0 0 16px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.description-content {
  font-size: 14px;
  color: #666;
  line-height: 1.8;
  white-space: pre-wrap;
}

.no-desc {
  color: #999;
  font-style: italic;
}

/* ========== 评价列表 ========== */
.review-count {
  color: #667eea;
  font-size: 14px;
  font-weight: normal;
}

.review-list {
  min-height: 80px;
}

.no-reviews {
  text-align: center;
  color: #999;
  padding: 20px 0;
  font-size: 14px;
}

.review-item {
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.reviewer-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

.reviewer-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
}

.reviewer-name {
  font-size: 13px;
  font-weight: 500;
  color: #333;
}

.review-time {
  font-size: 12px;
  color: #999;
}

.review-body {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-left: 42px;
}

.review-reply {
  margin-top: 8px;
  margin-left: 42px;
  font-size: 13px;
  color: #667eea;
  background: rgba(102, 126, 234, 0.08);
  padding: 8px 12px;
  border-radius: 6px;
}

.reply-label {
  font-weight: 600;
}

@media (max-width: 900px) {
  .detail-main {
    flex-direction: column;
    padding: 16px;
  }

  .sidebar {
    width: 100%;
    position: static;
  }

  .detail-inner {
    flex-direction: column;
  }

  .detail-left {
    width: 100%;
  }

  .main-image {
    width: 100%;
    height: 280px;
  }
}

/* ========== 侧边推荐位 ========== */
.detail-sidebar {
  width: 260px;
  flex-shrink: 0;
  margin-left: 20px;
}

.sidebar-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 2px solid #667eea;
}

.sidebar-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  background: #fff;
  border-radius: 8px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: box-shadow 0.2s;
}
.sidebar-item:hover {
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

.sidebar-img {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 6px;
  flex-shrink: 0;
}
.sidebar-img-placeholder {
  width: 50px;
  height: 50px;
  border-radius: 6px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ccc;
  flex-shrink: 0;
}

.sidebar-info {
  flex: 1;
  min-width: 0;
}

.sidebar-product-title {
  font-size: 12px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 4px;
}

.sidebar-price {
  font-size: 13px;
  color: #667eea;
  font-weight: 600;
}
.sidebar-price span {
  font-size: 11px;
  color: #999;
  font-weight: 400;
}



.review-stats-mini {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 2px;
}

.review-stats-mini .stats-text {
  font-size: 11px;
  color: #999;
}

.review-summary {
  display: flex;
  gap: 24px;
  padding: 16px;
  background: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 16px;
  align-items: center;
}

.summary-left {
  text-align: center;
  min-width: 80px;
}

.avg-rating {
  font-size: 32px;
  font-weight: 700;
  color: #ff9800;
  line-height: 1;
}

.total-label {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.summary-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.star-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.star-label {
  font-size: 12px;
  color: #666;
  width: 28px;
}

.star-bar {
  flex: 1;
  height: 8px;
  background: #eee;
  border-radius: 4px;
  overflow: hidden;
}

.star-fill {
  height: 100%;
  background: #ff9800;
  border-radius: 4px;
  transition: width 0.3s;
}

.star-count {
  font-size: 12px;
  color: #999;
  width: 20px;
  text-align: right;
}

.buy-btn {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  font-weight: 600;
}

.buy-btn:hover {
  background: linear-gradient(135deg, #5a6fd6 0%, #6a4190 100%);
}

.fav-btn {
  border-color: #667eea;
  color: #667eea;
}

.fav-btn:hover {
  background: #f0f2ff;
}



/* ========== 商品编号 ========== */
.product-id-row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}
.id-label { font-weight: 600; color: #667eea; }
.id-value { font-family: monospace; color: #333; }
.id-tip { color: #bbb; }

/* ========== 卖家更多商品 ========== */
.seller-more-link {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-top: 12px;
  padding: 10px;
  background: linear-gradient(135deg, #f8f6ff, #f0f0ff);
  border: 1px solid #e8e0ff;
  border-radius: 10px;
  font-size: 13px;
  color: #667eea;
  cursor: pointer;
  transition: all 0.3s;
}
.seller-more-link:hover { background: #eee8ff; border-color: #667eea; }
.seller-more-link .el-icon { font-size: 14px; }

/* ========== 平台保障声明 ========== */
.platform-guarantee {
  margin-top: 20px;
  padding: 20px;
  background: linear-gradient(135deg, #f8f6ff, #fff5f5);
  border-radius: 12px;
  border: 1px solid #ede0ff;
}
.guarantee-title {
  font-size: 15px;
  font-weight: 700;
  color: #333;
  margin-bottom: 14px;
}
.guarantee-items {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}
.guarantee-item-large {
  display: flex;
  gap: 10px;
  padding: 12px;
  background: rgba(255,255,255,0.8);
  border-radius: 10px;
}
.gi-icon { font-size: 24px; flex-shrink: 0; line-height: 1; }
.gi-text { flex: 1; }
.gi-title { font-size: 13px; font-weight: 600; color: #333; margin-bottom: 3px; }
.gi-sub { font-size: 11px; color: #888; line-height: 1.4; margin: 0; }

/* ========== 购买须知 ========== */
.purchase-notice {
  margin-top: 20px;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #f0f0f0;
}
.notice-title {
  font-size: 15px;
  font-weight: 700;
  color: #333;
  margin-bottom: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.notice-descriptions :deep(.el-descriptions__label) {
  background: #f9f9ff;
  color: #667eea;
  font-weight: 600;
}
.notice-value {
  color: #555;
  font-size: 13px;
}

/* ========== 商品问答 ========== */
.qa-section {
  margin-top: 24px;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #f0f0f0;
}
.qa-title {
  font-size: 15px;
  font-weight: 700;
  color: #333;
  margin-bottom: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.qa-count {
  font-size: 13px;
  color: #667eea;
  font-weight: normal;
}
.qa-list { display: flex; flex-direction: column; gap: 12px; margin-bottom: 14px; }
.qa-item { background: #f9f9ff; border-radius: 10px; padding: 12px 14px; }
.qa-q {
  display: flex; align-items: flex-start; gap: 8px; margin-bottom: 6px;
}
.qa-icon { color: #667eea; flex-shrink: 0; margin-top: 2px; font-size: 14px; }
.qa-icon-a { color: #67c23a; }
.qa-q-text { font-size: 13px; color: #333; flex: 1; line-height: 1.5; }
.qa-time { font-size: 11px; color: #bbb; flex-shrink: 0; }
.qa-a {
  display: flex; align-items: flex-start; gap: 8px;
  background: rgba(103,194,58,0.08); border-radius: 8px; padding: 8px 12px;
  margin-left: 22px;
}
.qa-a-text { font-size: 13px; color: #555; flex: 1; line-height: 1.5; }
.qa-form { display: flex; flex-direction: column; gap: 10px; }
.qa-form-actions { display: flex; justify-content: flex-end; gap: 8px; }
.qa-ask-btn {
  display: flex; align-items: center; justify-content: center; gap: 6px;
  padding: 10px; border: 1px dashed #c0b8f0; border-radius: 10px;
  color: #667eea; font-size: 13px; cursor: pointer; transition: all 0.2s;
}
.qa-ask-btn:hover { background: #f0f0ff; border-color: #667eea; }

/* ========== 优惠券选择弹窗 ========== */
.coupon-select-list { display: flex; flex-direction: column; gap: 10px; max-height: 400px; overflow-y: auto; }
.coupon-select-item {
  display: flex; align-items: center; gap: 12px;
  padding: 14px 16px; border: 2px solid #f0f0f0; border-radius: 10px; cursor: pointer; transition: all 0.2s;
}
.coupon-select-item:hover { border-color: #667eea; background: #f8f6ff; }
.coupon-select-item.selected { border-color: #667eea; background: #f0f2ff; }
.csel-left { display: flex; flex-direction: column; align-items: center; min-width: 70px; }
.csel-value { font-size: 22px; font-weight: 700; color: #ff6b6b; }
.coupon-select-item.selected .csel-value { color: #667eea; }
.csel-name { font-size: 12px; color: #666; margin-top: 2px; }
.csel-right { flex: 1; }
.csel-rule { font-size: 13px; color: #333; margin-bottom: 4px; }
.csel-exp { font-size: 12px; color: #999; }
.csel-check { color: #667eea; font-size: 20px; flex-shrink: 0; }

/* ========== 按钮样式 ========== */
.contact-btn { border-radius: 8px !important; }
.contact-btn:hover { background: #eee8ff !important; }
.share-btn { border-radius: 8px !important; }
.share-btn:hover { background: #f0f9eb !important; color: #67c23a !important; border-color: #c8e6b0 !important; }

</style>
