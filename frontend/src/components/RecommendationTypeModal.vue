<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content">
      <div class="modal-header">
        <h2>选择推荐类型</h2>
        <button class="close-btn" @click="$emit('close')">&times;</button>
      </div>
      <div class="modal-body">
        <button class="recommendation-btn" @click="selectType('normal')">
          <div class="btn-icon"></div>
          <div class="btn-content">
            <div class="btn-title">普通推荐</div>
            <div class="btn-description">根据距离、功率等条件推荐充电站</div>
          </div>
        </button>
        <button class="recommendation-btn" @click="selectType('personalized')">
          <div class="btn-icon"></div>
          <div class="btn-content">
            <div class="btn-title">个性化推荐</div>
            <div class="btn-description">基于您的车辆和充电习惯推荐（敬请期待）</div>
          </div>
        </button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'RecommendationTypeModal',
  methods: {
    selectType(type) {
      if (type === 'personalized') {
        alert('个性化推荐功能即将上线，敬请期待！')
        return
      }
      this.$emit('type-selected', type)
    }
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3000;
}

.modal-content {
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  width: 90%;
  max-width: 500px;
  overflow: hidden;
}

.modal-header {
  padding: 24px;
  border-bottom: 1px solid #e5e5e5;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #43CB83 0%, #3ab370 100%);
}

.modal-header h2 {
  margin: 0;
  color: white;
  font-size: 24px;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  font-size: 32px;
  color: white;
  cursor: pointer;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background 0.2s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.modal-body {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.recommendation-btn {
  background: white;
  border: 2px solid #e5e5e5;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.3s ease;
  text-align: left;
}

.recommendation-btn:hover {
  border-color: #43CB83;
  background: #f0fdf4;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(67, 203, 131, 0.2);
}

.btn-icon {
  font-size: 40px;
  flex-shrink: 0;
}

.btn-content {
  flex: 1;
}

.btn-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.btn-description {
  font-size: 14px;
  color: #666;
}
</style>
