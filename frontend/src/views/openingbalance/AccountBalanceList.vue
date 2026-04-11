<template>
  <div>
    <div class="page-header">
      <h2>账户余额</h2>
      <el-button type="primary" @click="showDialog = true">新增记录</el-button>
    </div>

    <div class="card">
      <div style="margin-bottom: 16px">
        <el-input-number v-model="filterProjectId" :min="1" placeholder="项目ID" controls-position="right" style="width: 180px" />
        <el-button type="primary" @click="loadData" style="margin-left: 8px">查询</el-button>
      </div>
      <el-table :data="list" stripe>
        <el-table-column prop="bankAccountId" label="账户ID" width="100" />
        <el-table-column prop="currency" label="币种" width="80" />
        <el-table-column prop="openingBalance" label="期初余额" width="140" />
        <el-table-column prop="remark" label="备注" min-width="200" />
      </el-table>
    </div>

    <el-dialog v-model="showDialog" title="新增账户余额" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="项目ID" prop="projectId">
              <el-input-number v-model="form.projectId" :min="1" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="账户ID" prop="bankAccountId">
              <el-input-number v-model="form.bankAccountId" :min="1" controls-position="right" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="币种">
              <el-input v-model="form.currency" maxlength="10" placeholder="CNY" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="期初余额" prop="openingBalance">
              <el-input-number v-model="form.openingBalance" :step="100" :precision="2" controls-position="right" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="form.remark" maxlength="200" />
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
import { createAccountBalance, listAccountBalances } from '@/api/accountbalance'
import type { AccountBalance } from '@/types'
import { required } from '@/utils/validators'

const list = ref<AccountBalance[]>([])
const showDialog = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const filterProjectId = ref<number>(1)

const form = ref({
  projectId: 1,
  bankAccountId: 1,
  currency: 'CNY',
  openingBalance: 0,
  remark: '',
})

const rules = {
  projectId: [required('项目ID不能为空')],
  bankAccountId: [required('账户ID不能为空')],
  openingBalance: [required('期初余额不能为空')],
}

async function loadData() {
  if (!filterProjectId.value) return
  const res = await listAccountBalances(filterProjectId.value)
  list.value = res.data.data
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createAccountBalance(form.value as unknown as Record<string, unknown>)
    ElMessage.success('账户余额创建成功')
    showDialog.value = false
    await loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>
