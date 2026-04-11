<template>
  <div>
    <div class="page-header">
      <h2>培训考核</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showExamDialog = true">录入考核</el-button>
        <el-button @click="showCheckDialog = true">上线准入检查</el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab" type="border-card" style="margin-bottom: 20px;">
      <!-- 考核记录 tab -->
      <el-tab-pane label="考核记录" name="records">
        <div class="card" style="margin-bottom: 20px;">
          <h3 style="margin-bottom: 16px; font-size: 16px;">上线准入规则</h3>
          <el-alert type="warning" :closable="false" show-icon>
            <template #title>
              KA（关键用户）必须通过所有必学课程考核（≥70分），否则系统将<strong>阻止推进到上线阶段</strong>。
            </template>
          </el-alert>
        </div>

        <div class="card">
          <h3 style="margin-bottom: 16px; font-size: 16px;">考核记录</h3>
          <el-empty v-if="!examRecords.length" description="暂无考核记录" />
          <el-table v-else :data="examRecords" stripe>
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="module" label="考核模块" width="160" />
            <el-table-column prop="examType" label="考核类型" width="100" />
            <el-table-column prop="score" label="分数" width="80">
              <template #default="{ row }">
                <span :class="row.passed ? 'score-pass' : 'score-fail'">{{ row.score }}</span>
              </template>
            </el-table-column>
            <el-table-column label="通过" width="80">
              <template #default="{ row }">
                <el-tag :type="row.passed ? 'success' : 'danger'" size="small">
                  {{ row.passed ? '通过' : '未通过' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="必学课程" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.requiredCourse" type="warning" size="small">★必学</el-tag>
                <span v-else class="text-secondary">选学</span>
              </template>
            </el-table-column>
            <el-table-column prop="weakPoints" label="薄弱点" min-width="200" />
          </el-table>
        </div>
      </el-tab-pane>

      <!-- AI出题 tab -->
      <el-tab-pane label="AI出题" name="aiExam">
        <div class="card" style="margin-bottom: 20px;">
          <h3 style="margin-bottom: 16px; font-size: 16px;">智能试卷生成</h3>
          <el-form :inline="true" :model="aiExamForm">
            <el-form-item label="项目ID">
              <el-input-number v-model="aiExamForm.projectId" :min="1" controls-position="right" />
            </el-form-item>
            <el-form-item label="考核模块">
              <el-select v-model="aiExamForm.module" placeholder="请选择模块" style="width: 160px;">
                <el-option label="采购管理" value="采购管理" />
                <el-option label="销售管理" value="销售管理" />
                <el-option label="库存管理" value="库存管理" />
                <el-option label="财务应收" value="财务应收" />
                <el-option label="基础档案" value="基础档案" />
              </el-select>
            </el-form-item>
            <el-form-item label="难度级别">
              <el-select v-model="aiExamForm.difficulty" placeholder="请选择难度" style="width: 120px;">
                <el-option label="基础级" value="基础级" />
                <el-option label="进阶级" value="进阶级" />
                <el-option label="综合级" value="综合级" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="aiExamLoading" @click="handleGenerateExam">生成试卷</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div v-if="generatedExam" class="card">
          <div style="display: flex; align-items: center; gap: 16px; margin-bottom: 20px;">
            <h3 style="margin: 0;">{{ generatedExam.module }} - {{ generatedExam.difficulty }}</h3>
            <el-tag>共 {{ generatedExam.totalQuestions }} 题</el-tag>
            <el-tag type="success">总分 {{ generatedExam.totalScore }} 分</el-tag>
          </div>

          <div v-for="(q, idx) in (generatedExam.questions as any[])" :key="q.questionId" class="question-item">
            <div class="question-header">
              <span class="question-num">{{ Number(idx) + 1 }}.</span>
              <el-tag
                :type="questionTypeBadge(q.questionType).type"
                size="small"
                style="margin-right: 8px;"
              >{{ questionTypeBadge(q.questionType).label }}</el-tag>
              <span class="question-text">{{ q.questionText }}</span>
              <span class="question-score">({{ q.score }}分)</span>
            </div>

            <div v-if="q.questionType === 'CHOICE' && q.options && q.options.length" class="question-options">
              <div v-for="(opt, oIdx) in (q.options as string[])" :key="oIdx" class="option-item">
                {{ String.fromCharCode(65 + Number(oIdx)) }}. {{ opt.replace(/^[A-D]\./, '') }}
              </div>
            </div>

            <div class="question-footer">
              <el-button size="small" link type="primary" @click="toggleAnswer(Number(idx))">
                {{ visibleAnswers[Number(idx)] ? '隐藏答案' : '显示答案' }}
              </el-button>
              <span v-if="visibleAnswers[Number(idx)]" class="answer-text">
                答案：{{ q.correctAnswer }}
              </span>
              <span v-if="q.hint" class="hint-text">
                提示：{{ q.hint }}
              </span>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- 薄弱点分析 tab -->
      <el-tab-pane label="薄弱点分析" name="weakPoints">
        <div class="card" style="margin-bottom: 20px;">
          <h3 style="margin-bottom: 16px; font-size: 16px;">学员薄弱点分析</h3>
          <el-form :inline="true" :model="weakPointForm">
            <el-form-item label="项目ID">
              <el-input-number v-model="weakPointForm.projectId" :min="1" controls-position="right" />
            </el-form-item>
            <el-form-item label="学员ID">
              <el-input-number v-model="weakPointForm.traineeId" :min="1" controls-position="right" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="weakPointLoading" @click="handleAnalyzeWeakPoints">分析</el-button>
            </el-form-item>
          </el-form>
        </div>

        <template v-if="weakPointResult">
          <div class="card" style="margin-bottom: 20px;">
            <h3 style="margin-bottom: 16px; font-size: 16px;">综合概览</h3>
            <el-row :gutter="16">
              <el-col :span="8">
                <div style="text-align: center;">
                  <div style="font-size: 24px; font-weight: 700;">{{ weakPointResult.totalModules }}</div>
                  <div style="color: #888;">总模块数</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div style="text-align: center;">
                  <div style="font-size: 24px; font-weight: 700; color: #67c23a;">{{ weakPointResult.passedModules }}</div>
                  <div style="color: #888;">已通过模块</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div style="text-align: center;">
                  <el-progress
                    type="circle"
                    :percentage="weakPointResult.totalModules > 0 ? Math.round(weakPointResult.passedModules * 100 / weakPointResult.totalModules) : 0"
                    :width="80"
                  />
                  <div style="color: #888; margin-top: 4px;">通过率</div>
                </div>
              </el-col>
            </el-row>
          </div>

          <div v-if="weakPointResult.weakModules && weakPointResult.weakModules.length" class="card" style="margin-bottom: 20px;">
            <h3 style="margin-bottom: 16px; font-size: 16px;">薄弱模块</h3>
            <el-table :data="weakPointResult.weakModules" stripe>
              <el-table-column prop="module" label="模块" min-width="160" />
              <el-table-column prop="bestScore" label="最高分" width="100">
                <template #default="{ row }">
                  <span :style="{ color: row.bestScore < 70 ? '#ef4444' : '#10b981', fontWeight: 600 }">
                    {{ row.bestScore }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="attempts" label="考核次数" width="100" />
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === '未通过' ? 'danger' : 'info'" size="small">
                    {{ row.status }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div v-if="weakPointResult.remediationPlan && weakPointResult.remediationPlan.length" class="card">
            <h3 style="margin-bottom: 16px; font-size: 16px;">补学计划</h3>
            <el-row :gutter="16">
              <el-col v-for="(item, idx) in weakPointResult.remediationPlan" :key="idx" :span="8" style="margin-bottom: 16px;">
                <el-card shadow="hover">
                  <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 8px;">
                    <span style="font-size: 20px;">{{ typeIcon(item.type) }}</span>
                    <span style="font-weight: 600;">{{ item.title }}</span>
                    <el-tag :type="item.priority === 'HIGH' ? 'danger' : 'warning'" size="small">
                      {{ item.priority }}
                    </el-tag>
                  </div>
                  <p style="color: #666; font-size: 13px; margin: 0;">{{ item.description }}</p>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </template>
      </el-tab-pane>
    </el-tabs>

    <!-- 录入考核弹窗 -->
    <el-dialog v-model="showExamDialog" title="录入考核" width="560px" destroy-on-close>
      <el-form ref="examFormRef" :model="examForm" :rules="examRules" label-width="100px">
        <el-form-item label="项目ID" prop="projectId">
          <el-input-number v-model="examForm.projectId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="学员ID" prop="traineeId">
          <el-input-number v-model="examForm.traineeId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="考核模块" prop="module">
          <el-select v-model="examForm.module" placeholder="请选择" filterable allow-create>
            <el-option label="系统基础操作" value="系统基础操作" />
            <el-option label="采购管理" value="采购管理" />
            <el-option label="销售管理" value="销售管理" />
            <el-option label="库存管理" value="库存管理" />
            <el-option label="财务应收" value="财务应收" />
            <el-option label="财务应付" value="财务应付" />
            <el-option label="仓储管理" value="仓储管理" />
            <el-option label="MES-生产工单" value="MES-生产工单" />
            <el-option label="报表与看板" value="报表与看板" />
          </el-select>
        </el-form-item>
        <el-form-item label="考核类型" prop="examType">
          <el-select v-model="examForm.examType" placeholder="请选择">
            <el-option label="基础级" value="基础级" />
            <el-option label="进阶级" value="进阶级" />
            <el-option label="综合级" value="综合级" />
            <el-option label="重考" value="重考" />
          </el-select>
        </el-form-item>
        <el-form-item label="考核分数" prop="score">
          <el-input-number v-model="examForm.score" :min="0" :max="100" controls-position="right" />
          <span class="form-tip" style="margin-left: 12px;">
            ≥70分通过
          </span>
        </el-form-item>
        <el-form-item label="必学课程" prop="requiredCourse">
          <el-switch v-model="examForm.requiredCourse" active-text="是" inactive-text="否" />
        </el-form-item>
        <el-form-item label="薄弱点">
          <el-input v-model="examForm.weakPoints" type="textarea" :rows="2" placeholder="如：盘盈盘亏处理流程" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showExamDialog = false">取消</el-button>
        <el-button type="primary" :loading="examSubmitting" @click="handleSubmitExam">提交</el-button>
      </template>
    </el-dialog>

    <!-- 上线准入检查弹窗 -->
    <el-dialog v-model="showCheckDialog" title="上线准入检查" width="480px">
      <el-form label-width="80px">
        <el-form-item label="项目ID">
          <el-input-number v-model="checkProjectId" :min="1" controls-position="right" />
        </el-form-item>
      </el-form>
      <div v-if="checkResult !== null" style="margin-top: 16px;">
        <el-result
          :icon="checkResult.goLiveReady ? 'success' : 'error'"
          :title="checkResult.goLiveReady ? '允许上线' : '不允许上线'"
          :sub-title="checkResult.message"
        />
      </div>
      <template #footer>
        <el-button @click="showCheckDialog = false">关闭</el-button>
        <el-button type="primary" :loading="checking" @click="handleCheck">检查</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import { submitExam, checkGoLive, generateExam, getWeakPoints } from '@/api/training'
import type { ExamRecord, GoLiveCheck } from '@/types'
import { required, scoreRule } from '@/utils/validators'

const activeTab = ref('records')

const examRecords = ref<ExamRecord[]>([])
const showExamDialog = ref(false)
const showCheckDialog = ref(false)
const examSubmitting = ref(false)
const checking = ref(false)
const examFormRef = ref<FormInstance>()
const checkProjectId = ref(1)
const checkResult = ref<GoLiveCheck | null>(null)

const examForm = ref({
  projectId: undefined as number | undefined,
  traineeId: undefined as number | undefined,
  module: '',
  examType: '',
  score: undefined as number | undefined,
  requiredCourse: true,
  weakPoints: '',
})

const examRules = {
  projectId: [required('项目ID不能为空')],
  traineeId: [required('学员ID不能为空')],
  module: [required('考核模块不能为空')],
  examType: [required('考核类型不能为空')],
  score: [scoreRule],
}

async function handleSubmitExam() {
  const valid = await examFormRef.value?.validate().catch(() => false)
  if (!valid) return

  examSubmitting.value = true
  try {
    const res = await submitExam(examForm.value as unknown as Record<string, unknown>)
    const record = res.data.data
    examRecords.value.unshift(record)
    ElMessage.success(record.passed ? '考核通过！' : '考核未通过，请安排补学')
    showExamDialog.value = false
  } finally {
    examSubmitting.value = false
  }
}

async function handleCheck() {
  checking.value = true
  checkResult.value = null
  try {
    const res = await checkGoLive(checkProjectId.value)
    checkResult.value = res.data.data
  } finally {
    checking.value = false
  }
}

// AI出题
const aiExamForm = reactive({
  projectId: 1,
  module: '采购管理',
  difficulty: '基础级',
})
const aiExamLoading = ref(false)
const generatedExam = ref<any>(null)
const visibleAnswers = ref<Record<number, boolean>>({})

function questionTypeBadge(type: string) {
  const map: Record<string, { type: string; label: string }> = {
    CHOICE: { type: '', label: '选择题' },
    TRUE_FALSE: { type: 'success', label: '判断题' },
    OPERATION: { type: 'warning', label: '操作题' },
    SCENARIO: { type: 'danger', label: '场景题' },
  }
  return map[type] || { type: 'info', label: type }
}

function toggleAnswer(idx: number) {
  visibleAnswers.value[idx] = !visibleAnswers.value[idx]
}

async function handleGenerateExam() {
  if (!aiExamForm.module || !aiExamForm.difficulty) {
    ElMessage.warning('请选择考核模块和难度级别')
    return
  }
  aiExamLoading.value = true
  generatedExam.value = null
  visibleAnswers.value = {}
  try {
    const res = await generateExam({
      projectId: aiExamForm.projectId,
      module: aiExamForm.module,
      difficulty: aiExamForm.difficulty,
    })
    generatedExam.value = res.data.data
  } finally {
    aiExamLoading.value = false
  }
}

// 薄弱点分析
const weakPointForm = reactive({
  projectId: 1,
  traineeId: 1,
})
const weakPointLoading = ref(false)
const weakPointResult = ref<any>(null)

function typeIcon(type: string) {
  const map: Record<string, string> = {
    VIDEO: '▶',
    DOCUMENT: '📄',
    PRACTICE: '📝',
  }
  return map[type] || '📋'
}

async function handleAnalyzeWeakPoints() {
  weakPointLoading.value = true
  weakPointResult.value = null
  try {
    const res = await getWeakPoints(weakPointForm.projectId, weakPointForm.traineeId)
    weakPointResult.value = res.data.data
  } finally {
    weakPointLoading.value = false
  }
}
</script>

<style scoped>
.header-actions {
  display: flex;
  gap: 8px;
}
.score-pass { color: #10b981; font-weight: 600; }
.score-fail { color: #ef4444; font-weight: 600; }
.text-secondary { color: var(--text-secondary); font-size: 13px; }
.form-tip { font-size: 12px; color: var(--text-secondary); }

.question-item {
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 12px;
}
.question-header {
  display: flex;
  align-items: flex-start;
  gap: 4px;
  flex-wrap: wrap;
}
.question-num {
  font-weight: 700;
  min-width: 24px;
}
.question-text {
  flex: 1;
  line-height: 1.6;
}
.question-score {
  color: #999;
  font-size: 13px;
  white-space: nowrap;
}
.question-options {
  margin: 12px 0 0 28px;
}
.option-item {
  padding: 4px 0;
  color: #555;
}
.question-footer {
  margin-top: 12px;
  padding-top: 8px;
  border-top: 1px dashed #ebeef5;
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}
.answer-text {
  color: #67c23a;
  font-weight: 600;
  font-size: 13px;
}
.hint-text {
  color: #e6a23c;
  font-size: 13px;
}
</style>
