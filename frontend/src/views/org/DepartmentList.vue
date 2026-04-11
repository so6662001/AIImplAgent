<template>
  <div>
    <div class="page-header">
      <h2>部门管理</h2>
      <el-button type="primary" @click="showDialog = true">新增部门</el-button>
    </div>

    <div class="card">
      <el-table :data="departments" stripe>
        <el-table-column prop="deptCode" label="部门编码" width="130" />
        <el-table-column prop="deptName" label="部门名称" min-width="180" />
        <el-table-column prop="parentId" label="上级部门ID" width="120" />
        <el-table-column prop="managerId" label="负责人ID" width="100" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'" size="small">
              {{ row.enabled ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="showDialog" title="新增部门" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="部门编码" prop="deptCode">
              <el-input v-model="form.deptCode" maxlength="32" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门名称" prop="deptName">
              <el-input v-model="form.deptName" maxlength="60" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="上级部门ID" prop="parentId">
              <el-input-number v-model="form.parentId" :min="0" controls-position="right" placeholder="无上级留空" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人ID">
              <el-input-number v-model="form.managerId" :min="0" controls-position="right" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="form.sortOrder" :min="0" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="启用">
              <el-switch v-model="form.enabled" />
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
import { createDepartment, listDepartments } from '@/api/department'
import type { Department } from '@/types'
import { required, maxLen } from '@/utils/validators'

const departments = ref<Department[]>([])
const showDialog = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = ref({
  deptCode: '',
  deptName: '',
  parentId: undefined as number | undefined,
  managerId: undefined as number | undefined,
  sortOrder: 0,
  enabled: true,
})

const rules = {
  deptCode: [required('部门编码不能为空'), maxLen(32)],
  deptName: [required('部门名称不能为空'), maxLen(60)],
}

async function loadData() {
  const res = await listDepartments()
  departments.value = res.data.data
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createDepartment(form.value as unknown as Record<string, unknown>)
    ElMessage.success('部门创建成功')
    showDialog.value = false
    await loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>
