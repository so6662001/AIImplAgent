<template>
  <div>
    <div class="page-header">
      <h2>培训仪表盘</h2>
      <div style="display: flex; align-items: center; gap: 12px;">
        <el-input-number v-model="projectId" :min="1" placeholder="项目ID" controls-position="right" />
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>
    </div>

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
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { getTrainingDashboard } from '@/api/dashboard'
import type { TrainingDashboard } from '@/types'

const projectId = ref<number>(1)
const dashboard = ref<TrainingDashboard | null>(null)

async function loadData() {
  if (!projectId.value) return
  const res = await getTrainingDashboard(projectId.value)
  dashboard.value = res.data.data
}
</script>
