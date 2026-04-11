<template>
  <div class="learning-page">
    <!-- Top bar -->
    <header class="learning-topbar">
      <div class="topbar-left">
        <router-link to="/client/chat" class="back-link">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="15 18 9 12 15 6"/>
          </svg>
          <span class="back-text">返回助手</span>
        </router-link>
      </div>
      <div class="topbar-center">
        <span class="topbar-icon">📚</span>
        <span class="topbar-title">ERP学习中心</span>
        <span v-if="clientAuth.projectName" class="topbar-project">— {{ clientAuth.projectName }}</span>
      </div>
      <div class="topbar-right">
        <el-button text class="logout-btn" @click="handleLogout">退出</el-button>
      </div>
    </header>

    <!-- Tab navigation -->
    <div class="tab-bar">
      <button
        class="tab-btn"
        :class="{ active: currentTab === 'learning' }"
        @click="switchTab('learning')"
      >
        <span class="tab-icon">📖</span> 我的学习
      </button>
      <button
        class="tab-btn"
        :class="{ active: currentTab === 'exam' }"
        @click="switchTab('exam')"
      >
        <span class="tab-icon">📝</span> 我的考试
      </button>
      <button
        class="tab-btn"
        :class="{ active: currentTab === 'videos' }"
        @click="switchTab('videos')"
      >
        <span class="tab-icon">🎬</span> 视频教材
      </button>
      <button
        class="tab-btn"
        :class="{ active: currentTab === 'points' }"
        @click="switchTab('points')"
      >
        <span class="tab-icon">🪙</span> 我的积分
      </button>
    </div>

    <!-- Main content -->
    <div class="learning-content">
      <!-- ========== 我的学习 tab ========== -->
      <div v-if="currentTab === 'learning'">
        <!-- Course detail view -->
        <div v-if="selectedCourse">
          <button class="back-btn" @click="selectedCourse = null; selectedChapter = null">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><polyline points="15 18 9 12 15 6"/></svg>
            返回课程列表
          </button>

          <!-- Course header -->
          <div class="course-detail-header">
            <div class="course-detail-info">
              <h2>{{ selectedCourse.courseName }}</h2>
              <el-tag size="small" type="primary" class="module-badge">{{ selectedCourse.module }}</el-tag>
              <p class="course-desc">{{ selectedCourse.description }}</p>
              <div class="course-meta-row">
                <span>{{ selectedCourse.totalChapters }} 章节</span>
                <span>·</span>
                <span>{{ formatDuration(selectedCourse.totalDuration) }}</span>
                <span>·</span>
                <span>完成 {{ selectedCourse.progressPercent ?? 0 }}%</span>
              </div>
              <el-progress
                :percentage="selectedCourse.progressPercent ?? 0"
                :stroke-width="8"
                :color="progressColor"
                style="margin-top: 12px; max-width: 400px;"
              />
            </div>
          </div>

          <div class="course-body">
            <!-- Chapter list -->
            <div class="chapter-list-panel">
              <h3 class="section-title">课程章节</h3>
              <div class="chapter-list">
                <div
                  v-for="ch in chapters"
                  :key="ch.id"
                  class="chapter-item"
                  :class="{ active: selectedChapter?.id === ch.id }"
                  @click="selectChapter(ch)"
                >
                  <div class="chapter-num">{{ ch.chapterNumber }}</div>
                  <div class="chapter-info">
                    <div class="chapter-name">{{ ch.chapterName }}</div>
                    <div class="chapter-duration">{{ formatVideoDuration(ch.videoDuration) }}</div>
                  </div>
                  <div class="chapter-status">
                    <span v-if="ch.status === 'COMPLETED'" class="status-icon completed">✅</span>
                    <span v-else-if="ch.status === 'IN_PROGRESS'" class="status-icon in-progress">🔄</span>
                    <span v-else class="status-icon not-started">⏳</span>
                  </div>
                </div>
                <div v-if="chapters.length === 0" class="empty-chapters">
                  暂无章节数据
                </div>
              </div>
            </div>

            <!-- Video player -->
            <div class="video-panel" v-if="selectedChapter">
              <div class="video-header">
                <h3>{{ selectedChapter.chapterName }}</h3>
                <p v-if="selectedChapter.description" class="video-desc">{{ selectedChapter.description }}</p>
              </div>
              <div class="video-container">
                <video
                  v-if="selectedChapter.videoUrl"
                  :key="selectedChapter.id"
                  :src="selectedChapter.videoUrl"
                  controls
                  controlslist="nodownload"
                  playsinline
                  class="video-player"
                >
                  您的浏览器不支持视频播放
                </video>
                <div v-else class="no-video">
                  <span class="no-video-icon">🎬</span>
                  <p>暂无视频资源</p>
                </div>
              </div>
              <div class="video-actions">
                <el-button
                  v-if="selectedChapter.status !== 'COMPLETED'"
                  type="success"
                  size="large"
                  :loading="markingComplete"
                  @click="markChapterComplete"
                >
                  ✅ 标记已完成
                </el-button>
                <el-tag v-else type="success" size="large" effect="dark">
                  ✅ 已完成
                </el-tag>
              </div>
            </div>
            <div class="video-panel video-placeholder" v-else>
              <div class="placeholder-content">
                <span class="placeholder-icon">🎯</span>
                <p>请从左侧选择一个章节开始学习</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Course list view (default) -->
        <div v-else>
          <!-- Summary cards -->
          <div class="summary-cards" v-if="summary">
            <div class="summary-card">
              <div class="card-icon blue">📚</div>
              <div class="card-body">
                <div class="card-value">{{ summary.totalCourses ?? 0 }}</div>
                <div class="card-label">总课程数</div>
              </div>
            </div>
            <div class="summary-card">
              <div class="card-icon green">✅</div>
              <div class="card-body">
                <div class="card-value">{{ summary.completedCourses ?? 0 }}</div>
                <div class="card-label">已完成</div>
              </div>
            </div>
            <div class="summary-card">
              <div class="card-icon orange">📖</div>
              <div class="card-body">
                <div class="card-value">{{ summary.inProgressCourses ?? 0 }}</div>
                <div class="card-label">学习中</div>
              </div>
            </div>
            <div class="summary-card">
              <div class="card-icon purple">⏱️</div>
              <div class="card-body">
                <div class="card-value">{{ formatTotalTime(summary.totalLearningMinutes ?? 0) }}</div>
                <div class="card-label">总学习时长</div>
              </div>
            </div>
          </div>

          <h3 class="section-title" style="margin: 24px 0 16px">我的课程</h3>

          <div v-if="loadingCourses" class="loading-area">
            <el-icon class="is-loading" :size="24"><Loading /></el-icon>
            <span>加载中...</span>
          </div>

          <el-row v-else :gutter="20">
            <el-col
              v-for="course in courses"
              :key="course.id"
              :xs="24" :sm="12" :md="8" :lg="6"
              style="margin-bottom: 20px"
            >
              <div class="course-card" @click="openCourse(course)">
                <div class="course-card-top">
                  <el-tag size="small" type="primary" effect="plain">{{ course.module }}</el-tag>
                </div>
                <h4 class="course-card-name">{{ course.courseName }}</h4>
                <p class="course-card-desc">{{ truncate(course.description, 60) }}</p>
                <div class="course-card-meta">
                  <span>{{ course.totalChapters }} 章节</span>
                  <span>{{ formatDuration(course.totalDuration) }}</span>
                </div>
                <el-progress
                  :percentage="course.progressPercent ?? 0"
                  :stroke-width="6"
                  :color="progressColor"
                  style="margin-top: 12px"
                />
              </div>
            </el-col>
            <el-col :span="24" v-if="courses.length === 0 && !loadingCourses">
              <el-empty description="暂无课程" />
            </el-col>
          </el-row>
        </div>
      </div>

      <!-- ========== 我的考试 tab ========== -->
      <div v-if="currentTab === 'exam'">
        <h3 class="section-title" style="margin-bottom: 16px">待考试卷</h3>

        <div v-if="loadingPapers" class="loading-area">
          <el-icon class="is-loading" :size="24"><Loading /></el-icon>
          <span>加载中...</span>
        </div>

        <el-row v-else :gutter="20">
          <el-col
            v-for="paper in availablePapers"
            :key="paper.id"
            :xs="24" :sm="12" :md="8"
            style="margin-bottom: 20px"
          >
            <div class="paper-card">
              <div class="paper-card-header">
                <h4>{{ paper.title }}</h4>
                <el-tag size="small" :type="difficultyType(paper.difficulty)">
                  {{ paper.difficulty }}
                </el-tag>
              </div>
              <div class="paper-card-info">
                <el-tag size="small" type="info" effect="plain">{{ paper.module }}</el-tag>
                <el-tag size="small" :type="paperStatusType(paper.status)">{{ paperStatusLabel(paper.status) }}</el-tag>
              </div>
              <div class="paper-card-footer">
                <el-button
                  v-if="paper.status === 'CREATED'"
                  type="primary"
                  @click="startExam(paper.id)"
                >
                  开始考试
                </el-button>
                <el-button
                  v-else-if="paper.status === 'SUBMITTED'"
                  type="info"
                  plain
                  disabled
                >
                  已提交
                </el-button>
              </div>
            </div>
          </el-col>
          <el-col :span="24" v-if="availablePapers.length === 0 && !loadingPapers">
            <el-empty description="暂无可用试卷" />
          </el-col>
        </el-row>

        <h3 class="section-title" style="margin: 32px 0 16px">考试记录</h3>

        <div v-if="loadingResults" class="loading-area">
          <el-icon class="is-loading" :size="24"><Loading /></el-icon>
          <span>加载中...</span>
        </div>

        <div v-else>
          <div
            v-for="result in examResults"
            :key="result.id"
            class="result-card"
          >
            <div class="result-header">
              <div class="result-title">
                <h4>{{ result.title }}</h4>
                <el-tag size="small" :type="result.passed ? 'success' : 'danger'">
                  {{ result.passed ? '通过' : '未通过' }}
                </el-tag>
              </div>
              <div class="result-score" :class="result.passed ? 'pass' : 'fail'">
                {{ result.score }}分
              </div>
            </div>
            <div class="result-meta">
              <span>{{ result.module }}</span>
              <span>·</span>
              <span>{{ formatDate(result.submitTime) }}</span>
              <span>·</span>
              <span>{{ result.correctCount }}/{{ result.totalQuestions }} 正确</span>
            </div>
            <el-collapse v-if="result.wrongQuestions?.length" class="wrong-collapse">
              <el-collapse-item title="查看错题">
                <div v-for="(wq, idx) in (result.wrongQuestions as any[])" :key="idx" class="wrong-item">
                  <div class="wrong-question">{{ (idx as number) + 1 }}. {{ wq.questionText }}</div>
                  <div class="wrong-your"><span class="label">你的答案:</span> {{ wq.yourAnswer }}</div>
                  <div class="wrong-correct"><span class="label">正确答案:</span> {{ wq.correctAnswer }}</div>
                </div>
              </el-collapse-item>
            </el-collapse>
          </div>
          <el-empty v-if="examResults.length === 0 && !loadingResults" description="暂无考试记录" />
        </div>
      </div>

      <!-- ========== 视频教材 tab ========== -->
      <div v-if="currentTab === 'videos'">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px">
          <h3 class="section-title">社区视频教材</h3>
          <el-button type="primary" @click="publishDialogVisible = true">发布教材</el-button>
        </div>

        <div v-if="loadingVideos" class="loading-area">
          <el-icon class="is-loading" :size="24"><Loading /></el-icon>
          <span>加载中...</span>
        </div>

        <el-row v-else :gutter="20">
          <el-col
            v-for="video in approvedVideos"
            :key="video.id"
            :xs="24" :sm="12" :md="8" :lg="6"
            style="margin-bottom: 20px"
          >
            <div class="course-card">
              <div class="course-card-top" style="display: flex; gap: 6px; flex-wrap: wrap">
                <el-tag size="small" type="primary" effect="plain">{{ video.categoryModule }}</el-tag>
                <el-tag size="small" :type="video.pointsCost > 0 ? 'warning' : 'success'" effect="plain">
                  {{ video.pointsCost > 0 ? `${video.pointsCost} 积分` : '免费' }}
                </el-tag>
              </div>
              <h4 class="course-card-name">{{ video.title }}</h4>
              <p class="course-card-desc">{{ video.publisherName }}</p>
              <div class="course-card-meta">
                <span>{{ formatVideoDuration(video.videoDuration) }}</span>
                <span>{{ video.totalLearners ?? 0 }} 人学习</span>
                <span>{{ video.totalViews ?? 0 }} 次浏览</span>
              </div>
              <div style="margin-top: 12px">
                <template v-if="isLearningVideo(video.id)">
                  <el-progress
                    :percentage="getLearningProgress(video.id)"
                    :stroke-width="6"
                    :color="progressColor"
                  />
                  <span style="font-size: 12px; color: #8e8e93">学习中</span>
                </template>
                <el-button
                  v-else
                  type="primary"
                  size="small"
                  :loading="startingVideoId === video.id"
                  @click="handleStartLearning(video)"
                >
                  开始学习
                </el-button>
              </div>
            </div>
          </el-col>
          <el-col :span="24" v-if="approvedVideos.length === 0 && !loadingVideos">
            <el-empty description="暂无视频教材" />
          </el-col>
        </el-row>

        <!-- Publish Dialog -->
        <el-dialog v-model="publishDialogVisible" title="发布视频教材" width="520px" destroy-on-close>
          <el-form :model="publishForm" label-width="100px">
            <el-form-item label="标题" required>
              <el-input v-model="publishForm.title" placeholder="请输入视频标题" maxlength="100" />
            </el-form-item>
            <el-form-item label="描述">
              <el-input v-model="publishForm.description" type="textarea" :rows="3" placeholder="请输入视频描述" />
            </el-form-item>
            <el-form-item label="分类模块" required>
              <el-select v-model="publishForm.categoryModule" placeholder="请选择分类" style="width: 100%">
                <el-option label="采购管理" value="采购管理" />
                <el-option label="销售管理" value="销售管理" />
                <el-option label="库存管理" value="库存管理" />
                <el-option label="使用技巧" value="使用技巧" />
                <el-option label="行业知识" value="行业知识" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
            <el-form-item label="视频地址" required>
              <el-input v-model="publishForm.videoUrl" placeholder="请输入视频URL" />
            </el-form-item>
            <el-form-item label="视频时长" required>
              <el-input-number v-model="publishForm.videoDuration" :min="1" :max="36000" placeholder="秒" style="width: 100%" />
            </el-form-item>
            <el-form-item label="积分费用">
              <el-input-number v-model="publishForm.pointsCost" :min="0" :max="100" style="width: 100%" />
              <div style="font-size: 12px; color: #8e8e93; margin-top: 4px">0 = 免费</div>
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="publishDialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="publishing" @click="handlePublishVideo">发布</el-button>
          </template>
        </el-dialog>
      </div>

      <!-- ========== 我的积分 tab ========== -->
      <div v-if="currentTab === 'points'">
        <div v-if="loadingPoints" class="loading-area">
          <el-icon class="is-loading" :size="24"><Loading /></el-icon>
          <span>加载中...</span>
        </div>

        <div v-else>
          <!-- Balance card -->
          <div class="points-balance-card">
            <div class="points-balance-value">{{ pointsBalance }}</div>
            <div class="points-balance-label">积分</div>
          </div>

          <!-- My published videos -->
          <h3 class="section-title" style="margin: 24px 0 12px">我发布的教材</h3>
          <div v-if="myPublishedVideos.length === 0">
            <el-empty description="暂无发布的教材" :image-size="60" />
          </div>
          <div v-else>
            <div v-for="vid in myPublishedVideos" :key="vid.id" class="result-card" style="margin-bottom: 12px">
              <div style="display: flex; justify-content: space-between; align-items: center">
                <div>
                  <h4 style="margin: 0 0 6px; font-size: 15px; font-weight: 600; color: #1d1d1f">{{ vid.title }}</h4>
                  <div style="display: flex; gap: 8px; align-items: center">
                    <el-tag size="small" :type="approvalStatusType(vid.approvalStatus)">
                      {{ approvalStatusLabel(vid.approvalStatus) }}
                    </el-tag>
                    <span style="font-size: 13px; color: #8e8e93">{{ vid.categoryModule }}</span>
                  </div>
                </div>
                <div style="text-align: right">
                  <div style="font-size: 18px; font-weight: 700; color: #67c23a">+{{ vid.publisherEarnedPoints ?? 0 }}</div>
                  <div style="font-size: 12px; color: #8e8e93">获得积分</div>
                </div>
              </div>
            </div>
          </div>

          <!-- Transaction history -->
          <h3 class="section-title" style="margin: 24px 0 12px">积分明细</h3>
          <el-table v-if="pointsHistory.length > 0" :data="pointsHistory" stripe style="width: 100%">
            <el-table-column label="时间" prop="createTime" width="180">
              <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="类型" width="120">
              <template #default="{ row }">
                <el-tag size="small" :type="txTypeTagType(row.transactionType)">
                  {{ txTypeLabel(row.transactionType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="数量" width="100">
              <template #default="{ row }">
                <span :style="{ color: row.amount > 0 ? '#67c23a' : '#f56c6c', fontWeight: 600 }">
                  {{ row.amount > 0 ? '+' : '' }}{{ row.amount }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="余额" prop="balanceAfter" width="100" />
            <el-table-column label="说明" prop="description" />
          </el-table>
          <el-empty v-else description="暂无积分记录" :image-size="60" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { useClientAuthStore } from '@/stores/clientAuth'
import { getCoursesWithProgress, getCourseWithProgress, updateLearningProgress, getLearningSummary } from '@/api/clientlearning'
import { getMyPapers, getMyResults } from '@/api/clientexam'
import { publishVideo, listApprovedVideos, listMyPublished, startLearning, listMyLearning, getPointsBalance, getPointsHistory } from '@/api/clientvideo'

const router = useRouter()
const clientAuth = useClientAuthStore()

type TabKey = 'learning' | 'exam' | 'videos' | 'points'
const currentTab = ref<TabKey>('learning')
const loadingCourses = ref(false)
const loadingPapers = ref(false)
const loadingResults = ref(false)
const markingComplete = ref(false)

const summary = ref<any>(null)
const courses = ref<any[]>([])
const selectedCourse = ref<any>(null)
const chapters = ref<any[]>([])
const selectedChapter = ref<any>(null)

const availablePapers = ref<any[]>([])
const examResults = ref<any[]>([])

const loadingVideos = ref(false)
const approvedVideos = ref<any[]>([])
const myLearningVideos = ref<any[]>([])
const startingVideoId = ref<number | null>(null)
const publishDialogVisible = ref(false)
const publishing = ref(false)
const publishForm = ref({
  title: '',
  description: '',
  categoryModule: '',
  videoUrl: '',
  videoDuration: 60,
  pointsCost: 0,
})

const loadingPoints = ref(false)
const pointsBalance = ref(0)
const myPublishedVideos = ref<any[]>([])
const pointsHistory = ref<any[]>([])

const progressColor = [
  { color: '#f56c6c', percentage: 30 },
  { color: '#e6a23c', percentage: 60 },
  { color: '#67c23a', percentage: 100 },
]

onMounted(() => {
  loadLearningData()
})

function switchTab(tab: TabKey) {
  currentTab.value = tab
  if (tab === 'exam') {
    loadExamData()
  } else if (tab === 'videos') {
    loadVideoData()
  } else if (tab === 'points') {
    loadPointsData()
  }
}

async function loadLearningData() {
  loadingCourses.value = true
  try {
    const [summaryRes, coursesRes] = await Promise.all([
      getLearningSummary(),
      getCoursesWithProgress(),
    ])
    summary.value = summaryRes.data.data
    courses.value = coursesRes.data.data || []
  } catch { /* handled by interceptor */ }
  finally { loadingCourses.value = false }
}

async function loadExamData() {
  loadingPapers.value = true
  loadingResults.value = true
  try {
    const [papersRes, resultsRes] = await Promise.all([
      getMyPapers(),
      getMyResults(),
    ])
    availablePapers.value = papersRes.data.data || []
    examResults.value = resultsRes.data.data || []
  } catch { /* handled by interceptor */ }
  finally {
    loadingPapers.value = false
    loadingResults.value = false
  }
}

async function openCourse(course: any) {
  selectedCourse.value = course
  selectedChapter.value = null
  try {
    const res = await getCourseWithProgress(course.id)
    const data = res.data.data
    if (data) {
      selectedCourse.value = { ...course, ...data }
      chapters.value = data.chapters || []
    }
  } catch { /* handled by interceptor */ }
}

function selectChapter(ch: any) {
  selectedChapter.value = ch
}

async function markChapterComplete() {
  if (!selectedChapter.value || !selectedCourse.value) return
  markingComplete.value = true
  try {
    await updateLearningProgress({
      courseId: selectedCourse.value.id,
      chapterId: selectedChapter.value.id,
      status: 'COMPLETED',
    })
    selectedChapter.value.status = 'COMPLETED'
    ElMessage.success('已标记完成！')

    const idx = chapters.value.findIndex((c: any) => c.id === selectedChapter.value!.id)
    if (idx >= 0 && idx < chapters.value.length - 1) {
      selectedChapter.value = chapters.value[idx + 1]
      ElMessage.info('已自动跳转到下一章节')
    }

    const completedCount = chapters.value.filter((c: any) => c.status === 'COMPLETED').length
    if (selectedCourse.value) {
      selectedCourse.value.progressPercent = Math.round((completedCount / chapters.value.length) * 100)
    }
  } catch { /* handled by interceptor */ }
  finally { markingComplete.value = false }
}

function startExam(paperId: number) {
  router.push(`/client/exam/${paperId}`)
}

function truncate(text: string, max: number): string {
  if (!text) return ''
  return text.length > max ? text.slice(0, max) + '...' : text
}

function formatDuration(minutes: number): string {
  if (!minutes) return '0分钟'
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  if (h > 0) return `${h}小时${m > 0 ? m + '分钟' : ''}`
  return `${m}分钟`
}

function formatTotalTime(minutes: number): string {
  if (!minutes) return '0:00'
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return `${h}:${String(m).padStart(2, '0')}`
}

function formatVideoDuration(seconds: number): string {
  if (!seconds) return '0:00'
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}:${String(s).padStart(2, '0')}`
}

function formatDate(iso: string): string {
  if (!iso) return ''
  const d = new Date(iso)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function difficultyType(d: string): 'success' | 'warning' | 'danger' | 'info' {
  if (d === '简单' || d === 'EASY') return 'success'
  if (d === '中等' || d === 'MEDIUM') return 'warning'
  if (d === '困难' || d === 'HARD') return 'danger'
  return 'info'
}

function paperStatusType(s: string): 'info' | 'primary' | 'success' | 'warning' {
  if (s === 'CREATED') return 'primary'
  if (s === 'SUBMITTED') return 'success'
  return 'info'
}

function paperStatusLabel(s: string): string {
  if (s === 'CREATED') return '待考试'
  if (s === 'SUBMITTED') return '已提交'
  return s
}

async function loadVideoData() {
  loadingVideos.value = true
  try {
    const [videosRes, learningRes] = await Promise.all([
      listApprovedVideos(),
      listMyLearning(),
    ])
    approvedVideos.value = videosRes.data.data || []
    myLearningVideos.value = learningRes.data.data || []
  } catch { /* handled by interceptor */ }
  finally { loadingVideos.value = false }
}

function isLearningVideo(videoId: number): boolean {
  return myLearningVideos.value.some((l: any) => l.videoId === videoId)
}

function getLearningProgress(videoId: number): number {
  const learning = myLearningVideos.value.find((l: any) => l.videoId === videoId)
  if (!learning) return 0
  if (learning.completed) return 100
  const video = approvedVideos.value.find((v: any) => v.id === videoId)
  if (!video || !video.videoDuration) return 0
  return Math.min(100, Math.round((learning.watchedDuration / video.videoDuration) * 100))
}

async function handleStartLearning(video: any) {
  if (video.pointsCost > 0) {
    try {
      await ElMessageBox.confirm(
        `将消费 ${video.pointsCost} 积分，确定？`,
        '积分消费确认',
        { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' },
      )
    } catch {
      return
    }
  }
  startingVideoId.value = video.id
  try {
    await startLearning(video.id)
    ElMessage.success('开始学习')
    await loadVideoData()
  } catch { /* handled by interceptor */ }
  finally { startingVideoId.value = null }
}

async function handlePublishVideo() {
  if (!publishForm.value.title?.trim()) {
    ElMessage.warning('请输入视频标题')
    return
  }
  if (!publishForm.value.categoryModule) {
    ElMessage.warning('请选择分类模块')
    return
  }
  if (!publishForm.value.videoUrl?.trim()) {
    ElMessage.warning('请输入视频地址')
    return
  }
  publishing.value = true
  try {
    await publishVideo({
      title: publishForm.value.title,
      description: publishForm.value.description,
      categoryModule: publishForm.value.categoryModule,
      videoUrl: publishForm.value.videoUrl,
      videoDuration: publishForm.value.videoDuration,
      pointsCost: publishForm.value.pointsCost,
    })
    ElMessage.success('发布成功，等待审批')
    publishDialogVisible.value = false
    publishForm.value = { title: '', description: '', categoryModule: '', videoUrl: '', videoDuration: 60, pointsCost: 0 }
  } catch { /* handled by interceptor */ }
  finally { publishing.value = false }
}

async function loadPointsData() {
  loadingPoints.value = true
  try {
    const [balanceRes, publishedRes, historyRes] = await Promise.all([
      getPointsBalance(),
      listMyPublished(),
      getPointsHistory(),
    ])
    pointsBalance.value = balanceRes.data.data ?? 0
    myPublishedVideos.value = publishedRes.data.data || []
    pointsHistory.value = historyRes.data.data || []
  } catch { /* handled by interceptor */ }
  finally { loadingPoints.value = false }
}

function approvalStatusType(status: string): 'warning' | 'success' | 'danger' | 'info' {
  if (status === 'PENDING') return 'warning'
  if (status === 'APPROVED') return 'success'
  if (status === 'REJECTED') return 'danger'
  return 'info'
}

function approvalStatusLabel(status: string): string {
  if (status === 'PENDING') return '审核中'
  if (status === 'APPROVED') return '已通过'
  if (status === 'REJECTED') return '已驳回'
  return status
}

function txTypeTagType(type: string): 'success' | 'danger' | '' {
  if (type === 'EARN_FROM_VIDEO') return 'success'
  if (type === 'SPEND_ON_VIDEO') return 'danger'
  return ''
}

function txTypeLabel(type: string): string {
  if (type === 'EARN_FROM_VIDEO') return '获得'
  if (type === 'SPEND_ON_VIDEO') return '消费'
  if (type === 'SYSTEM_REWARD') return '奖励'
  if (type === 'ADMIN_ADJUST') return '调整'
  return type
}

function handleLogout() {
  clientAuth.logout()
  router.push('/client/login')
}
</script>

<style scoped>
.learning-page {
  min-height: 100vh;
  background: #f7f8fa;
  display: flex;
  flex-direction: column;
}

/* ---- Top bar ---- */
.learning-topbar {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  background: #fff;
  border-bottom: 1px solid #e8e8ed;
  flex-shrink: 0;
}

.topbar-left {
  flex-shrink: 0;
}

.back-link {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #4361ee;
  text-decoration: none;
  font-size: 14px;
  transition: opacity 0.2s;
}

.back-link:hover {
  opacity: 0.7;
}

.topbar-center {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.topbar-icon {
  font-size: 20px;
}

.topbar-title {
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
}

.topbar-project {
  font-weight: 400;
  color: #8e8e93;
  font-size: 14px;
}

.topbar-right {
  flex-shrink: 0;
}

.logout-btn {
  color: #8e8e93 !important;
  font-size: 14px;
}

.logout-btn:hover {
  color: #f56c6c !important;
}

/* ---- Tab bar ---- */
.tab-bar {
  display: flex;
  gap: 0;
  background: #fff;
  border-bottom: 1px solid #e8e8ed;
  padding: 0 20px;
  flex-shrink: 0;
}

.tab-btn {
  padding: 14px 24px;
  border: none;
  background: none;
  font-size: 14px;
  font-weight: 500;
  color: #6e6e73;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.tab-btn:hover {
  color: #4361ee;
}

.tab-btn.active {
  color: #4361ee;
  border-bottom-color: #4361ee;
}

.tab-icon {
  font-size: 16px;
}

/* ---- Content area ---- */
.learning-content {
  flex: 1;
  padding: 24px 20px;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  box-sizing: border-box;
}

/* ---- Summary cards ---- */
.summary-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.summary-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}

.card-icon.blue { background: #e8f0fe; }
.card-icon.green { background: #e6f7e9; }
.card-icon.orange { background: #fef3e2; }
.card-icon.purple { background: #f0e8fe; }

.card-value {
  font-size: 24px;
  font-weight: 700;
  color: #1d1d1f;
}

.card-label {
  font-size: 13px;
  color: #8e8e93;
  margin-top: 2px;
}

/* ---- Section title ---- */
.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
}

/* ---- Loading ---- */
.loading-area {
  text-align: center;
  padding: 40px;
  color: #8e8e93;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

/* ---- Course cards ---- */
.course-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.25s;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  height: 100%;
  box-sizing: border-box;
}

.course-card:hover {
  box-shadow: 0 4px 16px rgba(67, 97, 238, 0.12);
  transform: translateY(-2px);
}

.course-card-top {
  margin-bottom: 12px;
}

.course-card-name {
  font-size: 15px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0 0 8px;
  line-height: 1.4;
}

.course-card-desc {
  font-size: 13px;
  color: #8e8e93;
  margin: 0 0 12px;
  line-height: 1.5;
  min-height: 40px;
}

.course-card-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #b0b0b5;
}

/* ---- Course detail ---- */
.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  cursor: pointer;
  color: #4361ee;
  font-size: 14px;
  padding: 0;
  margin-bottom: 16px;
}

.back-btn:hover {
  opacity: 0.7;
}

.course-detail-header {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.course-detail-info h2 {
  font-size: 20px;
  font-weight: 700;
  color: #1d1d1f;
  margin: 0 0 8px;
}

.module-badge {
  margin-bottom: 8px;
}

.course-desc {
  font-size: 14px;
  color: #6e6e73;
  margin: 8px 0 12px;
  line-height: 1.6;
}

.course-meta-row {
  display: flex;
  gap: 8px;
  font-size: 13px;
  color: #8e8e93;
}

/* ---- Course body (chapters + video) ---- */
.course-body {
  display: flex;
  gap: 20px;
}

.chapter-list-panel {
  width: 320px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  max-height: calc(100vh - 300px);
  overflow-y: auto;
}

.chapter-list {
  margin-top: 12px;
}

.chapter-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
  margin-bottom: 4px;
}

.chapter-item:hover {
  background: #f5f6fa;
}

.chapter-item.active {
  background: #e8f0fe;
}

.chapter-num {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  color: #6e6e73;
  flex-shrink: 0;
}

.chapter-item.active .chapter-num {
  background: #4361ee;
  color: #fff;
}

.chapter-info {
  flex: 1;
  min-width: 0;
}

.chapter-name {
  font-size: 14px;
  color: #1d1d1f;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chapter-duration {
  font-size: 12px;
  color: #b0b0b5;
  margin-top: 2px;
}

.chapter-status {
  flex-shrink: 0;
}

.status-icon {
  font-size: 16px;
}

.empty-chapters {
  text-align: center;
  padding: 24px;
  color: #b0b0b5;
  font-size: 13px;
}

/* ---- Video panel ---- */
.video-panel {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  min-width: 0;
}

.video-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0 0 8px;
}

.video-desc {
  font-size: 13px;
  color: #8e8e93;
  margin: 0 0 16px;
  line-height: 1.5;
}

.video-container {
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 16px;
}

.video-player {
  width: 100%;
  display: block;
  max-height: 480px;
}

.no-video {
  padding: 60px 20px;
  text-align: center;
  color: #8e8e93;
}

.no-video-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 12px;
}

.no-video p {
  margin: 0;
  font-size: 14px;
}

.video-actions {
  display: flex;
  justify-content: flex-end;
}

.video-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}

.placeholder-content {
  text-align: center;
  color: #b0b0b5;
}

.placeholder-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 12px;
}

.placeholder-content p {
  font-size: 14px;
  margin: 0;
}

/* ---- Paper cards ---- */
.paper-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  height: 100%;
  box-sizing: border-box;
}

.paper-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 12px;
}

.paper-card-header h4 {
  font-size: 15px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0;
}

.paper-card-info {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.paper-card-footer {
  display: flex;
  justify-content: flex-end;
}

/* ---- Result cards ---- */
.result-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.result-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.result-title h4 {
  font-size: 15px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0;
}

.result-score {
  font-size: 28px;
  font-weight: 700;
}

.result-score.pass {
  color: #67c23a;
}

.result-score.fail {
  color: #f56c6c;
}

.result-meta {
  display: flex;
  gap: 8px;
  font-size: 13px;
  color: #8e8e93;
  margin-top: 8px;
}

.wrong-collapse {
  margin-top: 12px;
  border: none;
}

.wrong-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f2f5;
}

.wrong-item:last-child {
  border-bottom: none;
}

.wrong-question {
  font-size: 14px;
  color: #1d1d1f;
  font-weight: 500;
  margin-bottom: 8px;
}

.wrong-your {
  color: #f56c6c;
  font-size: 13px;
  margin-bottom: 4px;
}

.wrong-correct {
  color: #67c23a;
  font-size: 13px;
}

.wrong-your .label,
.wrong-correct .label {
  font-weight: 500;
}

/* ---- Points balance card ---- */
.points-balance-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 32px;
  text-align: center;
  color: #fff;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.25);
}

.points-balance-value {
  font-size: 48px;
  font-weight: 800;
  line-height: 1.2;
}

.points-balance-label {
  font-size: 16px;
  opacity: 0.85;
  margin-top: 4px;
}

/* ---- Responsive ---- */
@media (max-width: 768px) {
  .learning-content {
    padding: 16px 12px;
  }

  .summary-cards {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .summary-card {
    padding: 14px;
  }

  .card-value {
    font-size: 20px;
  }

  .course-body {
    flex-direction: column;
  }

  .chapter-list-panel {
    width: 100%;
    max-height: none;
  }

  .back-text {
    display: none;
  }

  .topbar-center {
    gap: 4px;
  }

  .topbar-title {
    font-size: 14px;
  }

  .tab-btn {
    padding: 12px 16px;
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .summary-cards {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
