<template>
  <div>
    <div class="page-header">
      <h2>仓库档案</h2>
      <el-button type="primary" @click="showDialog = true">新增仓库</el-button>
    </div>

    <div class="card">
      <el-table :data="warehouses" stripe>
        <el-table-column prop="warehouseCode" label="仓库编码" width="120" />
        <el-table-column prop="warehouseName" label="仓库名称" min-width="160" />
        <el-table-column prop="warehouseType" label="类型" width="80" />
        <el-table-column prop="warehouseNature" label="性质" width="100" />
        <el-table-column prop="managementMode" label="管理模式" width="100" />
        <el-table-column prop="contact" label="联系人" width="90" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="areaSqm" label="面积(㎡)" width="100" />
        <el-table-column prop="craneCount" label="行车数" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'" size="small">
              {{ row.enabled ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="showDialog" title="新增仓库" width="700px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="仓库编码" prop="warehouseCode">
              <el-input v-model="form.warehouseCode" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="仓库名称" prop="warehouseName">
              <el-input v-model="form.warehouseName" maxlength="60" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="仓库类型">
              <el-select v-model="form.warehouseType" placeholder="请选择">
                <el-option label="自有" value="自有" />
                <el-option label="租赁" value="租赁" />
                <el-option label="第三方" value="第三方" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="仓库性质">
              <el-select v-model="form.warehouseNature" placeholder="请选择">
                <el-option label="钢材库" value="钢材库" />
                <el-option label="成品库" value="成品库" />
                <el-option label="半成品库" value="半成品库" />
                <el-option label="废料库" value="废料库" />
                <el-option label="虚拟库" value="虚拟库" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="管理模式">
          <el-select v-model="form.managementMode" placeholder="请选择">
            <el-option label="按库位" value="按库位" />
            <el-option label="不按库位" value="不按库位" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="form.address" maxlength="200" />
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
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="面积(㎡)" prop="areaSqm">
              <el-input-number v-model="form.areaSqm" :min="0" :precision="2" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="行车数量" prop="craneCount">
              <el-input-number v-model="form.craneCount" :min="0" :precision="0" controls-position="right" />
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
import { createWarehouse, listWarehouses } from '@/api/warehouse'
import type { Warehouse } from '@/types'
import { required, maxLen, phoneRule } from '@/utils/validators'

const warehouses = ref<Warehouse[]>([])
const showDialog = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = ref({
  warehouseCode: '',
  warehouseName: '',
  warehouseType: '',
  warehouseNature: '',
  managementMode: '',
  address: '',
  contact: '',
  phone: '',
  areaSqm: 0,
  craneCount: 0,
})

const rules = {
  warehouseCode: [required('仓库编码不能为空'), maxLen(20)],
  warehouseName: [required('仓库名称不能为空'), maxLen(60)],
  phone: [phoneRule],
}

async function loadData() {
  const res = await listWarehouses()
  warehouses.value = res.data.data
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createWarehouse(form.value as unknown as Record<string, unknown>)
    ElMessage.success('仓库创建成功')
    showDialog.value = false
    await loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>
