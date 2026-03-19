<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter } from 'vue-router'

import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const form = reactive({
  username: 'admin',
})

const submit = async () => {
  await authStore.login(form.username)
  router.push('/dashboard')
}
</script>

<template>
  <div class="login-page">
    <el-card class="login-card">
      <div class="page-heading">
        <h1>工单处理系统</h1>
        <p>当前版本采用开发/测试模式模拟 SSO，仅需输入用户名即可登录。</p>
      </div>

      <el-form label-position="top">
        <el-form-item label="用户名">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名，如：admin"
            @keyup.enter="submit"
          />
        </el-form-item>

        <el-button type="primary" style="width: 100%" @click="submit">
          登录系统
        </el-button>
      </el-form>

      <el-alert
        title="默认管理员账号：admin"
        type="info"
        :closable="false"
        show-icon
      />
    </el-card>
  </div>
</template>
