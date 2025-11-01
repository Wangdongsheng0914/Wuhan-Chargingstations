import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import MapContainer from '../views/MapContainer.vue'
import UserInfor from '../views/UserInfor.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/map',
    name: 'MapContainer',
    component: MapContainer
  },
  {
    path: '/userinfor',
    name: 'UserInfor',
    component: UserInfor
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router

