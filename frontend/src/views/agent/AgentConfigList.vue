<template>
  <div>
    <div class="page-header">
      <h2>智能体配置</h2>
      <el-button type="primary" @click="openCreate">新增智能体</el-button>
    </div>

    <div class="card">
      <el-table :data="agents" stripe>
        <el-table-column prop="agentCode" label="编码" width="160" />
        <el-table-column prop="agentName" label="名称" width="160" />
        <el-table-column label="LLM模型" width="140">
          <template #default="{ row }">
            {{ providerMap[row.llmProviderId] || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'" size="small">
              {{ row.enabled ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="RAG" width="80">
          <template #default="{ row }">
            <el-tag :type="row.ragEnabled ? 'success' : 'info'" size="small">
              {{ row.ragEnabled ? '开启' : '关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      v-model="showDialog"
      :title="isEdit ? '编辑智能体' : '新增智能体'"
      width="700px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="130px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="智能体编码" prop="agentCode">
              <el-input v-model="form.agentCode" maxlength="50" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="智能体名称" prop="agentName">
              <el-input v-model="form.agentName" maxlength="100" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" maxlength="500" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="LLM模型" prop="llmProviderId">
              <el-select v-model="form.llmProviderId" placeholder="请选择" style="width: 100%">
                <el-option
                  v-for="p in providers"
                  :key="p.id"
                  :label="p.providerName"
                  :value="p.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备用LLM">
              <el-select v-model="form.fallbackLlmProviderId" placeholder="可选" clearable style="width: 100%">
                <el-option
                  v-for="p in providers"
                  :key="p.id"
                  :label="p.providerName"
                  :value="p.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="提示词模板" prop="promptTemplate">
          <el-input
            v-model="form.promptTemplate"
            type="textarea"
            :rows="8"
            placeholder="请输入提示词模板"
          />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="RAG">
              <el-switch v-model="form.ragEnabled" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="RAG集合名">
              <el-input v-model="form.ragCollectionName" maxlength="100" :disabled="!form.ragEnabled" />
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
import { ref, onMounted, computed } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { createAgentConfig, listAgentConfigs, updateAgentConfig } from '@/api/agentconfig'
import { listProviders } from '@/api/llmprovider'
import type { AgentConfig, LlmProviderConfig } from '@/types'

const agents = ref<AgentConfig[]>([])
const providers = ref<LlmProviderConfig[]>([])
const showDialog = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const isEdit = ref(false)
const editId = ref<number | null>(null)

const providerMap = computed(() => {
  const map: Record<number, string> = {}
  providers.value.forEach((p) => { map[p.id] = p.providerName })
  return map
})

const defaultForm = () => ({
  agentCode: '',
  agentName: '',
  description: '',
  llmProviderId: undefined as number | undefined,
  fallbackLlmProviderId: null as number | null,
  promptTemplate: '',
  ragEnabled: false,
  ragCollectionName: '',
  enabled: true,
})

const form = ref(defaultForm())

const rules: FormRules = {
  agentCode: [{ required: true, message: '编码不能为空', trigger: 'blur' }],
  agentName: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
  llmProviderId: [{ required: true, message: '请选择LLM模型', trigger: 'change' }],
  promptTemplate: [{ required: true, message: '提示词模板不能为空', trigger: 'blur' }],
}

function openCreate() {
  isEdit.value = false
  editId.value = null
  form.value = defaultForm()
  showDialog.value = true
}

function openEdit(row: AgentConfig) {
  isEdit.value = true
  editId.value = row.id
  form.value = {
    agentCode: row.agentCode,
    agentName: row.agentName,
    description: row.description,
    llmProviderId: row.llmProviderId,
    fallbackLlmProviderId: row.fallbackLlmProviderId,
    promptTemplate: row.promptTemplate,
    ragEnabled: row.ragEnabled,
    ragCollectionName: row.ragCollectionName,
    enabled: row.enabled,
  }
  showDialog.value = true
}

async function loadData() {
  const [agentRes, providerRes] = await Promise.all([listAgentConfigs(), listProviders()])
  agents.value = agentRes.data.data
  providers.value = providerRes.data.data
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value && editId.value !== null) {
      await updateAgentConfig(editId.value, form.value as unknown as Record<string, unknown>)
      ElMessage.success('更新成功')
    } else {
      await createAgentConfig(form.value as unknown as Record<string, unknown>)
      ElMessage.success('创建成功')
    }
    showDialog.value = false
    await loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>
