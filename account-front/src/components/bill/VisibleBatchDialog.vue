<!-- src/components/Bill/VisibleBatchDialog.vue -->
<template>
  <el-dialog v-model="visible" title="批量修改可见范围" width="400px">
    <el-radio-group v-model="newVisible">
      <el-radio label="PRIVATE">仅自己可见</el-radio>
      <el-radio label="FAMILY" :disabled="!isFamilyMember">家庭成员可见</el-radio>
    </el-radio-group>
    <div v-if="!isFamilyMember" class="tip">独立用户无法批量设置为家庭成员可见</div>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="submit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { updateVisible } from '@/api/bill'

const props = defineProps({
  selectedIds: { type: Array, required: true }
})
const emit = defineEmits(['success'])

const visible = ref(false)
const newVisible = ref('PRIVATE')

// 从 localStorage 获取用户家庭ID
const getFamilyId = () => {
  const userStr = localStorage.getItem('userInfo')
  if (userStr) {
    try {
      const user = JSON.parse(userStr)
      return user.familyId || null
    } catch(e) { return null }
  }
  return null
}
const isFamilyMember = computed(() => !!getFamilyId())

const open = () => {
  newVisible.value = 'PRIVATE'
  visible.value = true
}

const submit = async () => {
  if (props.selectedIds.length === 0) {
    ElMessage.warning('没有选中的账单')
    return
  }
  const res = await updateVisible({ ids: props.selectedIds, visible: newVisible.value })
  if (res.code === 200) {
    ElMessage.success('修改成功')
    visible.value = false
    emit('success')
  } else {
    ElMessage.error(res.message)
  }
}

defineExpose({ open })
</script>

<style scoped>
.tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}
</style>