<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 class="title">家庭记账本</h2>
      <el-form :model="loginForm" :rules="rules" ref="formRef">
        <el-form-item prop="account">
          <el-input
            v-model="loginForm.account"
            placeholder="手机号或邮箱"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" @click="handleLogin" :loading="loading" class="login-btn">
            登录
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-link type="primary" @click="goToRegister">没有账号？去注册</el-link>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/user'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const loginForm = reactive({
  account: '',
  password: ''
})

const rules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    const res = await login(loginForm)
    // 注意：我们的 request 拦截器已经返回 response.data，所以 res 就是 { code, message, data }
    if (res.code === 200) {
      localStorage.setItem('token', res.data.token)
      // 保存用户信息（可选）
      if (res.data.nickname) {
        localStorage.setItem('userInfo', JSON.stringify({
          nickname: res.data.nickname,
          id: res.data.id,
          familyId: res.data.familyId || null
        }))
      }
      ElMessage.success('登录成功')
      router.push('/')
    } else {
      ElMessage.error(res.message || '登录失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('网络错误或服务器异常')
  } finally {
    loading.value = false
  }
}

const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  width: 400px;
  padding: 30px;
  border-radius: 8px;
}
.title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}
.login-btn {
  width: 100%;
}
</style>