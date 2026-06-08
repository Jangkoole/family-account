<template>
  <div class="journal">

    <!-- 顶部 - 本子封面线 -->
    <header class="journal-top">
      <div class="top-inner">
        <div class="top-brand" @click="router.push('/dashboard')">
          <span class="brand-emoji">📒</span>
          <span class="brand-name">家庭记账</span>
        </div>
        <nav class="top-nav">
          <button
            v-for="item in menuItems"
            :key="item.path"
            :class="['nav-pill', { active: route.path.startsWith(item.path) }]"
            @click="router.push(item.path)"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            {{ item.label }}
          </button>
        </nav>
        <div class="top-user">
          <span class="user-greeting">{{ userStore.userInfo.nickname }}</span>
          <button class="logout-link" @click="handleLogout">退出</button>
        </div>
      </div>
    </header>

    <!-- 主内容 -->
    <main class="journal-page">
      <RouterView />
    </main>

  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DataLine, List, PieChart, Collection, User } from '@element-plus/icons-vue'
import { logout } from '../api/user'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const menuItems = [
  { path: '/dashboard', label: '概览', icon: DataLine },
  { path: '/bill',      label: '收支', icon: List },
  { path: '/stat',      label: '统计', icon: PieChart },
  { path: '/category',  label: '分类', icon: Collection },
  { path: '/profile',   label: '我', icon: User },
]

async function handleLogout() {
  await ElMessageBox.confirm('确认退出登录？', '提示', {
    confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning'
  })
  await logout(); userStore.clear()
  ElMessage.success('已退出登录'); router.push('/login')
}
</script>

<style scoped>
/* ============================================
   Journal Shell
   ============================================ */
.journal {
  min-height: 100vh;
  display: flex; flex-direction: column;
}

/* ---- 顶部 ---- */
.journal-top {
  background: var(--paper-warm);
  border-bottom: 3px solid var(--paper-dark);
  position: sticky; top: 0; z-index: 100;
  padding: 0 32px;
}
.top-inner {
  max-width: 1200px; margin: 0 auto;
  display: flex; align-items: center; height: 60px; gap: 32px;
}

.top-brand {
  display: flex; align-items: center; gap: 8px; cursor: pointer;
  flex-shrink: 0;
}
.brand-emoji { font-size: 24px; }
.brand-name {
  font-family: var(--font-display); font-size: 22px; font-weight: 700;
  color: var(--ink); letter-spacing: 0.02em;
}

/* 导航丸 */
.top-nav { display: flex; gap: 6px; flex: 1; }

.nav-pill {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 18px; border-radius: 20px;
  border: 2px solid transparent;
  background: transparent;
  font-family: var(--font-body); font-size: 14px; font-weight: 600;
  color: var(--ink-light); cursor: pointer;
  transition: all var(--transition-smooth);
}
.nav-pill:hover {
  background: var(--paper-cream); color: var(--ink);
}
.nav-pill.active {
  background: var(--ink); color: var(--paper-warm);
  border-color: var(--ink);
}

/* 用户区 */
.top-user { display: flex; align-items: center; gap: 12px; flex-shrink: 0; }
.user-greeting { font-size: 13px; color: var(--ink-faded); font-weight: 500; }
.logout-link {
  background: none; border: none; font-family: var(--font-body);
  font-size: 12px; color: var(--ink-faded); cursor: pointer;
  text-decoration: underline; text-decoration-style: wavy;
  text-underline-offset: 4px; transition: color var(--transition-smooth);
}
.logout-link:hover { color: var(--expense); }

/* ---- 页面 ---- */
.journal-page {
  flex: 1;
  padding: 32px;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
}

@media (max-width: 768px) {
  .journal-top { padding: 0 16px; }
  .top-nav { gap: 2px; }
  .nav-pill { padding: 6px 12px; font-size: 13px; }
  .journal-page { padding: 16px; }
}
</style>
