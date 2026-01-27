import request from '@/utils/request'

export const getUsers = () => {
  return request({
    url: '/users',
    method: 'get'
  })
}

export const createUser = (data: any) => {
  return request({
    url: '/users',
    method: 'post',
    data
  })
}

export const updateUser = (id: number, data: any) => {
  return request({
    url: `/users/${id}`,
    method: 'put',
    data
  })
}
