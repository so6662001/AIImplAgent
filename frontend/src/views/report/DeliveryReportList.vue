<template>
  <div>
    <div class="page-header">
      <h2>交付报告</h2>
      <el-button type="primary" @click="showDialog = true">新建报告</el-button>
    </div>

    <!-- Auto Generate Section -->
    <div class="card" style="margin-bottom: 20px;">
      <h3 style="margin: 0 0 16px 0; font-size: 16px;">自动生成报告</h3>
      <el-form inline>
        <el-form-item label="项目ID">
          <el-input-number v-model="autoProjectId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="autoGenerating"
            @click="handleAutoGenerate"
            style="font-weight: bold; padding: 12px 28px;"
          >一键生成交付报告</el-button>
        </el-form-item>
      </el-form>

      <!-- Generated Report Display -->
      <template v-if="generatedReport">
        <!-- Summary Card -->
        <div class="summary-card">
          <div class="summary-score">
            <span class="score-number">{{ generatedReport.overallScore }}</span>
            <span class="score-label">综合评分</span>
          </div>
          <div class="summary-info">
            <el-tag
              :type="statusBadgeType(generatedReport.overallStatus)"
              size="large"
              effect="dark"
              style="font-size: 14px; padding: 8px 16px;"
            >{{ generatedReport.overallStatus }}</el-tag>
            <div class="generated-at">
              生成时间：{{ generatedReport.generatedAt }}
            </div>
            <div v-if="generatedReport.projectCode" style="margin-top: 4px; color: #606266;">
              项目：{{ generatedReport.projectCode }}
              <span v-if="generatedReport.customerName"> | 客户：{{ generatedReport.customerName }}</span>
            </div>
          </div>
        </div>

        <!-- Key Metrics Row -->
        <div class="metrics-row" v-if="generatedReport.metrics">
          <div class="metric-card" v-if="generatedReport.metrics.trainingPassRate != null">
            <div class="metric-value">{{ generatedReport.metrics.trainingPassRate }}%</div>
            <div class="metric-label">培训通过率</div>
          </div>
          <div class="metric-card" v-if="generatedReport.metrics.dataImportCompletionRate != null">
            <div class="metric-value">{{ generatedReport.metrics.dataImportCompletionRate }}%</div>
            <div class="metric-label">数据导入率</div>
          </div>
          <div class="metric-card" v-if="generatedReport.metrics.ticketResolutionRate != null">
            <div class="metric-value">{{ generatedReport.metrics.ticketResolutionRate }}%</div>
            <div class="metric-label">工单解决率</div>
          </div>
          <div class="metric-card" v-if="generatedReport.metrics.documentCompletionRate != null">
            <div class="metric-value">{{ generatedReport.metrics.documentCompletionRate }}%</div>
            <div class="metric-label">文档完成率</div>
          </div>
          <div class="metric-card" v-if="generatedReport.metrics.simulationPassRate != null">
            <div class="metric-value">{{ generatedReport.metrics.simulationPassRate }}%</div>
            <div class="metric-label">模拟演练通过率</div>
          </div>
          <div class="metric-card" v-if="generatedReport.metrics.worklogSubmissionRate != null">
            <div class="metric-value">{{ generatedReport.metrics.worklogSubmissionRate }}%</div>
            <div class="metric-label">日志提交率</div>
          </div>
        </div>

        <!-- Sections as Collapse Panels -->
        <el-collapse v-model="activeSections" style="margin-top: 16px;">
          <el-collapse-item
            v-for="section in generatedReport.sections"
            :key="section.sectionNumber"
            :name="section.sectionNumber"
          >
            <template #title>
              <span style="font-weight: 600; font-size: 15px;">
                第{{ section.sectionNumber }}章 {{ section.sectionTitle }}
              </span>
              <el-tag
                v-for="(hl, idx) in (section.highlights || []).slice(0, 3)"
                :key="idx"
                size="small"
                type="info"
                style="margin-left: 8px;"
              >{{ hl }}</el-tag>
            </template>
            <pre class="section-content">{{ section.content }}</pre>
            <div v-if="section.dataPoints && Object.keys(section.dataPoints).length" class="data-points">
              <div class="data-point" v-for="(val, key) in section.dataPoints" :key="key">
                <span class="dp-key">{{ key }}:</span>
                <span class="dp-value">{{ formatDataPointValue(val) }}</span>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>

        <!-- Export Button Placeholder -->
        <div style="margin-top: 16px; text-align: right;">
          <el-button type="info" disabled>导出（即将上线）</el-button>
        </div>
      </template>
    </div>

    <!-- Existing list filter -->
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
        <el-table-column prop="title" label="标题" min-width="160">
          <template #default="{ row }">
            {{ row.title }}
            <el-tag v-if="row.autoGenerated" type="success" size="small" style="margin-left: 6px;">自动生成</el-tag>
          </template>
        </el-table-column>
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
import { createDeliveryReport, listDeliveryReports, confirmDeliveryReport, autoGenerateReport } from '@/api/report'
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

const autoProjectId = ref(1)
const autoGenerating = ref(false)
const generatedReport = ref<any>(null)
const activeSections = ref<number[]>([])

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

function statusBadgeType(status: string) {
  const map: Record<string, string> = { '优秀': 'success', '良好': '', '合格': 'warning', '待改进': 'danger' }
  return map[status] || 'info'
}

function formatDataPointValue(val: unknown): string {
  if (val === null || val === undefined) return '-'
  if (typeof val === 'object') return JSON.stringify(val)
  return String(val)
}

async function handleAutoGenerate() {
  autoGenerating.value = true
  try {
    const res = await autoGenerateReport(autoProjectId.value)
    generatedReport.value = res.data.data
    activeSections.value = generatedReport.value.sections?.map((s: any) => s.sectionNumber) || []
    ElMessage.success('交付报告已自动生成')
  } catch {
    generatedReport.value = null
  } finally {
    autoGenerating.value = false
  }
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

<style scoped>
.summary-card {
  display: flex;
  align-items: center;
  gap: 32px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e8f4fd 100%);
  border-radius: 8px;
  padding: 24px;
  margin-top: 16px;
}
.summary-score {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 120px;
}
.score-number {
  font-size: 48px;
  font-weight: 700;
  color: #409eff;
  line-height: 1;
}
.score-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}
.summary-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.generated-at {
  color: #909399;
  font-size: 13px;
}
.metrics-row {
  display: flex;
  gap: 12px;
  margin-top: 16px;
  flex-wrap: wrap;
}
.metric-card {
  flex: 1;
  min-width: 130px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 16px;
  text-align: center;
}
.metric-value {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}
.metric-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.section-content {
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 14px;
  line-height: 1.7;
  color: #303133;
  background: #fafafa;
  padding: 12px 16px;
  border-radius: 4px;
  margin: 0;
}
.data-points {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 24px;
  margin-top: 12px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 4px;
}
.data-point {
  font-size: 13px;
}
.dp-key {
  color: #909399;
  margin-right: 4px;
}
.dp-value {
  color: #303133;
  font-weight: 500;
}
</style>
