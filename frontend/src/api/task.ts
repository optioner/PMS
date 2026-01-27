import request from '@/utils/request'

export const getTasks = (projectId: number) => {
  return request({
    url: `/projects/${projectId}/tasks`,
    method: 'get'
  })
}

export const createTask = (projectId: number, data: any) => {
  return request({
    url: `/projects/${projectId}/tasks`,
    method: 'post',
    data
  })
}

export const updateTask = (id: number, data: any) => {
  return request({
    url: `/tasks/${id}`,
    method: 'put',
    data
  })
}

export const deleteTask = (id: number) => {
  return request({
    url: `/tasks/${id}`,
    method: 'delete'
  })
}

export const getProjectMembers = (projectId: number) => {
  return request({
    url: `/projects/${projectId}/members`,
    method: 'get'
  })
}
