// import Vue from 'vue'
import { api } from '@/api'

const storeCenter = {
  state: {
    d1DsTree: [],
    domainAndItemTree: [],
    domainAndValueTree: [],
    defaultValCategories: []
  },

  mutations: {
    SET_D1_DS_TREE: (state, tree) => {
      state.d1DsTree = tree
    },
    SET_DOMAIN_AND_ITEM_TREE: (state, tree) => {
      state.domainAndItemTree = tree
    },
    SET_DOMAIN_AND_VALUE_TREE: (state, tree) => {
      state.domainAndValueTree = tree
    },
    SET_DEFAULT_VAL_CATEGORIES: (state, list) => {
      state.defaultValCategories = list
    }
  },

  actions: {
    getD1DataSourceTree ({ commit, getters }, cache = true) {
      return new Promise((resolve, reject) => {
        !cache && api.removeCache('getDataSourceList')
        api.getDataSourceList(false).then((resp) => {
          if (resp) {
            commit('SET_D1_DS_TREE', resp.children)
            resolve(resp)
          } else {
            resolve([])
          }
        }).catch((err) => {
          reject(err)
        })
      })
    },
    getDomainAndItemTree ({ commit, getters }, cache = true) {
      return new Promise((resolve, reject) => {
        !cache && api.removeCache('getDomainAndItemTree')
        api.getDomainAndItemTree().then((resp) => {
          if (resp) {
            commit('SET_DOMAIN_AND_ITEM_TREE', [resp])
            resolve(resp)
          } else {
            resolve([])
          }
        }).catch((err) => {
          reject(err)
        })
      })
    },
    getDomainAndValueTree ({ commit, getters }, cache = true) {
      return new Promise((resolve, reject) => {
        !cache && api.removeCache('getDomainAndValueTree')
        api.getDomainAndValueTree().then((resp) => {
          if (resp) {
            commit('SET_DOMAIN_AND_VALUE_TREE', [resp])
            resolve(resp)
          } else {
            resolve([])
          }
        }).catch((err) => {
          reject(err)
        })
      })
    },
    getDefaultValCategories ({ commit, getters }, cache = true) {
      return new Promise((resolve, reject) => {
        !cache && api.removeCache('getDefaultValCategories')
        api.getDefaultValCategories().then((resp) => {
          if (resp) {
            commit('SET_DEFAULT_VAL_CATEGORIES', [resp])
            resolve(resp)
          } else {
            resolve([])
          }
        }).catch((err) => {
          reject(err)
        })
      })
    }
  }
}

export default storeCenter
