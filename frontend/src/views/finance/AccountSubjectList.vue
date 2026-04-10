<template>
  <div>
    <div class="page-header">
      <h2>财务科目</h2>
      <el-button type="primary" @click="showDialog = true">新增科目</el-button>
    </div>

    <div class="card">
      <el-table :data="subjects" stripe>
        <el-table-column prop="subjectCode" label="科目编码" width="130" />
        <el-table-column prop="subjectName" label="科目名称" min-width="180" />
        <el-table-column prop="parentCode" label="上级编码" width="120" />
        <el-table-column prop="subjectCategory" label="科目类别" width="100" />
        <el-table-column prop="balanceDirection" label="余额方向" width="100" />
        <el-table-column prop="auxiliaryAccounting" label="辅助核算" width="120" />
        <el-table-column label="末级" width="70">
          <template #default="{ row }">
            {{ row.isLeaf ? '是' : '否' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'" size="small">
              {{ row.enabled ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="showDialog" title="新增财务科目" width="700px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="科目编码" prop="subjectCode">
              <el-input v-model="form.subjectCode" maxlength="32" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="科目名称" prop="subjectName">
              <el-input v-model="form.subjectName" maxlength="60" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="上级编码">
              <el-input v-model="form.parentCode" maxlength="32" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="科目类别" prop="subjectCategory">
              <el-select v-model="form.subjectCategory" placeholder="请选择">
                <el-option label="资产" value="资产" />
                <el-option label="负债" value="负债" />
                <el-option label="权益" value="权益" />
                <el-option label="损益" value="损益" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="余额方向" prop="balanceDirection">
              <el-select v-model="form.balanceDirection" placeholder="请选择">
                <el-option label="借" value="借" />
                <el-option label="贷" value="贷" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="辅助核算">
              <el-input v-model="form.auxiliaryAccounting" maxlength="60" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="末级科目">
              <el-switch v-model="form.isLeaf" />
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
import { createSubject, listSubjects } from '@/api/subject'
import type { AccountSubject } from '@/types'
import { required, maxLen } from '@/utils/validators'

const subjects = ref<AccountSubject[]>([])
const showDialog = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = ref({
  subjectCode: '',
  subjectName: '',
  parentCode: '',
  subjectCategory: '',
  balanceDirection: '',
  auxiliaryAccounting: '',
  isLeaf: true,
  enabled: true,
})

const rules = {
  subjectCode: [required('科目编码不能为空'), maxLen(32)],
  subjectName: [required('科目名称不能为空'), maxLen(60)],
  subjectCategory: [required('请选择科目类别')],
  balanceDirection: [required('请选择余额方向')],
}

async function loadData() {
  const res = await listSubjects()
  subjects.value = res.data.data
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createSubject(form.value as unknown as Record<string, unknown>)
    ElMessage.success('科目创建成功')
    showDialog.value = false
    await loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>
