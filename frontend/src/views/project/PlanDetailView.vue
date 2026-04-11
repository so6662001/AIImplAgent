<template>
  <div class="plan-detail">
    <!-- Summary Card -->
    <el-card shadow="hover" style="margin-bottom: 20px">
      <div style="display: flex; align-items: center; gap: 24px">
        <el-statistic title="预估工期" :value="plan.estimatedDurationDays" suffix="天" />
        <el-divider direction="vertical" style="height: 40px" />
        <div>
          <span style="color: #909399; font-size: 13px">风险等级</span>
          <div style="margin-top: 4px">
            <el-tag :type="riskTagType(plan.riskLevel)" size="large">{{ plan.riskLevel }}</el-tag>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 里程碑时间线 -->
    <el-card shadow="hover" style="margin-bottom: 20px" v-if="plan.milestones?.length">
      <template #header><span style="font-weight: 600">里程碑时间线</span></template>
      <el-timeline>
        <el-timeline-item
          v-for="(ms, idx) in plan.milestones"
          :key="idx"
          :timestamp="'第 ' + ms.dayOffset + ' 天'"
          placement="top"
        >
          <strong>{{ ms.name }}</strong>
          <p style="margin: 4px 0 0; color: #606266; font-size: 13px">{{ ms.deliverables }}</p>
        </el-timeline-item>
      </el-timeline>
    </el-card>

    <!-- WBS任务分解 -->
    <el-card shadow="hover" style="margin-bottom: 20px" v-if="plan.wbsItems?.length">
      <template #header><span style="font-weight: 600">WBS任务分解</span></template>
      <el-table :data="plan.wbsItems" stripe :span-method="wbsSpanMethod" border size="small">
        <el-table-column prop="phase" label="阶段" width="90" />
        <el-table-column prop="taskName" label="任务名称" min-width="160" />
        <el-table-column label="时间" width="120">
          <template #default="{ row }">第{{ row.startDay }}–{{ row.endDay }}天</template>
        </el-table-column>
        <el-table-column prop="responsible" label="负责人" width="110" />
        <el-table-column prop="deliverable" label="交付物" min-width="140" />
      </el-table>
    </el-card>

    <!-- 资源计划 -->
    <el-card shadow="hover" style="margin-bottom: 20px" v-if="plan.resourcePlan?.length">
      <template #header><span style="font-weight: 600">资源计划</span></template>
      <el-table :data="plan.resourcePlan" stripe border size="small">
        <el-table-column prop="role" label="角色" width="150" />
        <el-table-column prop="headcount" label="人数" width="80" />
        <el-table-column prop="phase" label="参与阶段" width="140" />
        <el-table-column prop="skills" label="技能要求" min-width="200" />
      </el-table>
    </el-card>

    <!-- 风险评估 -->
    <el-card shadow="hover" style="margin-bottom: 20px" v-if="plan.riskPlan?.length">
      <template #header><span style="font-weight: 600">风险评估</span></template>
      <el-table :data="plan.riskPlan" stripe border size="small">
        <el-table-column prop="riskName" label="风险名称" min-width="160">
          <template #default="{ row }">
            <span>{{ row.riskName }}</span>
            <el-icon v-if="row.isHighRisk || row.highRisk" style="color: #E6A23C; margin-left: 4px; vertical-align: middle"><StarFilled /></el-icon>
          </template>
        </el-table-column>
        <el-table-column label="风险等级" width="100">
          <template #default="{ row }">
            <el-tag :type="riskTagType(row.riskLevel)" size="small">{{ row.riskLevel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="impact" label="影响" min-width="200" />
        <el-table-column prop="mitigation" label="应对措施" min-width="200" />
      </el-table>
    </el-card>

    <!-- 甘特图 -->
    <el-card shadow="hover" style="margin-bottom: 20px" v-if="plan.ganttData?.length">
      <template #header><span style="font-weight: 600">甘特图</span></template>
      <div class="gantt-chart">
        <div class="gantt-header">
          <div class="gantt-label-col">任务</div>
          <div class="gantt-bar-col">
            <div class="gantt-scale">
              <span v-for="tick in ganttTicks" :key="tick" class="gantt-tick" :style="{ left: tickPercent(tick) + '%' }">
                {{ tick }}
              </span>
            </div>
          </div>
        </div>
        <div v-for="(task, idx) in plan.ganttData" :key="idx" class="gantt-row">
          <div class="gantt-label-col" :title="task.taskName">{{ task.taskName }}</div>
          <div class="gantt-bar-col">
            <div
              class="gantt-bar"
              :style="{
                left: barLeft(task.startDay) + '%',
                width: barWidth(task.startDay, task.endDay) + '%',
                backgroundColor: phaseColor(task.phase),
              }"
              :title="`${task.phase}: ${task.taskName} (第${task.startDay}-${task.endDay}天)`"
            />
          </div>
        </div>
        <div class="gantt-legend">
          <span v-for="(color, phase) in phaseColorMap" :key="phase" class="gantt-legend-item">
            <span class="gantt-legend-dot" :style="{ backgroundColor: color }" />
            {{ phase }}
          </span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { StarFilled } from '@element-plus/icons-vue'
import type { GeneratedPlan } from '@/types'

const props = defineProps<{ plan: GeneratedPlan }>()

function riskTagType(level: string) {
  if (level === 'HIGH') return 'danger'
  if (level === 'MEDIUM') return 'warning'
  return 'success'
}

const phaseColorMap: Record<string, string> = {
  '调研': '#409EFF',
  '计划': '#67C23A',
  '演练': '#E6A23C',
  '培训': '#F56C6C',
  '数据': '#909399',
  '上线': '#9B59B6',
  '跟进': '#1ABC9C',
  '验收': '#3498DB',
}

function phaseColor(phase: string) {
  return phaseColorMap[phase] || '#409EFF'
}

const maxDay = computed(() => {
  if (!props.plan.ganttData?.length) return 1
  return Math.max(...props.plan.ganttData.map(t => t.endDay), 1)
})

function barLeft(startDay: number) {
  return (startDay / maxDay.value) * 100
}

function barWidth(startDay: number, endDay: number) {
  return Math.max(((endDay - startDay) / maxDay.value) * 100, 0.5)
}

const ganttTicks = computed(() => {
  const max = maxDay.value
  const step = Math.max(1, Math.ceil(max / 10))
  const ticks: number[] = []
  for (let i = 0; i <= max; i += step) ticks.push(i)
  if (ticks[ticks.length - 1] !== max) ticks.push(max)
  return ticks
})

function tickPercent(tick: number) {
  return (tick / maxDay.value) * 100
}

function wbsSpanMethod({ row, column, rowIndex }: { row: any; column: any; rowIndex: number }) {
  if (column.property === 'phase') {
    const items = props.plan.wbsItems
    if (rowIndex === 0 || items[rowIndex].phase !== items[rowIndex - 1].phase) {
      let count = 1
      for (let i = rowIndex + 1; i < items.length && items[i].phase === row.phase; i++) count++
      return { rowspan: count, colspan: 1 }
    }
    return { rowspan: 0, colspan: 0 }
  }
}
</script>

<style scoped>
.gantt-chart {
  overflow-x: auto;
}
.gantt-header {
  display: flex;
  border-bottom: 2px solid #dcdfe6;
  padding-bottom: 4px;
  margin-bottom: 2px;
  font-size: 12px;
  font-weight: 600;
  color: #606266;
}
.gantt-label-col {
  width: 160px;
  min-width: 160px;
  padding: 4px 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 12px;
}
.gantt-bar-col {
  flex: 1;
  position: relative;
  min-height: 20px;
}
.gantt-scale {
  position: relative;
  height: 18px;
}
.gantt-tick {
  position: absolute;
  transform: translateX(-50%);
  font-size: 10px;
  color: #909399;
}
.gantt-row {
  display: flex;
  align-items: center;
  border-bottom: 1px solid #f0f0f0;
  min-height: 26px;
}
.gantt-bar {
  position: absolute;
  height: 16px;
  border-radius: 3px;
  opacity: 0.85;
  transition: opacity 0.2s;
  cursor: pointer;
}
.gantt-bar:hover {
  opacity: 1;
}
.gantt-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 12px;
  padding-top: 8px;
  border-top: 1px solid #ebeef5;
}
.gantt-legend-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #606266;
}
.gantt-legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 2px;
  display: inline-block;
}
</style>
