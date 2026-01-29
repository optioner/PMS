<template>
  <div class="dashboard-widgets">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="widget-card">
          <template #header>
            <div class="card-header">
              <span>My Tasks (ToDo)</span>
            </div>
          </template>
          <div class="widget-content">
            <span class="widget-number">{{ stats.taskStats?.todo || 0 }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="widget-card">
          <template #header>
            <div class="card-header">
              <span>In Progress</span>
            </div>
          </template>
          <div class="widget-content">
            <span class="widget-number">{{ stats.taskStats?.inProgress || 0 }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="widget-card danger">
          <template #header>
            <div class="card-header">
              <span>Overdue</span>
            </div>
          </template>
          <div class="widget-content">
            <span class="widget-number">{{ stats.taskStats?.overdue || 0 }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="widget-card">
          <template #header>
            <div class="card-header">
              <span>Hours (This Week)</span>
            </div>
          </template>
          <div class="widget-content">
            <span class="widget-number">{{ stats.timeStats?.weekTotal || 0 }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const stats = ref<any>({})

onMounted(async () => {
  const res = await request.get('/dashboard/personal')
  stats.value = res as any
})
</script>

<style scoped>
.dashboard-widgets {
  margin-bottom: 20px;
}
.widget-content {
  text-align: center;
}
.widget-number {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}
.widget-card.danger .widget-number {
  color: #f56c6c;
}
</style>
