<template>
  <div class="home">
    <el-container>
      <el-header class="header">
        <div class="logo">PMS</div>
        <div class="user-info">
          <notification-bell />
          <span class="ml-2">{{ authStore.user?.email }}</span>
          <el-button type="text" @click="$router.push('/users')">Users</el-button>
          <el-button type="text" @click="$router.push('/timesheets')">Timesheets</el-button>
          <el-button type="text" @click="logout">Logout</el-button>
        </div>
      </el-header>
      <el-main>
        <dashboard-widgets />
        <div class="actions">
          <h2>My Projects</h2>
          <el-button type="primary" @click="showCreateDialog = true">New Project</el-button>
        </div>
        
        <el-row :gutter="20">
          <el-col :span="6" v-for="project in projectStore.projects" :key="project.id">
            <el-card class="project-card" shadow="hover" @click="openProject(project.id)">
              <template #header>
                <div class="card-header">
                  <span>{{ project.name }}</span>
                  <el-button type="danger" icon="Delete" circle size="small" @click.stop="handleDelete(project.id)" />
                </div>
              </template>
              <p>{{ project.description || 'No description' }}</p>
            </el-card>
          </el-col>
        </el-row>

        <!-- Create Project Dialog -->
        <el-dialog v-model="showCreateDialog" title="Create New Project" width="30%">
          <el-form :model="form" label-width="100px">
            <el-form-item label="Name">
              <el-input v-model="form.name" />
            </el-form-item>
            <el-form-item label="Description">
              <el-input v-model="form.description" type="textarea" />
            </el-form-item>
          </el-form>
          <template #footer>
            <span class="dialog-footer">
              <el-button @click="showCreateDialog = false">Cancel</el-button>
              <el-button type="primary" @click="handleCreate">Create</el-button>
            </span>
          </template>
        </el-dialog>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, reactive } from 'vue'
import { useAuthStore } from '@/stores/auth.store'
import { useProjectStore } from '@/stores/project.store'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import DashboardWidgets from '@/components/DashboardWidgets.vue'
import NotificationBell from '@/components/NotificationBell.vue'

const authStore = useAuthStore()
const projectStore = useProjectStore()
const router = useRouter()

const showCreateDialog = ref(false)
const form = reactive({
  name: '',
  description: ''
})

onMounted(() => {
  projectStore.fetchProjects()
})

const logout = () => {
  authStore.logout()
  router.push('/login')
}

const handleCreate = async () => {
  if (!form.name) {
    ElMessage.warning('Project name is required')
    return
  }
  await projectStore.createProject(form)
  showCreateDialog.value = false
  form.name = ''
  form.description = ''
  ElMessage.success('Project created')
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('Are you sure to delete this project?', 'Warning', {
      confirmButtonText: 'Yes',
      cancelButtonText: 'No',
      type: 'warning'
    })
    await projectStore.removeProject(id)
    ElMessage.success('Project deleted')
  } catch (e) {
    // cancelled
  }
}

const openProject = (id: number) => {
  router.push(`/projects/${id}/board`)
}
</script>

<style scoped>
.ml-2 {
  margin-left: 10px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #dcdfe6;
  padding: 0 20px;
}
.actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.project-card {
  cursor: pointer;
  margin-bottom: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
