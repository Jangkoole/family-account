<template>
  <div>
    <el-card style="margin-bottom: 20px">
      <template #header>修改昵称</template>
      <el-form :model="nicknameForm" :rules="nicknameRules" ref="nicknameFormRef" label-width="80px">
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="nicknameForm.nickname" placeholder="请输入新昵称" style="width: 300px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="nicknameLoading" @click="handleUpdateNickname">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <template #header>修改密码</template>
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="80px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入旧密码" style="width: 300px" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" style="width: 300px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="passwordLoading" @click="handleUpdatePassword">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { updateNickname, updatePassword } from '../api/user'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()

const nicknameFormRef = ref()
const nicknameLoading = ref(false)
const nicknameForm = ref({
  nickname: userStore.userInfo.nickname || ''
})
const nicknameRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ]
}

async function handleUpdateNickname() {
  await nicknameFormRef.value.validate(async (valid) => {
    if (!valid) return
    nicknameLoading.value = true
    try {
      const res = await updateNickname(nicknameForm.value)
      if (res.code === 200) {
        userStore.setUserInfo({ ...userStore.userInfo, nickname: nicknameForm.value.nickname })
        ElMessage.success('昵称修改成功')
      } else {
        ElMessage.error(res.message)
      }
    } finally {
      nicknameLoading.value = false
    }
  })
}

const passwordFormRef = ref()
const passwordLoading = ref(false)
const passwordForm = ref({
  oldPassword: '',
  newPassword: ''
})
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

async function handleUpdatePassword() {
  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return
    passwordLoading.value = true
    try {
      const res = await updatePassword(passwordForm.value)
      if (res.code === 200) {
        ElMessage.success('密码修改成功，请重新登录')
        userStore.clear()
        setTimeout(() => {
          window.location.href = '/login'
        }, 1500)
      } else {
        ElMessage.error(res.message)
      }
    } finally {
      passwordLoading.value = false
    }
  })
}
</script>