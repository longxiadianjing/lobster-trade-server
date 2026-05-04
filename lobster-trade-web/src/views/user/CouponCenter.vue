<template>
  <PageLayout>
    <template #sidebar>
      <el-card class="menu-card" shadow="never" :body-style="{ padding: '0' }">
        <template #header>
          <span class="menu-title">💰 优惠券</span>
        </template>
        <el-menu :default-active="route.path" router>
          <el-menu-item index="/user">
            <el-icon><User /></el-icon>
            <span>个人信息</span>
          </el-menu-item>
          <el-menu-item index="/wallet">
            <el-icon><Wallet /></el-icon>
            <span>我的钱包</span>
          </el-menu-item>
          <el-menu-item index="/order/center">
            <el-icon><List /></el-icon>
            <span>订单中心</span>
          </el-menu-item>
          <el-menu-item index="/product/list">
            <el-icon><Goods /></el-icon>
            <span>商品列表</span>
          </el-menu-item>
        </el-menu>
      </el-card>
    </template>

    <div class="coupon-content">
      <!-- 领券专区 -->
      <el-card class="section-card" shadow="never">
        <template #header>
          <div class="card-header-flex">
            <span class="card-title">🎁 领券中心</span>
            <el-button text type="primary" @click="loadAvailable">刷新</el-button>
          </div>
        </template>
        <div v-if="availableCoupons.length === 0" class="empty-tip">暂无可领取的优惠券</div>
        <div v-else class="available-grid">
          <div v-for="c in availableCoupons" :key="c.id" class="coupon-card" :class="couponClass(c.type)">
            <div class="coupon-value">
              <span v-if="c.type === 1" class="amount">¥{{ c.discountValue }}</span>
              <span v-else-if="c.type === 2" class="amount">{{ (c.discountRate * 10).toFixed(1) }}折</span>
              <span v-else class="amount">赠{{ c.giftAmount }}元</span>
              <span class="coupon-name">{{ c.name }}</span>
            </div>
            <div class="coupon-info">
              <div class="coupon-desc">{{ c.description }}</div>
              <div class="coupon-rule">
                <span v-if="c.type === 1">满{{ c.minAmount }}元可用</span>
                <span v-else-if="c.type === 2">满{{ c.minAmount }}元可享{{ (c.discountRate * 10).toFixed(1) }}折</span>
                <span v-else>充值满{{ c.rechargeAmount }}元赠送{{ c.giftAmount }}元</span>
              </div>
              <div class="coupon-exp">有效期至：{{ formatDate(c.endTime) }}</div>
            </div>
            <el-button size="small" type="primary" class="receive-btn" @click="receiveCoupon(c.id)">立即领取</el-button>
          </div>
        </div>
      </el-card>

      <!-- 我的优惠券 -->
      <el-card class="section-card" shadow="never">
        <template #header>
          <div class="card-header-flex">
            <span class="card-title">📋 我的优惠券</span>
            <el-radio-group v-model="tabStatus" size="small" @change="loadMyCoupons">
              <el-radio-button :value="null">全部</el-radio-button>
              <el-radio-button :value="0">未使用</el-radio-button>
              <el-radio-button :value="1">已使用</el-radio-button>
              <el-radio-button :value="2">已过期</el-radio-button>
            </el-radio-group>
          </div>
        </template>

        <div v-if="myCoupons.length === 0" class="empty-tip">暂无优惠券</div>
        <div v-else class="my-coupon-list">
          <div
            v-for="uc in myCoupons"
            :key="uc.id"
            class="my-coupon-item"
            :class="{ used: uc.status === 1, expired: uc.status === 2 }"
          >
            <div class="mcoupon-left">
              <div class="mcoupon-value">
                <span v-if="uc.type === 1 || uc.discountValue">¥{{ uc.discountValue }}</span>
                <span v-else-if="uc.discountRate">{{ (uc.discountRate * 10).toFixed(1) }}折</span>
              </div>
              <div class="mcoupon-name">{{ uc.couponName }}</div>
            </div>
            <div class="mcoupon-right">
              <div class="mcoupon-rule" v-if="uc.minAmount">满{{ uc.minAmount }}元可用</div>
              <div class="mcoupon-time">
                <span v-if="uc.status === 0">有效期至 {{ formatDate(uc.endTime) }}</span>
                <span v-else-if="uc.status === 1">已使用 {{ formatDate(uc.useTime) }}</span>
                <span v-else>已过期</span>
              </div>
            </div>
            <div class="mcoupon-status">
              <el-tag v-if="uc.status === 0" type="success" size="small">未使用</el-tag>
              <el-tag v-else-if="uc.status === 1" type="info" size="small">已使用</el-tag>
              <el-tag v-else type="danger" size="small">已过期</el-tag>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </PageLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import PageLayout from '@/components/PageLayout.vue'
import { User, Wallet, List, Goods } from '@element-plus/icons-vue'

const route = useRoute()
const availableCoupons = ref([])
const myCoupons = ref([])
const tabStatus = ref(null)

const formatDate = (d) => d ? d.replace('T', ' ').substring(0, 10) : '-'
const couponClass = (type) => ({ 1: 'type-full', 2: 'type-discount', 3: 'type-gift' }[type] || 'type-full')

const loadAvailable = async () => {
  try {
    const res = await request.get('/coupon/available')
    if (res.code === 200) { availableCoupons.value = res.data || [] }
  } catch (e) { console.error(e) }
}

const loadMyCoupons = async () => {
  try {
    const res = await request.get('/coupon/my', { params: { status: tabStatus.value } })
    if (res.code === 200) { myCoupons.value = res.data || [] }
  } catch (e) { console.error(e) }
}

const receiveCoupon = async (couponId) => {
  try {
    const res = await request.post('/coupon/receive', { couponId })
    if (res.code === 200) {
      ElMessage.success('领取成功')
      loadAvailable()
      loadMyCoupons()
    } else { ElMessage.error(res.message || '领取失败') }
  } catch (e) { ElMessage.error('领取失败') }
}

onMounted(() => {
  loadAvailable()
  loadMyCoupons()

  document.title = '优惠券中心 - 龙虾道具交易平台'
})
</script>

<style scoped>
.coupon-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.menu-card {
  border-radius: 12px;
  border: none;
}

.menu-title {
  font-weight: 600;
  font-size: 14px;
}

.section-card {
  border-radius: 12px;
  border: none;
}

.card-header-flex {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
}

.empty-tip {
  text-align: center;
  color: #999;
  padding: 30px;
  font-size: 14px;
}

.available-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}

.coupon-card {
  border-radius: 10px;
  padding: 16px;
  display: flex;
  gap: 12px;
  align-items: center;
  border: 1px solid #f0f0f0;
  position: relative;
  overflow: hidden;
  transition: all 0.2s;
}

.coupon-card:hover {
  border-color: #7B5FD4;
  box-shadow: 0 4px 16px rgba(91, 71, 194, 0.1);
}

.coupon-card.type-full {
  background: linear-gradient(135deg, #fff5f5, #fff);
  border-left: 4px solid #ff6b6b;
}

.coupon-card.type-discount {
  background: linear-gradient(135deg, #f0f5ff, #fff);
  border-left: 4px solid #667eea;
}

.coupon-card.type-gift {
  background: linear-gradient(135deg, #f0fff0, #fff);
  border-left: 4px solid #51cf66;
}

.coupon-value {
  text-align: center;
  min-width: 70px;
}

.coupon-value .amount {
  font-size: 24px;
  font-weight: 700;
  color: #ff6b6b;
  display: block;
}

.type-discount .amount { color: #667eea; }
.type-gift .amount { color: #51cf66; }

.coupon-value .coupon-name {
  font-size: 12px;
  color: #666;
}

.coupon-info { flex: 1; }
.coupon-desc { font-size: 13px; color: #333; font-weight: 600; margin-bottom: 4px; }
.coupon-rule { font-size: 12px; color: #999; margin-bottom: 2px; }
.coupon-exp { font-size: 11px; color: #bbb; }
.receive-btn { flex-shrink: 0; }

.receive-btn {
  background: linear-gradient(135deg, #667eea, #7B5FD4) !important;
  border: none !important;
  font-weight: 600 !important;
}

.my-coupon-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.my-coupon-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-radius: 10px;
  background: #fff;
  border: 1px solid #f0f0f0;
  transition: all 0.2s;
}

.my-coupon-item:hover {
  border-color: #7B5FD4;
}

.my-coupon-item.used { opacity: 0.6; background: #fafafa; }
.my-coupon-item.expired { opacity: 0.5; background: #f5f5f5; }

.mcoupon-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.mcoupon-value {
  font-size: 22px;
  font-weight: 700;
  color: #ff6b6b;
  min-width: 60px;
}

.mcoupon-name {
  font-size: 13px;
  color: #333;
  font-weight: 600;
}

.mcoupon-right { flex: 1; }
.mcoupon-rule { font-size: 12px; color: #666; margin-bottom: 4px; }
.mcoupon-time { font-size: 12px; color: #999; }

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #667eea, #7B5FD4) !important;
  color: #fff !important;
}

:deep(.el-radio-button__inner) {
  border-radius: 8px;
}

:deep(.el-radio-button.is-active .el-radio-button__inner) {
  background: linear-gradient(135deg, #667eea, #7B5FD4) !important;
  border-color: #667eea !important;
}
</style>