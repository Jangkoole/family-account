<template>
  <div class="login-page">
    <div class="paper-card">
      <div class="card-tape"></div>
      <h1 class="handwrite">📒 家庭记账</h1>
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="account">
          <el-input v-model="form.account" placeholder="手机号或邮箱" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleLogin">登 录</el-button>
        </el-form-item>
      </el-form>
      <p class="switch-link" @click="router.push('/register')">还没有账号？<b>创建一个</b></p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'; import { useRouter } from 'vue-router'; import { ElMessage } from 'element-plus'
import { login } from '../../api/user'; import { useUserStore } from '../../stores/user'
const router = useRouter(); const userStore = useUserStore(); const formRef = ref(); const loading = ref(false)
const form = ref({ account: '', password: '' })
const rules = { account: [{ required: true, message: '请输入账号', trigger: 'blur' }], password: [{ required: true, message: '请输入密码', trigger: 'blur' }] }
async function handleLogin() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return; loading.value = true
    try {
      const res = await login(form.value)
      if (res.code === 200) { userStore.setToken(res.data.token); userStore.setUserInfo({ nickname: res.data.nickname }); await userStore.syncFamilyInfo(); ElMessage.success('登录成功'); router.push('/') }
      else ElMessage.error(res.message)
    } finally { loading.value = false }
  })
}
</script>

<style scoped>
.login-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background-color: var(--paper); background-image: radial-gradient(circle, rgba(58, 52, 40, 0.12) 1px, transparent 1px); background-size: 20px 20px; background-position: 2px 2px; }
.paper-card { width: 380px; padding: 44px 36px; background: var(--paper-warm); border: 3px solid var(--paper-dark); border-radius: var(--radius-lg); box-shadow: var(--shadow-lg); position: relative; animation: paperIn 0.5s ease-out; }
.card-tape { position: absolute; top: -12px; left: 50%; transform: translateX(-50%) rotate(-2deg); width: 60px; height: 24px; background: rgba(192, 152, 64, 0.3); border-radius: 2px; box-shadow: var(--shadow-tape); }
.paper-card h1 { text-align: center; font-size: 28px; margin: 0 0 32px; }
.paper-card :deep(.el-form-item) { margin-bottom: 22px; }
.submit-btn { width: 100%; height: 48px; font-size: 16px; font-weight: 700; letter-spacing: 0.1em; }
.switch-link { text-align: center; margin-top: 20px; font-size: 13px; color: var(--ink-faded); cursor: pointer; }
.switch-link:hover { color: var(--ink); }
.switch-link b { color: var(--tag-blue); }
</style>
