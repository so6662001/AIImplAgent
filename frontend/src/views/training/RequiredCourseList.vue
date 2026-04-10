<template>
  <div>
    <div class="page-header">
      <h2>KA必学课程配置</h2>
      <el-button type="primary" @click="showAddDialog = true">新增课程</el-button>
    </div>

    <div class="card" style="margin-bottom: 20px;">
      <el-alert type="info" :closable="false" show-icon>
        <template #title>
          按行业类型配置KA用户必须学习的课程模块。标记为 <strong>★必学</strong> 的课程，KA用户必须考核≥70分且综合≥75分，否则<strong>阻止上线</strong>。
        </template>
      </el-alert>
    </div>

    <div class="card">
      <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 16px;">
        <el-select v-model="filterIndustryType" placeholder="选择行业类型" clearable style="width: 200px">
          <el-option v-for="(label, key) in IndustryTypeLabels" :key="key" :label="label" :value="key" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
        <div style="flex:1" />
        <el-button v-if="selectedIds.length > 0" type="warning" @click="handleBatchSet(true)">
          批量设为★必学 ({{ selectedIds.length }})
        </el-button>
        <el-button v-if="selectedIds.length > 0" @click="handleBatchSet(false)">
          批量设为○选学 ({{ selectedIds.length }})
        </el-button>
      </div>

      <el-table :data="list" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="45" />
        <el-table-column label="行业类型" width="130">
          <template #default="{ row }">
            {{ IndustryTypeLabels[row.industryType as keyof typeof IndustryTypeLabels] || row.industryType }}
          </template>
        </el-table-column>
        <el-table-column prop="courseModule" label="课程模块" min-width="200" />
        <el-table-column label="KA必学" width="100">
          <template #default="{ row }">
            <el-switch
              :model-value="row.kaRequired"
              active-text="★"
              inactive-text="○"
              @change="(val: boolean) => handleToggle(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-popconfirm title="确定删除该课程配置？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增课程弹窗 -->
    <el-dialog v-model="showAddDialog" title="新增课程配置" width="520px" destroy-on-close>
      <el-form ref="addFormRef" :model="addForm" :rules="addRules" label-width="100px">
        <el-form-item label="行业类型" prop="industryType">
          <el-select v-model="addForm.industryType" placeholder="请选择">
            <el-option v-for="(label, key) in IndustryTypeLabels" :key="key" :label="label" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程模块" prop="courseModule">
          <el-select v-model="addForm.courseModule" placeholder="选择或输入" filterable allow-create>
            <el-option v-for="m in moduleOptions" :key="m" :label="m" :value="m" />
          </el-select>
        </el-form-item>
        <el-form-item label="KA必学">
          <el-switch v-model="addForm.kaRequired" active-text="★必学" inactive-text="○选学" />
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="addForm.sortOrder" :min="0" :max="999" controls-position="right" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleAdd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import {
  listRequiredCourses,
  createRequiredCourse,
  updateRequiredCourse,
  deleteRequiredCourse,
  batchToggle,
} from '@/api/requiredcourse'
import type { RequiredCourse } from '@/types'
import { IndustryTypeLabels } from '@/types'
import { required } from '@/utils/validators'

const list = ref<RequiredCourse[]>([])
const filterIndustryType = ref('')
const showAddDialog = ref(false)
const submitting = ref(false)
const addFormRef = ref<FormInstance>()
const selectedIds = ref<RequiredCourse[]>([])

const moduleOptions = [
  '系统基础操作', '采购管理', '销售管理', '库存管理',
  '仓储管理', '财务应收', '财务应付', '财务凭证',
  '磅房计量', '加工管理', 'MES-生产工单', 'MES-排产调度',
  'MES-质量管理', '智能财务', '智能采购', '报表与看板', '系统管理',
]

const addForm = ref({
  industryType: '',
  courseModule: '',
  kaRequired: true,
  sortOrder: 0,
})

const addRules = {
  industryType: [required('行业类型不能为空')],
  courseModule: [required('课程模块不能为空')],
}

function handleSelectionChange(rows: RequiredCourse[]) {
  selectedIds.value = rows
}

async function loadData() {
  const res = await listRequiredCourses(filterIndustryType.value || undefined)
  list.value = res.data.data
}

async function handleToggle(row: RequiredCourse, val: boolean) {
  await updateRequiredCourse(row.id, { kaRequired: val })
  row.kaRequired = val
  ElMessage.success(val ? '已设为★必学' : '已设为○选学')
}

async function handleDelete(id: number) {
  await deleteRequiredCourse(id)
  ElMessage.success('已删除')
  await loadData()
}

async function handleBatchSet(kaRequired: boolean) {
  const grouped = new Map<string, string[]>()
  for (const row of selectedIds.value) {
    const modules = grouped.get(row.industryType) || []
    modules.push(row.courseModule)
    grouped.set(row.industryType, modules)
  }

  for (const [industryType, courseModules] of grouped) {
    await batchToggle({ industryType, courseModules, kaRequired })
  }
  ElMessage.success(`已批量设为${kaRequired ? '★必学' : '○选学'}`)
  await loadData()
}

async function handleAdd() {
  const valid = await addFormRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createRequiredCourse(addForm.value as unknown as Record<string, unknown>)
    ElMessage.success('课程配置新增成功')
    showAddDialog.value = false
    addForm.value = { industryType: '', courseModule: '', kaRequired: true, sortOrder: 0 }
    await loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>
