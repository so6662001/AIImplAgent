<template>
  <div>
    <div class="page-header">
      <h2>交付计划</h2>
      <el-button type="primary" @click="showDialog = true">新增计划</el-button>
    </div>

    <div class="card">
      <div style="margin-bottom: 16px">
        <el-input-number v-model="filterProjectId" :min="1" placeholder="项目ID" controls-position="right" style="width: 180px" />
        <el-button type="primary" @click="loadData" style="margin-left: 8px">查询</el-button>
      </div>
      <el-table :data="list" stripe>
        <el-table-column prop="planName" label="计划名称" min-width="180" />
        <el-table-column prop="totalDays" label="总天数" width="90" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 'COMPLETED' ? 'success' : row.status === 'IN_PROGRESS' ? '' : 'info'"
              size="small"
            >
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="milestones" label="里程碑" min-width="200" show-overflow-tooltip />
        <el-table-column prop="wbsItems" label="WBS" min-width="160" show-overflow-tooltip />
        <el-table-column prop="resources" label="资源" width="140" show-overflow-tooltip />
        <el-table-column prop="risks" label="风险" width="140" show-overflow-tooltip />
      </el-table>
    </div>

    <el-dialog v-model="showDialog" title="新增交付计划" width="700px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="项目ID" prop="projectId">
              <el-input-number v-model="form.projectId" :min="1" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划名称" prop="planName">
              <el-input v-model="form.planName" maxlength="120" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="总天数" prop="totalDays">
              <el-input-number v-model="form.totalDays" :min="1" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status" placeholder="请选择">
                <el-option label="待启动" value="PENDING" />
                <el-option label="进行中" value="IN_PROGRESS" />
                <el-option label="已完成" value="COMPLETED" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="里程碑">
          <el-input v-model="form.milestones" type="textarea" :rows="3" placeholder="JSON格式" />
        </el-form-item>
        <el-form-item label="WBS">
          <el-input v-model="form.wbsItems" type="textarea" :rows="3" placeholder="JSON格式" />
        </el-form-item>
        <el-form-item label="资源">
          <el-input v-model="form.resources" type="textarea" :rows="2" placeholder="JSON格式" />
        </el-form-item>
        <el-form-item label="风险">
          <el-input v-model="form.risks" type="textarea" :rows="2" placeholder="JSON格式" />
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
import { createProjectPlan, listProjectPlans } from '@/api/projectplan'
import type { ProjectPlan } from '@/types'
import { required } from '@/utils/validators'

const list = ref<ProjectPlan[]>([])
const showDialog = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const filterProjectId = ref<number>(1)

const form = ref({
  projectId: 1,
  planName: '',
  totalDays: 30,
  milestones: '',
  wbsItems: '',
  resources: '',
  risks: '',
  status: 'PENDING',
})

const rules = {
  projectId: [required('项目ID不能为空')],
  planName: [required('计划名称不能为空')],
  totalDays: [{
    validator: (_rule: unknown, value: number, callback: (err?: Error) => void) => {
      if (!value || value < 1) {
        callback(new Error('总天数必须大于0'))
        return
      }
      callback()
    },
    trigger: 'blur',
  }],
}

async function loadData() {
  if (!filterProjectId.value) return
  const res = await listProjectPlans(filterProjectId.value)
  list.value = res.data.data
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createProjectPlan(form.value as unknown as Record<string, unknown>)
    ElMessage.success('交付计划创建成功')
    showDialog.value = false
    await loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>
