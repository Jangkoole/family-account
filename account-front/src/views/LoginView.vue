<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>家庭记账本</h2>
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="account">
          <el-input
            v-model="form.account"
            placeholder="手机号或邮箱"
            prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            style="width: 100%"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button
            style="width: 100%"
            @click="router.push('/register')"
          >
            没有账号？去注册
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/user'
import { useUserStore } from '../stores/user'

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
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f0f2f5;
}

.login-card {
  width: 400px;
  padding: 20px;
}

h2 {
  text-align: center;
  margin-bottom: 24px;
  color: #303133;
}
</style>