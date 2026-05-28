import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/stat',
      name: 'stat',
      component: () => import('@/views/stat/StatView.vue'),
    },
    { path: '/', redirect: '/stat' },
  ],
})

export default router
