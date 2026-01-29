<template>
  <el-dialog v-model="visible" :title="t('project.members')" width="50%" @close="closeDialog">
    <div class="mb-4">
      <el-select v-model="selectedUserId" filterable :placeholder="t('project.selectUser')" class="mr-2" style="width: 200px" value-key="id">
        <el-option
          v-for="user in availableUsers"
          :key="user.id"
          :label="user.fullName + ' (' + user.email + ')'"
          :value="user.id"
        />
      </el-select>
      <el-select v-model="selectedRole" :placeholder="t('project.selectRole')" class="mr-2" style="width: 150px">
        <el-option label="Member" value="MEMBER" />
        <el-option label="Manager" value="MANAGER" />
        <el-option label="Viewer" value="VIEWER" />
      </el-select>
      <el-button type="primary" @click="handleAddMember" :disabled="!selectedUserId">{{ t('common.add') }}</el-button>
    </div>

    <el-table :data="members" style="width: 100%">
      <el-table-column prop="user.fullName" :label="t('user.name')" />
      <el-table-column prop="user.email" :label="t('common.email')" />
      <el-table-column prop="role" :label="t('user.roles')" />
      <el-table-column :label="t('common.actions')" width="100">
        <template #default="scope">
          <el-button 
            type="danger" 
            icon="Delete" 
            circle 
            size="small" 
            @click="handleRemoveMember(scope.row.user.id)"
            :disabled="scope.row.role === 'MANAGER' && members.filter(m => m.role === 'MANAGER').length === 1"
          />
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useTaskStore } from '@/stores/task.store'
import { getUsers } from '@/api/user'
import { addProjectMember, removeProjectMember } from '@/api/project'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'

const props = defineProps<{
  modelValue: boolean
  projectId: number
}>()

const emit = defineEmits(['update:modelValue', 'refresh'])
const { t } = useI18n()
const taskStore = useTaskStore()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const allUsers = ref<any[]>([])
const selectedUserId = ref<number | null>(null)
const selectedRole = ref('MEMBER')

const members = computed(() => taskStore.members)

const availableUsers = computed(() => {
  return allUsers.value.filter(u => !members.value.find((m: any) => m.user.id === u.id))
})

const fetchData = async () => {
  if (props.projectId) {
    await taskStore.fetchMembers(props.projectId)
    const usersRes = await getUsers()
    allUsers.value = usersRes as any
  }
}

watch(() => props.modelValue, (val) => {
  if (val) {
    fetchData()
  }
})

const handleAddMember = async () => {
  if (!selectedUserId.value) return
  try {
    await addProjectMember(props.projectId, selectedUserId.value, selectedRole.value)
    ElMessage.success(t('common.success'))
    selectedUserId.value = null
    await taskStore.fetchMembers(props.projectId)
    emit('refresh')
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || t('common.error'))
  }
}

const handleRemoveMember = async (userId: number) => {
  try {
    await removeProjectMember(props.projectId, userId)
    ElMessage.success(t('common.success'))
    await taskStore.fetchMembers(props.projectId)
    emit('refresh')
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || t('common.error'))
  }
}
</script>

<style scoped>
.mb-4 { margin-bottom: 16px; }
.mr-2 { margin-right: 8px; }
</style>
