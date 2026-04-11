<template>
  <div>
    <div class="page-header">
      <h2>学员管理</h2>
      <div style="display: flex; align-items: center; gap: 12px;">
        <el-input-number v-model="filterProjectId" :min="1" placeholder="项目ID" controls-position="right" @change="loadData" />
        <el-button type="primary" @click="showDialog = true">新增学员</el-button>
      </div>
    </div>

    <div class="card">
      <el-table :data="trainees" stripe>
        <el-table-column prop="employeeName" label="姓名" width="100" />
        <el-table-column prop="role" label="角色" width="120" />
        <el-table-column prop="department" label="部门" width="100" />
        <el-table-column label="KA用户" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.kaUser" type="warning" size="small">KA</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="进度" min-width="160">
          <template #default="{ row }">
            <el-progress :percentage="row.progressPercent" :stroke-width="16" :text-inside="true" />
          </template>
        </el-table-column>
        <el-table-column prop="attendanceDays" label="出勤天数" width="90" />
        <el-table-column prop="totalDays" label="总天数" width="80" />
        <el-table-column label="风险等级" width="100">
          <template #default="{ row }">
            <el-tag
              :type="row.riskLevel === 'HIGH' ? 'danger' : row.riskLevel === 'MEDIUM' ? 'warning' : 'success'"
              size="small"
            >
              {{ row.riskLevel }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="showDialog" title="新增学员" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="项目ID" prop="projectId">
          <el-input-number v-model="form.projectId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="姓名" prop="employeeName">
          <el-input v-model="form.employeeName" maxlength="30" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择">
            <el-option label="业务骨干" value="业务骨干" />
            <el-option label="仓管员" value="仓管员" />
            <el-option label="财务" value="财务" />
            <el-option label="出纳" value="出纳" />
            <el-option label="磅房人员" value="磅房人员" />
            <el-option label="IT运维" value="IT运维" />
            <el-option label="管理层" value="管理层" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门">
          <el-input v-model="form.department" maxlength="30" />
        </el-form-item>
        <el-form-item label="标记KA用户">
          <el-switch v-model="form.kaUser" />
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
import { ref, onMounted } from 'vue'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import { createTrainee, listTrainees } from '@/api/trainee'
import type { TraineeProfile } from '@/types'
import { required, maxLen } from '@/utils/validators'

const trainees = ref<TraineeProfile[]>([])
const showDialog = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const filterProjectId = ref<number>(1)

const form = ref({
  projectId: 1,
  employeeName: '',
  role: '',
  department: '',
  kaUser: false,
})

const rules = {
  projectId: [required('项目ID不能为空')],
  employeeName: [required('姓名不能为空'), maxLen(30)],
  role: [required('角色不能为空')],
}

async function loadData() {
  if (!filterProjectId.value) return
  const res = await listTrainees(filterProjectId.value)
  trainees.value = res.data.data
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createTrainee(form.value as unknown as Record<string, unknown>)
    ElMessage.success('学员创建成功')
    showDialog.value = false
    await loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>
