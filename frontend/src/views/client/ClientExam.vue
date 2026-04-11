<template>
  <div class="exam-page">
    <!-- Results view -->
    <div v-if="showResults && examResult" class="results-view">
      <div class="results-container">
        <div class="results-hero">
          <div class="score-circle" :class="examResult.passed ? 'pass' : 'fail'">
            <span class="score-number">{{ examResult.score }}</span>
            <span class="score-label">分</span>
          </div>
          <el-tag :type="examResult.passed ? 'success' : 'danger'" size="large" effect="dark" class="result-badge">
            {{ examResult.passed ? '🎉 考试通过' : '😔 未通过' }}
          </el-tag>
        </div>

        <div class="results-stats">
          <div class="stat-item">
            <div class="stat-value">{{ examResult.correctCount }}/{{ examResult.totalQuestions }}</div>
            <div class="stat-label">正确题数</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ formatExamDuration(examResult.durationSeconds) }}</div>
            <div class="stat-label">用时</div>
          </div>
        </div>

        <!-- Wrong questions review -->
        <div v-if="examResult.wrongQuestions?.length" class="wrong-review">
          <h3 class="review-title">错题回顾</h3>
          <div v-for="(wq, idx) in (examResult.wrongQuestions as any[])" :key="idx" class="wrong-card">
            <div class="wrong-num">{{ (idx as number) + 1 }}</div>
            <div class="wrong-body">
              <div class="wrong-qtext">{{ wq.questionText }}</div>
              <div class="wrong-answers">
                <div class="answer-row your"><span class="answer-label">你的答案</span><span class="answer-val">{{ wq.yourAnswer || '未作答' }}</span></div>
                <div class="answer-row correct"><span class="answer-label">正确答案</span><span class="answer-val">{{ wq.correctAnswer }}</span></div>
              </div>
            </div>
          </div>
        </div>

        <div class="results-actions">
          <el-button type="primary" size="large" @click="goBackToLearning">返回学习中心</el-button>
        </div>
      </div>
    </div>

    <!-- Exam taking view -->
    <div v-else>
      <!-- Exam header -->
      <header class="exam-topbar">
        <div class="exam-topbar-left">
          <button class="back-btn" @click="confirmLeave">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><polyline points="15 18 9 12 15 6"/></svg>
          </button>
          <div class="exam-title-area">
            <h3 class="exam-name">{{ paper?.title || '在线考试' }}</h3>
            <div class="exam-meta-tags">
              <el-tag v-if="paper?.module" size="small" type="info">{{ paper.module }}</el-tag>
              <el-tag v-if="paper?.difficulty" size="small" :type="difficultyType(paper.difficulty)">{{ paper.difficulty }}</el-tag>
              <span class="question-count" v-if="questions.length">{{ questions.length }} 题</span>
            </div>
          </div>
        </div>
        <div class="exam-topbar-right">
          <div class="timer" :class="{ warning: remainingSeconds < 300 }">
            <span class="timer-icon">⏱️</span>
            <span class="timer-text">{{ formatCountdown(remainingSeconds) }}</span>
          </div>
        </div>
      </header>

      <!-- Loading -->
      <div v-if="loading" class="loading-area">
        <el-icon class="is-loading" :size="24"><Loading /></el-icon>
        <span>加载试卷中...</span>
      </div>

      <!-- Questions -->
      <div v-else class="questions-area">
        <div class="questions-scroll">
          <div
            v-for="(q, idx) in questions"
            :key="q.id || idx"
            class="question-card"
            :id="'q-' + idx"
          >
            <div class="question-header">
              <span class="question-num">{{ idx + 1 }}</span>
              <el-tag size="small" :type="questionTypeColor(q.questionType)">{{ questionTypeLabel(q.questionType) }}</el-tag>
            </div>
            <div class="question-text">{{ q.questionText }}</div>

            <!-- CHOICE -->
            <div v-if="q.questionType === 'CHOICE'" class="question-options">
              <el-radio-group v-model="answers[idx]" class="option-group">
                <el-radio
                  v-for="opt in (q.options || [])"
                  :key="opt.key"
                  :value="opt.key"
                  class="option-item"
                >
                  <span class="option-key">{{ opt.key }}</span>
                  <span class="option-text">{{ opt.value }}</span>
                </el-radio>
              </el-radio-group>
            </div>

            <!-- TRUE_FALSE -->
            <div v-else-if="q.questionType === 'TRUE_FALSE'" class="question-options">
              <el-radio-group v-model="answers[idx]" class="option-group">
                <el-radio value="TRUE" class="option-item">
                  <span class="option-key">✓</span>
                  <span class="option-text">正确</span>
                </el-radio>
                <el-radio value="FALSE" class="option-item">
                  <span class="option-key">✗</span>
                  <span class="option-text">错误</span>
                </el-radio>
              </el-radio-group>
            </div>

            <!-- OPERATION / SCENARIO -->
            <div v-else class="question-textarea">
              <el-input
                v-model="answers[idx]"
                type="textarea"
                :rows="4"
                placeholder="请输入你的答案..."
                resize="vertical"
              />
            </div>
          </div>
        </div>

        <!-- Answer card (quick nav) -->
        <div class="answer-card-panel">
          <div class="answer-card-title">答题卡</div>
          <div class="answer-card-grid">
            <button
              v-for="(_, idx) in questions"
              :key="idx"
              class="answer-dot"
              :class="{ answered: !!answers[idx] }"
              @click="scrollToQuestion(idx)"
            >
              {{ idx + 1 }}
            </button>
          </div>
          <div class="answer-card-summary">
            已答 {{ answeredCount }}/{{ questions.length }}
          </div>
          <el-button
            type="primary"
            size="large"
            :loading="submitting"
            class="submit-btn"
            @click="handleSubmit"
          >
            交卷
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { getPaper, submitExamAnswers } from '@/api/clientexam'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const submitting = ref(false)
const showResults = ref(false)

const paper = ref<any>(null)
const questions = ref<any[]>([])
const answers = ref<Record<number, string>>({})
const examResult = ref<any>(null)

const EXAM_DURATION = 30 * 60
const remainingSeconds = ref(EXAM_DURATION)
let timerInterval: ReturnType<typeof setInterval> | null = null
let startTime: number = 0

const answeredCount = computed(() => {
  return Object.values(answers.value).filter(v => !!v).length
})

onMounted(async () => {
  const paperId = Number(route.params.paperId)
  if (!paperId) {
    ElMessage.error('无效的试卷ID')
    router.push('/client/learning')
    return
  }
  await loadPaper(paperId)
})

onBeforeUnmount(() => {
  stopTimer()
})

async function loadPaper(paperId: number) {
  loading.value = true
  try {
    const res = await getPaper(paperId)
    const data = res.data.data
    paper.value = data
    questions.value = data?.questions || []
    answers.value = {}
    startTime = Date.now()
    startTimer()
  } catch {
    ElMessage.error('加载试卷失败')
  } finally {
    loading.value = false
  }
}

function startTimer() {
  stopTimer()
  timerInterval = setInterval(() => {
    remainingSeconds.value--
    if (remainingSeconds.value <= 0) {
      stopTimer()
      ElMessage.warning('考试时间到，自动交卷')
      doSubmit()
    }
  }, 1000)
}

function stopTimer() {
  if (timerInterval) {
    clearInterval(timerInterval)
    timerInterval = null
  }
}

function formatCountdown(secs: number): string {
  if (secs < 0) secs = 0
  const m = Math.floor(secs / 60)
  const s = secs % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

function formatExamDuration(secs: number): string {
  if (!secs) {
    const elapsed = Math.floor((Date.now() - startTime) / 1000)
    secs = elapsed
  }
  const m = Math.floor(secs / 60)
  const s = secs % 60
  return `${m}分${s}秒`
}

function questionTypeLabel(t: string): string {
  const map: Record<string, string> = {
    CHOICE: '选择题',
    TRUE_FALSE: '判断题',
    OPERATION: '操作题',
    SCENARIO: '场景题',
  }
  return map[t] || t
}

function questionTypeColor(t: string): 'primary' | 'success' | 'warning' | 'danger' {
  const map: Record<string, 'primary' | 'success' | 'warning' | 'danger'> = {
    CHOICE: 'primary',
    TRUE_FALSE: 'success',
    OPERATION: 'warning',
    SCENARIO: 'danger',
  }
  return map[t] || 'primary'
}

function difficultyType(d: string): 'success' | 'warning' | 'danger' | 'info' {
  if (d === '简单' || d === 'EASY') return 'success'
  if (d === '中等' || d === 'MEDIUM') return 'warning'
  if (d === '困难' || d === 'HARD') return 'danger'
  return 'info'
}

function scrollToQuestion(idx: number) {
  const el = document.getElementById('q-' + idx)
  if (el) el.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

async function handleSubmit() {
  try {
    await ElMessageBox.confirm('确定要交卷吗？提交后无法修改。', '确认交卷', {
      confirmButtonText: '确定交卷',
      cancelButtonText: '继续答题',
      type: 'warning',
    })
    await doSubmit()
  } catch { /* cancelled */ }
}

async function doSubmit() {
  stopTimer()
  submitting.value = true
  try {
    const elapsed = Math.floor((Date.now() - startTime) / 1000)
    const formattedAnswers = questions.value.map((q: any, idx: number) => ({
      questionId: q.id,
      answer: answers.value[idx] || '',
    }))
    const res = await submitExamAnswers({
      paperId: paper.value?.id,
      answers: formattedAnswers,
      durationSeconds: elapsed,
    })
    examResult.value = res.data.data
    showResults.value = true
    ElMessage.success('交卷成功！')
  } catch { /* handled by interceptor */ }
  finally { submitting.value = false }
}

function confirmLeave() {
  if (questions.value.length > 0 && !showResults.value) {
    ElMessageBox.confirm('退出考试将不会保存答案，确定要退出吗？', '确认退出', {
      confirmButtonText: '退出',
      cancelButtonText: '继续答题',
      type: 'warning',
    }).then(() => {
      stopTimer()
      router.push('/client/learning')
    }).catch(() => { /* stay */ })
  } else {
    router.push('/client/learning')
  }
}

function goBackToLearning() {
  router.push('/client/learning')
}
</script>

<style scoped>
.exam-page {
  min-height: 100vh;
  background: #f7f8fa;
  display: flex;
  flex-direction: column;
}

/* ---- Exam top bar ---- */
.exam-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  background: #fff;
  border-bottom: 1px solid #e8e8ed;
  flex-shrink: 0;
  position: sticky;
  top: 0;
  z-index: 10;
}

.exam-topbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.back-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: #6e6e73;
  padding: 4px;
  display: flex;
  align-items: center;
}

.back-btn:hover {
  color: #4361ee;
}

.exam-name {
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0;
}

.exam-meta-tags {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
}

.question-count {
  font-size: 12px;
  color: #8e8e93;
}

.timer {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #f0f2f5;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 18px;
  font-weight: 700;
  color: #1d1d1f;
  font-variant-numeric: tabular-nums;
}

.timer.warning {
  background: #fef0f0;
  color: #f56c6c;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.timer-icon {
  font-size: 18px;
}

/* ---- Loading ---- */
.loading-area {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #8e8e93;
  padding: 80px 20px;
}

/* ---- Questions area ---- */
.questions-area {
  display: flex;
  gap: 20px;
  padding: 24px 20px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

.questions-scroll {
  flex: 1;
  min-width: 0;
}

.question-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.question-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.question-num {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #4361ee;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

.question-text {
  font-size: 15px;
  color: #1d1d1f;
  line-height: 1.7;
  margin-bottom: 16px;
}

.question-options {
  padding-left: 4px;
}

.option-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 10px 14px;
  border-radius: 8px;
  border: 1px solid #e8e8ed;
  transition: all 0.2s;
  height: auto !important;
}

.option-item:hover {
  border-color: #4361ee;
  background: #f8f9ff;
}

.option-key {
  font-weight: 600;
  color: #4361ee;
  margin-right: 10px;
  min-width: 18px;
}

.option-text {
  color: #1d1d1f;
  font-size: 14px;
  white-space: normal;
  word-break: break-word;
}

.question-textarea {
  padding-left: 4px;
}

/* ---- Answer card panel ---- */
.answer-card-panel {
  width: 200px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  position: sticky;
  top: 80px;
  height: fit-content;
}

.answer-card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 12px;
}

.answer-card-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 6px;
  margin-bottom: 12px;
}

.answer-dot {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  border: 1px solid #e0e0e5;
  background: #fff;
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  color: #6e6e73;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.answer-dot:hover {
  border-color: #4361ee;
}

.answer-dot.answered {
  background: #4361ee;
  color: #fff;
  border-color: #4361ee;
}

.answer-card-summary {
  text-align: center;
  font-size: 13px;
  color: #8e8e93;
  margin-bottom: 16px;
}

.submit-btn {
  width: 100%;
}

/* ---- Results view ---- */
.results-view {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.results-container {
  max-width: 700px;
  width: 100%;
}

.results-hero {
  text-align: center;
  margin-bottom: 32px;
}

.score-circle {
  width: 140px;
  height: 140px;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
}

.score-circle.pass {
  background: linear-gradient(135deg, #e6f7e9, #c6efce);
}

.score-circle.fail {
  background: linear-gradient(135deg, #fef0f0, #fde2e2);
}

.score-number {
  font-size: 48px;
  font-weight: 800;
  line-height: 1;
}

.score-circle.pass .score-number {
  color: #67c23a;
}

.score-circle.fail .score-number {
  color: #f56c6c;
}

.score-label {
  font-size: 16px;
  font-weight: 500;
  margin-top: 4px;
}

.score-circle.pass .score-label {
  color: #67c23a;
}

.score-circle.fail .score-label {
  color: #f56c6c;
}

.result-badge {
  font-size: 16px;
  padding: 8px 24px;
}

.results-stats {
  display: flex;
  justify-content: center;
  gap: 48px;
  margin-bottom: 32px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #1d1d1f;
}

.stat-label {
  font-size: 13px;
  color: #8e8e93;
  margin-top: 4px;
}

/* ---- Wrong questions review ---- */
.wrong-review {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.review-title {
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0 0 16px;
}

.wrong-card {
  display: flex;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid #f0f2f5;
}

.wrong-card:last-child {
  border-bottom: none;
}

.wrong-num {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #fef0f0;
  color: #f56c6c;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

.wrong-body {
  flex: 1;
  min-width: 0;
}

.wrong-qtext {
  font-size: 14px;
  color: #1d1d1f;
  line-height: 1.6;
  margin-bottom: 10px;
}

.wrong-answers {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.answer-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  padding: 6px 12px;
  border-radius: 6px;
}

.answer-row.your {
  background: #fef0f0;
  color: #f56c6c;
}

.answer-row.correct {
  background: #e6f7e9;
  color: #67c23a;
}

.answer-label {
  font-weight: 600;
  flex-shrink: 0;
}

.answer-val {
  word-break: break-word;
}

.results-actions {
  text-align: center;
}

/* ---- Responsive ---- */
@media (max-width: 768px) {
  .questions-area {
    flex-direction: column;
    padding: 16px 12px;
  }

  .answer-card-panel {
    width: 100%;
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    border-radius: 12px 12px 0 0;
    box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.08);
    z-index: 20;
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 16px;
  }

  .answer-card-title {
    display: none;
  }

  .answer-card-grid {
    display: none;
  }

  .answer-card-summary {
    margin: 0;
    white-space: nowrap;
  }

  .submit-btn {
    width: auto;
    flex: 1;
  }

  .questions-scroll {
    padding-bottom: 80px;
  }

  .exam-name {
    font-size: 14px;
  }

  .timer {
    font-size: 15px;
    padding: 6px 12px;
  }

  .results-view {
    padding: 24px 12px;
    align-items: flex-start;
  }

  .score-circle {
    width: 110px;
    height: 110px;
  }

  .score-number {
    font-size: 36px;
  }
}
</style>
