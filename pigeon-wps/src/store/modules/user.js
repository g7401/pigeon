import Vue from 'vue'
// import { logout } from '@/api/login'
import { api } from '@/api'
import { ACCESS_TOKEN, ACCESS_USERNAME } from '@/store/mutation-types'
// import config from '@/config/defaultSettings'
// import { welcome } from '@/utils/util'

const user = {
  state: {
    token: '',
    tokenType: '',
    name: '',
    welcome: '',
    avatar: '',
    roles: [],
    info: {},
    userExpire: 7 * 24 * 60 * 60 * 1000,
    apiType: 'deployment',
    appKey: ''
  },

  mutations: {
    SET_TOKEN: (state, token) => {
      state.token = token
    },
    SET_TOKEN_TYPE: (state, type) => {
      state.tokenType = type
    },
    SET_EXPIRE: (state, userExpire) => {
      state.userExpire = userExpire
    },
    SET_NAME: (state, name) => {
      state.name = name
    },
    // SET_NAME: (state, { name, welcome }) => {
    //   state.name = name
    //   state.welcome = welcome
    // },
    SET_AVATAR: (state, avatar) => {
      state.avatar = avatar
    },
    SET_ROLES: (state, roles) => {
      state.roles = roles
    },
    SET_INFO: (state, info) => {
      state.info = info
    },
    SET_API_TYPE: (state, type) => {
      state.apiType = type
    },
    SET_APP_KEY: (state, key) => {
      state.appKey = key
    }
  },

  actions: {
    // 登录
    Login ({ state, commit }, { username, password }) {
      return new Promise((resolve, reject) => {
        api.login(username, password).then(result => {
          commit('SET_EXPIRE', result.expires_in * 1000)
          Vue.ls.set(ACCESS_TOKEN, result.access_token, state.userExpire)
          Vue.ls.set(ACCESS_USERNAME, result.username, state.userExpire)
          commit('SET_TOKEN', result.access_token)
          commit('SET_TOKEN_TYPE', result.token_type)
          commit('SET_NAME', result.username)
          resolve()
        }).catch(error => {
          reject(error)
        })
      })
    },

    // 获取用户信息
    GetInfo ({ commit, state }) {
      return new Promise((resolve, reject) => {
        api.getUserInfo(Vue.ls.get(ACCESS_USERNAME)).then(result => {
          commit('SET_INFO', result)
          result.roles = {
            permission: [result.role] || ['USER']
          }
          commit('SET_ROLES', result.roles)
          resolve(result)
        }).catch(error => {
          reject(error)
        })
      })
    },

    // 登出
    Logout ({ commit, state }) {
      return new Promise((resolve) => {
        api.logout(state.name).then(() => {
          resolve()
        }).catch(() => {
          resolve()
        }).finally(() => {
          commit('SET_TOKEN', '')
          commit('SET_NAME', '')
          commit('SET_ROLES', [])
          Vue.ls.remove(ACCESS_TOKEN)
          Vue.ls.remove(ACCESS_USERNAME)
        })
      })
    }

  }
}

export default user
