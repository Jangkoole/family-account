<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'

const loggedIn = ref(false)
const nickname = ref('')

const loginForm = reactive({ account: '', password: '' })
const loggingIn = ref(false)

async function onLogin() {
  loggingIn.value = true
  try {
    const res = await axios.post('http://localhost:8090/user/login', loginForm)
    if (res.data.code === 200) {
      localStorage.setItem('token', res.data.data.token)
      localStorage.setItem('nickname', res.data.data.nickname)
      nickname.value = res.data.data.nickname
      loggedIn.value = true
    } else {
      alert(res.data.message)
    }
  } catch (e) {
    alert('登录失败')
  }
  loggingIn.value = false
}

function onLogout() {
  localStorage.removeItem('token')
  localStorage.removeItem('nickname')
  nickname.value = ''
  loggedIn.value = false
}

onMounted(() => {
  if (localStorage.getItem('token')) {
    nickname.value = localStorage.getItem('nickname') || ''
    loggedIn.value = true
  }
})
</script>

<template>
  <div v-if="!loggedIn" class="login-page">
    <div class="login-box">
      <h2>家庭记账系统</h2>
      <input v-model="loginForm.account" placeholder="账号" />
      <input v-model="loginForm.password" type="password" placeholder="密码" @keyup.enter="onLogin" />
      <button :disabled="loggingIn" @click="onLogin">
        {{ loggingIn ? '登录中...' : '登录' }}
      </button>
      <p class="hint">账号 test@test.com 密码 123456</p>
    </div>
  </div>

  <div v-else>
    <header class="topbar">
      <span>家庭记账系统</span>
      <span>
        {{ nickname }}
        <button class="logout-btn" @click="onLogout">退出</button>
      </span>
    </header>
    <router-view />
  </div>
</template>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background: #f5f7fa; }

.login-page { display: flex; justify-content: center; align-items: center; height: 100vh; background: #f5f7fa; }
.login-box { background: #fff; padding: 40px; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.1); width: 360px; }
.login-box h2 { text-align: center; margin-bottom: 24px; }
.login-box input { width: 100%; padding: 10px 12px; margin-bottom: 12px; border: 1px solid #dcdfe6; border-radius: 4px; font-size: 14px; }
.login-box button { width: 100%; padding: 10px; background: #409EFF; color: #fff; border: none; border-radius: 4px; font-size: 14px; cursor: pointer; }
.login-box button:hover { background: #337ECC; }
.login-box .hint { text-align: center; margin-top: 12px; font-size: 12px; color: #909399; }

.topbar { display: flex; justify-content: space-between; align-items: center; padding: 0 20px; height: 50px; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.08); }
.logout-btn { margin-left: 12px; padding: 4px 12px; border: 1px solid #dcdfe6; background: #fff; border-radius: 4px; cursor: pointer; font-size: 13px; }
.logout-btn:hover { color: #F56C6C; border-color: #F56C6C; }
</style>
