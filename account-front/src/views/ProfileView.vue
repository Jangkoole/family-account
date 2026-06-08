<template>
  <div class="profile-container">
    <!-- 顶部横幅 -->
    <div class="profile-banner">
      <div class="banner-bg"></div>
      <div class="banner-content">
        <div class="banner-avatar">
          <span class="avatar-text">{{ userStore.userInfo.nickname?.charAt(0) }}</span>
        </div>
        <div class="banner-text">
          <div class="banner-name">{{ userStore.userInfo.nickname }}</div>
          <div class="banner-role">
            {{ userStore.familyRole === 'ADMIN' ? '家庭管理员' : userStore.familyInfo ? '家庭成员' : '个人用户' }}
          </div>
        </div>
        <div class="banner-seal">
          <span>个<br>人</span>
        </div>
      </div>
    </div>

    <!-- 主题选择 -->
    <el-card shadow="never" class="zh-card profile-card">
      <template #header>
        <div class="card-header-zh">
          <span class="header-dot"></span>
          <span>主题样式</span>
        </div>
      </template>
      <div class="theme-selector">
        <div
          v-for="t in themes"
          :key="t.value"
          :class="['theme-option', { active: themeStore.current === t.value }]"
          @click="themeStore.setTheme(t.value)"
        >
          <span class="theme-icon">{{ t.icon }}</span>
          <span class="theme-label">{{ t.label }}</span>
          <span v-if="themeStore.current === t.value" class="theme-check">✓</span>
        </div>
      </div>
    </el-card>

    <!-- 修改昵称 -->
    <el-card shadow="never" class="zh-card profile-card">
      <template #header>
        <div class="card-header-zh">
          <span class="header-dot"></span>
          <span>修改昵称</span>
        </div>
      </template>
      <el-form :model="nicknameForm" :rules="nicknameRules" ref="nicknameFormRef" label-width="80px">
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="nicknameForm.nickname" placeholder="请输入新昵称" style="width: 300px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="nicknameLoading" @click="handleUpdateNickname">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 修改密码 -->
    <el-card shadow="never" class="zh-card profile-card">
      <template #header>
        <div class="card-header-zh">
          <span class="header-dot"></span>
          <span>修改密码</span>
        </div>
      </template>
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

    <!-- 默认可见范围 -->
    <el-card shadow="never" class="zh-card profile-card">
      <template #header>
        <div class="card-header-zh">
          <span class="header-dot"></span>
          <span>默认可见范围</span>
        </div>
      </template>
      <el-form label-width="100px">
        <el-form-item label="新建记录时">
          <el-radio-group v-model="defaultVisible" @change="handleUpdateDefaultVisible">
            <el-radio value="PRIVATE">仅自己可见</el-radio>
            <el-radio value="FAMILY" :disabled="!userStore.familyInfo">家庭成员可见</el-radio>
          </el-radio-group>
          <div class="form-hint">设置后，新建收支记录时默认使用此可见范围</div>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 家庭组管理 -->
    <el-card shadow="never" class="zh-card profile-card">
      <template #header>
        <div class="card-header-zh">
          <span class="header-dot"></span>
          <span>家庭组管理</span>
        </div>
      </template>

      <!-- 未加入 -->
      <div v-if="!userStore.familyInfo">
        <el-empty description="您还未加入任何家庭组">
          <el-button type="primary" @click="showCreateDialog = true">创建家庭组</el-button>
          <el-button @click="showJoinDialog = true">加入家庭组</el-button>
        </el-empty>
      </div>

      <!-- 已加入 -->
      <div v-else>
        <el-descriptions :column="2" border style="margin-bottom: 20px">
          <el-descriptions-item label="家庭名称">{{ userStore.familyInfo.name }}</el-descriptions-item>
          <el-descriptions-item label="成员数量">{{ userStore.familyInfo.memberCount }}</el-descriptions-item>
          <el-descriptions-item label="管理员">{{ userStore.familyInfo.adminNickname }}</el-descriptions-item>
          <el-descriptions-item v-if="userStore.familyRole === 'ADMIN'" label="邀请码">
            {{ userStore.familyInfo.inviteCode }}
            <el-button size="small" style="margin-left: 10px" @click="handleRefreshInviteCode">刷新</el-button>
            <el-button size="small" style="margin-left: 6px" @click="handleCopyInviteCode">复制</el-button>
          </el-descriptions-item>
        </el-descriptions>

        <el-table :data="memberList" style="margin-bottom: 20px">
          <el-table-column label="昵称" prop="nickname" />
          <el-table-column label="账号" prop="account" />
          <el-table-column label="角色">
            <template #default="{ row }">
              <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'" effect="light">
                {{ row.role === 'ADMIN' ? '管理员' : '普通成员' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="加入时间" prop="joinTime" />
          <el-table-column v-if="userStore.familyRole === 'ADMIN'" label="操作">
            <template #default="{ row }">
              <el-button v-if="row.role !== 'ADMIN'" size="small" type="danger" @click="handleRemoveMember(row.userId)">移除</el-button>
              <el-button v-if="row.role !== 'ADMIN'" size="small" type="warning" @click="handleTransferAdmin(row.userId)">转让管理员</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 待审核申请 -->
        <div v-if="userStore.familyRole === 'ADMIN' && applyList.length > 0">
          <div class="section-subtitle">待审核申请</div>
          <el-table :data="applyList" style="margin-bottom: 20px">
            <el-table-column label="昵称" prop="nickname" />
            <el-table-column label="账号" prop="account" />
            <el-table-column label="申请时间" prop="applyTime" />
            <el-table-column label="操作">
              <template #default="{ row }">
                <el-button size="small" type="primary" @click="handleReviewApply(row.applyId, true)">通过</el-button>
                <el-button size="small" type="danger" @click="handleReviewApply(row.applyId, false)">拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="family-actions">
          <el-button v-if="userStore.familyRole !== 'ADMIN'" type="danger" @click="handleQuitFamily">退出家庭组</el-button>
          <el-button v-if="userStore.familyRole === 'ADMIN'" type="danger" @click="handleDissolveFamily">解散家庭组</el-button>
        </div>
      </div>
    </el-card>

    <!-- 创建家庭组对话框 -->
    <el-dialog v-model="showCreateDialog" title="创建家庭组" width="400px">
      <el-form :model="createForm" ref="createFormRef">
        <el-form-item label="家庭名称" prop="name" :rules="[{ required: true, message: '请输入家庭名称' }]">
          <el-input v-model="createForm.name" placeholder="请输入家庭名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="createLoading" @click="handleCreateFamily">确认</el-button>
      </template>
    </el-dialog>

    <!-- 加入家庭组对话框 -->
    <el-dialog v-model="showJoinDialog" title="加入家庭组" width="400px">
      <el-form :model="joinForm" ref="joinFormRef">
        <el-form-item label="邀请码" prop="inviteCode" :rules="[{ required: true, message: '请输入邀请码' }]">
          <el-input v-model="joinForm.inviteCode" placeholder="请输入邀请码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showJoinDialog = false">取消</el-button>
        <el-button type="primary" :loading="joinLoading" @click="handleJoinFamily">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { updateNickname, updatePassword, updateDefaultVisible } from '../api/user'
import {
  createFamily, joinFamily, getMemberList,
  getApplyList, reviewApply, removeMember,
  transferAdmin, refreshInviteCode, quitFamily,
  dissolveFamily
} from '../api/family'
import { useUserStore } from '../stores/user'
import { useThemeStore, themes } from '../stores/theme'

const userStore = useUserStore()
const themeStore = useThemeStore()

const nicknameFormRef = ref()
const nicknameLoading = ref(false)
const nicknameForm = ref({ nickname: userStore.userInfo.nickname || '' })
const nicknameRules = { nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }] }

async function handleUpdateNickname() {
  await nicknameFormRef.value.validate(async (valid) => {
    if (!valid) return
    nicknameLoading.value = true
    try {
      const res = await updateNickname(nicknameForm.value)
      if (res.code === 200) { userStore.setUserInfo({ ...userStore.userInfo, nickname: nicknameForm.value.nickname }); ElMessage.success('昵称修改成功') }
      else ElMessage.error(res.message)
    } finally { nicknameLoading.value = false }
  })
}

const passwordFormRef = ref()
const passwordLoading = ref(false)
const passwordForm = ref({ oldPassword: '', newPassword: '' })
const passwordRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }]
}

const defaultVisible = ref(userStore.userInfo.defaultVisible || 'PRIVATE')

async function handleUpdateDefaultVisible() {
  try {
    const res = await updateDefaultVisible({ defaultVisible: defaultVisible.value })
    if (res.code === 200) { userStore.setUserInfo({ ...userStore.userInfo, defaultVisible: defaultVisible.value }); ElMessage.success('默认可见范围已更新') }
    else ElMessage.error(res.message)
  } catch (e) { ElMessage.error('设置失败') }
}

async function handleUpdatePassword() {
  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return
    passwordLoading.value = true
    try {
      const res = await updatePassword(passwordForm.value)
      if (res.code === 200) { ElMessage.success('密码修改成功，请重新登录'); userStore.clear(); setTimeout(() => { window.location.href = '/login' }, 1500) }
      else ElMessage.error(res.message)
    } finally { passwordLoading.value = false }
  })
}

const memberList = ref([])
const applyList = ref([])
const showCreateDialog = ref(false)
const showJoinDialog = ref(false)
const createLoading = ref(false)
const joinLoading = ref(false)
const createFormRef = ref()
const joinFormRef = ref()
const createForm = ref({ name: '' })
const joinForm = ref({ inviteCode: '' })

onMounted(async () => {
  if (userStore.familyInfo) { await loadMemberList(); if (userStore.familyRole === 'ADMIN') await loadApplyList() }
})

async function loadMemberList() { const res = await getMemberList(); if (res.code === 200) memberList.value = res.data }
async function loadApplyList() { const res = await getApplyList(); if (res.code === 200) applyList.value = res.data }

async function handleCreateFamily() {
  await createFormRef.value.validate(async (valid) => {
    if (!valid) return; createLoading.value = true
    try {
      const res = await createFamily(createForm.value)
      if (res.code === 200) { ElMessage.success('家庭组创建成功'); showCreateDialog.value = false; await userStore.syncFamilyInfo(); await loadMemberList() }
      else ElMessage.error(res.message)
    } finally { createLoading.value = false }
  })
}

async function handleJoinFamily() {
  await joinFormRef.value.validate(async (valid) => {
    if (!valid) return; joinLoading.value = true
    try {
      const res = await joinFamily({ inviteCode: joinForm.value.inviteCode.trim() })
      if (res.code === 200) { ElMessage.success('申请已提交，等待管理员审核'); showJoinDialog.value = false }
      else ElMessage.error(res.message)
    } finally { joinLoading.value = false }
  })
}

async function handleReviewApply(applyId, approve) {
  const res = await reviewApply({ applyId, approve })
  if (res.code === 200) { ElMessage.success(approve ? '已通过申请' : '已拒绝申请'); await loadApplyList(); await loadMemberList(); await userStore.syncFamilyInfo() }
  else ElMessage.error(res.message)
}

async function handleRemoveMember(userId) {
  await ElMessageBox.confirm('确认移除该成员？', '提示', { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' })
  const res = await removeMember(userId)
  if (res.code === 200) { ElMessage.success('成员已移除'); await loadMemberList(); await userStore.syncFamilyInfo() }
  else ElMessage.error(res.message)
}

async function handleTransferAdmin(userId) {
  await ElMessageBox.confirm('确认转让管理员身份？转让后您将成为普通成员', '提示', { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' })
  const res = await transferAdmin({ userId })
  if (res.code === 200) { ElMessage.success('管理员已转让'); await userStore.syncFamilyInfo(); await loadMemberList() }
  else ElMessage.error(res.message)
}

async function handleRefreshInviteCode() {
  const res = await refreshInviteCode()
  if (res.code === 200) { ElMessage.success('邀请码已刷新'); await userStore.syncFamilyInfo() }
  else ElMessage.error(res.message)
}

function handleCopyInviteCode() { navigator.clipboard.writeText(userStore.familyInfo.inviteCode); ElMessage.success('邀请码已复制') }

async function handleQuitFamily() {
  await ElMessageBox.confirm('确认退出家庭组？', '提示', { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' })
  const res = await quitFamily()
  if (res.code === 200) { ElMessage.success('已退出家庭组'); userStore.clearFamilyInfo(); memberList.value = [] }
  else ElMessage.error(res.message)
}

async function handleDissolveFamily() {
  await ElMessageBox.confirm('解散后所有成员将自动退出，家庭组将永久删除，无法恢复，确认解散？', '解散家庭组', {
    confirmButtonText: '确认解散', cancelButtonText: '取消', type: 'warning', confirmButtonClass: 'el-button--danger'
  })
  const res = await dissolveFamily()
  if (res.code === 200) { ElMessage.success('家庭组已解散'); userStore.clearFamilyInfo(); memberList.value = []; applyList.value = [] }
  else ElMessage.error(res.message)
}
</script>

<style scoped>
.profile-container { max-width: 900px; margin: 0 auto; }

/* 顶部横幅 */
.profile-banner {
  position: relative;
  border-radius: var(--radius-lg);
  padding: 36px 40px;
  margin-bottom: 24px;
  overflow: hidden;
}

.banner-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #1a1a1a 0%, #2c2416 60%, #3d2810 100%);
}

/* 背景中式纹样 */
.banner-bg::after {
  content: '';
  position: absolute;
  inset: 0;
  background-image:
    repeating-linear-gradient(90deg, transparent, transparent 40px, rgba(201, 169, 110, 0.04) 40px, rgba(201, 169, 110, 0.04) 41px),
    radial-gradient(circle at 85% 20%, rgba(196, 52, 46, 0.15) 0%, transparent 40%);
}

.banner-content {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 20px;
  color: #FBF7F0;
}

.banner-avatar {
  width: 64px;
  height: 64px;
  border: 2px solid var(--gold-light);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(201, 169, 110, 0.1);
  flex-shrink: 0;
}

.avatar-text {
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 700;
  color: var(--gold-light);
}

.banner-text {
  flex: 1;
}

.banner-name {
  font-family: var(--font-display);
  font-size: 24px;
  font-weight: 600;
  letter-spacing: 3px;
  margin-bottom: 4px;
}

.banner-role {
  font-size: 13px;
  opacity: 0.7;
  letter-spacing: 2px;
}

.banner-seal {
  width: 48px;
  height: 48px;
  border: 1.5px solid rgba(201, 169, 110, 0.5);
  border-radius: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 13px;
  color: rgba(201, 169, 110, 0.6);
  line-height: 1.3;
  letter-spacing: 1px;
  transform: rotate(-6deg);
  flex-shrink: 0;
}

/* 卡片 */
.zh-card {
  background: #fffdf8;
  border: 1px solid var(--gold-pale);
  border-radius: var(--radius-md);
}
.profile-card { margin-bottom: 20px; }

.card-header-zh {
  display: flex; align-items: center; gap: 10px;
}
.header-dot {
  width: 8px; height: 8px; background: var(--cinnabar); border-radius: 50%; flex-shrink: 0;
}

.form-hint {
  color: var(--text-secondary); font-size: 12px; margin-top: 8px; letter-spacing: 1px;
}

.section-subtitle {
  font-family: var(--font-display); font-weight: 600; color: var(--ink-deep);
  letter-spacing: 2px; margin-bottom: 12px;
}

.family-actions { padding-top: 8px; }

/* ---- 主题选择器 ---- */
.theme-selector {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.theme-option {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 20px;
  border: 1px solid var(--gold-pale);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-normal);
  background: #fffdf8;
  min-width: 140px;
  position: relative;
}

.theme-option:hover {
  border-color: var(--gold);
  box-shadow: var(--shadow-sm);
}

.theme-option.active {
  border-color: var(--cinnabar);
  background: rgba(196, 52, 46, 0.04);
  box-shadow: 0 0 0 1px var(--cinnabar);
}

.theme-icon {
  font-size: 24px;
  line-height: 1;
}

.theme-label {
  font-family: var(--font-display);
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-deep);
  letter-spacing: 2px;
}

.theme-check {
  position: absolute;
  top: 6px;
  right: 8px;
  font-size: 12px;
  color: var(--cinnabar);
  font-weight: 700;
}

@media (max-width: 768px) {
  .profile-banner { padding: 24px; }
  .banner-seal { display: none; }
}
</style>
