<template>
  <div class="gantt-container">
    <div class="gantt-controls">
      <el-radio-group v-model="viewMode" size="small" @change="changeViewMode">
        <el-radio-button label="Day">Day</el-radio-button>
        <el-radio-button label="Week">Week</el-radio-button>
        <el-radio-button label="Month">Month</el-radio-button>
      </el-radio-group>
    </div>
    <div id="gantt"></div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useTaskStore } from '@/stores/task.store'
import Gantt from 'frappe-gantt'
import '@/assets/frappe-gantt.css'
import { updateTask } from '@/api/task'
import { ElMessage } from 'element-plus'

// No longer relying on props from parent, fetch data based on route
const route = useRoute()
const taskStore = useTaskStore()
const projectId = Number(route.params.id)

const tasks = computed(() => taskStore.tasks)

let gantt: any = null
const viewMode = ref('Week')

const transformTasks = (tasks: any[]) => {
  return tasks.map(t => ({
    id: t.id.toString(),
    name: t.title,
    start: t.plannedStartDate ? t.plannedStartDate.substring(0, 10) : new Date().toISOString().substring(0, 10),
    end: t.dueDate ? t.dueDate.substring(0, 10) : new Date(new Date().setDate(new Date().getDate() + 1)).toISOString().substring(0, 10),
    progress: t.status === 'DONE' ? 100 : (t.status === 'IN_PROGRESS' ? 50 : 0),
    dependencies: t.dependencies ? t.dependencies.map((d: any) => d.id.toString()).join(',') : '',
    custom_class: getStatusClass(t.status)
  }))
}

const getStatusClass = (status: string) => {
  switch(status) {
    case 'DONE': return 'bar-done'
    case 'IN_PROGRESS': return 'bar-inprogress'
    case 'REVIEW': return 'bar-review'
    case 'CANCELLED': return 'bar-cancelled'
    default: return 'bar-todo'
  }
}

const initGantt = () => {
  if (!tasks.value || tasks.value.length === 0) return
  
  const formattedTasks = transformTasks(tasks.value)
  const ganttContainer = document.getElementById('gantt')
  if(ganttContainer) {
      ganttContainer.innerHTML = '' // Clear previous
      gantt = new Gantt('#gantt', formattedTasks, {
        header_height: 50,
        column_width: 30,
        step: 24,
        view_modes: ['Quarter Day', 'Half Day', 'Day', 'Week', 'Month'],
        bar_height: 20,
        bar_corner_radius: 3,
        arrow_curve: 5,
        padding: 18,
        view_mode: viewMode.value,
        date_format: 'YYYY-MM-DD',
        custom_popup_html: function(task: any) {
          return `
            <div class="details-container">
              <h5>${task.name}</h5>
              <p>Start: ${task.start}</p>
              <p>End: ${task.end}</p>
              <p>Progress: ${task.progress}%</p>
            </div>
          `;
        },
        on_date_change: async (task: any, start: Date, end: Date) => {
            try {
                await updateTask(Number(task.id), {
                    plannedStartDate: start.toISOString(),
                    dueDate: end.toISOString()
                })
                ElMessage.success('Task timeline updated')
                taskStore.fetchTasks(projectId) // Refresh store
            } catch (error: any) {
                ElMessage.error(error.response?.data?.message || 'Failed to update task')
                initGantt() 
            }
        },
        on_click: (task: any) => {
            console.log(task)
        },
        on_view_change: (mode: string) => {
            console.log(mode)
        }
      })
  }
}

watch(tasks, () => {
  initGantt()
}, { deep: true })

const changeViewMode = (mode: string) => {
  if (gantt) {
    gantt.change_view_mode(mode)
  }
}

onMounted(async () => {
  await taskStore.fetchTasks(projectId)
  initGantt()
})
</script>

<style>
.gantt-container {
  margin-top: 20px;
  overflow-x: auto;
}
.gantt-controls {
  margin-bottom: 10px;
  display: flex;
  justify-content: flex-end;
}
/* Custom Gantt Styles */
.bar-done .bar { fill: #67c23a !important; }
.bar-inprogress .bar { fill: #409eff !important; }
.bar-review .bar { fill: #e6a23c !important; }
.bar-cancelled .bar { fill: #f56c6c !important; }
.bar-todo .bar { fill: #909399 !important; }

.details-container {
    padding: 10px;
    background: #fff;
    border: 1px solid #eee;
    border-radius: 4px;
}
</style>
