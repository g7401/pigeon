import Vue from 'vue'
import Vuex from 'vuex'

import app from './modules/app'
import user from './modules/user'

// default router permission control
import permission from './modules/permission'
import multiTab from './modules/multiTab'
import pageState from './modules/pageState'
import d1Admin from './modules/d1Admin'
import page from './modules/page'
// dynamic router permission control (Experimental)
// import permission from './modules/async-router'
import getters from './getters'

Vue.use(Vuex)

export default new Vuex.Store({
  modules: {
    app,
    user,
    permission,
    multiTab,
    pageState,
    d1Admin,
    page
  },
  state: {

  },
  mutations: {

  },
  actions: {

  },
  getters
})
