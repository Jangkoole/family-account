<template>
  <el-container class="layout-container">

    <!-- 侧边栏 - 墨色中式 -->
    <el-aside width="220px" class="sidebar-zh">
      <!-- Logo 区 - 印章 + 名称 -->
      <div class="logo-area">
        <div class="logo-seal">
          <span>家<br>账</span>
        </div>
        <div class="logo-text">
          <span class="logo-title">家庭记账本</span>
          <span class="logo-subtitle">岁 月 有 痕</span>
        </div>
      </div>

      <!-- 导航菜单 -->
      <div class="nav-section">
        <div class="nav-label">导 航</div>
        <div
          v-for="item in menuItems"
          :key="item.path"
          :class="['nav-item', { active: route.path.startsWith(item.path) }]"
          @click="router.push(item.path)"
        >
          <el-icon class="nav-icon"><component :is="item.icon" /></el-icon>
          <span class="nav-text">{{ item.label }}</span>
          <span v-if="route.path.startsWith(item.path)" class="nav-indicator"></span>
        </div>
      </div>

      <!-- 底部装饰 -->
      <div class="sidebar-footer">
        <div class="footer-seal">記</div>
        <div class="footer-text">开源节流<br>持家有道</div>
      </div>
    </el-aside>

    <el-container class="main-area">
      <!-- 顶部栏 - 极简中式 -->
      <el-header class="header-zh">
        <div class="header-breadcrumb">
          <span class="breadcrumb-dot"></span>
          {{ currentPageTitle }}
        </div>
        <div class="header-right">
          <span class="greeting">
            <span class="greeting-icon">☰</span>
            {{ userStore.userInfo.nickname }}
          </span>
          <span class="header-divider">|</span>
          <button class="logout-btn" @click="handleLogout">
            退出登录
          </button>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <RouterView />
      </el-main>
    </el-container>

  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DataLine, List, PieChart, Collection, User } from '@element-plus/icons-vue'
import { logout } from '../api/user'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const menuItems = [
  { path: '/dashboard', label: '首页概览', icon: DataLine },
  { path: '/bill',      label: '收支记录', icon: List },
  { path: '/stat',      label: '统计分析', icon: PieChart },
  { path: '/category',  label: '分类管理', icon: Collection },
  { path: '/profile',   label: '个人信息', icon: User },
]

const titleMap = {
  '/dashboard': '首页概览',
  '/bill':      '收支记录',
  '/stat':      '统计分析',
  '/category':  '分类管理',
  '/profile':   '个人信息',
}

const currentPageTitle = computed(() => {
  return titleMap[route.path] || ''
})

async function handleLogout() {
  await ElMessageBox.confirm('确认退出登录？', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await logout()
  userStore.clear()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
/* ============================================
   侧边栏 - 新中式墨色风格
   ============================================ */
.layout-container {
  height: 100vh;
}

.sidebar-zh {
  background: linear-gradient(180deg, #1a1a1a 0%, #1f1b14 30%, #2c2416 100%);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

/* 背景装饰纹样 */
.sidebar-zh::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    repeating-linear-gradient(
      90deg,
      transparent,
      transparent 30px,
      rgba(201, 169, 110, 0.02) 30px,
      rgba(201, 169, 110, 0.02) 31px
    );
  pointer-events: none;
}

/* Logo 区 */
.logo-area {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 24px 20px 20px;
  border-bottom: 1px solid rgba(201, 169, 110, 0.15);
  position: relative;
  z-index: 1;
}

.logo-seal {
  width: 48px;
  height: 48px;
  border: 2px solid var(--cinnabar);
  border-radius: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--cinnabar);
  font-family: var(--font-display);
  font-weight: 900;
  font-size: 14px;
  line-height: 1.3;
  letter-spacing: 2px;
  transform: rotate(-5deg);
  flex-shrink: 0;
  background: rgba(196, 52, 46, 0.06);
}

.logo-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.logo-title {
  font-family: var(--font-display);
  font-size: 17px;
  font-weight: 700;
  color: #FBF7F0;
  letter-spacing: 3px;
}

.logo-subtitle {
  font-size: 11px;
  color: var(--gold-light);
  letter-spacing: 6px;
  opacity: 0.7;
}

/* 导航区 */
.nav-section {
  flex: 1;
  padding: 16px 0;
  position: relative;
  z-index: 1;
}

.nav-label {
  font-size: 11px;
  color: rgba(201, 169, 110, 0.4);
  letter-spacing: 6px;
  padding: 0 24px 12px;
  font-family: var(--font-display);
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 13px 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  color: rgba(251, 247, 240, 0.6);
}

.nav-item:hover {
  background: rgba(201, 169, 110, 0.06);
  color: rgba(251, 247, 240, 0.85);
}

.nav-item.active {
  color: var(--gold-light);
  background: linear-gradient(90deg, rgba(184, 134, 11, 0.12) 0%, rgba(184, 134, 11, 0.02) 100%);
}

.nav-indicator {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: var(--gold);
  border-radius: 2px 0 0 2px;
}

.nav-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.nav-text {
  font-size: 14px;
  letter-spacing: 2px;
  font-family: var(--font-body);
}

/* 侧边栏底部 */
.sidebar-footer {
  padding: 16px 20px 20px;
  border-top: 1px solid rgba(201, 169, 110, 0.1);
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
  z-index: 1;
}

.footer-seal {
  width: 36px;
  height: 36px;
  border: 1.5px solid rgba(201, 169, 110, 0.4);
  border-radius: 2px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(201, 169, 110, 0.5);
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 16px;
  transform: rotate(-6deg);
  flex-shrink: 0;
}

.footer-text {
  font-size: 11px;
  color: rgba(201, 169, 110, 0.35);
  line-height: 1.6;
  letter-spacing: 1px;
}

/* ============================================
   顶部栏 - 极简中式
   ============================================ */
.main-area {
  flex-direction: column;
}

.header-zh {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px !important;
  background: linear-gradient(180deg, #fffdf8 0%, #FBF7F0 100%);
  border-bottom: 1px solid var(--gold-pale);
  padding: 0 28px;
  box-shadow: 0 1px 4px rgba(44, 36, 22, 0.04);
}

.header-breadcrumb {
  display: flex;
  align-items: center;
  gap: 10px;
  font-family: var(--font-display);
  font-size: 15px;
  font-weight: 600;
  color: var(--ink-deep);
  letter-spacing: 2px;
}

.breadcrumb-dot {
  width: 6px;
  height: 6px;
  background: var(--cinnabar);
  border-radius: 50%;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: var(--text-secondary);
}

.greeting {
  display: flex;
  align-items: center;
  gap: 6px;
}

.greeting-icon {
  font-size: 16px;
  color: var(--gold);
}

.header-divider {
  color: var(--gold-pale);
}

.logout-btn {
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  font-family: var(--font-body);
  font-size: 13px;
  padding: 4px 8px;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
  letter-spacing: 1px;
}
.logout-btn:hover {
  color: var(--cinnabar);
  background: rgba(196, 52, 46, 0.05);
}

/* ============================================
   主内容区
   ============================================ */
.main-content {
  background-color: transparent;
  padding: 24px;
  overflow-y: auto;
}
</style>
