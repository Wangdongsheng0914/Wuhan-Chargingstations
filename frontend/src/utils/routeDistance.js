/**
 * 使用百度地图JavaScript API进行路径规划，计算两点之间的实际距离
 * 在浏览器端并行处理多个请求，性能更好
 */

/**
 * 计算单个路径的距离
 * @param {Object} origin - 起点坐标 {lat: number, lng: number}
 * @param {Object} destination - 终点坐标 {lat: number, lng: number}
 * @returns {Promise<number|null>} 距离（公里），失败返回null
 */
// 加载百度地图路径规划服务库
function loadRouteService() {
  return new Promise((resolve) => {
    // 检查是否已经加载
    if (typeof BMap !== 'undefined' && typeof BMap.DrivingRoute !== 'undefined') {
      resolve()
      return
    }

    // 检查是否正在加载
    if (window._routeServiceLoading) {
      const checkInterval = setInterval(() => {
        if (typeof BMap !== 'undefined' && typeof BMap.DrivingRoute !== 'undefined') {
          clearInterval(checkInterval)
          resolve()
        }
      }, 100)
      setTimeout(() => {
        clearInterval(checkInterval)
        resolve() // 超时后也继续，使用备用方案
      }, 5000)
      return
    }

    // 开始加载路径规划服务库
    window._routeServiceLoading = true
    const script = document.createElement('script')
    script.src = 'https://api.map.baidu.com/library/DrivingRoute/1.2/src/DrivingRoute_min.js'
    script.onload = () => {
      window._routeServiceLoading = false
      resolve()
    }
    script.onerror = () => {
      window._routeServiceLoading = false
      console.warn('路径规划服务库加载失败，将使用直线距离')
      resolve() // 即使加载失败也继续，使用备用方案
    }
    document.head.appendChild(script)
  })
}

export async function calculateRouteDistance(origin, destination) {
  // 加载路径规划服务
  await loadRouteService()

  return new Promise((resolve) => {
    // 检查百度地图API是否已加载
    if (typeof BMap === 'undefined' || !BMap.DrivingRoute) {
      console.warn('百度地图路径规划服务未加载，使用直线距离')
      resolve(calculateStraightDistance(origin, destination))
      return
    }

    try {
      const driving = new BMap.DrivingRoute(window.map || null, {
        onSearchComplete: (results) => {
          // 使用数字状态码：0表示成功
          if (driving.getStatus() === 0) {
            const plan = results.getPlan(0)
            if (plan) {
              // 获取路径距离（米），转换为公里
              const distance = plan.getDistance(false) / 1000
              resolve(distance)
            } else {
              resolve(calculateStraightDistance(origin, destination))
            }
          } else {
            // 路径规划失败，使用直线距离作为备用
            resolve(calculateStraightDistance(origin, destination))
          }
        }
      })

      const startPoint = new BMap.Point(origin.lng, origin.lat)
      const endPoint = new BMap.Point(destination.lng, destination.lat)
      driving.search(startPoint, endPoint)
      
      // 设置超时，10秒后如果还没返回结果，使用直线距离
      setTimeout(() => {
        if (driving.getStatus() === undefined || driving.getStatus() === -1) {
          resolve(calculateStraightDistance(origin, destination))
        }
      }, 10000)
    } catch (error) {
      console.error('路径规划计算失败:', error)
      resolve(calculateStraightDistance(origin, destination))
    }
  })
}

/**
 * 批量计算多个路径的距离（并行处理）
 * @param {Object} origin - 起点坐标 {lat: number, lng: number}
 * @param {Array<Object>} destinations - 终点坐标数组 [{lat: number, lng: number, id: any}, ...]
 * @param {number} concurrency - 并发数量，默认5
 * @param {Function} onProgress - 进度回调 (completed, total) => {}
 * @returns {Promise<Map>} 返回Map<id, distance>，失败的距离为null
 */
export async function calculateRouteDistances(
  origin,
  destinations,
  concurrency = 5,
  onProgress = null
) {
  const results = new Map()
  let completed = 0
  const total = destinations.length

  // 创建任务队列
  const tasks = destinations.map((dest, index) => ({
    id: dest.id || index,
    destination: dest
  }))

  // 并发控制
  const execute = async (task) => {
    const distance = await calculateRouteDistance(origin, task.destination)
    results.set(task.id, distance)
    completed++
    if (onProgress) {
      onProgress(completed, total)
    }
  }

  // 分批处理
  for (let i = 0; i < tasks.length; i += concurrency) {
    const batch = tasks.slice(i, i + concurrency)
    await Promise.all(batch.map(execute))
    
    // 每批之间稍作延迟，避免触发API频率限制
    if (i + concurrency < tasks.length) {
      await new Promise(resolve => setTimeout(resolve, 100))
    }
  }

  return results
}

/**
 * 使用Haversine公式计算直线距离（备用方案）
 * @param {Object} origin - 起点坐标 {lat: number, lng: number}
 * @param {Object} destination - 终点坐标 {lat: number, lng: number}
 * @returns {number} 距离（公里）
 */
function calculateStraightDistance(origin, destination) {
  const R = 6371 // 地球半径（公里）
  const dLat = toRad(destination.lat - origin.lat)
  const dLng = toRad(destination.lng - origin.lng)
  
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRad(origin.lat)) *
      Math.cos(toRad(destination.lat)) *
      Math.sin(dLng / 2) *
      Math.sin(dLng / 2)
  
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  return R * c
}

function toRad(degrees) {
  return degrees * (Math.PI / 180)
}

