<template>
  <div>
    <div class="page-header">
      <h2>工程师管理</h2>
      <el-button type="primary" @click="showDialog = true">新建工程师</el-button>
    </div>

    <div class="card">
      <el-table :data="list" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="engineerCode" label="工号" width="100" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column label="级别" width="110">
          <template #default="{ row }">
            {{ EngineerLevelLabels[row.level] || row.level }}
          </template>
        </el-table-column>
        <el-table-column prop="skills" label="技能" min-width="160" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="engineerStatusType(row.currentStatus)" size="small">
              {{ EngineerStatusLabels[row.currentStatus] || row.currentStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="compositeScore" label="综合评分" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="160" />
        <el-table-column prop="joinDate" label="入职日期" width="120" />
      </el-table>
    </div>

    <el-dialog v-model="showDialog" title="新建工程师" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="工号" prop="engineerCode">
          <el-input v-model="form.engineerCode" maxlength="20" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" maxlength="30" />
        </el-form-item>
        <el-form-item label="级别" prop="level">
          <el-select v-model="form.level" placeholder="请选择">
            <el-option v-for="(label, key) in EngineerLevelLabels" :key="key" :label="label" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item label="技能" prop="skills">
          <el-input v-model="form.skills" maxlength="200" />
        </el-form-item>
        <el-form-item label="入职日期" prop="joinDate">
          <el-date-picker v-model="form.joinDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" maxlength="20" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" maxlength="60" />
        </el-form-item>
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
import { createEngineer, listEngineers } from '@/api/workforce'
import { EngineerLevelLabels, EngineerStatusLabels } from '@/types'
import type { Engineer } from '@/types'
import { required, maxLen, phoneRule } from '@/utils/validators'

const list = ref<Engineer[]>([])
const showDialog = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const initForm = () => ({
  engineerCode: '',
  name: '',
  level: '',
  skills: '',
  joinDate: '',
  phone: '',
  email: '',
})

const form = ref(initForm())

const emailRule = {
  type: 'email' as const,
  message: '请输入正确的邮箱地址',
  trigger: 'blur' as const,
}

const rules = {
  engineerCode: [required('工号不能为空'), maxLen(20)],
  name: [required('姓名不能为空'), maxLen(30)],
  level: [required('级别不能为空')],
  phone: [phoneRule],
  email: [emailRule],
}

function engineerStatusType(status: string) {
  const map: Record<string, string> = {
    IDLE: 'success',
    ON_PROJECT: '',
    TRAINING: 'warning',
    LEAVE: 'info',
  }
  return map[status] || 'info'
}

async function loadData() {
  const res = await listEngineers()
  list.value = res.data.data
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createEngineer(form.value as unknown as Record<string, unknown>)
    ElMessage.success('工程师创建成功')
    showDialog.value = false
    form.value = initForm()
    await loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>
