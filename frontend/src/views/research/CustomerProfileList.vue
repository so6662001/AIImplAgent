<template>
  <div>
    <div class="page-header">
      <h2>调研分析</h2>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- Tab 1: 客户画像 -->
      <el-tab-pane label="客户画像" name="profiles">
        <div style="margin-bottom: 16px; display: flex; align-items: center; gap: 12px;">
          <el-form inline style="margin: 0;">
            <el-form-item label="项目ID" style="margin-bottom: 0;">
              <el-input-number v-model="filterProjectId" :min="1" controls-position="right" />
            </el-form-item>
            <el-form-item style="margin-bottom: 0;">
              <el-button type="primary" @click="loadProfiles">查询</el-button>
            </el-form-item>
          </el-form>
          <el-button type="success" @click="showProfileDialog = true">新建调研档案</el-button>
        </div>

        <el-table :data="profileList" stripe border>
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="projectId" label="项目ID" width="80" />
          <el-table-column prop="companyName" label="公司名称" min-width="160" />
          <el-table-column prop="industryType" label="行业类型" width="120">
            <template #default="{ row }">
              {{ IndustryTypeLabels[row.industryType as IndustryType] || row.industryType }}
            </template>
          </el-table-column>
          <el-table-column label="企业规模" width="100">
            <template #default="{ row }">
              {{ getScaleLabel(row.totalStaff) }}
            </template>
          </el-table-column>
          <el-table-column label="模块数" width="80">
            <template #default="{ row }">
              {{ getModuleCount(row.targetModules) }}
            </template>
          </el-table-column>
          <el-table-column prop="totalStaff" label="员工总数" width="100" />
          <el-table-column prop="monthlyVolume" label="月销量" width="100" />
          <el-table-column prop="monthlyAmount" label="月销额" width="100" />
        </el-table>
      </el-tab-pane>

      <!-- Tab 2: 调研问卷 -->
      <el-tab-pane label="调研问卷" name="questionnaire">
        <div class="card" style="margin-bottom: 20px;">
          <el-form inline>
            <el-form-item label="行业类型">
              <el-select v-model="qForm.industryType" placeholder="请选择行业类型" style="width: 160px;">
                <el-option v-for="(label, key) in IndustryTypeLabels" :key="key" :label="label" :value="key" />
              </el-select>
            </el-form-item>
            <el-form-item label="企业规模">
              <el-select v-model="qForm.scale" placeholder="请选择规模" style="width: 120px;">
                <el-option label="大型" value="大型" />
                <el-option label="中型" value="中型" />
                <el-option label="小型" value="小型" />
              </el-select>
            </el-form-item>
            <el-form-item label="目标模块">
              <el-select v-model="qForm.modules" multiple collapse-tags placeholder="请选择模块" style="width: 300px;">
                <el-option v-for="m in standardModules" :key="m" :label="m" :value="m" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="qGenerating" @click="handleGenerateQuestionnaire">生成问卷</el-button>
            </el-form-item>
          </el-form>
        </div>

        <template v-if="questionnaire">
          <div style="margin-bottom: 12px; display: flex; justify-content: space-between; align-items: center;">
            <h3 style="margin: 0;">{{ questionnaire.title }}</h3>
            <el-button disabled>导出</el-button>
          </div>
          <el-collapse v-model="qExpandedSections">
            <el-collapse-item
              v-for="(section, sIdx) in questionnaire.sections"
              :key="sIdx"
              :title="section.sectionName"
              :name="sIdx"
            >
              <p v-if="section.sectionDescription" style="color: #909399; margin: 0 0 12px 0; font-size: 13px;">
                {{ section.sectionDescription }}
              </p>
              <div v-for="(q, qIdx) in section.questions" :key="qIdx" style="margin-bottom: 16px; padding: 10px; background: #fafafa; border-radius: 6px;">
                <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 6px;">
                  <span style="font-weight: 500;">{{ q.questionId }}. {{ q.questionText }}</span>
                  <el-tag size="small" :type="getQuestionTypeTag(q.questionType)">{{ q.questionType }}</el-tag>
                  <span v-if="q.required" style="color: #f56c6c; font-weight: bold;">*</span>
                </div>
                <div v-if="q.options && q.options.length" style="margin-left: 16px; color: #606266; font-size: 13px;">
                  选项: {{ q.options.join(' / ') }}
                </div>
                <div v-if="q.hint" style="margin-left: 16px; color: #909399; font-size: 12px; font-style: italic;">
                  提示: {{ q.hint }}
                </div>
              </div>
            </el-collapse-item>
          </el-collapse>
        </template>
      </el-tab-pane>

      <!-- Tab 3: 背景分析报告 -->
      <el-tab-pane label="背景分析报告" name="reports">
        <div class="card" style="margin-bottom: 20px;">
          <el-form inline>
            <el-form-item label="项目ID">
              <el-input-number v-model="reportProjectId" :min="1" controls-position="right" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleListReports">查看已有报告</el-button>
            </el-form-item>
          </el-form>
          <el-table v-if="reportList.length" :data="reportList" stripe border style="margin-top: 12px;" max-height="240">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="profileId" label="画像ID" width="80" />
            <el-table-column label="风险等级" width="120">
              <template #default="{ row }">
                <el-tag :type="getRiskTagType(row.overallRiskLevel)" size="small">{{ row.overallRiskLevel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80" />
            <el-table-column prop="createTime" label="生成时间" min-width="160" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button link type="primary" @click="viewReport(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="card" style="margin-bottom: 20px;">
          <el-form inline>
            <el-form-item label="选择客户画像">
              <el-select v-model="reportProfileId" placeholder="请选择画像" style="width: 280px;" filterable>
                <el-option
                  v-for="p in profileList"
                  :key="p.id"
                  :label="`${p.id} - ${p.companyName}`"
                  :value="p.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="success" :loading="reportGenerating" @click="handleGenerateReport">生成报告</el-button>
            </el-form-item>
          </el-form>
        </div>

        <template v-if="currentReport">
          <div style="margin-bottom: 12px; display: flex; justify-content: space-between; align-items: center;">
            <h3 style="margin: 0;">{{ currentReport.companyName }} - 背景分析报告</h3>
            <el-tag :type="getRiskTagType(currentReport.overallRiskLevel)" size="default">
              整体风险: {{ currentReport.overallRiskLevel }}
            </el-tag>
          </div>
          <div v-for="(section, sIdx) in currentReport.sections" :key="sIdx" class="card" style="margin-bottom: 16px;">
            <h4 style="margin: 0 0 8px 0; color: #303133;">{{ section.sectionTitle }}</h4>
            <p style="white-space: pre-wrap; color: #606266; line-height: 1.8; margin: 0 0 8px 0;">{{ section.content }}</p>
            <div v-if="section.highlights && section.highlights.length" style="display: flex; flex-wrap: wrap; gap: 6px;">
              <el-tag v-for="(h, hIdx) in section.highlights" :key="hIdx" size="small" type="info">{{ h }}</el-tag>
            </div>
          </div>
        </template>
      </el-tab-pane>
    </el-tabs>

    <!-- Create Profile Dialog -->
    <el-dialog v-model="showProfileDialog" title="新建调研档案" width="860px" destroy-on-close top="4vh">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="130px" style="max-height: 65vh; overflow-y: auto; padding-right: 12px;">
        <el-collapse v-model="formExpandedSections">
          <el-collapse-item title="基本信息" name="basic">
            <el-form-item label="项目ID" prop="projectId">
              <el-input-number v-model="form.projectId" :min="1" controls-position="right" />
            </el-form-item>
            <el-form-item label="公司名称" prop="companyName">
              <el-input v-model="form.companyName" maxlength="100" />
            </el-form-item>
            <el-form-item label="法定代表人" prop="legalPerson">
              <el-input v-model="form.legalPerson" maxlength="30" />
            </el-form-item>
            <el-form-item label="注册资本" prop="registeredCapital">
              <el-input v-model="form.registeredCapital" maxlength="30" placeholder="例如: 5000万元" />
            </el-form-item>
            <el-form-item label="成立日期" prop="establishmentDate">
              <el-date-picker v-model="form.establishmentDate" type="date" value-format="YYYY-MM-DD" />
            </el-form-item>
            <el-form-item label="办公地址" prop="address">
              <el-input v-model="form.address" maxlength="200" />
            </el-form-item>
          </el-collapse-item>

          <el-collapse-item title="经营模式" name="business">
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
            <el-form-item label="销售模式" prop="salesMode">
              <el-select v-model="form.salesMode" placeholder="请选择">
                <el-option label="现货批发" value="现货批发" />
                <el-option label="期货" value="期货" />
                <el-option label="零售" value="零售" />
                <el-option label="混合" value="混合" />
              </el-select>
            </el-form-item>
          </el-collapse-item>

          <el-collapse-item title="生产能力" name="production">
            <el-form-item label="产线数量" prop="totalProductionLines">
              <el-input-number v-model="form.totalProductionLines" :min="0" controls-position="right" />
            </el-form-item>
            <el-form-item label="排班模式" prop="productionShifts">
              <el-select v-model="form.productionShifts" placeholder="请选择">
                <el-option label="单班" value="单班" />
                <el-option label="两班" value="两班" />
                <el-option label="三班" value="三班" />
              </el-select>
            </el-form-item>
            <el-form-item label="MES现状" prop="mesCurrentStatus">
              <el-select v-model="form.mesCurrentStatus" placeholder="请选择">
                <el-option label="无" value="无" />
                <el-option label="部分上线" value="部分上线" />
                <el-option label="已有完整MES" value="已有完整MES" />
              </el-select>
            </el-form-item>
            <el-form-item label="质量标准" prop="qualityStandards">
              <el-input v-model="form.qualityStandards" maxlength="200" />
            </el-form-item>
          </el-collapse-item>

          <el-collapse-item title="库存仓储" name="warehouse">
            <el-form-item label="仓库数量" prop="totalWarehouseCount">
              <el-input-number v-model="form.totalWarehouseCount" :min="0" controls-position="right" />
            </el-form-item>
            <el-form-item label="仓库面积(㎡)" prop="totalWarehouseAreaSqm">
              <el-input-number v-model="form.totalWarehouseAreaSqm" :min="0" :precision="2" controls-position="right" />
            </el-form-item>
            <el-form-item label="行车数量" prop="totalCraneCount">
              <el-input-number v-model="form.totalCraneCount" :min="0" controls-position="right" />
            </el-form-item>
            <el-form-item label="库存周转率" prop="inventoryTurnoverRate">
              <el-input-number v-model="form.inventoryTurnoverRate" :min="0" :precision="2" controls-position="right" />
            </el-form-item>
            <el-form-item label="库存管理方式" prop="inventoryManagementMethod">
              <el-select v-model="form.inventoryManagementMethod" placeholder="请选择">
                <el-option label="手工台账" value="手工台账" />
                <el-option label="Excel" value="Excel" />
                <el-option label="WMS系统" value="WMS系统" />
                <el-option label="ERP库存模块" value="ERP库存模块" />
              </el-select>
            </el-form-item>
          </el-collapse-item>

          <el-collapse-item title="销售状况" name="sales">
            <el-form-item label="月销量(吨)" prop="monthlyVolume">
              <el-input-number v-model="form.monthlyVolume" :min="0" :precision="2" controls-position="right" />
            </el-form-item>
            <el-form-item label="月销额(万)" prop="monthlyAmount">
              <el-input-number v-model="form.monthlyAmount" :min="0" :precision="2" controls-position="right" />
            </el-form-item>
            <el-form-item label="定价模式" prop="pricingModel">
              <el-select v-model="form.pricingModel" placeholder="请选择">
                <el-option label="一口价" value="一口价" />
                <el-option label="基价+加工费" value="基价+加工费" />
                <el-option label="期货联动" value="期货联动" />
                <el-option label="协议价" value="协议价" />
                <el-option label="混合" value="混合" />
              </el-select>
            </el-form-item>
            <el-form-item label="结算方式" prop="settlementMethods">
              <el-input v-model="form.settlementMethods" maxlength="100" placeholder="现款现货,账期,承兑汇票" />
            </el-form-item>
            <el-form-item label="信用政策" prop="creditPolicy">
              <el-input v-model="form.creditPolicy" type="textarea" :rows="2" maxlength="200" />
            </el-form-item>
          </el-collapse-item>

          <el-collapse-item title="客户群体" name="customers">
            <el-form-item label="客户总数" prop="totalCustomerCount">
              <el-input-number v-model="form.totalCustomerCount" :min="0" controls-position="right" />
            </el-form-item>
            <el-form-item label="客户类型" prop="customerTypes">
              <el-input v-model="form.customerTypes" maxlength="200" placeholder="终端用户,贸易商,加工厂" />
            </el-form-item>
            <el-form-item label="主要大客户" prop="topCustomers">
              <el-input v-model="form.topCustomers" type="textarea" :rows="2" maxlength="500" />
            </el-form-item>
          </el-collapse-item>

          <el-collapse-item title="组织架构" name="organization">
            <el-form-item label="员工总数" prop="totalStaff">
              <el-input-number v-model="form.totalStaff" :min="0" :max="100000" controls-position="right" />
            </el-form-item>
            <el-form-item label="部门设置" prop="departments">
              <el-input v-model="form.departments" type="textarea" :rows="2" maxlength="500" placeholder="销售部,采购部,仓储部,财务部" />
            </el-form-item>
            <el-form-item label="关键岗位" prop="keyPositions">
              <el-input v-model="form.keyPositions" type="textarea" :rows="2" maxlength="500" />
            </el-form-item>
            <el-form-item label="决策链" prop="decisionChain">
              <el-input v-model="form.decisionChain" maxlength="200" placeholder="销售员→销售经理→总经理" />
            </el-form-item>
          </el-collapse-item>

          <el-collapse-item title="现有系统" name="systems">
            <el-form-item label="现有系统" prop="existingSystems">
              <el-input v-model="form.existingSystems" type="textarea" :rows="4" maxlength="1000" placeholder='JSON格式描述现有系统，例如: [{"name":"用友U8","modules":["财务","进销存"]}]' />
            </el-form-item>
          </el-collapse-item>

          <el-collapse-item title="项目范围" name="scope">
            <el-form-item label="目标模块" prop="targetModules">
              <el-input v-model="form.targetModules" maxlength="500" placeholder="进销存,财务,仓储,生产" />
            </el-form-item>
            <el-form-item label="模块优先级" prop="modulePriorities">
              <el-input v-model="form.modulePriorities" type="textarea" :rows="2" maxlength="500" />
            </el-form-item>
          </el-collapse-item>

          <el-collapse-item title="项目目标" name="goals">
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
          </el-collapse-item>
        </el-collapse>
      </el-form>
      <template #footer>
        <el-button @click="showProfileDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import {
  createCustomerProfile,
  listCustomerProfiles,
  generateQuestionnaire,
  generateReport,
  listReports,
} from '@/api/research'
import { IndustryTypeLabels } from '@/types'
import type { CustomerProfile, IndustryType } from '@/types'
import { required, maxLen } from '@/utils/validators'

const activeTab = ref('profiles')

// ========== Tab 1: Profiles ==========
const profileList = ref<CustomerProfile[]>([])
const showProfileDialog = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const filterProjectId = ref(1)
const formExpandedSections = ref(['basic', 'business'])

const standardModules = ['进销存', '财务', '仓储', '生产', '质量', '物流', 'OA', 'CRM', '电商', 'BI报表']

function getScaleLabel(staff: number | undefined | null): string {
  if (staff == null) return '-'
  if (staff >= 500) return '大型'
  if (staff >= 100) return '中型'
  if (staff > 0) return '小型'
  return '-'
}

function getModuleCount(targetModules: string | undefined | null): number {
  if (!targetModules) return 0
  return targetModules.split(/[,，]/).filter((s: string) => s.trim()).length
}

const initForm = () => ({
  projectId: undefined as number | undefined,
  companyName: '',
  legalPerson: '',
  registeredCapital: '',
  establishmentDate: '',
  address: '',
  industryType: '',
  businessModel: '',
  tradeMode: '',
  tradeScope: '',
  mainBusiness: '',
  salesMode: '',
  totalProductionLines: 0,
  productionShifts: '',
  mesCurrentStatus: '',
  qualityStandards: '',
  totalWarehouseCount: 0,
  totalWarehouseAreaSqm: 0,
  totalCraneCount: 0,
  inventoryTurnoverRate: 0,
  inventoryManagementMethod: '',
  monthlyVolume: 0,
  monthlyAmount: 0,
  pricingModel: '',
  settlementMethods: '',
  creditPolicy: '',
  totalCustomerCount: 0,
  customerTypes: '',
  topCustomers: '',
  totalStaff: 0,
  departments: '',
  keyPositions: '',
  decisionChain: '',
  existingSystems: '',
  targetModules: '',
  modulePriorities: '',
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

async function loadProfiles() {
  const res = await listCustomerProfiles(filterProjectId.value)
  profileList.value = res.data.data
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createCustomerProfile(form.value as unknown as Record<string, unknown>)
    ElMessage.success('调研档案创建成功')
    showProfileDialog.value = false
    form.value = initForm()
    await loadProfiles()
  } finally {
    submitting.value = false
  }
}

// ========== Tab 2: Questionnaire ==========
const qForm = ref({
  industryType: '',
  scale: '',
  modules: [] as string[],
})
const qGenerating = ref(false)
const questionnaire = ref<any>(null)
const qExpandedSections = ref<number[]>([])

function getQuestionTypeTag(type: string): '' | 'success' | 'warning' | 'danger' | 'info' {
  const map: Record<string, '' | 'success' | 'warning' | 'danger' | 'info'> = {
    TEXT: '',
    SELECT: 'success',
    MULTI_SELECT: 'warning',
    NUMBER: 'info',
  }
  return map[type] ?? 'info'
}

async function handleGenerateQuestionnaire() {
  if (!qForm.value.industryType) {
    ElMessage.warning('请选择行业类型')
    return
  }
  if (!qForm.value.scale) {
    ElMessage.warning('请选择企业规模')
    return
  }
  qGenerating.value = true
  try {
    const res = await generateQuestionnaire({
      industryType: qForm.value.industryType,
      scale: qForm.value.scale,
      modules: qForm.value.modules,
    })
    questionnaire.value = res.data.data
    qExpandedSections.value = (questionnaire.value?.sections || []).map((_: any, i: number) => i)
    ElMessage.success('问卷生成成功')
  } finally {
    qGenerating.value = false
  }
}

// ========== Tab 3: Reports ==========
const reportProjectId = ref(1)
const reportList = ref<any[]>([])
const reportProfileId = ref<number | undefined>(undefined)
const reportGenerating = ref(false)
const currentReport = ref<any>(null)

function getRiskTagType(level: string): '' | 'success' | 'warning' | 'danger' {
  const map: Record<string, '' | 'success' | 'warning' | 'danger'> = {
    LOW: 'success',
    MEDIUM: 'warning',
    HIGH: 'danger',
  }
  return map[level] ?? ''
}

async function handleListReports() {
  const res = await listReports(reportProjectId.value)
  reportList.value = res.data.data
}

async function handleGenerateReport() {
  if (!reportProfileId.value) {
    ElMessage.warning('请选择客户画像')
    return
  }
  reportGenerating.value = true
  try {
    const res = await generateReport(reportProfileId.value)
    currentReport.value = res.data.data
    ElMessage.success('报告生成成功')
  } finally {
    reportGenerating.value = false
  }
}

function viewReport(row: any) {
  try {
    currentReport.value = JSON.parse(row.reportContent)
  } catch {
    ElMessage.error('报告内容解析失败')
  }
}
</script>
