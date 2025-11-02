import axios from 'axios'

// 创建 axios 实例
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 响应拦截器
api.interceptors.response.use(
  response => response.data,
  error => {
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

// 登录接口
export const login = (username, password) => {
  return api.post('/auth/login', { username, password })
}

// 注册接口
export const register = (username, password, email) => {
  return api.post('/auth/register', { username, password, email })
}

// 测试接口
export const testConnection = () => {
  return api.get('/auth/test')
}

export const updateUserCarModel = (username, carModel) => {
  return axios.put('http://localhost:8080/api/auth/user/car', {
    username,
    carModel
  });
};

