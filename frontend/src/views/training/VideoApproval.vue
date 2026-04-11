<template>
  <div>
    <h2 style="margin: 0 0 20px; font-size: 20px; font-weight: 700; color: #1d1d1f">视频审批</h2>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane name="PENDING">
        <template #label>
          待审批
          <el-badge v-if="pendingCount > 0" :value="pendingCount" :max="99" class="tab-badge" />
        </template>
      </el-tab-pane>
      <el-tab-pane label="已审批" name="APPROVED" />
      <el-tab-pane label="已驳回" name="REJECTED" />
    </el-tabs>

    <div v-if="loading" style="text-align: center; padding: 40px; color: #8e8e93">
      <el-icon class="is-loading" :size="24"><Loading /></el-icon>
      <span style="margin-left: 8px">加载中...</span>
    </div>

    <el-table v-else :data="videos" stripe style="width: 100%">
      <el-table-column label="标题" prop="title" min-width="160" />
      <el-table-column label="发布者" prop="publisherName" width="120" />
      <el-table-column label="分类" prop="categoryModule" width="120">
        <template #default="{ row }">
          <el-tag size="small" type="info">{{ row.categoryModule }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="积分费用" width="100">
        <template #default="{ row }">
          {{ row.pointsCost > 0 ? `${row.pointsCost} 积分` : '免费' }}
        </template>
      </el-table-column>
      <el-table-column label="视频" width="80">
        <template #default="{ row }">
          <a v-if="row.videoUrl" :href="row.videoUrl" target="_blank" style="color: #4361ee">查看</a>
          <span v-else style="color: #b0b0b5">-</span>
        </template>
      </el-table-column>
      <el-table-column label="发布时间" width="170">
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>

      <template v-if="activeTab === 'PENDING'">
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="success" size="small" @click="handleApprove(row)">通过</el-button>
            <el-button type="danger" size="small" @click="handleReject(row)">驳回</el-button>
          </template>
        </el-table-column>
      </template>

      <template v-if="activeTab === 'APPROVED'">
        <el-table-column label="审批人" prop="approvedBy" width="120" />
        <el-table-column label="审批时间" width="170">
          <template #default="{ row }">{{ formatDate(row.approvedAt) }}</template>
        </el-table-column>
      </template>

      <template v-if="activeTab === 'REJECTED'">
        <el-table-column label="驳回原因" prop="rejectionReason" min-width="180" />
      </template>
    </el-table>

    <el-empty v-if="!loading && videos.length === 0" description="暂无数据" />

    <!-- Reject dialog -->
    <el-dialog v-model="rejectDialogVisible" title="驳回视频" width="420px" destroy-on-close>
      <el-form>
        <el-form-item label="驳回原因" required>
          <el-input v-model="rejectionReason" type="textarea" :rows="3" placeholder="请输入驳回原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { listAllVideos, approveVideo, getPendingCount } from '@/api/uservideo'

const activeTab = ref('PENDING')
const loading = ref(false)
const submitting = ref(false)
const videos = ref<any[]>([])
const pendingCount = ref(0)

const rejectDialogVisible = ref(false)
const rejectionReason = ref('')
const rejectingVideo = ref<any>(null)

onMounted(() => {
  loadVideos()
  loadPendingCount()
})

async function loadVideos() {
  loading.value = true
  try {
    const res = await listAllVideos(activeTab.value)
    videos.value = res.data.data || []
  } catch { /* handled by interceptor */ }
  finally { loading.value = false }
}

async function loadPendingCount() {
  try {
    const res = await getPendingCount()
    pendingCount.value = res.data.data ?? 0
  } catch { /* ignore */ }
}

function handleTabChange() {
  loadVideos()
}

async function handleApprove(row: any) {
  try {
    await approveVideo(row.id, { approved: true })
    ElMessage.success('已通过')
    loadVideos()
    loadPendingCount()
  } catch { /* handled by interceptor */ }
}

function handleReject(row: any) {
  rejectingVideo.value = row
  rejectionReason.value = ''
  rejectDialogVisible.value = true
}

async function confirmReject() {
  if (!rejectionReason.value.trim()) {
    ElMessage.warning('请输入驳回原因')
    return
  }
  submitting.value = true
  try {
    await approveVideo(rejectingVideo.value.id, {
      approved: false,
      rejectionReason: rejectionReason.value,
    })
    ElMessage.success('已驳回')
    rejectDialogVisible.value = false
    loadVideos()
    loadPendingCount()
  } catch { /* handled by interceptor */ }
  finally { submitting.value = false }
}

function formatDate(iso: string): string {
  if (!iso) return '-'
  const d = new Date(iso)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}
</script>

<style scoped>
.tab-badge {
  margin-left: 6px;
}
</style>
