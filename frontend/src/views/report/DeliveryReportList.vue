<template>
  <div>
    <div class="page-header">
      <h2>交付报告</h2>
      <el-button type="primary" @click="showDialog = true">新建报告</el-button>
    </div>

    <div class="card" style="margin-bottom: 20px;">
      <el-form inline>
        <el-form-item label="项目ID">
          <el-input-number v-model="filterProjectId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="card">
      <el-table :data="list" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="projectId" label="项目ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="reportType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.reportType === 'FINAL' ? 'danger' : ''" size="small">
              {{ row.reportType === 'FINAL' ? '终验报告' : '里程碑' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="overallScore" label="综合评分" width="100" />
        <el-table-column prop="trainingPassRate" label="培训通过率%" width="120" />
        <el-table-column prop="customerSatisfactionScore" label="客户满意度" width="110" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="confirmedBy" label="确认人" width="100" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'SUBMITTED'"
              type="primary"
              link
              size="small"
              @click="handleConfirmClick(row.id)"
            >确认</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="showDialog" title="新建交付报告" width="640px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="130px">
        <el-form-item label="项目ID" prop="projectId">
          <el-input-number v-model="form.projectId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="100" />
        </el-form-item>
        <el-form-item label="报告类型" prop="reportType">
          <el-select v-model="form.reportType" placeholder="请选择">
            <el-option label="里程碑" value="MILESTONE" />
            <el-option label="终验报告" value="FINAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="培训通过率%" prop="trainingPassRate">
          <el-input-number v-model="form.trainingPassRate" :min="0" :max="100" :precision="2" controls-position="right" />
        </el-form-item>
        <el-form-item label="数据导入完成率%" prop="dataImportCompletionRate">
          <el-input-number v-model="form.dataImportCompletionRate" :min="0" :max="100" :precision="2" controls-position="right" />
        </el-form-item>
        <el-form-item label="问题总数" prop="totalIssues">
          <el-input-number v-model="form.totalIssues" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="已解决问题数" prop="resolvedIssues">
          <el-input-number v-model="form.resolvedIssues" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="客户满意度" prop="customerSatisfactionScore">
          <el-input-number v-model="form.customerSatisfactionScore" :min="0" :max="5" :precision="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="进度偏差%" prop="scheduleDeviationPercent">
          <el-input-number v-model="form.scheduleDeviationPercent" :precision="2" controls-position="right" />
        </el-form-item>
        <el-form-item label="文档完成率%" prop="documentCompletionRate">
          <el-input-number v-model="form.documentCompletionRate" :min="0" :max="100" :precision="2" controls-position="right" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showConfirmDialog" title="确认报告" width="400px" destroy-on-close>
      <el-form ref="confirmFormRef" :model="confirmForm" :rules="confirmRules" label-width="80px">
        <el-form-item label="确认人" prop="confirmedBy">
          <el-input v-model="confirmForm.confirmedBy" maxlength="30" placeholder="请输入确认人姓名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showConfirmDialog = false">取消</el-button>
        <el-button type="primary" :loading="confirming" @click="handleConfirm">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import { createDeliveryReport, listDeliveryReports, confirmDeliveryReport } from '@/api/report'
import type { DeliveryReport } from '@/types'
import { required, maxLen, percentRule, issuesRule } from '@/utils/validators'

const list = ref<DeliveryReport[]>([])
const showDialog = ref(false)
const showConfirmDialog = ref(false)
const submitting = ref(false)
const confirming = ref(false)
const formRef = ref<FormInstance>()
const confirmFormRef = ref<FormInstance>()
const filterProjectId = ref(1)
const confirmTargetId = ref(0)

const initForm = () => ({
  projectId: undefined as number | undefined,
  title: '',
  reportType: '' as string,
  trainingPassRate: 0,
  dataImportCompletionRate: 0,
  totalIssues: 0,
  resolvedIssues: 0,
  customerSatisfactionScore: 0,
  scheduleDeviationPercent: 0,
  documentCompletionRate: 0,
})

const form = ref(initForm())

const rules = reactive({
  projectId: [required('项目ID不能为空')],
  title: [required('标题不能为空'), maxLen(100)],
  reportType: [required('报告类型不能为空')],
  trainingPassRate: [percentRule],
  dataImportCompletionRate: [percentRule],
  documentCompletionRate: [percentRule],
  resolvedIssues: [issuesRule(form.value)],
})

const confirmForm = ref({ confirmedBy: '' })
const confirmRules = { confirmedBy: [required('确认人不能为空')] }

function statusTagType(status: string) {
  const map: Record<string, string> = { DRAFT: 'info', SUBMITTED: 'warning', CONFIRMED: 'success' }
  return map[status] || 'info'
}

function statusLabel(status: string) {
  const map: Record<string, string> = { DRAFT: '草稿', SUBMITTED: '已提交', CONFIRMED: '已确认' }
  return map[status] || status
}

async function loadData() {
  const res = await listDeliveryReports(filterProjectId.value)
  list.value = res.data.data
}

function handleConfirmClick(id: number) {
  confirmTargetId.value = id
  confirmForm.value = { confirmedBy: '' }
  showConfirmDialog.value = true
}

async function handleConfirm() {
  const valid = await confirmFormRef.value?.validate().catch(() => false)
  if (!valid) return

  confirming.value = true
  try {
    await confirmDeliveryReport(confirmTargetId.value, confirmForm.value.confirmedBy)
    ElMessage.success('报告已确认')
    showConfirmDialog.value = false
    await loadData()
  } finally {
    confirming.value = false
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createDeliveryReport(form.value as unknown as Record<string, unknown>)
    ElMessage.success('报告创建成功')
    showDialog.value = false
    form.value = initForm()
    await loadData()
  } finally {
    submitting.value = false
  }
}
</script>
