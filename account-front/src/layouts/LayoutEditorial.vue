<template>
  <div class="ed-shell">

    <!-- 页眉 - 极简 -->
    <header class="ed-masthead">
      <div class="masthead-inner">
        <div class="masthead-left" @click="router.push('/dashboard')">
          <span class="pub-name">家庭记账</span>
          <span class="pub-rule">—</span>
          <span class="pub-tag">个人财务月刊</span>
        </div>
        <nav class="masthead-nav">
          <a
            v-for="item in menuItems"
            :key="item.path"
            :class="['nav-link', { active: route.path.startsWith(item.path) }]"
            @click="router.push(item.path)"
          >
            {{ item.label }}
          </a>
        </nav>
        <div class="masthead-right">
          <span class="byline">{{ userStore.userInfo.nickname }}</span>
          <button class="logout-link" @click="handleLogout">退出</button>
        </div>
      </div>
      <!-- 页眉线 -->
      <div class="masthead-rule"></div>
    </header>

    <!-- 内容 -->
    <main class="ed-content">
      <RouterView />
    </main>

    <!-- 页脚 -->
    <footer class="ed-footer">
      <div class="footer-rule"></div>
      <span>Family Account Book &copy; 2026</span>
    </footer>

  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { logout } from '../api/user'
import { useUserStore } from '../stores/user'

const route = useRoute(); const router = useRouter()
const userStore = useUserStore()

const menuItems = [
  { path: '/dashboard', label: '概览' },
  { path: '/bill',      label: '收支' },
  { path: '/stat',      label: '统计' },
  { path: '/category',  label: '分类' },
  { path: '/profile',   label: '账户' },
]

async function handleLogout() {
  await ElMessageBox.confirm('确认退出登录？', '提示', {
    confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning'
  })
  await logout(); userStore.clear(); ElMessage.success('已退出登录'); router.push('/login')
}
</script>

<style scoped>
/* ============================================
   Editorial Shell
   ============================================ */
.ed-shell {
  min-height: 100vh;
  display: flex; flex-direction: column;
  background: var(--paper);
}

/* ---- 页眉 ---- */
.ed-masthead {
  padding: 32px 48px 0;
  position: sticky; top: 0; z-index: 100;
  background: var(--paper);
}
.masthead-inner {
  display: flex; align-items: baseline; justify-content: space-between;
  padding-bottom: 16px;
}

.masthead-left { display: flex; align-items: baseline; gap: 10px; cursor: pointer; }
.pub-name {
  font-family: var(--font-display); font-size: 24px; font-weight: 700;
  color: var(--ink); letter-spacing: -0.01em;
}
.pub-rule { color: var(--ink-faint); font-family: var(--font-display); }
.pub-tag {
  font-family: var(--font-display); font-style: italic; font-size: 13px;
  color: var(--ink-light);
}

/* 导航 */
.masthead-nav { display: flex; gap: 28px; }
.nav-link {
  font-family: var(--font-display); font-size: 15px; color: var(--ink-light);
  cursor: pointer; transition: color var(--transition-smooth);
  letter-spacing: 0.02em;
}
.nav-link:hover { color: var(--ink); }
.nav-link.active { color: var(--ink); font-weight: 600; }

/* 右侧 */
.masthead-right { display: flex; align-items: baseline; gap: 16px; }
.byline { font-size: 13px; color: var(--ink-light); font-style: italic; }
.logout-link {
  background: none; border: none; font-family: var(--font-body);
  font-size: 11px; color: var(--ink-faint); cursor: pointer;
  text-transform: uppercase; letter-spacing: 0.08em;
  transition: color var(--transition-smooth);
}
.logout-link:hover { color: var(--accent); }

/* 页眉线 */
.masthead-rule {
  height: 2px; background: var(--ink);
  margin: 0;
}

/* ---- 内容 ---- */
.ed-content {
  flex: 1;
  padding: 48px;
  max-width: 1100px;
  width: 100%;
  margin: 0 auto;
}

/* ---- 页脚 ---- */
.ed-footer {
  text-align: center; padding: 40px 48px;
  font-size: 11px; color: var(--ink-faint);
  text-transform: uppercase; letter-spacing: 0.1em;
}
.footer-rule {
  height: 1px; background: var(--rule); margin-bottom: 20px;
}

@media (max-width: 768px) {
  .ed-masthead { padding: 20px 16px 0; }
  .masthead-nav { gap: 16px; }
  .nav-link { font-size: 13px; }
  .ed-content { padding: 24px 16px; }
}
</style>
