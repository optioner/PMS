<template>
  <el-dialog v-model="visible" title="Task Details" width="50%" @close="closeDialog">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="General" name="general">
        <el-form :model="form" label-width="120px">
          <el-form-item label="Title">
            <el-input v-model="form.title" />
          </el-form-item>
          <el-form-item label="Description">
            <el-input v-model="form.description" type="textarea" :rows="4" />
          </el-form-item>
          <el-form-item label="Status">
            <el-select v-model="form.status">
              <el-option v-for="s in ['TODO', 'IN_PROGRESS', 'REVIEW', 'DONE', 'CANCELLED']" :key="s" :label="s" :value="s" />
            </el-select>
          </el-form-item>
          <el-form-item label="Priority">
            <el-select v-model="form.priority">
              <el-option v-for="p in ['LOW', 'MEDIUM', 'HIGH', 'URGENT']" :key="p" :label="p" :value="p" />
            </el-select>
          </el-form-item>
          <el-form-item label="Assignee">
            <el-select v-model="form.assigneeId" placeholder="Select Assignee" value-key="id">
               <el-option
                v-for="member in members"
                :key="member.user.id"
                :label="member.user.fullName"
                :value="member.user.id"
              />
            </el-select>
          </el-form-item>
           <el-form-item label="Planned Start">
            <el-date-picker v-model="form.plannedStartDate" type="datetime" />
          </el-form-item>
          <el-form-item label="Due Date">
            <el-date-picker v-model="form.dueDate" type="datetime" />
          </el-form-item>
          <el-form-item label="Story Points">
            <el-input-number v-model="form.storyPoints" :step="0.5" />
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="Subtasks" name="subtasks">
        <div class="mb-2">
           <el-button size="small" type="primary" @click="addSubtask">Add Subtask</el-button>
        </div>
        <el-table :data="subtasks" border>
          <el-table-column prop="title" label="Title" />
          <el-table-column prop="status" label="Status" width="100" />
           <el-table-column label="Action" width="80">
            <template #default="scope">
               <el-button type="danger" icon="Delete" circle size="small" @click="removeSubtask(scope.row.id)" />
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="Dependencies" name="dependencies">
         <div class="mb-2">
           <el-select v-model="newDependencyId" placeholder="Add dependency task" style="width: 200px" class="mr-2">
              <el-option v-for="t in availableTasks" :key="t.id" :label="t.title" :value="t.id" />
           </el-select>
           <el-button type="primary" @click="addDependency">Add</el-button>
         </div>
         <el-list>
           <el-list-item v-for="dep in dependencies" :key="dep.id">
             {{ dep.title }} - {{ dep.status }}
             <el-button type="text" style="float: right" @click="removeDependency(dep.id)">Remove</el-button>
           </el-list-item>
         </el-list>
      </el-tab-pane>
    </el-tabs>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="closeDialog">Cancel</el-button>
        <el-button type="primary" @click="saveTask">Save</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue'
import { useTaskStore } from '@/stores/task.store'
import { createTask } from '@/api/task'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  modelValue: boolean
  taskId?: number
  projectId: number
}>()

const emit = defineEmits(['update:modelValue', 'refresh'])

const taskStore = useTaskStore()
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const activeTab = ref('general')
const members = computed(() => taskStore.members)
const allTasks = computed(() => taskStore.tasks)

// Filter out current task and existing dependencies from dropdown
const availableTasks = computed(() => {
  return allTasks.value.filter(t => t.id !== props.taskId && !dependencies.value.find(d => d.id === t.id))
})

const form = reactive({
  title: '',
  description: '',
  status: 'TODO',
  priority: 'MEDIUM',
  assigneeId: null,
  plannedStartDate: '',
  dueDate: '',
  storyPoints: 0
})

const subtasks = ref<any[]>([])
const dependencies = ref<any[]>([])
const newDependencyId = ref(null)

// Load task data
watch(() => props.taskId, async (newId) => {
  if (newId) {
    const task = allTasks.value.find(t => t.id === newId)
    if (task) {
      // Ensure date format for element-plus date-picker
      const plannedStart = task.plannedStartDate ? new Date(task.plannedStartDate) : ''
      const dueDate = task.dueDate ? new Date(task.dueDate) : ''
      
      Object.assign(form, {
        title: task.title,
        description: task.description,
        status: task.status,
        priority: task.priority,
        assigneeId: task.assignee ? task.assignee.id : null,
        plannedStartDate: plannedStart,
        dueDate: dueDate,
        storyPoints: task.storyPoints
      })
      // Fetch details if not eager loaded (mocking for now, ideally fetch single task API)
      // subtasks.value = task.subTasks || []
      // dependencies.value = task.dependencies || []
    }
  } else {
    // Reset form for new task (if we re-use this dialog for creation)
    Object.assign(form, { title: '', description: '', status: 'TODO', assigneeId: null, plannedStartDate: '', dueDate: '', storyPoints: 0 })
    subtasks.value = []
    dependencies.value = []
  }
}, { immediate: true })

const closeDialog = () => {
  visible.value = false
}

const saveTask = async () => {
  if (props.taskId) {
     const payload = {
        ...form,
        assignee: form.assigneeId ? { id: form.assigneeId } : null,
        // dependencies: dependencies.value.map(d => ({ id: d.id })) // Backend needs to handle this update
     }
     await taskStore.updateTask(props.taskId, payload)
     ElMessage.success('Task updated')
     emit('refresh')
     closeDialog()
  }
}

const addSubtask = async () => {
   // Quick create subtask
   if(!props.taskId) return
   const title = prompt("Subtask title:")
   if(title) {
       // Ideally call create task API with parentTaskId
       const sub = await createTask(props.projectId, { title, parentTask: { id: props.taskId }, status: 'TODO' })
       subtasks.value.push(sub)
       ElMessage.success('Subtask added')
   }
}

const removeSubtask = async (_id: number) => {
    // Call delete API
}

const addDependency = () => {
    if(newDependencyId.value) {
        const task = allTasks.value.find(t => t.id === newDependencyId.value)
        if(task) {
            dependencies.value.push(task)
            newDependencyId.value = null
        }
    }
}

const removeDependency = (id: number) => {
    dependencies.value = dependencies.value.filter(d => d.id !== id)
}

</script>

<style scoped>
.mb-2 { margin-bottom: 8px; }
.mr-2 { margin-right: 8px; }
</style>
