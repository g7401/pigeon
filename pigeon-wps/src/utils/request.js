import Vue from 'vue'
import axios from 'axios'
import store from '@/store'
import notification from 'ant-design-vue/es/notification'
import message from 'ant-design-vue/es/message'
import { VueAxios } from './axios'
import { ACCESS_TOKEN } from '@/store/mutation-types'

const serverConfig = require('serverConfig')
const baseUrl = serverConfig.VUE_APP_API_BASE_URL || process.env.VUE_APP_API_BASE_URL

// 创建 axios 实例
const service = axios.create({
  baseURL: baseUrl, // api base_url
  timeout: 5 * 60 * 1000 // 请求超时时间
})

const err = (error) => {
  if (error.response) {
    const data = error.response.data
    const token = Vue.ls.get(ACCESS_TOKEN)
    if (error.response.status === 403) {
      notification.error({
        message: 'Forbidden',
        description: data.message
      })
    } else if (error.response.status === 404) {
      notification.error({
        message: '404',
        description: data.message
      })
    } else if (error.response.status === 401) {
      notification.error({
        message: 'Unauthorized',
        description: 'Authorization verification failed'
      })
      if (token) {
        store.dispatch('Logout').then(() => {
          setTimeout(() => {
            window.location.reload()
          }, 1500)
        })
      }
    } else if (error.response.status === 500) {
      if (data.err_message || data.err_code) {
        const prefix = ['post', 'patch', 'delete', 'put'].includes(error.response.config.method) ? 'Operation failed. More info: ' : ''
        notification.error({
          message: data.err_code,
          description: `${prefix}${data.err_message}`
        })
      }
    } else if (error.response.status === 504) {
      notification.error({
        message: '504',
        description: 'Gateway Timeout'
      })
    } else if (error.response.status) {
      notification.error({
        message: error.response.status,
        description: error.message
      })
    }
  }
  if (error.code === 'ECONNABORTED' && error.message.indexOf('timeout') !== -1) { // 请求超时
    notification.error({
      message: '请求超时',
      description: error.message
    })
  }
  return Promise.reject(error)
}

// request interceptor
service.interceptors.request.use(config => {
  if (store.getters.apiType === 'deployment') {
    config.headers['X-USERNAME'] = store.getters.nickname || 'mars_user'
    const token = Vue.ls.get(ACCESS_TOKEN)
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}` // 让每个请求携带自定义 token 请根据实际情况自行修改
    }
  }
  return config
}, err)

// response interceptor
service.interceptors.response.use((response) => {
  if (['post', 'patch', 'delete', 'put'].includes(response.config.method)) {
    if ((response.data && response.data.successful && !response.data._confg.disableMessage) || !response.data) {
      message.success('Operation succeeded')
    }
  }
  if (response.data._confg && response.data._confg.isRawResponse) {
    return response.data
  }
  if (response.data.hasOwnProperty('object')) {
    return response.data.object
  }
  return response.data
}, err)

const installer = {
  vm: {},
  install (Vue) {
    Vue.use(VueAxios, service)
  }
}

export {
  installer as VueAxios,
  service as axios
}
