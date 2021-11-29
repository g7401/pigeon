import { api } from '@/api'

const page = {
  state: {
    info: {
      terms_of_service_url: '',
      privacy_policy_url: '',
      release_version: '1.0.0',
      vendor_name: 'Inkstone'
    }
  },

  mutations: {
    SET_PAGE_INFO: (state, info) => {
      state.info = info
    }
  },

  actions: {
    // 获取页面底部信息
    GetPageInfo ({ commit }) {
      return new Promise((resolve, reject) => {
        api.getPageInfo().then(result => {
          result && commit('SET_PAGE_INFO', result)
          resolve()
        }).catch(error => {
          reject(error)
        })
      })
    },

    // 保存页面底部信息
    savePageInfo ({ commit, state }, info) {
      return new Promise((resolve, reject) => {
        api.savePageInfo(info).then(result => {
          commit('SET_PAGE_INFO', info)
          resolve()
        }).catch(error => {
          reject(error)
        })
      })
    }
  }
}

export default page
