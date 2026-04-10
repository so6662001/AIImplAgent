<template>
  <div>
    <div class="page-header">
      <h2>发票期初</h2>
      <el-button type="primary" @click="showDialog = true">新增记录</el-button>
    </div>

    <div class="card">
      <div style="margin-bottom: 16px">
        <el-input-number v-model="filterProjectId" :min="1" placeholder="项目ID" controls-position="right" style="width: 180px" />
        <el-select v-model="filterInvoiceType" placeholder="发票类型" clearable style="width: 180px; margin-left: 8px">
          <el-option label="应开销项" value="SALES_OUTPUT" />
          <el-option label="应收进项" value="PURCHASE_INPUT" />
        </el-select>
        <el-button type="primary" @click="loadData" style="margin-left: 8px">查询</el-button>
      </div>
      <el-table :data="list" stripe>
        <el-table-column label="发票类型" width="100">
          <template #default="{ row }">
            {{ row.invoiceType === 'SALES_OUTPUT' ? '应开销项' : '应收进项' }}
          </template>
        </el-table-column>
        <el-table-column prop="counterpartyName" label="对方名称" min-width="140" />
        <el-table-column prop="docNo" label="单据编号" width="130" />
        <el-table-column prop="productName" label="货品名称" width="120" />
        <el-table-column prop="spec" label="规格" width="100" />
        <el-table-column prop="material" label="材质" width="90" />
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="unitPrice" label="单价" width="90" />
        <el-table-column prop="amount" label="金额" width="100" />
        <el-table-column prop="taxRate" label="税率" width="70" />
        <el-table-column prop="taxAmount" label="税额" width="100" />
        <el-table-column prop="totalAmount" label="价税合计" width="110" />
        <el-table-column prop="docDate" label="日期" width="110" />
      </el-table>
    </div>

    <el-dialog v-model="showDialog" title="新增发票期初" width="700px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="项目ID" prop="projectId">
              <el-input-number v-model="form.projectId" :min="1" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发票类型" prop="invoiceType">
              <el-select v-model="form.invoiceType" placeholder="请选择">
                <el-option label="应开销项" value="SALES_OUTPUT" />
                <el-option label="应收进项" value="PURCHASE_INPUT" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="对方ID" prop="counterpartyId">
              <el-input-number v-model="form.counterpartyId" :min="1" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="对方名称">
              <el-input v-model="form.counterpartyName" maxlength="120" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="单据编号">
              <el-input v-model="form.docNo" maxlength="60" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="货品ID">
              <el-input-number v-model="form.productId" :min="0" controls-position="right" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="货品名称">
              <el-input v-model="form.productName" maxlength="60" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="规格">
              <el-input v-model="form.spec" maxlength="60" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="材质">
              <el-input v-model="form.material" maxlength="60" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="数量" prop="quantity">
              <el-input-number v-model="form.quantity" :min="0.01" :step="1" :precision="2" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="单价" prop="unitPrice">
              <el-input-number v-model="form.unitPrice" :min="0.01" :step="1" :precision="2" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="税率" prop="taxRate">
              <el-input-number v-model="form.taxRate" :min="0" :max="1" :step="0.01" :precision="2" controls-position="right" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="日期">
              <el-date-picker v-model="form.docDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
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
import { createInvoiceBalance, listInvoiceBalances } from '@/api/invoice'
import type { InvoiceBalance } from '@/types'
import { required } from '@/utils/validators'

const list = ref<InvoiceBalance[]>([])
const showDialog = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const filterProjectId = ref<number>(1)
const filterInvoiceType = ref('')

const form = ref({
  projectId: 1,
  invoiceType: '',
  counterpartyId: 1,
  counterpartyName: '',
  docNo: '',
  productId: 0,
  productName: '',
  spec: '',
  material: '',
  quantity: 1,
  unitPrice: 1,
  taxRate: 0.13,
  docDate: '',
})

const rules = {
  projectId: [required('项目ID不能为空')],
  invoiceType: [required('请选择发票类型')],
  counterpartyId: [required('对方ID不能为空')],
  quantity: [required('数量不能为空')],
  unitPrice: [required('单价不能为空')],
  taxRate: [required('税率不能为空')],
}

async function loadData() {
  if (!filterProjectId.value) return
  const res = await listInvoiceBalances(filterProjectId.value, filterInvoiceType.value || undefined)
  list.value = res.data.data
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createInvoiceBalance(form.value as unknown as Record<string, unknown>)
    ElMessage.success('发票期初创建成功')
    showDialog.value = false
    await loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>
