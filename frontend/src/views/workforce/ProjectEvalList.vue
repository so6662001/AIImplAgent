<template>
  <div>
    <div class="page-header">
      <h2>项目评价</h2>
      <el-button type="primary" @click="showDialog = true">新建评价</el-button>
    </div>

    <div class="card" style="margin-bottom: 20px;">
      <el-form inline>
        <el-form-item label="项目ID">
          <el-input-number v-model="filterProjectId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="card">
      <el-table :data="list" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="projectId" label="项目ID" width="80" />
        <el-table-column prop="evaluatorId" label="评估人ID" width="90" />
        <el-table-column prop="scheduleScore" label="进度" width="70" />
        <el-table-column prop="qualityScore" label="质量" width="70" />
        <el-table-column prop="csatScore" label="满意度" width="80" />
        <el-table-column prop="processScore" label="流程" width="70" />
        <el-table-column prop="costScore" label="成本" width="70" />
        <el-table-column prop="pqiScore" label="PQI" width="80">
          <template #default="{ row }">
            <span style="font-weight: 600;">{{ row.pqiScore }}</span>
          </template>
        </el-table-column>
        <el-table-column label="评级" width="90">
          <template #default="{ row }">
            <el-tag :type="ratingTagType(row.rating)" size="small">
              {{ EvalRatingLabels[row.rating] || row.rating }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="aiComment" label="AI点评" min-width="200" />
      </el-table>
    </div>

    <el-dialog v-model="showDialog" title="新建项目评价" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="项目ID" prop="projectId">
          <el-input-number v-model="form.projectId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="评估人ID" prop="evaluatorId">
          <el-input-number v-model="form.evaluatorId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="进度评分" prop="scheduleScore">
          <el-input-number v-model="form.scheduleScore" :min="0" :max="100" controls-position="right" />
        </el-form-item>
        <el-form-item label="质量评分" prop="qualityScore">
          <el-input-number v-model="form.qualityScore" :min="0" :max="100" controls-position="right" />
        </el-form-item>
        <el-form-item label="满意度评分" prop="csatScore">
          <el-input-number v-model="form.csatScore" :min="0" :max="100" controls-position="right" />
        </el-form-item>
        <el-form-item label="流程评分" prop="processScore">
          <el-input-number v-model="form.processScore" :min="0" :max="100" controls-position="right" />
        </el-form-item>
        <el-form-item label="成本评分" prop="costScore">
          <el-input-number v-model="form.costScore" :min="0" :max="100" controls-position="right" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import { createProjectEvaluation, listProjectEvaluations } from '@/api/workforce'
import { EvalRatingLabels } from '@/types'
import type { ProjectEvaluation } from '@/types'
import { required, percentRule } from '@/utils/validators'

const list = ref<ProjectEvaluation[]>([])
const showDialog = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const filterProjectId = ref(1)

const initForm = () => ({
  projectId: undefined as number | undefined,
  evaluatorId: undefined as number | undefined,
  scheduleScore: 0,
  qualityScore: 0,
  csatScore: 0,
  processScore: 0,
  costScore: 0,
})

const form = ref(initForm())

const rules = {
  projectId: [required('项目ID不能为空')],
  evaluatorId: [required('评估人ID不能为空')],
  scheduleScore: [required('进度评分不能为空'), percentRule],
  qualityScore: [required('质量评分不能为空'), percentRule],
  csatScore: [required('满意度评分不能为空'), percentRule],
  processScore: [required('流程评分不能为空'), percentRule],
  costScore: [required('成本评分不能为空'), percentRule],
}

function ratingTagType(rating: string) {
  const map: Record<string, string> = {
    EXCELLENT: 'success',
    GOOD: '',
    QUALIFIED: 'info',
    NEEDS_IMPROVEMENT: 'warning',
    UNQUALIFIED: 'danger',
  }
  return map[rating] || 'info'
}

async function loadData() {
  const res = await listProjectEvaluations(filterProjectId.value)
  list.value = res.data.data
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createProjectEvaluation(form.value as unknown as Record<string, unknown>)
    ElMessage.success('评价创建成功')
    showDialog.value = false
    form.value = initForm()
    await loadData()
  } finally {
    submitting.value = false
  }
}
</script>
