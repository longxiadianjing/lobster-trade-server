<template>
  <PageLayout>
    <template #sidebar>
      <el-card class="menu-card" shadow="never" :body-style="{ padding: '0' }">
        <template #header>
          <span class="menu-title">📋 订单中心</span>
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
    </template>

    <div class="order-content">
      <el-card class="order-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">📦 订单中心</span>
          </div>
        </template>

        <!-- 角色切换标签 -->
        <el-tabs v-model="activeTab" @tab-change="loadOrders" class="order-tabs">
          <el-tab-pane label="我买到的" name="buyer">
            <template #label>
              <span class="tab-label">
                <el-icon><ShoppingCart /></el-icon>
                我买到的
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane label="我卖出的" name="seller">
            <template #label>
              <span class="tab-label">
                <el-icon><Sell /></el-icon>
                我卖出的
              </span>
            </template>
          </el-tab-pane>
        </el-tabs>

        <!-- 订单列表 -->
        <div class="order-list" v-loading="loading">
          <template v-if="orders.length > 0">
            <div
              v-for="order in orders"
              :key="order.id"
              class="order-item"
              @click="router.push({ path: '/order/detail/' + order.id })"
            >
              <div class="order-header">
                <span class="order-no">订单号：{{ order.id }}</span>
                <span class="order-time">{{ formatTime(order.createTime) }}</span>
                <el-tag :type="getStatusType(order.status)" size="small">
                  {{ getStatusText(order.status) }}
                </el-tag>
              </div>

              <div class="order-body">
                <div class="order-product">
                  <div class="product-info">
                    <h4 class="product-title">{{ order.productTitle }}</h4>
                    <p class="product-meta">
                      {{ order.gameName }} · {{ order.gameZone || '全部区服' }}
                    </p>
                  </div>
                  <div class="price-info">
                    <span class="order-price">¥{{ order.amount }}</span>
                    <span class="order-count">x{{ order.quantity }}</span>
                  </div>
                </div>

                <div class="order-parties">
                  <span class="party-label">{{ activeTab === 'buyer' ? '卖家' : '买家' }}：</span>
                  <span class="party-name">{{ activeTab === 'buyer' ? order.sellerNickname : order.buyerNickname }}</span>
                </div>
              </div>

              <div class="order-footer">
                <el-button size="small" @click.stop="router.push({ path: '/order/detail/' + order.id })">
                  查看详情
                </el-button>
                <template v-if="order.status === 'pending_pay' && activeTab === 'buyer'">
                  <el-button type="primary" size="small" @click.stop="handlePay(order)">去付款</el-button>
                  <el-button size="small" type="danger" @click.stop="handleCancel(order)">取消订单</el-button>
                </template>
                <template v-if="order.status === 'paid' && activeTab === 'seller'">
                  <el-button type="success" size="small" @click.stop="handleDeliver(order)">发货</el-button>
                </template>
                <template v-if="(order.status === 'submitted' || order.status === 'paid') && activeTab === 'buyer'">
                  <el-button type="primary" size="small" @click.stop="handleConfirm(order)">确认收货</el-button>
                </template>
                <template v-if="order.status === 'submitted' && activeTab === 'seller'">
                  <el-button type="success" size="small" @click.stop="handleDeliver(order)">发货</el-button>
                </template>
              </div>
            </div>

            <!-- 分页 -->
            <div class="pagination-wrap">
              <el-pagination
                v-model:current-page="pagination.page"
                :page-size="pagination.pageSize"
                :total="pagination.total"
                layout="total, prev, pager, next"
                @current-change="loadOrders"
              />
            </div>
          </template>

          <template v-else-if="!loading">
            <el-empty :description="activeTab === 'buyer' ? '暂无购买订单' : '暂无卖出订单'" />
          </template>
        </div>
      </el-card>
    </div>
  </PageLayout>
</template>

<script setup>
  document.title = '订单中心 - 龙虾道具交易平台';

import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCart, Sell, User, Wallet, Goods, List } from '@element-plus/icons-vue'
import { getBuyerOrders, getSellerOrders, payOrder, confirmOrder, cancelOrder } from '@/api/order'
import PageLayout from '@/components/PageLayout.vue'

const router = useRouter()
const route = useRoute()
const activeTab = ref('buyer')
const loading = ref(false)
const orders = ref([])
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

onMounted(() => {
  loadOrders()
})

const loadOrders = async () => {
  loading.value = true
  try {
    const params = { page: pagination.page, pageSize: pagination.pageSize }
    let res
    if (activeTab.value === 'buyer') {
      res = await getBuyerOrders(params)
    } else {
      res = await getSellerOrders(params)
    }
    if (res.data) {
      orders.value = res.data.records || res.data.list || []
      pagination.total = res.data.total || 0
    }
  } catch (e) {
    console.error('加载订单失败', e)
  } finally {
    loading.value = false
  }
}

const getStatusType = (status) => {
  const map = {
    pending_pay: 'warning',
    paid: 'primary',
    submitted: 'info',
    confirmed: 'success',
    completed: 'success',
    cancelled: 'info',
    disputed: 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    pending_pay: '待付款',
    paid: '已付款',
    submitted: '待确认',
    confirmed: '已确认',
    completed: '已完成',
    cancelled: '已取消',
    disputed: '纠纷中'
  }
  return map[status] || status
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

const handlePay = (order) => {
  router.push({ path: '/order/detail/' + order.id })
}

const handleCancel = async (order) => {
  try {
    await cancelOrder(order.id)
    ElMessage.success('订单已取消')
    loadOrders()
  } catch (e) {
    ElMessage.error('取消失败')
  }
}

const handleDeliver = async (order) => {
  ElMessage.info('请联系客服发货')
}

const handleConfirm = async (order) => {
  try {
    await confirmOrder(order.id)
    ElMessage.success('已确认收货')
    loadOrders()
  } catch (e) {
    ElMessage.error('确认失败')
  }
}
</script>

<style scoped>
.order-content {
  display: flex;
  flex-direction: column;
}

.menu-card {
  border-radius: 12px;
  border: none;
}

.menu-title {
  font-weight: 600;
  font-size: 14px;
}

.order-card {
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

.order-tabs {
  margin-bottom: 20px;
}

:deep(.el-tabs__item) {
  font-size: 14px;
  font-weight: 600;
}

:deep(.el-tabs__item.is-active) {
  color: #667eea !important;
}

:deep(.el-tabs__active-bar) {
  background-color: #667eea !important;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-item {
  background: #fff;
  border-radius: 12px;
  padding: 18px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #f0f0f0;
}

.order-item:hover {
  border-color: #7B5FD4;
  box-shadow: 0 4px 16px rgba(91, 71, 194, 0.08);
}

.order-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f5f5f5;
}

.order-no {
  font-size: 13px;
  color: #999;
  font-family: monospace;
}

.order-time {
  font-size: 12px;
  color: #bbb;
}

.order-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 14px;
}

.order-product {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.product-info {
  flex: 1;
}

.product-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin: 0 0 4px 0;
}

.product-meta {
  font-size: 12px;
  color: #999;
  margin: 0;
}

.price-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
}

.order-price {
  font-size: 16px;
  font-weight: 700;
  color: #667eea;
}

.order-count {
  font-size: 12px;
  color: #999;
}

.order-parties {
  font-size: 13px;
  color: #666;
}

.party-label {
  color: #999;
}

.party-name {
  font-weight: 600;
  color: #333;
}

.order-footer {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: flex-end;
  padding-top: 12px;
  border-top: 1px solid #f5f5f5;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea, #7B5FD4) !important;
  border: none !important;
}

:deep(.el-button--primary:hover) {
  opacity: 0.9;
}

:deep(.el-button--success) {
  background: #67c23a !important;
  border: none !important;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #667eea, #7B5FD4) !important;
  color: #fff !important;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  padding: 16px 0;
}
</style>