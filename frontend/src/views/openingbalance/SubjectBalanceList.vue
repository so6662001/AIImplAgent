<template>
  <div>
    <div class="page-header">
      <h2>科目余额</h2>
      <div>
        <el-button @click="handleTrialBalance">试算平衡</el-button>
        <el-button type="primary" @click="showDialog = true">新增记录</el-button>
      </div>
    </div>

    <div class="card">
      <div style="margin-bottom: 16px">
        <el-input-number v-model="filterProjectId" :min="1" placeholder="项目ID" controls-position="right" style="width: 180px" />
        <el-button type="primary" @click="loadData" style="margin-left: 8px">查询</el-button>
      </div>
      <el-table :data="list" stripe>
        <el-table-column prop="subjectCode" label="科目编码" width="130" />
        <el-table-column prop="subjectName" label="科目名称" min-width="180" />
        <el-table-column prop="debitBalance" label="借方余额" width="130" />
        <el-table-column prop="creditBalance" label="贷方余额" width="130" />
      </el-table>
    </div>

    <el-dialog v-model="showDialog" title="新增科目余额" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="项目ID" prop="projectId">
          <el-input-number v-model="form.projectId" :min="1" controls-position="right" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="科目编码" prop="subjectCode">
              <el-input v-model="form.subjectCode" maxlength="32" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="科目名称">
              <el-input v-model="form.subjectName" maxlength="60" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="借方余额" prop="debitBalance">
              <el-input-number v-model="form.debitBalance" :step="100" :precision="2" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="贷方余额" prop="creditBalance">
              <el-input-number v-model="form.creditBalance" :step="100" :precision="2" controls-position="right" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showTrialDialog" title="试算平衡结果" width="500px" destroy-on-close>
      <el-result
        :icon="trialResult?.balanced ? 'success' : 'error'"
        :title="trialResult?.balanced ? '试算平衡' : '试算不平衡'"
      >
        <template #sub-title>
          <div style="font-size: 16px; line-height: 2">
            <div>借方合计：{{ trialResult?.totalDebit?.toFixed(2) }}</div>
            <div>贷方合计：{{ trialResult?.totalCredit?.toFixed(2) }}</div>
          </div>
        </template>
      </el-result>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import { createSubjectBalance, listSubjectBalances, getTrialBalance } from '@/api/subjectbalance'
import type { SubjectBalance, TrialBalance } from '@/types'
import { required } from '@/utils/validators'

const list = ref<SubjectBalance[]>([])
const showDialog = ref(false)
const showTrialDialog = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const filterProjectId = ref<number>(1)
const trialResult = ref<TrialBalance | null>(null)

const form = ref({
  projectId: 1,
  subjectCode: '',
  subjectName: '',
  debitBalance: 0,
  creditBalance: 0,
})

const rules = {
  projectId: [required('项目ID不能为空')],
  subjectCode: [required('科目编码不能为空')],
  debitBalance: [required('借方余额不能为空')],
  creditBalance: [required('贷方余额不能为空')],
}

async function loadData() {
  if (!filterProjectId.value) return
  const res = await listSubjectBalances(filterProjectId.value)
  list.value = res.data.data
}

async function handleTrialBalance() {
  if (!filterProjectId.value) {
    ElMessage.warning('请先输入项目ID')
    return
  }
  const res = await getTrialBalance(filterProjectId.value)
  trialResult.value = res.data.data
  showTrialDialog.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createSubjectBalance(form.value as unknown as Record<string, unknown>)
    ElMessage.success('科目余额创建成功')
    showDialog.value = false
    await loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>
