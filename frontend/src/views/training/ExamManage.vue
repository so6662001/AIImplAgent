<template>
  <div>
    <div class="page-header">
      <h2>培训考核</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showExamDialog = true">录入考核</el-button>
        <el-button @click="showCheckDialog = true">上线准入检查</el-button>
      </div>
    </div>

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
import { ref } from 'vue'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import { submitExam, checkGoLive } from '@/api/training'
import type { ExamRecord, GoLiveCheck } from '@/types'
import { required, scoreRule } from '@/utils/validators'

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
</style>
