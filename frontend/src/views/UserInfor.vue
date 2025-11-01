<template>
  <div class="userinfor-root">
    <div class="userinfo-sidebar">
      <div class="avatar-box">
        <img class="avatar" src="../image/user.png" alt="头像" />
        <div class="username">{{ user && user.username ? user.username : '' }}</div>
      </div>
      <div class="userinfo-list">
        <div class="userinfo-item settings"><i class="iconfont icon-setting"></i> 账号设置 <span class="ui-arrow">&gt;</span></div>
        <div class="userinfo-item records"><i class="iconfont icon-record"></i> 充电记录 <span class="ui-arrow">&gt;</span></div>
        <div class="userinfo-item logout" @click="logout"><i class="iconfont icon-logout"></i> 退出登录</div>
      </div>
    </div>
    <div class="userinfo-main">
      <div class="section">
        <div class="section-title">我的车辆</div>
        <div class="cars-list">
          <div class="car-card">
            <img class="car-img" src="../image/仰望U9.png" alt="车辆图片">
            <div class="car-meta">
              <div class="car-model">特斯拉 Model 3</div>
              <div class="car-info">白色 | 动力长续航 | 2021年款</div>
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
            <span class="fav-title">小桔快充·中山北路</span>
            <span class="fav-meta">上海市静安区中山北路1018号</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'UserInfor',
  data() {
    return {
      user: null,
      avatarUrl: ''
    }
  },
  mounted() {
    const userStr = localStorage.getItem('user')
    if (userStr) {
      this.user = JSON.parse(userStr)
      // 支持user.avatar字段，若有则作为头像，否则沿用默认头像
      this.avatarUrl = this.user.avatar || 'https://i.gtimg.cn/club/item/face/img/2/15922_100.gif'
    } else {
      // 没有登录信息，则跳转登录页
      this.$router.push('/login')
    }
  },
  methods: {
    logout() {
      localStorage.removeItem('user')
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.userinfor-root {
  min-height: 100vh;
  height: auto;
  max-width: 1150px;
  margin: 0 auto;
  padding: 0 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 42px;
  background: none;
}
.userinfo-sidebar {
  width: 260px;
  background: #fff;
  border: 1px solid #e1e1e1;
  box-shadow: 0px 6px 12px  0px rgba(0,0,0,0.08);
  padding: 36px 10px 24px 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.avatar-box {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.avatar {
  width: 78px;
  height: 78px;
  border-radius: 50%;
  object-fit: cover;
  margin-bottom: 14px;
  box-shadow: 0 2px 12px rgba(67,203,131,0.17);
  background: #f6f6f6;
}
.username {
  font-size: 19px;
  font-weight: 600;
  margin-bottom: 24px;
  color: #434343;
  text-align: center;
  word-break: break-all;
}
.userinfo-list {
  width: 100%;
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.userinfo-item {
  width: 100%;
  font-size: 20px;
  color: #2d2d2d;
  padding: 11px 18px;
  cursor: pointer;
  transition: background 0.15s;
  display: flex;
  align-items: center;
  gap: 8px;
}
.userinfo-item.logout {
  color: #ef5151;
  font-weight: 500;
  justify-content: center;
}
.userinfo-item.settings {
  border-bottom: 1px solid grey;
  justify-content: flex-start;
}
.userinfo-item.records {
  border-bottom: 1px solid grey;
  justify-content: flex-start;
}
.userinfo-item:hover {
  background: #e8f5ee;
}
.userinfo-item.logout:hover {
  background: #ffeded;
}
.userinfo-item .iconfont {
  font-size: 18px;
}
.ui-arrow {
  margin-left: auto;
  font-size: 18px;
  color: #bbb;
  font-weight: bold;
  padding-left: 10px;
}
.userinfo-main {
  flex: 1 1 0;
  background: #fff;
  border: 1px solid #e1e1e1;
  padding: 34px 38px 32px 38px;
  box-shadow: 0 6px 12px 0px rgba(0,0,0,0.08);
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 28px;
}
.section {
  margin-bottom: 12px;
}
.section-title {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 1.5px;
  margin-bottom: 26px;
  color: #333;
}
.cars-list {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  gap: 19px;
}
.car-card {
  display: flex;
  align-items: center;
  flex-direction: column;
  padding: 10px 16px;
  background: #f8fefa;
  border-radius: 11px;
  box-shadow: 0 2px 10px rgba(67,203,131,0.02);
  gap: 19px;
  min-width: 250px;
  max-width: 350px;
}
.car-img {
  width: 400px;
  height: 300px;
  object-fit: cover;
  box-shadow: 0 1px 8px rgba(67,203,131,0.15);
  background: #eeeeee;
}
.car-meta {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
}
.car-model {
  font-weight: 600;
  font-size: 22px;
  color: #222;
  display: flex;
  white-space: nowrap;
}
.car-info {
  font-size: 20px;
  color: #617173;
  display: flex;
  white-space: nowrap;
}
.section.sec-fav {
  margin-top: 20px;
}
.fav-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.fav-item {
  padding: 10px 16px;
  border-radius: 8px;
  background: #f7fcfa;
  box-shadow: 0 1px 7px rgba(67,203,131,0.07);
  display: flex;
  flex-direction: column;
}
.fav-title {
  font-size: 16px;
  font-weight: 500;
  color: #21785d;
}
.fav-meta {
  font-size: 14px;
  color: #6a6a6a;
  margin-top: 3px;
}
@media (max-width: 900px) {
  .userinfor-root {
    flex-direction: column;
    padding: 0 2vw 20px 2vw;
    gap: 24px;
  }
  .userinfo-sidebar,
  .userinfo-main {
    border-radius: 10px;
    box-shadow: 0 1px 6px rgba(0,0,0,0.11);
  }
  .userinfo-sidebar {
    width: 98vw;
    max-width: 400px;
    margin: 0 auto;
  }
}
</style>
