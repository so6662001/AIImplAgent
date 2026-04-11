<template>
  <div>
    <div class="page-header">
      <h2>服务器管理</h2>
      <el-button type="primary" @click="showDialog = true">新建服务器</el-button>
    </div>

    <div class="card" style="margin-bottom: 20px;">
      <el-form inline>
        <el-form-item label="项目ID">
          <el-input-number v-model="filterProjectId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="card">
      <el-table :data="list" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="projectId" label="项目ID" width="80" />
        <el-table-column prop="serverName" label="服务器名称" min-width="140" />
        <el-table-column prop="host" label="主机地址" width="140" />
        <el-table-column prop="port" label="端口" width="80" />
        <el-table-column prop="dbType" label="数据库类型" width="110" />
        <el-table-column prop="dbName" label="数据库名" width="120" />
        <el-table-column label="SSL" width="70">
          <template #default="{ row }">
            <el-tag :type="row.sslEnabled ? 'success' : 'info'" size="small">
              {{ row.sslEnabled ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="serverStatusType(row.status)" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="networkLatencyMs" label="延迟(ms)" width="90" />
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleHealthCheck(row.id)">健康检查</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="showDialog" title="新建服务器" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="项目ID" prop="projectId">
          <el-input-number v-model="form.projectId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="服务器名称" prop="serverName">
          <el-input v-model="form.serverName" maxlength="60" />
        </el-form-item>
        <el-form-item label="主机地址" prop="host">
          <el-input v-model="form.host" />
        </el-form-item>
        <el-form-item label="端口" prop="port">
          <el-input-number v-model="form.port" :min="1" :max="65535" controls-position="right" />
        </el-form-item>
        <el-form-item label="数据库类型" prop="dbType">
          <el-select v-model="form.dbType" placeholder="请选择">
            <el-option label="MySQL" value="MySQL" />
            <el-option label="PostgreSQL" value="PostgreSQL" />
            <el-option label="SQLServer" value="SQLServer" />
          </el-select>
        </el-form-item>
        <el-form-item label="数据库名" prop="dbName">
          <el-input v-model="form.dbName" maxlength="60" />
        </el-form-item>
        <el-form-item label="SSL" prop="sslEnabled">
          <el-switch v-model="form.sslEnabled" active-text="启用" inactive-text="关闭" />
        </el-form-item>
        <el-form-item label="操作系统" prop="osType">
          <el-input v-model="form.osType" maxlength="30" />
        </el-form-item>
        <el-form-item label="ERP版本" prop="erpVersion">
          <el-input v-model="form.erpVersion" maxlength="30" />
        </el-form-item>
        <el-form-item label="API地址" prop="apiBaseUrl">
          <el-input v-model="form.apiBaseUrl" placeholder="http(s)://..." />
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
import { ref } from 'vue'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import { createServerProfile, listServerProfiles, healthCheck } from '@/api/server'
import type { ServerProfile } from '@/types'
import { required, maxLen, portRule, urlRule } from '@/utils/validators'

const list = ref<ServerProfile[]>([])
const showDialog = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const filterProjectId = ref(1)

const initForm = () => ({
  projectId: undefined as number | undefined,
  serverName: '',
  host: '',
  port: 3306,
  dbType: '',
  dbName: '',
  sslEnabled: false,
  osType: '',
  erpVersion: '',
  apiBaseUrl: '',
})

const form = ref(initForm())

const rules = {
  projectId: [required('项目ID不能为空')],
  serverName: [required('服务器名称不能为空'), maxLen(60)],
  host: [required('主机地址不能为空')],
  port: [required('端口不能为空'), portRule],
  dbType: [required('数据库类型不能为空')],
  dbName: [required('数据库名不能为空'), maxLen(60)],
  apiBaseUrl: [urlRule],
}

function serverStatusType(status: string) {
  const map: Record<string, string> = {
    HEALTHY: 'success',
    UNHEALTHY: 'danger',
    UNKNOWN: 'info',
    ACTIVE: 'success',
  }
  return map[status] || 'info'
}

async function loadData() {
  const res = await listServerProfiles(filterProjectId.value)
  list.value = res.data.data
}

async function handleHealthCheck(id: number) {
  await healthCheck(id)
  ElMessage.success('健康检查完成')
  await loadData()
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createServerProfile(form.value as unknown as Record<string, unknown>)
    ElMessage.success('服务器创建成功')
    showDialog.value = false
    form.value = initForm()
    await loadData()
  } finally {
    submitting.value = false
  }
}
</script>
