<template>
  <div>
    <div class="page-header">
      <h2>品类档案</h2>
      <el-button type="primary" @click="showDialog = true">新增品类</el-button>
    </div>

    <div class="card">
      <el-table :data="categories" stripe>
        <el-table-column prop="categoryCode" label="品类编码" width="120" />
        <el-table-column prop="categoryName" label="品类名称" min-width="160" />
        <el-table-column prop="parentId" label="上级ID" width="80" />
        <el-table-column prop="level" label="层级" width="60" />
        <el-table-column prop="sortOrder" label="排序" width="60" />
        <el-table-column prop="categoryGroup" label="品类组" width="140" />
        <el-table-column prop="defaultUnit" label="默认单位" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'" size="small">
              {{ row.enabled ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="showDialog" title="新增品类" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="品类编码" prop="categoryCode">
              <el-input v-model="form.categoryCode" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品类名称" prop="categoryName">
              <el-input v-model="form.categoryName" maxlength="60" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="上级品类ID">
              <el-input-number v-model="form.parentId" :min="0" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="层级">
              <el-input-number v-model="form.level" :min="1" controls-position="right" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="form.sortOrder" :min="0" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品类组">
              <el-select v-model="form.categoryGroup" placeholder="请选择">
                <el-option label="板材 PLATE" value="PLATE" />
                <el-option label="方管 SQUARE_PIPE" value="SQUARE_PIPE" />
                <el-option label="圆管 ROUND_PIPE" value="ROUND_PIPE" />
                <el-option label="H型钢 SECTION_H" value="SECTION_H" />
                <el-option label="槽钢 SECTION_CHANNEL" value="SECTION_CHANNEL" />
                <el-option label="角钢 SECTION_ANGLE" value="SECTION_ANGLE" />
                <el-option label="线材/棒材 WIRE_BAR" value="WIRE_BAR" />
                <el-option label="非钢 NON_STEEL" value="NON_STEEL" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="默认单位">
          <el-input v-model="form.defaultUnit" maxlength="10" />
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
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import { createCategory, listCategories } from '@/api/category'
import type { ProductCategory } from '@/types'
import { required, maxLen } from '@/utils/validators'

const categories = ref<ProductCategory[]>([])
const showDialog = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = ref({
  categoryCode: '',
  categoryName: '',
  parentId: undefined as number | undefined,
  level: 1,
  sortOrder: 0,
  categoryGroup: '',
  defaultUnit: '',
})

const rules = {
  categoryCode: [required('品类编码不能为空'), maxLen(20)],
  categoryName: [required('品类名称不能为空'), maxLen(60)],
}

async function loadData() {
  const res = await listCategories()
  categories.value = res.data.data
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createCategory(form.value as unknown as Record<string, unknown>)
    ElMessage.success('品类创建成功')
    showDialog.value = false
    await loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>
