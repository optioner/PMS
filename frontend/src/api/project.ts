import request from '@/utils/request'

export const getProjects = () => {
  return request({
    url: '/projects',
    method: 'get'
  })
}

export const getAllProjects = () => {
  return request({
    url: '/projects/all',
    method: 'get'
  })
}

export const createProject = (data: any) => {
  return request({
    url: '/projects',
    method: 'post',
    data
  })
}

export const addProjectMember = (projectId: number, userId: number, role: string) => {
  return request({
    url: `/projects/${projectId}/members`,
    method: 'post',
    data: { userId, role }
  })
}

export const removeProjectMember = (projectId: number, userId: number) => {
  return request({
    url: `/projects/${projectId}/members/${userId}`,
    method: 'delete'
  })
}

export const getProject = (id: number) => {
  return request({
    url: `/projects/${id}`,
    method: 'get'
  })
}

export const deleteProject = (id: number) => {
  return request({
    url: `/projects/${id}`,
    method: 'delete'
  })
}
