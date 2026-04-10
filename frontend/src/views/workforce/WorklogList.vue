<template>
  <div>
    <div class="page-header">
      <h2>工作日志</h2>
      <div style="display: flex; align-items: center; gap: 12px;">
        <el-input-number v-model="filterEngineerId" :min="1" placeholder="工程师ID" controls-position="right" />
        <el-input-number v-model="filterProjectId" :min="1" placeholder="项目ID" controls-position="right" />
        <el-button @click="loadData">查询</el-button>
        <el-button type="primary" @click="showDialog = true">新增日志</el-button>
        <el-button type="success" @click="showTaskDialog = true">生成今日任务</el-button>
        <el-button type="warning" @click="showReportDialog = true">生成日报草稿</el-button>
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

    <!-- 新增工作日志弹窗 -->
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

    <!-- 生成今日任务弹窗 -->
    <el-dialog v-model="showTaskDialog" title="生成今日任务" width="800px" destroy-on-close>
      <el-form :inline="true" :model="taskForm" style="margin-bottom: 16px;">
        <el-form-item label="工程师ID">
          <el-input-number v-model="taskForm.engineerId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="项目ID">
          <el-input-number v-model="taskForm.projectId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="taskForm.date" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="taskLoading" @click="handleGenerateTasks">生成</el-button>
        </el-form-item>
      </el-form>

      <template v-if="taskResult">
        <el-tag style="margin-bottom: 12px;">项目阶段：{{ taskResult.phase }}</el-tag>

        <template v-for="cat in taskCategories" :key="cat">
          <div v-if="getTasksByCategory(cat).length" style="margin-bottom: 16px;">
            <h4 style="margin-bottom: 8px;">
              {{ categoryLabel(cat) }}
            </h4>
            <div v-for="(task, idx) in getTasksByCategory(cat)" :key="idx" class="task-item">
              <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 4px;">
                <span style="font-weight: 600;">{{ task.title }}</span>
                <el-tag :type="task.priority === 'HIGH' ? 'danger' : task.priority === 'MEDIUM' ? 'warning' : 'info'" size="small">
                  {{ task.priority }}
                </el-tag>
                <span style="color: #999; font-size: 12px;">⏱ {{ task.estimatedTime }}</span>
              </div>
              <p style="color: #666; font-size: 13px; margin: 0;">{{ task.description }}</p>
            </div>
          </div>
        </template>

        <div v-if="taskResult.nextDayPrep && taskResult.nextDayPrep.length" style="margin-top: 16px;">
          <h4 style="margin-bottom: 8px;">次日准备</h4>
          <ul style="margin: 0; padding-left: 20px;">
            <li v-for="(item, idx) in taskResult.nextDayPrep" :key="idx" style="color: #666;">{{ item }}</li>
          </ul>
        </div>
      </template>
    </el-dialog>

    <!-- 生成日报草稿弹窗 -->
    <el-dialog v-model="showReportDialog" title="生成日报草稿" width="800px" destroy-on-close>
      <el-form :inline="true" :model="reportForm" style="margin-bottom: 16px;">
        <el-form-item label="工程师ID">
          <el-input-number v-model="reportForm.engineerId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="项目ID">
          <el-input-number v-model="reportForm.projectId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="reportForm.date" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="reportLoading" @click="handleGenerateReport">生成</el-button>
        </el-form-item>
      </el-form>

      <template v-if="reportResult">
        <div v-for="section in reportSections" :key="section.key" style="margin-bottom: 16px;">
          <h4 style="margin-bottom: 8px;">{{ section.label }}</h4>
          <el-input
            v-model="reportResult[section.key]"
            type="textarea"
            :rows="3"
          />
          <el-button size="small" style="margin-top: 4px;" @click="copyText(reportResult[section.key])">复制</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import { createWorklog, listWorklogs, submitWorklog, getDailyTasks, getReportDraft } from '@/api/worklog'
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

// Daily tasks
const showTaskDialog = ref(false)
const taskLoading = ref(false)
const taskResult = ref<any>(null)
const taskForm = reactive({
  engineerId: 1,
  projectId: 1,
  date: new Date().toISOString().slice(0, 10),
})

const taskCategories = ['PREP', 'EXECUTION', 'DOCUMENT', 'FOLLOWUP'] as const

function categoryLabel(cat: string) {
  const map: Record<string, string> = {
    PREP: '📋 准备工作',
    EXECUTION: '🔧 执行任务',
    DOCUMENT: '📄 文档工作',
    FOLLOWUP: '🔍 后续跟进',
  }
  return map[cat] || cat
}

function getTasksByCategory(cat: string) {
  if (!taskResult.value?.tasks) return []
  return taskResult.value.tasks.filter((t: any) => t.category === cat)
}

async function handleGenerateTasks() {
  taskLoading.value = true
  taskResult.value = null
  try {
    const res = await getDailyTasks({
      engineerId: taskForm.engineerId,
      projectId: taskForm.projectId,
      date: taskForm.date,
    })
    taskResult.value = res.data.data
  } finally {
    taskLoading.value = false
  }
}

// Report draft
const showReportDialog = ref(false)
const reportLoading = ref(false)
const reportResult = ref<any>(null)
const reportForm = reactive({
  engineerId: 1,
  projectId: 1,
  date: new Date().toISOString().slice(0, 10),
})

const reportSections = [
  { key: 'summary', label: '工作总结' },
  { key: 'trainingSection', label: '培训情况' },
  { key: 'examSection', label: '考核情况' },
  { key: 'issuesSection', label: '问题记录' },
  { key: 'nextDayPlan', label: '次日计划' },
]

async function handleGenerateReport() {
  reportLoading.value = true
  reportResult.value = null
  try {
    const res = await getReportDraft({
      engineerId: reportForm.engineerId,
      projectId: reportForm.projectId,
      date: reportForm.date,
    })
    reportResult.value = res.data.data
  } finally {
    reportLoading.value = false
  }
}

function copyText(text: string) {
  if (!text) return
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('已复制到剪贴板')
  }).catch(() => {
    ElMessage.warning('复制失败，请手动选择复制')
  })
}

import { onMounted } from 'vue'
onMounted(loadData)
</script>

<style scoped>
.task-item {
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 8px;
}
</style>
