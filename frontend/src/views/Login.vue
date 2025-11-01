<template>
  <div class="auth-container">
    <!-- 左半屏幕 - 浅绿色背景 + 圆形图片 + SVG曲线 -->
    <div class="left-section">
      <svg class="curve-border" viewBox="0 0 100 100" preserveAspectRatio="none">
        <path d="M 0,0 L 100,0 Q 60,50 100,100 L 0,100 Z" fill="#00FF80"/>
      </svg>
      <div class="left-content">
        <div class="images-layout">
          <div class="image-circle large top-left">
            <img src="../image/656.jpg" alt="Welcome 1" />
          </div>
          <div class="image-circle medium right-center">
            <img src="../image/657.jpg" alt="Welcome 2" />
          </div>
          <div class="image-circle small bottom-left">
            <img src="../image/658.jpg" alt="Welcome 3" />
          </div>
        </div>
      </div>
    </div>

    <!-- 右半屏幕 - 登录表单 -->
    <div class="right-section">
      <div class="auth-card">
        <h2 class="auth-title">用户登录</h2>
        
        <form @submit.prevent="handleLogin" class="auth-form">
          <div class="form-group">
            <label for="username">用户名</label>
            <input
              type="text"
              id="username"
              v-model="formData.username"
              placeholder="请输入用户名"
              required
            />
          </div>

          <div class="form-group">
            <label for="password">密码</label>
            <input
              type="password"
              id="password"
              v-model="formData.password"
              placeholder="请输入密码"
              required
            />
          </div>

          <div class="form-group">
            <label for="captcha">验证码</label>
            <div class="captcha-container">
              <input
                type="text"
                id="captcha"
                v-model="captchaInput"
                placeholder="请输入验证码"
                required
              />
              <div @click="refreshCode" class="captcha-img">
                <val-with-nums :identify-code="identifyCode"></val-with-nums>
              </div>
            </div>
          </div>

          <button type="submit" class="btn-primary" :disabled="loading">
            {{ loading ? '登录中...' : '登录' }}
          </button>

          <div v-if="message" :class="['message', messageType]">
            {{ message }}
          </div>

          <div class="auth-footer">
            还没有账号？
            <router-link to="/register" class="link">立即注册</router-link>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import { login } from '../api/auth'
import ValWithNums from '../components/ValWithNums.vue'

export default {
  name: 'Login',
  components: {
    ValWithNums
  },
  data() {
    return {
      formData: {
        username: '',
        password: ''
      },
      loading: false,
      message: '',
      messageType: '',
      identifyCode: '',
      captchaInput: ''
    }
  },
  mounted() {
    this.refreshCode();
  },
  methods: {
    refreshCode() {
      this.identifyCode = ''
      this.makeCode(4)
    },
    makeCode(len) {
      const chars = 'ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678'
      let str = ''
      for (let i = 0; i < len; i++) {
        str += chars.charAt(Math.floor(Math.random() * chars.length))
      }
      this.identifyCode = str
    },
    async handleLogin() {
      if (this.captchaInput.toLowerCase() !== this.identifyCode.toLowerCase()) {
        this.message = '验证码错误'
        this.messageType = 'error'
        this.refreshCode()
        this.captchaInput = ''
        return
      }

      this.loading = true
      this.message = ''

      try {
        const response = await login(this.formData.username, this.formData.password)
        
        if (response.success) {
          this.messageType = 'success'
          this.message = response.message
          
          // 保存用户信息到 localStorage
          localStorage.setItem('user', JSON.stringify(response.user))
          
          // 跳转到地图页面
          setTimeout(() => {
            this.$router.push('/map')
          }, 1000)
        } else {
          this.messageType = 'error'
          this.message = response.message
        }
      } catch (error) {
        this.messageType = 'error'
        this.message = '登录失败，请检查网络连接'
        console.error('Login error:', error)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
/* 主容器 - 左右分屏 */
.auth-container {
  display: flex;
  min-height: 100vh;
  height: 100vh;
  width: 100vw;
  margin: 0;
  padding: 0;
  overflow: hidden;
}

/* 左半屏幕 - 浅绿色背景，带波浪曲线边界 */
.left-section {
  flex: 1.5;
  width: 60%;
  height: 100vh;
  position: relative;
  overflow: hidden;
}

/* SVG 曲线边界 */
.curve-border {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

/* 左侧内容容器 */
.left-content {
  position: relative;
  z-index: 2;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 图片布局容器 */
.images-layout {
  position: relative;
  width: 500px;
  height: 600px;
}

/* 圆形图片容器基础样式 */
.image-circle {
  position: absolute;
  border-radius: 50%;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
}

.image-circle img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 大图 - 左上 */
.image-circle.large {
  width: 350px;
  height: 350px;
  top: -60px;
  left: -160px;
  z-index: 3;
}

/* 中图 - 右中 */
.image-circle.medium {
  width: 300px;
  height: 300px;
  top: 50%;
  right: 0;
  transform: translateY(-50%);
  z-index: 2;
}

/* 小图 - 左下 */
.image-circle.small {
  width: 200px;
  height: 200px;
  bottom: 0;
  left: 50px;
  z-index: 1;
}

/* 右半屏幕 - 白色背景 */
.right-section {
  flex: 1;
  width: 40%;
  height: 100vh;
  background: white;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px;
  overflow-y: auto;
}

/* 登录卡片 - 占据右半屏幕约2/3高度 */
.auth-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  padding: 50px 40px;
  width: 100%;
  max-width: 450px;
  max-height: 100vh;
}

.auth-title {
  color: #333;
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 35px;
  text-align: center;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.form-group label {
  color: #555;
  font-size: 15px;
  font-weight: 600;
}

.form-group input {
  padding: 14px 18px;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  font-size: 15px;
  transition: all 0.3s;
}

.form-group input:focus {
  outline: none;
  border-color: black;
}

.btn-primary {
  background: #C0C0C0;
  color: black;
  border: none;
  padding: 16px;
  border-radius: 10px;
  font-size: 17px;
  font-weight: 700;
  cursor: pointer;
  margin-top: 15px;
}

.btn-primary:hover:not(:disabled) {
  background: #A0A0A0;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.message {
  padding: 0px;
  font-size: 14px;
  text-align: center;
}

.message.success {
  color: #155724;
}

.message.error {
  color: #721c24;
}


.auth-footer {
  text-align: center;
  color: green;
  font-size: 15px;
  font-weight:500;
}

.link {
  color: green;
  text-decoration: none;
  font-weight: 1000;
}

.link:hover {
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .auth-container {
    flex-direction: column;
  }
  
  .left-section {
    min-height: 40vh;
  }
  
  .image-circle {
    width: 200px;
    height: 200px;
  }
  
  .right-section {
    min-height: 60vh;
  }
  
  .auth-card {
    max-height: none;
  }
}

.captcha-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.captcha-container input {
  flex: 1;
}

.captcha-img {
  cursor: pointer;
  border-radius: 10px;
  overflow: hidden;
}
</style>

