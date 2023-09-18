<script lang="ts" setup>
let user = JSON.parse(localStorage.getItem('user')!)
let length: number

if (user['characters'][0]['uuid'] == '') {
  length = 0
} else {
  length = 1
}

import { reactive } from 'vue'
import axios from 'axios'

const character = reactive({
  uuid: user['characters'][0]['uuid'],
  name: {
    value: user['characters'][0]['name'],
    flag: {
      red: {
        value: '',
        status: false
      },
      green: {
        value: '',
        status: false
      }
    }
  },
  model: {
    value: user['characters'][0]['model'],
    flag: {
      steve: user['characters'][0]['model'] === 'steve',
      alex: user['characters'][0]['model'] === 'alex'
    }
  },
  texture: {
    skin: user['characters'][0]['texture']['skin'],
    cape: user['characters'][0]['texture']['cape']
  }
})

function steve() {
  character.model.value = 'alex'
  character.model.flag.steve = false
  character.model.flag.alex = true
}

function alex() {
  character.model.value = 'steve'
  character.model.flag.steve = true
  character.model.flag.alex = false
}

function exit() {
  localStorage.clear()
  window.location.assign('/login')
}

declare const lightyear: any

function apiProfilesMinecraft() {
  lightyear.loading('show')
  axios
    .post('http://localhost:8080/yggdrasil/api/profiles/minecraft', [character.name.value])
    .then(function (response) {
      lightyear.loading('hide')
      if (response.data.length === 1) {
        lightyear.notify('该名称已被注册或不符合要求', 'danger', 3000)
        character.name.flag.red.status = true
        character.name.flag.green.status = false
        character.name.flag.red.value = character.name.value
      } else {
        lightyear.notify('你可以使用该名称,点击创建按钮即可完成.', 'success', 3000)
        character.name.flag.red.status = false
        character.name.flag.green.status = true
        character.name.flag.green.value = character.name.value
      }
    })
    .catch(function (response) {
      lightyear.loading('hide')
      lightyear.notify(response.response.data, 'warning', 1000)
    })
}

function extensionCreateCharacter() {
  lightyear.loading('show')
  axios
    .post('http://localhost:8080/yggdrasil/extension/create/character', {
      uid: user['_id'],
      accessToken: user['password'],
      name: character.name.value
    })
    .then(function (response) {
      lightyear.loading('hide')
      lightyear.notify('创建成功，页面即将自动跳转~', 'success', 1000)
      localStorage.setItem('user', JSON.stringify(response.data))
      setTimeout(() => {
        window.location.assign('/user')
      }, 1000)
    })
    .catch(function (response) {
      lightyear.loading('hide')
      lightyear.notify(response.response.data, 'warning', 1000)
    })
}

function extensionUpdateCharacterSkin() {
  lightyear.loading('show')
  axios
    .post('http://localhost:8080/yggdrasil/extension/update/character/skin', {
      uid: user['_id'],
      accessToken: user['password'],
      model: character.model.value,
      texturesSkin: character.texture.skin
    })
    .then(function (response) {
      lightyear.loading('hide')
      lightyear.notify('修改成功，页面即将自动跳转~', 'success', 1000)
      localStorage.setItem('user', JSON.stringify(response.data))
      setTimeout(() => {
        window.location.assign('/user')
      }, 1000)
    })
    .catch(function (response) {
      lightyear.loading('hide')
      lightyear.notify(response.response.data, 'warning', 1000)
    })
}

function extensionUpdateCharacterCape() {
  lightyear.loading('show')
  axios
    .post('http://localhost:8080/yggdrasil/extension/update/character/cape', {
      uid: user['_id'],
      accessToken: user['password'],
      texturesCape: character.texture.cape
    })
    .then(function (response) {
      lightyear.loading('hide')
      lightyear.notify('修改成功，页面即将自动跳转~', 'success', 1000)
      localStorage.setItem('user', JSON.stringify(response.data))
      setTimeout(() => {
        window.location.assign('/user')
      }, 1000)
    })
    .catch(function (response) {
      lightyear.loading('hide')
      lightyear.notify(response.response.data, 'warning', 1000)
    })
}
</script>
<template>
  <!--导航内容-->
  <div class="lyear-layout-web">
    <div class="lyear-layout-container">
      <!--左侧导航-->
      <aside class="lyear-layout-sidebar">
        <div id="logo" class="sidebar-header">
          <a href="/"><img alt="" class="lowlro-logo" src="/img/lusciniamegarhynchoslogo.png" /></a>
        </div>
        <div class="lyear-layout-sidebar-scroll">
          <nav class="sidebar-main">
            <ul class="nav nav-drawer">
              <li class="nav-item active">
                <a href="" onclick="return false"><i class="mdi mdi-home"></i> 首页</a>
              </li>
            </ul>
          </nav>
          <div class="sidebar-footer">
            <p class="m-b-0">
              Copyright © 2023 <a href="" onclick="return false">lowlro</a>. All right reserved
            </p>
          </div>
        </div>
      </aside>
      <!--End 左侧导航-->

      <!--头部信息-->
      <header class="lyear-layout-header">
        <nav class="navbar navbar-default">
          <div class="topbar">
            <div class="topbar-left">
              <span class="navbar-page-title"> 首页 </span>
            </div>
            <ul class="topbar-right">
              <li class="dropdown dropdown-profile">
                <a data-toggle="dropdown" href="" onclick="return false">
                  <img alt="" class="img-avatar img-avatar-48 m-r-10" src="/img/lusciniamegarhynchoslogo.png" />
                  <span> {{ user['mail'] }} <span class="caret"></span></span>
                </a>
                <ul class="dropdown-menu dropdown-menu-right">
                  <li>
                    <a href="" onclick="return false" @click="exit"
                      ><i class="mdi mdi-logout-variant"></i> 退出登录</a
                    >
                  </li>
                </ul>
              </li>
            </ul>
          </div>
        </nav>
      </header>
      <!--End 头部信息-->
    </div>
  </div>
  <!--End 导航内容-->

  <!--页面内容-->
  <main class="lyear-layout-content">
    <div class="container-fluid">
      <!--卡片视图-->
      <div class="row">
        <div class="col-sm-6 col-lg-3">
          <div class="card bg-primary">
            <div class="card-body clearfix">
              <div class="pull-right">
                <p class="h6 text-white m-t-0">账户余额</p>
                <p class="h3 text-white m-b-0">0 元</p>
              </div>
              <div class="pull-left">
                <span class="img-avatar img-avatar-48 bg-translucent"
                  ><i class="mdi mdi-currency-cny fa-1-5x"></i
                ></span>
              </div>
            </div>
          </div>
        </div>
        <div class="col-sm-6 col-lg-3">
          <div class="card bg-danger">
            <div class="card-body clearfix">
              <div class="pull-right">
                <p class="h6 text-white m-t-0">角色总数</p>
                <p class="h3 text-white m-b-0">{{ length }} 个</p>
              </div>
              <div class="pull-left">
                <span class="img-avatar img-avatar-48 bg-translucent"
                  ><i class="mdi mdi-account fa-1-5x"></i
                ></span>
              </div>
            </div>
          </div>
        </div>
        <div class="col-sm-6 col-lg-3">
          <div class="card bg-success">
            <div class="card-body clearfix">
              <div class="pull-right">
                <p class="h6 text-white m-t-0">下载总量</p>
                <p class="h3 text-white m-b-0">0 次</p>
              </div>
              <div class="pull-left">
                <span class="img-avatar img-avatar-48 bg-translucent"
                  ><i class="mdi mdi-arrow-down-bold fa-1-5x"></i
                ></span>
              </div>
            </div>
          </div>
        </div>
        <div class="col-sm-6 col-lg-3">
          <div class="card bg-purple">
            <div class="card-body clearfix">
              <div class="pull-right">
                <p class="h6 text-white m-t-0">新增留言</p>
                <p class="h3 text-white m-b-0">0 条</p>
              </div>
              <div class="pull-left">
                <span class="img-avatar img-avatar-48 bg-translucent"
                  ><i class="mdi mdi-comment-outline fa-1-5x"></i
                ></span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <!--End 卡片视图-->

      <!--地址公示-->
      <div class="row">
        <div class="col-xs-12">
          <div class="card">
            <div class="card-header">
              <h4>Yggdrasil API Root 地址公示 <small>外置登录 ( authlib-injector )</small></h4>
            </div>
            <div class="card-body">
              <p>本站的 Yggdrasil API 地址: <code>http://localhost:8080/yggdrasil/</code></p>
            </div>
          </div>
        </div>
      </div>
      <!--End 地址公示-->

      <!--创建角色-->
      <div class="row">
        <div class="col-xs-12">
          <div class="card">
            <div class="card-header">
              <h5>创建角色</h5>
            </div>
            <div class="card-body">
              <div v-if="length === 0" class="form-inline">
                <div
                  :class="
                    character.name.flag.red.status &&
                    character.name.flag.red.value === character.name.value
                      ? 'has-error'
                      : character.name.flag.green.status &&
                        character.name.flag.green.value === character.name.value
                      ? 'has-success'
                      : ''
                  "
                  class="form-group"
                >
                  <input
                    v-model="character.name.value"
                    class="form-control"
                    placeholder="请输入名称.."
                    style="width: 256px"
                  />
                  &nbsp;
                  <button
                    v-if="
                      !(
                        character.name.flag.red.status &&
                        character.name.flag.red.value === character.name.value
                      ) &&
                      !(
                        character.name.flag.green.status &&
                        character.name.flag.green.value === character.name.value
                      )
                    "
                    class="btn btn-label btn-info"
                    @click="apiProfilesMinecraft"
                  >
                    <label><i class="mdi mdi-magnify"></i></label>
                    检查名称
                  </button>
                  <button
                    v-if="
                      character.name.flag.red.status &&
                      character.name.flag.red.value === character.name.value
                    "
                    class="btn btn-label btn-danger"
                  >
                    <label><i class="mdi mdi-close"></i></label>
                    名称无效
                  </button>
                  <button
                    v-if="
                      character.name.flag.green.status &&
                      character.name.flag.green.value === character.name.value
                    "
                    class="btn btn-label btn-primary"
                    @click="extensionCreateCharacter"
                  >
                    <label><i class="mdi mdi-checkbox-marked-circle-outline"></i></label>
                    创建角色
                  </button>
                </div>
              </div>
              <h6 v-if="length === 1">你无法拥有更多角色</h6>
            </div>
          </div>
        </div>
      </div>
      <!--End 创建角色-->

      <!--更新皮肤-->
      <div class="row">
        <div class="col-xs-12">
          <div class="card">
            <div class="card-header">
              <h5>更新皮肤</h5>
            </div>
            <div class="card-body">
              <h6 v-if="length === 0">请先创建角色</h6>
              <div v-if="length === 1" class="form-inline">
                <input
                  v-model="character.texture.skin"
                  class="form-control"
                  placeholder="请输入网址.."
                  style="width: 355px"
                />
                &nbsp;
                <div v-if="character.model.value === ''" class="btn-group">
                  <button
                    aria-expanded="false"
                    aria-haspopup="true"
                    class="btn btn-label btn-warning dropdown-toggle"
                    data-toggle="dropdown"
                    type="button"
                  >
                    <label><i class="mdi mdi-help-circle-outline"></i></label>模型
                    <span class="caret"></span>
                  </button>
                  <ul class="dropdown-menu">
                    <li>
                      <a href="" onclick="return false" @click="alex"
                        ><i class="mdi mdi-gender-male"></i> Steve</a
                      >
                    </li>
                    <li>
                      <a href="" onclick="return false" @click="steve"
                        ><i class="mdi mdi-gender-female"></i> Alex</a
                      >
                    </li>
                  </ul>
                </div>
                <div v-if="character.model.flag.steve" class="btn-group">
                  <button
                    aria-expanded="false"
                    aria-haspopup="true"
                    class="btn btn-label btn-cyan dropdown-toggle"
                    data-toggle="dropdown"
                    type="button"
                  >
                    <label><i class="mdi mdi-gender-male"></i></label>Steve
                    <span class="caret"></span>
                  </button>
                  <ul class="dropdown-menu">
                    <li>
                      <a href="" onclick="return false" @click="steve"
                        ><i class="mdi mdi-gender-female"></i> Alex</a
                      >
                    </li>
                  </ul>
                </div>
                <div v-if="character.model.flag.alex" class="btn-group">
                  <button
                    aria-expanded="false"
                    aria-haspopup="true"
                    class="btn btn-label btn-pink dropdown-toggle"
                    data-toggle="dropdown"
                    type="button"
                  >
                    <label><i class="mdi mdi-gender-female"></i></label>Alex
                    <span class="caret"></span>
                  </button>
                  <ul class="dropdown-menu">
                    <li>
                      <a href="" onclick="return false" @click="alex"
                        ><i class="mdi mdi-gender-male"></i> Steve</a
                      >
                    </li>
                  </ul>
                </div>
                &nbsp;
                <button
                  v-if="
                    character.texture.skin !== user['characters'][0]['texture']['skin'] ||
                    character.model.value !== user['characters'][0]['model']
                  "
                  class="btn btn-label btn-primary"
                  @click="extensionUpdateCharacterSkin"
                >
                  <label><i class="mdi mdi-checkbox-marked-circle-outline"></i></label>
                  提交变更
                </button>
              </div>
              &nbsp;
              <div v-if="length === 1">
                &nbsp;
                <img :src="character.texture.skin" style="height: 64px; width: 64px" alt="" />
              </div>
            </div>
          </div>
        </div>
      </div>
      <!--End 更新皮肤-->

      <!--更新披风-->
      <div class="row">
        <div class="col-xs-12">
          <div class="card">
            <div class="card-header">
              <h5>更新披风</h5>
            </div>
            <div class="card-body">
              <h6 v-if="length === 0">请先创建角色</h6>
              <div v-if="length === 1" class="form-inline">
                <input
                  v-model="character.texture.cape"
                  class="form-control"
                  placeholder="请输入网址.."
                  style="width: 750px"
                />
                &nbsp;
                <button
                  v-if="character.texture.cape !== user['characters'][0]['texture']['cape']"
                  class="btn btn-label btn-primary"
                  @click="extensionUpdateCharacterCape"
                >
                  <label><i class="mdi mdi-checkbox-marked-circle-outline"></i></label>
                  提交变更
                </button>
              </div>
              &nbsp;
              <div v-if="length === 1">
                &nbsp;
                <img :src="character.texture.cape" style="height: 32px; width: 64px" alt="" />
              </div>
            </div>
          </div>
        </div>
      </div>
      <!--End 更新披风-->
    </div>
  </main>
  <!--End 页面内容-->
</template>
<style scoped>
.lowlro-logo {
  width: 210px;
  height: 36px;
  object-fit: cover;
}
</style>
