<template>
  <div>
    <div class="page-header">
      <h2>模拟演练</h2>
      <el-button type="primary" @click="showCreateDialog = true">新建场景</el-button>
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
        <el-table-column prop="sceneName" label="场景名称" min-width="160" />
        <el-table-column prop="sceneType" label="场景类型" width="120" />
        <el-table-column prop="description" label="描述" min-width="160" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="simStatusType(row.status)" size="small">
              {{ SimulationStatusLabels[row.status] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="executedBy" label="执行人" width="90" />
        <el-table-column prop="executedAt" label="执行时间" width="160" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING' || row.status === 'RUNNING'"
              type="primary"
              link
              size="small"
              @click="handleExecuteClick(row)"
            >执行</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="showCreateDialog" title="新建模拟场景" width="640px" destroy-on-close>
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
        <el-form-item label="项目ID" prop="projectId">
          <el-input-number v-model="createForm.projectId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="场景名称" prop="sceneName">
          <el-input v-model="createForm.sceneName" maxlength="100" />
        </el-form-item>
        <el-form-item label="场景类型" prop="sceneType">
          <el-select v-model="createForm.sceneType" placeholder="请选择">
            <el-option label="采购流程" value="采购流程" />
            <el-option label="销售流程" value="销售流程" />
            <el-option label="库存管理" value="库存管理" />
            <el-option label="财务结算" value="财务结算" />
            <el-option label="生产加工" value="生产加工" />
            <el-option label="综合场景" value="综合场景" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="createForm.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="步骤" prop="steps">
          <el-input v-model="createForm.steps" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="预期结果" prop="expectedResult">
          <el-input v-model="createForm.expectedResult" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreate">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showExecuteDialog" title="执行模拟场景" width="560px" destroy-on-close>
      <el-form ref="execFormRef" :model="execForm" :rules="execRules" label-width="100px">
        <el-form-item label="实际结果" prop="actualResult">
          <el-input v-model="execForm.actualResult" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="execForm.status" placeholder="请选择">
            <el-option label="通过" value="PASSED" />
            <el-option label="未通过" value="FAILED" />
          </el-select>
        </el-form-item>
        <el-form-item label="偏差" prop="deviation">
          <el-input v-model="execForm.deviation" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="执行人" prop="executedBy">
          <el-input v-model="execForm.executedBy" maxlength="30" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showExecuteDialog = false">取消</el-button>
        <el-button type="primary" :loading="executing" @click="handleExecute">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import { createScene, listScenes, executeScene } from '@/api/simulation'
import { SimulationStatusLabels } from '@/types'
import type { SimulationScene } from '@/types'
import { required, maxLen } from '@/utils/validators'

const list = ref<SimulationScene[]>([])
const showCreateDialog = ref(false)
const showExecuteDialog = ref(false)
const creating = ref(false)
const executing = ref(false)
const createFormRef = ref<FormInstance>()
const execFormRef = ref<FormInstance>()
const filterProjectId = ref(1)
const currentExecId = ref(0)

const initCreateForm = () => ({
  projectId: undefined as number | undefined,
  sceneName: '',
  sceneType: '',
  description: '',
  steps: '',
  expectedResult: '',
})

const createForm = ref(initCreateForm())

const createRules = {
  projectId: [required('项目ID不能为空')],
  sceneName: [required('场景名称不能为空'), maxLen(100)],
  sceneType: [required('场景类型不能为空')],
  steps: [required('步骤不能为空')],
}

const execForm = ref({
  actualResult: '',
  status: '',
  deviation: '',
  executedBy: '',
})

const execRules = {
  actualResult: [required('实际结果不能为空')],
  status: [required('状态不能为空')],
  executedBy: [required('执行人不能为空')],
}

function simStatusType(status: string) {
  const map: Record<string, string> = {
    PENDING: 'info',
    RUNNING: 'warning',
    PASSED: 'success',
    FAILED: 'danger',
  }
  return map[status] || 'info'
}

async function loadData() {
  const res = await listScenes(filterProjectId.value)
  list.value = res.data.data
}

function handleExecuteClick(row: SimulationScene) {
  currentExecId.value = row.id
  execForm.value = { actualResult: '', status: '', deviation: '', executedBy: '' }
  showExecuteDialog.value = true
}

async function handleCreate() {
  const valid = await createFormRef.value?.validate().catch(() => false)
  if (!valid) return

  creating.value = true
  try {
    await createScene(createForm.value as unknown as Record<string, unknown>)
    ElMessage.success('场景创建成功')
    showCreateDialog.value = false
    createForm.value = initCreateForm()
    await loadData()
  } finally {
    creating.value = false
  }
}

async function handleExecute() {
  const valid = await execFormRef.value?.validate().catch(() => false)
  if (!valid) return

  executing.value = true
  try {
    await executeScene(currentExecId.value, execForm.value as unknown as Record<string, unknown>)
    ElMessage.success('场景执行完成')
    showExecuteDialog.value = false
    await loadData()
  } finally {
    executing.value = false
  }
}
</script>
