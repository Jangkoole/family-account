<template>
  <el-dialog v-model="visible" :title="dialogTitle" width="500px" @close="handleClose">
    <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
      <el-form-item label="类型" prop="type">
        <el-radio-group v-model="form.type">
          <el-radio label="EXPENSE">支出</el-radio>
          <el-radio label="INCOME">收入</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="分类" prop="categoryId">
        <el-select v-model="form.categoryId" placeholder="请选择分类" clearable>
          <el-option
            v-for="item in categoryList"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="金额" prop="amount">
        <el-input-number v-model="form.amount" :precision="2" :min="0.01" placeholder="输入金额" />
      </el-form-item>
      <el-form-item label="日期" prop="date">
        <el-date-picker v-model="form.date" type="date" value-format="YYYY-MM-DD" />
      </el-form-item>
      <el-form-item label="备注" prop="note">
        <el-input v-model="form.note" type="textarea" rows="2" placeholder="可选" />
      </el-form-item>
      <el-form-item label="可见范围" prop="visible">
        <el-radio-group v-model="form.visible">
          <el-radio label="PRIVATE">仅自己可见</el-radio>
          <el-radio label="FAMILY" :disabled="!isFamilyMember">家庭成员可见</el-radio>
        </el-radio-group>
        <div v-if="!isFamilyMember" class="tip">独立用户只能设置为仅自己可见</div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="submitForm">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { addBill, updateBill, getBillDetail } from '@/api/bill'

const emit = defineEmits(['success'])
const visible = ref(false)
const formRef = ref()
const isEdit = ref(false)
const currentId = ref(null)

// 从 localStorage 获取用户信息，判断是否为家庭成员
const getUserFamilyId = () => {
  const userStr = localStorage.getItem('userInfo')
  if (userStr) {
    try {
      const user = JSON.parse(userStr)
      return user.familyId || null
    } catch(e) { return null }
  }
  return null
}
const isFamilyMember = computed(() => !!getUserFamilyId())

const dialogTitle = computed(() => isEdit.value ? '编辑账单' : '新增账单')

const form = reactive({
  type: 'EXPENSE',
  categoryId: null,
  amount: null,
  date: new Date().toISOString().slice(0,10),
  note: '',
  visible: 'PRIVATE'
})

const rules = {
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  date: [{ required: true, message: '请选择日期', trigger: 'change' }]
}

// 分类列表（从API获取，这里简化示例，实际应调用 /category/list）
const categoryList = ref([])
const loadCategories = async () => {
  // 临时模拟数据，正式使用时请替换为真实API调用
  categoryList.value = [
    { id: 1, name: '餐饮', type: 'EXPENSE' },
    { id: 2, name: '交通', type: 'EXPENSE' },
    { id: 9, name: '工资', type: 'INCOME' }
  ]
}

const open = async (id) => {
  visible.value = true
  if (id) {
    isEdit.value = true
    currentId.value = id
    const res = await getBillDetail(id)
    if (res.code === 200) {
      Object.assign(form, res.data)
    } else {
      ElMessage.error(res.message)
    }
  } else {
    isEdit.value = false
    currentId.value = null
    form.type = 'EXPENSE'
    form.categoryId = null
    form.amount = null
    form.date = new Date().toISOString().slice(0,10)
    form.note = ''
    form.visible = 'PRIVATE'
  }
}

const submitForm = async () => {
  await formRef.value.validate()
  const submitData = { ...form }
  if (isEdit.value) {
    submitData.id = currentId.value
    const res = await updateBill(submitData)
    if (res.code === 200) {
      ElMessage.success('修改成功')
      visible.value = false
      emit('success')
    } else {
      ElMessage.error(res.message)
    }
  } else {
    const res = await addBill(submitData)
    if (res.code === 200) {
      ElMessage.success('新增成功')
      visible.value = false
      emit('success')
    } else {
      ElMessage.error(res.message)
    }
  }
}

const handleClose = () => {
  formRef.value?.resetFields()
}

onMounted(() => {
  loadCategories()
})

defineExpose({ open })
</script>

<style scoped>
.tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>