import axios from 'axios'

// 创建 axios 实例（复用auth.js的配置）
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 30000, // 增加超时时间到30秒，因为服务端需要查询数据库
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

/**
 * 获取推荐的充电站
 * @param {Object} params - 推荐参数
 * @param {number} params.lat - 纬度
 * @param {number} params.lng - 经度
 * @param {string} params.priority - 优先级：distance(距离优先), power(功率优先), available(可用数量优先)
 * @param {number} params.maxDistance - 最大搜索半径（公里），默认50
 * @param {number} params.limit - 返回结果数量，默认20
 * @param {number} params.minPower - 最小功率要求（可选）
 * @param {number} params.minAvailable - 最小可用充电桩数量（可选）
 * @param {string} params.chargingType - 充电类型过滤（可选）
 */
export const recommendStations = (params) => {
  return api.post('/stations/recommend', params)
}
