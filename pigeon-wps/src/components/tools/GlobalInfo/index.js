import events from '@/components/_util/events'
import GlobalInfo from './GlobalInfo'
const api = {
  show: function (config) {
    if (typeof config === 'string') {
      config = {
        message: config
      }
    }
    events.$emit('showAlert', config)
  }
}

GlobalInfo.install = function (Vue) {
  if (Vue.prototype.$alert) {
    return
  }
  Vue.prototype.$alert = api
}

export default GlobalInfo
