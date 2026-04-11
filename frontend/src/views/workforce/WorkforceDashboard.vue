<template>
  <div>
    <div class="page-header">
      <h2>人力看板</h2>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- Tab 1: 人力全景看板 -->
      <el-tab-pane label="人力全景看板" name="panorama">
        <div v-loading="dashboardLoading">
          <el-row :gutter="16" class="summary-cards">
            <el-col :span="4">
              <div class="summary-card">
                <div class="card-value">{{ dashboard.totalEngineers }}</div>
                <div class="card-label">总工程师数</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="summary-card">
                <div class="card-value">
                  {{ dashboard.onProjectCount }}
                  <el-tag size="small" type="primary" style="margin-left:4px">在项目</el-tag>
                </div>
                <div class="card-label">在项目人数</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="summary-card">
                <div class="card-value">{{ dashboard.idleCount }}</div>
                <div class="card-label">空闲人数</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="summary-card">
                <div class="card-value">{{ dashboard.trainingCount }}</div>
                <div class="card-label">培训中</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="summary-card">
                <div class="card-value">{{ dashboard.leaveCount }}</div>
                <div class="card-label">请假人数</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="summary-card">
                <div class="card-value highlight">{{ dashboard.avgCompositeScore ?? '-' }}</div>
                <div class="card-label">平均综合评分</div>
              </div>
            </el-col>
          </el-row>

          <el-row :gutter="16" style="margin-top:16px">
            <el-col :span="12">
              <div class="card">
                <div class="card-title">利用率</div>
                <el-progress
                  :percentage="Number(dashboard.utilizationRate ?? 0)"
                  :stroke-width="20"
                  :text-inside="true"
                  status="success"
                />
              </div>
            </el-col>
            <el-col :span="12">
              <div class="card">
                <div class="card-title">空闲率</div>
                <el-progress
                  :percentage="Number(dashboard.idleRate ?? 0)"
                  :stroke-width="20"
                  :text-inside="true"
                  status="warning"
                />
              </div>
            </el-col>
          </el-row>

          <div class="card" style="margin-top:16px">
            <div class="card-title">工程师列表</div>
            <el-table
              :data="dashboard.engineers || []"
              stripe
              highlight-current-row
              @row-click="handleEngineerRowClick"
              style="cursor:pointer"
            >
              <el-table-column prop="name" label="姓名" width="100" />
              <el-table-column label="级别" width="110">
                <template #default="{ row }">
                  <el-tag size="small" :type="levelTagType(row.level)">
                    {{ EngineerLevelLabels[row.level] || row.level }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="statusTagType(row.currentStatus)" size="small">
                    {{ EngineerStatusLabels[row.currentStatus] || row.currentStatus }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="currentProjectName" label="当前项目" min-width="140" />
              <el-table-column prop="expectedRelease" label="预计释放" width="120" />
              <el-table-column prop="compositeScore" label="综合评分" width="100" />
              <el-table-column prop="monthlyProjectCount" label="月项目数" width="100" />
            </el-table>
          </div>
        </div>
      </el-tab-pane>

      <!-- Tab 2: 个人综合评价 -->
      <el-tab-pane label="个人综合评价" name="evaluation">
        <div class="card">
          <el-row :gutter="16" align="middle">
            <el-col :span="8">
              <el-select v-model="selectedEngineerId" placeholder="选择工程师" filterable style="width:100%">
                <el-option
                  v-for="e in engineerList"
                  :key="e.id"
                  :label="`${e.name} (${e.engineerCode})`"
                  :value="e.id"
                />
              </el-select>
            </el-col>
            <el-col :span="4">
              <el-button
                type="primary"
                :loading="scoreLoading"
                :disabled="!selectedEngineerId"
                @click="calculateScore"
              >
                计算综合评分
              </el-button>
            </el-col>
          </el-row>
        </div>

        <div v-if="compositeScore" class="card" style="margin-top:16px">
          <el-row :gutter="24">
            <el-col :span="8">
              <div class="score-display">
                <div class="big-score">{{ compositeScore.compositeScore }}</div>
                <el-rate
                  :model-value="starRating"
                  disabled
                  show-score
                  :score-template="`${compositeScore.compositeScore}分`"
                  style="margin-top:8px"
                />
                <div class="rating-text">{{ compositeScore.rating }}</div>
              </div>
            </el-col>
            <el-col :span="16">
              <div class="card-title">六维评分</div>
              <div v-for="dim in compositeScore.dimensions" :key="dim.dimensionName" class="dimension-bar">
                <div class="dimension-label">
                  <span>{{ dim.dimensionName }}</span>
                  <span class="dim-weight">权重: {{ dim.weight }}%</span>
                </div>
                <el-progress
                  :percentage="Number(dim.score)"
                  :stroke-width="16"
                  :text-inside="true"
                  :color="dimensionColor(Number(dim.score))"
                />
                <div v-if="dim.source" class="dim-source">{{ dim.source }}</div>
              </div>
            </el-col>
          </el-row>

          <div v-if="compositeScore.growthSuggestions?.length" style="margin-top:20px">
            <div class="card-title">成长建议</div>
            <el-alert
              v-for="(s, i) in compositeScore.growthSuggestions"
              :key="i"
              :title="s"
              type="info"
              :closable="false"
              show-icon
              style="margin-bottom:8px"
            />
          </div>
        </div>
      </el-tab-pane>

      <!-- Tab 3: 月度人力报表 -->
      <el-tab-pane label="月度人力报表" name="monthly">
        <div class="card">
          <el-button type="primary" :loading="reportLoading" @click="generateReport">
            生成月度报表
          </el-button>
        </div>

        <div v-if="monthlyReport" v-loading="reportLoading">
          <div class="card" style="margin-top:16px">
            <div class="card-title">排名</div>
            <el-table :data="monthlyReport.rankings || []" stripe>
              <el-table-column prop="rank" label="排名" width="70" />
              <el-table-column prop="name" label="姓名" width="100" />
              <el-table-column prop="level" label="级别" width="110">
                <template #default="{ row }">
                  {{ EngineerLevelLabels[row.level] || row.level }}
                </template>
              </el-table-column>
              <el-table-column prop="monthlyProjectCount" label="月项目数" width="100" />
              <el-table-column prop="compositeScore" label="综合评分" width="100" />
              <el-table-column prop="monthlyIdleRate" label="月空闲率" width="100">
                <template #default="{ row }">
                  {{ row.monthlyIdleRate }}%
                </template>
              </el-table-column>
              <el-table-column prop="trend" label="趋势" width="70">
                <template #default="{ row }">
                  <span :class="trendClass(row.trend)">{{ row.trend }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="card" style="margin-top:16px">
            <div class="card-title">空闲率分布</div>
            <el-row :gutter="12" v-if="monthlyReport.idleDistribution">
              <el-col :span="4" v-for="bucket in idleBuckets" :key="bucket.label">
                <div class="bucket-card" :style="{ borderColor: bucket.color }">
                  <div class="bucket-value">{{ bucket.value }}</div>
                  <div class="bucket-label">{{ bucket.label }}</div>
                </div>
              </el-col>
            </el-row>
          </div>

          <div class="card" style="margin-top:16px" v-if="monthlyReport.talentOverview">
            <div class="card-title">人才总览</div>
            <el-row :gutter="16">
              <el-col :span="8">
                <div class="talent-card talent-promotion">
                  <div class="talent-title">晋升候选人</div>
                  <el-tag
                    v-for="name in monthlyReport.talentOverview.promotionCandidates"
                    :key="name"
                    type="success"
                    style="margin: 2px"
                  >{{ name }}</el-tag>
                  <span v-if="!monthlyReport.talentOverview.promotionCandidates?.length" class="no-data">暂无</span>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="talent-card talent-training">
                  <div class="talent-title">重点培养</div>
                  <el-tag
                    v-for="name in monthlyReport.talentOverview.keyTrainingTargets"
                    :key="name"
                    style="margin: 2px"
                  >{{ name }}</el-tag>
                  <span v-if="!monthlyReport.talentOverview.keyTrainingTargets?.length" class="no-data">暂无</span>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="talent-card talent-attention">
                  <div class="talent-title">需要关注</div>
                  <el-tag
                    v-for="name in monthlyReport.talentOverview.needAttention"
                    :key="name"
                    type="warning"
                    style="margin: 2px"
                  >{{ name }}</el-tag>
                  <span v-if="!monthlyReport.talentOverview.needAttention?.length" class="no-data">暂无</span>
                </div>
              </el-col>
            </el-row>

            <div v-if="monthlyReport.talentOverview.skillGaps?.length" style="margin-top:12px">
              <div class="card-title">技能缺口</div>
              <el-tag
                v-for="gap in monthlyReport.talentOverview.skillGaps"
                :key="gap"
                type="danger"
                style="margin: 2px"
              >{{ gap }}</el-tag>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- Composite Score Dialog -->
    <el-dialog v-model="scoreDialogVisible" title="综合评分详情" width="600px" destroy-on-close>
      <div v-if="dialogScore" v-loading="dialogScoreLoading">
        <div class="score-display" style="text-align:center;margin-bottom:20px">
          <div class="big-score">{{ dialogScore.compositeScore }}</div>
          <div class="rating-text">{{ dialogScore.rating }}</div>
        </div>
        <div v-for="dim in dialogScore.dimensions" :key="dim.dimensionName" class="dimension-bar">
          <div class="dimension-label">
            <span>{{ dim.dimensionName }}</span>
            <span class="dim-weight">权重: {{ dim.weight }}%</span>
          </div>
          <el-progress
            :percentage="Number(dim.score)"
            :stroke-width="16"
            :text-inside="true"
            :color="dimensionColor(Number(dim.score))"
          />
          <div v-if="dim.source" class="dim-source">{{ dim.source }}</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getWorkforceDashboard,
  getCompositeScore,
  getMonthlyReport,
  listEngineers,
} from '@/api/workforce'
import { EngineerLevelLabels, EngineerStatusLabels } from '@/types'
import type { Engineer } from '@/types'

const activeTab = ref('panorama')

const dashboard = ref<any>({})
const dashboardLoading = ref(false)

const engineerList = ref<Engineer[]>([])
const selectedEngineerId = ref<number | null>(null)
const compositeScore = ref<any>(null)
const scoreLoading = ref(false)

const monthlyReport = ref<any>(null)
const reportLoading = ref(false)

const scoreDialogVisible = ref(false)
const dialogScore = ref<any>(null)
const dialogScoreLoading = ref(false)

const starRating = computed(() => {
  if (!compositeScore.value) return 0
  return Math.min(5, Math.max(0, Math.round(compositeScore.value.compositeScore / 20)))
})

function statusTagType(status: string) {
  const map: Record<string, string> = {
    IDLE: 'success',
    ON_PROJECT: '',
    TRAINING: 'warning',
    LEAVE: 'info',
  }
  return map[status] || 'info'
}

function levelTagType(level: string) {
  const map: Record<string, string> = {
    INTERN: 'info',
    JUNIOR: '',
    MIDDLE: 'success',
    SENIOR: 'warning',
    PM: 'danger',
    SENIOR_PM: 'danger',
    EXPERT_PM: 'danger',
  }
  return map[level] || ''
}

function dimensionColor(score: number) {
  if (score >= 80) return '#67c23a'
  if (score >= 60) return '#e6a23c'
  return '#f56c6c'
}

function trendClass(trend: string) {
  if (trend === '↑') return 'trend-up'
  if (trend === '↓') return 'trend-down'
  return 'trend-flat'
}

const idleBuckets = computed(() => {
  const d = monthlyReport.value?.idleDistribution
  if (!d) return []
  return [
    { label: '<10%', value: d.under10, color: '#67c23a' },
    { label: '10-20%', value: d.range10to20, color: '#409eff' },
    { label: '20-30%', value: d.range20to30, color: '#e6a23c' },
    { label: '30-50%', value: d.range30to50, color: '#f56c6c' },
    { label: '>50%', value: d.over50, color: '#909399' },
  ]
})

async function loadDashboard() {
  dashboardLoading.value = true
  try {
    const res = await getWorkforceDashboard()
    dashboard.value = res.data.data
  } catch {
    ElMessage.error('加载看板数据失败')
  } finally {
    dashboardLoading.value = false
  }
}

async function loadEngineers() {
  try {
    const res = await listEngineers()
    engineerList.value = res.data.data
  } catch {
    ElMessage.error('加载工程师列表失败')
  }
}

async function calculateScore() {
  if (!selectedEngineerId.value) return
  scoreLoading.value = true
  try {
    const res = await getCompositeScore(selectedEngineerId.value)
    compositeScore.value = res.data.data
  } catch {
    ElMessage.error('计算评分失败')
  } finally {
    scoreLoading.value = false
  }
}

async function generateReport() {
  reportLoading.value = true
  try {
    const res = await getMonthlyReport()
    monthlyReport.value = res.data.data
  } catch {
    ElMessage.error('生成报表失败')
  } finally {
    reportLoading.value = false
  }
}

async function handleEngineerRowClick(row: any) {
  scoreDialogVisible.value = true
  dialogScoreLoading.value = true
  try {
    const res = await getCompositeScore(row.engineerId)
    dialogScore.value = res.data.data
  } catch {
    ElMessage.error('获取评分详情失败')
  } finally {
    dialogScoreLoading.value = false
  }
}

onMounted(() => {
  loadDashboard()
  loadEngineers()
})
</script>

<style scoped>
.summary-cards {
  margin-bottom: 8px;
}

.summary-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.card-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.card-value.highlight {
  color: #409eff;
}

.card-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.score-display {
  text-align: center;
}

.big-score {
  font-size: 56px;
  font-weight: 700;
  color: #409eff;
}

.rating-text {
  font-size: 16px;
  color: #606266;
  margin-top: 4px;
}

.dimension-bar {
  margin-bottom: 12px;
}

.dimension-label {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
  font-size: 13px;
  color: #606266;
}

.dim-weight {
  color: #909399;
}

.dim-source {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 2px;
}

.bucket-card {
  background: #fff;
  border: 2px solid #dcdfe6;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
}

.bucket-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}

.bucket-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.talent-card {
  border-radius: 8px;
  padding: 16px;
  min-height: 80px;
}

.talent-promotion {
  background: #f0f9eb;
}

.talent-training {
  background: #ecf5ff;
}

.talent-attention {
  background: #fdf6ec;
}

.talent-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #303133;
}

.no-data {
  color: #c0c4cc;
  font-size: 13px;
}

.trend-up {
  color: #67c23a;
  font-weight: bold;
}

.trend-down {
  color: #f56c6c;
  font-weight: bold;
}

.trend-flat {
  color: #909399;
}
</style>
