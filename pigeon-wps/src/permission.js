import Vue from 'vue'
import router from './router'
import store from './store'
import cloneDeep from 'lodash.clonedeep'
import NProgress from 'nprogress' // progress bar
import '@/components/NProgress/nprogress.less' // progress bar custom style
import notification from 'ant-design-vue/es/notification'
import { setDocumentTitle, domTitle } from '@/utils/domUtil'
import { ACCESS_TOKEN, ACCESS_USERNAME } from '@/store/mutation-types'
import { api } from '@/api'
// import config from '@/config/defaultSettings'
const formatRouter = (routes, newRoutes = []) => {
  routes.map(item => {
    if (item.children && item.children.length > 0) formatRouter(item.children, newRoutes)
    newRoutes.push(item)
    item.children = null
  })
  return newRoutes
}

NProgress.configure({ showSpinner: false }) // NProgress Configuration

const whiteList = ['login', 'register', 'registerResult'] // no redirect whitelist
// const defaultRoutePath = '/dashboard/workplace'

function handleRedirect (to, from, next) {
  const redirect = decodeURIComponent(from.query.redirect || to.path)
  if (to.path === redirect) {
    // hack方法 确保addRoutes已完成 ,set the replace: true so the navigation will not leave a history record
    next({ ...to, replace: true })
  } else {
    // 跳转到目的路由
    next({ path: redirect })
  }
}

router.beforeEach((to, from, next) => {
  NProgress.start() // start progress bar
  to.meta && (typeof to.meta.title !== 'undefined' && setDocumentTitle(`${to.meta.title} - ${domTitle}`))
  // if (store.getters.roles.length === 0) {
  //   store.dispatch('GetPageInfo')
  //   // store.commit('SET_ROLES', [{}])
  //   store
  //     .dispatch('GetInfo')
  //     .then(result => {
  //       const roles = result.roles
  //       store.dispatch('GenerateRoutes', { roles: roles }).then(() => {
  //         // 根据roles权限生成可访问的路由表
  //         // 动态添加可访问路由表
  //         const routers = cloneDeep(store.getters.addRouters)
  //         routers[0].children = formatRouter(routers[0].children)
  //         router.addRoutes(routers)
  //         // router.addRoutes(store.getters.addRouters)
  //         // 请求带有 redirect 重定向时，登录自动重定向到该地址
  //         handleRedirect(to, from, next)
  //       })
  //     })
  //     .catch(() => {
  //       notification.error({
  //         message: '错误',
  //         description: '请求用户信息失败，请重试'
  //       })
  //       store.dispatch('Logout').then(() => {
  //         next({ path: '/user/login', query: { redirect: to.fullPath } })
  //       })
  //     })
  // }

  // if (Vue.ls.get(ACCESS_TOKEN)) {
  //   // if (to.path !== '/user/login') {
  //   //   store.dispatch('GetInfo')
  //   // }
  //   next()
  // } else {
  //   if (whiteList.includes(to.name)) {
  //     // 在免登录白名单，直接进入
  //     next()
  //   } else {
  //     next({ path: '/user/login', query: { redirect: to.fullPath } })
  //     NProgress.done() // if current page is login will not trigger afterEach hook, so manually handle it
  //   }
  // }

  // 从编辑页跳到列表页不缓存
  if (from.meta.noCacheToRoutes && from.meta.noCacheToRoutes.includes(to.name) && !store.getters.isCacheRoute(from)) {
    store.commit('removeRouteCache', to)
  }
  const baseUrls = {
    openapi: 'openapi',
    deployment: 'deployment'
  }
  if (to.query.access_token && to.query.username && to.query.client_id) {
    Vue.ls.set(ACCESS_TOKEN, to.query.access_token, store.getters.userExpire)
    Vue.ls.set(ACCESS_USERNAME, to.query.username, store.getters.userExpire)
    store.commit('SET_TOKEN', to.query.access_token)
    store.commit('SET_NAME', to.query.username)
    store.commit('SET_API_TYPE', 'openapi')
    store.commit('SET_APP_KEY', to.query.client_id)

    const apiTypeParams = {
      openapi: {
        client_id: to.query.client_id,
        access_token: to.query.access_token,
        username: to.query.username
      }
    }

    api.setApiType('openapi', {
      params: apiTypeParams,
      baseUrls
    })
  } else {
    api.setApiType('deployment', {
      baseUrls
    })
  }
  if (store.getters.apiType === 'openapi') {
    if (store.getters.roles.length === 0) {
      const roles = {
        permission: ['APP']
      }
      store.commit('SET_ROLES', roles)
      store.dispatch('GenerateRoutes', { roles }).then(() => {
        // 根据roles权限生成可访问的路由表
        // 动态添加可访问路由表
        const routers = cloneDeep(store.getters.addRouters)
        routers[0].children = formatRouter(routers[0].children)
        router.addRoutes(routers)
        // router.addRoutes(store.getters.addRouters)
        // 请求带有 redirect 重定向时，登录自动重定向到该地址
        handleRedirect(to, from, next)
      })
    } else {
      next()
    }
  } else if (Vue.ls.get(ACCESS_TOKEN)) {
    /* has token */
    if (to.path === '/user/login') {
      next({ path: '/' })
      NProgress.done()
    } else {
      if (store.getters.roles.length === 0) {
        store
          .dispatch('GetInfo')
          .then(result => {
            const roles = result.roles
            store.dispatch('GenerateRoutes', { roles }).then(() => {
              // 根据roles权限生成可访问的路由表
              // 动态添加可访问路由表
              const routers = cloneDeep(store.getters.addRouters)
              routers[0].children = formatRouter(routers[0].children)
              router.addRoutes(routers)
              // router.addRoutes(store.getters.addRouters)
              // 请求带有 redirect 重定向时，登录自动重定向到该地址
              handleRedirect(to, from, next)
            })
          })
          .catch(() => {
            notification.error({
              message: '错误',
              description: '请求用户信息失败，请重试'
            })
            store.dispatch('Logout').then(() => {
              next({ path: '/user/login', query: { redirect: to.fullPath } })
            })
          })
      } else {
        next()
      }
    }
  } else {
    if (whiteList.includes(to.name)) {
      // 在免登录白名单，直接进入
      next()
    } else {
      next({ path: '/user/login', query: { redirect: to.fullPath } })
      NProgress.done() // if current page is login will not trigger afterEach hook, so manually handle it
    }
  }
})

router.afterEach((to, from) => {
  NProgress.done() // finish progress bar
})
