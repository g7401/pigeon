import Vue from 'vue'

const localSavePages = (state) => {
  const pages = []
  state.pages.forEach(page => {
    const newPage = {
      ...page
    }
    delete newPage.state
    pages.push(newPage)
  })
  Vue.ls.set('localstorage_pages', pages)
}

const localSaveState = (state, page) => {
  Vue.ls.set(`${page.name}_page_state`, page.state, page.timeline || state.timeline)
}

const findPage = (state, name) => {
  if (!name) return null
  return state.pages.find(page => page.name.toLowerCase() === name.toLowerCase())
}

export default {
  state: {
    pages: [{
      name: 'featureStoreIndex',
      updateType: 'queryParams',
      state: {}
    }, {
      name: 'reporting',
      updateType: 'queryParams',
      state: {}
    }],
    timeline: 24 * 60 * 60 * 1000
  },
  getters: {
    getPage: (state) => (name) => {
      return findPage(state, name)
    },
    getPageState: (state, getters) => (name) => {
      const page = findPage(state, name)
      return page ? page.state : null
    },
    canUpdatePage: (state, getters) => (name) => {
      const page = findPage(state, name)
      return page && page.state && page.update
    }
  },
  mutations: {
    setPageUpdated: (state, name) => {
      const page = findPage(state, name)
      page && (page.update = false)
      localSavePages(state)
    },
    setAsyncPages: (state, pages) => {
      pages.forEach(localPage => {
        const oldPage = findPage(state, localPage.name)
        if (oldPage) {
          Object.assign(oldPage, localPage)
        }
      })
    },
    savePage: (state, pageData) => {
      if (!pageData || !pageData.name) return
      let page = findPage(state, pageData.name)
      if (page) {
        Object.assign(page.state, pageData.state)
        delete pageData.state
        Object.assign(page, pageData)
      } else {
        page = pageData
        state.pages.push(page)
      }
      localSaveState(state, page)
      localSavePages(state)
    },
    removePageState: (state, { name, stateKey }) => {
      const page = findPage(state, name)
      if (!page) return
      if (stateKey) {
        page.state[stateKey] = null
      } else {
        page.state = {}
      }
      localSaveState(state, page)
    }
  },
  actions: {
    savePage ({ commit }, page) {
      commit('savePage', page)
    }
  }
}
