<template>
  <el-container class="layout-container">

    <!-- 侧边栏 -->
    <el-aside width="200px">
      <div class="logo">家庭记账本</div>
      <el-menu
        :default-active="route.path"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataLine /></el-icon>
          <span>首页概览</span>
        </el-menu-item>
        <el-menu-item index="/bill">
          <el-icon><List /></el-icon>
          <span>收支记录</span>
        </el-menu-item>
        <el-menu-item index="/stat">
          <el-icon><PieChart /></el-icon>
          <span>统计分析</span>
        </el-menu-item>
        <el-menu-item index="/category">
          <el-icon><Collection /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/profile">
          <el-icon><User /></el-icon>
          <span>个人信息</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部导航 -->
      <el-header>
        <div class="header-right">
          <span>你好，{{ userStore.userInfo.nickname }}</span>
          <el-button type="danger" text @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main>
        <RouterView />
      </el-main>
    </el-container>

  </el-container>
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
.layout-container {
  height: 100vh;
}

.el-aside {
  background-color: #304156;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  border-bottom: 1px solid #3f5166;
}

.el-header {
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  border-bottom: 1px solid #e6e6e6;
  box-shadow: 0 1px 4px rgba(0,0,0,0.1);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>