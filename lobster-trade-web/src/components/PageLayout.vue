<template>
  <div class="page-layout">
    <SiteHeader>
      <template #actions>
        <slot name="header-actions" />
      </template>
    </SiteHeader>

    <div class="page-body">
      <aside class="page-sidebar" v-if="$slots.sidebar">
        <slot name="sidebar" />
      </aside>
      <main class="page-main">
        <slot />
      </main>
    </div>
  </div>
</template>

<script setup>
import SiteHeader from './SiteHeader.vue'
</script>

<style scoped>
.page-layout {
  min-height: 100vh;
  background: var(--bg-dark);
}

.page-body {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px;
  display: flex;
  gap: 24px;
}

.page-sidebar {
  width: 280px;
  flex-shrink: 0;
  overflow-y: visible;
}

.page-main {
  flex: 1;
  min-width: 0;
}
</style>