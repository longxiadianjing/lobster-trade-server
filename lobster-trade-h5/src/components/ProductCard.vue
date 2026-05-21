<template>
  <div class="product-card" @click="$router.push(`/product/${product.id}`)">
    <van-image fit="cover" :src="product.coverImage || product.images?.[0]" class="product-image" />
    <div class="product-info">
      <div class="product-title">{{ product.title }}</div>
      <div class="product-meta">
        <span class="game-name">{{ product.gameName }}</span>
        <span class="price">¥{{ product.price }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  product: { type: Object, required: true }
})
</script>

<style scoped>
.product-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
}
.product-image {
  width: 100%;
  height: 140px;
}
.product-info {
  padding: 10px;
}
.product-title {
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 6px;
}
.game-name {
  font-size: 12px;
  color: #999;
}
.price {
  font-size: 16px;
  color: #ff6000;
  font-weight: bold;
}
</style>