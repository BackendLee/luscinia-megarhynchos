import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'index',
      component: () => import('../views/IndexView.vue')
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue')
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue')
    },
    {
      path: '/user',
      name: 'user',
      component: () => import('../views/UserView.vue')
    }
  ]
})

router.beforeEach(async (to, from) => {
  if (localStorage.getItem('user')) {
    if (to.name === 'index') {
      return true
    } else if (to.name === 'user') {
      return true
    } else {
      return { name: 'user' }
    }
  } else {
    if (to.name === 'index') {
      return true
    } else if (to.name === 'login') {
      return true
    } else if (to.name === 'register') {
      return true
    } else {
      return { name: 'login' }
    }
  }
})

export default router
