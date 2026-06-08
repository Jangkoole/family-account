<template>
  <div class="register-page">
    <div class="bg-pattern"></div>

    <!-- 左侧 -->
    <div class="register-hero">
      <div class="hero-content">
        <div class="hero-seal">注<br>册</div>
        <h1 class="hero-title">加入记账</h1>
        <p class="hero-subtitle">开 启 你 的 理 财 之 旅</p>
        <div class="hero-line"></div>
      </div>
    </div>

    <!-- 右侧表单 -->
    <div class="register-form-area">
      <div class="form-card corner-decoration">
        <h2 class="form-title">注 册 账 号</h2>

        <el-form :model="form" :rules="rules" ref="formRef" class="register-form">
          <el-form-item prop="account">
            <el-input
              v-model="form.account"
              placeholder="手机号或邮箱"
              size="large"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="nickname">
            <el-input
              v-model="form.nickname"
              placeholder="昵称"
              size="large"
            >
              <template #prefix>
                <el-icon><EditPen /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码（不少于6位）"
              size="large"
              show-password
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="register-btn"
              :loading="loading"
              @click="handleRegister"
            >
              注 册
            </el-button>
          </el-form-item>

          <div class="form-footer">
            <span class="login-link" @click="router.push('/login')">
              已有账号？<b>去登录</b>
            </span>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, EditPen } from '@element-plus/icons-vue'
import { register } from '../../api/user'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = ref({
  account: '',
  password: '',
  nickname: ''
})

const rules = {
  account: [
    { required: true, message: '请输入手机号或邮箱', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ]
}

async function handleRegister() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await register(form.value)
      if (res.code === 200) {
        ElMessage.success('注册成功，请登录')
        router.push('/login')
      } else {
        ElMessage.error(res.message)
      }
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.register-page {
  height: 100vh;
  display: flex;
  background-color: #FBF7F0;
  position: relative;
  overflow: hidden;
}

.bg-pattern {
  position: absolute;
  inset: 0;
  background:
    repeating-linear-gradient(
      0deg,
      transparent,
      transparent 3px,
      rgba(139, 125, 107, 0.025) 3px,
      rgba(139, 125, 107, 0.025) 6px
    ),
    radial-gradient(ellipse at 70% 30%, rgba(196, 52, 46, 0.04) 0%, transparent 60%),
    radial-gradient(ellipse at 30% 70%, rgba(184, 134, 11, 0.04) 0%, transparent 60%);
  pointer-events: none;
}

.register-hero {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.hero-content {
  text-align: center;
  animation: fadeInUp 0.8s ease-out;
}

.hero-seal {
  width: 80px;
  height: 80px;
  border: 3px solid var(--cinnabar);
  border-radius: 5px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--cinnabar);
  font-family: var(--font-display);
  font-weight: 900;
  font-size: 24px;
  line-height: 1.3;
  letter-spacing: 3px;
  transform: rotate(-8deg);
  margin-bottom: 32px;
  background: rgba(196, 52, 46, 0.03);
}

.hero-title {
  font-family: var(--font-display);
  font-size: 36px;
  font-weight: 700;
  color: var(--ink-black);
  letter-spacing: 6px;
  margin-bottom: 12px;
}

.hero-subtitle {
  font-size: 13px;
  color: var(--text-secondary);
  letter-spacing: 8px;
  margin-bottom: 32px;
}

.hero-line {
  width: 60px;
  height: 2px;
  background: linear-gradient(90deg, var(--gold), var(--cinnabar));
  margin: 0 auto;
}

.register-form-area {
  width: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #fffdf8 0%, #F5F0E6 100%);
  border-left: 1px solid var(--gold-pale);
  box-shadow: -4px 0 24px rgba(44, 36, 22, 0.06);
  position: relative;
}

.form-card {
  width: 360px;
  padding: 48px 40px;
  background: #fffdf8;
  border: 1px solid var(--gold-pale);
  border-radius: var(--radius-lg);
  position: relative;
}

.form-title {
  font-family: var(--font-display);
  font-size: 24px;
  font-weight: 700;
  color: var(--ink-deep);
  text-align: center;
  letter-spacing: 6px;
  margin-bottom: 40px;
}

.register-form {
  animation: fadeInUp 0.6s ease-out 0.2s both;
}

.register-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 6px;
  font-family: var(--font-display);
  font-weight: 600;
  background: linear-gradient(135deg, var(--cinnabar) 0%, var(--cinnabar-dark) 100%);
  border: none;
  border-radius: var(--radius-sm);
  transition: all var(--transition-normal);
}

.register-btn:hover {
  box-shadow: 0 6px 20px rgba(196, 52, 46, 0.35);
  transform: translateY(-1px);
}

.form-footer {
  text-align: center;
  padding-top: 8px;
}

.login-link {
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  letter-spacing: 1px;
  transition: color var(--transition-fast);
}
.login-link:hover {
  color: var(--cinnabar);
}
.login-link b {
  color: var(--cinnabar);
  font-weight: 600;
}

@media (max-width: 768px) {
  .register-hero {
    display: none;
  }
  .register-form-area {
    width: 100%;
    border-left: none;
  }
  .form-card {
    width: 90%;
    padding: 32px 24px;
  }
}
</style>
