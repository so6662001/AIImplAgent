<template>
  <div>
    <div class="page-header">
      <h2>培训日志</h2>
      <div style="display: flex; align-items: center; gap: 12px;">
        <el-input-number v-model="filterProjectId" :min="1" placeholder="项目ID" controls-position="right" @change="loadData" />
        <el-button type="primary" @click="openCreate">新增日志</el-button>
      </div>
    </div>

    <div class="card">
      <el-table :data="logs" stripe @row-click="openUpdate">
        <el-table-column prop="logDate" label="日期" width="110" />
        <el-table-column prop="topic" label="培训主题" min-width="180" />
        <el-table-column prop="trainerName" label="讲师" width="100" />
        <el-table-column prop="attendeeCount" label="参训人数" width="90" />
        <el-table-column label="签到" width="60">
          <template #default="{ row }">{{ row.signInCompleted ? '✅' : '❌' }}</template>
        </el-table-column>
        <el-table-column label="课件" width="60">
          <template #default="{ row }">{{ row.coursewareUploaded ? '✅' : '❌' }}</template>
        </el-table-column>
        <el-table-column label="总结" width="60">
          <template #default="{ row }">{{ row.summaryUploaded ? '✅' : '❌' }}</template>
        </el-table-column>
        <el-table-column label="考核" width="60">
          <template #default="{ row }">{{ row.examConducted ? '✅' : '❌' }}</template>
        </el-table-column>
        <el-table-column label="日报" width="60">
          <template #default="{ row }">{{ row.dailyReportSubmitted ? '✅' : '❌' }}</template>
        </el-table-column>
        <el-table-column prop="issues" label="问题" min-width="150" />
      </el-table>
    </div>

    <el-dialog v-model="showDialog" :title="isUpdate ? '更新日志' : '新增日志'" width="700px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="项目ID" prop="projectId">
          <el-input-number v-model="form.projectId" :min="1" controls-position="right" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="日期" prop="logDate">
              <el-date-picker v-model="form.logDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="讲师" prop="trainerName">
              <el-input v-model="form.trainerName" maxlength="30" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="培训主题" prop="topic">
          <el-input v-model="form.topic" maxlength="100" />
        </el-form-item>
        <el-form-item label="参训人数">
          <el-input-number v-model="form.attendeeCount" :min="0" controls-position="right" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="签到完成">
              <el-switch v-model="form.signInCompleted" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="课件上传">
              <el-switch v-model="form.coursewareUploaded" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="总结上传">
              <el-switch v-model="form.summaryUploaded" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="考核完成">
              <el-switch v-model="form.examConducted" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="日报提交">
              <el-switch v-model="form.dailyReportSubmitted" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="问题记录">
          <el-input v-model="form.issues" type="textarea" :rows="3" maxlength="500" />
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
import { createTrainingLog, listTrainingLogs, updateTrainingLog } from '@/api/traininglog'
import type { TrainingDailyLog } from '@/types'
import { required, maxLen } from '@/utils/validators'

const logs = ref<TrainingDailyLog[]>([])
const showDialog = ref(false)
const submitting = ref(false)
const isUpdate = ref(false)
const updateId = ref<number>(0)
const formRef = ref<FormInstance>()
const filterProjectId = ref<number>(1)

function getDefaultForm() {
  return {
    projectId: filterProjectId.value,
    logDate: '',
    topic: '',
    trainerName: '',
    attendeeCount: 0,
    signInCompleted: false,
    coursewareUploaded: false,
    summaryUploaded: false,
    examConducted: false,
    dailyReportSubmitted: false,
    issues: '',
  }
}

const form = ref(getDefaultForm())

const rules = {
  projectId: [required('项目ID不能为空')],
  logDate: [required('日期不能为空')],
  topic: [required('培训主题不能为空'), maxLen(100)],
  trainerName: [required('讲师不能为空')],
}

function openCreate() {
  isUpdate.value = false
  form.value = getDefaultForm()
  showDialog.value = true
}

function openUpdate(row: TrainingDailyLog) {
  isUpdate.value = true
  updateId.value = row.id
  form.value = {
    projectId: row.projectId,
    logDate: row.logDate,
    topic: row.topic,
    trainerName: row.trainerName,
    attendeeCount: row.attendeeCount,
    signInCompleted: row.signInCompleted,
    coursewareUploaded: row.coursewareUploaded,
    summaryUploaded: row.summaryUploaded,
    examConducted: row.examConducted,
    dailyReportSubmitted: row.dailyReportSubmitted,
    issues: row.issues,
  }
  showDialog.value = true
}

async function loadData() {
  if (!filterProjectId.value) return
  const res = await listTrainingLogs(filterProjectId.value)
  logs.value = res.data.data
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isUpdate.value) {
      await updateTrainingLog(updateId.value, form.value as unknown as Record<string, unknown>)
      ElMessage.success('日志更新成功')
    } else {
      await createTrainingLog(form.value as unknown as Record<string, unknown>)
      ElMessage.success('日志创建成功')
    }
    showDialog.value = false
    await loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>
