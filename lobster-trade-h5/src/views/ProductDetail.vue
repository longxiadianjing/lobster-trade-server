<template>
  <div class="product-detail">
    <MobileHeader title="商品详情" :showBack="true" />

    <van-loading v-if="loading" type="spinner" class="loading-center" />
    <template v-else-if="product">
      <van-swipe class="images" :autoplay="3000" indicator-color="#ff6000">
        <van-swipe-item v-for="(img, i) in images" :key="i">
          <img :src="img" alt="商品图片" />
        </van-swipe-item>
      </van-swipe>

      <div class="info">
        <div class="price-row">
          <span class="price">¥{{ product.price }}</span>
          <span class="views">浏览 {{ product.viewCount || 0 }}</span>
        </div>
        <div class="title">{{ product.title }}</div>
        <div class="meta">
          <van-tag type="primary" size="small">{{ product.gameName }}</van-tag>
          <van-tag type="success" size="small">{{ product.categoryName }}</van-tag>
        </div>
      </div>

      <van-cell-group title="商品描述">
        <van-cell :label="product.description || '暂无描述'" />
      </van-cell-group>

      <van-cell-group title="卖家信息">
        <van-cell title="卖家" :value="product.sellerNickname || '未知'" />
      </van-cell-group>

      <div class="bottom-bar">
        <van-button type="primary" size="large" block @click="handleBuy">立即购买</van-button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductDetail } from '@/api/product'
import MobileHeader from '@/components/MobileHeader.vue'

document.title = '商品详情 - 龙虾道具交易平台'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const product = ref(null)

const images = computed(() => {
  if (!product.value) return []
  if (product.value.images) {
    try {
      return JSON.parse(product.value.images)
    } catch {
      return [product.value.images]
    }
  }
  if (product.value.coverImage) return [product.value.coverImage]
  return []
})

const handleBuy = () => {
  router.push(`/order/create/${product.value.id}`)
}

onMounted(async () => {
  try {
    const res = await getProductDetail(route.params.id)
    product.value = res.data || res
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.images img { width: 100%; height: 300px; display: block; }
.info { background: #fff; padding: 12px; margin-bottom: 8px; }
.price-row { display: flex; justify-content: space-between; align-items: center; }
.price { font-size: 22px; color: #ff6000; font-weight: bold; }
.views { font-size: 12px; color: #999; }
.title { font-size: 16px; color: #333; margin: 8px 0; }
.meta { display: flex; gap: 6px; }
.bottom-bar { position: fixed; bottom: 50px; left: 0; right: 0; padding: 10px; background: #fff; }
.loading-center { position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%); }
.product-detail { padding-bottom: 60px; }
</style>