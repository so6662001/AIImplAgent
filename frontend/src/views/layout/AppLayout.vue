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
        <el-menu-item index="/training">
          <el-icon><Trophy /></el-icon>
          <span>培训考核</span>
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
import { Folder, Box, User, Trophy } from '@element-plus/icons-vue'

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
