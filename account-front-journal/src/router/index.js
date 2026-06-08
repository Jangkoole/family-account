import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue')
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue')
    },
    {
      path: '/',
      name: 'layout',
      component: () => import('../views/LayoutView.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('../views/DashboardView.vue')
        },
        {
          path: 'profile',
          name: 'profile',
          component: () => import('../views/ProfileView.vue')
        },
        {
          path: 'bill',
          name: 'bill',
          component: () => import('../views/BillView.vue')
        },
        {
          path: 'stat',
          name: 'stat',
          component: () => import('../views/StatView.vue')
        },
        {
          path: 'category',
          name: 'category',
          component: () => import('../views/CategoryView.vue')
        }
      ]
    }
  ]
})

// 路由守卫：未登录自动跳转到登录页
router.beforeEach((to, from) => {
  const token = localStorage.getItem('token')
  if (to.name !== 'login' && to.name !== 'register' && !token) {
    return { name: 'login' }
  }
})

export default router