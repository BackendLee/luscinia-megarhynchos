<script lang="ts" setup>
import axios from 'axios'
import { reactive } from 'vue'

const apiRoot = 'http://localhost:8080/yggdrasil'

const lowlro = reactive({
  url: {
    login: apiRoot + '/extension/api/user/login'
  },
  data: {
    email: '',
    password: ''
  }
})

declare const lightyear: any

function login() {
  lightyear.loading('show')
  if (lowlro.data.email === '' && lowlro.data.password === '') {
    lightyear.loading('hide')
    lightyear.notify('请认真填写所有的信息以进行登录', 'warning', 1000)
  } else {
    axios
      .post(lowlro.url.login, {
        email: lowlro.data.email,
        password: lowlro.data.password
      })
      .then(function (response) {
        lightyear.loading('hide')
        if (response.data['mail'] === lowlro.data.email) {
          lightyear.notify('登录成功，页面即将自动跳转~', 'success', 1000)
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
function register() {
  window.location.assign('/register')
}
</script>
<template>
  <div class="row lowlro-wrapper">
    <div class="lowlro">
      <div class="lowlro-center">
        <!--logo-->
        <div class="lowlro-header text-center">
          <a href="/">
            <img alt="" class="lowlro-logo" src="/img/lusciniamegarhynchoslogo.png" />
          </a>
        </div>
        <!--End logo-->

        <!--标题-->
        <div class="text-center">
          <h3>欢迎回来</h3>
        </div>
        <!--End 标题-->

        <hr />
        &nbsp;

        <!--邮箱-->
        <div class="form-group has-feedback feedback-left">
          <input v-model="lowlro.data.email" class="form-control" placeholder="邮箱" type="text" />
          <span aria-hidden="true" class="mdi mdi-account form-control-feedback"></span>
        </div>
        <!--End 邮箱-->

        <!--密码-->
        <div class="form-group has-feedback feedback-left">
          <input
            v-model="lowlro.data.password"
            class="form-control"
            placeholder="密码"
            type="password"
          />
          <span aria-hidden="true" class="mdi mdi-lock form-control-feedback"></span>
        </div>
        <!--End 密码-->

        <!--跳转-->
        <div class="form-group has-feedback feedback-left row" style="width: 391px">
          <div class="col-xs-6">
            <button class="btn btn-link pull-left" v-on:click="register">注册账户</button>
          </div>
          <div class="col-xs-6"></div>
        </div>
        <!--End 跳转-->

        <!--登录-->
        <div class="form-group">
          <button v-on:click="login" class="btn btn-block btn-primary">立即登录</button>
        </div>
        <!--End 登录-->

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
</style>
