<template>
  <div>
    <div class="page-header">
      <h2>帐套管理</h2>
      <el-button type="primary" @click="showDialog = true">新建帐套</el-button>
    </div>

    <div class="card" style="margin-bottom: 20px;">
      <h3 style="margin-top: 0;">智能推荐</h3>
      <el-form inline>
        <el-form-item label="项目ID">
          <el-input-number v-model="recommendProjectId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item>
          <el-button type="success" :loading="recommendLoading" @click="handleGetRecommendation">获取推荐配置</el-button>
        </el-form-item>
      </el-form>

      <el-card v-if="recommendation" shadow="hover" style="margin-top: 12px;">
        <template #header>
          <div style="display: flex; align-items: center; justify-content: space-between;">
            <span>推荐配置</span>
            <el-tag
              :type="recommendation.confidence === 'HIGH' ? 'success' : 'warning'"
              size="small"
            >
              置信度: {{ recommendation.confidence }}
            </el-tag>
          </div>
        </template>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="帐套名称">{{ recommendation.recommendedSetName }}</el-descriptions-item>
          <el-descriptions-item label="会计制度">{{ recommendation.recommendedAccountingSystem }}</el-descriptions-item>
          <el-descriptions-item label="计价方式">{{ recommendation.recommendedPricingMethod }}</el-descriptions-item>
          <el-descriptions-item label="使用重量">
            <el-tag :type="recommendation.recommendedUseWeight ? 'success' : 'info'" size="small">
              {{ recommendation.recommendedUseWeight ? '是' : '否' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="数量小数位">{{ recommendation.qtyDecimals }}</el-descriptions-item>
          <el-descriptions-item label="重量小数位">{{ recommendation.wgtDecimals }}</el-descriptions-item>
          <el-descriptions-item label="单价小数位">{{ recommendation.prcDecimals }}</el-descriptions-item>
          <el-descriptions-item label="金额小数位">{{ recommendation.amtDecimals }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="recommendation.recommendations && recommendation.recommendations.length" style="margin-top: 12px;">
          <strong>建议:</strong>
          <ul style="margin: 4px 0 0 0; padding-left: 20px;">
            <li v-for="(item, idx) in recommendation.recommendations" :key="idx">{{ item }}</li>
          </ul>
        </div>
        <div style="margin-top: 16px; text-align: right;">
          <el-button type="primary" :loading="createFromRecLoading" @click="handleCreateFromRecommendation">一键创建帐套</el-button>
        </div>
      </el-card>
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
        <el-table-column prop="setName" label="帐套名称" min-width="140" />
        <el-table-column prop="accountingSystem" label="会计制度" width="140" />
        <el-table-column prop="pricingMethod" label="计价方式" width="120" />
        <el-table-column prop="currency" label="币种" width="80" />
        <el-table-column label="使用重量" width="90">
          <template #default="{ row }">
            <el-tag :type="row.useWeight ? 'success' : 'info'" size="small">
              {{ row.useWeight ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
              {{ row.status === 'ACTIVE' ? '已启用' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'DRAFT'"
              type="primary"
              link
              size="small"
              @click="handleActivate(row.id)"
            >启用</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="showDialog" title="新建帐套" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="项目ID" prop="projectId">
          <el-input-number v-model="form.projectId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="帐套名称" prop="setName">
          <el-input v-model="form.setName" maxlength="60" />
        </el-form-item>
        <el-form-item label="会计制度" prop="accountingSystem">
          <el-select v-model="form.accountingSystem" placeholder="请选择">
            <el-option label="企业会计准则" value="企业会计准则" />
            <el-option label="小企业会计准则" value="小企业会计准则" />
          </el-select>
        </el-form-item>
        <el-form-item label="计价方式" prop="pricingMethod">
          <el-select v-model="form.pricingMethod" placeholder="请选择">
            <el-option label="移动加权" value="移动加权" />
            <el-option label="先进先出" value="先进先出" />
            <el-option label="个别计价" value="个别计价" />
          </el-select>
        </el-form-item>
        <el-form-item label="数量小数位" prop="qtyDecimals">
          <el-input-number v-model="form.qtyDecimals" :min="0" :max="4" controls-position="right" />
        </el-form-item>
        <el-form-item label="重量小数位" prop="wgtDecimals">
          <el-input-number v-model="form.wgtDecimals" :min="0" :max="4" controls-position="right" />
        </el-form-item>
        <el-form-item label="单价小数位" prop="prcDecimals">
          <el-input-number v-model="form.prcDecimals" :min="0" :max="4" controls-position="right" />
        </el-form-item>
        <el-form-item label="金额小数位" prop="amtDecimals">
          <el-input-number v-model="form.amtDecimals" :min="0" :max="2" controls-position="right" />
        </el-form-item>
        <el-form-item label="使用重量" prop="useWeight">
          <el-switch v-model="form.useWeight" active-text="是" inactive-text="否" />
        </el-form-item>
        <el-form-item label="币种" prop="currency">
          <el-input v-model="form.currency" maxlength="10" />
        </el-form-item>
        <el-form-item label="财年起始月" prop="fiscalYearStart">
          <el-input-number v-model="form.fiscalYearStart" :min="1" :max="12" controls-position="right" />
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
import { createAccountSet, listAccountSets, activateAccountSet, getRecommendation, createFromRecommendation } from '@/api/accountset'
import type { AccountSet } from '@/types'
import { required, maxLen } from '@/utils/validators'

const list = ref<AccountSet[]>([])
const showDialog = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const filterProjectId = ref(1)

const recommendProjectId = ref(1)
const recommendLoading = ref(false)
const createFromRecLoading = ref(false)
const recommendation = ref<any>(null)

const initForm = () => ({
  projectId: undefined as number | undefined,
  setName: '',
  accountingSystem: '',
  pricingMethod: '',
  qtyDecimals: 2,
  wgtDecimals: 3,
  prcDecimals: 2,
  amtDecimals: 2,
  useWeight: true,
  currency: 'CNY',
  fiscalYearStart: 1,
})

const form = ref(initForm())

const rules = {
  projectId: [required('项目ID不能为空')],
  setName: [required('帐套名称不能为空'), maxLen(60)],
  accountingSystem: [required('会计制度不能为空')],
  pricingMethod: [required('计价方式不能为空')],
}

async function loadData() {
  const res = await listAccountSets(filterProjectId.value)
  list.value = res.data.data
}

async function handleActivate(id: number) {
  await activateAccountSet(id)
  ElMessage.success('帐套已启用')
  await loadData()
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createAccountSet(form.value as unknown as Record<string, unknown>)
    ElMessage.success('帐套创建成功')
    showDialog.value = false
    form.value = initForm()
    await loadData()
  } finally {
    submitting.value = false
  }
}

async function handleGetRecommendation() {
  recommendLoading.value = true
  try {
    const res = await getRecommendation(recommendProjectId.value)
    recommendation.value = res.data.data
  } catch {
    recommendation.value = null
  } finally {
    recommendLoading.value = false
  }
}

async function handleCreateFromRecommendation() {
  createFromRecLoading.value = true
  try {
    await createFromRecommendation(recommendProjectId.value)
    ElMessage.success('帐套已从推荐配置创建')
    recommendation.value = null
    filterProjectId.value = recommendProjectId.value
    await loadData()
  } finally {
    createFromRecLoading.value = false
  }
}
</script>
