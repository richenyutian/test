import type { RouteRecordRaw } from 'vue-router'

export const asyncRoutes: RouteRecordRaw[] = [
  {
    path: '/dashboard',
    name: 'dashboard',
    component: () => import('../views/dashboard/DashboardView.vue'),
    meta: {
      title: '工作台',
      icon: 'DataAnalysis',
      permission: 'dashboard:view',
    },
  },
  {
    path: '/tickets',
    name: 'ticket-root',
    redirect: '/tickets/list',
    meta: {
      title: '工单管理',
      icon: 'Tickets',
      permission: 'ticket:view',
    },
    children: [
      {
        path: '/tickets/list',
        name: 'ticket-list',
        component: () => import('../views/ticket/TicketListView.vue'),
        meta: {
          title: '工单列表',
          permission: 'ticket:view',
        },
      },
      {
        path: '/tickets/create',
        name: 'ticket-create',
        component: () => import('../views/ticket/TicketCreateView.vue'),
        meta: {
          title: '创建工单',
          permission: 'ticket:view',
          hidden: true,
        },
      },
      {
        path: '/tickets/:ticketId',
        name: 'ticket-detail',
        component: () => import('../views/ticket/TicketDetailView.vue'),
        meta: {
          title: '工单详情',
          permission: 'ticket:view',
          hidden: true,
        },
      },
    ],
  },
  {
    path: '/system',
    name: 'system-root',
    redirect: '/system/users',
    meta: {
      title: '系统管理',
      icon: 'Setting',
      permission: 'system:view',
    },
    children: [
      {
        path: '/system/users',
        name: 'system-users',
        component: () => import('../views/system/user/UserListView.vue'),
        meta: {
          title: '用户管理',
          permission: 'sys:user:view',
        },
      },
      {
        path: '/system/roles',
        name: 'system-roles',
        component: () => import('../views/system/role/RoleListView.vue'),
        meta: {
          title: '角色管理',
          permission: 'sys:role:view',
        },
      },
      {
        path: '/system/menus',
        name: 'system-menus',
        component: () => import('../views/system/menu/MenuListView.vue'),
        meta: {
          title: '菜单管理',
          permission: 'sys:menu:view',
        },
      },
      {
        path: '/system/groups',
        name: 'system-groups',
        component: () => import('../views/system/group/HandleGroupListView.vue'),
        meta: {
          title: '处理组管理',
          permission: 'sys:group:view',
        },
      },
    ],
  },
  {
    path: '/sla',
    name: 'sla',
    component: () => import('../views/sla/SlaRuleView.vue'),
    meta: {
      title: 'SLA 管理',
      icon: 'AlarmClock',
      permission: 'ticket:sla:view',
    },
  },
  {
    path: '/notices',
    name: 'notice',
    component: () => import('../views/notice/NoticeListView.vue'),
    meta: {
      title: '站内消息',
      icon: 'Bell',
      permission: 'notice:view',
    },
  },
  {
    path: '/audit',
    name: 'audit',
    component: () => import('../views/audit/AuditLogView.vue'),
    meta: {
      title: '审计日志',
      icon: 'Document',
      permission: 'audit:view',
    },
  },
  {
    path: '/reports',
    name: 'reports',
    component: () => import('../views/report/ReportView.vue'),
    meta: {
      title: '报表中心',
      icon: 'PieChart',
      permission: 'report:view',
    },
  },
]

export const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/login/LoginView.vue'),
    meta: {
      title: '登录',
      public: true,
      hidden: true,
    },
  },
  {
    path: '/',
    component: () => import('../layout/Layout.vue'),
    redirect: '/dashboard',
    children: asyncRoutes,
  },
]
