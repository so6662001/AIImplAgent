<template>
  <div>
    <div class="page-header">
      <h2>数据分析仪表盘</h2>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- Tab 1: 项目分析 -->
      <el-tab-pane label="项目分析" name="project">
        <el-form :inline="true" style="margin-bottom: 20px">
          <el-form-item label="项目ID">
            <el-input-number v-model="projectId" :min="1" placeholder="输入项目ID" style="width: 180px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="projectLoading" :disabled="!projectId" @click="loadProjectAnalytics">
              分析
            </el-button>
          </el-form-item>
        </el-form>

        <template v-if="projectData">
          <el-row :gutter="20" style="margin-bottom: 20px">
            <el-col :span="8">
              <div class="score-card" :class="healthColorClass">
                <div class="score-number">{{ projectData.overallHealthScore }}</div>
                <div class="score-label">综合健康度</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="card" style="text-align: center; padding: 24px">
                <el-tag :type="healthTagType" size="large" effect="dark" style="font-size: 16px; padding: 8px 20px">
                  {{ projectData.healthLevel }}
                </el-tag>
                <div style="margin-top: 12px; color: #909399; font-size: 13px">健康等级</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="card" style="text-align: center; padding: 24px">
                <div style="font-size: 14px; color: #606266">{{ projectData.projectCode }}</div>
                <div style="font-size: 16px; font-weight: 600; margin-top: 4px">{{ projectData.customerName }}</div>
                <div style="margin-top: 8px">
                  <el-tag size="small">{{ projectData.status }}</el-tag>
                </div>
              </div>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <!-- Training -->
            <el-col :span="6">
              <div class="card metric-card">
                <h4>培训指标</h4>
                <div class="metric-row">
                  <span class="metric-label">学员数</span>
                  <span class="metric-value">{{ projectData.trainingMetrics.traineeCount }}</span>
                </div>
                <div class="metric-row">
                  <span class="metric-label">通过率</span>
                  <el-progress
                    :percentage="Number(projectData.trainingMetrics.passRate)"
                    :stroke-width="14"
                    :text-inside="true"
                    style="flex: 1; margin-left: 8px"
                  />
                </div>
                <div class="metric-row">
                  <span class="metric-label">KA就绪</span>
                  <el-tag :type="projectData.trainingMetrics.kaReady ? 'success' : 'danger'" size="small">
                    {{ projectData.trainingMetrics.kaReady ? '是' : '否' }}
                  </el-tag>
                </div>
              </div>
            </el-col>

            <!-- Import -->
            <el-col :span="6">
              <div class="card metric-card">
                <h4>导入指标</h4>
                <div class="metric-row">
                  <span class="metric-label">已完成</span>
                  <span class="metric-value">{{ projectData.importMetrics.completed }} / {{ projectData.importMetrics.total }}</span>
                </div>
                <div class="metric-row">
                  <span class="metric-label">完成率</span>
                  <el-progress
                    :percentage="Number(projectData.importMetrics.rate)"
                    :stroke-width="14"
                    :text-inside="true"
                    style="flex: 1; margin-left: 8px"
                  />
                </div>
              </div>
            </el-col>

            <!-- Tickets -->
            <el-col :span="6">
              <div class="card metric-card">
                <h4>工单指标</h4>
                <div class="metric-row">
                  <span class="metric-label">总工单</span>
                  <span class="metric-value">{{ projectData.ticketMetrics.total }}</span>
                </div>
                <div class="metric-row">
                  <span class="metric-label">已解决</span>
                  <span class="metric-value">{{ projectData.ticketMetrics.resolved }}</span>
                </div>
                <div class="metric-row">
                  <span class="metric-label">解决率</span>
                  <el-progress
                    :percentage="Number(projectData.ticketMetrics.rate)"
                    :stroke-width="14"
                    :text-inside="true"
                    style="flex: 1; margin-left: 8px"
                  />
                </div>
              </div>
            </el-col>

            <!-- Simulation -->
            <el-col :span="6">
              <div class="card metric-card">
                <h4>演练指标</h4>
                <div class="metric-row">
                  <span class="metric-label">总场景</span>
                  <span class="metric-value">{{ projectData.simulationMetrics.total }}</span>
                </div>
                <div class="metric-row">
                  <span class="metric-label">已通过</span>
                  <span class="metric-value">{{ projectData.simulationMetrics.passed }}</span>
                </div>
                <div class="metric-row">
                  <span class="metric-label">通过率</span>
                  <el-progress
                    :percentage="Number(projectData.simulationMetrics.rate)"
                    :stroke-width="14"
                    :text-inside="true"
                    style="flex: 1; margin-left: 8px"
                  />
                </div>
              </div>
            </el-col>
          </el-row>
        </template>
        <el-empty v-else description="请输入项目ID并点击分析" />
      </el-tab-pane>

      <!-- Tab 2: 部门概览 -->
      <el-tab-pane label="部门概览" name="department">
        <div style="margin-bottom: 20px">
          <el-button type="primary" :loading="deptLoading" @click="loadDeptAnalytics">
            刷新
          </el-button>
        </div>

        <template v-if="deptData">
          <el-row :gutter="20" style="margin-bottom: 20px">
            <el-col :span="6">
              <div class="card stat-card">
                <div class="stat-number">{{ deptData.activeProjects }}</div>
                <div class="stat-label">活跃项目</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="card stat-card">
                <div class="stat-number">{{ deptData.completedProjects }}</div>
                <div class="stat-label">已完成项目</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="card stat-card">
                <div class="stat-number">{{ deptData.totalEngineers }}</div>
                <div class="stat-label">工程师总数</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="card stat-card">
                <div class="stat-number">{{ deptData.avgUtilizationRate }}%</div>
                <div class="stat-label">平均利用率</div>
              </div>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="8">
              <div class="card" style="padding: 24px; text-align: center">
                <div style="font-size: 28px; font-weight: 700; color: #409eff">{{ deptData.avgPqi }}</div>
                <div style="margin-top: 8px; color: #909399; font-size: 13px">平均PQI评分</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="card" style="padding: 24px">
                <div style="font-size: 13px; color: #909399; margin-bottom: 8px">工单解决率</div>
                <el-progress
                  :percentage="Number(deptData.ticketResolutionRate)"
                  :stroke-width="20"
                  :text-inside="true"
                />
              </div>
            </el-col>
            <el-col :span="8">
              <div class="card" style="padding: 24px; text-align: center">
                <div style="font-size: 28px; font-weight: 700; color: #67c23a">{{ deptData.knowledgeEntryCount }}</div>
                <div style="margin-top: 8px; color: #909399; font-size: 13px">知识库条目数</div>
              </div>
            </el-col>
          </el-row>
        </template>
        <el-empty v-else description="点击刷新加载部门数据" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getProjectAnalytics, getDepartmentAnalytics } from '@/api/analytics'

const activeTab = ref('project')
const projectId = ref<number>()
const projectLoading = ref(false)
const projectData = ref<any>(null)
const deptLoading = ref(false)
const deptData = ref<any>(null)

const healthColorClass = computed(() => {
  if (!projectData.value) return ''
  const score = projectData.value.overallHealthScore
  if (score >= 80) return 'score-green'
  if (score >= 60) return 'score-orange'
  return 'score-red'
})

const healthTagType = computed(() => {
  if (!projectData.value) return 'info' as const
  const level = projectData.value.healthLevel
  if (level === 'HEALTHY' || level === 'EXCELLENT') return 'success' as const
  if (level === 'WARNING' || level === 'AT_RISK') return 'warning' as const
  return 'danger' as const
})

async function loadProjectAnalytics() {
  if (!projectId.value) return
  projectLoading.value = true
  try {
    const res = await getProjectAnalytics(projectId.value)
    projectData.value = res.data.data
  } catch {
    ElMessage.error('加载项目分析失败')
  } finally {
    projectLoading.value = false
  }
}

async function loadDeptAnalytics() {
  deptLoading.value = true
  try {
    const res = await getDepartmentAnalytics()
    deptData.value = res.data.data
  } catch {
    ElMessage.error('加载部门分析失败')
  } finally {
    deptLoading.value = false
  }
}
</script>

<style scoped>
.score-card {
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  color: #fff;
}
.score-number {
  font-size: 56px;
  font-weight: 800;
  line-height: 1;
}
.score-label {
  margin-top: 12px;
  font-size: 14px;
  opacity: 0.9;
}
.score-green { background: linear-gradient(135deg, #67c23a, #529b2e); }
.score-orange { background: linear-gradient(135deg, #e6a23c, #cf8a1e); }
.score-red { background: linear-gradient(135deg, #f56c6c, #c45656); }

.metric-card {
  padding: 20px;
}
.metric-card h4 {
  margin: 0 0 16px;
  color: #303133;
  font-size: 15px;
}
.metric-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.metric-row:last-child { margin-bottom: 0; }
.metric-label {
  color: #909399;
  font-size: 13px;
  flex-shrink: 0;
}
.metric-value {
  font-weight: 600;
  color: #303133;
  font-size: 15px;
}

.stat-card {
  padding: 24px;
  text-align: center;
}
.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
}
.stat-label {
  margin-top: 8px;
  color: #909399;
  font-size: 13px;
}
</style>
