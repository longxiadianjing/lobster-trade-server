<template>
  <van-nav-bar
    :title="title"
    left-arrow
    @click-left="onClickLeft"
    @click-right="onClickRight"
  >
    <template #left>
      <span class="nav-logo">🦞</span>
    </template>
    <template #right v-if="showSearch">
      <van-icon name="search" size="18" />
    </template>
  </van-nav-bar>
</template>

<script setup>
import { useRouter } from 'vue-router'

defineProps({
  title: { type: String, default: '' },
  showSearch: { type: Boolean, default: false },
  showBack: { type: Boolean, default: true }
})

const router = useRouter()
const onClickLeft = () => router.back()
const onClickRight = () => router.push('/search')
</script>

<style scoped>
.nav-logo { font-size: 20px; }
</style>