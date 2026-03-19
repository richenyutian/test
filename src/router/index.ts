import { createRouter, createWebHashHistory } from 'vue-router'

import { useAuthStore } from '../stores/authStore'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: {
        title: '登录',
        public: true,
      },
    },
    {
      path: '/',
      component: () => import('../layouts/TicketAppLayout.vue'),
      children: [
        {
          path: '',
          redirect: '/dashboard',
        },
        {
          path: '/dashboard',
          name: 'dashboard',
          component: () => import('../views/DashboardView.vue'),
          meta: {
            title: '工作台',
            permission: 'dashboard:view',
          },
        },
        {
          path: '/tickets',
          name: 'tickets',
          component: () => import('../views/TicketListView.vue'),
          meta: {
            title: '工单管理',
            permission: 'ticket:list',
          },
        },
        {
          path: '/tickets/:ticketId',
          name: 'ticket-detail',
          component: () => import('../views/TicketDetailView.vue'),
          meta: {
            title: '工单详情',
            permission: 'ticket:detail',
          },
        },
        {
          path: '/dispatch',
          name: 'dispatch',
          component: () => import('../views/DispatchCenterView.vue'),
          meta: {
            title: '分派中心',
            permission: 'dispatch:center',
          },
        },
        {
          path: '/sla',
          name: 'sla',
          component: () => import('../views/SlaManageView.vue'),
          meta: {
            title: 'SLA 管理',
            permission: 'sla:manage',
          },
        },
        {
          path: '/notifications',
          name: 'notifications',
          component: () => import('../views/NotificationCenterView.vue'),
          meta: {
            title: '通知中心',
            permission: 'notification:center',
          },
        },
        {
          path: '/reports',
          name: 'reports',
          component: () => import('../views/ReportCenterView.vue'),
          meta: {
            title: '报表中心',
            permission: 'report:center',
          },
        },
        {
          path: '/system',
          name: 'system',
          component: () => import('../views/SystemConfigView.vue'),
          meta: {
            title: '系统配置',
            permission: 'system:config',
          },
        },
        {
          path: '/audits',
          name: 'audits',
          component: () => import('../views/AuditLogView.vue'),
          meta: {
            title: '审计日志',
            permission: 'audit:log',
          },
        },
      ],
    },
  ],
})

router.beforeEach((to) => {
  const authStore = useAuthStore()

  if (to.meta.public) {
    return true
  }

  if (!authStore.isLoggedIn) {
    return '/login'
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
