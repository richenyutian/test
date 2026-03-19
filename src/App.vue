<script setup lang="ts">
import { DocumentChecked, EditPen } from '@element-plus/icons-vue'
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => route.path)

const menus = [
  {
    index: '/manage',
    icon: EditPen,
    title: '表单管理',
    description: '设计模板、字段、校验与布局',
  },
  {
    index: '/fill',
    icon: DocumentChecked,
    title: '表单填写',
    description: '选择模板并完成填写提交',
  },
]
</script>

<template>
  <el-container class="app-layout">
    <el-aside width="260px" class="app-sidebar">
      <div class="brand-block">
        <div class="brand-title">Form Edit Studio</div>
        <div class="brand-subtitle">Vue3 + Element Plus 表单设计平台</div>
      </div>

      <el-menu
        class="side-menu"
        :default-active="activeMenu"
        @select="(index) => router.push(index)"
      >
        <el-menu-item
          v-for="menu in menus"
          :key="menu.index"
          :index="menu.index"
          class="menu-item"
        >
          <el-icon><component :is="menu.icon" /></el-icon>
          <div class="menu-content">
            <div class="menu-title">{{ menu.title }}</div>
            <div class="menu-desc">{{ menu.description }}</div>
          </div>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="app-header">
        <div>
          <h1 class="page-title">{{ route.meta.title }}</h1>
          <p class="page-subtitle">
            支持表单模板设计、字段配置、校验规则、布局预览与提交记录管理
          </p>
        </div>
      </el-header>

      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>
