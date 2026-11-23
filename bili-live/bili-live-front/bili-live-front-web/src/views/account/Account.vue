<template>
  <div>
    <Dialog
      :show="loginStore.showLogin"
      :buttons="dialogConfig.buttons"
      width="1000px"
      :showCancel="false"
      @close="closeDialog"
      :padding="0"
      :draggable="false"
      :top="100"
    >
      <div class="dialog-panel">
        <div class="bg">
          <img :src="proxy.Utils.getLocalImage('login_bg.png')" />
        </div>
        <el-form
          class="login-register"
          :model="formData"
          :rules="rules"
          ref="formDataRef"
        >
          <div class="tab-panel">
            <div :class="[opType == 0 ? '' : 'active']" @click="showPanel(1)">
              登录
            </div>
            <el-divider direction="vertical" />
            <div :class="[opType == 1 ? '' : 'active']" @click="showPanel(0)">
              注册
            </div>
          </div>
          <!--input输入-->
          <el-form-item prop="email">
            <el-input
              size="large"
              clearable
              placeholder="请输入邮箱"
              v-model="formData.email"
              maxLength="150"
            >
              <template #prefix>
                <span class="iconfont icon-account"></span>
              </template>
            </el-input>
          </el-form-item>
          <!--登录密码-->
          <el-form-item prop="password" v-if="opType == 1">
            <el-input
              show-password
              size="large"
              placeholder="请输入密码"
              v-model="formData.password"
            >
              <template #prefix>
                <span class="iconfont icon-password"></span>
              </template>
            </el-input>
          </el-form-item>
          <!--注册-->
          <div v-if="opType == 0">
            <el-form-item prop="nickName" v-if="opType == 0">
              <el-input
                size="large"
                clearable
                placeholder="请输入昵称"
                v-model="formData.nickName"
                maxLength="20"
              >
                <template #prefix>
                  <span class="iconfont icon-account"></span>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item prop="registerPassword">
              <el-input
                show-password
                type="password"
                size="large"
                placeholder="请输入密码"
                v-model="formData.registerPassword"
              >
                <template #prefix>
                  <span class="iconfont icon-password"></span>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item prop="reRegisterPassword">
              <el-input
                show-password
                type="password"
                size="large"
                placeholder="请再次输入密码"
                v-model="formData.reRegisterPassword"
              >
                <template #prefix>
                  <span class="iconfont icon-password"></span>
                </template>
              </el-input>
            </el-form-item>
            <!-- 邮箱验证码（新增：注册时必填） -->
            <el-form-item prop="emailCode">
              <div class="email-code-panel">
                <el-input
                  size="large"
                  placeholder="请输入邮箱验证码"
                  v-model="formData.emailCode"
                  maxlength="6"
                >
                  <template #prefix>
                    <span class="iconfont icon-checkcode"></span>
                  </template>
                </el-input>
                <el-button
                  size="large"
                  class="email-code-btn"
                  :disabled="emailCodeBtnDisabled"
                  @click="sendEmailCode"
                >
                  {{ emailCodeBtnText }}
                </el-button>
              </div>
            </el-form-item>
          </div>
          <el-form-item prop="checkCode">
            <div class="check-code-panel">
              <el-input
                size="large"
                placeholder="请输入验证码"
                v-model="formData.checkCode"
                @keyup.enter="doSubmit"
              >
                <template #prefix>
                  <span class="iconfont icon-checkcode"></span>
                </template>
              </el-input>
              <img
                :src="checkCodeInfo.checkCode"
                class="check-code"
                @click="changeCheckCode()"
              />
            </div>
          </el-form-item>
          <el-form-item class="bottom-btn">
            <el-button
              type="primary"
              size="large"
              class="login-btn"
              @click="doSubmit"
            >
              <span v-if="opType == 0">注册</span>
              <span v-if="opType == 1">登录</span>
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </Dialog>
  </div>
</template>

<script setup>
import {
  ref,
  reactive,
  getCurrentInstance,
  nextTick,
  onMounted,
  onUpdated,
} from "vue";
import { useRouter, useRoute } from "vue-router";
import md5 from "js-md5";
const { proxy } = getCurrentInstance();
const router = useRouter();
const route = useRoute();

import { useLoginStore } from "@/stores/loginStore.js";
const loginStore = useLoginStore();

//验证码
const checkCodeInfo = ref({});
const changeCheckCode = async () => {
  let result = await proxy.Request({
    url: proxy.Api.checkCode,
  });
  if (!result) {
    return;
  }
  checkCodeInfo.value = result.data;
};

//登录，注册 弹出配置
const dialogConfig = ref({
  show: true,
});

const checkRePassword = (rule, value, callback) => {
  if (value !== formData.value.registerPassword) {
    callback(new Error(rule.message));
  } else {
    callback();
  }
};

// 0:注册 1:登录
const opType = ref(1);
const formData = ref({});
const formDataRef = ref();
const rules = {
  email: [
    { required: true, message: "请输入邮箱" },
    { validator: proxy.Verify.email, message: "请输入正确的邮箱" },
  ],
  password: [{ required: true, message: "请输入密码" }],
  nickName: [{ required: true, message: "请输入昵称" }],
  registerPassword: [
    { required: true, message: "请输入密码" },
    {
      validator: proxy.Verify.password,
      message: "密码只能是数字，字母，特殊字符 8-18位",
    },
  ],
  reRegisterPassword: [
    { required: true, message: "请再次输入密码" },
    {
      validator: checkRePassword,
      message: "两次输入的密码不一致",
    },
  ],
  checkCode: [{ required: true, message: "请输入图片验证码" }],
  emailCode: [{ required: true, message: "请输入邮箱验证码" }],
};

// ============ 邮箱验证码（注册功能）============
const emailCodeBtnDisabled = ref(false);
const emailCodeBtnText = ref("获取验证码");
let emailCodeTimer = null;

// 本地 QQ 邮箱格式校验：QQ 邮箱为纯数字@qq.com
const isQQEmail = (email) => {
  return /^[1-9]\d{4,10}@qq\.com$/i.test(email || "");
};

// 点击「获取验证码」
const sendEmailCode = async () => {
  const email = (formData.value.email || "").trim();
  if (!email) {
    proxy.Message.warning("请先输入邮箱");
    return;
  }
  if (!isQQEmail(email)) {
    proxy.Message.warning("目前只支持QQ邮箱注册");
    return;
  }
  let result = await proxy.Request({
    url: proxy.Api.sendEmailCode,
    params: { email: email },
  });
  if (!result) {
    return;
  }
  proxy.Message.success("验证码已发送，请查收邮件");

  // 启动 60 秒倒计时，防止重复点击
  emailCodeBtnDisabled.value = true;
  let seconds = 60;
  emailCodeBtnText.value = seconds + "s后重发";
  emailCodeTimer = setInterval(() => {
    seconds--;
    if (seconds <= 0) {
      clearInterval(emailCodeTimer);
      emailCodeTimer = null;
      emailCodeBtnDisabled.value = false;
      emailCodeBtnText.value = "获取验证码";
    } else {
      emailCodeBtnText.value = seconds + "s后重发";
    }
  }, 1000);
};

//重置表单
const resetForm = () => {
  changeCheckCode();
  // 重置邮箱验证码按钮状态并清理倒计时
  if (emailCodeTimer) {
    clearInterval(emailCodeTimer);
    emailCodeTimer = null;
  }
  emailCodeBtnDisabled.value = false;
  emailCodeBtnText.value = "获取验证码";
  nextTick(() => {
    formDataRef.value.resetFields();
    formData.value = {};
  });
};

// 登录、注册、重置密码  提交表单
const doSubmit = () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }
    let params = {};
    Object.assign(params, formData.value);
    params.checkCodeKey = checkCodeInfo.value.checkCodeKey;
    //登录
    if (opType.value == 1) {
      params.password = md5(params.password).toUpperCase();
    }
    let result = await proxy.Request({
      url: opType.value == 0 ? proxy.Api.register : proxy.Api.login,
      params: params,
      errorCallback: () => {
        changeCheckCode();
      },
    });
    if (!result) {
      return;
    }
    //注册返回
    if (opType.value == 0) {
      proxy.Message.success("注册成功,请登录");
      showPanel(1);
    } else if (opType.value == 1) {
      proxy.Message.success("登录成功");
      loginStore.setLogin(false);
      loginStore.saveUserInfo(result.data);
    }
  });
};

const closeDialog = () => {
  dialogConfig.value.show = false;
  loginStore.setLogin(false);
};

const showPanel = (type) => {
  opType.value = type;
  if (loginStore.showLogin) {
    resetForm();
  }
};

onUpdated(() => {
  showPanel(1);
});

onMounted(() => {
  showPanel(1);
});
</script>

<style lang="scss">
.dialog-panel {
  display: flex;
  align-items: center;
  justify-content: space-around;
  .bg {
    width: 450px;
    height: 580px;
    overflow: hidden;
    img {
      width: 100%;
    }
  }
  .login-register {
    width: 350px;
    .tab-panel {
      margin: 10px auto;
      display: flex;
      width: 130px;
      font-size: 18px;
      align-items: center;
      justify-content: space-around;
      cursor: pointer;
      .active {
        color: var(--blue2);
      }
    }
    .no-account {
      width: 100%;
      display: flex;
      justify-content: space-between;
    }
    .login-btn {
      width: 100%;
    }
    .bottom-btn {
      margin-bottom: 0px;
    }
  }
}

.check-code-panel {
  display: flex;
  .check-code {
    margin-left: 5px;
    cursor: pointer;
  }
}

.email-code-panel {
  display: flex;
  width: 100%;
  .email-code-btn {
    margin-left: 5px;
    width: 120px;
    flex-shrink: 0;
  }
}
</style>