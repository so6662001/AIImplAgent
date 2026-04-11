<template>
  <div>
    <div class="page-header">
      <h2>供应商档案</h2>
      <el-button type="primary" @click="showDialog = true">新增供应商</el-button>
    </div>

    <div class="card">
      <el-table :data="suppliers" stripe>
        <el-table-column prop="supplierCode" label="供应商编码" width="130" />
        <el-table-column prop="fullName" label="全称" min-width="200" />
        <el-table-column prop="shortName" label="简称" width="100" />
        <el-table-column prop="supplierType" label="类型" width="90" />
        <el-table-column prop="contact" label="联系人" width="90" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="settlementMethod" label="结算方式" width="100" />
        <el-table-column prop="taxRate" label="税率" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'" size="small">
              {{ row.enabled ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="showDialog" title="新增供应商" width="700px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="供应商编码" prop="supplierCode">
              <el-input v-model="form.supplierCode" maxlength="32" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商类型">
              <el-select v-model="form.supplierType" placeholder="请选择">
                <el-option label="钢厂" value="钢厂" />
                <el-option label="贸易商" value="贸易商" />
                <el-option label="加工厂" value="加工厂" />
                <el-option label="物流商" value="物流商" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="供应商全称" prop="fullName">
          <el-input v-model="form.fullName" maxlength="120" />
        </el-form-item>
        <el-form-item label="供应商简称">
          <el-input v-model="form.shortName" maxlength="40" />
        </el-form-item>
        <el-form-item label="统一社会信用代码" prop="creditCode">
          <el-input v-model="form.creditCode" maxlength="18" placeholder="18位字母数字" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="联系人">
              <el-input v-model="form.contact" maxlength="30" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" maxlength="20" placeholder="手机或座机" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="详细地址">
          <el-input v-model="form.address" maxlength="200" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="结算方式">
              <el-select v-model="form.settlementMethod" placeholder="请选择">
                <el-option label="现款" value="现款" />
                <el-option label="月结" value="月结" />
                <el-option label="账期" value="账期" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="税率" prop="taxRate">
              <el-input-number v-model="form.taxRate" :min="0" :max="1" :step="0.01" :precision="2" controls-position="right" />
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
import { createSupplier, listSuppliers } from '@/api/supplier'
import type { Supplier } from '@/types'
import { required, maxLen, phoneRule, creditCodeRule, taxRateRule } from '@/utils/validators'

const suppliers = ref<Supplier[]>([])
const showDialog = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = ref({
  supplierCode: '',
  fullName: '',
  shortName: '',
  supplierType: '',
  creditCode: '',
  contact: '',
  phone: '',
  address: '',
  settlementMethod: '',
  taxRate: 0.13,
})

const rules = {
  supplierCode: [required('供应商编码不能为空'), maxLen(32)],
  fullName: [required('供应商全称不能为空'), maxLen(120)],
  creditCode: [creditCodeRule],
  phone: [phoneRule],
  taxRate: [taxRateRule],
}

async function loadData() {
  const res = await listSuppliers()
  suppliers.value = res.data.data
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createSupplier(form.value as unknown as Record<string, unknown>)
    ElMessage.success('供应商创建成功')
    showDialog.value = false
    await loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>
