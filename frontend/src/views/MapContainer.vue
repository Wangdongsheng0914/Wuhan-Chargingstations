<template>
  <div class="navbar-trapezoid">
    <div class="navbar-btn-group">
      <button class="nav-btn" @click="showRecommendationTypeModal">充电推荐</button>
      <button class="nav-btn">途中充电</button>
      <button class="nav-btn">充电管理</button>
      <button class="nav-btn">AI助手</button>
      <button class="nav-btn" @click="$router.push('/userinfor')">个人信息</button>
    </div>
  </div>
  <div class="map-container" :class="{ 'sidebar-open': sidebarOpen }">
    <!-- 百度地图容器 -->
    <div id="baiduMap" class="baidu-map"></div>

    <!-- 自定义地图控制按钮 -->
    <div class="map-controls">
      <!-- 重新定位按钮 -->
      <button class="control-btn location-btn" @click="relocate" title="重新定位">
        <svg viewBox="0 0 24 24" width="20" height="20">
          <circle cx="12" cy="12" r="8" fill="none" stroke="currentColor" stroke-width="2"/>
          <circle cx="12" cy="12" r="3" fill="currentColor"/>
        </svg>
      </button>
      
      <!-- 放大按钮 -->
      <button class="control-btn zoom-btn" @click="zoomIn" title="放大">
        <span>+</span>
      </button>
      
      <!-- 缩小按钮 -->
      <button class="control-btn zoom-btn" @click="zoomOut" title="缩小">
        <span>−</span>
      </button>
    </div>

    <!-- 加载提示 -->
    <div v-if="!mapLoaded" class="loading-overlay">
      <div class="loading-spinner"></div>
      <p>地图加载中...</p>
    </div>
  </div>

  <!-- 推荐类型选择模态框 -->
  <RecommendationTypeModal 
    v-if="showTypeModal"
    @close="showTypeModal = false"
    @type-selected="handleRecommendationTypeSelected"
  />

  <!-- 推荐结果侧边栏 -->
  <RecommendationSidebar
    :is-open="sidebarOpen"
    :user-location="userLocationForAPI"
    @close="closeSidebar"
    @stations-loaded="handleStationsLoaded"
    @station-selected="handleStationSelected"
  />
</template>

<script>
import RecommendationTypeModal from '../components/RecommendationTypeModal.vue'
import RecommendationSidebar from '../components/RecommendationSidebar.vue'

export default {
  name: 'MapContainer',
  components: {
    RecommendationTypeModal,
    RecommendationSidebar
  },
  data() {
    return {
      user: null,
      map: null,
      mapLoaded: false,
      currentLocation: null,  // 保存当前位置（百度地图Point对象）
      showTypeModal: false,
      sidebarOpen: false,
      stationMarkers: [],  // 保存充电站标记
      recommendedStations: []  // 保存推荐的充电站列表
    }
  },
  computed: {
    // 将百度坐标转换为API需要的格式
    userLocationForAPI() {
      if (!this.currentLocation) return null
      return {
        lat: this.currentLocation.lat,
        lng: this.currentLocation.lng
      }
    }
  },
  mounted() {
    // 获取用户信息
    const userStr = localStorage.getItem('user')
    if (userStr) {
      this.user = JSON.parse(userStr)
    } else {
      // 如果没有用户信息，跳转到登录页
      this.$router.push('/login')
      return
    }

    // 异步加载百度地图API
    this.loadBaiduMapScript()
  },
  methods: {
    // 异步加载百度地图API
    loadBaiduMapScript() {
      // 如果已经加载过，直接初始化
      if (typeof BMap !== 'undefined') {
        this.initMap()
        return
      }

      // 创建script标签异步加载
      const script = document.createElement('script')
      script.type = 'text/javascript'
      // 加载包含路径规划服务的完整API
      script.src = 'https://api.map.baidu.com/api?v=3.0&ak=1pqRR7L9bsfPDSvESGJtLhtzLk45cFhX&callback=initBaiduMap'
      script.async = true
      
      // 设置全局回调函数
      window.initBaiduMap = () => {
        // 将地图实例保存到全局，供路径规划工具使用
        window.map = null // 稍后在initMap中设置
        this.initMap()
      }
      
      // 添加到页面
      document.head.appendChild(script)
    },

    initMap() {
      // 确保BMap已加载
      if (typeof BMap === 'undefined') {
        return
      }

      try {
        // 创建地图实例
        this.map = new BMap.Map('baiduMap')
        
        // 将地图实例保存到全局，供路径规划工具使用
        window.map = this.map
        
        // 设置默认中心点（中国中心位置）
        const defaultPoint = new BMap.Point(104.195, 35.861)
        
        // 初始化地图，设置中心点和缩放级别
        this.map.centerAndZoom(defaultPoint, 5)
        
        // 启用滚轮缩放
        this.map.enableScrollWheelZoom(true)
        
        // 不添加任何控件
        
        // 直接使用浏览器原生GPS定位（最准确）
        if (navigator.geolocation) {
          navigator.geolocation.getCurrentPosition((position) => {
            const gpsLng = position.coords.longitude
            const gpsLat = position.coords.latitude
            const accuracy = position.coords.accuracy
            
            // 将GPS坐标（WGS84）转换为百度坐标（BD09）
            const convertor = new BMap.Convertor()
            const gpsPoint = new BMap.Point(gpsLng, gpsLat)
            
            // 参数说明：1表示GPS坐标，5表示转换为百度坐标
            convertor.translate([gpsPoint], 1, 5, (data) => {
              if (data.status === 0) {
                const bdPoint = data.points[0]
                
                // 保存当前位置
                this.currentLocation = bdPoint
                
                // 设置地图中心为当前位置，放大到20级
                this.map.centerAndZoom(bdPoint, 20)
                
                // 在当前位置添加红色标注
                const marker = new BMap.Marker(bdPoint)
                this.map.addOverlay(marker)
                
                // 添加蓝色精度圆圈
                const circle = new BMap.Circle(bdPoint, accuracy || 50, {
                  strokeColor: '#1791fc',
                  strokeWeight: 2,
                  strokeOpacity: 0.5,
                  fillColor: '#1791fc',
                  fillOpacity: 0.2
                })
                this.map.addOverlay(circle)
              }
            })
          }, (error) => {
            if (error.code === 1) {
              alert('定位失败：用户拒绝了位置请求')
            } else if (error.code === 2) {
              alert('定位失败：位置信息不可用')
            } else if (error.code === 3) {
              alert('定位失败：请求超时')
            }
          }, {
            enableHighAccuracy: true,
            timeout: 10000,
            maximumAge: 0
          })
        } else {
          alert('您的浏览器不支持地理定位功能')
        }

        this.mapLoaded = true
      } catch (error) {
        console.error('地图初始化失败:', error)
      }
    },

    // 放大地图
    zoomIn() {
      if (this.map) {
        const currentZoom = this.map.getZoom()
        this.map.setZoom(currentZoom + 1)
      }
    },

    // 缩小地图
    zoomOut() {
      if (this.map) {
        const currentZoom = this.map.getZoom()
        this.map.setZoom(currentZoom - 1)
      }
    },

    // 重新定位到当前位置
    relocate() {
      if (this.currentLocation && this.map) {
        this.map.panTo(this.currentLocation)
      } else {
        // 如果没有保存的位置，重新获取GPS定位
        if (navigator.geolocation) {
          navigator.geolocation.getCurrentPosition((position) => {
            const gpsLng = position.coords.longitude
            const gpsLat = position.coords.latitude
            
            const convertor = new BMap.Convertor()
            const gpsPoint = new BMap.Point(gpsLng, gpsLat)
            
            convertor.translate([gpsPoint], 1, 5, (data) => {
              if (data.status === 0) {
                const bdPoint = data.points[0]
                this.currentLocation = bdPoint
                this.map.panTo(bdPoint)
              }
            })
          })
        }
      }
    },

    // 显示推荐类型选择模态框
    showRecommendationTypeModal() {
      if (!this.currentLocation) {
        alert('请等待定位完成后再使用推荐功能')
        return
      }
      this.showTypeModal = true
    },

    // 处理推荐类型选择
    handleRecommendationTypeSelected(type) {
      this.showTypeModal = false
      if (type === 'normal') {
        this.sidebarOpen = true
        // 侧边栏会自动加载推荐数据
      }
    },

    // 关闭侧边栏
    closeSidebar() {
      this.sidebarOpen = false
      this.clearStationMarkers()
    },

    // 处理充电站加载完成
    handleStationsLoaded(stations) {
      this.recommendedStations = stations
      this.displayStationsOnMap(stations)
    },

    // 在地图上显示充电站标记
    displayStationsOnMap(stations) {
      if (!this.map || !stations || stations.length === 0) return

      // 清除之前的标记
      this.clearStationMarkers()

      // 为每个充电站创建标记
      stations.forEach((station, index) => {
        const point = new BMap.Point(station.lng, station.lat)
        
        // 创建自定义图标
        const icon = new BMap.Icon(
          this.createStationIcon(station, index),
          new BMap.Size(32, 32),
          { anchor: new BMap.Size(16, 32) }
        )

        const marker = new BMap.Marker(point, { icon: icon })
        
        // 添加信息窗口
        const infoWindow = new BMap.InfoWindow(
          `<div style="padding: 10px; min-width: 200px;">
            <h4 style="margin: 0 0 8px 0; color: #333;">${station.name}</h4>
            <p style="margin: 4px 0; color: #666; font-size: 12px;">地址：${station.address || '未知'}</p>
            <p style="margin: 4px 0; color: #666; font-size: 12px;">距离：${this.formatDistance(station.distance)}</p>
            <p style="margin: 4px 0; color: #666; font-size: 12px;">功率：${station.maxPowerKw ? station.maxPowerKw + 'kW' : '未知'}</p>
            <p style="margin: 4px 0; color: #666; font-size: 12px;">可用：${station.availableConnectors || 0}/${station.totalConnectors || 0}</p>
          </div>`,
          { width: 250, height: 200, title: station.name }
        )

        marker.addEventListener('click', () => {
          this.map.openInfoWindow(infoWindow, point)
        })

        this.map.addOverlay(marker)
        this.stationMarkers.push(marker)
      })

      // 如果有充电站，调整地图视野以包含所有标记
      if (stations.length > 0) {
        this.adjustMapView(stations)
      }
    },

    // 创建充电站图标（SVG）
    createStationIcon(station, index) {
      const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 32 32">
        <circle cx="16" cy="16" r="14" fill="#43CB83" stroke="white" stroke-width="2"/>
        <text x="16" y="20" text-anchor="middle" fill="white" font-size="12" font-weight="bold">${index + 1}</text>
      </svg>`
      const encoded = encodeURIComponent(svg)
      return 'data:image/svg+xml;charset=utf-8,' + encoded
    },

    // 调整地图视野以包含所有充电站
    adjustMapView(stations) {
      if (!this.map || stations.length === 0) return

      const points = stations.map(s => new BMap.Point(s.lng, s.lat))
      
      // 如果只有当前位置，添加当前位置到视野计算
      if (this.currentLocation) {
        points.push(this.currentLocation)
      }

      // 计算包含所有点的最佳视野
      const viewport = this.map.getViewport(points, {
        padding: { top: 50, right: 50, bottom: 50, left: 470 } // 左侧留出侧边栏空间（420px侧边栏 + 50px边距）
      })

      this.map.setViewport(viewport)
    },

    // 清除所有充电站标记
    clearStationMarkers() {
      if (!this.map) return
      
      this.stationMarkers.forEach(marker => {
        this.map.removeOverlay(marker)
      })
      this.stationMarkers = []
    },

    // 处理充电站选择
    handleStationSelected(station) {
      if (!this.map) return

      const point = new BMap.Point(station.lng, station.lat)
      this.map.panTo(point)
      this.map.setZoom(16)
    },

    // 格式化距离显示
    formatDistance(distance) {
      if (distance === null || distance === undefined) return '未知'
      if (distance < 1) {
        return (distance * 1000).toFixed(0) + 'm'
      }
      return distance.toFixed(2) + 'km'
    }
  },
  beforeUnmount() {
    // 组件销毁时清理地图
    if (this.map) {
      this.map = null
    }
  }
}
</script>

<style scoped>
.navbar-trapezoid {
  height: 60px;
  width: 1000px;
  margin: 0px auto 0 auto; /* 距顶部适度留白 */
  background: #43CB83;
  clip-path: polygon(0 0, 100% 0, 95% 100%, 5% 100%);
  /* 上宽360，下宽140，实现倒梯形 */
  box-shadow: 0 4px 16px 0 rgba(0,0,0,0.12), 0 1.5px 6px 0 rgba(0,255,128,0.25); /* 立体悬浮 */
  z-index: 2002;
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  /* 使用absolute确保其确实悬浮于页面最上部 */
}
@media (max-width: 390px) {
  .navbar-trapezoid {
    width: 90vw;
    min-width: 120px;
  }
}
.map-container {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}

.map-container {
  transition: margin-left 0.3s ease;
}

.map-container.sidebar-open .baidu-map {
  margin-left: 420px;
  width: calc(100% - 420px);
}

.baidu-map {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  width: 100%;
  height: 100vh;
  transition: width 0.3s ease, margin-left 0.3s ease;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.95);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 5px solid #f3f3f3;
  border-top: 5px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-overlay p {
  color: #667eea;
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

/* 隐藏百度地图logo（可选） */
:deep(.anchorBL) {
  display: none !important;
}

/* 自定义地图控制按钮 */
.map-controls {
  position: absolute;
  right: 20px;
  bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  z-index: 1000;
}

.control-btn {
  width: 44px;
  height: 44px;
  background: white;
  border: none;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  color: #333;
}

.control-btn:hover {
  background: #f5f5f5;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.control-btn:active {
  transform: scale(0.95);
  background: #e8e8e8;
}

.location-btn svg {
  color: #1791fc;
}

.zoom-btn span {
  font-size: 24px;
  font-weight: 300;
  line-height: 1;
  color: #666;
}

.control-btn:hover .zoom-btn span {
  color: #333;
}

.navbar-btn-group {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-btn {
  flex: 1 1 0%;
  height: 100%;
  background: transparent;
  border: none;
  outline: none;
  color: #fff;
  font-size: 23px;
  font-weight: 700;
  letter-spacing: 2px;
  cursor: pointer;
  transition: background 0.18s cubic-bezier(.4,0,.2,1), color 0.2s;
  position: relative;
  border-radius: 0;
  box-shadow: none;
  padding: 0;
  margin: 0;
}

.nav-btn:not(:last-child)::after {
  content: '';
  position: absolute;
  right: 0;
  top: 0;
  height: 100%;
  width: 1px;
  background: rgba(255,255,255,0.32);
}

.nav-btn:hover {
  background: #21804d; /* 深绿色 */
  color: #fff;
}

.nav-btn:first-child {
  margin-left: 48px;
}
.nav-btn:last-child {
  margin-right: 48px;
}
</style>

