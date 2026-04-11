<template>
  <div>
    <div class="page-header">
      <h2>决策日志</h2>
    </div>

    <!-- Performance Summary -->
    <div class="card perf-section" v-if="perfSummary.length > 0">
      <h3 style="margin: 0 0 12px 0">性能概览</h3>
      <el-row :gutter="16">
        <el-col :span="8" v-for="p in perfSummary" :key="p.agentCode">
          <div class="perf-card">
            <div class="perf-agent">{{ p.agentCode }}</div>
            <div class="perf-stats">
              <span>总决策: {{ p.totalDecisions }}</span>
              <span>平均耗时: {{ Math.round(p.avgExecutionTimeMs) }}ms</span>
            </div>
            <el-progress
              :percentage="p.reviewedCount > 0 ? Math.round((p.approvedCount / p.reviewedCount) * 100) : 0"
              :stroke-width="8"
              :format="() => `通过率 ${p.reviewedCount > 0 ? Math.round((p.approvedCount / p.reviewedCount) * 100) : 0}%`"
            />
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="card">
      <div class="filter-bar">
        <el-select v-model="filterAgent" placeholder="智能体" clearable style="width: 180px">
          <el-option v-for="a in agentCodes" :key="a" :label="a" :value="a" />
        </el-select>
        <el-input-number v-model="filterProjectId" placeholder="项目ID" :min="1" controls-position="right" style="width: 140px" clearable />
        <el-button type="primary" @click="loadDecisions">查询</el-button>
      </div>

      <el-table :data="decisions" stripe>
        <el-table-column prop="agentCode" label="智能体" width="160" />
        <el-table-column label="触发类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.triggerType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="inputSummary" label="输入摘要" min-width="180" show-overflow-tooltip />
        <el-table-column prop="outputSummary" label="输出摘要" min-width="180" show-overflow-tooltip />
        <el-table-column prop="modelUsed" label="模型" width="120" />
        <el-table-column prop="executionTimeMs" label="耗时(ms)" width="100" align="center" />
        <el-table-column label="已审核" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.humanReviewed ? 'success' : 'info'" size="small">
              {{ row.humanReviewed ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审核结果" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.reviewResult" :type="reviewTagType(row.reviewResult)" size="small">
              {{ row.reviewResult }}
            </el-tag>
            <span v-else style="color: #c0c4cc">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button v-if="!row.humanReviewed" link type="primary" size="small" @click="openReview(row)">
              审核
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Review Dialog -->
    <el-dialog v-model="showReview" title="审核决策" width="500px" destroy-on-close>
      <el-form :model="reviewForm" label-width="90px">
        <el-form-item label="审核结果">
          <el-select v-model="reviewForm.reviewResult" placeholder="请选择" style="width: 100%">
            <el-option label="APPROVED" value="APPROVED" />
            <el-option label="MODIFIED" value="MODIFIED" />
            <el-option label="REJECTED" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核备注">
          <el-input v-model="reviewForm.reviewComment" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReview = false">取消</el-button>
        <el-button type="primary" :loading="reviewing" @click="handleReview">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listDecisions, reviewDecision, getAllPerformance } from '@/api/agentdecision'

const agentCodes = [
  'DISPATCH_AGENT', 'PLAN_AGENT', 'RESEARCH_AGENT', 'TRAINING_AGENT',
  'DATA_GOV_AGENT', 'SIMULATION_AGENT', 'GOLIVE_AGENT', 'REPORT_AGENT',
  'WORKFORCE_AGENT', 'AFTERSALES_AGENT', 'INPUT_ASSIST_AGENT', 'ACCOUNT_SET_AGENT',
]

const filterAgent = ref('')
const filterProjectId = ref<number | undefined>(undefined)
const decisions = ref<any[]>([])
const perfSummary = ref<any[]>([])

const showReview = ref(false)
const reviewing = ref(false)
const reviewTargetId = ref<number | null>(null)
const reviewForm = ref({ reviewResult: '', reviewComment: '' })

function reviewTagType(result: string) {
  const map: Record<string, string> = { APPROVED: 'success', MODIFIED: 'warning', REJECTED: 'danger' }
  return map[result] || 'info'
}

async function loadDecisions() {
  const params: Record<string, any> = {}
  if (filterAgent.value) params.agentCode = filterAgent.value
  if (filterProjectId.value) params.projectId = filterProjectId.value
  const res = await listDecisions(params)
  decisions.value = res.data.data
}

async function loadPerformance() {
  const res = await getAllPerformance()
  perfSummary.value = res.data.data
}

function openReview(row: any) {
  reviewTargetId.value = row.id
  reviewForm.value = { reviewResult: '', reviewComment: '' }
  showReview.value = true
}

async function handleReview() {
  if (!reviewForm.value.reviewResult) {
    ElMessage.warning('请选择审核结果')
    return
  }
  reviewing.value = true
  try {
    await reviewDecision(reviewTargetId.value!, reviewForm.value)
    ElMessage.success('审核完成')
    showReview.value = false
    await loadDecisions()
  } finally {
    reviewing.value = false
  }
}

onMounted(() => {
  loadDecisions()
  loadPerformance()
})
</script>

<style scoped>
.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
  flex-wrap: wrap;
}

.perf-section {
  margin-bottom: 20px;
}

.perf-card {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 14px 16px;
}

.perf-agent {
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 6px;
}

.perf-stats {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
}
</style>
