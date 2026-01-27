<template>
  <div class="board-container">
    <el-page-header @back="$router.push('/')">
      <template #content>
        <span class="text-large font-600 mr-3"> {{ projectStore.currentProject?.name }} </span>
      </template>
      <template #extra>
        <el-upload
          class="import-upload"
          :action="importUrl"
          :show-file-list="false"
          :headers="uploadHeadersSafe"
          :on-success="handleImportSuccess"
          :on-error="handleImportError"
          accept=".csv"
        >
          <el-button>Import CSV</el-button>
        </el-upload>
        <el-button @click="$router.push(`/projects/${projectId}/report`)">Report</el-button>
        <el-button type="primary" @click="showTaskDialog = true">New Task</el-button>
      </template>
    </el-page-header>

    <div class="kanban-board">
      <div class="kanban-column" v-for="status in ['TODO', 'IN_PROGRESS', 'REVIEW', 'DONE', 'CANCELLED']" :key="status">
        <div class="column-header" :class="status.toLowerCase()">
          {{ formatStatus(status) }}
        </div>
        <draggable
          class="list-group"
          :list="getTasksByStatus(status)"
          group="tasks"
          item-key="id"
          @change="(event) => onTaskMove(event, status)"
        >
          <template #item="{ element }">
            <el-card class="task-card" shadow="hover" @click="openTaskDetail(element)">
              <div class="task-title">{{ element.title }}</div>
              <div class="task-desc">{{ element.description }}</div>
              <div class="task-meta" v-if="element.storyPoints">
                <el-tag size="small" type="info" effect="plain">{{ element.storyPoints }} SP</el-tag>
              </div>
              <div class="task-dates" v-if="element.dueDate">
                <el-icon><Calendar /></el-icon> {{ new Date(element.dueDate).toLocaleDateString() }}
              </div>
              <div class="task-footer">
                <el-avatar v-if="element.assignee" size="small" :src="element.assignee.avatarUrl">{{ element.assignee.fullName.charAt(0) }}</el-avatar>
                <el-tag size="small" :type="getPriorityType(element.priority)">{{ element.priority }}</el-tag>
                <el-button type="danger" icon="Delete" circle size="small" text @click.stop="handleDeleteTask(element.id)"></el-button>
              </div>
            </el-card>
          </template>
        </draggable>
      </div>
    </div>

    <!-- Task Detail Dialog -->
    <task-detail-dialog
      v-if="showDetailDialog"
      v-model="showDetailDialog"
      :task-id="selectedTaskId"
      :project-id="projectId"
      @refresh="onTaskUpdated"
    />

    <!-- Create Task Dialog -->
    <el-dialog v-model="showTaskDialog" title="Create New Task" width="30%">
      <el-form :model="taskForm" label-width="100px">
        <el-form-item label="Title">
          <el-input v-model="taskForm.title" />
        </el-form-item>
        <el-form-item label="Description">
          <el-input v-model="taskForm.description" type="textarea" />
        </el-form-item>
        <el-form-item label="Planned Start">
          <el-date-picker v-model="taskForm.plannedStartDate" type="datetime" placeholder="Select date and time" />
        </el-form-item>
        <el-form-item label="Due Date">
          <el-date-picker v-model="taskForm.dueDate" type="datetime" placeholder="Select date and time" />
        </el-form-item>
        <el-form-item label="Assignee">
          <el-select v-model="taskForm.assigneeId" placeholder="Select Assignee">
            <el-option
              v-for="member in taskStore.members"
              :key="member.user.id"
              :label="member.user.fullName"
              :value="member.user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="Priority">
          <el-select v-model="taskForm.priority">
            <el-option label="Low" value="LOW" />
            <el-option label="Medium" value="MEDIUM" />
            <el-option label="High" value="HIGH" />
            <el-option label="Urgent" value="URGENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="Story Points">
          <el-input-number v-model="taskForm.storyPoints" :precision="1" :step="0.5" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showTaskDialog = false">Cancel</el-button>
          <el-button type="primary" @click="handleCreateTask">Create</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, reactive, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useProjectStore } from '@/stores/project.store'
import { useTaskStore } from '@/stores/task.store'
import { useAuthStore } from '@/stores/auth.store'
import draggable from 'vuedraggable'
import { ElMessage, ElMessageBox } from 'element-plus'

import TaskDetailDialog from '@/components/TaskDetailDialog.vue'

const route = useRoute()
const projectStore = useProjectStore()
const taskStore = useTaskStore()
const authStore = useAuthStore()
const projectId = Number(route.params.id)

const importUrl = computed(() => `/api/v1/projects/${projectId}/import`)
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${authStore.user?.token || ''}` // Assuming token is stored in user object or similar
  // Adjust based on your actual auth store implementation. Usually token is in localStorage or authStore.token
}))

// Fix headers if token is not directly in user object
if (!authStore.user && localStorage.getItem('user')) {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    if(user.token) {
        // Manually setting for this scope if store isn't ready
    }
}
// Actually let's just get token from localStorage if simple
const getToken = () => {
    const userStr = localStorage.getItem('user');
    if (userStr) {
        const user = JSON.parse(userStr);
        return user.token;
    }
    return '';
}

const finalUploadHeaders = computed(() => ({
    Authorization: `Bearer ${getToken()}`
}))

// Override previous computed to use the safe one
const uploadHeadersSafe = finalUploadHeaders

const handleImportSuccess = (response: any) => {
  ElMessage.success(response.message || 'Import successful')
  taskStore.fetchTasks(projectId)
}

const handleImportError = (error: any) => {
  ElMessage.error('Import failed: ' + (error.response?.data?.message || 'Unknown error'))
}

const showTaskDialog = ref(false)
const showDetailDialog = ref(false)
const selectedTaskId = ref<number | undefined>(undefined)

const taskForm = reactive({
  title: '',
  description: '',
  priority: 'MEDIUM',
  status: 'TODO',
  storyPoints: 0,
  plannedStartDate: '',
  dueDate: '',
  assigneeId: null
})

onMounted(async () => {
  await projectStore.fetchProject(projectId)
  await taskStore.fetchTasks(projectId)
  await taskStore.fetchMembers(projectId)
})

const getTasksByStatus = (status: string) => {
  return taskStore.tasks.filter((t: any) => t.status === status)
}

const formatStatus = (status: string) => {
  return status.replace('_', ' ')
}

const getPriorityType = (priority: string) => {
  switch (priority) {
    case 'URGENT': return 'danger'
    case 'HIGH': return 'warning' // Element plus doesn't have orange for warning, usually warning is yellow/orange. Danger is red.
    case 'MEDIUM': return 'primary'
    case 'LOW': return 'info'
    default: return ''
  }
}

const onTaskMove = (event: any, newStatus: string) => {
  if (event.added) {
    const task = event.added.element
    taskStore.updateTask(task.id, { ...task, status: newStatus })
  }
}

const openTaskDetail = (task: any) => {
  selectedTaskId.value = task.id
  showDetailDialog.value = true
}

const onTaskUpdated = () => {
  taskStore.fetchTasks(projectId)
}

const handleCreateTask = async () => {
  if (!taskForm.title) return
  const payload = {
    ...taskForm,
    assignee: taskForm.assigneeId ? { id: taskForm.assigneeId } : null
  }
  await taskStore.createTask(projectId, payload)
  showTaskDialog.value = false
  taskForm.title = ''
  taskForm.description = ''
  ElMessage.success('Task created')
}

const handleDeleteTask = async (id: number) => {
    try {
    await ElMessageBox.confirm('Are you sure to delete this task?', 'Warning', {
      confirmButtonText: 'Yes',
      cancelButtonText: 'No',
      type: 'warning'
    })
    await taskStore.removeTask(id)
    ElMessage.success('Task deleted')
  } catch (e) {
    // cancelled
  }
}
</script>

<style scoped>
.import-upload {
  display: inline-block;
  margin-right: 10px;
}
.board-container {
  padding: 20px;
  height: 100vh;
  box-sizing: border-box;
}
.kanban-board {
  display: flex;
  gap: 20px;
  margin-top: 20px;
  height: calc(100vh - 100px);
}
.kanban-column {
  flex: 1;
  background-color: #f4f5f7;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  padding: 10px;
}
.column-header {
  font-weight: bold;
  padding: 10px;
  margin-bottom: 10px;
  border-radius: 4px;
  color: white;
}
.column-header.todo { background-color: #909399; }
.column-header.in_progress { background-color: #409eff; }
.column-header.review { background-color: #e6a23c; }
.column-header.done { background-color: #67c23a; }
.column-header.cancelled { background-color: #f56c6c; }

.list-group {
  flex: 1;
  overflow-y: auto;
  min-height: 100px;
}
.task-card {
  margin-bottom: 10px;
  cursor: grab;
}
.task-title {
  font-weight: bold;
  margin-bottom: 5px;
}
.task-desc {
  font-size: 12px;
  color: #666;
  margin-bottom: 10px;
}
.task-dates {
  font-size: 11px;
  color: #888;
  margin-bottom: 5px;
  display: flex;
  align-items: center;
  gap: 4px;
}
.task-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
