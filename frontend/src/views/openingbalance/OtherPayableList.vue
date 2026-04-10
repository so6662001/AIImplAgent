<template>
  <div>
    <div class="page-header">
      <h2>其他应付</h2>
      <el-button type="primary" @click="showDialog = true">新增记录</el-button>
    </div>

    <div class="card">
      <div style="margin-bottom: 16px">
        <el-input-number v-model="filterProjectId" :min="1" placeholder="项目ID" controls-position="right" style="width: 180px" />
        <el-button type="primary" @click="loadData" style="margin-left: 8px">查询</el-button>
      </div>
      <el-table :data="list" stripe>
        <el-table-column prop="subjectCode" label="科目编码" width="120" />
        <el-table-column prop="objectType" label="对象类型" width="100" />
        <el-table-column prop="objectName" label="对象名称" min-width="140" />
        <el-table-column prop="summary" label="摘要" min-width="160" />
        <el-table-column prop="amount" label="金额" width="120" />
        <el-table-column prop="occurDate" label="发生日期" width="110" />
      </el-table>
    </div>

    <el-dialog v-model="showDialog" title="新增其他应付" width="700px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="项目ID" prop="projectId">
              <el-input-number v-model="form.projectId" :min="1" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="科目编码" prop="subjectCode">
              <el-input v-model="form.subjectCode" maxlength="32" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="对象类型" prop="objectType">
              <el-select v-model="form.objectType" placeholder="请选择">
                <el-option label="客户" value="客户" />
                <el-option label="供应商" value="供应商" />
                <el-option label="员工" value="员工" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="对象名称" prop="objectName">
              <el-input v-model="form.objectName" maxlength="60" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="摘要">
          <el-input v-model="form.summary" maxlength="200" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="金额" prop="amount">
              <el-input-number v-model="form.amount" :min="0.01" :step="100" :precision="2" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发生日期">
              <el-date-picker v-model="form.occurDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import { createOtherPayable, listOtherPayables } from '@/api/otherpayable'
import type { OtherPayable } from '@/types'
import { required } from '@/utils/validators'

const list = ref<OtherPayable[]>([])
const showDialog = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const filterProjectId = ref<number>(1)

const form = ref({
  projectId: 1,
  subjectCode: '',
  objectType: '',
  objectId: 0,
  objectName: '',
  summary: '',
  amount: 0,
  occurDate: '',
})

const positiveAmount = {
  validator: (_rule: unknown, value: number, callback: (err?: Error) => void) => {
    if (value === undefined || value === null || value <= 0) {
      callback(new Error('金额必须大于0'))
      return
    }
    callback()
  },
  trigger: 'blur',
}

const rules = {
  projectId: [required('项目ID不能为空')],
  subjectCode: [required('科目编码不能为空')],
  objectType: [required('请选择对象类型')],
  objectName: [required('对象名称不能为空')],
  amount: [positiveAmount],
}

async function loadData() {
  if (!filterProjectId.value) return
  const res = await listOtherPayables(filterProjectId.value)
  list.value = res.data.data
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createOtherPayable(form.value as unknown as Record<string, unknown>)
    ElMessage.success('其他应付创建成功')
    showDialog.value = false
    await loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>
