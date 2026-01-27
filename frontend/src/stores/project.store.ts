import { defineStore } from 'pinia'
import { getProjects, createProject, deleteProject, getProject } from '@/api/project'

export const useProjectStore = defineStore('project', {
  state: () => ({
    projects: [] as any[],
    currentProject: null as any
  }),
  actions: {
    async fetchProjects() {
      this.projects = await getProjects() as any
    },
    async createProject(project: any) {
      const newProject = await createProject(project)
      this.projects.push(newProject)
    },
    async fetchProject(id: number) {
      this.currentProject = await getProject(id)
    },
    async removeProject(id: number) {
      await deleteProject(id)
      this.projects = this.projects.filter(p => p.id !== id)
    }
  }
})
