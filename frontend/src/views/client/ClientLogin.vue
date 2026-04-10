<template>
  <div class="client-login-page">
    <div class="login-card">
      <div class="login-header">
        <div class="chat-icon">
          <svg viewBox="0 0 48 48" width="56" height="56" fill="none">
            <rect x="4" y="8" width="40" height="28" rx="6" fill="#4361ee" opacity="0.15"/>
            <rect x="4" y="8" width="40" height="28" rx="6" stroke="#4361ee" stroke-width="2.5"/>
            <circle cx="16" cy="22" r="2.5" fill="#4361ee"/>
            <circle cx="24" cy="22" r="2.5" fill="#4361ee"/>
            <circle cx="32" cy="22" r="2.5" fill="#4361ee"/>
            <path d="M14 36l6-8h-6" fill="#4361ee" opacity="0.15"/>
            <path d="M14 36l6-8" stroke="#4361ee" stroke-width="2.5" stroke-linecap="round"/>
          </svg>
        </div>
        <h1 class="login-title">ERP培训助手</h1>
        <p class="login-subtitle">有问题？随时向我提问，帮您快速掌握系统操作</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item label="项目编号" prop="projectCode">
          <el-input
            v-model="form.projectCode"
            placeholder="请输入您的项目编号"
            size="large"
            :prefix-icon="FolderOpened"
            clearable
          />
        </el-form-item>
        <el-form-item label="您的姓名" prop="employeeName">
          <el-input
            v-model="form.employeeName"
            placeholder="请输入您的姓名"
            size="large"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            进入问答
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, FolderOpened } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useClientAuthStore } from '@/stores/clientAuth'
import { clientLogin } from '@/api/qasession'

const router = useRouter()
const clientAuth = useClientAuthStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  projectCode: '',
  employeeName: '',
})

const rules: FormRules = {
  projectCode: [{ required: true, message: '请输入项目编号', trigger: 'blur' }],
  employeeName: [{ required: true, message: '请输入您的姓名', trigger: 'blur' }],
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await clientLogin(form.projectCode, form.employeeName)
    const data = res.data.data
    clientAuth.setAuth({
      accessToken: data.accessToken,
      projectId: data.projectId,
      projectName: data.projectName,
      employeeName: data.employeeName,
      role: data.role,
    })
    router.push('/client/chat')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.client-login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f0fe 0%, #f0e6ff 50%, #fce4ec 100%);
  padding: 20px;
}

.login-card {
  background: #fff;
  border-radius: 20px;
  padding: 48px 40px 36px;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 20px 60px rgba(67, 97, 238, 0.1), 0 4px 16px rgba(0, 0, 0, 0.05);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.chat-icon {
  margin-bottom: 12px;
}

.login-title {
  font-size: 26px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 8px;
  letter-spacing: 1px;
}

.login-subtitle {
  font-size: 14px;
  color: #8e8e93;
  margin: 0;
  line-height: 1.5;
}

.login-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #3a3a4a;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 4px 12px;
}

.login-btn {
  width: 100%;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  height: 48px;
  margin-top: 8px;
  background: linear-gradient(135deg, #4361ee, #6c63ff);
  border: none;
}

.login-btn:hover {
  background: linear-gradient(135deg, #3a56d4, #5b54e6);
}

@media (max-width: 480px) {
  .login-card {
    padding: 32px 24px 28px;
    border-radius: 16px;
  }

  .login-title {
    font-size: 22px;
  }
}
</style>
