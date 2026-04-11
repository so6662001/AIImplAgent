<template>
  <div>
    <div class="page-header">
      <h2>文档中心</h2>
    </div>

    <div class="card">
      <el-form :inline="true">
        <el-form-item label="项目ID">
          <el-input-number v-model="projectId" :min="1" placeholder="输入项目ID" style="width: 180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loadingTypes" :disabled="!projectId" @click="loadTypes">
            查看文档状态
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div v-if="docTypes.length" class="card" style="margin-top: 20px">
      <el-table :data="docTypes" stripe>
        <el-table-column prop="typeCode" label="类型编码" width="200" />
        <el-table-column prop="typeName" label="类型名称" width="200" />
        <el-table-column prop="description" label="描述" min-width="240" show-overflow-tooltip />
        <el-table-column label="已生成" width="90" align="center">
          <template #default="{ row }">
            <span :style="{ color: row.generated ? '#67c23a' : '#f56c6c', fontSize: '16px' }">
              {{ row.generated ? '✅' : '❌' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="上次生成" width="180">
          <template #default="{ row }">
            {{ row.lastGeneratedAt || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="recordCount" label="记录数" width="90" align="center" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              :loading="generatingType === row.typeCode"
              @click="handleGenerate(row.typeCode)"
            >
              生成
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div v-if="generatedDoc" class="card" style="margin-top: 20px">
      <h3 style="margin-bottom: 16px">
        生成结果
        <el-tag :type="generatedDoc.status === 'SUCCESS' ? 'success' : 'warning'" size="small" style="margin-left: 12px">
          {{ generatedDoc.status }}
        </el-tag>
      </h3>
      <el-descriptions :column="2" border size="small" style="margin-bottom: 16px">
        <el-descriptions-item label="文档名称">{{ generatedDoc.documentName }}</el-descriptions-item>
        <el-descriptions-item label="文档类型">{{ generatedDoc.documentType }}</el-descriptions-item>
        <el-descriptions-item label="项目ID">{{ generatedDoc.projectId }}</el-descriptions-item>
        <el-descriptions-item label="生成时间">{{ generatedDoc.generatedAt }}</el-descriptions-item>
      </el-descriptions>
      <div class="response-box">
        <pre class="response-pre">{{ generatedDoc.content }}</pre>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listDocumentTypes, generateDocument } from '@/api/docgen'

const projectId = ref<number>()
const docTypes = ref<any[]>([])
const loadingTypes = ref(false)
const generatingType = ref('')
const generatedDoc = ref<any>(null)

async function loadTypes() {
  if (!projectId.value) return
  loadingTypes.value = true
  try {
    const res = await listDocumentTypes(projectId.value)
    docTypes.value = res.data.data
  } catch {
    ElMessage.error('加载文档类型失败')
  } finally {
    loadingTypes.value = false
  }
}

async function handleGenerate(typeCode: string) {
  if (!projectId.value) return
  generatingType.value = typeCode
  try {
    const res = await generateDocument({
      documentType: typeCode,
      projectId: projectId.value,
    })
    generatedDoc.value = res.data.data
    ElMessage.success('文档生成成功')
    await loadTypes()
  } catch {
    ElMessage.error('文档生成失败')
  } finally {
    generatingType.value = ''
  }
}
</script>

<style scoped>
.response-box {
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  max-height: 500px;
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
</style>
