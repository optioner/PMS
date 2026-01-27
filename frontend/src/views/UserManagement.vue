<template>
  <div class="user-mgmt-container">
    <el-page-header @back="$router.push('/')">
      <template #content>User Management</template>
      <template #extra>
        <el-button type="primary" @click="showCreateDialog = true">Create User</el-button>
      </template>
    </el-page-header>
    
    <el-table :data="users" style="width: 100%; margin-top: 20px" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="fullName" label="Name" />
      <el-table-column prop="email" label="Email" />
      <el-table-column prop="position" label="Position" />
      <el-table-column label="Roles">
        <template #default="scope">
          <el-tag v-for="role in scope.row.roles" :key="role.id" size="small" class="mr-1">
            {{ role.name }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Status" width="100">
        <template #default="scope">
          <el-switch
            v-model="scope.row.active"
            @change="handleStatusChange(scope.row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="Actions" width="150">
        <template #default="scope">
          <el-button size="small" @click="openEditDialog(scope.row)">Edit</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showCreateDialog" title="Create New User">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="Email">
          <el-input v-model="createForm.email" />
        </el-form-item>
        <el-form-item label="Full Name">
          <el-input v-model="createForm.fullName" />
        </el-form-item>
        <el-form-item label="Password">
          <el-input v-model="createForm.password" type="password" />
        </el-form-item>
        <el-form-item label="Position">
          <el-input v-model="createForm.position" />
        </el-form-item>
        <el-form-item label="Roles">
          <el-checkbox-group v-model="createForm.roles">
            <el-checkbox label="admin">Admin</el-checkbox>
            <el-checkbox label="project_manager">Project Manager</el-checkbox>
            <el-checkbox label="team_member">Team Member</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateDialog = false">Cancel</el-button>
          <el-button type="primary" @click="handleCreateUser">Create</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="showDialog" title="Edit User">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="Name">
          <el-input v-model="editForm.fullName" />
        </el-form-item>
        <el-form-item label="Position">
          <el-input v-model="editForm.position" />
        </el-form-item>
        <el-form-item label="Roles">
          <el-checkbox-group v-model="selectedRoles">
            <el-checkbox label="admin">Admin</el-checkbox>
            <el-checkbox label="project_manager">Project Manager</el-checkbox>
            <el-checkbox label="team_member">Team Member</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showDialog = false">Cancel</el-button>
          <el-button type="primary" @click="saveUser">Save</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { getUsers, updateUser, createUser } from '@/api/user'
import { ElMessage } from 'element-plus'

const users = ref<any[]>([])
const loading = ref(false)
const showDialog = ref(false)
const showCreateDialog = ref(false)
const selectedRoles = ref<string[]>([])

const createForm = reactive({
  email: '',
  fullName: '',
  password: '',
  position: '',
  roles: [] as string[]
})

const editForm = reactive({
  id: 0,
  fullName: '',
  position: '',
  active: true
})

onMounted(async () => {
  fetchUsers()
})

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await getUsers()
    users.value = res as any
  } finally {
    loading.value = false
  }
}

const handleStatusChange = async (user: any) => {
  try {
    await updateUser(user.id, { ...user, active: user.active })
    ElMessage.success('Status updated')
  } catch (e) {
    user.active = !user.active // revert
  }
}

const openEditDialog = (user: any) => {
  editForm.id = user.id
  editForm.fullName = user.fullName
  editForm.position = user.position
  editForm.active = user.active
  selectedRoles.value = user.roles.map((r: any) => r.name)
  showDialog.value = true
}

const handleCreateUser = async () => {
  if (!createForm.email || !createForm.password || !createForm.fullName) {
    ElMessage.warning('Please fill in required fields')
    return
  }
  const roles = createForm.roles.map(name => ({ name }))
  try {
    await createUser({ ...createForm, roles })
    ElMessage.success('User created')
    showCreateDialog.value = false
    createForm.email = ''
    createForm.fullName = ''
    createForm.password = ''
    createForm.position = ''
    createForm.roles = []
    fetchUsers()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || 'Create failed')
  }
}

const saveUser = async () => {
  const roles = selectedRoles.value.map(name => ({ name }))
  try {
    await updateUser(editForm.id, {
      ...editForm,
      roles
    })
    ElMessage.success('User updated')
    showDialog.value = false
    fetchUsers()
  } catch (e) {
    // ignore
  }
}
</script>

<style scoped>
.user-mgmt-container {
  padding: 20px;
}
.mr-1 {
  margin-right: 4px;
}
</style>
