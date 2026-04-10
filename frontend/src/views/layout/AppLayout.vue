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
        <el-menu-item index="/projects">
          <el-icon><Folder /></el-icon>
          <span>项目管理</span>
        </el-menu-item>
        <el-menu-item index="/products">
          <el-icon><Box /></el-icon>
          <span>货品档案</span>
        </el-menu-item>
        <el-menu-item index="/customers">
          <el-icon><User /></el-icon>
          <span>客户档案</span>
        </el-menu-item>
        <el-menu-item index="/suppliers">
          <el-icon><Goods /></el-icon>
          <span>供应商档案</span>
        </el-menu-item>
        <el-menu-item index="/warehouses">
          <el-icon><House /></el-icon>
          <span>仓库档案</span>
        </el-menu-item>
        <el-menu-item index="/categories">
          <el-icon><OfficeBuilding /></el-icon>
          <span>品类档案</span>
        </el-menu-item>
        <el-menu-item index="/related-units">
          <el-icon><Connection /></el-icon>
          <span>往来单位</span>
        </el-menu-item>
        <el-menu-item index="/training">
          <el-icon><Trophy /></el-icon>
          <span>培训考核</span>
        </el-menu-item>
        <el-menu-item index="/trainees">
          <el-icon><Avatar /></el-icon>
          <span>学员管理</span>
        </el-menu-item>
        <el-menu-item index="/training-logs">
          <el-icon><Calendar /></el-icon>
          <span>培训日志</span>
        </el-menu-item>
        <el-menu-item index="/training-dashboard">
          <el-icon><Odometer /></el-icon>
          <span>培训仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/research">
          <el-icon><DataAnalysis /></el-icon>
          <span>调研分析</span>
        </el-menu-item>
        <el-menu-item index="/account-sets">
          <el-icon><Setting /></el-icon>
          <span>帐套管理</span>
        </el-menu-item>
        <el-menu-item index="/engineers">
          <el-icon><UserFilled /></el-icon>
          <span>工程师管理</span>
        </el-menu-item>
        <el-menu-item index="/project-evals">
          <el-icon><Document /></el-icon>
          <span>项目评价</span>
        </el-menu-item>
        <el-menu-item index="/worklogs">
          <el-icon><EditPen /></el-icon>
          <span>工作日志</span>
        </el-menu-item>
        <el-menu-item index="/servers">
          <el-icon><Monitor /></el-icon>
          <span>服务器管理</span>
        </el-menu-item>
        <el-menu-item index="/go-live-checks">
          <el-icon><Checked /></el-icon>
          <span>上线检查</span>
        </el-menu-item>
        <el-menu-item index="/simulations">
          <el-icon><VideoPlay /></el-icon>
          <span>模拟演练</span>
        </el-menu-item>
        <el-menu-item index="/reports">
          <el-icon><Connection /></el-icon>
          <span>交付报告</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-main class="main-content">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
import { Folder, Box, User, Trophy, Document, Setting, DataAnalysis, Monitor, Checked, VideoPlay, Connection, UserFilled, Goods, House, OfficeBuilding, Avatar, Calendar, EditPen, Odometer } from '@element-plus/icons-vue'

const route = useRoute()
</script>

<style scoped>
.app-layout {
  height: 100vh;
}

.sidebar {
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  border-right: none;
  overflow-y: auto;
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
  --el-menu-bg-color: transparent;
  --el-menu-text-color: #a0aec0;
  --el-menu-active-color: #fff;
  --el-menu-hover-bg-color: rgba(255, 255, 255, 0.06);
}

.sidebar-menu .el-menu-item.is-active {
  background: rgba(67, 97, 238, 0.15);
  border-right: 3px solid var(--primary);
}

.main-content {
  padding: 24px 32px;
  overflow-y: auto;
}
</style>
