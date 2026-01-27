import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  timeout: 15000 // Increased timeout to 15s
})

// Request interceptor
service.interceptors.request.use(
  (config) => {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    if (user.token) {
      config.headers['Authorization'] = 'Bearer ' + user.token
    }
    return config
  },
  (error) => {
    console.log(error)
    return Promise.reject(error)
  }
)

// Response interceptor
service.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    console.log('err' + error)
    ElMessage({
      message: error.response?.data?.message || error.message,
      type: 'error',
      duration: 5 * 1000
    })
    return Promise.reject(error)
  }
)

export default service
