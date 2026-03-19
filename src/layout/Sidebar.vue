<script setup lang="ts">
import {
  AlarmClock,
  Bell,
  DataAnalysis,
  Document,
  PieChart,
  Setting,
  Tickets,
} from '@element-plus/icons-vue'
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

import { useAuthStore } from '../stores/auth'
import { asyncRoutes } from '../router/routes'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const iconMap = {
  DataAnalysis,
  Setting,
  Tickets,
  AlarmClock,
  Bell,
  Document,
  PieChart,
}

interface MenuNode {
  path: string
  title: string
  icon?: string
  children?: MenuNode[]
}

const menuRoutes = computed<MenuNode[]>(() => filterRoutes(asyncRoutes))

function filterRoutes(routes: RouteRecordRaw[]): MenuNode[] {
  return routes
    .filter((item) => !item.meta?.hidden)
    .filter((item) => authStore.hasMenuPermission(item.meta?.permission as string | undefined))
    .map((item) => ({
      path: item.path,
      title: String(item.meta?.title || ''),
      icon: item.meta?.icon as string | undefined,
      children: item.children ? filterRoutes(item.children) : undefined,
    }))
}

const handleSelect = (index: string) => {
  router.push(index)
}
</script>

<template>
  <aside class="sidebar">
    <div class="sidebar-brand">
      <div class="sidebar-title">Work Order Service</div>
      <div class="sidebar-subtitle">工单处理系统</div>
    </div>

    <el-menu
      class="sidebar-menu"
      :default-active="route.path"
      @select="handleSelect"
    >
      <template v-for="menu in menuRoutes" :key="menu.path">
        <el-sub-menu v-if="menu.children?.length" :index="menu.path">
          <template #title>
            <el-icon>
              <component :is="iconMap[menu.icon as keyof typeof iconMap] || Document" />
            </el-icon>
            <span>{{ menu.title }}</span>
          </template>
          <el-menu-item
            v-for="child in menu.children"
            :key="child.path"
            :index="child.path"
          >
            {{ child.title }}
          </el-menu-item>
        </el-sub-menu>

        <el-menu-item v-else :index="menu.path">
          <el-icon>
            <component :is="iconMap[menu.icon as keyof typeof iconMap] || Document" />
          </el-icon>
          <span>{{ menu.title }}</span>
        </el-menu-item>
      </template>
    </el-menu>
  </aside>
</template>
