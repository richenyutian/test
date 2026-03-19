<script setup lang="ts">
import { Fold, SwitchButton } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const handleLogout = async () => {
  await authStore.logout()
  router.push('/login')
}
</script>

<template>
  <header class="header-bar">
    <div class="header-title-group">
      <el-icon><Fold /></el-icon>
      <div>
        <div class="header-title">工单处理系统</div>
        <div class="header-desc">准生产级前后端分离后台管理项目</div>
      </div>
    </div>

    <div class="header-actions">
      <el-tag type="primary">{{ authStore.profile?.displayName }}</el-tag>
      <el-tag effect="plain">{{ authStore.profile?.roles.join(', ') }}</el-tag>
      <el-button :icon="SwitchButton" @click="handleLogout">退出登录</el-button>
    </div>
  </header>
</template>
