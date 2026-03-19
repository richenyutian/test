<script setup lang="ts">
import {
  Bell,
  DataAnalysis,
  Document,
  Files,
  Histogram,
  Lock,
  Operation,
  Setting,
} from '@element-plus/icons-vue'
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { useAuthStore } from '../stores/authStore'
import { useTicketWorkbenchStore } from '../stores/ticketWorkbenchStore'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const workbenchStore = useTicketWorkbenchStore()

const menus = [
  { path: '/dashboard', title: '工作台', icon: DataAnalysis, permission: 'dashboard:view' },
  { path: '/tickets', title: '工单管理', icon: Document, permission: 'ticket:list' },
  { path: '/dispatch', title: '分派中心', icon: Operation, permission: 'dispatch:center' },
  { path: '/sla', title: 'SLA 管理', icon: Histogram, permission: 'sla:manage' },
  { path: '/notifications', title: '通知中心', icon: Bell, permission: 'notification:center' },
  { path: '/reports', title: '报表中心', icon: Files, permission: 'report:center' },
  { path: '/system', title: '系统配置', icon: Setting, permission: 'system:config' },
  { path: '/audits', title: '审计日志', icon: Lock, permission: 'audit:log' },
]

const visibleMenus = computed(() =>
  menus.filter((menu) => authStore.hasMenuPermission(menu.permission)),
)

const handleMenuSelect = (index: string) => {
  router.push(index)
}
</script>

<template>
  <el-container class="ticket-layout">
    <el-aside width="260px" class="ticket-sidebar">
      <div class="brand-block">
        <div class="brand-title">Support Ticket Hub</div>
        <div class="brand-subtitle">标准化工单受理、分派、处理、SLA 与审计平台</div>
      </div>

      <el-menu
        class="side-menu"
        :default-active="route.path"
        @select="handleMenuSelect"
      >
        <el-menu-item
          v-for="menu in visibleMenus"
          :key="menu.path"
          :index="menu.path"
        >
          <el-icon><component :is="menu.icon" /></el-icon>
          <span>{{ menu.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="ticket-header">
        <div>
          <h1 class="page-title">{{ route.meta.title }}</h1>
          <p class="page-subtitle">
            企业级工单处理系统原型，覆盖 RBAC、SLA、分派、报表与审计等关键能力
          </p>
        </div>

        <div class="chip-row">
          <el-badge :value="workbenchStore.unreadNotificationCount" :max="99">
            <el-button circle :icon="Bell" @click="router.push('/notifications')" />
          </el-badge>
          <el-tag effect="dark">{{ authStore.currentUser?.displayName }}</el-tag>
          <el-button @click="authStore.logout(); router.push('/login')">退出登录</el-button>
        </div>
      </el-header>

      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>
