<template>
  <div>
    <div class="page-header">
      <h2>调研分析</h2>
      <el-button type="primary" @click="showDialog = true">新建调研档案</el-button>
    </div>

    <div class="card" style="margin-bottom: 20px;">
      <el-form inline>
        <el-form-item label="项目ID">
          <el-input-number v-model="filterProjectId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="card">
      <el-table :data="list" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="projectId" label="项目ID" width="80" />
        <el-table-column prop="companyName" label="公司名称" min-width="160" />
        <el-table-column prop="industryType" label="行业类型" width="120">
          <template #default="{ row }">
            {{ IndustryTypeLabels[row.industryType as IndustryType] || row.industryType }}
          </template>
        </el-table-column>
        <el-table-column prop="businessModel" label="经营模式" width="120" />
        <el-table-column prop="tradeMode" label="贸易模式" width="100" />
        <el-table-column prop="tradeScope" label="贸易范围" width="100" />
        <el-table-column prop="totalStaff" label="员工总数" width="100" />
        <el-table-column prop="monthlyVolume" label="月销量" width="100" />
        <el-table-column prop="monthlyAmount" label="月销额" width="100" />
      </el-table>
    </div>

    <el-dialog v-model="showDialog" title="新建调研档案" width="720px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="项目ID" prop="projectId">
          <el-input-number v-model="form.projectId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="公司名称" prop="companyName">
          <el-input v-model="form.companyName" maxlength="100" />
        </el-form-item>
        <el-form-item label="行业类型" prop="industryType">
          <el-select v-model="form.industryType" placeholder="请选择">
            <el-option v-for="(label, key) in IndustryTypeLabels" :key="key" :label="label" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item label="经营模式" prop="businessModel">
          <el-select v-model="form.businessModel" placeholder="请选择">
            <el-option label="钢贸商" value="钢贸商" />
            <el-option label="钢厂" value="钢厂" />
            <el-option label="加工中心" value="加工中心" />
            <el-option label="综合服务商" value="综合服务商" />
          </el-select>
        </el-form-item>
        <el-form-item label="贸易模式" prop="tradeMode">
          <el-select v-model="form.tradeMode" placeholder="请选择">
            <el-option label="直营" value="直营" />
            <el-option label="代理" value="代理" />
            <el-option label="混合" value="混合" />
          </el-select>
        </el-form-item>
        <el-form-item label="贸易范围" prop="tradeScope">
          <el-select v-model="form.tradeScope" placeholder="请选择">
            <el-option label="内贸" value="内贸" />
            <el-option label="外贸" value="外贸" />
            <el-option label="内外贸" value="内外贸" />
          </el-select>
        </el-form-item>
        <el-form-item label="主营业务" prop="mainBusiness">
          <el-input v-model="form.mainBusiness" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="产线数量" prop="totalProductionLines">
          <el-input-number v-model="form.totalProductionLines" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="仓库数量" prop="totalWarehouseCount">
          <el-input-number v-model="form.totalWarehouseCount" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="仓库面积(㎡)" prop="totalWarehouseAreaSqm">
          <el-input-number v-model="form.totalWarehouseAreaSqm" :min="0" :precision="2" controls-position="right" />
        </el-form-item>
        <el-form-item label="行车数量" prop="totalCraneCount">
          <el-input-number v-model="form.totalCraneCount" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="月销量(吨)" prop="monthlyVolume">
          <el-input-number v-model="form.monthlyVolume" :min="0" :precision="2" controls-position="right" />
        </el-form-item>
        <el-form-item label="月销额(万)" prop="monthlyAmount">
          <el-input-number v-model="form.monthlyAmount" :min="0" :precision="2" controls-position="right" />
        </el-form-item>
        <el-form-item label="员工总数" prop="totalStaff">
          <el-input-number v-model="form.totalStaff" :min="0" :max="100000" controls-position="right" />
        </el-form-item>
        <el-form-item label="管理目标" prop="managementGoals">
          <el-input v-model="form.managementGoals" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="流程目标" prop="processGoals">
          <el-input v-model="form.processGoals" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="效率目标" prop="efficiencyGoals">
          <el-input v-model="form.efficiencyGoals" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="风控目标" prop="riskControlGoals">
          <el-input v-model="form.riskControlGoals" type="textarea" :rows="2" />
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
import { ref } from 'vue'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import { createCustomerProfile, listCustomerProfiles } from '@/api/research'
import { IndustryTypeLabels } from '@/types'
import type { CustomerProfile, IndustryType } from '@/types'
import { required, maxLen } from '@/utils/validators'

const list = ref<CustomerProfile[]>([])
const showDialog = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const filterProjectId = ref(1)

const initForm = () => ({
  projectId: undefined as number | undefined,
  companyName: '',
  industryType: '',
  businessModel: '',
  tradeMode: '',
  tradeScope: '',
  mainBusiness: '',
  totalProductionLines: 0,
  totalWarehouseCount: 0,
  totalWarehouseAreaSqm: 0,
  totalCraneCount: 0,
  monthlyVolume: 0,
  monthlyAmount: 0,
  totalStaff: 0,
  managementGoals: '',
  processGoals: '',
  efficiencyGoals: '',
  riskControlGoals: '',
})

const form = ref(initForm())

const rules = {
  projectId: [required('项目ID不能为空')],
  companyName: [required('公司名称不能为空'), maxLen(100)],
  industryType: [required('行业类型不能为空')],
  businessModel: [required('经营模式不能为空')],
  tradeMode: [required('贸易模式不能为空')],
  tradeScope: [required('贸易范围不能为空')],
}

async function loadData() {
  const res = await listCustomerProfiles(filterProjectId.value)
  list.value = res.data.data
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createCustomerProfile(form.value as unknown as Record<string, unknown>)
    ElMessage.success('调研档案创建成功')
    showDialog.value = false
    form.value = initForm()
    await loadData()
  } finally {
    submitting.value = false
  }
}
</script>
