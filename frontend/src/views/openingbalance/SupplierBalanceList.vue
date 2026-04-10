<template>
  <div>
    <div class="page-header">
      <h2>供应商往来</h2>
      <el-button type="primary" @click="showDialog = true">新增记录</el-button>
    </div>

    <div class="card">
      <div style="margin-bottom: 16px">
        <el-input-number v-model="filterProjectId" :min="1" placeholder="项目ID" controls-position="right" style="width: 180px" />
        <el-button type="primary" @click="loadData" style="margin-left: 8px">查询</el-button>
      </div>
      <el-table :data="list" stripe>
        <el-table-column prop="supplierId" label="供应商ID" width="100" />
        <el-table-column prop="docType" label="单据类型" width="100" />
        <el-table-column prop="docNo" label="单据编号" width="140" />
        <el-table-column prop="docDate" label="单据日期" width="110" />
        <el-table-column prop="balance" label="余额" width="120" />
        <el-table-column label="余额类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.balanceType === '应付' ? '' : 'warning'" size="small">
              {{ row.balanceType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" />
      </el-table>
    </div>

    <el-dialog v-model="showDialog" title="新增供应商往来" width="700px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="项目ID" prop="projectId">
              <el-input-number v-model="form.projectId" :min="1" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商ID" prop="supplierId">
              <el-input-number v-model="form.supplierId" :min="1" controls-position="right" />
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
            <el-form-item label="余额类型">
              <el-select v-model="form.balanceType" placeholder="请选择">
                <el-option label="应付" value="应付" />
                <el-option label="预付" value="预付" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="余额" prop="balance">
          <el-input-number v-model="form.balance" :step="100" :precision="2" controls-position="right" style="width: 100%" />
        </el-form-item>
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
import { createSupplierBalance, listSupplierBalances } from '@/api/supplierbalance'
import type { SupplierBalance } from '@/types'
import { required } from '@/utils/validators'

const list = ref<SupplierBalance[]>([])
const showDialog = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const filterProjectId = ref<number>(1)

const form = ref({
  projectId: 1,
  supplierId: 1,
  docType: '',
  docNo: '',
  docDate: '',
  balance: 0,
  balanceType: '应付',
  remark: '',
})

const rules = {
  projectId: [required('项目ID不能为空')],
  supplierId: [required('供应商ID不能为空')],
  balance: [required('余额不能为空')],
}

async function loadData() {
  if (!filterProjectId.value) return
  const res = await listSupplierBalances(filterProjectId.value)
  list.value = res.data.data
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createSupplierBalance(form.value as unknown as Record<string, unknown>)
    ElMessage.success('供应商往来创建成功')
    showDialog.value = false
    await loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>
