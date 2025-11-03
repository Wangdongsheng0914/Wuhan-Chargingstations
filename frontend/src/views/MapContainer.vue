<template>
  <div class="navbar-trapezoid">
    <div class="navbar-btn-group">
      <button class="nav-btn">充电推荐</button>
      <button class="nav-btn">途中充电</button>
      <button class="nav-btn">充电管理</button>
      <button class="nav-btn">AI助手</button>
      <button class="nav-btn" @click="$router.push('/userinfor')">个人信息</button>
    </div>
  </div>
  <div class="map-container">
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
</template>

<script>
export default {
  name: 'MapContainer',
  data() {
    return {
      user: null,
      map: null,
      mapLoaded: false,
      currentLocation: null  // 保存当前位置
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
      script.src = 'https://api.map.baidu.com/api?v=3.0&ak=1pqRR7L9bsfPDSvESGJtLhtzLk45cFhX&callback=initBaiduMap'
      script.async = true
      
      // 设置全局回调函数
      window.initBaiduMap = () => {
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

.baidu-map {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  width: 100%;
  height: 100vh;
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

