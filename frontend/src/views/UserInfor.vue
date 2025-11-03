<template>
  <div class="userinfor-root">
    <div class="userinfo-sidebar">
      <div class="avatar-box">
        <img class="avatar" src="../image/user.png" alt="头像" />
        <div class="username">{{ user && user.username ? user.username : '' }}</div>
      </div>
      <div class="userinfo-list">
        <div class="userinfo-item settings" @click="isAccountSettingsVisible = true"><i class="iconfont icon-setting"></i> 账号设置 <span class="ui-arrow">&gt;</span></div>
        <div class="userinfo-item records"><i class="iconfont icon-record"></i> 充电记录 <span class="ui-arrow">&gt;</span></div>
        <div class="userinfo-item logout" @click="logout"><i class="iconfont icon-logout"></i> 退出登录</div>
      </div>
    </div>
    <div class="userinfo-main">
      <div class="section">
        <div class="section-title">
          <span>我的车辆</span>
          <button class="change-car-btn" @click="isModalVisible = true">更改</button>
        </div>
        <div class="cars-list">
          <div class="car-card">
            <img class="car-img" :src="carImageUrl" alt="车辆图片">
            <div class="car-meta">
              <div class="car-model">{{ selectedCarModel }}</div>
            </div>
          </div>
        </div>
      </div>
      <div class="section sec-fav">
        <div class="section-title">我的收藏</div>
        <div class="fav-list">
          <div class="fav-item">
            <span class="fav-title">超级充电站·人民广场</span>
            <span class="fav-meta">上海市黄浦区西藏中路2号</span>
          </div>
          <div class="fav-item">
            <span class="fav-title">超级充电站·人民广场</span>
            <span class="fav-meta">上海市黄浦区西藏中路2号</span>
          </div><div class="fav-item">
            <span class="fav-title">超级充电站·人民广场</span>
            <span class="fav-meta">上海市黄浦区西藏中路2号</span>
          </div><div class="fav-item">
            <span class="fav-title">超级充电站·人民广场</span>
            <span class="fav-meta">上海市黄浦区西藏中路2号</span>
          </div><div class="fav-item">
            <span class="fav-title">超级充电站·人民广场</span>
            <span class="fav-meta">上海市黄浦区西藏中路2号</span>
          </div><div class="fav-item">
            <span class="fav-title">超级充电站·人民广场</span>
            <span class="fav-meta">上海市黄浦区西藏中路2号</span>
          </div>
          <div class="fav-item">
            <span class="fav-title">小桔快充·中山北路</span>
            <span class="fav-meta">上海市静安区中山北路1018号</span>
          </div>
        </div>
      </div>
    </div>
    <CarSelectionModal 
      v-if="isModalVisible" 
      @close="isModalVisible = false"
      @car-selected="handleCarSelection" 
    />
    <AccountSettingsModal 
      v-if="isAccountSettingsVisible" 
      :visible="isAccountSettingsVisible"
      :user="user"
      @close="isAccountSettingsVisible = false"
      @username-updated="handleUsernameUpdated"
    />
  </div>
</template>

<script>
import CarSelectionModal from '../components/CarSelectionModal.vue';
import AccountSettingsModal from '../components/AccountSettingsModal.vue';
import { updateUserCarModel } from '../api/auth.js';
import axios from 'axios';

export default {
  name: 'UserInfor',
  components: {
    CarSelectionModal,
    AccountSettingsModal
  },
  data() {
    return {
      user: null,
      avatarUrl: '',
      isModalVisible: false,
      isAccountSettingsVisible: false,
      selectedCarModel: '暂无车辆',
      carImageUrl: new URL('../image/default.png', import.meta.url).href
    }
  },
  mounted() {
    const userStr = localStorage.getItem('user')
    if (userStr) {
      this.user = JSON.parse(userStr)
      this.avatarUrl = this.user.avatar || 'https://i.gtimg.cn/club/item/face/img/2/15922_100.gif'
      if (this.user.carModel) {
        this.selectedCarModel = this.user.carModel;
        this.fetchCarImage(this.user.carModel);
      }
    } else {
      this.$router.push('/login')
    }
  },
  methods: {
    logout() {
      localStorage.removeItem('user')
      this.$router.push('/login')
    },
    async handleCarSelection(car) {
      this.selectedCarModel = car.model;
      
      try {
        await updateUserCarModel(this.user.username, car.model);
        
        let userToUpdate = JSON.parse(localStorage.getItem('user'));
        userToUpdate.carModel = car.model;
        localStorage.setItem('user', JSON.stringify(userToUpdate));
        this.user.carModel = car.model;

      } catch (error) {
        console.error("更新用户车型失败:", error);
      }
      
      this.fetchCarImage(car.model);
    },
    async fetchCarImage(modelName) {
      try {
        const response = await axios.get(`http://localhost:8080/api/cars/image`, { 
          params: { model: modelName },
          responseType: 'blob' 
        });
        this.carImageUrl = URL.createObjectURL(response.data);
      } catch (error) {
        console.error("获取车辆图片失败:", error);
        this.carImageUrl = new URL('../image/仰望U9.png', import.meta.url).href;
      }
    },
    handleUsernameUpdated(newUsername) {
      this.user.username = newUsername;
      // 更新页面显示的用户名
      this.$forceUpdate();
    }
  }
}
</script>

<style scoped>
/* --- 1. Global Layout & Background --- */
.userinfor-root {
  min-height: 100vh;
  padding: 50px 32px;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  gap: 30px;
  /* New Thematic Gradient Background */
  background: linear-gradient(180deg, #f8f9fa 0%, #e9f5ff 100%);
}

/* --- 2. Card Base Style --- */
.userinfo-sidebar,
.userinfo-main {
  background: #ffffff;
  border: 1px solid #dee2e6;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.05); /* Enhanced shadow for more depth */
  border-radius: 16px; /* Larger radius for a softer, more modern look */
  transition: all 0.3s ease;
}

/* --- 3. Sidebar --- */
.userinfo-sidebar {
  width: 280px;
  padding: 30px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  overflow: hidden;
}

.userinfo-sidebar::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 150px;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1440 320'%3E%3Cpath fill='%231DE9B6' fill-opacity='0.15' d='M0,96L48,112C96,128,192,160,288,160C384,160,480,128,576,133.3C672,139,768,181,864,181.3C960,181,1056,139,1152,117.3C1248,96,1344,96,1392,96L1440,96L1440,0L1392,0C1344,0,1248,0,1152,0C1056,0,960,0,864,0C768,0,672,0,576,0C480,0,384,0,288,0C192,0,96,0,48,0L0,0Z'%3E%3C/path%3E%3C/svg%3E");
  background-size: cover;
  background-repeat: no-repeat;
  z-index: 0;
}

.userinfo-sidebar::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 150px;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1440 320'%3E%3Cpath fill='%231DE9B6' fill-opacity='0.15' d='M0,192L48,197.3C96,203,192,213,288,224C384,235,480,245,576,250.7C672,256,768,256,864,240C960,224,1056,192,1152,170.7C1248,149,1344,139,1392,133.3L1440,128L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z'%3E%3C/path%3E%3C/svg%3E");
  background-size: cover;
  background-repeat: no-repeat;
  z-index: 0;
}

.avatar-box,
.userinfo-list {
  position: relative;
  z-index: 1;
}

.avatar-box {
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  object-fit: cover;
  margin-bottom: 15px;
  border: 4px solid #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.username {
  font-size: 22px;
  font-weight: 600;
  color: #343a40;
}

.userinfo-list {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.userinfo-item {
  width: 100%;
  font-size: 20px;
  font-weight: 550;
  color: #495057;
  padding: 15px 20px;
  cursor: pointer;
  transition: all 0.2s ease-in-out;
  display: flex;
  gap: 12px;
  border-radius: 10px;
  border: none;
}

.userinfo-item .iconfont {
  font-size: 20px;
  color: #868e96;
  transition: all 0.2s ease;
}

.userinfo-item:hover {
  background-color: #f1faff;
  color: #007BFF;
  /* transform: translateX(5px); // Removed for a cleaner feel */
}

.userinfo-item:hover .iconfont {
  color: #007BFF;
}

.ui-arrow {
  margin-left: auto;
  font-size: 16px;
  color: #adb5bd;
}

.userinfo-item.logout {
  justify-content: center;
  color: #dc3545;
}
.userinfo-item.logout:hover {
  background-color: #fdf2f2;
}

/* --- 4. Main Content --- */
.userinfo-main {
  flex: 1;
  padding: 35px;
  display: flex;
  flex-direction: column;
  gap: 40px;
  position: relative;
  overflow: hidden; /* Important for containing the pseudo-elements */
}

/* Top Wave background pattern */
.userinfo-main::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 250px;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1440 320'%3E%3Cpath fill='%231DE9B6' fill-opacity='0.2' d='M0,160L48,144C96,128,192,96,288,106.7C384,117,480,171,576,197.3C672,224,768,224,864,202.7C960,181,1056,139,1152,128C1248,117,1344,139,1392,149.3L1440,160L1440,0L1392,0C1344,0,1248,0,1152,0C1056,0,960,0,864,0C768,0,672,0,576,0C480,0,384,0,288,0C192,0,96,0,48,0L0,0Z'%3E%3C/path%3E%3C/svg%3E");
  background-size: cover;
  background-repeat: no-repeat;
  z-index: 0;
}

/* Bottom Wave background pseudo-element */
.userinfo-main::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 250px;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1440 320'%3E%3Cpath fill='%231DE9B6' fill-opacity='0.2' d='M0,160L48,181.3C96,203,192,245,288,256C384,267,480,245,576,208C672,171,768,117,864,112C960,107,1056,149,1152,165.3C1248,181,1344,171,1392,165.3L1440,160L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z'%3E%3C/path%3E%3C/svg%3E");
  background-size: cover;
  background-repeat: no-repeat;
  z-index: 0;
}

/* Ensure content is above the pseudo-element */
.section {
  position: relative;
  z-index: 1;
}

.section-title {
  font-size: 24px;
  font-weight: 700;
  color: #343a40;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e9ecef;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.change-car-btn {
  background-color: #1DE9B6; /* Vibrant Teal */
  color: #00695C; /* Darker text for contrast */
  border: none;
  padding: 12px 25px;
  border-radius: 8px;
  font-size: 20px;
  font-weight: 600; /* Bold text */
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4px 15px rgba(29, 233, 182, 0.3);
}
.change-car-btn:hover {
  background-color: #00BFA5;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(29, 233, 182, 0.4);
}

.car-card {
  background: rgba(255, 255, 255, 0.6); /* Frosted Glass */
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px); /* For Safari */
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.car-img {
  width: 100%;
  max-width: 450px;
  height: auto;
  object-fit: contain;
}

.car-model {
  font-size: 22px;
  font-weight: 600;
  color: #343a40;
  text-align: center;
}

/* --- 5. Favorites List --- */
.fav-list {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 20px;
}

.fav-item {
  background: rgba(255, 255, 255, 0.6); /* Frosted Glass */
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px); /* For Safari */
  padding: 18px 20px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.2s ease;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.fav-item:hover {
  border-color: #1DE9B6;
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 4px 12px rgba(29, 233, 182, 0.2);
  transform: translateY(-3px);
}

.fav-title {
  font-size: 20px;
  font-weight: 600;
  color: #343a40;
}

.fav-meta {
  font-size: 16px;
  color: #6c757d;
  margin-top: 5px;
}

/* --- 6. Responsive Design --- */
@media (max-width: 900px) {
  .userinfor-root {
    flex-direction: column;
    padding: 20px 15px;
    gap: 20px;
  }
  .userinfo-sidebar, .userinfo-main {
    width: 100%;
    max-width: none;
  }
}
</style>
