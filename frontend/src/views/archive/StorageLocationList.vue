<template>
  <div>
    <div class="page-header">
      <h2>库位管理</h2>
      <el-button type="primary" @click="showDialog = true">新增库位</el-button>
    </div>

    <div class="card">
      <div style="margin-bottom: 16px">
        <el-input-number v-model="filterWarehouseId" :min="1" placeholder="仓库ID" controls-position="right" style="width: 180px" />
        <el-button type="primary" @click="loadData" style="margin-left: 8px">查询</el-button>
      </div>
      <el-table :data="locations" stripe>
        <el-table-column prop="locationCode" label="库位编码" width="130" />
        <el-table-column prop="locationName" label="库位名称" min-width="180" />
        <el-table-column prop="warehouseId" label="仓库ID" width="100" />
        <el-table-column prop="locationType" label="库位类型" width="120" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'" size="small">
              {{ row.enabled ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="showDialog" title="新增库位" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="库位编码" prop="locationCode">
              <el-input v-model="form.locationCode" maxlength="32" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库位名称" prop="locationName">
              <el-input v-model="form.locationName" maxlength="60" />
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
            <el-form-item label="库位类型">
              <el-select v-model="form.locationType" placeholder="请选择">
                <el-option label="普通库位" value="普通库位" />
                <el-option label="暂存区" value="暂存区" />
                <el-option label="待检区" value="待检区" />
                <el-option label="退货区" value="退货区" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" />
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
import { createStorageLocation, listStorageLocations } from '@/api/storagelocation'
import type { StorageLocation } from '@/types'
import { required, maxLen } from '@/utils/validators'

const locations = ref<StorageLocation[]>([])
const showDialog = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const filterWarehouseId = ref<number | undefined>(undefined)

const form = ref({
  locationCode: '',
  locationName: '',
  warehouseId: 1,
  locationType: '',
  enabled: true,
})

const rules = {
  locationCode: [required('库位编码不能为空'), maxLen(32)],
  locationName: [required('库位名称不能为空'), maxLen(60)],
  warehouseId: [required('仓库ID不能为空')],
}

async function loadData() {
  const res = await listStorageLocations(filterWarehouseId.value)
  locations.value = res.data.data
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createStorageLocation(form.value as unknown as Record<string, unknown>)
    ElMessage.success('库位创建成功')
    showDialog.value = false
    await loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>
