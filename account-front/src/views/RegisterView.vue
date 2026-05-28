<template>
  <div class="register-container">
    <el-card class="register-card">
      <h2>注册账号</h2>
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
        <el-form-item prop="nickname">
          <el-input
            v-model="form.nickname"
            placeholder="昵称"
            prefix-icon="EditPen"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            style="width: 100%"
            :loading="loading"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button
            style="width: 100%"
            @click="router.push('/login')"
          >
            已有账号？去登录
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
import { register } from '../api/user'

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
.register-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f0f2f5;
}

.register-card {
  width: 400px;
  padding: 20px;
}

h2 {
  text-align: center;
  margin-bottom: 24px;
  color: #303133;
}
</style>