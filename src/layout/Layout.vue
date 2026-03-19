<template>
  <div class="layout-root">
    <Sidebar />
    <div class="layout-content">
      <HeaderBar />
      <main class="layout-main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import Sidebar from './Sidebar.vue'
import HeaderBar from './HeaderBar.vue'
</script>
