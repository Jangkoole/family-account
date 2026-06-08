<template>
  <div class="reg-page">
    <div class="login-card">
      <h1 class="pub-title">创建账户</h1>
      <p class="pub-desc">加入家庭记账</p>
      <div class="ed-rule" style="margin: 28px 0"></div>
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="account">
          <el-input v-model="form.account" placeholder="手机号或邮箱" size="large" />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input v-model="form.nickname" placeholder="昵称" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码（不少于6位）" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleRegister">注 册</el-button>
        </el-form-item>
      </el-form>
      <p class="switch-link" @click="router.push('/login')">已有账号？<b>登录</b></p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'; import { useRouter } from 'vue-router'; import { ElMessage } from 'element-plus'
import { register } from '../api/user'
const router = useRouter(); const formRef = ref(); const loading = ref(false)
const form = ref({ account: '', password: '', nickname: '' })
const rules = {
  account: [{ required: true, message: '请输入手机号或邮箱', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
}
async function handleRegister() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return; loading.value = true
    try { const res = await register(form.value); if (res.code === 200) { ElMessage.success('注册成功，请登录'); router.push('/login') } else ElMessage.error(res.message) }
    finally { loading.value = false }
  })
}
</script>

<style scoped>
.reg-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: var(--paper); }
.login-card { width: 420px; padding: 56px 44px; border: 1px solid var(--rule); background: var(--paper); }
.pub-title { font-family: var(--font-display); font-size: 32px; font-weight: 800; text-align: center; margin: 0; }
.pub-desc { text-align: center; font-family: var(--font-display); font-style: italic; font-size: 14px; color: var(--ink-light); margin-top: 8px; }
.reg-page :deep(.el-form-item) { margin-bottom: 24px; }
.submit-btn { width: 100%; height: 48px; font-size: 13px; font-weight: 600; letter-spacing: 0.1em; text-transform: uppercase; }
.switch-link { text-align: center; margin-top: 24px; font-size: 13px; color: var(--ink-light); cursor: pointer; }
.switch-link:hover { color: var(--ink); }
.switch-link b { color: var(--accent); }
</style>
