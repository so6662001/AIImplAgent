<template>
  <div>
    <div class="page-header">
      <h2>课程管理</h2>
      <el-button type="primary" @click="openCourseDialog()">新增课程</el-button>
    </div>

    <div class="card">
      <el-table :data="courseList" stripe @row-click="handleRowClick" style="cursor: pointer">
        <el-table-column prop="courseCode" label="课程编号" width="120" />
        <el-table-column prop="courseName" label="课程名称" min-width="180" />
        <el-table-column label="模块" width="140">
          <template #default="{ row }">
            <el-tag size="small" type="primary">{{ row.module }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalChapters" label="章节数" width="90" align="center" />
        <el-table-column label="总时长" width="100">
          <template #default="{ row }">
            {{ formatDuration(row.totalDuration) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.enabled !== false ? 'success' : 'info'" size="small">
              {{ row.enabled !== false ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Chapter management -->
    <div v-if="selectedCourse" class="card" style="margin-top: 20px;">
      <div class="page-header" style="margin-bottom: 16px;">
        <h3 style="margin: 0; font-size: 16px;">
          {{ selectedCourse.courseName }} — 章节管理
        </h3>
        <el-button type="primary" size="small" @click="openChapterDialog()">新增章节</el-button>
      </div>
      <el-table :data="chapterList" stripe>
        <el-table-column prop="chapterNumber" label="序号" width="70" align="center" />
        <el-table-column prop="chapterName" label="章节名称" min-width="200" />
        <el-table-column prop="videoUrl" label="视频地址" min-width="250">
          <template #default="{ row }">
            <el-link v-if="row.videoUrl" :href="row.videoUrl" target="_blank" type="primary" :underline="false">
              {{ truncateUrl(row.videoUrl) }}
            </el-link>
            <span v-else class="text-secondary">—</span>
          </template>
        </el-table-column>
        <el-table-column label="视频时长" width="100">
          <template #default="{ row }">
            {{ formatVideoDuration(row.videoDuration) }}
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Course dialog -->
    <el-dialog v-model="showCourseDialog" title="新增课程" width="600px" destroy-on-close>
      <el-form ref="courseFormRef" :model="courseForm" :rules="courseRules" label-width="100px">
        <el-form-item label="课程编号" prop="courseCode">
          <el-input v-model="courseForm.courseCode" maxlength="30" placeholder="如: C001" />
        </el-form-item>
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="courseForm.courseName" maxlength="100" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="课程描述">
          <el-input v-model="courseForm.description" type="textarea" :rows="3" maxlength="500" placeholder="课程简介" />
        </el-form-item>
        <el-form-item label="模块" prop="module">
          <el-select v-model="courseForm.module" placeholder="请选择模块" style="width: 100%;">
            <el-option label="基础资料" value="基础资料" />
            <el-option label="采购管理" value="采购管理" />
            <el-option label="销售管理" value="销售管理" />
            <el-option label="库存管理" value="库存管理" />
            <el-option label="财务管理" value="财务管理" />
            <el-option label="加工管理" value="加工管理" />
            <el-option label="质量管理" value="质量管理" />
            <el-option label="报表统计" value="报表统计" />
            <el-option label="系统管理" value="系统管理" />
          </el-select>
        </el-form-item>
        <el-form-item label="行业类型">
          <el-select v-model="courseForm.industryType" placeholder="请选择行业" style="width: 100%;">
            <el-option label="钢贸商" value="STEEL_TRADER" />
            <el-option label="钢厂" value="STEEL_MILL" />
            <el-option label="加工中心" value="PROCESSING_CENTER" />
            <el-option label="综合服务商" value="INTEGRATED_SERVICE" />
          </el-select>
        </el-form-item>
        <el-form-item label="总章节数">
          <el-input-number v-model="courseForm.totalChapters" :min="0" :max="200" controls-position="right" />
        </el-form-item>
        <el-form-item label="总时长(分钟)">
          <el-input-number v-model="courseForm.totalDuration" :min="0" :max="99999" controls-position="right" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCourseDialog = false">取消</el-button>
        <el-button type="primary" :loading="submittingCourse" @click="handleCourseSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- Chapter dialog -->
    <el-dialog v-model="showChapterDialog" title="新增章节" width="600px" destroy-on-close>
      <el-form ref="chapterFormRef" :model="chapterForm" :rules="chapterRules" label-width="100px">
        <el-form-item label="章节序号" prop="chapterNumber">
          <el-input-number v-model="chapterForm.chapterNumber" :min="1" :max="200" controls-position="right" />
        </el-form-item>
        <el-form-item label="章节名称" prop="chapterName">
          <el-input v-model="chapterForm.chapterName" maxlength="100" placeholder="请输入章节名称" />
        </el-form-item>
        <el-form-item label="视频地址">
          <el-input v-model="chapterForm.videoUrl" maxlength="500" placeholder="视频URL" />
        </el-form-item>
        <el-form-item label="视频时长(秒)">
          <el-input-number v-model="chapterForm.videoDuration" :min="0" :max="99999" controls-position="right" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showChapterDialog = false">取消</el-button>
        <el-button type="primary" :loading="submittingChapter" @click="handleChapterSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { listCourses, createCourse, listChapters, createChapter } from '@/api/course'

const courseList = ref<any[]>([])
const chapterList = ref<any[]>([])
const selectedCourse = ref<any>(null)

const showCourseDialog = ref(false)
const showChapterDialog = ref(false)
const submittingCourse = ref(false)
const submittingChapter = ref(false)

const courseFormRef = ref<FormInstance>()
const chapterFormRef = ref<FormInstance>()

const courseForm = ref({
  courseCode: '',
  courseName: '',
  description: '',
  module: '',
  industryType: '',
  totalChapters: 0,
  totalDuration: 0,
})

const chapterForm = ref({
  chapterNumber: 1,
  chapterName: '',
  videoUrl: '',
  videoDuration: 0,
})

const courseRules: FormRules = {
  courseCode: [{ required: true, message: '请输入课程编号', trigger: 'blur' }],
  courseName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  module: [{ required: true, message: '请选择模块', trigger: 'change' }],
}

const chapterRules: FormRules = {
  chapterNumber: [{ required: true, message: '请输入章节序号', trigger: 'blur' }],
  chapterName: [{ required: true, message: '请输入章节名称', trigger: 'blur' }],
}

onMounted(() => {
  loadCourses()
})

async function loadCourses() {
  try {
    const res = await listCourses()
    courseList.value = res.data.data || []
  } catch { /* handled by interceptor */ }
}

async function loadChapters(courseId: number) {
  try {
    const res = await listChapters(courseId)
    chapterList.value = res.data.data || []
  } catch { /* handled by interceptor */ }
}

function handleRowClick(row: any) {
  selectedCourse.value = row
  loadChapters(row.id)
}

function openCourseDialog() {
  courseForm.value = {
    courseCode: '',
    courseName: '',
    description: '',
    module: '',
    industryType: '',
    totalChapters: 0,
    totalDuration: 0,
  }
  showCourseDialog.value = true
}

function openChapterDialog() {
  chapterForm.value = {
    chapterNumber: chapterList.value.length + 1,
    chapterName: '',
    videoUrl: '',
    videoDuration: 0,
  }
  showChapterDialog.value = true
}

async function handleCourseSubmit() {
  const form = courseFormRef.value
  if (!form) return
  const valid = await form.validate().catch(() => false)
  if (!valid) return

  submittingCourse.value = true
  try {
    await createCourse(courseForm.value as unknown as Record<string, unknown>)
    ElMessage.success('课程创建成功')
    showCourseDialog.value = false
    await loadCourses()
  } catch { /* handled by interceptor */ }
  finally { submittingCourse.value = false }
}

async function handleChapterSubmit() {
  const form = chapterFormRef.value
  if (!form) return
  const valid = await form.validate().catch(() => false)
  if (!valid) return
  if (!selectedCourse.value) return

  submittingChapter.value = true
  try {
    await createChapter(selectedCourse.value.id, chapterForm.value as unknown as Record<string, unknown>)
    ElMessage.success('章节创建成功')
    showChapterDialog.value = false
    await loadChapters(selectedCourse.value.id)
  } catch { /* handled by interceptor */ }
  finally { submittingChapter.value = false }
}

function formatDuration(minutes: number): string {
  if (!minutes) return '0分钟'
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  if (h > 0) return `${h}h${m > 0 ? m + 'm' : ''}`
  return `${m}分钟`
}

function formatVideoDuration(seconds: number): string {
  if (!seconds) return '0:00'
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}:${String(s).padStart(2, '0')}`
}

function truncateUrl(url: string): string {
  if (!url) return ''
  return url.length > 50 ? url.slice(0, 50) + '...' : url
}
</script>
