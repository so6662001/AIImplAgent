<template>
  <div>
    <div class="page-header">
      <h2>现场辅助</h2>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- Tab 1: 工单管理 -->
      <el-tab-pane label="工单管理" name="tickets">
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

        <!-- Stats -->
        <el-row :gutter="16" style="margin-bottom: 20px;" v-if="stats">
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-value">{{ stats.total ?? 0 }}</div>
              <div class="stat-label">工单总数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card stat-open">
              <div class="stat-value">{{ stats.open ?? 0 }}</div>
              <div class="stat-label">待处理</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card stat-resolved">
              <div class="stat-value">{{ stats.resolved ?? 0 }}</div>
              <div class="stat-label">已解决</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card stat-level">
              <div class="stat-value" v-if="stats.byLevel">
                L1:{{ stats.byLevel.L1 }} / L2:{{ stats.byLevel.L2 }} / L3:{{ stats.byLevel.L3 }} / L4:{{ stats.byLevel.L4 }}
              </div>
              <div class="stat-label">按级别分布</div>
            </div>
          </el-col>
        </el-row>

        <div class="card">
          <el-table :data="tickets" stripe row-key="id">
            <el-table-column type="expand">
              <template #default="{ row }">
                <div style="padding: 12px 24px;">
                  <p><strong>问题描述：</strong>{{ row.description }}</p>
                  <div v-if="row.aiSuggestion" class="ai-suggestion-box">
                    <strong>🤖 AI建议：</strong>{{ row.aiSuggestion }}
                  </div>
                  <p v-if="row.resolution"><strong>处理结果：</strong>{{ row.resolution }}</p>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="title" label="标题" min-width="160" />
            <el-table-column label="级别" width="80">
              <template #default="{ row }">
                <el-tag :type="levelTagType(row.level)" size="small">{{ row.level }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="category" label="分类" width="100" />
            <el-table-column label="优先级" width="90">
              <template #default="{ row }">
                <el-tag :type="priorityTagType(row.priority)" size="small">{{ row.priority }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 'RESOLVED' ? 'success' : (row.status === 'OPEN' ? 'warning' : 'info')" size="small">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="assignedTo" label="指派" width="100" />
            <el-table-column prop="createTime" label="创建时间" width="160" />
            <el-table-column label="操作" width="80" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.status === 'OPEN'" type="primary" link size="small" @click="handleResolveOpen(row)">处理</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <!-- Tab 2: 系统预警 -->
      <el-tab-pane label="系统预警" name="alerts">
        <div class="card" style="margin-bottom: 20px;">
          <el-form inline>
            <el-form-item label="项目ID">
              <el-input-number v-model="alertProjectId" :min="1" controls-position="right" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadAlerts">查询</el-button>
              <el-button type="warning" @click="handleHealthCheck">执行健康检查</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="card">
          <div v-for="alert in alerts" :key="alert.id" style="margin-bottom: 12px;">
            <el-alert
              :title="severityIcon(alert.severity) + ' ' + alert.title"
              :type="severityAlertType(alert.severity)"
              :closable="false"
              show-icon
            >
              <template #default>
                <p>{{ alert.description }}</p>
                <p v-if="alert.suggestion" style="color: #409eff;"><strong>建议：</strong>{{ alert.suggestion }}</p>
                <p style="font-size: 12px; color: #999;">{{ alert.createTime }}</p>
                <el-button
                  v-if="!alert.acknowledged"
                  type="primary"
                  size="small"
                  style="margin-top: 8px;"
                  @click="handleAcknowledge(alert)"
                >确认</el-button>
              </template>
            </el-alert>
          </div>
          <el-empty v-if="alerts.length === 0" description="暂无告警" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- Create Ticket Dialog -->
    <el-dialog v-model="showCreateDialog" title="新建工单" width="520px" destroy-on-close>
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="createForm.title" maxlength="100" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="createForm.description" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="createForm.category" placeholder="请选择分类">
            <el-option label="操作问题" value="OPERATION" />
            <el-option label="配置问题" value="CONFIG" />
            <el-option label="流程问题" value="PROCESS" />
            <el-option label="系统缺陷" value="BUG" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreateSubmit">提交</el-button>
      </template>
    </el-dialog>

    <!-- Resolve Ticket Dialog -->
    <el-dialog v-model="showResolveDialog" title="处理工单" width="480px" destroy-on-close>
      <el-form ref="resolveFormRef" :model="resolveForm" :rules="resolveRules" label-width="80px">
        <el-form-item label="处理结果" prop="resolution">
          <el-input v-model="resolveForm.resolution" type="textarea" :rows="4" placeholder="请输入处理结果" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showResolveDialog = false">取消</el-button>
        <el-button type="primary" :loading="resolving" @click="handleResolveSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import { createTicket, listTickets, resolveTicket, getTicketStats } from '@/api/supportticket'
import { runHealthCheck, listAlerts, acknowledgeAlert } from '@/api/alert'
import { required } from '@/utils/validators'

const activeTab = ref('tickets')
const filterProjectId = ref(1)
const alertProjectId = ref(1)

const tickets = ref<any[]>([])
const alerts = ref<any[]>([])
const stats = ref<any>(null)

const showCreateDialog = ref(false)
const showResolveDialog = ref(false)
const creating = ref(false)
const resolving = ref(false)
const createFormRef = ref<FormInstance>()
const resolveFormRef = ref<FormInstance>()
const currentTicketId = ref(0)

const createForm = ref({
  title: '',
  description: '',
  category: '',
})

const resolveForm = ref({
  resolution: '',
})

const createRules = {
  title: [required('标题不能为空')],
  description: [required('描述不能为空')],
  category: [required('分类不能为空')],
}

const resolveRules = {
  resolution: [required('处理结果不能为空')],
}

function levelTagType(level: string) {
  const map: Record<string, string> = { L1: '', L2: 'success', L3: 'warning', L4: 'danger' }
  return map[level] || 'info'
}

function priorityTagType(priority: string) {
  const map: Record<string, string> = { URGENT: 'danger', HIGH: 'warning', MEDIUM: 'info', LOW: 'success' }
  return map[priority] || 'info'
}

function severityIcon(severity: string) {
  const map: Record<string, string> = { CRITICAL: '❌', WARNING: '⚠️', INFO: 'ℹ️' }
  return map[severity] || 'ℹ️'
}

function severityAlertType(severity: string): 'error' | 'warning' | 'info' | 'success' {
  const map: Record<string, 'error' | 'warning' | 'info'> = { CRITICAL: 'error', WARNING: 'warning', INFO: 'info' }
  return map[severity] || 'info'
}

async function loadTickets() {
  const [ticketRes, statsRes] = await Promise.all([
    listTickets(filterProjectId.value),
    getTicketStats(filterProjectId.value),
  ])
  tickets.value = ticketRes.data.data
  stats.value = statsRes.data.data
}

function handleCreateOpen() {
  createForm.value = { title: '', description: '', category: '' }
  showCreateDialog.value = true
}

async function handleCreateSubmit() {
  const valid = await createFormRef.value?.validate().catch(() => false)
  if (!valid) return

  creating.value = true
  try {
    await createTicket({
      projectId: filterProjectId.value,
      ...createForm.value,
    })
    ElMessage.success('工单创建成功')
    showCreateDialog.value = false
    await loadTickets()
  } finally {
    creating.value = false
  }
}

function handleResolveOpen(row: any) {
  currentTicketId.value = row.id
  resolveForm.value = { resolution: '' }
  showResolveDialog.value = true
}

async function handleResolveSubmit() {
  const valid = await resolveFormRef.value?.validate().catch(() => false)
  if (!valid) return

  resolving.value = true
  try {
    await resolveTicket(currentTicketId.value, resolveForm.value.resolution)
    ElMessage.success('工单处理成功')
    showResolveDialog.value = false
    await loadTickets()
  } finally {
    resolving.value = false
  }
}

async function loadAlerts() {
  const res = await listAlerts(alertProjectId.value)
  alerts.value = res.data.data
}

async function handleHealthCheck() {
  const res = await runHealthCheck(alertProjectId.value)
  alerts.value = res.data.data
  ElMessage.success('健康检查完成')
}

async function handleAcknowledge(alert: any) {
  await acknowledgeAlert(alert.id, 'system')
  ElMessage.success('已确认')
  await loadAlerts()
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

.stat-card.stat-open {
  background: #fffbeb;
  border-color: #fde68a;
}

.stat-card.stat-resolved {
  background: #f0fdf4;
  border-color: #bbf7d0;
}

.stat-card.stat-level {
  background: #faf5ff;
  border-color: #e9d5ff;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 4px;
}

.stat-level .stat-value {
  font-size: 14px;
  font-weight: 600;
}

.stat-label {
  font-size: 13px;
  color: #64748b;
}

.ai-suggestion-box {
  background: #eff6ff;
  border-left: 4px solid #3b82f6;
  padding: 12px 16px;
  margin: 8px 0;
  border-radius: 4px;
  color: #1e40af;
}
</style>
