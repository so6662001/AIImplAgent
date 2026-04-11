<template>
  <div>
    <div class="page-header">
      <h2>库存期初</h2>
      <el-button type="primary" @click="showDialog = true">新增记录</el-button>
    </div>

    <div class="card">
      <div style="margin-bottom: 16px">
        <el-input-number v-model="filterProjectId" :min="1" placeholder="项目ID" controls-position="right" style="width: 180px" />
        <el-button type="primary" @click="loadData" style="margin-left: 8px">查询</el-button>
      </div>
      <el-table :data="list" stripe>
        <el-table-column prop="productId" label="货品ID" width="90" />
        <el-table-column prop="warehouseId" label="仓库ID" width="90" />
        <el-table-column prop="batchNo" label="批号" width="120" />
        <el-table-column prop="inboundDate" label="入库日期" width="110" />
        <el-table-column prop="quantity" label="数量" width="90" />
        <el-table-column prop="weight" label="重量" width="90" />
        <el-table-column prop="packQuantity" label="件数" width="80" />
        <el-table-column prop="costUnitPrice" label="成本单价" width="100" />
        <el-table-column prop="wholeUnits" label="整件数" width="90" />
        <el-table-column prop="oddUnits" label="零散数" width="90" />
        <el-table-column prop="costAmount" label="成本金额" width="110" />
        <el-table-column prop="unitWeight" label="件均重" width="90" />
      </el-table>
    </div>

    <el-dialog v-model="showDialog" title="新增库存期初" width="700px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="项目ID" prop="projectId">
              <el-input-number v-model="form.projectId" :min="1" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="货品ID" prop="productId">
              <el-input-number v-model="form.productId" :min="1" controls-position="right" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="仓库ID" prop="warehouseId">
              <el-input-number v-model="form.warehouseId" :min="1" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库位ID">
              <el-input-number v-model="form.locationId" :min="0" controls-position="right" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="批号">
              <el-input v-model="form.batchNo" maxlength="60" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入库日期">
              <el-date-picker v-model="form.inboundDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="数量" prop="quantity">
              <el-input-number v-model="form.quantity" :min="0.01" :step="1" :precision="2" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="重量" prop="weight">
              <el-input-number v-model="form.weight" :min="0.01" :step="1" :precision="2" controls-position="right" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="件数" prop="packQuantity">
              <el-input-number v-model="form.packQuantity" :min="1" :step="1" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成本单价" prop="costUnitPrice">
              <el-input-number v-model="form.costUnitPrice" :min="0.01" :step="1" :precision="2" controls-position="right" />
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
import { createInventoryBalance, listInventoryBalances } from '@/api/inventory'
import type { InventoryBalance } from '@/types'
import { required } from '@/utils/validators'

const list = ref<InventoryBalance[]>([])
const showDialog = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const filterProjectId = ref<number>(1)

const form = ref({
  projectId: 1,
  productId: 1,
  warehouseId: 1,
  locationId: undefined as number | undefined,
  batchNo: '',
  inboundDate: '',
  quantity: 1,
  weight: 1,
  packQuantity: 1,
  costUnitPrice: 1,
})

const positiveNumber = {
  validator: (_rule: unknown, value: number, callback: (err?: Error) => void) => {
    if (value === undefined || value === null || value <= 0) {
      callback(new Error('必须大于0'))
      return
    }
    callback()
  },
  trigger: 'blur',
}

const rules = {
  projectId: [required('项目ID不能为空')],
  productId: [required('货品ID不能为空')],
  warehouseId: [required('仓库ID不能为空')],
  quantity: [positiveNumber],
  weight: [positiveNumber],
  packQuantity: [positiveNumber],
  costUnitPrice: [positiveNumber],
}

async function loadData() {
  if (!filterProjectId.value) return
  const res = await listInventoryBalances(filterProjectId.value)
  list.value = res.data.data
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createInventoryBalance(form.value as unknown as Record<string, unknown>)
    ElMessage.success('库存期初创建成功')
    showDialog.value = false
    await loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>
