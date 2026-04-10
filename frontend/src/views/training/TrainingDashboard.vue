<template>
  <div>
    <div class="page-header">
      <h2>培训仪表盘</h2>
      <div style="display: flex; align-items: center; gap: 12px;">
        <el-input-number v-model="projectId" :min="1" placeholder="项目ID" controls-position="right" />
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- 基础看板 -->
      <el-tab-pane label="基础看板" name="basic">
        <template v-if="dashboard">
          <el-row :gutter="16" style="margin-bottom: 20px;">
            <el-col :span="6">
              <div class="card" style="text-align: center; padding: 24px;">
                <div style="font-size: 32px; font-weight: 700; color: var(--primary);">{{ dashboard.totalTrainees }}</div>
                <div style="color: #888; margin-top: 4px;">学员总数</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="card" style="text-align: center; padding: 24px;">
                <div style="font-size: 32px; font-weight: 700; color: #e6a23c;">{{ dashboard.kaUserCount }}</div>
                <div style="color: #888; margin-top: 4px;">KA用户数</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="card" style="text-align: center; padding: 24px;">
                <div style="font-size: 32px; font-weight: 700; color: #67c23a;">{{ dashboard.overallPassRate }}%</div>
                <div style="color: #888; margin-top: 4px;">总体通过率</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="card" style="text-align: center; padding: 24px;">
                <el-tag :type="dashboard.goLiveReady ? 'success' : 'danger'" size="large" effect="dark">
                  {{ dashboard.goLiveReady ? '可上线' : '未就绪' }}
                </el-tag>
                <div style="color: #888; margin-top: 8px;">上线就绪</div>
              </div>
            </el-col>
          </el-row>

          <div class="card" style="margin-bottom: 20px;">
            <h3 style="margin-bottom: 16px;">模块统计</h3>
            <el-table :data="dashboard.moduleStats" stripe>
              <el-table-column prop="module" label="模块" min-width="160" />
              <el-table-column prop="examCount" label="考核次数" width="100" />
              <el-table-column label="通过率" width="120">
                <template #default="{ row }">{{ row.passRate }}%</template>
              </el-table-column>
            </el-table>
          </div>

          <el-row :gutter="16" style="margin-bottom: 20px;">
            <el-col :span="12">
              <div class="card" style="padding: 24px;">
                <h3 style="margin-bottom: 16px;">出勤率</h3>
                <el-progress :percentage="dashboard.attendanceRate" :stroke-width="20" :text-inside="true" />
              </div>
            </el-col>
            <el-col :span="12">
              <div class="card" style="padding: 24px;">
                <h3 style="margin-bottom: 16px;">文档完成率</h3>
                <el-progress :percentage="dashboard.documentCompletionRate" :stroke-width="20" :text-inside="true" />
              </div>
            </el-col>
          </el-row>

          <div class="card" style="padding: 24px;">
            <el-result
              :icon="dashboard.goLiveReady ? 'success' : 'error'"
              :title="dashboard.goLiveReady ? '项目已具备上线条件' : '项目尚未具备上线条件'"
              :sub-title="dashboard.goLiveReady ? '所有培训指标均已达标' : '部分培训指标未达标，请继续推进'"
            />
          </div>
        </template>

        <div v-else class="card" style="padding: 40px; text-align: center; color: #999;">
          请输入项目ID后点击查询
        </div>
      </el-tab-pane>

      <!-- 详细看板 -->
      <el-tab-pane label="详细看板" name="enhanced">
        <template v-if="enhancedData">
          <!-- 风险预警 -->
          <div v-if="enhancedData.riskWarnings && enhancedData.riskWarnings.length" class="card" style="margin-bottom: 20px;">
            <h3 style="margin-bottom: 16px;">风险预警</h3>
            <el-alert
              v-for="(warn, idx) in enhancedData.riskWarnings"
              :key="idx"
              :title="warn"
              type="warning"
              show-icon
              :closable="false"
              style="margin-bottom: 8px;"
            />
          </div>

          <!-- 学员状态 -->
          <div class="card" style="margin-bottom: 20px;">
            <h3 style="margin-bottom: 16px;">学员状态</h3>
            <el-table :data="enhancedData.traineeStatuses" stripe>
              <el-table-column prop="name" label="姓名" width="100" />
              <el-table-column prop="role" label="角色" width="100" />
              <el-table-column label="KA" width="60">
                <template #default="{ row }">
                  <el-tag v-if="row.kaUser" type="warning" size="small">KA</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="学习进度" min-width="160">
                <template #default="{ row }">
                  <el-progress :percentage="row.progressPercent" :stroke-width="16" :text-inside="true" />
                </template>
              </el-table-column>
              <el-table-column label="出勤" width="100">
                <template #default="{ row }">{{ row.attendanceDays }}/{{ row.totalDays }}</template>
              </el-table-column>
              <el-table-column label="通过模块" width="100">
                <template #default="{ row }">{{ row.passedModules }}/{{ row.totalModules }}</template>
              </el-table-column>
              <el-table-column label="风险等级" width="100">
                <template #default="{ row }">
                  <el-tag
                    :type="riskTagType(row.riskLevel)"
                    size="small"
                  >{{ row.riskLevel }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 模块状态 -->
          <div class="card" style="margin-bottom: 20px;">
            <h3 style="margin-bottom: 16px;">模块培训状态</h3>
            <el-table :data="enhancedData.moduleStatuses" stripe>
              <el-table-column prop="module" label="模块" min-width="160" />
              <el-table-column label="已培训" width="80">
                <template #default="{ row }">
                  <span v-if="row.trained" style="color: #67c23a;">✅</span>
                  <span v-else style="color: #ccc;">—</span>
                </template>
              </el-table-column>
              <el-table-column prop="examCount" label="考核次数" width="100" />
              <el-table-column label="通过率" min-width="160">
                <template #default="{ row }">
                  <el-progress :percentage="Number(row.passRate)" :stroke-width="16" :text-inside="true" />
                </template>
              </el-table-column>
              <el-table-column prop="topWeakPoint" label="主要薄弱点" min-width="160" />
            </el-table>
          </div>

          <!-- 文档矩阵 -->
          <div v-if="enhancedData.documentMatrix && enhancedData.documentMatrix.length" class="card" style="margin-bottom: 20px;">
            <h3 style="margin-bottom: 16px;">文档完成矩阵</h3>
            <el-table :data="enhancedData.documentMatrix" stripe>
              <el-table-column prop="date" label="日期" width="120" />
              <el-table-column label="培训计划" width="100">
                <template #default="{ row }">{{ row.planUploaded ? '✅' : '❌' }}</template>
              </el-table-column>
              <el-table-column label="课件" width="80">
                <template #default="{ row }">{{ row.coursewareUploaded ? '✅' : '❌' }}</template>
              </el-table-column>
              <el-table-column label="签到" width="80">
                <template #default="{ row }">{{ row.signInCompleted ? '✅' : '❌' }}</template>
              </el-table-column>
              <el-table-column label="纪要" width="80">
                <template #default="{ row }">{{ row.summaryUploaded ? '✅' : '❌' }}</template>
              </el-table-column>
              <el-table-column label="考核" width="80">
                <template #default="{ row }">{{ row.examConducted ? '✅' : '❌' }}</template>
              </el-table-column>
              <el-table-column label="日报" width="80">
                <template #default="{ row }">{{ row.dailyReportSubmitted ? '✅' : '❌' }}</template>
              </el-table-column>
            </el-table>
          </div>
        </template>

        <div v-else class="card" style="padding: 40px; text-align: center; color: #999;">
          请先在上方输入项目ID并点击查询
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { getTrainingDashboard } from '@/api/dashboard'
import { getEnhancedDashboard } from '@/api/training'
import type { TrainingDashboard } from '@/types'

const activeTab = ref('basic')
const projectId = ref<number>(1)
const dashboard = ref<TrainingDashboard | null>(null)
const enhancedData = ref<any>(null)

function riskTagType(level: string) {
  const map: Record<string, string> = {
    HIGH: 'danger',
    MEDIUM: 'warning',
    NORMAL: 'success',
  }
  return map[level] || 'info'
}

async function loadData() {
  if (!projectId.value) return
  const [basicRes, enhancedRes] = await Promise.all([
    getTrainingDashboard(projectId.value),
    getEnhancedDashboard(projectId.value),
  ])
  dashboard.value = basicRes.data.data
  enhancedData.value = enhancedRes.data.data
}
</script>
