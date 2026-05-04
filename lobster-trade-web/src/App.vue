<style>
@import '@/styles/mobile.css';
</style>

<template>
  <router-view />
  <CustomerService />
</template>

<script setup>
import CustomerService from '@/components/CustomerService.vue'
</script>
