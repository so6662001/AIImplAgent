<template>
  <div>
    <div class="page-header">
      <h2>银行账号</h2>
      <el-button type="primary" @click="showDialog = true">新增账号</el-button>
    </div>

    <div class="card">
      <el-table :data="accounts" stripe>
        <el-table-column prop="accountCode" label="账号编码" width="120" />
        <el-table-column prop="accountName" label="账号名称" min-width="160" />
        <el-table-column prop="accountType" label="账号类型" width="100" />
        <el-table-column prop="bankName" label="开户行" min-width="160" />
        <el-table-column prop="bankAccountNo" label="银行账号" width="200" />
        <el-table-column prop="bankBranch" label="支行" width="140" />
        <el-table-column prop="currency" label="币种" width="80" />
        <el-table-column prop="subjectCode" label="科目编码" width="120" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'" size="small">
              {{ row.enabled ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="showDialog" title="新增银行账号" width="700px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="账号编码" prop="accountCode">
              <el-input v-model="form.accountCode" maxlength="32" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="账号名称" prop="accountName">
              <el-input v-model="form.accountName" maxlength="60" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="账号类型" prop="accountType">
              <el-select v-model="form.accountType" placeholder="请选择">
                <el-option label="银行" value="银行" />
                <el-option label="现金" value="现金" />
                <el-option label="微信" value="微信" />
                <el-option label="支付宝" value="支付宝" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="币种">
              <el-input v-model="form.currency" maxlength="10" placeholder="CNY" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="开户行">
          <el-input v-model="form.bankName" maxlength="120" />
        </el-form-item>
        <el-form-item label="银行账号">
          <el-input v-model="form.bankAccountNo" maxlength="40" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="支行">
              <el-input v-model="form.bankBranch" maxlength="120" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="科目编码">
              <el-input v-model="form.subjectCode" maxlength="32" />
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
import { createBankAccount, listBankAccounts } from '@/api/bankaccount'
import type { BankAccount } from '@/types'
import { required, maxLen } from '@/utils/validators'

const accounts = ref<BankAccount[]>([])
const showDialog = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = ref({
  accountCode: '',
  accountName: '',
  accountType: '',
  bankName: '',
  bankAccountNo: '',
  bankBranch: '',
  currency: 'CNY',
  subjectCode: '',
  enabled: true,
})

const rules = {
  accountCode: [required('账号编码不能为空'), maxLen(32)],
  accountName: [required('账号名称不能为空'), maxLen(60)],
  accountType: [required('请选择账号类型')],
}

async function loadData() {
  const res = await listBankAccounts()
  accounts.value = res.data.data
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createBankAccount(form.value as unknown as Record<string, unknown>)
    ElMessage.success('银行账号创建成功')
    showDialog.value = false
    await loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>
