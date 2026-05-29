<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside width="200px" class="aside">
      <div class="logo">家庭记账本</div>
      <el-menu
        :default-active="activeMenu"
        class="menu"
        router
        @select="handleMenuSelect"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>首页概览</span>
        </el-menu-item>
        <el-menu-item index="/bill">
          <el-icon><Document /></el-icon>
          <span>收支记录</span>
        </el-menu-item>
        <el-menu-item index="/stat">
          <el-icon><DataLine /></el-icon>
          <span>统计分析</span>
        </el-menu-item>
        <el-menu-item index="/category">
          <el-icon><Folder /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/profile">
          <el-icon><User /></el-icon>
          <span>个人信息</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <!-- 顶部栏 -->
      <el-header class="header">
        <div class="header-right">
          <span class="welcome">你好，{{ nickname }}</span>
          <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Odometer, Document, DataLine, Folder, User } from '@element-plus/icons-vue'
import { logout, getUserInfo } from '@/api/user'

const router = useRouter()
const route = useRoute()
const nickname = ref('用户')

// 当前激活菜单项
const activeMenu = computed(() => route.path)

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    const res = await getUserInfo()
    if (res.code === 200) {
      nickname.value = res.data.nickname || '用户'
      localStorage.setItem('userInfo', JSON.stringify(res.data))
    } else {
      // 如果获取失败，尝试从 localStorage 读取
      const cached = localStorage.getItem('userInfo')
      if (cached) {
        const user = JSON.parse(cached)
        nickname.value = user.nickname || '用户'
      }
      console.warn('获取用户信息失败', res.message)
    }
  } catch (error) {
    console.error('获取用户信息异常', error)
  }
}

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' })
    .then(async () => {
      try {
        const res = await logout()
        if (res.code === 200) {
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          ElMessage.success('已退出登录')
          router.push('/login')
        } else {
          ElMessage.error(res.message || '退出失败')
        }
      } catch (error) {
        console.error(error)
        ElMessage.error('退出请求失败')
      }
    })
    .catch(() => {})
}

const handleMenuSelect = (index) => {
  // 菜单选中时不做额外处理，router 已处理
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
}
.aside {
  background-color: #304156;
  color: #fff;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 20px;
  font-weight: bold;
  color: #fff;
  background-color: #2b3a4a;
  margin-bottom: 20px;
}
.menu {
  border-right: none;
  background-color: #304156;
}
.menu :deep(.el-menu-item) {
  color: #bfcbd9;
}
.menu :deep(.el-menu-item.is-active) {
  color: #409eff;
  background-color: #263445;
}
.header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 20px;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}
.welcome {
  font-size: 14px;
  color: #333;
}
.main {
  background-color: #f0f2f6;
  padding: 20px;
}
</style>