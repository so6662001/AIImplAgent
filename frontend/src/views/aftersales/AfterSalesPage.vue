<template>
  <div>
    <div class="page-header">
      <h2>售后服务工作台</h2>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- Tab 1: 售后工单 -->
      <el-tab-pane label="售后工单" name="tickets">
        <div class="card" style="margin-bottom: 20px;">
          <el-form inline>
            <el-form-item label="项目ID">
              <el-input-number v-model="filterProjectId" :min="1" controls-position="right" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadTickets">查询</el-button>
              <el-button type="success" @click="handleCreateOpen">新建工单</el-button>
            </el-form-item>
          </el-form>
        </div>

        <el-row :gutter="16" style="margin-bottom: 20px;" v-if="ticketStats">
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-value">{{ ticketStats.total ?? 0 }}</div>
              <div class="stat-label">工单总数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card stat-sla">
              <div class="stat-value" v-if="ticketStats.bySla">
                P0:{{ ticketStats.bySla.P0 }} / P1:{{ ticketStats.bySla.P1 }} / P2:{{ ticketStats.bySla.P2 }} / P3:{{ ticketStats.bySla.P3 }}
              </div>
              <div class="stat-label">按SLA分布</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card stat-satisfaction">
              <div class="stat-value">{{ ticketStats.avgSatisfaction ?? '—' }}</div>
              <div class="stat-label">平均满意度</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card stat-resolution">
              <div class="stat-value">{{ ticketStats.avgResolutionHours ?? '—' }}h</div>
              <div class="stat-label">平均解决时长</div>
            </div>
          </el-col>
        </el-row>

        <div class="card">
          <el-table :data="tickets" stripe row-key="id">
            <el-table-column type="expand">
              <template #default="{ row }">
                <div style="padding: 12px 24px;">
                  <p><strong>问题描述：</strong>{{ row.description }}</p>
                  <p v-if="row.resolution"><strong>处理结果：</strong>{{ row.resolution }}</p>
                  <p v-if="row.knowledgeCreated">
                    <el-tag type="success" size="small">已录入知识库</el-tag>
                  </p>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="title" label="标题" min-width="160" />
            <el-table-column label="渠道" width="90">
              <template #default="{ row }">
                <el-tag size="small">{{ channelLabel(row.channel) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="意图类型" width="110">
              <template #default="{ row }">
                <el-tag :type="intentTagType(row.intentType)" size="small">{{ intentLabel(row.intentType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="SLA" width="80">
              <template #default="{ row }">
                <el-tag :type="slaTagType(row.slaPriority)" size="small">{{ row.slaPriority }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="assignedEngineerName" label="指派工程师" width="120" />
            <el-table-column label="SLA截止" width="160">
              <template #default="{ row }">{{ row.slaDeadline ?? '—' }}</template>
            </el-table-column>
            <el-table-column label="满意度" width="140">
              <template #default="{ row }">
                <el-rate v-if="row.customerSatisfaction" v-model="row.customerSatisfaction" disabled />
                <span v-else style="color:#999;">未评价</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.status !== 'RESOLVED' && row.status !== 'CLOSED'" type="primary" link size="small" @click="handleResolveOpen(row)">解决</el-button>
                <el-button v-if="row.status === 'RESOLVED' && !row.customerSatisfaction" type="warning" link size="small" @click="handleRateOpen(row)">评价</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <!-- Tab 2: 客户健康度 -->
      <el-tab-pane label="客户健康度" name="health">
        <div class="card" style="margin-bottom: 20px;">
          <el-form inline>
            <el-form-item label="项目ID">
              <el-input-number v-model="healthProjectId" :min="1" controls-position="right" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadHealth">查询</el-button>
              <el-button type="warning" @click="handleHealthCheck">健康检查</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div v-if="healthData" style="margin-bottom: 20px;">
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="health-score-card" :style="{ borderColor: healthColor(healthData.healthLevel) }">
                <div class="health-score" :style="{ color: healthColor(healthData.healthLevel) }">{{ healthData.healthScore }}</div>
                <div class="health-label">健康评分</div>
                <el-tag :type="healthTagType(healthData.healthLevel)" size="large" style="margin-top: 8px;">{{ healthLevelLabel(healthData.healthLevel) }}</el-tag>
              </div>
            </el-col>
            <el-col :span="16">
              <el-row :gutter="12">
                <el-col :span="8">
                  <div class="metric-card">
                    <div class="metric-value">{{ healthData.ticketCount30d ?? 0 }}</div>
                    <div class="metric-label">近30天工单</div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="metric-card">
                    <div class="metric-value">
                      <span :style="{ color: trendColor(healthData.ticketTrend) }">{{ trendIcon(healthData.ticketTrend) }}</span>
                      {{ healthData.ticketTrend }}
                    </div>
                    <div class="metric-label">工单趋势</div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="metric-card">
                    <div class="metric-value">{{ healthData.openTicketCount ?? 0 }}</div>
                    <div class="metric-label">未解决工单</div>
                  </div>
                </el-col>
                <el-col :span="8" style="margin-top: 12px;">
                  <div class="metric-card">
                    <div class="metric-value">{{ healthData.avgResolutionHours ?? '—' }}h</div>
                    <div class="metric-label">平均解决时长</div>
                  </div>
                </el-col>
                <el-col :span="8" style="margin-top: 12px;">
                  <div class="metric-card">
                    <div class="metric-value">{{ healthData.customerSatisfactionAvg ?? '—' }}</div>
                    <div class="metric-label">平均满意度</div>
                  </div>
                </el-col>
              </el-row>
            </el-col>
          </el-row>

          <div v-if="healthData.careActions && healthData.careActions.length" style="margin-top: 20px;">
            <h4 style="margin-bottom: 12px;">关怀建议</h4>
            <div v-for="(action, idx) in healthData.careActions" :key="idx" style="margin-bottom: 8px;">
              <el-alert :title="action" type="warning" :closable="false" show-icon />
            </div>
          </div>
        </div>
        <el-empty v-else description="请选择项目并查询健康度" />

        <div v-if="healthHistory.length" class="card" style="margin-top: 20px;">
          <h4 style="margin-bottom: 12px;">检查历史</h4>
          <el-timeline>
            <el-timeline-item
              v-for="item in healthHistory"
              :key="item.id"
              :timestamp="item.checkDate"
              placement="top"
            >
              <el-card shadow="hover">
                <div style="display:flex;align-items:center;gap:12px;">
                  <el-tag :type="healthTagType(item.healthLevel)" size="small">{{ healthLevelLabel(item.healthLevel) }}</el-tag>
                  <span>评分: <strong>{{ item.healthScore }}</strong></span>
                  <span>工单数: {{ item.ticketCount30d }}</span>
                  <span>趋势: {{ trendIcon(item.ticketTrend) }}{{ item.ticketTrend }}</span>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </div>
      </el-tab-pane>

      <!-- Tab 3: 统计分析 -->
      <el-tab-pane label="统计分析" name="stats">
        <div class="card" style="margin-bottom: 20px;">
          <el-form inline>
            <el-form-item label="项目ID">
              <el-input-number v-model="statsProjectId" :min="1" controls-position="right" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadStatsTab">查询</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div v-if="analysisStats">
          <h4 style="margin-bottom: 12px;">按意图类型</h4>
          <el-row :gutter="16" style="margin-bottom: 20px;">
            <el-col :span="6">
              <div class="stat-card">
                <div class="stat-value">{{ analysisStats.byIntent?.CONSULT ?? 0 }}</div>
                <div class="stat-label">操作咨询</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-card stat-open">
                <div class="stat-value">{{ analysisStats.byIntent?.FAULT ?? 0 }}</div>
                <div class="stat-label">故障报修</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-card stat-satisfaction">
                <div class="stat-value">{{ analysisStats.byIntent?.SUGGESTION ?? 0 }}</div>
                <div class="stat-label">需求建议</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-card stat-resolution">
                <div class="stat-value">{{ analysisStats.byIntent?.COMPLAINT ?? 0 }}</div>
                <div class="stat-label">投诉处理</div>
              </div>
            </el-col>
          </el-row>

          <h4 style="margin-bottom: 12px;">按SLA等级</h4>
          <el-row :gutter="16" style="margin-bottom: 20px;">
            <el-col :span="6">
              <div class="stat-card" style="background:#fef2f2;border-color:#fca5a5;">
                <div class="stat-value" style="color:#dc2626;">{{ analysisStats.bySla?.P0 ?? 0 }}</div>
                <div class="stat-label">P0 (紧急)</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-card" style="background:#fffbeb;border-color:#fde68a;">
                <div class="stat-value" style="color:#d97706;">{{ analysisStats.bySla?.P1 ?? 0 }}</div>
                <div class="stat-label">P1 (高)</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-card" style="background:#eff6ff;border-color:#93c5fd;">
                <div class="stat-value" style="color:#2563eb;">{{ analysisStats.bySla?.P2 ?? 0 }}</div>
                <div class="stat-label">P2 (中)</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-card" style="background:#f0fdf4;border-color:#86efac;">
                <div class="stat-value" style="color:#16a34a;">{{ analysisStats.bySla?.P3 ?? 0 }}</div>
                <div class="stat-label">P3 (低)</div>
              </div>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <div class="card" style="padding: 20px;">
                <h4 style="margin-bottom: 12px;">解决率</h4>
                <el-progress
                  :percentage="resolutionRate"
                  :stroke-width="18"
                  :format="(p: number) => p.toFixed(1) + '%'"
                  :color="resolutionRate >= 80 ? '#67c23a' : resolutionRate >= 50 ? '#e6a23c' : '#f56c6c'"
                />
              </div>
            </el-col>
            <el-col :span="12">
              <div class="card" style="padding: 20px;">
                <h4 style="margin-bottom: 12px;">平均满意度</h4>
                <el-rate
                  v-model="avgSatisfactionNum"
                  disabled
                  show-score
                  score-template="{value} 分"
                  :colors="['#f56c6c', '#e6a23c', '#67c23a']"
                />
              </div>
            </el-col>
          </el-row>
        </div>
        <el-empty v-else description="请选择项目并查询统计数据" />
      </el-tab-pane>
    </el-tabs>

    <!-- Create Ticket Dialog -->
    <el-dialog v-model="showCreateDialog" title="新建售后工单" width="560px" destroy-on-close>
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="createForm.title" maxlength="100" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="createForm.description" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="渠道" prop="channel">
          <el-select v-model="createForm.channel" placeholder="请选择渠道">
            <el-option label="WEB" value="WEB" />
            <el-option label="微信" value="WECHAT" />
            <el-option label="电话" value="PHONE" />
            <el-option label="邮件" value="EMAIL" />
          </el-select>
        </el-form-item>
        <el-form-item label="意图类型" prop="intentType">
          <el-select v-model="createForm.intentType" placeholder="请选择意图">
            <el-option label="操作咨询" value="CONSULT" />
            <el-option label="故障报修" value="FAULT" />
            <el-option label="需求建议" value="SUGGESTION" />
            <el-option label="投诉处理" value="COMPLAINT" />
          </el-select>
        </el-form-item>
        <el-form-item label="报告人" prop="reporterName">
          <el-input v-model="createForm.reporterName" maxlength="30" />
        </el-form-item>
        <el-form-item label="联系方式" prop="reporterContact">
          <el-input v-model="createForm.reporterContact" maxlength="30" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreateSubmit">提交</el-button>
      </template>
    </el-dialog>

    <!-- Resolve Ticket Dialog -->
    <el-dialog v-model="showResolveDialog" title="解决工单" width="500px" destroy-on-close>
      <el-form ref="resolveFormRef" :model="resolveForm" :rules="resolveRules" label-width="100px">
        <el-form-item label="处理结果" prop="resolution">
          <el-input v-model="resolveForm.resolution" type="textarea" :rows="4" placeholder="请输入处理结果" />
        </el-form-item>
        <el-form-item label="录入知识库">
          <el-switch v-model="resolveForm.knowledgeCreated" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showResolveDialog = false">取消</el-button>
        <el-button type="primary" :loading="resolving" @click="handleResolveSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- Rate Ticket Dialog -->
    <el-dialog v-model="showRateDialog" title="工单评价" width="400px" destroy-on-close>
      <div style="text-align:center;padding:20px 0;">
        <p style="margin-bottom:16px;color:#606266;">请对本次服务进行评分</p>
        <el-rate v-model="rateScore" :colors="['#f56c6c', '#e6a23c', '#67c23a']" show-text :texts="['很差', '较差', '一般', '满意', '非常满意']" />
      </div>
      <template #footer>
        <el-button @click="showRateDialog = false">取消</el-button>
        <el-button type="primary" :loading="rating" @click="handleRateSubmit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import { createAfterSalesTicket, listAfterSalesTickets, resolveAfterSalesTicket, rateAfterSalesTicket, getAfterSalesStats } from '@/api/aftersales'
import { checkCustomerHealth, getLatestHealth, getHealthHistory } from '@/api/customerhealth'
import { required } from '@/utils/validators'

const activeTab = ref('tickets')
const filterProjectId = ref(1)
const healthProjectId = ref(1)
const statsProjectId = ref(1)

const tickets = ref<any[]>([])
const ticketStats = ref<any>(null)
const healthData = ref<any>(null)
const healthHistory = ref<any[]>([])
const analysisStats = ref<any>(null)

const showCreateDialog = ref(false)
const showResolveDialog = ref(false)
const showRateDialog = ref(false)
const creating = ref(false)
const resolving = ref(false)
const rating = ref(false)
const createFormRef = ref<FormInstance>()
const resolveFormRef = ref<FormInstance>()
const currentTicketId = ref(0)
const rateScore = ref(5)

const createForm = ref({
  title: '',
  description: '',
  channel: '',
  intentType: '',
  reporterName: '',
  reporterContact: '',
})

const resolveForm = ref({
  resolution: '',
  knowledgeCreated: false,
})

const createRules = {
  title: [required('标题不能为空')],
  description: [required('描述不能为空')],
  channel: [required('渠道不能为空')],
  intentType: [required('意图类型不能为空')],
}

const resolveRules = {
  resolution: [required('处理结果不能为空')],
}

function channelLabel(ch: string) {
  const m: Record<string, string> = { WEB: 'WEB', WECHAT: '微信', PHONE: '电话', EMAIL: '邮件' }
  return m[ch] || ch
}

function intentLabel(t: string) {
  const m: Record<string, string> = { CONSULT: '操作咨询', FAULT: '故障报修', SUGGESTION: '需求建议', COMPLAINT: '投诉处理' }
  return m[t] || t
}

function intentTagType(t: string) {
  const m: Record<string, string> = { CONSULT: '', FAULT: 'danger', SUGGESTION: 'warning', COMPLAINT: 'danger' }
  return m[t] || 'info'
}

function slaTagType(sla: string) {
  const m: Record<string, string> = { P0: 'danger', P1: 'warning', P2: '', P3: 'success' }
  return m[sla] || 'info'
}

function statusTagType(s: string) {
  const m: Record<string, string> = { NEW: 'info', ASSIGNED: 'warning', RESOLVED: 'success', CLOSED: '' }
  return m[s] || 'info'
}

function statusLabel(s: string) {
  const m: Record<string, string> = { NEW: '新建', ASSIGNED: '已指派', RESOLVED: '已解决', CLOSED: '已关闭' }
  return m[s] || s
}

function healthColor(level: string) {
  const m: Record<string, string> = { HEALTHY: '#16a34a', ATTENTION: '#2563eb', WARNING: '#d97706', CRITICAL: '#dc2626' }
  return m[level] || '#999'
}

function healthTagType(level: string) {
  const m: Record<string, string> = { HEALTHY: 'success', ATTENTION: '', WARNING: 'warning', CRITICAL: 'danger' }
  return m[level] || 'info'
}

function healthLevelLabel(level: string) {
  const m: Record<string, string> = { HEALTHY: '健康', ATTENTION: '关注', WARNING: '预警', CRITICAL: '危险' }
  return m[level] || level
}

function trendIcon(trend: string) {
  const m: Record<string, string> = { UP: '↑', STABLE: '→', DOWN: '↓' }
  return m[trend] || ''
}

function trendColor(trend: string) {
  const m: Record<string, string> = { UP: '#dc2626', STABLE: '#9ca3af', DOWN: '#16a34a' }
  return m[trend] || '#999'
}

const resolutionRate = computed(() => {
  if (!analysisStats.value || !analysisStats.value.total || analysisStats.value.total === 0) return 0
  const resolved = (analysisStats.value.byIntent?.CONSULT ?? 0)
    + (analysisStats.value.total - (analysisStats.value.bySla?.P0 ?? 0) - (analysisStats.value.bySla?.P1 ?? 0) - (analysisStats.value.bySla?.P2 ?? 0) - (analysisStats.value.bySla?.P3 ?? 0))
  const total = analysisStats.value.total as number
  if (total === 0) return 0
  const resolvedCount = tickets.value.filter((t: any) => t.status === 'RESOLVED' || t.status === 'CLOSED').length
  return total > 0 ? (resolvedCount / total) * 100 : 0
})

const avgSatisfactionNum = computed(() => {
  return analysisStats.value?.avgSatisfaction ? Number(analysisStats.value.avgSatisfaction) : 0
})

async function loadTickets() {
  const [ticketRes, statsRes] = await Promise.all([
    listAfterSalesTickets(filterProjectId.value),
    getAfterSalesStats(filterProjectId.value),
  ])
  tickets.value = ticketRes.data.data
  ticketStats.value = statsRes.data.data
}

function handleCreateOpen() {
  createForm.value = { title: '', description: '', channel: '', intentType: '', reporterName: '', reporterContact: '' }
  showCreateDialog.value = true
}

async function handleCreateSubmit() {
  const valid = await createFormRef.value?.validate().catch(() => false)
  if (!valid) return
  creating.value = true
  try {
    await createAfterSalesTicket({ projectId: filterProjectId.value, ...createForm.value })
    ElMessage.success('工单创建成功')
    showCreateDialog.value = false
    await loadTickets()
  } finally {
    creating.value = false
  }
}

function handleResolveOpen(row: any) {
  currentTicketId.value = row.id
  resolveForm.value = { resolution: '', knowledgeCreated: false }
  showResolveDialog.value = true
}

async function handleResolveSubmit() {
  const valid = await resolveFormRef.value?.validate().catch(() => false)
  if (!valid) return
  resolving.value = true
  try {
    await resolveAfterSalesTicket(currentTicketId.value, {
      resolution: resolveForm.value.resolution,
      knowledgeCreated: resolveForm.value.knowledgeCreated,
    })
    ElMessage.success('工单已解决')
    showResolveDialog.value = false
    await loadTickets()
  } finally {
    resolving.value = false
  }
}

function handleRateOpen(row: any) {
  currentTicketId.value = row.id
  rateScore.value = 5
  showRateDialog.value = true
}

async function handleRateSubmit() {
  rating.value = true
  try {
    await rateAfterSalesTicket(currentTicketId.value, rateScore.value)
    ElMessage.success('评价成功')
    showRateDialog.value = false
    await loadTickets()
  } finally {
    rating.value = false
  }
}

async function loadHealth() {
  const [latestRes, historyRes] = await Promise.all([
    getLatestHealth(healthProjectId.value),
    getHealthHistory(healthProjectId.value),
  ])
  healthData.value = latestRes.data.data
  healthHistory.value = historyRes.data.data ?? []
}

async function handleHealthCheck() {
  const res = await checkCustomerHealth(healthProjectId.value)
  healthData.value = res.data.data
  ElMessage.success('健康检查完成')
  const historyRes = await getHealthHistory(healthProjectId.value)
  healthHistory.value = historyRes.data.data ?? []
}

async function loadStatsTab() {
  const res = await getAfterSalesStats(statsProjectId.value)
  analysisStats.value = res.data.data
  const ticketRes = await listAfterSalesTickets(statsProjectId.value)
  tickets.value = ticketRes.data.data
}
</script>

<style scoped>
.stat-card {
  background: #f0f9ff;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
  border: 1px solid #e0e7ff;
}

.stat-card.stat-sla {
  background: #faf5ff;
  border-color: #e9d5ff;
}

.stat-card.stat-sla .stat-value {
  font-size: 14px;
  font-weight: 600;
}

.stat-card.stat-satisfaction {
  background: #fffbeb;
  border-color: #fde68a;
}

.stat-card.stat-resolution {
  background: #f0fdf4;
  border-color: #bbf7d0;
}

.stat-card.stat-open {
  background: #fffbeb;
  border-color: #fde68a;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #64748b;
}

.health-score-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  text-align: center;
  border: 2px solid #e5e7eb;
}

.health-score {
  font-size: 56px;
  font-weight: 800;
  line-height: 1.2;
}

.health-label {
  font-size: 14px;
  color: #64748b;
  margin-top: 4px;
}

.metric-card {
  background: #f8fafc;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
  border: 1px solid #e2e8f0;
}

.metric-value {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 4px;
}

.metric-label {
  font-size: 12px;
  color: #94a3b8;
}
</style>
