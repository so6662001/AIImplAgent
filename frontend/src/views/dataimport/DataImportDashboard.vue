<template>
  <div>
    <div class="page-header">
      <h2>数据治理</h2>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- Tab 1: 导入总览 -->
      <el-tab-pane label="导入总览" name="overview">
        <div class="card" style="margin-bottom: 20px">
          <el-form inline>
            <el-form-item label="项目ID">
              <el-input-number v-model="overviewProjectId" :min="1" controls-position="right" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="overviewLoading" @click="loadOverview">查看状态</el-button>
            </el-form-item>
          </el-form>
        </div>

        <template v-if="overview">
          <div class="summary-cards">
            <el-card shadow="hover" class="summary-card">
              <div class="card-value">{{ overview.totalItems }}</div>
              <div class="card-label">总项数</div>
            </el-card>
            <el-card shadow="hover" class="summary-card">
              <div class="card-value">{{ overview.completedItems }}</div>
              <div class="card-label">已完成</div>
            </el-card>
            <el-card shadow="hover" class="summary-card" style="min-width: 280px">
              <div class="card-label" style="margin-bottom: 8px">完成率</div>
              <el-progress
                :percentage="Number(overview.completionRate)"
                :stroke-width="18"
                :text-inside="true"
                :status="Number(overview.completionRate) === 100 ? 'success' : ''"
              />
            </el-card>
            <el-card shadow="hover" class="summary-card">
              <el-tag :type="overallStatusType(overview.overallStatus)" size="large">
                {{ overview.overallStatus }}
              </el-tag>
              <div class="card-label" style="margin-top: 8px">总体状态</div>
            </el-card>
          </div>

          <div class="card">
            <el-table :data="overview.items" stripe :row-class-name="itemRowClass">
              <el-table-column prop="category" label="分类" width="80" />
              <el-table-column prop="itemCode" label="编码" width="80" />
              <el-table-column prop="itemName" label="项目名称" min-width="160" />
              <el-table-column prop="recordCount" label="记录数" width="100" />
              <el-table-column prop="batchNumber" label="批次" width="80" />
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === '已导入' ? 'success' : 'info'" size="small">
                    {{ row.status }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </template>
      </el-tab-pane>

      <!-- Tab 2: 导入编排 -->
      <el-tab-pane label="导入编排" name="orchestration">
        <div class="card" style="margin-bottom: 20px">
          <el-form inline>
            <el-form-item label="项目ID">
              <el-input-number v-model="orchProjectId" :min="1" controls-position="right" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleInit">初始化</el-button>
              <el-button @click="loadProgress">刷新状态</el-button>
            </el-form-item>
          </el-form>
        </div>

        <template v-if="progress">
          <div class="card" style="margin-bottom: 20px">
            <el-steps :active="progress.currentBatch - 1" finish-status="success" align-center>
              <el-step
                v-for="batch in progress.batches"
                :key="batch.batchNumber"
                :title="batch.batchName"
                :description="`${batch.itemCount} 条记录`"
              >
                <template #icon>
                  <el-tag
                    :type="batchStatusType(batch.status)"
                    size="small"
                    round
                    effect="dark"
                  >
                    {{ batch.batchNumber }}
                  </el-tag>
                </template>
              </el-step>
            </el-steps>

            <div style="margin-top: 16px; display: flex; gap: 12px; justify-content: center; flex-wrap: wrap">
              <div v-for="batch in progress.batches" :key="batch.batchNumber">
                <el-tag
                  :type="batchStatusType(batch.status)"
                  size="small"
                >
                  {{ batch.batchName }}: {{ batchStatusLabel(batch.status) }}
                </el-tag>
              </div>
            </div>
          </div>

          <div class="card" style="text-align: center">
            <el-button
              type="success"
              :loading="completingBatch"
              @click="handleCompleteBatch"
            >
              完成当前批次 (批次{{ progress.currentBatch }})
            </el-button>
            <el-button
              type="primary"
              :loading="advancing"
              @click="handleAdvanceBatch"
            >
              推进下一批次
            </el-button>
          </div>

          <el-alert
            v-if="progress.lastError"
            :title="progress.lastError"
            type="error"
            show-icon
            :closable="false"
            style="margin-top: 16px"
          />
        </template>
      </el-tab-pane>

      <!-- Tab 3: 勾稽校验 -->
      <el-tab-pane label="勾稽校验" name="reconciliation">
        <div class="card" style="margin-bottom: 20px">
          <el-form inline>
            <el-form-item label="项目ID">
              <el-input-number v-model="reconProjectId" :min="1" controls-position="right" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="reconLoading" @click="loadReconciliation">执行校验</el-button>
            </el-form-item>
          </el-form>
        </div>

        <template v-if="reconciliation">
          <el-alert
            v-if="reconciliation.allPassed"
            title="全部校验通过"
            type="success"
            show-icon
            :closable="false"
            style="margin-bottom: 16px"
          />
          <el-alert
            v-else
            title="存在不平衡项"
            type="error"
            show-icon
            :closable="false"
            style="margin-bottom: 16px"
          />

          <div class="summary-cards" style="margin-bottom: 20px">
            <el-card shadow="hover" class="summary-card">
              <div class="card-value" style="color: #67c23a">{{ reconciliation.passCount }}</div>
              <div class="card-label">通过</div>
            </el-card>
            <el-card shadow="hover" class="summary-card">
              <div class="card-value" style="color: #e6a23c">{{ reconciliation.warnCount }}</div>
              <div class="card-label">警告</div>
            </el-card>
            <el-card shadow="hover" class="summary-card">
              <div class="card-value" style="color: #f56c6c">{{ reconciliation.failCount }}</div>
              <div class="card-label">失败</div>
            </el-card>
            <el-card shadow="hover" class="summary-card">
              <el-tag :type="reconciliation.allPassed ? 'success' : 'danger'" size="large">
                {{ reconciliation.allPassed ? '全部通过' : '存在异常' }}
              </el-tag>
              <div class="card-label" style="margin-top: 8px">校验结果</div>
            </el-card>
          </div>

          <div class="card">
            <el-table :data="reconciliation.items" stripe>
              <el-table-column prop="checkName" label="校验项" min-width="180" />
              <el-table-column prop="leftLabel" label="左项" min-width="140" />
              <el-table-column prop="leftValue" label="左值" width="120" align="right" />
              <el-table-column prop="rightLabel" label="右项" min-width="140" />
              <el-table-column prop="rightValue" label="右值" width="120" align="right" />
              <el-table-column prop="difference" label="差异" width="120" align="right" />
              <el-table-column label="结果" width="90">
                <template #default="{ row }">
                  <el-tag :type="reconResultType(row.result)" size="small">
                    {{ row.result }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="remark" label="备注" min-width="160" />
            </el-table>
          </div>
        </template>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  initImportProgress,
  getImportProgress,
  completeBatch,
  advanceBatch,
  getReconciliation,
  getImportOverview,
} from '@/api/dataimport'

const activeTab = ref('overview')

// Tab 1 state
const overviewProjectId = ref(1)
const overviewLoading = ref(false)
const overview = ref<any>(null)

// Tab 2 state
const orchProjectId = ref(1)
const progress = ref<any>(null)
const completingBatch = ref(false)
const advancing = ref(false)

// Tab 3 state
const reconProjectId = ref(1)
const reconLoading = ref(false)
const reconciliation = ref<any>(null)

function overallStatusType(status: string) {
  if (status === '已完成') return 'success'
  if (status === '进行中') return 'warning'
  return 'info'
}

function itemRowClass({ row }: { row: any }) {
  return row.status === '已导入' ? 'imported-row' : ''
}

function batchStatusType(status: string) {
  const map: Record<string, string> = {
    PENDING: 'info',
    IN_PROGRESS: '',
    COMPLETED: 'success',
    FAILED: 'danger',
  }
  return map[status] || 'info'
}

function batchStatusLabel(status: string) {
  const map: Record<string, string> = {
    PENDING: '待处理',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    FAILED: '失败',
  }
  return map[status] || status
}

function reconResultType(result: string) {
  const map: Record<string, string> = {
    PASS: 'success',
    WARNING: 'warning',
    FAIL: 'danger',
  }
  return map[result] || 'info'
}

async function loadOverview() {
  overviewLoading.value = true
  try {
    const res = await getImportOverview(overviewProjectId.value)
    overview.value = res.data.data
  } finally {
    overviewLoading.value = false
  }
}

async function handleInit() {
  try {
    await initImportProgress(orchProjectId.value)
    ElMessage.success('初始化成功')
    await loadProgress()
  } catch {
    // error handled by request interceptor
  }
}

async function loadProgress() {
  try {
    const res = await getImportProgress(orchProjectId.value)
    progress.value = res.data.data
  } catch {
    // error handled by request interceptor
  }
}

async function handleCompleteBatch() {
  if (!progress.value) return
  completingBatch.value = true
  try {
    const res = await completeBatch(orchProjectId.value, progress.value.currentBatch)
    progress.value = res.data.data
    const batchStatus = progress.value.batches.find(
      (b: any) => b.batchNumber === progress.value.currentBatch
    )?.status
    if (batchStatus === 'FAILED') {
      ElMessage.error(progress.value.lastError || '批次完成失败')
    } else {
      ElMessage.success('批次完成')
    }
  } finally {
    completingBatch.value = false
  }
}

async function handleAdvanceBatch() {
  if (!progress.value) return
  advancing.value = true
  try {
    const res = await advanceBatch(orchProjectId.value)
    progress.value = res.data.data
    ElMessage.success('已推进到批次 ' + progress.value.currentBatch)
  } finally {
    advancing.value = false
  }
}

async function loadReconciliation() {
  reconLoading.value = true
  try {
    const res = await getReconciliation(reconProjectId.value)
    reconciliation.value = res.data.data
  } finally {
    reconLoading.value = false
  }
}
</script>

<style scoped>
.summary-cards {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.summary-card {
  flex: 1;
  min-width: 160px;
  text-align: center;
}

.card-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--el-color-primary);
}

.card-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

:deep(.imported-row) {
  background-color: #f0f9eb !important;
}
</style>
