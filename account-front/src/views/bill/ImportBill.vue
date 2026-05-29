<!-- src/views/bill/ImportBill.vue -->
<template>
  <el-dialog v-model="visible" title="导入账单" width="600px" @close="reset">
    <el-steps :active="activeStep" finish-status="success" align-center>
      <el-step title="上传文件" />
      <el-step title="预览映射" />
      <el-step title="完成" />
    </el-steps>

    <!-- 步骤1：上传文件 -->
    <div v-if="activeStep === 0" class="step-content">
      <el-form label-width="100px">
        <el-form-item label="账单来源">
          <el-select v-model="source">
            <el-option label="系统标准模板" value="SYSTEM" />
            <el-option label="微信账单" value="WECHAT" />
            <el-option label="支付宝账单" value="ALIPAY" />
          </el-select>
        </el-form-item>
        <el-form-item label="上传文件">
          <el-upload
            drag
            :auto-upload="false"
            :on-change="handleFileChange"
            :limit="1"
            accept=".xlsx,.csv"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
            <template #tip>
              <div class="el-upload__tip">支持 .xlsx 或 .csv 文件</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
    </div>

    <!-- 步骤2：预览映射结果 -->
    <div v-if="activeStep === 1" class="step-content">
      <el-alert title="预览映射" type="info" :closable="false" />
      <el-table :data="previewList" border stripe height="300">
        <el-table-column prop="date" label="日期" />
        <el-table-column prop="type" label="类型">
          <template #default="{ row }">{{ row.type === 'INCOME' ? '收入' : '支出' }}</template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" />
        <el-table-column prop="amount" label="金额" />
        <el-table-column prop="note" label="备注" show-overflow-tooltip />
      </el-table>
      <div class="field-mapping">
        <strong>字段映射：</strong>
        <pre>{{ JSON.stringify(fieldMapping, null, 2) }}</pre>
      </div>
    </div>

    <!-- 步骤3：导入结果 -->
    <div v-if="activeStep === 2" class="step-content">
      <el-result
        :icon="importResult.successCount > 0 ? 'success' : 'error'"
        :title="`成功导入 ${importResult.successCount} 条，失败 ${importResult.failCount} 条`"
      >
        <template #extra>
          <el-button type="primary" @click="visible = false">关闭</el-button>
        </template>
      </el-result>
      <div v-if="importResult.failReasons.length">
        <el-divider>失败详情</el-divider>
        <el-table :data="importResult.failReasons" size="small">
          <el-table-column prop="row" label="行号" width="80" />
          <el-table-column prop="reason" label="原因" />
        </el-table>
      </div>
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button v-if="activeStep === 0" type="primary" @click="nextStep1">下一步</el-button>
      <el-button v-if="activeStep === 1" type="primary" @click="nextStep2">确认导入</el-button>
      <el-button v-if="activeStep === 1" @click="activeStep--">上一步</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { previewImport, importBills } from '@/api/bill'

const visible = ref(false)
const activeStep = ref(0)
const source = ref('SYSTEM')
const file = ref(null)
const previewList = ref([])
const fieldMapping = ref({})
const importResult = ref({ successCount: 0, failCount: 0, failReasons: [] })

const handleFileChange = (uploadFile) => {
  file.value = uploadFile.raw
}

const nextStep1 = async () => {
  if (!file.value) {
    ElMessage.warning('请选择文件')
    return
  }
  const res = await previewImport(file.value, source.value)
  if (res.code === 200) {
    previewList.value = res.data.previewList || []
    fieldMapping.value = res.data.fieldMapping || {}
    activeStep.value = 1
  } else {
    ElMessage.error(res.message)
  }
}

const nextStep2 = async () => {
  const res = await importBills(file.value, source.value)
  if (res.code === 200) {
    importResult.value = res.data
    activeStep.value = 2
    emit('success')
  } else {
    ElMessage.error(res.message)
  }
}

const reset = () => {
  activeStep.value = 0
  file.value = null
  previewList.value = []
  fieldMapping.value = {}
  importResult.value = { successCount: 0, failCount: 0, failReasons: [] }
  source.value = 'SYSTEM'
}

const open = () => {
  reset()
  visible.value = true
}
const emit = defineEmits(['success'])
defineExpose({ open })
</script>

<style scoped>
.step-content {
  margin: 20px 0;
  min-height: 300px;
}
.field-mapping {
  margin-top: 16px;
  font-size: 12px;
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
}
</style>