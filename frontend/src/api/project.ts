import request from '@/utils/request'

export const getProjects = () => {
  return request({
    url: '/projects',
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
