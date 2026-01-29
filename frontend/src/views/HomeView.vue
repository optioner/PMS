<template>
  <div class="home">
    <el-container>
      <el-header class="header">
        <div class="logo">PMS</div>
        <div class="user-info">
          <language-switcher class="mr-4" />
          <notification-bell />
          <span class="ml-2">{{ authStore.user?.email }}</span>
          <el-button type="text" @click="$router.push('/users')">{{ t('nav.users') }}</el-button>
          <el-button type="text" @click="$router.push('/timesheets')">{{ t('nav.timesheets') }}</el-button>
          <el-button type="text" @click="logout">{{ t('common.logout') }}</el-button>
        </div>
      </el-header>
      <el-main>
        <dashboard-widgets />
        <div class="actions">
          <h2>{{ activeTab === 'my' ? t('dashboard.myProjects') : t('dashboard.allProjects') }}</h2>
          <div>
            <el-radio-group v-model="activeTab" class="mr-4">
              <el-radio-button label="my">{{ t('dashboard.myProjects') }}</el-radio-button>
              <el-radio-button label="all">{{ t('dashboard.allProjects') }}</el-radio-button>
            </el-radio-group>
            <el-button type="primary" @click="showCreateDialog = true">{{ t('dashboard.newProject') }}</el-button>
          </div>
        </div>
        
        <el-row :gutter="20" v-loading="loading">
          <el-col :span="6" v-for="project in displayedProjects" :key="project.id">
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
        <el-dialog v-model="showCreateDialog" :title="t('project.createTitle')" width="30%">
          <el-form :model="form" label-width="100px">
            <el-form-item :label="t('project.name')">
              <el-input v-model="form.name" />
            </el-form-item>
            <el-form-item :label="t('project.description')">
              <el-input v-model="form.description" type="textarea" />
            </el-form-item>
          </el-form>
          <template #footer>
            <span class="dialog-footer">
              <el-button @click="showCreateDialog = false">{{ t('common.cancel') }}</el-button>
              <el-button type="primary" @click="handleCreate">{{ t('common.create') }}</el-button>
            </span>
          </template>
        </el-dialog>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, reactive, computed, watch } from 'vue'
import { useAuthStore } from '@/stores/auth.store'
import { useProjectStore } from '@/stores/project.store'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import DashboardWidgets from '@/components/DashboardWidgets.vue'
import NotificationBell from '@/components/NotificationBell.vue'
import LanguageSwitcher from '@/components/LanguageSwitcher.vue'

const { t } = useI18n()
const authStore = useAuthStore()
const projectStore = useProjectStore()
const router = useRouter()

const showCreateDialog = ref(false)
const activeTab = ref('my')
const loading = ref(false)

const displayedProjects = computed(() => {
  return activeTab.value === 'my' ? projectStore.projects : projectStore.allProjects
})

watch(activeTab, async (val) => {
  loading.value = true
  try {
    if (val === 'all' && projectStore.allProjects.length === 0) {
      await projectStore.fetchAllProjects()
    } else if (val === 'my' && projectStore.projects.length === 0) {
      await projectStore.fetchProjects()
    }
  } catch (error) {
    ElMessage.error(t('common.error'))
  } finally {
    loading.value = false
  }
})

const form = reactive({
  name: '',
  description: ''
})

onMounted(async () => {
  loading.value = true
  await projectStore.fetchProjects()
  loading.value = false
})

const logout = () => {
  authStore.logout()
  router.push('/login')
}

const handleCreate = async () => {
  if (!form.name) {
    ElMessage.warning(t('common.warning'))
    return
  }
  await projectStore.createProject(form)
  showCreateDialog.value = false
  form.name = ''
  form.description = ''
  ElMessage.success(t('project.created'))
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm(t('project.deleteConfirm'), t('common.warning'), {
      confirmButtonText: t('common.yes'),
      cancelButtonText: t('common.no'),
      type: 'warning'
    })
    await projectStore.removeProject(id)
    ElMessage.success(t('project.deleted'))
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
.mr-4 {
  margin-right: 16px;
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
