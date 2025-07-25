<script setup lang="ts">
import { RouterView } from 'vue-router'
</script>

<template>
  <div id="app">
    <el-container>
      <el-header class="app-header">
        <div class="header-content">
          <h1 class="app-title">订单管理系统</h1>
          <el-menu
            mode="horizontal"
            :router="true"
            class="header-menu"
          >
            <el-menu-item index="/">
              <span class="menu-icon">🏠</span>
              仪表盘
            </el-menu-item>
            <el-menu-item index="/orders">
              <span class="menu-icon">📋</span>
              订单列表
            </el-menu-item>
            <el-menu-item index="/search">
              <span class="menu-icon">🔍</span>
              订单查询
            </el-menu-item>
            <el-menu-item index="/export">
              <span class="menu-icon">📥</span>
              导出功能
            </el-menu-item>
          </el-menu>
        </div>
      </el-header>
      <el-main class="app-main">
        <RouterView />
      </el-main>
    </el-container>
  </div>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
}

.app-header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  padding: 0 20px;
}

.app-title {
  color: #303133;
  font-size: 24px;
  font-weight: 500;
  margin: 0;
}

.header-menu {
  border-bottom: none;
}

.menu-icon {
  margin-right: 8px;
  font-size: 16px;
}

.app-main {
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
  padding: 20px;
}

.el-container {
  min-height: 100vh;
}
</style>
