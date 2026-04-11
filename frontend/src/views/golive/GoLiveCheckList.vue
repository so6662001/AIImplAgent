<template>
  <div>
    <div class="page-header">
      <h2>上线检查</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleInit">初始化检查清单</el-button>
        <el-button @click="handleReadiness">评估上线准入</el-button>
      </div>
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
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="itemName" label="检查项" min-width="160" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column label="结果" width="90">
          <template #default="{ row }">
            <el-tag :type="checkResultTagType(row.checkResult)" size="small">
              {{ CheckResultLabels[row.checkResult] || row.checkResult }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="detail" label="详情" min-width="150" />
        <el-table-column prop="checkedBy" label="检查人" width="90" />
        <el-table-column prop="checkedAt" label="检查时间" width="160" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleCheckClick(row)">检查</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="showCheckDialog" title="执行检查" width="480px" destroy-on-close>
      <el-form ref="checkFormRef" :model="checkForm" :rules="checkRules" label-width="80px">
        <el-form-item label="检查结果" prop="checkResult">
          <el-select v-model="checkForm.checkResult" placeholder="请选择">
            <el-option label="通过" value="PASS" />
            <el-option label="未通过" value="FAIL" />
            <el-option label="警告" value="WARNING" />
          </el-select>
        </el-form-item>
        <el-form-item label="详情" prop="detail">
          <el-input v-model="checkForm.detail" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="检查人" prop="checkedBy">
          <el-input v-model="checkForm.checkedBy" maxlength="30" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCheckDialog = false">取消</el-button>
        <el-button type="primary" :loading="checking" @click="handleCheckSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showReadinessDialog" title="上线准入评估" width="480px">
      <el-result
        v-if="readinessResult"
        :icon="readinessResult.ready ? 'success' : 'error'"
        :title="readinessResult.ready ? '允许上线' : '不允许上线'"
        :sub-title="readinessResult.message"
      >
        <template #extra>
          <p>通过项: {{ readinessResult.passCount }} / {{ readinessResult.totalCount }}</p>
        </template>
      </el-result>
      <template #footer>
        <el-button @click="showReadinessDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import { initChecklist, listCheckItems, updateCheckItem, getReadiness } from '@/api/golive'
import { CheckResultLabels } from '@/types'
import type { GoLiveCheckItem } from '@/types'
import { required } from '@/utils/validators'

const list = ref<GoLiveCheckItem[]>([])
const showCheckDialog = ref(false)
const showReadinessDialog = ref(false)
const checking = ref(false)
const checkFormRef = ref<FormInstance>()
const filterProjectId = ref(1)
const currentCheckId = ref(0)
const readinessResult = ref<{ ready: boolean; message: string; passCount: number; totalCount: number } | null>(null)

const checkForm = ref({
  checkResult: '',
  detail: '',
  checkedBy: '',
})

const checkRules = {
  checkResult: [required('检查结果不能为空')],
  checkedBy: [required('检查人不能为空')],
}

function checkResultTagType(result: string) {
  const map: Record<string, string> = {
    PASS: 'success',
    FAIL: 'danger',
    WARNING: 'warning',
    UNCHECKED: 'info',
  }
  return map[result] || 'info'
}

async function loadData() {
  const res = await listCheckItems(filterProjectId.value)
  list.value = res.data.data
}

async function handleInit() {
  await initChecklist(filterProjectId.value)
  ElMessage.success('检查清单初始化成功')
  await loadData()
}

function handleCheckClick(row: GoLiveCheckItem) {
  currentCheckId.value = row.id
  checkForm.value = { checkResult: '', detail: '', checkedBy: '' }
  showCheckDialog.value = true
}

async function handleCheckSubmit() {
  const valid = await checkFormRef.value?.validate().catch(() => false)
  if (!valid) return

  checking.value = true
  try {
    await updateCheckItem(currentCheckId.value, checkForm.value as unknown as Record<string, unknown>)
    ElMessage.success('检查完成')
    showCheckDialog.value = false
    await loadData()
  } finally {
    checking.value = false
  }
}

async function handleReadiness() {
  readinessResult.value = null
  showReadinessDialog.value = true
  const res = await getReadiness(filterProjectId.value)
  readinessResult.value = res.data.data
}
</script>

<style scoped>
.header-actions {
  display: flex;
  gap: 8px;
}
</style>
