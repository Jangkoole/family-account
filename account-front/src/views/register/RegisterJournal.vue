<template>
  <div class="reg-page">
    <div class="paper-card">
      <div class="card-tape"></div>
      <h1 class="handwrite">📝 创建账户</h1>
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
import { register } from '../../api/user'
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
.reg-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background-color: var(--paper); background-image: radial-gradient(circle, rgba(58, 52, 40, 0.12) 1px, transparent 1px); background-size: 20px 20px; background-position: 2px 2px; }
.paper-card { width: 380px; padding: 44px 36px; background: var(--paper-warm); border: 3px solid var(--paper-dark); border-radius: var(--radius-lg); box-shadow: var(--shadow-lg); position: relative; }
.card-tape { position: absolute; top: -12px; left: 50%; transform: translateX(-50%) rotate(-2deg); width: 60px; height: 24px; background: rgba(192, 152, 64, 0.3); border-radius: 2px; box-shadow: var(--shadow-tape); }
.paper-card h1 { text-align: center; font-size: 28px; margin: 0 0 32px; }
.paper-card :deep(.el-form-item) { margin-bottom: 22px; }
.submit-btn { width: 100%; height: 48px; font-size: 16px; font-weight: 700; letter-spacing: 0.1em; }
.switch-link { text-align: center; margin-top: 20px; font-size: 13px; color: var(--ink-faded); cursor: pointer; }
.switch-link:hover { color: var(--ink); }
.switch-link b { color: var(--tag-blue); }
</style>
