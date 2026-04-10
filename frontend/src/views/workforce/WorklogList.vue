<template>
  <div>
    <div class="page-header">
      <h2>工作日志</h2>
      <div style="display: flex; align-items: center; gap: 12px;">
        <el-input-number v-model="filterEngineerId" :min="1" placeholder="工程师ID" controls-position="right" />
        <el-input-number v-model="filterProjectId" :min="1" placeholder="项目ID" controls-position="right" />
        <el-button @click="loadData">查询</el-button>
        <el-button type="primary" @click="showDialog = true">新增日志</el-button>
      </div>
    </div>

    <div class="card">
      <el-table :data="worklogs" stripe>
        <el-table-column prop="engineerId" label="工程师ID" width="100" />
        <el-table-column prop="projectId" label="项目ID" width="80" />
        <el-table-column prop="workDate" label="日期" width="110" />
        <el-table-column prop="tasksPlan" label="计划任务" min-width="160" />
        <el-table-column prop="tasksCompleted" label="完成任务" min-width="160" />
        <el-table-column prop="documentsSubmitted" label="提交文档" width="140" />
        <el-table-column prop="issues" label="问题" width="140" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SUBMITTED' ? 'success' : 'info'" size="small">
              {{ row.status === 'SUBMITTED' ? '已提交' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'DRAFT'"
              type="primary"
              size="small"
              link
              @click="handleSubmitWorklog(row.id)"
            >
              提交
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="showDialog" title="新增工作日志" width="700px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="工程师ID" prop="engineerId">
              <el-input-number v-model="form.engineerId" :min="1" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="项目ID" prop="projectId">
              <el-input-number v-model="form.projectId" :min="1" controls-position="right" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="工作日期" prop="workDate">
          <el-date-picker v-model="form.workDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="计划任务" prop="tasksPlan">
          <el-input v-model="form.tasksPlan" type="textarea" :rows="3" maxlength="500" />
        </el-form-item>
        <el-form-item label="完成任务">
          <el-input v-model="form.tasksCompleted" type="textarea" :rows="3" maxlength="500" />
        </el-form-item>
        <el-form-item label="提交文档">
          <el-input v-model="form.documentsSubmitted" type="textarea" :rows="2" maxlength="500" />
        </el-form-item>
        <el-form-item label="问题记录">
          <el-input v-model="form.issues" type="textarea" :rows="2" maxlength="500" />
        </el-form-item>
        <el-form-item label="次日计划">
          <el-input v-model="form.nextDayPlan" type="textarea" :rows="2" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleCreate">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import { createWorklog, listWorklogs, submitWorklog } from '@/api/worklog'
import type { EngineerWorklog } from '@/types'
import { required } from '@/utils/validators'

const worklogs = ref<EngineerWorklog[]>([])
const showDialog = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const filterEngineerId = ref<number | undefined>(undefined)
const filterProjectId = ref<number | undefined>(undefined)

const form = ref({
  engineerId: undefined as number | undefined,
  projectId: undefined as number | undefined,
  workDate: '',
  tasksPlan: '',
  tasksCompleted: '',
  documentsSubmitted: '',
  issues: '',
  nextDayPlan: '',
})

const rules = {
  engineerId: [required('工程师ID不能为空')],
  projectId: [required('项目ID不能为空')],
  workDate: [required('工作日期不能为空')],
  tasksPlan: [required('计划任务不能为空')],
}

async function loadData() {
  const params: { engineerId?: number; projectId?: number } = {}
  if (filterEngineerId.value) params.engineerId = filterEngineerId.value
  if (filterProjectId.value) params.projectId = filterProjectId.value
  const res = await listWorklogs(params)
  worklogs.value = res.data.data
}

async function handleCreate() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createWorklog(form.value as unknown as Record<string, unknown>)
    ElMessage.success('工作日志创建成功')
    showDialog.value = false
    await loadData()
  } finally {
    submitting.value = false
  }
}

async function handleSubmitWorklog(id: number) {
  await submitWorklog(id)
  ElMessage.success('工作日志已提交')
  await loadData()
}

onMounted(loadData)
</script>
