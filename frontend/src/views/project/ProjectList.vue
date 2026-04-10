<template>
  <div>
    <div class="page-header">
      <h2>项目管理</h2>
      <el-button type="primary" @click="showDialog = true">新建项目</el-button>
    </div>

    <div class="card">
      <el-table :data="projects" stripe>
        <el-table-column prop="projectCode" label="项目编号" width="140" />
        <el-table-column prop="customerName" label="客户名称" min-width="160" />
        <el-table-column label="行业类型" width="120">
          <template #default="{ row }">
            {{ IndustryTypeLabels[row.industryType as IndustryType] || row.industryType }}
          </template>
        </el-table-column>
        <el-table-column prop="scale" label="规模" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ ProjectStatusLabels[row.status] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="region" label="区域" width="100" />
        <el-table-column prop="startDate" label="启动日期" width="120" />
      </el-table>
    </div>

    <el-dialog v-model="showDialog" title="新建项目" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="项目编号" prop="projectCode">
          <el-input v-model="form.projectCode" placeholder="如：PRJ-2026-001" maxlength="32" />
        </el-form-item>
        <el-form-item label="客户名称" prop="customerName">
          <el-input v-model="form.customerName" placeholder="请输入客户全称" maxlength="100" />
        </el-form-item>
        <el-form-item label="行业类型" prop="industryType">
          <el-select v-model="form.industryType" placeholder="请选择">
            <el-option v-for="(label, key) in IndustryTypeLabels" :key="key" :label="label" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item label="企业规模" prop="scale">
          <el-select v-model="form.scale" placeholder="请选择">
            <el-option label="大型" value="大型" />
            <el-option label="中型" value="中型" />
            <el-option label="小型" value="小型" />
          </el-select>
        </el-form-item>
        <el-form-item label="项目经理" prop="pmId">
          <el-input-number v-model="form.pmId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="区域" prop="region">
          <el-input v-model="form.region" maxlength="50" />
        </el-form-item>
        <el-form-item label="启动日期">
          <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
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
import { createProject, listProjects } from '@/api/project'
import { IndustryTypeLabels, ProjectStatusLabels } from '@/types'
import type { Project, IndustryType } from '@/types'
import { required, maxLen } from '@/utils/validators'

const projects = ref<Project[]>([])
const showDialog = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = ref({
  projectCode: '',
  customerName: '',
  industryType: '' as string,
  scale: '',
  pmId: undefined as number | undefined,
  region: '',
  startDate: '',
  remark: '',
})

const rules = {
  projectCode: [required('项目编号不能为空'), maxLen(32)],
  customerName: [required('客户名称不能为空'), maxLen(100)],
  industryType: [required('行业类型不能为空')],
  scale: [required('企业规模不能为空')],
  pmId: [required('请指定项目经理')],
}

function statusTagType(status: string) {
  const map: Record<string, string> = {
    PENDING: 'info', RESEARCH: '', PLAN: '', TRAINING: 'warning',
    DATA_IMPORT: 'warning', GO_LIVE: 'danger', DELIVERED: 'success', AFTER_SALES: 'success',
  }
  return map[status] || 'info'
}

async function loadData() {
  const res = await listProjects()
  projects.value = res.data.data
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createProject(form.value as unknown as Record<string, unknown>)
    ElMessage.success('项目创建成功')
    showDialog.value = false
    form.value = { projectCode: '', customerName: '', industryType: '', scale: '', pmId: undefined, region: '', startDate: '', remark: '' }
    await loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>
