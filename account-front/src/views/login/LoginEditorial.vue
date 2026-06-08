<template>
  <div class="login-page">
    <div class="login-card">
      <h1 class="pub-title">家庭记账</h1>
      <p class="pub-desc">个人财务管理月刊</p>
      <div class="ed-rule" style="margin: 28px 0"></div>
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
      <p class="switch-link" @click="router.push('/register')">没有账号？<b>注册</b></p>
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
.login-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: var(--paper); }
.login-card { width: 420px; padding: 56px 44px; border: 1px solid var(--rule); background: var(--paper); animation: editorialFade 0.6s ease-out; }
.pub-title { font-family: var(--font-display); font-size: 32px; font-weight: 800; text-align: center; margin: 0; letter-spacing: -0.01em; }
.pub-desc { text-align: center; font-family: var(--font-display); font-style: italic; font-size: 14px; color: var(--ink-light); margin-top: 8px; }
.login-card :deep(.el-form-item) { margin-bottom: 24px; }
.submit-btn { width: 100%; height: 48px; font-size: 13px; font-weight: 600; letter-spacing: 0.1em; text-transform: uppercase; }
.switch-link { text-align: center; margin-top: 24px; font-size: 13px; color: var(--ink-light); cursor: pointer; }
.switch-link:hover { color: var(--ink); }
.switch-link b { color: var(--accent); }
</style>
