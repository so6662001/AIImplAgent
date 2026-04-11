<template>
  <div>
    <div class="page-header">
      <h2>LLM模型配置</h2>
      <el-button type="primary" @click="openCreate">新增模型</el-button>
    </div>

    <div class="card">
      <el-table :data="providers" stripe>
        <el-table-column prop="providerName" label="名称" min-width="140" />
        <el-table-column prop="providerType" label="类型" width="100" />
        <el-table-column prop="apiEndpoint" label="API地址" min-width="200" show-overflow-tooltip />
        <el-table-column label="API Key" width="140">
          <template #default="{ row }">
            {{ row.apiKey ? '••••••••' + row.apiKey.slice(-4) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="modelName" label="模型名称" width="160" />
        <el-table-column prop="maxTokens" label="最大Token" width="100" />
        <el-table-column prop="temperature" label="温度" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'" size="small">
              {{ row.enabled ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确认删除该模型配置？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button link type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      v-model="showDialog"
      :title="isEdit ? '编辑模型配置' : '新增模型配置'"
      width="600px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="名称" prop="providerName">
          <el-input v-model="form.providerName" maxlength="50" />
        </el-form-item>
        <el-form-item label="类型" prop="providerType">
          <el-select v-model="form.providerType" placeholder="请选择">
            <el-option label="GLM (智谱)" value="GLM" />
            <el-option label="QWEN (千问)" value="QWEN" />
            <el-option label="CLAUDE" value="CLAUDE" />
          </el-select>
        </el-form-item>
        <el-form-item label="API地址" prop="apiEndpoint">
          <el-input v-model="form.apiEndpoint" placeholder="https://" />
        </el-form-item>
        <el-form-item label="API Key" prop="apiKey">
          <el-input v-model="form.apiKey" type="password" show-password />
        </el-form-item>
        <el-form-item label="模型名称" prop="modelName">
          <el-input v-model="form.modelName" placeholder="e.g. glm-4, qwen-max" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="最大Token" prop="maxTokens">
              <el-input-number v-model="form.maxTokens" :min="100" :max="128000" :step="100" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="温度" prop="temperature">
              <el-input-number v-model="form.temperature" :min="0" :max="2" :step="0.1" :precision="1" controls-position="right" style="width: 100%" />
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
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { createProvider, listProviders, updateProvider, deleteProvider } from '@/api/llmprovider'
import type { LlmProviderConfig } from '@/types'

const providers = ref<LlmProviderConfig[]>([])
const showDialog = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const isEdit = ref(false)
const editId = ref<number | null>(null)

const defaultForm = () => ({
  providerName: '',
  providerType: '',
  apiEndpoint: '',
  apiKey: '',
  modelName: '',
  maxTokens: 4096,
  temperature: 0.7,
  enabled: true,
})

const form = ref(defaultForm())

const rules: FormRules = {
  providerName: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
  providerType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  apiEndpoint: [
    { required: true, message: 'API地址不能为空', trigger: 'blur' },
    { pattern: /^https?:\/\/.+/, message: '请输入有效的URL', trigger: 'blur' },
  ],
  modelName: [{ required: true, message: '模型名称不能为空', trigger: 'blur' }],
}

function openCreate() {
  isEdit.value = false
  editId.value = null
  form.value = defaultForm()
  showDialog.value = true
}

function openEdit(row: LlmProviderConfig) {
  isEdit.value = true
  editId.value = row.id
  form.value = {
    providerName: row.providerName,
    providerType: row.providerType,
    apiEndpoint: row.apiEndpoint,
    apiKey: row.apiKey,
    modelName: row.modelName,
    maxTokens: row.maxTokens,
    temperature: row.temperature,
    enabled: row.enabled,
  }
  showDialog.value = true
}

async function loadData() {
  const res = await listProviders()
  providers.value = res.data.data
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value && editId.value !== null) {
      await updateProvider(editId.value, form.value as unknown as Record<string, unknown>)
      ElMessage.success('更新成功')
    } else {
      await createProvider(form.value as unknown as Record<string, unknown>)
      ElMessage.success('创建成功')
    }
    showDialog.value = false
    await loadData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id: number) {
  await deleteProvider(id)
  ElMessage.success('删除成功')
  await loadData()
}

onMounted(loadData)
</script>
