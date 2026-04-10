<template>
  <div>
    <div class="page-header">
      <h2>员工管理</h2>
      <el-button type="primary" @click="showDialog = true">新增员工</el-button>
    </div>

    <div class="card">
      <el-table :data="employees" stripe>
        <el-table-column prop="employeeCode" label="员工编码" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="70" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="deptId" label="部门ID" width="90" />
        <el-table-column prop="position" label="职位" width="120" />
        <el-table-column prop="joinDate" label="入职日期" width="120" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'" size="small">
              {{ row.enabled ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="showDialog" title="新增员工" width="700px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="员工编码" prop="employeeCode">
              <el-input v-model="form.employeeCode" maxlength="32" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" maxlength="30" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="性别">
              <el-select v-model="form.gender" placeholder="请选择">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="form.idCard" maxlength="18" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" maxlength="60" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="部门ID" prop="deptId">
              <el-input-number v-model="form.deptId" :min="1" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职位">
              <el-input v-model="form.position" maxlength="60" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="入职日期">
              <el-date-picker v-model="form.joinDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
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
import { createEmployee, listEmployees } from '@/api/employee'
import type { EmployeeRecord } from '@/types'
import { required, maxLen, phoneRule } from '@/utils/validators'

const employees = ref<EmployeeRecord[]>([])
const showDialog = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = ref({
  employeeCode: '',
  name: '',
  gender: '男',
  idCard: '',
  phone: '',
  email: '',
  deptId: 1,
  position: '',
  joinDate: '',
  enabled: true,
})

const rules = {
  employeeCode: [required('员工编码不能为空'), maxLen(32)],
  name: [required('姓名不能为空'), maxLen(30)],
  idCard: [{ pattern: /^[0-9Xx]{18}$/, message: '身份证号必须为18位', trigger: 'blur' }],
  phone: [phoneRule],
  email: [{ type: 'email' as const, message: '请输入正确的邮箱地址', trigger: 'blur' }],
  deptId: [required('部门ID不能为空')],
}

async function loadData() {
  const res = await listEmployees()
  employees.value = res.data.data
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createEmployee(form.value as unknown as Record<string, unknown>)
    ElMessage.success('员工创建成功')
    showDialog.value = false
    await loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>
