import Gateway from '@/api/gateway'

const queryAppDefine = {
  total: {
    type: 'number',
    property: 'total_elements'
  }
}
export const api = {
  app: '/admin/app',
  queryApp: '/admin/app/query',
  dfAccess: '/admin/app/access/df',
  userAccess: '/admin/app/access/user'
}

const config = {
  getApp: {
    url: api.app,
    method: 'get',
    paramsFields: ['uid']
  },
  createApp: {
    url: api.app,
    method: 'post'
  },
  updateApp: {
    url: api.app,
    method: 'patch'
  },
  delApp: {
    url: api.app,
    method: 'delete',
    paramsFields: ['uid']
  },
  queryApp: {
    url: api.queryApp,
    method: 'get',
    gateway: new Gateway(queryAppDefine)
  },
  getDfAccess: {
    url: api.dfAccess,
    method: 'get',
    paramsFields: ['app_uid']
  },
  saveDfAccess: {
    url: api.dfAccess,
    method: 'put',
    dataFields: ['app_uid', 'df_uids']
  },
  getUserAccess: {
    url: api.userAccess,
    method: 'get',
    paramsFields: ['app_uid']
  },
  saveUserAccess: {
    url: api.userAccess,
    method: 'put',
    dataFields: ['app_uid', 'usernames']
  }
}

export default config
