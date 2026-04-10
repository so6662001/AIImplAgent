<template>
  <div>
    <div class="page-header">
      <h2>KA必学课程</h2>
    </div>

    <div class="card">
      <div style="margin-bottom: 16px">
        <el-select v-model="filterIndustryType" placeholder="行业类型" clearable style="width: 200px">
          <el-option label="钢贸商" value="STEEL_TRADER" />
          <el-option label="钢厂" value="STEEL_MILL" />
          <el-option label="加工中心" value="PROCESSING_CENTER" />
          <el-option label="综合服务商" value="INTEGRATED_SERVICE" />
        </el-select>
        <el-button type="primary" @click="loadData" style="margin-left: 8px">查询</el-button>
      </div>
      <el-table :data="list" stripe>
        <el-table-column prop="industryType" label="行业类型" width="130" />
        <el-table-column prop="courseModule" label="课程模块" min-width="200" />
        <el-table-column label="KA必学" width="100">
          <template #default="{ row }">
            <span :style="{ color: row.kaRequired ? '#e6a23c' : '#c0c4cc', fontSize: '18px' }">
              {{ row.kaRequired ? '★' : '○' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { listRequiredCourses } from '@/api/requiredcourse'
import type { RequiredCourse } from '@/types'

const list = ref<RequiredCourse[]>([])
const filterIndustryType = ref('')

async function loadData() {
  const res = await listRequiredCourses(filterIndustryType.value || undefined)
  list.value = res.data.data
}

onMounted(loadData)
</script>
