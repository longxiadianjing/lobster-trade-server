<template>
  <div class="home-page">
    <van-nav-bar title="龙虾道具交易平台">
      <template #right>
        <van-icon name="search" size="18" @click="$router.push('/search')" />
      </template>
    </van-nav-bar>

    <van-swipe class="banner" :autoplay="3000" indicator-color="#ff6000">
      <van-swipe-item v-for="(img, i) in banners" :key="i">
        <img :src="img" alt="banner" />
      </van-swipe-item>
    </van-swipe>

    <van-tabs v-model:active="activeTab" shrink sticky offset-top="46">
      <van-tab v-for="game in games" :key="game.id" :title="game.name" :name="game.id">
        <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
          <van-list
            v-model:loading="loading"
            :finished="finished"
            finished-text="没有更多了"
            @load="loadMore"
          >
            <van-grid :column="2" :gutter="8">
              <van-grid-item v-for="p in products" :key="p.id">
                <ProductCard :product="p" />
              </van-grid-item>
            </van-grid>
          </van-list>
        </van-pull-refresh>
      </van-tab>
    </van-tabs>

    <TabBar />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { searchProduct } from '@/api/product'
import { getGameTree } from '@/api/game'
import ProductCard from '@/components/ProductCard.vue'
import TabBar from '@/components/TabBar.vue'

document.title = '首页 - 龙虾道具交易平台'

const banners = ref([
  'https://picsum.photos/375/150?random=1'
])

const games = ref([])
const activeTab = ref(0)
const products = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const page = ref(1)

const loadGames = async () => {
  try {
    const res = await getGameTree()
    games.value = res.data || []
    if (games.value.length > 0) {
      activeTab.value = games.value[0].id
    }
  } catch (e) {
    console.error(e)
  }
}

const loadProducts = async (reset = false) => {
  if (reset) {
    page.value = 1
    products.value = []
    finished.value = false
  }
  loading.value = true
  try {
    const res = await searchProduct({ gameId: activeTab.value, page: page.value, size: 10 })
    const records = res.data?.records || res.records || []
    products.value.push(...records)
    const total = res.data?.total || res.total || 0
    if (products.value.length >= total) {
      finished.value = true
    }
    page.value++
  } catch (e) {
    console.error(e)
    finished.value = true
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const onRefresh = () => loadProducts(true)
const loadMore = () => loadProducts()

onMounted(async () => {
  await loadGames()
  if (games.value.length > 0) {
    activeTab.value = games.value[0].id
    await loadProducts(true)
  }
})
</script>

<style scoped>
.banner img { width: 100%; height: 150px; display: block; }
</style>