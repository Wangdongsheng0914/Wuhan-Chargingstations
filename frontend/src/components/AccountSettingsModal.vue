<template>
  <transition name="slide-left">
    <div v-if="visible" class="account-settings-modal" @click.self="$emit('close')">
      <div class="modal-container">
        <!-- 右上角返回按钮 -->
        <button class="close-btn" @click="$emit('close')">
          ←
        </button>

        <!-- 弹窗内容 -->
        <div class="modal-content">
          <div class="modal-header">
            <h2>账号设置</h2>
          </div>

          <div class="modal-body">
            <!-- 用户名部分 -->
            <div class="info-section">
              <div class="section-header">
                <h3>用户名</h3>
                <button 
                  v-if="!editingUsername" 
                  class="edit-btn" 
                  @click="startEditUsername"
                >
                  修改
                </button>
                <div v-else class="edit-actions">
                  <button class="cancel-btn" @click="cancelEditUsername">取消</button>
                  <button class="save-btn" @click="saveUsername">保存</button>
                </div>
              </div>
              <div v-if="!editingUsername" class="info-display">
                {{ user.username }}
              </div>
              <div v-else class="edit-form">
                <div class="form-group">
                  <label>原用户名：</label>
                  <input 
                    type="text" 
                    v-model="usernameForm.oldUsername" 
                    placeholder="请输入原用户名"
                    class="form-input"
                  />
                </div>
                <div class="form-group">
                  <label>新用户名：</label>
                  <input 
                    type="text" 
                    v-model="usernameForm.newUsername" 
                    placeholder="请输入新用户名"
                    class="form-input"
                  />
                </div>
                <div class="form-group">
                  <label>密码验证：</label>
                  <input 
                    type="password" 
                    v-model="usernameForm.password" 
                    placeholder="请输入密码以验证"
                    class="form-input"
                  />
                </div>
                <div v-if="usernameError" class="error-message">{{ usernameError }}</div>
              </div>
            </div>

            <!-- 密码部分 -->
            <div class="info-section">
              <div class="section-header">
                <h3>密码</h3>
                <button 
                  v-if="!editingPassword" 
                  class="edit-btn" 
                  @click="startEditPassword"
                >
                  修改
                </button>
                <div v-else class="edit-actions">
                  <button class="cancel-btn" @click="cancelEditPassword">取消</button>
                  <button class="save-btn" @click="savePassword">保存</button>
                </div>
              </div>
              <div v-if="!editingPassword" class="info-display">
                ********
              </div>
              <div v-else class="edit-form">
                <div class="form-group">
                  <label>原密码：</label>
                  <input 
                    type="password" 
                    v-model="passwordForm.oldPassword" 
                    placeholder="请输入原密码"
                    class="form-input"
                  />
                </div>
                <div class="form-group">
                  <label>新密码：</label>
                  <input 
                    type="password" 
                    v-model="passwordForm.newPassword" 
                    placeholder="请输入新密码"
                    class="form-input"
                  />
                </div>
                <div class="form-group">
                  <label>确认新密码：</label>
                  <input 
                    type="password" 
                    v-model="passwordForm.confirmPassword" 
                    placeholder="请再次输入新密码"
                    class="form-input"
                  />
                </div>
                <div v-if="passwordError" class="error-message">{{ passwordError }}</div>
              </div>
            </div>

            <!-- 邮箱部分 -->
            <div class="info-section">
              <div class="section-header">
                <h3>邮箱</h3>
              </div>
              <div class="info-display">
                {{ user.email || '未设置' }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script>
import { updateUsername, updatePassword } from '../api/auth.js';

export default {
  name: 'AccountSettingsModal',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    user: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      editingUsername: false,
      editingPassword: false,
      usernameForm: {
        oldUsername: '',
        newUsername: '',
        password: ''
      },
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      usernameError: '',
      passwordError: ''
    };
  },
  methods: {
    startEditUsername() {
      this.editingUsername = true;
      this.usernameForm.oldUsername = this.user.username;
      this.usernameForm.newUsername = '';
      this.usernameForm.password = '';
      this.usernameError = '';
    },
    cancelEditUsername() {
      this.editingUsername = false;
      this.usernameForm = {
        oldUsername: '',
        newUsername: '',
        password: ''
      };
      this.usernameError = '';
    },
    async saveUsername() {
      this.usernameError = '';
      
      // 验证输入
      if (!this.usernameForm.oldUsername || !this.usernameForm.newUsername || !this.usernameForm.password) {
        this.usernameError = '请填写所有字段';
        return;
      }

      if (this.usernameForm.newUsername === this.usernameForm.oldUsername) {
        this.usernameError = '新用户名不能与原用户名相同';
        return;
      }

      try {
        await updateUsername(
          this.usernameForm.oldUsername,
          this.usernameForm.newUsername,
          this.usernameForm.password
        );

        // 更新本地存储的用户信息
        let userToUpdate = JSON.parse(localStorage.getItem('user'));
        userToUpdate.username = this.usernameForm.newUsername;
        localStorage.setItem('user', JSON.stringify(userToUpdate));

        // 通知父组件更新
        this.$emit('username-updated', this.usernameForm.newUsername);

        // 重置表单并关闭编辑
        this.cancelEditUsername();
        alert('用户名修改成功！');
      } catch (error) {
        // 处理错误信息
        let errorMsg = '修改失败，请检查输入信息';
        if (error.response && error.response.data) {
          errorMsg = typeof error.response.data === 'string' 
            ? error.response.data 
            : error.response.data.message || errorMsg;
        } else if (error.message) {
          errorMsg = error.message;
        }
        this.usernameError = errorMsg;
      }
    },
    startEditPassword() {
      this.editingPassword = true;
      this.passwordForm = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      };
      this.passwordError = '';
    },
    cancelEditPassword() {
      this.editingPassword = false;
      this.passwordForm = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      };
      this.passwordError = '';
    },
    async savePassword() {
      this.passwordError = '';
      
      // 验证输入
      if (!this.passwordForm.oldPassword || !this.passwordForm.newPassword || !this.passwordForm.confirmPassword) {
        this.passwordError = '请填写所有字段';
        return;
      }

      if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
        this.passwordError = '两次输入的密码不一致';
        return;
      }

      if (this.passwordForm.newPassword.length < 6) {
        this.passwordError = '新密码长度至少为6位';
        return;
      }

      try {
        await updatePassword(
          this.user.username,
          this.passwordForm.oldPassword,
          this.passwordForm.newPassword
        );

        // 重置表单并关闭编辑
        this.cancelEditPassword();
        alert('密码修改成功！');
      } catch (error) {
        // 处理错误信息
        let errorMsg = '修改失败，请检查原密码是否正确';
        if (error.response && error.response.data) {
          errorMsg = typeof error.response.data === 'string' 
            ? error.response.data 
            : error.response.data.message || errorMsg;
        } else if (error.message) {
          errorMsg = error.message;
        }
        this.passwordError = errorMsg;
      }
    }
  },
  watch: {
    visible(newVal) {
      if (!newVal) {
        // 弹窗关闭时重置所有编辑状态
        this.cancelEditUsername();
        this.cancelEditPassword();
      }
    }
  }
};
</script>

<style scoped>
.account-settings-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: flex-start;
}

.modal-container {
  width: 350px;
  max-height: 85vh;
  min-height: 500px;
  background: #ffffff;
  box-shadow: 4px 0 20px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
  border-radius: 0 12px 12px 0;
  margin-top: auto;
  margin-bottom: auto;
}

/* 波浪背景装饰 */
.modal-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 150px;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1440 320'%3E%3Cpath fill='%231DE9B6' fill-opacity='0.2' d='M0,160L48,144C96,128,192,96,288,106.7C384,117,480,171,576,197.3C672,224,768,224,864,202.7C960,181,1056,139,1152,128C1248,117,1344,139,1392,149.3L1440,160L1440,0L1392,0C1344,0,1248,0,1152,0C1056,0,960,0,864,0C768,0,672,0,576,0C480,0,384,0,288,0C192,0,96,0,48,0L0,0Z'%3E%3C/path%3E%3C/svg%3E");
  background-size: cover;
  background-repeat: no-repeat;
  z-index: 0;
  border-radius: 0 12px 0 0;
}

.modal-container::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 150px;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1440 320'%3E%3Cpath fill='%231DE9B6' fill-opacity='0.2' d='M0,160L48,181.3C96,203,192,245,288,256C384,267,480,245,576,208C672,171,768,117,864,112C960,107,1056,149,1152,165.3C1248,181,1344,171,1392,165.3L1440,160L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z'%3E%3C/path%3E%3C/svg%3E");
  background-size: cover;
  background-repeat: no-repeat;
  z-index: 0;
  border-radius: 0 0 12px 0;
}

.close-btn {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid #e1e1e1;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  transition: all 0.2s ease;
  font-size: 20px;
  font-weight: 1000;
  color: #495057;
}

.close-btn:hover {
  background: #f1faff;
  border-color: #1DE9B6;
  color: #1DE9B6;
  transform: scale(1.1);
}

.modal-content {
  flex: 1;
  padding: 50px 30px 30px 30px;
  overflow-y: auto;
  position: relative;
  z-index: 1;
}

.modal-header {
  margin-bottom: 40px;
  text-align: center;
}

.modal-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #343a40;
  margin: 0;
}

.modal-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.info-section {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(29, 233, 182, 0.2);
  border-radius: 12px;
  padding: 10px;
  transition: all 0.3s ease;
}

.info-section:hover {
  border-color: rgba(29, 233, 182, 0.4);
  box-shadow: 0 4px 12px rgba(29, 233, 182, 0.15);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e9ecef;
}

.section-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: #343a40;
  margin: 0;
}

.edit-btn, .save-btn, .cancel-btn {
  padding: 8px 20px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.edit-btn {
  background-color: #1DE9B6;
  color: #00695C;
}

.edit-btn:hover {
  background-color: #00BFA5;
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(29, 233, 182, 0.3);
}

.save-btn {
  background-color: #1DE9B6;
  color: #00695C;
  margin-left: 10px;
}

.save-btn:hover {
  background-color: #00BFA5;
}

.cancel-btn {
  background-color: #6c757d;
  color: white;
}

.cancel-btn:hover {
  background-color: #5a6268;
}

.edit-actions {
  display: flex;
  gap: 8px;
}

.info-display {
  font-size: 18px;
  color: #495057;
  padding: 12px 0;
  word-break: break-word;
}

.edit-form {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-top: 10px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 14px;
  font-weight: 500;
  color: #495057;
}

.form-input {
  padding: 10px 15px;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  font-size: 15px;
  transition: all 0.2s ease;
}

.form-input:focus {
  outline: none;
  border-color: #1DE9B6;
  box-shadow: 0 0 0 3px rgba(29, 233, 182, 0.1);
}

.error-message {
  color: #dc3545;
  font-size: 14px;
  margin-top: 5px;
  padding: 8px 12px;
  background-color: #f8d7da;
  border: 1px solid #f5c6cb;
  border-radius: 6px;
}

/* 滑入动画 */
.slide-left-enter-active {
  transition: all 0.3s ease-out;
}

.slide-left-leave-active {
  transition: all 0.3s ease-in;
}

.slide-left-enter-from {
  transform: translateX(-100%);
  opacity: 0;
}

.slide-left-leave-to {
  transform: translateX(-100%);
  opacity: 0;
}

/* 响应式设计 */
@media (max-width: 600px) {
  .modal-container {
    width: 100%;
  }
}
</style>

