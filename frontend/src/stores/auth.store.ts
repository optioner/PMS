import { defineStore } from 'pinia'
import { login, register } from '@/api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: JSON.parse(localStorage.getItem('user') || 'null')
  }),
  actions: {
    async login(userInfo: any) {
      const res: any = await login(userInfo)
      if (res) { // Assuming response is the user object/token directly or wrapped
        this.user = res
        localStorage.setItem('user', JSON.stringify(res))
      }
      return res
    },
    async register(userInfo: any) {
      return await register(userInfo)
    },
    logout() {
      this.user = null
      localStorage.removeItem('user')
    }
  }
})
