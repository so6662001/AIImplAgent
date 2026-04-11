<template>
  <div>
    <div class="page-header">
      <h2>往来单位</h2>
      <el-button type="primary" @click="showDialog = true">新增往来单位</el-button>
    </div>

    <div class="card">
      <el-table :data="units" stripe>
        <el-table-column prop="unitCode" label="单位编码" width="130" />
        <el-table-column prop="fullName" label="全称" min-width="200" />
        <el-table-column prop="unitType" label="类型" width="100" />
        <el-table-column prop="contactPerson" label="联系人" width="90" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="bankName" label="开户银行" width="140" />
        <el-table-column prop="bankAccount" label="银行账号" width="180" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'" size="small">
              {{ row.enabled ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="showDialog" title="新增往来单位" width="700px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="单位编码" prop="unitCode">
              <el-input v-model="form.unitCode" maxlength="32" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位类型">
              <el-select v-model="form.unitType" placeholder="请选择">
                <el-option label="企业" value="企业" />
                <el-option label="个人" value="个人" />
                <el-option label="政府机构" value="政府机构" />
                <el-option label="关联公司" value="关联公司" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="单位全称" prop="fullName">
          <el-input v-model="form.fullName" maxlength="120" />
        </el-form-item>
        <el-form-item label="统一社会信用代码" prop="creditCode">
          <el-input v-model="form.creditCode" maxlength="18" placeholder="18位字母数字" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="联系人">
              <el-input v-model="form.contactPerson" maxlength="30" />
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
            <el-form-item label="开户银行">
              <el-input v-model="form.bankName" maxlength="60" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="银行账号">
              <el-input v-model="form.bankAccount" maxlength="30" />
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
import { createRelatedUnit, listRelatedUnits } from '@/api/relatedunit'
import type { RelatedUnit } from '@/types'
import { required, maxLen, phoneRule, creditCodeRule } from '@/utils/validators'

const units = ref<RelatedUnit[]>([])
const showDialog = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = ref({
  unitCode: '',
  fullName: '',
  unitType: '',
  creditCode: '',
  contactPerson: '',
  phone: '',
  address: '',
  bankName: '',
  bankAccount: '',
})

const rules = {
  unitCode: [required('单位编码不能为空'), maxLen(32)],
  fullName: [required('单位全称不能为空'), maxLen(120)],
  creditCode: [creditCodeRule],
  phone: [phoneRule],
}

async function loadData() {
  const res = await listRelatedUnits()
  units.value = res.data.data
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createRelatedUnit(form.value as unknown as Record<string, unknown>)
    ElMessage.success('往来单位创建成功')
    showDialog.value = false
    await loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>
