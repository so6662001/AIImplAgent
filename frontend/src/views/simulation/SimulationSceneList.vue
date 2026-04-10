<template>
  <div>
    <div class="page-header">
      <h2>模拟演练工作台</h2>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- Tab 1: 模拟数据生成 -->
      <el-tab-pane label="模拟数据生成" name="mockData">
        <div class="card" style="margin-bottom: 20px;">
          <el-form inline>
            <el-form-item label="项目ID">
              <el-input-number v-model="mockProjectId" :min="1" controls-position="right" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="generatingMock" @click="handleGenerateMock">生成模拟数据</el-button>
            </el-form-item>
          </el-form>
        </div>

        <template v-if="mockData">
          <div class="card" style="margin-bottom: 20px;">
            <el-row :gutter="20">
              <el-col :span="6">
                <el-statistic title="产品数量" :value="mockData.summary.totalProducts" />
              </el-col>
              <el-col :span="6">
                <el-statistic title="客户数量" :value="mockData.summary.totalCustomers" />
              </el-col>
              <el-col :span="6">
                <el-statistic title="供应商数量" :value="mockData.summary.totalSuppliers" />
              </el-col>
              <el-col :span="6">
                <el-statistic title="库存总价值" :value="mockData.summary.totalInventoryValue" prefix="¥" />
              </el-col>
            </el-row>
          </div>

          <div class="card" style="margin-bottom: 20px;">
            <h4 style="margin: 0 0 12px;">产品数据</h4>
            <el-table :data="mockData.products" stripe size="small" max-height="300">
              <el-table-column prop="name" label="品名" min-width="120" />
              <el-table-column prop="spec" label="规格" min-width="120" />
              <el-table-column prop="material" label="材质" width="100" />
              <el-table-column prop="steelMill" label="钢厂" width="100" />
              <el-table-column prop="unit" label="单位" width="60" />
              <el-table-column prop="category" label="类别" width="80" />
            </el-table>
          </div>

          <div class="card" style="margin-bottom: 20px;">
            <h4 style="margin: 0 0 12px;">客户数据</h4>
            <el-table :data="mockData.customers" stripe size="small" max-height="250">
              <el-table-column prop="code" label="编码" width="80" />
              <el-table-column prop="name" label="名称" min-width="200" />
              <el-table-column prop="type" label="类型" width="100" />
              <el-table-column prop="contact" label="联系人" width="100" />
            </el-table>
          </div>

          <div class="card" style="margin-bottom: 20px;">
            <h4 style="margin: 0 0 12px;">供应商数据</h4>
            <el-table :data="mockData.suppliers" stripe size="small" max-height="250">
              <el-table-column prop="code" label="编码" width="80" />
              <el-table-column prop="name" label="名称" min-width="200" />
              <el-table-column prop="type" label="类型" width="100" />
            </el-table>
          </div>

          <div class="card" style="margin-bottom: 20px;">
            <h4 style="margin: 0 0 12px;">库存数据</h4>
            <el-table :data="mockData.inventory" stripe size="small" max-height="300">
              <el-table-column prop="productName" label="品名" min-width="120" />
              <el-table-column prop="spec" label="规格" min-width="120" />
              <el-table-column prop="warehouse" label="仓库" width="110" />
              <el-table-column prop="qty" label="数量" width="80" align="right" />
              <el-table-column prop="weight" label="重量" width="100" align="right" />
              <el-table-column prop="unitPrice" label="单价" width="100" align="right" />
              <el-table-column prop="amount" label="金额" width="120" align="right" />
            </el-table>
          </div>

          <div class="card">
            <h4 style="margin: 0 0 12px;">模拟交易</h4>
            <el-table :data="mockData.transactions" stripe size="small" max-height="300">
              <el-table-column label="类型" width="90">
                <template #default="{ row }">
                  <el-tag :type="row.type === 'PURCHASE' ? '' : 'success'" size="small">
                    {{ row.type === 'PURCHASE' ? '采购' : '销售' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="counterpartyName" label="往来单位" min-width="180" />
              <el-table-column prop="productName" label="品名" min-width="120" />
              <el-table-column prop="qty" label="数量" width="80" align="right" />
              <el-table-column prop="amount" label="金额" width="120" align="right" />
              <el-table-column prop="date" label="日期" width="110" />
            </el-table>
          </div>
        </template>
      </el-tab-pane>

      <!-- Tab 2: 演练场景 -->
      <el-tab-pane label="演练场景" name="scenes">
        <div class="card" style="margin-bottom: 20px;">
          <el-form inline>
            <el-form-item label="项目ID">
              <el-input-number v-model="filterProjectId" :min="1" controls-position="right" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadScenes">查询</el-button>
            </el-form-item>
            <el-form-item>
              <el-button type="success" :loading="autoGenerating" @click="handleAutoGenerate">自动生成场景</el-button>
            </el-form-item>
            <el-form-item>
              <el-button @click="showCreateDialog = true">新建场景</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="card">
          <el-table :data="sceneList" stripe>
            <el-table-column prop="sceneName" label="场景名称" min-width="160" />
            <el-table-column label="场景类型" width="140">
              <template #default="{ row }">
                <el-tag size="small" type="info">{{ row.sceneType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="simStatusType(statusName(row.status))" size="small">
                  {{ SimulationStatusLabels[statusName(row.status)] || statusName(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="难度" width="80">
              <template #default="{ row }">
                <el-tag v-if="row.difficulty" size="small" :type="difficultyType(row.difficulty)">{{ row.difficulty }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleViewSteps(row)">详情</el-button>
                <el-button
                  v-if="statusName(row.status) === 'PENDING'"
                  type="warning"
                  link
                  size="small"
                  @click="handleExecuteClick(row)"
                >执行</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- Steps dialog -->
        <el-dialog v-model="showStepsDialog" title="场景步骤详情" width="700px" destroy-on-close>
          <template v-if="currentViewScene">
            <p><strong>场景名称：</strong>{{ currentViewScene.sceneName }}</p>
            <p><strong>描述：</strong>{{ currentViewScene.description }}</p>
            <el-divider />
            <template v-if="parsedSteps.length > 0">
              <el-timeline>
                <el-timeline-item v-for="step in parsedSteps" :key="step.stepNumber" :timestamp="'步骤 ' + step.stepNumber" placement="top">
                  <el-card shadow="never">
                    <p><strong>操作：</strong>{{ step.instruction }}</p>
                    <p v-if="step.expectedAction"><strong>预期：</strong>{{ step.expectedAction }}</p>
                    <p v-if="step.checkpoint"><strong>检查点：</strong>{{ step.checkpoint }}</p>
                  </el-card>
                </el-timeline-item>
              </el-timeline>
            </template>
            <template v-else>
              <p>{{ currentViewScene.steps }}</p>
            </template>
            <el-divider />
            <p><strong>预期结果：</strong>{{ currentViewScene.expectedResult }}</p>
          </template>
        </el-dialog>
      </el-tab-pane>

      <!-- Tab 3: 场景执行 -->
      <el-tab-pane label="场景执行" name="execute">
        <div class="card" style="margin-bottom: 20px;">
          <el-form ref="execFormRef" :model="execForm" :rules="execRules" label-width="100px">
            <el-form-item label="选择场景" prop="sceneId">
              <el-select v-model="execForm.sceneId" placeholder="请选择待执行场景" style="width: 400px;" @change="onExecSceneChange">
                <el-option v-for="s in pendingScenes" :key="s.id" :label="s.sceneName" :value="s.id" />
              </el-select>
            </el-form-item>

            <template v-if="execSelectedScene">
              <el-divider content-position="left">场景步骤</el-divider>
              <div style="margin-bottom: 20px;">
                <template v-if="execParsedSteps.length > 0">
                  <el-steps :active="execParsedSteps.length" direction="vertical" finish-status="process">
                    <el-step v-for="step in execParsedSteps" :key="step.stepNumber" :title="'步骤 ' + step.stepNumber + ': ' + step.instruction">
                      <template #description>
                        <span v-if="step.checkpoint">检查点: {{ step.checkpoint }}</span>
                      </template>
                    </el-step>
                  </el-steps>
                </template>
                <template v-else>
                  <p style="color: #666;">{{ execSelectedScene.steps }}</p>
                </template>
              </div>
              <el-divider content-position="left">执行结果</el-divider>
            </template>

            <el-form-item label="实际结果" prop="actualResult">
              <el-input v-model="execForm.actualResult" type="textarea" :rows="3" />
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-select v-model="execForm.status" placeholder="请选择">
                <el-option label="通过" value="PASSED" />
                <el-option label="未通过" value="FAILED" />
              </el-select>
            </el-form-item>
            <el-form-item label="偏差" prop="deviation">
              <el-input v-model="execForm.deviation" type="textarea" :rows="2" />
            </el-form-item>
            <el-form-item label="执行人" prop="executedBy">
              <el-input v-model="execForm.executedBy" maxlength="30" style="width: 200px;" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="executing" :disabled="!execForm.sceneId" @click="handleExecute">提交执行结果</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>

      <!-- Tab 4: 演练评估报告 -->
      <el-tab-pane label="演练评估报告" name="report">
        <div class="card" style="margin-bottom: 20px;">
          <el-form inline>
            <el-form-item label="项目ID">
              <el-input-number v-model="reportProjectId" :min="1" controls-position="right" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="generatingReport" @click="handleGetReport">生成评估报告</el-button>
            </el-form-item>
          </el-form>
        </div>

        <template v-if="report">
          <div class="card" style="margin-bottom: 20px;">
            <el-row :gutter="20">
              <el-col :span="6">
                <el-statistic title="总场景数" :value="report.totalScenes" />
              </el-col>
              <el-col :span="6">
                <el-statistic title="已执行" :value="report.executedScenes" />
              </el-col>
              <el-col :span="6">
                <el-statistic title="通过" :value="report.passedScenes" />
              </el-col>
              <el-col :span="6">
                <el-statistic title="失败" :value="report.failedScenes" />
              </el-col>
            </el-row>
          </div>

          <div class="card" style="margin-bottom: 20px;">
            <el-row :gutter="40" align="middle">
              <el-col :span="10">
                <h4 style="margin: 0 0 8px;">整体通过率</h4>
                <el-progress
                  :percentage="Number(report.overallPassRate)"
                  :color="passRateColor(Number(report.overallPassRate))"
                  :stroke-width="20"
                  :text-inside="true"
                />
              </el-col>
              <el-col :span="6">
                <h4 style="margin: 0 0 8px;">综合评分</h4>
                <span style="font-size: 28px; font-weight: bold;">{{ report.overallScore }}</span>
              </el-col>
              <el-col :span="8">
                <h4 style="margin: 0 0 8px;">是否达标上线</h4>
                <el-result
                  :icon="report.readyForTraining ? 'success' : 'error'"
                  :title="report.readyForTraining ? '已达标' : '未达标'"
                  :sub-title="report.readyForTraining ? '可以进入培训上线阶段' : '通过率未达80%标准'"
                  style="padding: 0;"
                />
              </el-col>
            </el-row>
          </div>

          <div class="card" style="margin-bottom: 20px;">
            <h4 style="margin: 0 0 12px;">场景执行结果</h4>
            <el-table :data="report.sceneResults" stripe size="small">
              <el-table-column prop="sceneName" label="场景名称" min-width="160" />
              <el-table-column prop="sceneType" label="场景类型" width="140">
                <template #default="{ row }">
                  <el-tag size="small" type="info">{{ row.sceneType }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="90">
                <template #default="{ row }">
                  <el-tag :type="simStatusType(row.status)" size="small">
                    {{ SimulationStatusLabels[row.status] || row.status }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="score" label="评分" width="80" align="center" />
              <el-table-column prop="deviation" label="偏差" min-width="140" />
            </el-table>
          </div>

          <div class="card" v-if="report.recommendations && report.recommendations.length">
            <h4 style="margin: 0 0 12px;">改进建议</h4>
            <el-alert
              v-for="(rec, idx) in report.recommendations"
              :key="idx"
              :title="rec"
              type="warning"
              :closable="false"
              show-icon
              style="margin-bottom: 8px;"
            />
          </div>
        </template>
      </el-tab-pane>
    </el-tabs>

    <!-- Create scene dialog (shared) -->
    <el-dialog v-model="showCreateDialog" title="新建模拟场景" width="640px" destroy-on-close>
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
        <el-form-item label="项目ID" prop="projectId">
          <el-input-number v-model="createForm.projectId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="场景名称" prop="sceneName">
          <el-input v-model="createForm.sceneName" maxlength="100" />
        </el-form-item>
        <el-form-item label="场景类型" prop="sceneType">
          <el-select v-model="createForm.sceneType" placeholder="请选择">
            <el-option label="采购入库" value="PURCHASE_INBOUND" />
            <el-option label="销售出库" value="SALES_OUTBOUND" />
            <el-option label="库存盘点" value="INVENTORY_CHECK" />
            <el-option label="调拨" value="TRANSFER" />
            <el-option label="财务结算" value="FINANCIAL_SETTLEMENT" />
            <el-option label="全流程" value="FULL_PROCESS" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="createForm.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="步骤" prop="steps">
          <el-input v-model="createForm.steps" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="预期结果" prop="expectedResult">
          <el-input v-model="createForm.expectedResult" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreate">确定</el-button>
      </template>
    </el-dialog>

    <!-- Execute scene dialog (from scene list) -->
    <el-dialog v-model="showExecuteDialog" title="执行模拟场景" width="560px" destroy-on-close>
      <el-form ref="dialogExecFormRef" :model="dialogExecForm" :rules="dialogExecRules" label-width="100px">
        <el-form-item label="实际结果" prop="actualResult">
          <el-input v-model="dialogExecForm.actualResult" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="dialogExecForm.status" placeholder="请选择">
            <el-option label="通过" value="PASSED" />
            <el-option label="未通过" value="FAILED" />
          </el-select>
        </el-form-item>
        <el-form-item label="偏差" prop="deviation">
          <el-input v-model="dialogExecForm.deviation" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="执行人" prop="executedBy">
          <el-input v-model="dialogExecForm.executedBy" maxlength="30" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showExecuteDialog = false">取消</el-button>
        <el-button type="primary" :loading="dialogExecuting" @click="handleDialogExecute">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import {
  createScene,
  listScenes,
  executeScene,
  generateMockData,
  autoGenerateScenes,
  getSimulationReport,
} from '@/api/simulation'
import { SimulationStatusLabels } from '@/types'
import { required, maxLen } from '@/utils/validators'

const activeTab = ref('mockData')

// ========== Tab 1: Mock Data ==========
const mockProjectId = ref(1)
const generatingMock = ref(false)
const mockData = ref<any>(null)

async function handleGenerateMock() {
  generatingMock.value = true
  try {
    const res = await generateMockData(mockProjectId.value)
    mockData.value = res.data.data
    ElMessage.success('模拟数据生成成功')
  } finally {
    generatingMock.value = false
  }
}

// ========== Tab 2: Scenes ==========
const filterProjectId = ref(1)
const sceneList = ref<any[]>([])
const autoGenerating = ref(false)
const showCreateDialog = ref(false)
const showStepsDialog = ref(false)
const showExecuteDialog = ref(false)
const creating = ref(false)
const dialogExecuting = ref(false)
const createFormRef = ref<FormInstance>()
const dialogExecFormRef = ref<FormInstance>()
const currentExecId = ref(0)
const currentViewScene = ref<any>(null)

function statusName(status: any): string {
  if (typeof status === 'string') return status
  if (status && typeof status === 'object' && status.name) return status.name
  return String(status || '')
}

function simStatusType(status: string) {
  const map: Record<string, string> = { PENDING: 'info', RUNNING: 'warning', PASSED: 'success', FAILED: 'danger' }
  return map[status] || 'info'
}

function difficultyType(d: string) {
  if (d === '基础') return 'success'
  if (d === '进阶') return 'warning'
  if (d === '综合') return 'danger'
  return 'info'
}

const parsedSteps = computed(() => {
  if (!currentViewScene.value) return []
  return tryParseSteps(currentViewScene.value.steps)
})

function tryParseSteps(steps: any): any[] {
  if (!steps) return []
  if (Array.isArray(steps)) return steps
  if (typeof steps === 'string') {
    try {
      const parsed = JSON.parse(steps)
      if (Array.isArray(parsed)) return parsed
    } catch {
      // not JSON
    }
  }
  return []
}

async function loadScenes() {
  const res = await listScenes(filterProjectId.value)
  sceneList.value = res.data.data
}

async function handleAutoGenerate() {
  autoGenerating.value = true
  try {
    await autoGenerateScenes(filterProjectId.value)
    ElMessage.success('场景自动生成成功')
    await loadScenes()
  } finally {
    autoGenerating.value = false
  }
}

function handleViewSteps(row: any) {
  currentViewScene.value = row
  showStepsDialog.value = true
}

function handleExecuteClick(row: any) {
  currentExecId.value = row.id
  dialogExecForm.value = { actualResult: '', status: '', deviation: '', executedBy: '' }
  showExecuteDialog.value = true
}

const initCreateForm = () => ({
  projectId: undefined as number | undefined,
  sceneName: '',
  sceneType: '',
  description: '',
  steps: '',
  expectedResult: '',
})

const createForm = ref(initCreateForm())

const createRules = {
  projectId: [required('项目ID不能为空')],
  sceneName: [required('场景名称不能为空'), maxLen(100)],
  sceneType: [required('场景类型不能为空')],
  steps: [required('步骤不能为空')],
}

async function handleCreate() {
  const valid = await createFormRef.value?.validate().catch(() => false)
  if (!valid) return
  creating.value = true
  try {
    await createScene(createForm.value as unknown as Record<string, unknown>)
    ElMessage.success('场景创建成功')
    showCreateDialog.value = false
    createForm.value = initCreateForm()
    await loadScenes()
  } finally {
    creating.value = false
  }
}

const dialogExecForm = ref({ actualResult: '', status: '', deviation: '', executedBy: '' })

const dialogExecRules = {
  actualResult: [required('实际结果不能为空')],
  status: [required('状态不能为空')],
  executedBy: [required('执行人不能为空')],
}

async function handleDialogExecute() {
  const valid = await dialogExecFormRef.value?.validate().catch(() => false)
  if (!valid) return
  dialogExecuting.value = true
  try {
    await executeScene(currentExecId.value, dialogExecForm.value as unknown as Record<string, unknown>)
    ElMessage.success('场景执行完成')
    showExecuteDialog.value = false
    await loadScenes()
  } finally {
    dialogExecuting.value = false
  }
}

// ========== Tab 3: Execute ==========
const execFormRef = ref<FormInstance>()
const executing = ref(false)

const pendingScenes = computed(() =>
  sceneList.value.filter((s) => statusName(s.status) === 'PENDING'),
)

const execSelectedScene = ref<any>(null)

const execParsedSteps = computed(() => {
  if (!execSelectedScene.value) return []
  return tryParseSteps(execSelectedScene.value.steps)
})

const execForm = ref({
  sceneId: undefined as number | undefined,
  actualResult: '',
  status: '',
  deviation: '',
  executedBy: '',
})

const execRules = {
  sceneId: [required('请选择场景')],
  actualResult: [required('实际结果不能为空')],
  status: [required('状态不能为空')],
  executedBy: [required('执行人不能为空')],
}

function onExecSceneChange(id: number) {
  execSelectedScene.value = sceneList.value.find((s) => s.id === id) || null
}

async function handleExecute() {
  const valid = await execFormRef.value?.validate().catch(() => false)
  if (!valid) return
  executing.value = true
  try {
    const { sceneId, ...payload } = execForm.value
    await executeScene(sceneId!, payload as unknown as Record<string, unknown>)
    ElMessage.success('场景执行完成')
    execForm.value = { sceneId: undefined, actualResult: '', status: '', deviation: '', executedBy: '' }
    execSelectedScene.value = null
    await loadScenes()
  } finally {
    executing.value = false
  }
}

// ========== Tab 4: Report ==========
const reportProjectId = ref(1)
const generatingReport = ref(false)
const report = ref<any>(null)

function passRateColor(rate: number) {
  if (rate >= 80) return '#67C23A'
  if (rate >= 60) return '#E6A23C'
  return '#F56C6C'
}

async function handleGetReport() {
  generatingReport.value = true
  try {
    const res = await getSimulationReport(reportProjectId.value)
    report.value = res.data.data
    ElMessage.success('评估报告生成成功')
  } finally {
    generatingReport.value = false
  }
}
</script>
