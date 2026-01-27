<template>
  <div class="report-container">
    <el-page-header @back="$router.back()" content="Project Report" />
    
    <div v-if="report" class="report-content">
      <h2 class="project-title">{{ report.projectName }}</h2>
      
      <el-row :gutter="20" class="stat-row">
        <el-col :span="6">
          <el-card shadow="hover">
            <template #header>Total Tasks</template>
            <div class="stat-value">{{ report.taskStats.total }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <template #header>Completion Rate</template>
            <div class="stat-value">{{ report.taskStats.completionRate.toFixed(1) }}%</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <template #header>Hours (Est. / Act.)</template>
            <div class="stat-value">
              {{ report.timeStats.totalEstimated }} / {{ report.timeStats.totalActual }}
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="danger">
            <template #header>Overdue Tasks</template>
            <div class="stat-value">{{ report.taskStats.overdue }}</div>
          </el-card>
        </el-col>
      </el-row>

      <div class="charts-row">
        <el-card class="chart-card">
          <template #header>Task Distribution</template>
          <div class="distribution-bars">
            <div v-for="(count, status) in report.taskDistribution" :key="status" class="bar-item">
              <span class="bar-label">{{ status }}</span>
              <div class="bar-track">
                <div class="bar-fill" :style="{ width: getPercentage(count) + '%', backgroundColor: getStatusColor(status) }"></div>
              </div>
              <span class="bar-count">{{ count }}</span>
            </div>
          </div>
        </el-card>

        <el-card class="chart-card">
          <template #header>Burndown Chart (Placeholder)</template>
          <div class="burndown-placeholder">
            <div class="placeholder-text">Chart visualization coming soon</div>
            <!-- Simple CSS representation of a downward trend -->
            <div class="trend-line"></div>
          </div>
        </el-card>
      </div>

      <el-card class="chart-card mt-4" v-if="report.userEfficiency">
        <template #header>User Efficiency</template>
        <el-table :data="report.userEfficiency" style="width: 100%">
          <el-table-column prop="userName" label="User" />
          <el-table-column prop="assignedTasks" label="Assigned" width="100" />
          <el-table-column prop="completedTasks" label="Completed" width="100" />
          <el-table-column label="Hours (Est / Act)">
            <template #default="scope">
              {{ scope.row.totalEstimatedHours }} / {{ scope.row.totalActualHours }}
            </template>
          </el-table-column>
          <el-table-column label="Efficiency Ratio">
            <template #default="scope">
              <el-tag :type="getEfficiencyColor(scope.row.efficiencyRatio)">
                {{ scope.row.efficiencyRatio?.toFixed(1) }}%
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'

const route = useRoute()
const projectId = route.params.id
const report = ref<any>(null)

onMounted(async () => {
  const res = await request.get(`/dashboard/project/${projectId}`)
  report.value = res as any
})

const getPercentage = (count: number) => {
  if (!report.value?.taskStats.total) return 0
  return (count / report.value.taskStats.total) * 100
}

const getStatusColor = (status: string) => {
  const colors: Record<string, string> = {
    'TODO': '#909399',
    'IN_PROGRESS': '#409eff',
    'REVIEW': '#e6a23c',
    'DONE': '#67c23a',
    'CANCELLED': '#f56c6c'
  }
  return colors[status] || '#ccc'
}

const getEfficiencyColor = (ratio: number) => {
  if (!ratio) return 'info'
  if (ratio >= 100) return 'success'
  if (ratio >= 80) return 'warning'
  return 'danger'
}
</script>

<style scoped>
.report-container {
  padding: 20px;
}
.report-content {
  margin-top: 20px;
}
.project-title {
  margin-bottom: 20px;
  color: #303133;
}
.stat-row {
  margin-bottom: 20px;
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  text-align: center;
}
.danger .stat-value {
  color: #f56c6c;
}
.charts-row {
  display: flex;
  gap: 20px;
}
.chart-card {
  flex: 1;
}
.distribution-bars {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.bar-item {
  display: flex;
  align-items: center;
  gap: 10px;
}
.bar-label {
  width: 100px;
  font-size: 12px;
}
.bar-track {
  flex: 1;
  height: 20px;
  background-color: #f0f2f5;
  border-radius: 4px;
  overflow: hidden;
}
.bar-fill {
  height: 100%;
  transition: width 0.3s ease;
}
.bar-count {
  width: 30px;
  text-align: right;
  font-weight: bold;
}
.burndown-placeholder {
  height: 200px;
  background-color: #f9fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  border: 1px dashed #dcdfe6;
}
.trend-line {
  position: absolute;
  width: 80%;
  height: 2px;
  background: linear-gradient(to right, #409eff, #67c23a);
  transform: rotate(15deg);
}
.mt-4 {
  margin-top: 20px;
}
</style>
