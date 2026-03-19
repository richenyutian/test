<script setup lang="ts">
import { Lock, User } from '@element-plus/icons-vue'
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

import { mockUsers } from '../mock/ticketSystemMock'
import { useAuthStore } from '../stores/authStore'

const router = useRouter()
const authStore = useAuthStore()

const loginForm = reactive({
  username: 'service.desk',
  password: 'ChangeMe123!',
})

const handleLogin = () => {
  try {
    authStore.login(loginForm.username, loginForm.password)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '登录失败')
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <div>
        <div class="panel-title">工单处理系统</div>
        <div class="panel-desc">
          技术栈：Vue3 + TypeScript + Vite + Element Plus + Pinia + Vue Router
        </div>
      </div>

      <el-form label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="loginForm.username" :prefix-icon="User" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="loginForm.password"
            type="password"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>

        <el-button type="primary" style="width: 100%" @click="handleLogin">
          登录系统
        </el-button>
      </el-form>

      <el-divider>快捷体验账号</el-divider>

      <div class="template-list">
        <div
          v-for="user in mockUsers"
          :key="user.userId"
          class="template-card"
        >
          <div class="template-card__header">
            <div class="template-card__name">{{ user.displayName }}</div>
            <el-tag>{{ user.roles.join(', ') }}</el-tag>
          </div>
          <div class="muted-text" style="margin: 10px 0">
            {{ user.departmentName }} / {{ user.teamName }}
          </div>
          <div class="chip-row">
            <el-button type="primary" plain @click="authStore.quickLogin(user); router.push('/dashboard')">
              以该角色登录
            </el-button>
            <el-tag effect="plain">{{ user.username }}</el-tag>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
