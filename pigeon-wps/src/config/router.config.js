// eslint-disable-next-line
// import { UserLayout, BasicLayout, RouteView, BlankLayout, PageView } from '@/layouts'
import { UserLayout, BasicLayout, SimpleBasicLayout } from '@/layouts'
// import { bxAnaalyse } from '@/core/icons'

export const asyncRouterMap = [
  {
    path: '/',
    name: 'home',
    component: BasicLayout,
    meta: { title: 'Home' },
    redirect: '/index',
    children: [
      {
        path: '/index',
        name: 'D1UserView',
        component: () => import('@/views/d1/D1UserView'),
        props: (route) => ({ dfk: route.query.df_key }),
        meta: { title: 'Query', permission: ['user', 'admin', 'developer'] }
      },
      {
        path: '/data-facet-admin',
        name: 'D1AdminView',
        component: () => import('@/views/d1/D1AdminView'),
        props: (route) => ({ mode: route.query.mode }),
        meta: { title: 'Data Facet', permission: ['admin', 'developer'] }
      },
      {
        path: '/dictionary-managemenet',
        name: 'DictionaryManagemenet',
        meta: { title: 'Dictionary', permission: ['admin', 'developer'] },
        children: [
          {
            path: '/dictionary-definition-and-configuration',
            name: 'DictDef',
            meta: { title: 'Dictionary Definition & Configuration' },
            component: () => import('@/views/d1/DictDef')
          },
          {
            path: '/dictionary-content-management',
            name: 'DictValue',
            meta: { title: 'Dictionary Content' },
            component: () => import('@/views/d1/DictValue')
          }
        ]
      },
      {
        path: '/defaults-managemenet',
        name: 'DefaultsManagemenet',
        meta: { title: 'Defaults', permission: ['admin', 'developer'] },
        children: [
          {
            path: '/defaults-categorys',
            name: 'DefaultValCategoryDef',
            meta: { title: 'Defaults Definition' },
            component: () => import('@/views/d1/DefaultValCategoryDef')
          },
          {
            path: '/defaults-contents',
            name: 'DefaultValContentList',
            meta: { title: 'Defaults Content' },
            component: () => import('@/views/d1/DefaultValContentList')
          }
        ]
      },
      {
        path: '/app',
        name: 'App',
        meta: { title: 'App', permission: ['admin', 'developer'] },
        children: [
          {
            path: '/app-management',
            name: 'AppManagement',
            component: () => import('@/views/app/AppManagement'),
            meta: { title: 'Apps' }
          },
          {
            path: '/monitor',
            name: 'OperationList',
            component: () => import('@/views/app/OperationList'),
            meta: { title: 'Monitor' }
          }
        ]
      },
      {
        path: '/system-admin',
        name: 'SystemAdmin',
        meta: { title: 'System', permission: ['admin'] },
        children: [
          {
            path: 'user-management',
            name: 'UserManagement',
            component: () => import('@/views/user/UserManagement'),
            meta: { title: 'User' }
          },
          {
            path: '/system-parameters',
            name: 'PageInfoConfig',
            meta: { title: 'System Parameters' },
            component: () => import('@/views/page/PageInfoConfig')
          }
        ]
      }
      // {
      //   path: 'offline-center',
      //   name: 'OfflineCenter',
      //   component: () => import('@/views/OfflineCenter'),
      //   meta: { title: 'Offline Center' }
      // }
      // {
      //   path: 'msg-center',
      //   name: 'AlertCenter',
      //   component: () => import('@/views/AlertCenter'),
      //   meta: { title: 'msg Center' }
      // }
    ]
  },
  {
    name: 'SimpleView',
    path: '/simple-view',
    component: SimpleBasicLayout,
    children: [
      {
        path: '/app-table',
        name: 'AppTable',
        component: () => import('@/views/d1/D1PreviewView'),
        props: (route) => ({ dfk: route.query.df_key }),
        meta: { title: '报表' },
        hidden: true
      },
      {
        path: '/preview-table',
        name: 'PreviewTable',
        component: () => import('@/views/d1/D1PreviewView'),
        props: (route) => ({ dfk: route.query.df_key, apiType: 'deployment' }),
        meta: { title: 'Preview' },
        hidden: true
      }
    ]
  },
  {
    path: '*', redirect: '/404', hidden: true
  }
]

/**
 * 基础路由
 * @type { *[] }
 */
export const constantRouterMap = [
  {
    path: '/user',
    component: UserLayout,
    redirect: '/user/login',
    hidden: true,
    children: [
      {
        path: 'login',
        name: 'login',
        component: () => import(/* webpackChunkName: "user" */ '@/views/user/Login')
      }
      // {
      //   path: 'register',
      //   name: 'register',
      //   component: () => import(/* webpackChunkName: "user" */ '@/views/user/Register')
      // },
      // {
      //   path: 'register-result',
      //   name: 'registerResult',
      //   component: () => import(/* webpackChunkName: "user" */ '@/views/user/RegisterResult')
      // },
      // {
      //   path: 'recover',
      //   name: 'recover',
      //   component: undefined
      // }
    ]
  },
  {
    path: '/404',
    component: () => import(/* webpackChunkName: "fail" */ '@/views/exception/404')
  }
]
