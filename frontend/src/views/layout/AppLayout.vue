<template>
  <el-container class="app-layout">
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <span class="logo-icon">⬡</span>
        <span class="logo-text">AI交付平台</span>
      </div>
      <el-menu
        :default-active="route.path"
        router
        class="sidebar-menu"
      >
        <el-menu-item index="/dispatch">
          <el-icon><Guide /></el-icon>
          <span>项目调度</span>
        </el-menu-item>

        <el-menu-item index="/projects">
          <el-icon><Folder /></el-icon>
          <span>项目管理</span>
        </el-menu-item>

        <el-sub-menu index="org">
          <template #title>
            <el-icon><Avatar /></el-icon>
            <span>组织架构</span>
          </template>
          <el-menu-item index="/departments">部门管理</el-menu-item>
          <el-menu-item index="/employees">员工管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="finance">
          <template #title>
            <el-icon><Money /></el-icon>
            <span>财务基础</span>
          </template>
          <el-menu-item index="/bank-accounts">银行账号</el-menu-item>
          <el-menu-item index="/account-subjects">财务科目</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="archive">
          <template #title>
            <el-icon><Box /></el-icon>
            <span>档案管理</span>
          </template>
          <el-menu-item index="/products">货品档案</el-menu-item>
          <el-menu-item index="/customers">客户档案</el-menu-item>
          <el-menu-item index="/suppliers">供应商档案</el-menu-item>
          <el-menu-item index="/warehouses">仓库档案</el-menu-item>
          <el-menu-item index="/categories">品类档案</el-menu-item>
          <el-menu-item index="/related-units">往来单位</el-menu-item>
          <el-menu-item index="/storage-locations">库位管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="opening">
          <template #title>
            <el-icon><Coin /></el-icon>
            <span>期初数据</span>
          </template>
          <el-menu-item index="/inventory-balances">库存期初</el-menu-item>
          <el-menu-item index="/customer-balances">客户往来</el-menu-item>
          <el-menu-item index="/supplier-balances">供应商往来</el-menu-item>
          <el-menu-item index="/account-balances">账户余额</el-menu-item>
          <el-menu-item index="/subject-balances">科目余额</el-menu-item>
          <el-menu-item index="/other-receivables">其他应收</el-menu-item>
          <el-menu-item index="/other-payables">其他应付</el-menu-item>
          <el-menu-item index="/invoice-balances">发票期初</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/data-import">
          <el-icon><DataLine /></el-icon>
          <span>数据治理</span>
        </el-menu-item>

        <el-sub-menu index="training">
          <template #title>
            <el-icon><Trophy /></el-icon>
            <span>培训管理</span>
          </template>
          <el-menu-item index="/training">培训考核</el-menu-item>
          <el-menu-item index="/trainees">学员管理</el-menu-item>
          <el-menu-item index="/training-logs">培训日志</el-menu-item>
          <el-menu-item index="/training-dashboard">培训仪表盘</el-menu-item>
          <el-menu-item index="/required-courses">KA必学课程</el-menu-item>
          <el-menu-item index="/qa-sessions">
            <el-icon><ChatDotSquare /></el-icon>
            <span>问答管理</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="delivery">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>项目交付</span>
          </template>
          <el-menu-item index="/research">调研分析</el-menu-item>
          <el-menu-item index="/account-sets">帐套管理</el-menu-item>
          <el-menu-item index="/project-plans">交付计划</el-menu-item>
          <el-menu-item index="/project-evals">项目评价</el-menu-item>
          <el-menu-item index="/go-live-checks">上线检查</el-menu-item>
          <el-menu-item index="/simulations">模拟演练</el-menu-item>
          <el-menu-item index="/reports">交付报告</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="workforce">
          <template #title>
            <el-icon><UserFilled /></el-icon>
            <span>团队管理</span>
          </template>
          <el-menu-item index="/engineers">工程师管理</el-menu-item>
          <el-menu-item index="/worklogs">工作日志</el-menu-item>
          <el-menu-item index="/workforce-dashboard">
            <el-icon><TrendCharts /></el-icon>
            <span>人力看板</span>
          </el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/servers">
          <el-icon><Monitor /></el-icon>
          <span>服务器管理</span>
        </el-menu-item>

        <el-menu-item index="/support-tickets">
          <el-icon><Headset /></el-icon>
          <span>现场辅助</span>
        </el-menu-item>

        <el-menu-item index="/after-sales">
          <el-icon><Service /></el-icon>
          <span>售后服务</span>
        </el-menu-item>

        <el-menu-item index="/smart-input">
          <el-icon><Edit /></el-icon>
          <span>智能录入</span>
        </el-menu-item>

        <el-menu-item index="/knowledge">
          <el-icon><Collection /></el-icon>
          <span>知识库</span>
        </el-menu-item>

        <el-sub-menu index="sys-config">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统配置</span>
          </template>
          <el-menu-item index="/llm-providers">
            <el-icon><Cpu /></el-icon>
            <span>LLM模型配置</span>
          </el-menu-item>
          <el-menu-item index="/agent-configs">
            <el-icon><MagicStick /></el-icon>
            <span>智能体配置</span>
          </el-menu-item>
          <el-menu-item index="/agent-decisions">
            <el-icon><Document /></el-icon>
            <span>决策日志</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>

      <div class="sidebar-footer">
        <div class="user-info">
          <el-icon><User /></el-icon>
          <span class="user-name">{{ authStore.realName || authStore.username }}</span>
        </div>
        <el-button text class="logout-btn" @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          <span>退出</span>
        </el-button>
      </div>
    </el-aside>
    <el-main class="main-content">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import {
  Folder, Box, User, Trophy, Document, Setting, Monitor, UserFilled,
  Avatar, Money, Coin, Cpu, MagicStick, SwitchButton, ChatDotSquare, Guide, DataLine, Headset, Service, Edit, TrendCharts, Collection,
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.app-layout {
  height: 100vh;
}

.sidebar {
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  border-right: none;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 24px;
  color: #fff;
}

.logo-icon {
  font-size: 24px;
  color: var(--primary);
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
}

.sidebar-menu {
  border-right: none;
  background: transparent;
  flex: 1;
  --el-menu-bg-color: transparent;
  --el-menu-text-color: #a0aec0;
  --el-menu-active-color: #fff;
  --el-menu-hover-bg-color: rgba(255, 255, 255, 0.06);
}

.sidebar-menu .el-menu-item.is-active {
  background: rgba(67, 97, 238, 0.15);
  border-right: 3px solid var(--primary);
}

.sidebar-footer {
  padding: 12px 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #a0aec0;
  font-size: 13px;
  min-width: 0;
}

.user-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.logout-btn {
  color: #a0aec0;
  font-size: 13px;
  padding: 4px 8px;
  flex-shrink: 0;
}

.logout-btn:hover {
  color: #f56c6c;
}

.main-content {
  padding: 24px 32px;
  overflow-y: auto;
}
</style>
