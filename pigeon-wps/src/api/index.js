import { axios } from '@/utils/request'
import config from './config'
import Vue from 'vue'
import cloneDeep from 'lodash.clonedeep'
import qs from 'qs'
import md5 from 'md5'
// import store from '@/store/'

const baseParamsSerializer = function (params) {
  return qs.stringify(params, { arrayFormat: 'repeat' })
}

class Api {
  constructor (config) {
    this.urlPrefix = ''
    this.cache = {}
    this.cacheKeyMap = {}
    this.config = config
    this.apiType = 'openapi'
    this.apiTypeConfig = {
      params: {},
      functions: {},
      baseUrls: {}
    }
    this.generateApiMethod(config)
  }

  prefix () {
    this.urlPrefix = [...arguments].filter(x => x).map(x => `/${x}`).join('')
    return this
  }

  setApiType (apiType, apiTypeConfig) {
    this.apiType = apiType
    Object.assign(this.apiTypeConfig, apiTypeConfig)
    // this.apiTypeParams = apiTypeParams
    // if (apiTypeFunctions) {
    //   this.apiTypeFunctions = apiTypeFunctions
    // }
    // if (apiTypeBaseUrls) {
    //   this.apiTypeBaseUrls = apiTypeBaseUrls
    // }
  }

  removeCache (apiName) {
    const keys = this.cacheKeyMap[apiName] || []
    keys.forEach(key => {
      this.cache[key] = null
    })
  }

  generateApiMethod (config) {
    Object.keys(config).forEach(apiFnName => {
      const apiConfig = config[apiFnName]
      if (typeof apiConfig === 'function') {
        this[apiFnName] = apiConfig
        return
      } else if (typeof apiConfig === 'string') {
        return
      }
      this[apiFnName] = async function () {
        let params = {}
        let data = {}
        let argsIndex = 0
        const method = apiConfig.method || 'get'
        const setValue = (data, field) => {
          if (typeof field === 'string') {
            data[field] = arguments[argsIndex]
          } else {
            data[field.key] = arguments[argsIndex] || field.default
          }
          argsIndex++
        }
        apiConfig.paramsFields &&
          apiConfig.paramsFields.forEach(field => {
            setValue(params, field)
          })
        apiConfig.dataFields &&
          apiConfig.dataFields.forEach(field => {
            setValue(data, field)
          })
        if (arguments[argsIndex]) {
          if (method === 'get' || apiConfig.argsMode === 'paramsAndData') {
            Object.assign(params, arguments[argsIndex])
          } else {
            if (Array.isArray(arguments[argsIndex])) {
              data = arguments[argsIndex]
            } else {
              Object.assign(data, arguments[argsIndex])
            }
          }
        }
        if (arguments[argsIndex++] && apiConfig.argsMode === 'paramsAndData') {
          data = arguments[argsIndex++]
        }

        // if (!apiConfig.url) return
        if (apiConfig.gateway) {
          params = apiConfig.gateway.toBack(params)
          data = apiConfig.gateway.toBack(data, 'body')
        }
        const urlPrefix = this.urlPrefix
        this.urlPrefix = ''
        let baseUrl = ''
        if (apiConfig.apiTypes && apiConfig.apiTypes.includes(this.apiType)) {
          const apiFn = this.apiTypeConfig.functions[this.apiType]
          if (apiFn) {
            await apiFn()
          }
          const extraParams = this.apiTypeConfig.params[this.apiType]
          if (extraParams) {
            Object.assign(params, extraParams)
          }
          baseUrl = this.apiTypeConfig.baseUrls[this.apiType] || ''
        }
        const axiosConfig = {
          url: `${baseUrl}${urlPrefix}${apiConfig.url}`,
          headers: {
            'Content-Type': 'application/json'
          },
          method: method,
          params,
          data: data || {},
          paramsSerializer: apiConfig.paramsSerializer || baseParamsSerializer,
          transformResponse: axios.defaults.transformResponse.concat((data) => {
            if (data && typeof data === 'object') {
              data._confg = apiConfig
            }
            return data
          })
        }
        let cacheKey
        if (apiConfig.cache) {
          cacheKey = md5(JSON.stringify(axiosConfig))
          if (this.cacheKeyMap[apiFnName]) {
            this.cacheKeyMap[apiFnName].push(cacheKey)
          } else {
            this.cacheKeyMap[apiFnName] = [cacheKey]
          }
          if (this.cache[cacheKey]) {
            return cloneDeep(this.cache[cacheKey])
          }
        }
        if (apiConfig.loading) {
          Vue.prototype.$loading.show()
        }
        let resp = await axios(axiosConfig).finally(() => {
          if (apiConfig.loading) {
            Vue.prototype.$loading.hide()
          }
        })
        if (apiConfig.gateway) {
          resp = apiConfig.gateway.toFront(resp)
        }
        if (apiConfig.cache) {
          this.cache[cacheKey] = cloneDeep(resp)
        }
        if (process.env.NODE_ENV !== 'production' && apiConfig.mock) {
          Vue.ls.set(`${apiFnName}_mock`, resp)
        }
        return resp
      }
    })
  }
}

export let api
const installer = {
  vm: {},
  install (Vue) {
    api = new Api(config)
    Vue.prototype.$api = api
  }
}

export default installer
