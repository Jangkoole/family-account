import { createRouter, createWebHistory } from 'vue-router'
import BillList from '@/views/bill/BillList.vue'

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
        { path: 'dashboard', name: 'dashboard', component: () => import('../views/DashboardView.vue') },
        { path: 'profile', name: 'profile', component: () => import('../views/ProfileView.vue') },
        { path: 'bill', name: 'bill', component: BillList, meta: { title: '账单管理' } },
        { path: 'stat', name: 'stat', component: () => import('../views/StatView.vue') },
        { path: 'category', name: 'category', component: () => import('../views/CategoryView.vue') }
      ]
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.name !== 'login' && to.name !== 'register' && !token) {
    next({ name: 'login' })
  } else {
    next()
  }
})

export default router