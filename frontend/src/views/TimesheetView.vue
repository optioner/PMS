<template>
  <div class="timesheet-container">
    <el-page-header @back="$router.push('/')" content="My Timesheets" />
    
    <div class="actions">
      <el-button type="primary" @click="showDialog = true">Log Time</el-button>
    </div>

    <el-table :data="timesheets" style="width: 100%">
      <el-table-column prop="date" label="Date" width="180" />
      <el-table-column prop="task.title" label="Task" />
      <el-table-column prop="hours" label="Hours" width="100" />
      <el-table-column prop="description" label="Description" />
      <el-table-column label="Status" width="120">
        <template #default="scope">
          <el-tag :type="scope.row.isSubmitted ? 'success' : 'info'">{{ scope.row.isSubmitted ? 'Submitted' : 'Draft' }}</el-tag>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showDialog" title="Log Time">
      <el-form :model="form" label-width="100px">
        <el-form-item label="Project">
          <el-select v-model="selectedProjectId" placeholder="Select Project" @change="fetchProjectTasks">
            <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="Task">
          <el-select v-model="form.taskId" placeholder="Select Task" :disabled="!selectedProjectId">
            <el-option v-for="t in tasks" :key="t.id" :label="t.title" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="Date">
          <el-date-picker v-model="form.date" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="Hours">
          <el-input-number v-model="form.hours" :step="0.5" :min="0" />
        </el-form-item>
        <el-form-item label="Description">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showDialog = false">Cancel</el-button>
          <el-button type="primary" @click="handleSubmit">Submit</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { useProjectStore } from '@/stores/project.store'
import { getTasks } from '@/api/task'
import { ElMessage } from 'element-plus'

const projectStore = useProjectStore()
const timesheets = ref([])
const showDialog = ref(false)
const projects = ref([])
const tasks = ref([])
const selectedProjectId = ref(null)

const form = reactive({
  taskId: null,
  date: new Date().toISOString().slice(0, 10),
  hours: 0,
  description: ''
})

onMounted(async () => {
  fetchTimesheets()
  await projectStore.fetchProjects()
  projects.value = projectStore.projects as any
})

const fetchTimesheets = async () => {
  const res = await request.get('/timesheets')
  timesheets.value = res as any
}

const fetchProjectTasks = async () => {
  if (selectedProjectId.value) {
    tasks.value = await getTasks(selectedProjectId.value) as any
  }
}

const handleSubmit = async () => {
  if (!form.taskId || !form.hours) return
  await request.post(`/timesheets?taskId=${form.taskId}`, form)
  ElMessage.success('Time logged successfully')
  showDialog.value = false
  fetchTimesheets()
}
</script>

<style scoped>
.timesheet-container {
  padding: 20px;
}
.actions {
  margin: 20px 0;
}
</style>
