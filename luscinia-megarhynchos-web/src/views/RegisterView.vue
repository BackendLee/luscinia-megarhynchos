<script lang="ts" setup>
import axios from 'axios'
import { reactive } from 'vue'

const apiRoot = 'http://localhost:8080/yggdrasil'

const lowlro = reactive({
  url: {
    captcha: apiRoot + '/extension/api/user/register/captcha',
    register: apiRoot + '/extension/api/user/register'
  },
  data: {
    email: '',
    password: '',
    captcha: ''
  },
  temp: {
    password: ''
  }
})

declare const lightyear: any

function captcha() {
  lightyear.loading('show')
  axios
    .post(lowlro.url.captcha, {
      email: lowlro.data.email
    })
    .then(function (response) {
      lightyear.loading('hide')
      if (response.data['message'] === '邮件发送成功，请注意查收。') {
        lightyear.notify(response.data['message'], 'success', 1000)
      } else {
        lightyear.notify(response.data['message'], 'warning', 1000)
      }
    })
    .catch(function (response) {
      lightyear.loading('hide')
      lightyear.notify(response.response.data, 'warning', 1000)
    })
}

function register() {
  lightyear.loading('show')
  if (
    lowlro.data.email === '' &&
    lowlro.data.password === '' &&
    lowlro.data.captcha === '' &&
    lowlro.temp.password === ''
  ) {
    lightyear.loading('hide')
    lightyear.notify('请认真填写所有的信息以进行注册', 'warning', 1000)
  } else {
    if (lowlro.data.password !== lowlro.temp.password) {
      lightyear.loading('hide')
      lightyear.notify('两次输入的密码不一致', 'warning', 1000)
    } else {
      axios
        .post(lowlro.url.register, {
          email: lowlro.data.email,
          password: lowlro.data.password,
          captcha: lowlro.data.captcha
        })
        .then(function (response) {
          lightyear.loading('hide')
          if (response.data['mail'] === lowlro.data.email) {
            lightyear.notify('注册成功，页面即将自动跳转~', 'success', 1000)
            localStorage.setItem('user', JSON.stringify(response.data))
            setTimeout(() => {
              window.location.assign('/user')
            }, 1000)
          } else {
            lightyear.notify(response.data['message'], 'warning', 1000)
          }
        })
        .catch(function (response) {
          lightyear.loading('hide')
          lightyear.notify(response.response.data, 'warning', 1000)
        })
    }
  }
}

function login() {
  window.location.assign('/login')
}
</script>
<template>
  <div class="row lowlro-wrapper">
    <div class="lowlro">
      <div class="lowlro-center">
        <!--logo-->
        <div class="lowlro-header text-center">
          <a href="/">
            <img class="lowlro-logo" alt="" src="/img/lusciniamegarhynchoslogo.png" />
          </a>
        </div>
        <!--End logo-->

        <!--标题-->
        <div class="text-center">
          <h3>欢迎使用</h3>
        </div>
        <!--End 标题-->

        <hr />
        &nbsp;

        <!--邮箱-->
        <div class="form-group has-feedback feedback-left">
          <input v-model="lowlro.data.email" type="text" placeholder="邮箱" class="form-control" />
          <span class="mdi mdi-account form-control-feedback" aria-hidden="true"></span>
        </div>
        <!--End 邮箱-->

        <!--密码-->
        <div class="form-group has-feedback feedback-left">
          <input
            v-model="lowlro.data.password"
            type="password"
            placeholder="密码"
            class="form-control"
          />
          <span class="mdi mdi-lock form-control-feedback" aria-hidden="true"></span>
        </div>
        <!--End 密码-->

        <!--重复-->
        <div class="form-group has-feedback feedback-left">
          <input
            v-model="lowlro.temp.password"
            type="password"
            placeholder="重复"
            class="form-control"
          />
          <span class="mdi mdi-lock form-control-feedback" aria-hidden="true"></span>
        </div>
        <!--End 重复-->

        <!--验证-->
        <div class="form-group has-feedback feedback-left row">
          <div class="col-xs-7">
            <input
              v-model="lowlro.data.captcha"
              type="text"
              class="form-control"
              placeholder="验证码"
            />
            <span class="mdi mdi-check-all form-control-feedback" aria-hidden="true"></span>
          </div>
          <div class="col-xs-5">
            <button v-on:click="captcha" class="btn btn-info pull-right lowlro-captcha">
              获取验证码
            </button>
          </div>
        </div>
        <!--End 验证-->

        <!--跳转-->
        <div class="form-group has-feedback feedback-left row" style="width: 391px">
          <div class="col-xs-6">
            <button class="btn btn-link pull-left" v-on:click="login">登录账户</button>
          </div>
          <div class="col-xs-6">
            <button class="btn btn-link pull-right"></button>
          </div>
        </div>
        <!--End 跳转-->

        <!--注册-->
        <div class="form-group">
          <button v-on:click="register" class="btn btn-block btn-primary">立即注册</button>
        </div>
        <!--End 注册-->

        <hr />

        <!--尾部版权-->
        <footer class="col-sm-12 text-center">
          <p class="m-b-0">
            Copyright © 2023 <a href="" onclick="return false">lowlro</a>. All right reserved
          </p>
        </footer>
        <!--End 尾部版权-->
      </div>
    </div>
  </div>
</template>
<style scoped>
.lowlro-wrapper {
  position: relative;
}
.lowlro {
  display: flex !important;
  min-height: 100vh;
  align-items: center !important;
  justify-content: center !important;
}
.lowlro:after {
  content: '';
  min-height: inherit;
  font-size: 0;
}
.lowlro-center {
  background: #fff;
  min-width: 29.25rem;
  padding: 2.14286em 3.57143em;
  border-radius: 3px;
  margin: 2.85714em;
}
.lowlro-header {
  margin-bottom: 1.5rem !important;
}
.lowlro-center .has-feedback.feedback-left .form-control {
  padding-left: 38px;
  padding-right: 12px;
}
.lowlro-center .has-feedback.feedback-left .form-control-feedback {
  left: 0;
  right: auto;
  width: 38px;
  height: 38px;
  line-height: 38px;
  z-index: 4;
  color: #dcdcdc;
}
.lowlro-center .has-feedback.feedback-left.row .form-control-feedback {
  left: 15px;
}
.lowlro-logo {
  width: 210px;
  height: 36px;
  object-fit: cover;
}
.lowlro-captcha {
  width: 120px;
  height: 38px;
}
</style>
