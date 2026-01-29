import request from '@/utils/request'

export const getAllTags = () => {
  return request({
    url: '/tags',
    method: 'get'
  })
}

export const createTag = (data: any) => {
  return request({
    url: '/tags',
    method: 'post',
    data
  })
}
