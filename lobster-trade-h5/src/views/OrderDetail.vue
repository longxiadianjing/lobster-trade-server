<template>
  <div class="order-detail">
    <MobileHeader title="订单详情" :showBack="true" />

    <van-loading v-if="loading" type="spinner" class="loading-center" />
    <template v-else-if="order">
      <van-steps :active="statusStep" class="steps" direction="vertical">
        <van-step>提交订单</van-step>
        <van-step>等待付款</van-step>
        <van-step>等待发货</van-step>
        <van-step>已发货</van-step>
        <van-step>已完成</van-step>
      </van-steps>

      <van-cell-group title="订单信息">
        <van-cell title="订单号" :value="order.orderNo" />
        <van-cell title="商品" :label="order.productTitle" />
        <van-cell title="订单金额">
          <span style="color:#ff6000;font-weight:bold">¥{{ order.amount }}</span>
        </van-cell>
        <van-cell title="订单状态">
          <van-tag :type="statusType">{{ statusText }}</van-tag>
        </van-cell>
      </van-cell-group>

      <van-cell-group title="发货信息" v-if="order.deliveryRemark">
        <van-cell title="发货备注" :label="order.deliveryRemark" />
      </van-cell-group>

      <div class="bottom-actions" v-if="showActions">
        <van-button v-if="order.status === 'PAID'" type="warning" size="small" @click="handleCancel">取消订单</van-button>
        <van-button v-if="order.status === 'PAID'" type="primary" size="small" @click="handleDeliver">确认发货</van-button>
        <van-button v-if="order.status === 'DELIVERED'" type="primary" size="small" @click="handleConfirm">确认收货</van-button>
      </div>
    </template>

    <TabBar />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail, confirmOrder, cancelOrder } from '@/api/order'
import MobileHeader from '@/components/MobileHeader.vue'
import TabBar from '@/components/TabBar.vue'

document.title = '订单详情 - 龙虾道具交易平台'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const order = ref(null)

const statusMap = { PENDING: 0, PAID: 1, DELIVERED: 2, CONFIRMED: 3, CANCELLED: -1 }
const statusTextMap = { PENDING: '待付款', PAID: '已付款', DELIVERED: '已发货', CONFIRMED: '已完成', CANCELLED: '已取消' }
const statusTypeMap = { PENDING: 'default', PAID: 'primary', DELIVERED: 'warning', CONFIRMED: 'success', CANCELLED: 'danger' }

const statusStep = computed(() => statusMap[order.value?.status] ?? 0)
const statusText = computed(() => statusTextMap[order.value?.status] ?? '')
const statusType = computed(() => statusTypeMap[order.value?.status] ?? 'default')
const showActions = computed(() => ['PAID', 'DELIVERED'].includes(order.value?.status))

const handleConfirm = async () => {
  try { await confirmOrder(route.params.id); router.back() } catch (e) { console.error(e) }
}
const handleCancel = async () => {
  try { await cancelOrder(route.params.id, '用户取消'); router.back() } catch (e) { console.error(e) }
}
const handleDeliver = () => { router.push(`/order/deliver/${route.params.id}`) }

onMounted(async () => {
  try {
    const res = await getOrderDetail(route.params.id)
    order.value = res.data || res
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.steps { margin-bottom: 12px; }
.bottom-actions { display: flex; gap: 10px; padding: 12px; justify-content: flex-end; }
.loading-center { position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%); }
.order-detail { padding-bottom: 60px; }
</style>