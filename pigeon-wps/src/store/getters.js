const getters = {
  device: state => state.app.device,
  theme: state => state.app.theme,
  color: state => state.app.color,
  sidebar: (state) => state.app.sidebar,
  token: state => state.user.token,
  tokenType: state => state.user.tokenType,
  userExpire: state => state.user.userExpire,
  avatar: state => state.user.avatar,
  nickname: state => state.user.name,
  welcome: state => state.user.welcome,
  roles: state => state.user.roles,
  userInfo: state => state.user.info,
  apiType: state => state.user.apiType,
  appKey: state => state.user.appKey,
  addRouters: state => state.permission.addRouters,
  multiTab: state => state.app.multiTab,
  include: state => state.multiTab.include,
  maxIncludeCount: state => state.multiTab.maxIncludeCount,
  lang: state => state.i18n.lang,
  // suppliers: state => state.user.suppliers,
  d1DsTree: state => state.d1Admin.d1DsTree,
  domainAndItemTree: state => state.d1Admin.domainAndItemTree,
  domainAndValueTree: state => state.d1Admin.domainAndValueTree,
  defaultValCategories: state => state.d1Admin.defaultValCategories,
  pageInfo: state => state.page.info
}

export default getters
