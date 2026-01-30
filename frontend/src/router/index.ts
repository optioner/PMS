import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/HomeView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/projects/:id/board',
      name: 'project-board',
      component: () => import('../views/ProjectBoard.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/projects/:id/gantt',
      name: 'project-gantt',
      component: () => import('../views/ProjectGantt.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/timesheets',
      name: 'timesheets',
      component: () => import('../views/TimesheetView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/projects/:id/report',
      name: 'project-report',
      component: () => import('../views/ProjectReport.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/users',
      name: 'user-management',
      component: () => import('../views/UserManagement.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/Login.vue')
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/Register.vue')
    }
  ]
})

router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()
  const loggedIn = authStore.user

  if (to.meta.requiresAuth && !loggedIn) {
    next('/login')
  } else {
    next()
  }
})

export default router
