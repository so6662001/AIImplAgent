<template>
  <div class="dispatch-page">
    <div class="page-header">
      <h2>项目调度中心</h2>
      <p class="page-desc">智能推荐最优项目经理，生成排期建议</p>
    </div>

    <el-row :gutter="24">
      <!-- Section 1: PM推荐 -->
      <el-col :span="24">
        <el-card class="section-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <div class="card-title">
                <el-icon :size="20"><User /></el-icon>
                <span>PM推荐</span>
              </div>
            </div>
          </template>

          <!-- Mode Toggle -->
          <div class="mode-toggle">
            <el-radio-group v-model="inputMode" size="large">
              <el-radio-button value="project">选择项目</el-radio-button>
              <el-radio-button value="manual">手动输入</el-radio-button>
            </el-radio-group>
          </div>

          <!-- Project Mode -->
          <el-form
            v-if="inputMode === 'project'"
            :model="projectForm"
            label-width="100px"
            class="dispatch-form"
          >
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="选择项目">
                  <el-select
                    v-model="projectForm.projectId"
                    placeholder="请选择项目"
                    filterable
                    clearable
                    style="width: 100%"
                  >
                    <el-option
                      v-for="p in projectList"
                      :key="p.id"
                      :label="`${p.projectCode} - ${p.customerName}`"
                      :value="p.id"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item>
                  <el-button
                    type="primary"
                    size="large"
                    :loading="loading"
                    :disabled="!projectForm.projectId"
                    @click="handleRecommend"
                  >
                    <el-icon><MagicStick /></el-icon>
                    获取推荐
                  </el-button>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>

          <!-- Manual Mode -->
          <el-form
            v-else
            :model="manualForm"
            label-width="100px"
            class="dispatch-form"
          >
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="客户名称">
                  <el-input v-model="manualForm.customerName" placeholder="请输入客户名称" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="行业类型">
                  <el-select v-model="manualForm.industryType" placeholder="请选择" style="width: 100%">
                    <el-option
                      v-for="(label, key) in IndustryTypeLabels"
                      :key="key"
                      :label="label"
                      :value="key"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="企业规模">
                  <el-select v-model="manualForm.scale" placeholder="请选择" style="width: 100%">
                    <el-option label="大型" value="大型" />
                    <el-option label="中型" value="中型" />
                    <el-option label="小型" value="小型" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="实施模块">
                  <el-select
                    v-model="manualForm.modules"
                    multiple
                    placeholder="请选择模块"
                    style="width: 100%"
                  >
                    <el-option label="采购" value="采购" />
                    <el-option label="销售" value="销售" />
                    <el-option label="库存" value="库存" />
                    <el-option label="财务" value="财务" />
                    <el-option label="MES" value="MES" />
                    <el-option label="WMS" value="WMS" />
                    <el-option label="质检" value="质检" />
                    <el-option label="加工" value="加工" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="所在区域">
                  <el-input v-model="manualForm.region" placeholder="如：华东" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="期望开始">
                  <el-date-picker
                    v-model="manualForm.expectedStartDate"
                    type="date"
                    placeholder="选择日期"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="24" style="text-align: right">
                <el-button
                  type="primary"
                  size="large"
                  :loading="loading"
                  :disabled="!manualFormValid"
                  @click="handleRecommend"
                >
                  <el-icon><MagicStick /></el-icon>
                  获取推荐
                </el-button>
              </el-col>
            </el-row>
          </el-form>

          <!-- Results: PM Cards -->
          <div v-if="result" class="result-section">
            <el-divider content-position="left">推荐结果</el-divider>

            <el-empty v-if="result.recommendedPms.length === 0" description="暂无匹配的项目经理" />

            <el-row :gutter="16" v-else>
              <el-col
                v-for="pm in result.recommendedPms"
                :key="pm.engineerId"
                :xs="24" :sm="12" :md="8" :lg="8"
              >
                <div class="pm-card" :class="getScoreClass(pm.matchScore)">
                  <div class="pm-card-header">
                    <div class="pm-info">
                      <span class="pm-name">{{ pm.name }}</span>
                      <el-tag size="small" :type="getLevelTagType(pm.level)">{{ pm.level }}</el-tag>
                    </div>
                    <div class="score-circle" :class="getScoreClass(pm.matchScore)">
                      <svg viewBox="0 0 60 60" class="score-svg">
                        <circle cx="30" cy="30" r="26" fill="none" stroke="#e8e8e8" stroke-width="4" />
                        <circle
                          cx="30" cy="30" r="26" fill="none"
                          :stroke="getScoreColor(pm.matchScore)"
                          stroke-width="4"
                          stroke-linecap="round"
                          :stroke-dasharray="getScoreDash(pm.matchScore)"
                          transform="rotate(-90 30 30)"
                        />
                      </svg>
                      <span class="score-text">{{ pm.matchScore }}</span>
                    </div>
                  </div>

                  <div class="pm-card-body">
                    <div class="pm-meta">
                      <el-tag
                        size="small"
                        :type="getStatusType(pm.currentStatus)"
                        effect="light"
                        class="status-tag"
                      >
                        {{ pm.currentStatus }}
                      </el-tag>
                      <span class="meta-text">当前 {{ pm.currentProjectCount }} 个项目</span>
                    </div>

                    <div class="match-reasons">
                      <el-tag
                        v-for="(reason, idx) in pm.matchReasons"
                        :key="idx"
                        size="small"
                        effect="plain"
                        class="reason-tag"
                      >
                        {{ reason }}
                      </el-tag>
                    </div>

                    <el-alert
                      v-if="pm.riskNotes"
                      type="warning"
                      :title="pm.riskNotes"
                      :closable="false"
                      show-icon
                      class="risk-alert"
                    />
                  </div>

                  <div class="pm-card-footer">
                    <el-button
                      type="primary"
                      size="default"
                      :loading="assigningId === pm.engineerId"
                      @click="handleAssign(pm)"
                    >
                      分配
                    </el-button>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-col>

      <!-- Section 2: 排期建议 -->
      <el-col :span="24" v-if="result?.scheduleSuggestion" style="margin-top: 24px">
        <el-card class="section-card schedule-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <div class="card-title">
                <el-icon :size="20"><Calendar /></el-icon>
                <span>排期建议</span>
              </div>
              <div class="schedule-summary">
                <el-tag type="info" effect="dark" size="large">
                  建议开始：{{ result.scheduleSuggestion.recommendedStart }}
                </el-tag>
                <el-tag type="success" effect="dark" size="large">
                  预计工期：{{ result.scheduleSuggestion.estimatedDurationDays }} 天
                </el-tag>
              </div>
            </div>
          </template>

          <el-steps
            :active="result.scheduleSuggestion.milestones.length"
            finish-status="success"
            align-center
            class="milestone-steps"
          >
            <el-step
              v-for="(ms, idx) in result.scheduleSuggestion.milestones"
              :key="idx"
              :title="ms.name"
              :description="`Day ${ms.dayOffset} · ${ms.deliverables}`"
            />
          </el-steps>

          <div class="timeline-section">
            <el-timeline>
              <el-timeline-item
                v-for="(ms, idx) in result.scheduleSuggestion.milestones"
                :key="idx"
                :timestamp="`第 ${ms.dayOffset} 天`"
                placement="top"
                :type="idx === result.scheduleSuggestion.milestones.length - 1 ? 'success' : 'primary'"
                :hollow="idx !== result.scheduleSuggestion.milestones.length - 1"
              >
                <el-card shadow="never" class="timeline-card">
                  <h4>{{ ms.name }}</h4>
                  <p>{{ ms.deliverables }}</p>
                </el-card>
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, MagicStick, Calendar } from '@element-plus/icons-vue'
import { getRecommendation, assignPm } from '@/api/dispatch'
import { listProjects } from '@/api/project'
import type { Project, DispatchResult, PmRecommendation, IndustryType } from '@/types'
import { IndustryTypeLabels } from '@/types'

const inputMode = ref<'project' | 'manual'>('project')
const loading = ref(false)
const assigningId = ref<number | null>(null)
const projectList = ref<Project[]>([])

const projectForm = ref<{ projectId: number | null }>({ projectId: null })
const manualForm = ref({
  customerName: '',
  industryType: '' as IndustryType | '',
  scale: '',
  modules: [] as string[],
  region: '',
  expectedStartDate: '',
})

const result = ref<DispatchResult | null>(null)

const manualFormValid = computed(() => {
  return manualForm.value.customerName.trim() !== ''
    && manualForm.value.industryType !== ''
    && manualForm.value.scale !== ''
})

onMounted(async () => {
  try {
    const res = await listProjects()
    projectList.value = res.data.data
  } catch {
    // ignore
  }
})

async function handleRecommend() {
  loading.value = true
  result.value = null
  try {
    let payload: Record<string, unknown>
    if (inputMode.value === 'project') {
      payload = { projectId: projectForm.value.projectId }
    } else {
      payload = {
        customerName: manualForm.value.customerName,
        industryType: manualForm.value.industryType,
        scale: manualForm.value.scale,
        modules: manualForm.value.modules,
        region: manualForm.value.region || undefined,
        expectedStartDate: manualForm.value.expectedStartDate || undefined,
      }
    }
    const res = await getRecommendation(payload)
    result.value = res.data.data
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}

async function handleAssign(pm: PmRecommendation) {
  const targetProjectId = inputMode.value === 'project' ? projectForm.value.projectId : null
  if (!targetProjectId) {
    ElMessage.warning('手动输入模式下暂不支持直接分配，请先选择具体项目')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确定将 ${pm.name} 分配到该项目吗？`,
      '确认分配',
      { type: 'info' },
    )
    assigningId.value = pm.engineerId
    await assignPm({ projectId: targetProjectId, engineerId: pm.engineerId })
    ElMessage.success(`已成功分配 ${pm.name}`)
  } catch (e: any) {
    if (e !== 'cancel') {
      // error handled by interceptor
    }
  } finally {
    assigningId.value = null
  }
}

function getScoreColor(score: number): string {
  if (score > 80) return '#52c41a'
  if (score > 60) return '#409eff'
  if (score > 40) return '#e6a23c'
  return '#f56c6c'
}

function getScoreClass(score: number): string {
  if (score > 80) return 'score-green'
  if (score > 60) return 'score-blue'
  if (score > 40) return 'score-orange'
  return 'score-red'
}

function getScoreDash(score: number): string {
  const circumference = 2 * Math.PI * 26
  const filled = (score / 100) * circumference
  return `${filled} ${circumference}`
}

function getLevelTagType(level: string): 'success' | 'warning' | 'danger' | 'info' | '' {
  if (level === '资深PM') return 'danger'
  if (level === '高级PM') return 'warning'
  if (level === 'PM') return 'success'
  return 'info'
}

function getStatusType(status: string): 'success' | 'warning' | 'danger' | 'info' | '' {
  if (status === '空闲') return 'success'
  if (status === '项目中') return 'warning'
  if (status === '培训中') return 'info'
  if (status === '休假') return 'danger'
  return 'info'
}
</script>

<style scoped>
.dispatch-page {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0;
}

.page-desc {
  color: #8c8c8c;
  margin-top: 4px;
  font-size: 14px;
}

.section-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.mode-toggle {
  margin-bottom: 20px;
}

.dispatch-form {
  margin-bottom: 8px;
}

.result-section {
  margin-top: 8px;
}

.pm-card {
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.pm-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
}

.pm-card.score-green::before { background: #52c41a; }
.pm-card.score-blue::before { background: #409eff; }
.pm-card.score-orange::before { background: #e6a23c; }
.pm-card.score-red::before { background: #f56c6c; }

.pm-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.pm-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.pm-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pm-name {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
}

.score-circle {
  position: relative;
  width: 56px;
  height: 56px;
  flex-shrink: 0;
}

.score-svg {
  width: 56px;
  height: 56px;
}

.score-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 16px;
  font-weight: 700;
}

.score-green .score-text { color: #52c41a; }
.score-blue .score-text { color: #409eff; }
.score-orange .score-text { color: #e6a23c; }
.score-red .score-text { color: #f56c6c; }

.pm-card-body {
  margin-bottom: 12px;
}

.pm-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.meta-text {
  font-size: 12px;
  color: #999;
}

.match-reasons {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 10px;
}

.reason-tag {
  font-size: 12px;
}

.risk-alert {
  margin-top: 8px;
}

.risk-alert :deep(.el-alert__title) {
  font-size: 12px;
}

.pm-card-footer {
  text-align: right;
}

.schedule-card .card-header {
  flex-wrap: wrap;
  gap: 12px;
}

.schedule-summary {
  display: flex;
  gap: 12px;
}

.milestone-steps {
  margin: 24px 0;
}

.milestone-steps :deep(.el-step__description) {
  font-size: 12px;
  max-width: 160px;
}

.timeline-section {
  margin-top: 32px;
  padding: 0 24px;
}

.timeline-card {
  border-radius: 8px;
}

.timeline-card h4 {
  margin: 0 0 4px;
  font-size: 14px;
  color: #303133;
}

.timeline-card p {
  margin: 0;
  font-size: 13px;
  color: #909399;
}
</style>
