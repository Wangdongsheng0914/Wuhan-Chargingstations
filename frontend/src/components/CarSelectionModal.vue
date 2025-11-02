<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content">
      <div class="modal-header">
        <h2>选择您的车型</h2>
        <button class="close-btn" @click="$emit('close')">&times;</button>
      </div>
      <div class="modal-body">
        <div class="filters">
          <input type="text" v-model="searchTerm" placeholder="搜索车型或品牌..." class="search-input">
          <select v-model="selectedBrand" class="brand-select">
            <option value="">全部品牌</option>
            <option v-for="brand in uniqueBrands" :key="brand" :value="brand">{{ brand }}</option>
          </select>
          <button @click="clearFilters" class="clear-filter-btn">清除筛选</button>
        </div>
        <div class="car-list-container">
          <div v-for="car in filteredCars" :key="car.id" class="car-item-card">
            <div class="car-title">{{ car.model }}</div>
            <div class="car-details">
              <span>续航: {{ car.electricRangeKm }}km</span>
              <span>厂商: {{ car.manufacturer }}</span>
              <span>充电: {{ car.chargingTimeHours }}</span>
            </div>
            <button @click="selectCar(car)" class="select-car-btn">选择此车型</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'CarSelectionModal',
  data() {
    return {
      allCars: [],
      searchTerm: '',
      selectedBrand: '',
    };
  },
  computed: {
    uniqueBrands() {
      const brands = this.allCars.map(car => car.manufacturer);
      return [...new Set(brands)];
    },
    filteredCars() {
      return this.allCars.filter(car => {
        const matchesSearch = car.model.toLowerCase().includes(this.searchTerm.toLowerCase()) || 
                              car.manufacturer.toLowerCase().includes(this.searchTerm.toLowerCase());
        const matchesBrand = !this.selectedBrand || car.manufacturer === this.selectedBrand;
        return matchesSearch && matchesBrand;
      });
    }
  },
  methods: {
    async fetchAllCars() {
      try {
        const response = await axios.get('http://localhost:8080/api/cars');
        this.allCars = response.data;
      } catch (error) {
        console.error("获取车辆列表失败:", error);
      }
    },
    clearFilters() {
      this.searchTerm = '';
      this.selectedBrand = '';
    },
    selectCar(car) {
      this.$emit('car-selected', car);
      this.$emit('close');
    }
  },
  created() {
    this.fetchAllCars();
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 25px;
  border-radius: 10px;
  box-shadow: 0 5px 15px rgba(0,0,0,0.3);
  width: 90%;
  max-width: 1000px;
  height: 80vh;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eee;
  padding-bottom: 15px;
  margin-bottom: 20px;
}

.modal-header h2 {
  margin: 0;
  font-size: 24px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 30px;
  cursor: pointer;
}

.modal-body {
  display: flex;
  flex-direction: column;
  flex-grow: 1;
  overflow: hidden;
}

.filters {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.search-input, .brand-select {
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 5px;
  font-size: 16px;
}

.search-input {
  flex-grow: 1;
}

.clear-filter-btn {
  padding: 10px 15px;
  background-color: #f44336;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.car-list-container {
  flex-grow: 1;
  overflow-y: auto;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  padding: 10px;
  background: #f9f9f9;
  border-radius: 5px;
}

.car-item-card {
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 15px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.car-title {
  font-weight: bold;
  font-size: 18px;
}

.car-details {
  font-size: 14px;
  color: #555;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.select-car-btn {
  margin-top: auto;
  padding: 10px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}
</style>
