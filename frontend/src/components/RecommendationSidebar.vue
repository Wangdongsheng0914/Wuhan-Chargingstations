<template>
  <div class="sidebar" :class="{ 'sidebar-open': isOpen }">
    <div class="sidebar-header">
      <h3>充电站推荐</h3>
      <button class="close-btn" @click="$emit('close')">&times;</button>
    </div>

    <div class="sidebar-content">
      <!-- 筛选条件区域 -->
      <div class="filters-section">
        <!-- 优先级选择：按钮组 -->
        <div class="priority-buttons">
          <button 
            class="priority-btn" 
            :class="{ active: filters.priority === 'distance' }"
            @click="setPriority('distance')"
          >
            距离优先
          </button>
          <button 
            class="priority-btn" 
            :class="{ active: filters.priority === 'power' }"
            @click="setPriority('power')"
          >
            功率优先
          </button>
          <button 
            class="priority-btn" 
            :class="{ active: filters.priority === 'available' }"
            @click="setPriority('available')"
          >
            可用优先
          </button>
        </div>

        <!-- 其他筛选条件：一行布局 -->
        <div class="filter-row">
          <div class="filter-item">
            <label>最小功率</label>
            <input 
              type="number" 
              v-model.number="filters.minPower" 
              @change="applyFilters" 
              min="0" 
              step="10" 
              placeholder="不限"
              class="compact-input"
            >
          </div>
          <div class="filter-item">
            <label>最少可用</label>
            <input 
              type="number" 
              v-model.number="filters.minAvailable" 
              @change="applyFilters" 
              min="0" 
              placeholder="不限"
              class="compact-input"
            >
          </div>
          <div class="filter-item">
            <label>充电类型</label>
            <select v-model="filters.chargingType" @change="applyFilters" class="compact-select">
              <option value="">全部</option>
              <option value="fast">快充</option>
              <option value="super_fast">超快充</option>
            </select>
          </div>
        </div>
      </div>

      <!-- 结果列表区域 -->
      <div class="results-section">
        <div class="section-title">
          推荐结果（{{ stations.length }}个）
        </div>
        <div v-if="loading" class="loading">
          <div class="loading-spinner"></div>
          <p>加载中...</p>
        </div>
        <div v-else-if="stations.length === 0" class="empty-state">
          <p>暂无符合条件的充电站</p>
        </div>
        <div v-else class="station-list">
          <div 
            v-for="station in stations" 
            :key="station.id"
            class="station-item"
            :class="{ 'active': selectedStation?.id === station.id }"
            @click="selectStation(station)"
          >
            <div class="station-header">
              <h4 class="station-name">{{ station.name }}</h4>
              <div class="station-scores">
                <span class="station-score" :title="`综合评分: ${station.score || 0}分`">{{ station.score ? station.score.toFixed(1) : '--' }}分</span>
                <span class="station-distance">{{ formatDistance(station.distance) }}</span>
              </div>
            </div>
            <div class="station-info">
              <div class="info-row">
                <span class="info-label">地址：</span>
                <span class="info-value">{{ station.address || '未知' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">运营商：</span>
                <span class="info-value">{{ station.operator || '未知' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">功率：</span>
                <span class="info-value">{{ station.maxPowerKw ? station.maxPowerKw + 'kW' : '未知' }}</span>
                <span class="info-label" style="margin-left: 16px;">可用：</span>
                <span class="info-value">{{ station.availableConnectors || 0 }}/{{ station.totalConnectors || 0 }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">类型：</span>
                <span class="info-value">{{ getChargingTypeText(station.chargingType) }}</span>
                <span class="info-label" style="margin-left: 16px;">24小时：</span>
                <span class="info-value">{{ station.is24h ? '是' : '否' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'RecommendationSidebar',
  props: {
    isOpen: {
      type: Boolean,
      default: false
    },
    userLocation: {
      type: Object,
      required: false,
      default: null
    }
  },
  data() {
    return {
      stations: [],
      loading: false,
      selectedStation: null,
      filters: {
        priority: 'distance',
        maxDistance: 10,
        minPower: null,
        minAvailable: null,
        chargingType: ''
      }
    }
  },
  mounted() {
    if (this.isOpen && this.userLocation) {
      this.loadRecommendations()
    }
  },
  watch: {
    isOpen(newVal) {
      if (newVal && this.userLocation) {
        this.loadRecommendations()
      }
    }
  },
  methods: {
    async loadRecommendations() {
      if (!this.userLocation || !this.userLocation.lat || !this.userLocation.lng) {
        console.error('用户位置信息不完整')
        return
      }

      this.loading = true
      try {
        const { recommendStations } = await import('../api/stations')
        
        const params = {
          lat: this.userLocation.lat,
          lng: this.userLocation.lng,
          priority: this.filters.priority,
          maxDistance: 10, // 最大搜索半径为10公里
          limit: 50, // 增加限制，因为前端还要筛选和评分
          minPower: this.filters.minPower || undefined,
          minAvailable: this.filters.minAvailable || undefined,
          chargingType: this.filters.chargingType || undefined
        }

        // 获取充电站列表（不包含实际路径距离）
        const stations = await recommendStations(params) || []
        
        // 如果列表为空，直接返回
        if (stations.length === 0) {
          this.stations = []
          this.$emit('stations-loaded', this.stations)
          return
        }

        // 在浏览器端使用百度地图API计算实际路径距离
        await this.calculateDistances(stations)

        // 根据实际距离筛选和排序
        this.filterAndSortStations(stations)

        this.$emit('stations-loaded', this.stations)
      } catch (error) {
        console.error('加载推荐结果失败:', error)
        if (error.code === 'ECONNABORTED') {
          alert('请求超时，请检查网络连接或稍后重试')
        } else {
          alert('加载推荐结果失败，请稍后重试')
        }
        this.stations = []
      } finally {
        this.loading = false
      }
    },
    async calculateDistances(stations) {
      // 等待百度地图API加载
      if (typeof BMap === 'undefined') {
        console.warn('百度地图API未加载，使用直线距离')
        return
      }

      try {
        const { calculateRouteDistances } = await import('../utils/routeDistance')
        
        const origin = {
          lat: this.userLocation.lat,
          lng: this.userLocation.lng
        }
        
        const destinations = stations.map(station => ({
          lat: station.lat,
          lng: station.lng,
          id: station.id
        }))

        // 批量计算距离（并发数设为3，避免触发API限制）
        const distanceMap = await calculateRouteDistances(
          origin,
          destinations,
          3,
          (completed, total) => {
            // 可以在这里更新进度
            console.log(`计算距离进度: ${completed}/${total}`)
          }
        )

        // 更新每个充电站的距离
        stations.forEach(station => {
          const distance = distanceMap.get(station.id)
          if (distance !== undefined && distance !== null) {
            station.distance = distance
          }
        })
      } catch (error) {
        console.error('计算路径距离失败:', error)
        // 如果计算失败，使用已有的Haversine距离
      }
    },
    filterAndSortStations(stations) {
      // 根据实际距离筛选（如果已计算）
      let filtered = stations.filter(station => {
        if (station.distance === null || station.distance === undefined) {
          return false // 没有距离信息的直接过滤掉
        }
        return station.distance <= 10 // 最大搜索距离10公里
      })

      // 如果过滤后没有结果，直接返回
      if (filtered.length === 0) {
        this.stations = []
        return
      }

      // 使用加权评分系统进行排序
      const scoredStations = filtered.map(station => ({
        ...station,
        score: this.calculateScore(station, filtered)
      }))

      // 按评分降序排序
      scoredStations.sort((a, b) => b.score - a.score)

      // 限制返回数量
      this.stations = scoredStations.slice(0, 20)
    },

    /**
     * 计算充电站的综合评分（0-100分）
     * 根据优先级设置权重，综合考虑距离、功率、可用性三个因子
     */
    calculateScore(station, allStations) {
      // 获取权重配置
      const weights = this.getWeightsByPriority()

      // 归一化各因子（0-1范围）
      const distanceNormalized = this.normalizeDistance(station.distance, allStations)
      const powerNormalized = this.normalizePower(station.maxPowerKw, allStations)
      const availabilityNormalized = this.normalizeAvailability(
        station.availableConnectors,
        station.totalConnectors,
        allStations
      )

      // 加权计算（每个因子满分100，按权重分配）
      const distanceScore = distanceNormalized * 100 * weights.distance
      const powerScore = powerNormalized * 100 * weights.power
      const availabilityScore = availabilityNormalized * 100 * weights.availability

      // 综合评分（总分100分）
      const totalScore = distanceScore + powerScore + availabilityScore

      return Math.round(totalScore * 100) / 100 // 保留两位小数
    },

    /**
     * 根据优先级获取权重配置
     */
    getWeightsByPriority() {
      switch (this.filters.priority) {
        case 'distance':
          // 距离优先：距离50%，功率30%，可用20%
          return { distance: 0.5, power: 0.3, availability: 0.2 }
        case 'power':
          // 功率优先：功率50%，距离30%，可用20%
          return { distance: 0.3, power: 0.5, availability: 0.2 }
        case 'available':
          // 可用优先：可用50%，距离30%，功率20%
          return { distance: 0.3, power: 0.2, availability: 0.5 }
        default:
          return { distance: 0.5, power: 0.3, availability: 0.2 }
      }
    },

    /**
     * 归一化距离分数（0-1）
     * 距离越近分数越高，最大距离10km，超过的直接为0
     */
    normalizeDistance(distance, allStations) {
      if (distance === null || distance === undefined || distance > 10) {
        return 0
      }

      // 找到所有站点的最小和最大距离
      const distances = allStations
        .map(s => s.distance)
        .filter(d => d !== null && d !== undefined && d <= 10)

      if (distances.length === 0) return 0

      const minDistance = Math.min(...distances)
      const maxDistance = Math.max(...distances)

      // 如果只有一个站点，返回满分
      if (minDistance === maxDistance) return 1

      // 距离越小分数越高，使用反向归一化
      // score = (max - current) / (max - min)
      return (maxDistance - distance) / (maxDistance - minDistance)
    },

    /**
     * 归一化功率分数（0-1）
     * 功率越高分数越高
     */
    normalizePower(powerKw, allStations) {
      if (powerKw === null || powerKw === undefined || powerKw === 0) {
        return 0
      }

      // 找到所有站点的最小和最大功率
      const powers = allStations
        .map(s => s.maxPowerKw)
        .filter(p => p !== null && p !== undefined && p > 0)

      if (powers.length === 0) return 0

      const minPower = Math.min(...powers)
      const maxPower = Math.max(...powers)

      // 如果只有一个站点，返回满分
      if (minPower === maxPower) return 1

      // 功率越大分数越高
      return (powerKw - minPower) / (maxPower - minPower)
    },

    /**
     * 归一化可用性分数（0-1）
     * 综合考虑可用数量和可用率
     */
    normalizeAvailability(available, total, allStations) {
      // 如果没有可用充电桩，返回0分
      const availCount = available || 0
      if (availCount === 0) {
        return 0
      }

      // 计算可用率
      const availabilityRate = total && total > 0 ? availCount / total : 0

      // 找到所有站点的最小和最大可用数量
      const availabilities = allStations
        .map(s => s.availableConnectors || 0)
        .filter(a => a >= 0) // 包含0，以便正确归一化

      if (availabilities.length === 0) return 0

      const minAvail = Math.min(...availabilities)
      const maxAvail = Math.max(...availabilities)

      // 如果只有一个站点或所有站点可用数相同
      if (minAvail === maxAvail) {
        // 如果所有站点都是0，返回0
        if (maxAvail === 0) return 0
        // 否则只考虑可用率
        return availabilityRate
      }

      // 综合可用数量和可用率
      // 可用数量归一化（0-0.6权重） + 可用率（0-0.4权重）
      const countScore = (availCount - minAvail) / (maxAvail - minAvail) * 0.6
      const rateScore = availabilityRate * 0.4

      return Math.min(countScore + rateScore, 1) // 确保不超过1
    },
    setPriority(priority) {
      this.filters.priority = priority
      this.applyFilters()
    },
    applyFilters() {
      this.loadRecommendations()
    },
    selectStation(station) {
      this.selectedStation = station
      this.$emit('station-selected', station)
    },
    formatDistance(distance) {
      if (distance === null || distance === undefined) return '未知'
      if (distance < 1) {
        return (distance * 1000).toFixed(0) + 'm'
      }
      return distance.toFixed(2) + 'km'
    },
    getChargingTypeText(type) {
      const typeMap = {
        'fast': '快充',
        'super_fast': '超快充',
        'slow': '慢充'
      }
      return typeMap[type] || type || '未知'
    }
  }
}
</script>

<style scoped>
.sidebar {
  position: fixed;
  left: -420px;
  top: 0px;
  width: 420px;
  height: calc(100vh - 60px);
  background: white;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  transition: left 0.3s ease;
  z-index: 2001;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sidebar-open {
  left: 0;
}

.sidebar-header {
  padding: 15px;
  border-bottom: 1px solid #e5e5e5;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #43CB83 0%, #3ab370 100%);
}

.sidebar-header h3 {
  margin: 0;
  color: white;
  font-size: 23px;
  font-weight: 600;
}

.close-btn {
  position: relative;
  right: 40px;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  font-size: 28px;
  color: white;
  cursor: pointer;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background 0.2s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.4);
}

.sidebar-content {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.filters-section {
  padding: 16px;
  border-bottom: 1px solid #e5e5e5;
  background: #f9f9f9;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
}

/* 优先级按钮组 */
.priority-buttons {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.priority-btn {
  flex: 1;
  padding: 8px 12px;
  border: 2px solid #ddd;
  border-radius: 6px;
  background: white;
  color: #666;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.priority-btn:hover {
  border-color: #43CB83;
  color: #43CB83;
}

.priority-btn.active {
  background: #43CB83;
  border-color: #43CB83;
  color: white;
}

/* 其他筛选条件：一行布局 */
.filter-row {
  display: flex;
  gap: 8px;
  align-items: flex-end;
}

.filter-item {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.filter-item label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
  margin-bottom: 4px;
  white-space: nowrap;
}

.compact-input,
.compact-select {
  width: 100%;
  padding: 8px 10px;
  border: 2px solid #ddd;
  border-radius: 6px;
  font-size: 15px;
  transition: border-color 0.2s;
  box-sizing: border-box;
}

.compact-input:focus,
.compact-select:focus {
  outline: none;
  border-color: #43CB83;
}

.results-section {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.station-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.station-item {
  padding: 16px;
  border: 2px solid #e5e5e5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  background: white;
}

.station-item:hover {
  border-color: #43CB83;
  box-shadow: 0 2px 8px rgba(67, 203, 131, 0.2);
}

.station-item.active {
  border-color: #43CB83;
  background: #f0fdf4;
}

.station-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
  gap: 12px;
}

.station-name {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
  flex: 1;
}

.station-scores {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

.station-score {
  font-size: 13px;
  font-weight: 600;
  color: blue;
  padding: 2px 8px;
  background: #f0f4ff;
  border-radius: 4px;
  white-space: nowrap;
}

.station-distance {
  font-size: 14px;
  font-weight: 600;
  color: #43CB83;
  white-space: nowrap;
}

.station-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.info-row {
  font-size: 13px;
  color: #666;
  display: flex;
  flex-wrap: wrap;
}

.info-label {
  color: #999;
  margin-right: 4px;
}

.info-value {
  color: #333;
}

.loading,
.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #43CB83;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
