import { createRouter, createWebHashHistory } from 'vue-router'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      redirect: '/manage',
    },
    {
      path: '/manage',
      name: 'manage',
      component: () => import('../views/FormManagerView.vue'),
      meta: {
        title: '表单管理',
      },
    },
    {
      path: '/fill',
      name: 'fill',
      component: () => import('../views/FormFillView.vue'),
      meta: {
        title: '表单填写',
      },
    },
  ],
})

router.afterEach((to) => {
  document.title = `${to.meta.title || '表单设计平台'} - 表单设计平台`
})

export default router
