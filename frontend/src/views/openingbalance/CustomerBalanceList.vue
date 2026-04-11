<template>
  <div>
    <div class="page-header">
      <h2>客户往来</h2>
      <el-button type="primary" @click="showDialog = true">新增记录</el-button>
    </div>

    <div class="card">
      <div style="margin-bottom: 16px">
        <el-input-number v-model="filterProjectId" :min="1" placeholder="项目ID" controls-position="right" style="width: 180px" />
        <el-button type="primary" @click="loadData" style="margin-left: 8px">查询</el-button>
      </div>
      <el-table :data="list" stripe>
        <el-table-column prop="customerId" label="客户ID" width="90" />
        <el-table-column prop="docType" label="单据类型" width="100" />
        <el-table-column prop="docNo" label="单据编号" width="140" />
        <el-table-column prop="docDate" label="单据日期" width="110" />
        <el-table-column prop="receivableAmount" label="应收金额" width="110" />
        <el-table-column prop="receivedAmount" label="已收金额" width="110" />
        <el-table-column prop="balance" label="余额" width="110" />
        <el-table-column label="余额类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.balanceType === '应收' ? '' : 'warning'" size="small">
              {{ row.balanceType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="expectedDate" label="预计日期" width="110" />
        <el-table-column prop="remark" label="备注" min-width="140" />
      </el-table>
    </div>

    <el-dialog v-model="showDialog" title="新增客户往来" width="700px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="项目ID" prop="projectId">
              <el-input-number v-model="form.projectId" :min="1" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户ID" prop="customerId">
              <el-input-number v-model="form.customerId" :min="1" controls-position="right" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="单据类型">
              <el-input v-model="form.docType" maxlength="30" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单据编号">
              <el-input v-model="form.docNo" maxlength="60" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="单据日期">
              <el-date-picker v-model="form.docDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预计日期">
              <el-date-picker v-model="form.expectedDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="应收金额">
              <el-input-number v-model="form.receivableAmount" :step="100" :precision="2" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="已收金额">
              <el-input-number v-model="form.receivedAmount" :step="100" :precision="2" controls-position="right" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="余额" prop="balance">
              <el-input-number v-model="form.balance" :step="100" :precision="2" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="余额类型">
              <el-select v-model="form.balanceType" placeholder="请选择">
                <el-option label="应收" value="应收" />
                <el-option label="预收" value="预收" />
              </el-select>
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
import { createCustomerBalance, listCustomerBalances } from '@/api/customerbalance'
import type { CustomerBalance } from '@/types'
import { required } from '@/utils/validators'

const list = ref<CustomerBalance[]>([])
const showDialog = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const filterProjectId = ref<number>(1)

const form = ref({
  projectId: 1,
  customerId: 1,
  docType: '',
  docNo: '',
  docDate: '',
  receivableAmount: 0,
  receivedAmount: 0,
  balance: 0,
  balanceType: '应收',
  expectedDate: '',
  remark: '',
})

const rules = {
  projectId: [required('项目ID不能为空')],
  customerId: [required('客户ID不能为空')],
  balance: [required('余额不能为空')],
}

async function loadData() {
  if (!filterProjectId.value) return
  const res = await listCustomerBalances(filterProjectId.value)
  list.value = res.data.data
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createCustomerBalance(form.value as unknown as Record<string, unknown>)
    ElMessage.success('客户往来创建成功')
    showDialog.value = false
    await loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>
