<template>
  <van-tabbar v-model="active" route>
    <van-tabbar-item to="/" icon="home-o">首页</van-tabbar-item>
    <van-tabbar-item to="/orders" icon="orders-o">订单</van-tabbar-item>
    <van-tabbar-item to="/publish" icon="plus" dot>发布</van-tabbar-item>
    <van-tabbar-item to="/center" icon="user-o">我的</van-tabbar-item>
  </van-tabbar>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const active = ref(0)
</script>