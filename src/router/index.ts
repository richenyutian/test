import { createRouter, createWebHashHistory } from 'vue-router'

import pinia from '../stores'
import { useAuthStore } from '../stores/auth'
import { constantRoutes } from './routes'

const router = createRouter({
  history: createWebHashHistory(),
  routes: constantRoutes,
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore(pinia)

  if (to.meta.public) {
    return true
  }

  if (!authStore.isLoggedIn) {
    return '/login'
  }

  if (!authStore.profile) {
    try {
      await authStore.fetchProfile()
    } catch {
      authStore.clearLogin()
      return '/login'
    }
  }

  if (typeof to.meta.permission === 'string' && !authStore.hasMenuPermission(to.meta.permission)) {
    return '/dashboard'
  }

  return true
})

router.afterEach((to) => {
  document.title = `${to.meta.title || '工单处理系统'} - 工单处理系统`
})

export default router
