import { isIE } from '@/utils/util'
import Vue from 'vue'
// 判断环境不是 prod 或者 preview 是 true 时，加载 mock 服务
if (process.env.NODE_ENV !== 'production' || process.env.VUE_APP_PREVIEW === 'true') {
  if (isIE()) {
    console.error('[antd-pro] ERROR: `mockjs` NOT SUPPORT `IE` PLEASE DO NOT USE IN `production` ENV.')
  }
  // 使用同步加载依赖
  // 防止 vuex 中的 GetInfo 早于 mock 运行，导致无法 mock 请求返回结果
  console.log('[antd-pro] mock mounting')
  const Mock = require('mockjs2')
  require('./services/auth')
  require('./services/user')
  require('./services/manage')
  require('./services/other')
  require('./services/article')
  // require('./services/d1')
  const files = require.context('./services/', false, /\.js$/)
  const mockDataMap = files.keys().reduce((acc, key) => {
    return Object.assign(acc, files(key))
  }, {})
  const { default: apiConfigMap } = require('@/api/config')
  const generateMock = (configMap, mockDataMap) => {
    Object.keys(apiConfigMap).forEach(key => {
      const apiConfig = apiConfigMap[key]
      let mockData = mockDataMap[key] || Vue.ls.get(`${key}_mock`)
      if (!mockData && apiConfig.gateway) {
        mockData = apiConfig.gateway.getMockData()
      }
      if (mockData && apiConfig.mock) {
        Mock.mock(new RegExp(apiConfig.url), apiConfig.method, typeof mockData === 'function' ? mockData : () => mockData)
      }
    })
  }
  generateMock(apiConfigMap, mockDataMap)

  Mock.setup({
    timeout: 500 // setter delay time
  })
  console.log('[antd-pro] mock mounted')
}
