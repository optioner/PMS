import { defineStore } from 'pinia'
import { getTasks, createTask, updateTask, deleteTask, getProjectMembers } from '@/api/task'

export const useTaskStore = defineStore('task', {
  state: () => ({
    tasks: [] as any[],
    members: [] as any[]
  }),
  actions: {
    async fetchTasks(projectId: number) {
      this.tasks = await getTasks(projectId) as any
    },
    async createTask(projectId: number, task: any) {
      const newTask = await createTask(projectId, task)
      this.tasks.push(newTask)
    },
    async updateTask(id: number, task: any) {
      const updatedTask: any = await updateTask(id, task)
      const index = this.tasks.findIndex(t => t.id === id)
      if (index !== -1) {
        this.tasks[index] = updatedTask
      }
    },
    async removeTask(id: number) {
      await deleteTask(id)
      this.tasks = this.tasks.filter(t => t.id !== id)
    },
    async fetchMembers(projectId: number) {
      this.members = await getProjectMembers(projectId) as any
    }
  }
})
