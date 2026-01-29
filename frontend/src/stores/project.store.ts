import { defineStore } from 'pinia'
import { getProjects, getAllProjects, createProject, deleteProject, getProject } from '@/api/project'

export const useProjectStore = defineStore('project', {
  state: () => ({
    projects: [] as any[],
    allProjects: [] as any[],
    currentProject: null as any
  }),
  actions: {
    async fetchProjects() {
      this.projects = await getProjects() as any
    },
    async fetchAllProjects() {
      this.allProjects = await getAllProjects() as any
    },
    async createProject(project: any) {
      const newProject = await createProject(project)
      this.projects.push(newProject)
      this.allProjects.push(newProject)
    },
    async fetchProject(id: number) {
      this.currentProject = await getProject(id)
    },
    async removeProject(id: number) {
      await deleteProject(id)
      this.projects = this.projects.filter(p => p.id !== id)
      this.allProjects = this.allProjects.filter(p => p.id !== id)
    }
  }
})
