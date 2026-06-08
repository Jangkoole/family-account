<template>
  <div class="login-page">
    <!-- 背景装饰 -->
    <div class="bg-pattern"></div>

    <!-- 左侧装饰区 -->
    <div class="login-hero">
      <div class="hero-content">
        <div class="hero-seal">家<br>账</div>
        <h1 class="hero-title">家庭记账本</h1>
        <p class="hero-subtitle">岁 月 有 痕 · 开 源 节 流</p>
        <div class="hero-line"></div>
        <p class="hero-desc">
          一粥一饭，当思来处不易<br>
          半丝半缕，恒念物力维艰
        </p>
      </div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="login-form-area">
      <div class="form-card corner-decoration">
        <h2 class="form-title">登 录</h2>

        <el-form :model="form" :rules="rules" ref="formRef" class="login-form">
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

          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
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
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              登 录
            </el-button>
          </el-form-item>

          <div class="form-footer">
            <span class="register-link" @click="router.push('/register')">
              没有账号？<b>去注册</b>
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
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '../../api/user'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = ref({
  account: '',
  password: ''
})

const rules = {
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

async function handleLogin() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await login(form.value)
      if (res.code === 200) {
        userStore.setToken(res.data.token)
        userStore.setUserInfo({ nickname: res.data.nickname })
        await userStore.syncFamilyInfo()
        ElMessage.success('登录成功')
        router.push('/')
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
/* ============================================
   登录页 - 新中式
   ============================================ */
.login-page {
  height: 100vh;
  display: flex;
  background-color: #FBF7F0;
  position: relative;
  overflow: hidden;
}

/* 背景纹理 */
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
    radial-gradient(ellipse at 30% 40%, rgba(196, 52, 46, 0.04) 0%, transparent 60%),
    radial-gradient(ellipse at 70% 60%, rgba(184, 134, 11, 0.04) 0%, transparent 60%);
  pointer-events: none;
}

/* 左侧装饰区 */
.login-hero {
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
  letter-spacing: 8px;
  margin-bottom: 12px;
}

.hero-subtitle {
  font-size: 13px;
  color: var(--text-secondary);
  letter-spacing: 10px;
  margin-bottom: 32px;
}

.hero-line {
  width: 60px;
  height: 2px;
  background: linear-gradient(90deg, var(--gold), var(--cinnabar));
  margin: 0 auto 32px;
}

.hero-desc {
  font-family: var(--font-display);
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 2;
  letter-spacing: 2px;
}

/* 右侧表单区 */
.login-form-area {
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
  letter-spacing: 8px;
  margin-bottom: 40px;
}

.login-form {
  animation: fadeInUp 0.6s ease-out 0.2s both;
}

.login-btn {
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

.login-btn:hover {
  box-shadow: 0 6px 20px rgba(196, 52, 46, 0.35);
  transform: translateY(-1px);
}

.form-footer {
  text-align: center;
  padding-top: 8px;
}

.register-link {
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  letter-spacing: 1px;
  transition: color var(--transition-fast);
}
.register-link:hover {
  color: var(--cinnabar);
}
.register-link b {
  color: var(--cinnabar);
  font-weight: 600;
}

/* 响应式 */
@media (max-width: 768px) {
  .login-hero {
    display: none;
  }
  .login-form-area {
    width: 100%;
    border-left: none;
  }
  .form-card {
    width: 90%;
    padding: 32px 24px;
  }
}
</style>
