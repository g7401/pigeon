// import Vue from 'vue'
import { api } from '@/api'

const storeCenter = {
  state: {
    dsTypes: [],
    dsTree: [],
    dtTree: []
  },

  mutations: {
    SET_DS_TYPES: (state, dsTypes) => {
      state.dsTypes = dsTypes
    },
    SET_DS_TREE: (state, tree) => {
      state.dsTree = tree
    },
    SET_DT_TREE: (state, tree) => {
      state.dtTree = tree
    }
  },

  actions: {
    GetDsTypes ({ commit, getters }) {
      return new Promise((resolve, reject) => {
        if (getters.dsTypes.length) {
          resolve(getters.dsTypes)
          return
        }
        api.getDataSourceType().then((resp) => {
          if (resp && resp.object) {
            commit('SET_DS_TYPES', resp.object)
            resolve(resp.object)
          }
        }).catch((err) => {
          reject(err)
        })
      })
    },
    getDataSourceTree ({ commit, getters }, cache = true) {
      return new Promise((resolve, reject) => {
        !cache && api.removeCache('getDataSourceTree')
        api.getDataSourceTree().then((resp) => {
          if (resp && resp.object) {
            const tree = [resp.object]
            commit('SET_DS_TREE', tree)
            resolve(resp)
          }
        }).catch((err) => {
          reject(err)
        })
      })
    },
    getDataTargetTree ({ commit, getters }, cache = true) {
      return new Promise((resolve, reject) => {
        !cache && api.removeCache('getDataSourceDt')
        api.getDataSourceDt().then((resp) => {
          if (resp && resp.object) {
            const tree = [resp.object]
            commit('SET_DT_TREE', tree)
            resolve(resp)
          }
        }).catch((err) => {
          reject(err)
        })
      })
    }
  }
}

export default storeCenter
