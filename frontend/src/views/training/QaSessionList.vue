<template>
  <div>
    <div class="page-header">
      <h2>问答管理</h2>
    </div>
    <div class="card">
      <div style="margin-bottom: 16px; display: flex; align-items: center; gap: 12px">
        <el-input
          v-model.number="filterProjectId"
          placeholder="输入项目ID筛选"
          style="width: 200px"
          clearable
          @clear="filterProjectId = undefined"
        />
        <el-button type="primary" @click="loadSessions">查询</el-button>
      </div>

      <el-table :data="sessions" stripe v-loading="loading" @row-click="openSession">
        <el-table-column prop="id" label="会话ID" width="80" />
        <el-table-column prop="title" label="会话标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="employeeName" label="学员姓名" width="120" />
        <el-table-column prop="messageCount" label="消息数" width="90" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'CLOSED' ? 'info' : 'success'" size="small">
              {{ row.status === 'CLOSED' ? '已关闭' : '进行中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Session detail dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="'会话详情 — ' + (activeSession?.title || '')"
      width="720px"
      top="5vh"
      destroy-on-close
    >
      <div class="conversation-view" v-loading="loadingMsgs">
        <div v-for="msg in sessionMessages" :key="msg.id" class="conv-msg" :class="msg.role === 'USER' ? 'conv-user' : 'conv-ai'">
          <div class="conv-role">{{ msg.role === 'USER' ? '学员' : (msg.role === 'PM' ? '顾问' : 'AI') }}</div>
          <div class="conv-content">{{ msg.content }}</div>
          <div v-if="msg.relatedModule" class="conv-meta">
            <el-tag size="small" type="info">{{ msg.relatedModule }}</el-tag>
          </div>
          <div class="conv-time">{{ formatTime(msg.createTime) }}</div>
        </div>
        <div v-if="sessionMessages.length === 0 && !loadingMsgs" style="text-align: center; color: #8e8e93; padding: 32px">
          暂无消息
        </div>
      </div>

      <el-divider />

      <div class="reply-section">
        <h4 style="margin: 0 0 12px; font-size: 15px">回复学员</h4>
        <el-form label-position="top">
          <el-form-item label="回复内容" required>
            <el-input v-model="replyForm.content" type="textarea" :rows="3" placeholder="请输入回复内容" />
          </el-form-item>
          <el-form-item label="相关模块">
            <el-input v-model="replyForm.relatedModule" placeholder="可选，如：库存管理" />
          </el-form-item>
          <el-form-item label="操作视频链接">
            <el-input v-model="replyForm.relatedVideoUrl" placeholder="可选，视频URL" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="replying" @click="handleReply">发送回复</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { listQaSessions, getSessionMessages, replyToSession } from '@/api/qasession'
import type { QaSession, QaMessage } from '@/types'

const filterProjectId = ref<number | undefined>(undefined)
const sessions = ref<QaSession[]>([])
const loading = ref(false)

const dialogVisible = ref(false)
const activeSession = ref<QaSession | null>(null)
const sessionMessages = ref<QaMessage[]>([])
const loadingMsgs = ref(false)
const replying = ref(false)

const replyForm = reactive({
  content: '',
  relatedModule: '',
  relatedVideoUrl: '',
})

async function loadSessions() {
  if (!filterProjectId.value) {
    ElMessage.warning('请输入项目ID')
    return
  }
  loading.value = true
  try {
    const res = await listQaSessions(filterProjectId.value)
    sessions.value = res.data.data || []
  } finally {
    loading.value = false
  }
}

async function openSession(row: QaSession) {
  activeSession.value = row
  dialogVisible.value = true
  loadingMsgs.value = true
  replyForm.content = ''
  replyForm.relatedModule = ''
  replyForm.relatedVideoUrl = ''
  try {
    const res = await getSessionMessages(row.id)
    sessionMessages.value = res.data.data || []
  } finally {
    loadingMsgs.value = false
  }
}

async function handleReply() {
  if (!replyForm.content.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  if (!activeSession.value) return
  replying.value = true
  try {
    const data: { content: string; relatedModule?: string; relatedVideoUrl?: string } = {
      content: replyForm.content,
    }
    if (replyForm.relatedModule) data.relatedModule = replyForm.relatedModule
    if (replyForm.relatedVideoUrl) data.relatedVideoUrl = replyForm.relatedVideoUrl

    const res = await replyToSession(activeSession.value.id, data)
    sessionMessages.value.push(res.data.data)
    replyForm.content = ''
    replyForm.relatedModule = ''
    replyForm.relatedVideoUrl = ''
    ElMessage.success('回复成功')
  } finally {
    replying.value = false
  }
}

function formatTime(iso: string) {
  if (!iso) return ''
  const d = new Date(iso)
  const MM = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  return `${MM}-${dd} ${hh}:${mm}`
}
</script>

<style scoped>
.conversation-view {
  max-height: 400px;
  overflow-y: auto;
  padding: 8px 0;
}

.conv-msg {
  margin-bottom: 16px;
  padding: 12px 16px;
  border-radius: 10px;
}

.conv-user {
  background: #e8f0fe;
  margin-left: 40px;
}

.conv-ai {
  background: #f5f5f7;
  margin-right: 40px;
}

.conv-role {
  font-size: 12px;
  font-weight: 600;
  color: #8e8e93;
  margin-bottom: 4px;
}

.conv-content {
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
  color: #1d1d1f;
}

.conv-meta {
  margin-top: 6px;
}

.conv-time {
  font-size: 11px;
  color: #b0b0b5;
  margin-top: 4px;
}

.reply-section {
  padding: 0 4px;
}
</style>
