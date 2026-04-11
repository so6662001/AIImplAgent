<template>
  <div>
    <div class="page-header">
      <h2>智能录入管理</h2>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- Tab 1: 视频知识库 -->
      <el-tab-pane label="视频知识库" name="videos">
        <div class="card" style="margin-bottom: 20px">
          <el-form inline>
            <el-form-item label="模块">
              <el-select v-model="videoModuleFilter" clearable placeholder="全部模块" @change="loadVideos">
                <el-option v-for="m in moduleOptions" :key="m" :label="m" :value="m" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadVideos">查询</el-button>
              <el-button type="success" @click="videoDialogVisible = true; resetVideoForm()">新增视频</el-button>
            </el-form-item>
          </el-form>
        </div>

        <el-row :gutter="16">
          <el-col :span="8" v-for="video in videoList" :key="video.id" style="margin-bottom: 16px">
            <el-card
              shadow="hover"
              class="video-card"
              :class="{ active: expandedVideoId === video.id }"
              @click="toggleVideo(video)"
            >
              <template #header>
                <div class="video-card-header">
                  <span class="video-title">{{ video.title }}</span>
                  <el-tag size="small" type="info">{{ video.module }}</el-tag>
                </div>
              </template>
              <div class="video-meta">
                <span>功能: {{ video.functionName }}</span>
                <span>时长: {{ formatDuration(video.duration) }}</span>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <template v-if="expandedVideoId">
          <div class="card" style="margin-top: 20px">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px">
              <h4 style="margin: 0">视频切片列表</h4>
              <el-button type="success" size="small" @click="clipDialogVisible = true; resetClipForm()">新增切片</el-button>
            </div>
            <el-table :data="clipList" stripe border>
              <el-table-column prop="clipTitle" label="片段标题" min-width="140" />
              <el-table-column label="时间范围" width="140">
                <template #default="{ row }">{{ row.startSecond }}s - {{ row.endSecond }}s</template>
              </el-table-column>
              <el-table-column prop="relatedPage" label="关联页面" width="140" />
              <el-table-column prop="relatedField" label="关联字段" width="140" />
            </el-table>
          </div>
        </template>
      </el-tab-pane>

      <!-- Tab 2: 字段帮助配置 -->
      <el-tab-pane label="字段帮助配置" name="fieldHelp">
        <div class="card" style="margin-bottom: 20px">
          <el-form inline>
            <el-form-item label="页面">
              <el-input v-model="helpPageFilter" placeholder="如 采购入库单" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadFieldHelps">查询</el-button>
              <el-button type="success" @click="helpDialogVisible = true; resetHelpForm()">新增帮助</el-button>
            </el-form-item>
          </el-form>
        </div>

        <el-table :data="helpList" stripe border style="margin-bottom: 20px">
          <el-table-column prop="page" label="页面" width="140" />
          <el-table-column prop="fieldName" label="字段名" width="120" />
          <el-table-column prop="helpText" label="帮助文本" min-width="200" show-overflow-tooltip />
          <el-table-column prop="formatExample" label="格式示例" width="160" show-overflow-tooltip />
          <el-table-column prop="commonErrors" label="常见错误" width="180" show-overflow-tooltip />
        </el-table>

        <div class="card">
          <h4 style="margin-top: 0">测试查询</h4>
          <el-form inline>
            <el-form-item label="页面">
              <el-input v-model="lookupPage" placeholder="页面名称" />
            </el-form-item>
            <el-form-item label="字段">
              <el-input v-model="lookupField" placeholder="字段名" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="doLookup">查询</el-button>
            </el-form-item>
          </el-form>
          <el-descriptions v-if="lookupResult" border :column="1" style="margin-top: 12px">
            <el-descriptions-item label="页面">{{ lookupResult.page }}</el-descriptions-item>
            <el-descriptions-item label="字段">{{ lookupResult.fieldName }}</el-descriptions-item>
            <el-descriptions-item label="帮助文本">{{ lookupResult.helpText }}</el-descriptions-item>
            <el-descriptions-item label="格式示例">{{ lookupResult.formatExample }}</el-descriptions-item>
            <el-descriptions-item label="常见错误">{{ lookupResult.commonErrors }}</el-descriptions-item>
          </el-descriptions>
          <el-empty v-if="lookupQueried && !lookupResult" description="未找到匹配的帮助内容" />
        </div>
      </el-tab-pane>

      <!-- Tab 3: 录入辅助统计 -->
      <el-tab-pane label="录入辅助统计" name="stats">
        <div class="card" style="margin-bottom: 20px">
          <el-form inline>
            <el-form-item label="页面">
              <el-input v-model="statsPageFilter" placeholder="如 采购入库单" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="statsLoading" @click="loadStats">查询统计</el-button>
            </el-form-item>
          </el-form>
        </div>

        <template v-if="statsData">
          <el-row :gutter="16" style="margin-bottom: 20px">
            <el-col :span="8">
              <el-card shadow="hover">
                <div class="stat-value">{{ statsData.totalAssists }}</div>
                <div class="stat-label">总辅助次数</div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover">
                <div class="stat-label" style="margin-bottom: 8px">解决率</div>
                <el-progress
                  :percentage="Number((statsData.resolvedRate * 100).toFixed(1))"
                  :stroke-width="18"
                  :text-inside="true"
                  :status="statsData.resolvedRate >= 0.8 ? 'success' : ''"
                />
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover">
                <div class="stat-value">{{ Object.keys(statsData.byTriggerType || {}).length }}</div>
                <div class="stat-label">触发类型数</div>
              </el-card>
            </el-col>
          </el-row>

          <el-row :gutter="16" style="margin-bottom: 20px">
            <el-col :span="4" v-for="(count, type) in (statsData.byTriggerType || {})" :key="type">
              <el-card shadow="hover" class="trigger-card">
                <div class="stat-value">{{ count }}</div>
                <div class="stat-label">{{ triggerTypeLabel(type as string) }}</div>
              </el-card>
            </el-col>
          </el-row>

          <div class="card" style="margin-bottom: 20px" v-if="statsData.topFields && statsData.topFields.length">
            <h4 style="margin-top: 0">最常求助字段</h4>
            <el-table :data="statsData.topFields" stripe border>
              <el-table-column type="index" label="排名" width="80" />
              <el-table-column prop="field" label="字段" />
              <el-table-column prop="count" label="次数" width="120" />
            </el-table>
          </div>
        </template>

        <div class="card" v-if="statsLogs.length">
          <h4 style="margin-top: 0">最近辅助日志</h4>
          <el-table :data="statsLogs" stripe border>
            <el-table-column prop="page" label="页面" width="140" />
            <el-table-column prop="field" label="字段" width="120" />
            <el-table-column label="触发类型" width="130">
              <template #default="{ row }">
                <el-tag :type="triggerTagType(row.triggerType)" size="small">{{ triggerTypeLabel(row.triggerType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="question" label="问题" min-width="180" show-overflow-tooltip />
            <el-table-column label="已解决" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.resolved ? 'success' : 'danger'" size="small">{{ row.resolved ? '是' : '否' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="时间" width="170" />
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 新增视频 Dialog -->
    <el-dialog v-model="videoDialogVisible" title="新增视频" width="520px">
      <el-form :model="videoForm" label-width="100px">
        <el-form-item label="模块" required>
          <el-select v-model="videoForm.module" placeholder="选择模块">
            <el-option v-for="m in moduleOptions" :key="m" :label="m" :value="m" />
          </el-select>
        </el-form-item>
        <el-form-item label="功能名称" required>
          <el-input v-model="videoForm.functionName" />
        </el-form-item>
        <el-form-item label="标题" required>
          <el-input v-model="videoForm.title" />
        </el-form-item>
        <el-form-item label="视频地址" required>
          <el-input v-model="videoForm.videoUrl" />
        </el-form-item>
        <el-form-item label="时长(秒)" required>
          <el-input-number v-model="videoForm.duration" :min="1" controls-position="right" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="videoDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="videoSaving" @click="submitVideo">确定</el-button>
      </template>
    </el-dialog>

    <!-- 新增切片 Dialog -->
    <el-dialog v-model="clipDialogVisible" title="新增切片" width="520px">
      <el-form :model="clipForm" label-width="100px">
        <el-form-item label="片段标题" required>
          <el-input v-model="clipForm.clipTitle" />
        </el-form-item>
        <el-form-item label="开始秒数" required>
          <el-input-number v-model="clipForm.startSecond" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="结束秒数" required>
          <el-input-number v-model="clipForm.endSecond" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="关联页面">
          <el-input v-model="clipForm.relatedPage" />
        </el-form-item>
        <el-form-item label="关联字段">
          <el-input v-model="clipForm.relatedField" />
        </el-form-item>
        <el-form-item label="操作步骤">
          <el-input v-model="clipForm.operationStep" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="clipDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="clipSaving" @click="submitClip">确定</el-button>
      </template>
    </el-dialog>

    <!-- 新增帮助 Dialog -->
    <el-dialog v-model="helpDialogVisible" title="新增字段帮助" width="560px">
      <el-form :model="helpForm" label-width="100px">
        <el-form-item label="页面" required>
          <el-input v-model="helpForm.page" placeholder="如 采购入库单" />
        </el-form-item>
        <el-form-item label="字段名" required>
          <el-input v-model="helpForm.fieldName" />
        </el-form-item>
        <el-form-item label="帮助文本" required>
          <el-input v-model="helpForm.helpText" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="格式示例">
          <el-input v-model="helpForm.formatExample" />
        </el-form-item>
        <el-form-item label="常见错误">
          <el-input v-model="helpForm.commonErrors" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="helpDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="helpSaving" @click="submitHelp">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createVideo, listVideos, createClip, listClips } from '@/api/videolibrary'
import { createFieldHelp, getPageHelps, lookupFieldHelp } from '@/api/fieldhelp'
import { getAssistStats, listAssistLogs } from '@/api/inputassist'

const activeTab = ref('videos')

const moduleOptions = ['采购管理', '销售管理', '库存管理', '基础档案', '财务管理', '仓储管理']

// ====== Tab 1: 视频知识库 ======
const videoModuleFilter = ref('')
const videoList = ref<any[]>([])
const expandedVideoId = ref<number | null>(null)
const clipList = ref<any[]>([])

const videoDialogVisible = ref(false)
const videoSaving = ref(false)
const videoForm = ref({ module: '', functionName: '', title: '', videoUrl: '', duration: 60 })

const clipDialogVisible = ref(false)
const clipSaving = ref(false)
const clipForm = ref({ clipTitle: '', startSecond: 0, endSecond: 30, relatedPage: '', relatedField: '', operationStep: '' })

function resetVideoForm() {
  videoForm.value = { module: '', functionName: '', title: '', videoUrl: '', duration: 60 }
}

function resetClipForm() {
  clipForm.value = { clipTitle: '', startSecond: 0, endSecond: 30, relatedPage: '', relatedField: '', operationStep: '' }
}

function formatDuration(seconds: number) {
  if (!seconds) return '0:00'
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}:${s.toString().padStart(2, '0')}`
}

async function loadVideos() {
  const res = await listVideos(videoModuleFilter.value || undefined)
  videoList.value = res.data.data
}

async function toggleVideo(video: any) {
  if (expandedVideoId.value === video.id) {
    expandedVideoId.value = null
    clipList.value = []
    return
  }
  expandedVideoId.value = video.id
  const res = await listClips(video.id)
  clipList.value = res.data.data
}

async function submitVideo() {
  videoSaving.value = true
  try {
    await createVideo(videoForm.value as unknown as Record<string, unknown>)
    ElMessage.success('视频创建成功')
    videoDialogVisible.value = false
    await loadVideos()
  } finally {
    videoSaving.value = false
  }
}

async function submitClip() {
  if (!expandedVideoId.value) return
  clipSaving.value = true
  try {
    await createClip(expandedVideoId.value, clipForm.value as unknown as Record<string, unknown>)
    ElMessage.success('切片创建成功')
    clipDialogVisible.value = false
    const res = await listClips(expandedVideoId.value)
    clipList.value = res.data.data
  } finally {
    clipSaving.value = false
  }
}

// ====== Tab 2: 字段帮助配置 ======
const helpPageFilter = ref('')
const helpList = ref<any[]>([])
const helpDialogVisible = ref(false)
const helpSaving = ref(false)
const helpForm = ref({ page: '', fieldName: '', helpText: '', formatExample: '', commonErrors: '' })

const lookupPage = ref('')
const lookupField = ref('')
const lookupResult = ref<any>(null)
const lookupQueried = ref(false)

function resetHelpForm() {
  helpForm.value = { page: '', fieldName: '', helpText: '', formatExample: '', commonErrors: '' }
}

async function loadFieldHelps() {
  if (!helpPageFilter.value) {
    ElMessage.warning('请输入页面名称')
    return
  }
  const res = await getPageHelps(helpPageFilter.value)
  helpList.value = res.data.data
}

async function submitHelp() {
  helpSaving.value = true
  try {
    await createFieldHelp(helpForm.value as unknown as Record<string, unknown>)
    ElMessage.success('帮助内容创建成功')
    helpDialogVisible.value = false
    if (helpPageFilter.value) await loadFieldHelps()
  } finally {
    helpSaving.value = false
  }
}

async function doLookup() {
  if (!lookupPage.value || !lookupField.value) {
    ElMessage.warning('请输入页面和字段')
    return
  }
  lookupQueried.value = true
  const res = await lookupFieldHelp(lookupPage.value, lookupField.value)
  lookupResult.value = res.data.data
}

// ====== Tab 3: 录入辅助统计 ======
const statsPageFilter = ref('')
const statsLoading = ref(false)
const statsData = ref<any>(null)
const statsLogs = ref<any[]>([])

const triggerTypeLabels: Record<string, string> = {
  HELP_CLICK: '点击帮助',
  FIELD_DWELL: '字段停留',
  REPEATED_EDIT: '反复修改',
  VALIDATION_FAIL: '校验失败',
  FIRST_USE: '首次使用',
}

function triggerTypeLabel(type: string) {
  return triggerTypeLabels[type] || type
}

function triggerTagType(type: string): '' | 'success' | 'warning' | 'danger' | 'info' {
  const map: Record<string, '' | 'success' | 'warning' | 'danger' | 'info'> = {
    HELP_CLICK: 'info',
    FIELD_DWELL: 'warning',
    REPEATED_EDIT: 'danger',
    VALIDATION_FAIL: 'danger',
    FIRST_USE: 'success',
  }
  return map[type] || 'info'
}

async function loadStats() {
  if (!statsPageFilter.value) {
    ElMessage.warning('请输入页面名称')
    return
  }
  statsLoading.value = true
  try {
    const [statsRes, logsRes] = await Promise.all([
      getAssistStats(statsPageFilter.value),
      listAssistLogs(statsPageFilter.value),
    ])
    statsData.value = statsRes.data.data
    statsLogs.value = logsRes.data.data
  } finally {
    statsLoading.value = false
  }
}

loadVideos()
</script>

<style scoped>
.page-header {
  margin-bottom: 20px;
}
.page-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
}
.card {
  background: #fff;
  border-radius: 8px;
  padding: 16px 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}
.video-card {
  cursor: pointer;
  transition: border-color 0.2s;
}
.video-card.active {
  border-color: var(--el-color-primary);
}
.video-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.video-title {
  font-weight: 600;
  font-size: 15px;
}
.video-meta {
  display: flex;
  justify-content: space-between;
  color: #666;
  font-size: 13px;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--el-color-primary);
  text-align: center;
}
.stat-label {
  font-size: 13px;
  color: #888;
  text-align: center;
  margin-top: 4px;
}
.trigger-card {
  text-align: center;
}
</style>
