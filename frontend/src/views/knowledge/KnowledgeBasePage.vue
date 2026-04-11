<template>
  <div>
    <div class="page-header">
      <h2>知识库</h2>
    </div>

    <!-- Stats section -->
    <el-row :gutter="16" class="stats-row" v-if="stats">
      <el-col :span="6">
        <div class="stat-card stat-total">
          <div class="stat-value">{{ stats.totalEntries }}</div>
          <div class="stat-label">知识总数</div>
        </div>
      </el-col>
      <el-col :span="4" v-for="(count, cat) in stats.byCategory" :key="cat">
        <div class="stat-card">
          <div class="stat-value">{{ count }}</div>
          <div class="stat-label">{{ categoryLabels[cat as string] || cat }}</div>
        </div>
      </el-col>
    </el-row>

    <el-tabs v-model="activeTab" class="card">
      <!-- Tab 1: Knowledge Management -->
      <el-tab-pane label="知识库管理" name="manage">
        <div class="filter-bar">
          <el-select v-model="filters.category" placeholder="分类" clearable style="width: 140px">
            <el-option label="通用" value="GENERAL" />
            <el-option label="行业" value="INDUSTRY" />
            <el-option label="经验" value="EXPERIENCE" />
            <el-option label="项目私有" value="PROJECT_PRIVATE" />
          </el-select>
          <el-select v-model="filters.layer" placeholder="层级" clearable style="width: 140px">
            <el-option v-for="l in layerOptions" :key="l" :label="l" :value="l" />
          </el-select>
          <el-input v-model="filters.query" placeholder="搜索标题/关键词" clearable style="width: 220px" @keyup.enter="loadList" />
          <el-button type="primary" @click="loadList">查询</el-button>
          <el-button type="success" @click="openCreate">新增知识</el-button>
        </div>

        <el-table :data="entries" stripe>
          <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
          <el-table-column label="分类" width="110">
            <template #default="{ row }">
              <el-tag :type="categoryTagType(row.category)" size="small">
                {{ categoryLabels[row.category] || row.category }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="层级" width="110">
            <template #default="{ row }">
              <el-tag type="info" size="small" effect="plain">{{ row.layer }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="viewCount" label="浏览" width="80" align="center" />
          <el-table-column prop="helpfulCount" label="有帮助" width="80" align="center" />
          <el-table-column prop="source" label="来源" width="120" show-overflow-tooltip />
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.enabled ? 'success' : 'danger'" size="small">
                {{ row.enabled ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="handleHelpful(row)">
                有帮助👍
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- Tab 2: Knowledge Search -->
      <el-tab-pane label="知识搜索" name="search">
        <div class="search-bar">
          <el-input
            v-model="searchQuery"
            placeholder="输入关键词搜索知识库..."
            size="large"
            clearable
            style="width: 500px"
            @keyup.enter="handleSearch"
          />
          <el-select v-model="searchCategory" placeholder="分类筛选" clearable style="width: 140px" size="large">
            <el-option label="通用" value="GENERAL" />
            <el-option label="行业" value="INDUSTRY" />
            <el-option label="经验" value="EXPERIENCE" />
            <el-option label="项目私有" value="PROJECT_PRIVATE" />
          </el-select>
          <el-input-number v-model="searchProjectId" placeholder="项目ID" :min="1" controls-position="right" style="width: 140px" size="large" clearable />
          <el-button type="primary" size="large" @click="handleSearch">搜索</el-button>
        </div>

        <div v-if="searchResults.length === 0 && searchPerformed" class="empty-search">
          <el-empty description="未找到相关知识" />
        </div>

        <div class="search-results">
          <div
            v-for="item in searchResults"
            :key="item.id"
            class="result-card"
            @click="toggleExpand(item.id)"
          >
            <div class="result-header">
              <span class="result-title">{{ item.title }}</span>
              <div class="result-badges">
                <el-tag :type="categoryTagType(item.category)" size="small">
                  {{ categoryLabels[item.category] || item.category }}
                </el-tag>
                <el-tag type="info" size="small" effect="plain">{{ item.layer }}</el-tag>
              </div>
            </div>
            <div class="result-content">
              {{ expandedId === item.id ? item.content : (item.content?.substring(0, 200) + (item.content?.length > 200 ? '...' : '')) }}
            </div>
            <div class="result-footer">
              <div class="result-keywords" v-if="item.keywords">
                <el-tag v-for="kw in item.keywords.split(',')" :key="kw" size="small" type="info" effect="plain" class="kw-tag">
                  {{ kw.trim() }}
                </el-tag>
              </div>
              <div class="result-stats">
                <span>浏览 {{ item.viewCount }}</span>
                <span>有帮助 {{ item.helpfulCount }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- Create Dialog -->
    <el-dialog v-model="showDialog" title="新增知识" width="700px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-select v-model="form.category" placeholder="请选择" style="width: 100%">
                <el-option label="通用" value="GENERAL" />
                <el-option label="行业" value="INDUSTRY" />
                <el-option label="经验" value="EXPERIENCE" />
                <el-option label="项目私有" value="PROJECT_PRIVATE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="层级" prop="layer">
              <el-select v-model="form.layer" placeholder="请选择" style="width: 100%">
                <el-option v-for="l in layerOptions" :key="l" :label="l" :value="l" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="200" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" />
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="form.keywords" placeholder="多个关键词用逗号分隔" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="来源">
              <el-input v-model="form.source" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="访问级别">
              <el-select v-model="form.accessLevel" style="width: 100%">
                <el-option label="公开" value="PUBLIC" />
                <el-option label="仅项目内" value="PROJECT_ONLY" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="项目ID" v-if="form.category === 'PROJECT_PRIVATE'">
          <el-input-number v-model="form.projectId" :min="1" controls-position="right" />
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
import { ref, reactive, onMounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { createKnowledge, listKnowledge, searchKnowledge, markHelpful, getKnowledgeStats } from '@/api/knowledge'

const activeTab = ref('manage')
const entries = ref<any[]>([])
const stats = ref<any>(null)

const categoryLabels: Record<string, string> = {
  GENERAL: '通用',
  INDUSTRY: '行业',
  EXPERIENCE: '经验',
  PROJECT_PRIVATE: '项目私有',
}

const layerOptions = [
  '产品文档', '操作手册', 'FAQ', '术语表', '业务流程',
  '品类知识', '最佳实践', '项目案例', '问题方案', '经验总结', '客户QA',
]

function categoryTagType(category: string) {
  const map: Record<string, string> = {
    GENERAL: '',
    INDUSTRY: 'success',
    EXPERIENCE: 'warning',
    PROJECT_PRIVATE: 'danger',
  }
  return map[category] || 'info'
}

const filters = reactive({ category: '', layer: '', query: '' })

const showDialog = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const defaultForm = () => ({
  category: '',
  layer: '',
  title: '',
  content: '',
  keywords: '',
  source: '',
  projectId: undefined as number | undefined,
  accessLevel: 'PUBLIC',
})

const form = ref(defaultForm())

const rules: FormRules = {
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  layer: [{ required: true, message: '请选择层级', trigger: 'change' }],
  title: [{ required: true, message: '标题不能为空', trigger: 'blur' }],
  content: [{ required: true, message: '内容不能为空', trigger: 'blur' }],
}

// Search state
const searchQuery = ref('')
const searchCategory = ref('')
const searchProjectId = ref<number | undefined>(undefined)
const searchResults = ref<any[]>([])
const searchPerformed = ref(false)
const expandedId = ref<number | null>(null)

function toggleExpand(id: number) {
  expandedId.value = expandedId.value === id ? null : id
}

async function loadList() {
  const params: Record<string, any> = {}
  if (filters.category) params.category = filters.category
  if (filters.layer) params.layer = filters.layer
  if (filters.query) params.query = filters.query
  const res = await listKnowledge(params)
  entries.value = res.data.data
}

async function loadStats() {
  const res = await getKnowledgeStats()
  stats.value = res.data.data
}

function openCreate() {
  form.value = defaultForm()
  showDialog.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createKnowledge(form.value as unknown as Record<string, unknown>)
    ElMessage.success('创建成功')
    showDialog.value = false
    await Promise.all([loadList(), loadStats()])
  } finally {
    submitting.value = false
  }
}

async function handleHelpful(row: any) {
  await markHelpful(row.id)
  row.helpfulCount = (row.helpfulCount || 0) + 1
  ElMessage.success('已标记有帮助')
}

async function handleSearch() {
  if (!searchQuery.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  searchPerformed.value = true
  const res = await searchKnowledge(
    searchQuery.value,
    searchCategory.value || undefined,
    searchProjectId.value,
  )
  searchResults.value = res.data.data
}

onMounted(() => {
  loadList()
  loadStats()
})
</script>

<style scoped>
.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.stat-card.stat-total {
  background: linear-gradient(135deg, #4361ee, #3a0ca3);
  color: #fff;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
}

.stat-label {
  font-size: 13px;
  margin-top: 4px;
  opacity: 0.85;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
  align-items: center;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  align-items: center;
  flex-wrap: wrap;
}

.empty-search {
  padding: 40px 0;
}

.search-results {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.result-card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px 20px;
  cursor: pointer;
  transition: box-shadow 0.2s;
}

.result-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.result-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.result-badges {
  display: flex;
  gap: 6px;
}

.result-content {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 10px;
  white-space: pre-wrap;
}

.result-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.result-keywords {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.kw-tag {
  margin: 0;
}

.result-stats {
  display: flex;
  gap: 16px;
  color: #909399;
  font-size: 13px;
}
</style>
