<template>
  <el-dropdown trigger="click" @command="handleCommand">
    <span class="el-dropdown-link">
      {{ currentLocale === 'zh' ? '中文' : 'English' }}
      <el-icon class="el-icon--right"><arrow-down /></el-icon>
    </span>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="zh">中文</el-dropdown-item>
        <el-dropdown-item command="en">English</el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ArrowDown } from '@element-plus/icons-vue'

const { locale } = useI18n()

const currentLocale = computed(() => locale.value)

const handleCommand = (command: string) => {
  locale.value = command
  // Ideally save to localStorage
  localStorage.setItem('locale', command)
}
</script>

<style scoped>
.el-dropdown-link {
  cursor: pointer;
  color: var(--el-color-primary);
  display: flex;
  align-items: center;
}
</style>
