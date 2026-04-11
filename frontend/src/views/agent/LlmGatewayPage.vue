<template>
  <div>
    <div class="page-header">
      <h2>LLM网关测试台</h2>
    </div>

    <el-row :gutter="20">
      <el-col :span="12">
        <div class="card">
          <h3 style="margin-bottom: 16px">快速测试</h3>
          <el-form label-width="100px">
            <el-form-item label="选择智能体">
              <el-select v-model="selectedAgent" placeholder="请选择智能体" style="width: 100%" filterable>
                <el-option
                  v-for="a in agents"
                  :key="a.agentCode"
                  :label="`${a.agentName} (${a.agentCode})`"
                  :value="a.agentCode"
                />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="testLoading" :disabled="!selectedAgent" @click="handleTest">
                快速测试
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="card" style="margin-top: 20px">
          <h3 style="margin-bottom: 16px">高级调用</h3>
          <el-form label-width="100px">
            <el-form-item label="智能体">
              <el-select v-model="advForm.agentCode" placeholder="请选择智能体" style="width: 100%" filterable>
                <el-option
                  v-for="a in agents"
                  :key="a.agentCode"
                  :label="`${a.agentName} (${a.agentCode})`"
                  :value="a.agentCode"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="Prompt">
              <el-input
                v-model="advForm.prompt"
                type="textarea"
                :rows="6"
                placeholder="输入提示词内容…"
              />
            </el-form-item>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="Temperature">
                  <el-input-number v-model="advForm.temperature" :min="0" :max="2" :step="0.1" :precision="1" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="MaxTokens">
                  <el-input-number v-model="advForm.maxTokens" :min="1" :max="8192" :step="256" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item>
              <el-button type="success" :loading="sendLoading" :disabled="!advForm.agentCode || !advForm.prompt" @click="handleSend">
                发送
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-col>

      <el-col :span="12">
        <div class="card" style="min-height: 400px">
          <h3 style="margin-bottom: 16px">响应结果</h3>
          <template v-if="response">
            <el-descriptions :column="2" border size="small" style="margin-bottom: 16px">
              <el-descriptions-item label="供应商">{{ response.providerName }}</el-descriptions-item>
              <el-descriptions-item label="模型">{{ response.modelName }}</el-descriptions-item>
              <el-descriptions-item label="Token用量">{{ response.tokensUsed }}</el-descriptions-item>
              <el-descriptions-item label="耗时">{{ response.executionTimeMs }}ms</el-descriptions-item>
              <el-descriptions-item label="Fallback">
                <el-tag :type="response.fallbackUsed ? 'warning' : 'success'" size="small">
                  {{ response.fallbackUsed ? '已启用' : '未启用' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="决策日志">
                <router-link v-if="response.decisionLogId" :to="'/agent-decisions'" class="link-text">
                  #{{ response.decisionLogId }}
                </router-link>
                <span v-else>-</span>
              </el-descriptions-item>
            </el-descriptions>
            <div class="response-box">
              <pre class="response-pre">{{ response.response }}</pre>
            </div>
          </template>
          <el-empty v-else description="暂无响应数据" />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { testLlm, completeLlm } from '@/api/llmgateway'
import { listAgentConfigs } from '@/api/agentconfig'
import type { AgentConfig } from '@/types'

const agents = ref<AgentConfig[]>([])
const selectedAgent = ref('')
const testLoading = ref(false)
const sendLoading = ref(false)
const response = ref<any>(null)

const advForm = ref({
  agentCode: '',
  prompt: '',
  temperature: 0.7,
  maxTokens: 2048,
})

async function loadAgents() {
  try {
    const res = await listAgentConfigs()
    agents.value = res.data.data
  } catch {
    ElMessage.error('加载智能体列表失败')
  }
}

async function handleTest() {
  testLoading.value = true
  try {
    const res = await testLlm(selectedAgent.value)
    response.value = res.data.data
    ElMessage.success('测试完成')
  } catch {
    ElMessage.error('测试失败')
  } finally {
    testLoading.value = false
  }
}

async function handleSend() {
  sendLoading.value = true
  try {
    const res = await completeLlm({
      agentCode: advForm.value.agentCode,
      prompt: advForm.value.prompt,
      temperature: advForm.value.temperature,
      maxTokens: advForm.value.maxTokens,
    })
    response.value = res.data.data
    ElMessage.success('发送完成')
  } catch {
    ElMessage.error('发送失败')
  } finally {
    sendLoading.value = false
  }
}

onMounted(loadAgents)
</script>

<style scoped>
.response-box {
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  max-height: 400px;
  overflow-y: auto;
}
.response-pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 13px;
  line-height: 1.6;
  color: #303133;
}
.link-text {
  color: var(--el-color-primary);
  text-decoration: none;
}
.link-text:hover {
  text-decoration: underline;
}
</style>
