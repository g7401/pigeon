// import { axios } from '@/utils/request'

/**
 * login func
 * parameter: {
 *     username: '',
 *     password: '',
 *     remember_me: true,
 *     captcha: '12345'
 * }
 * @param parameter
 * @returns {*}
 */

export const api = {
  login: '/account/oauth/sign_in',
  logout: '/account/oauth/sign_out',
  getUserInfo: '/account/profile'
  // ForgePassword: '/auth/forge-password',
  // Register: '/auth/register',
  // twoStepCode: '/auth/2step-code',
  // SendSms: '/account/sms',
  // SendSmsErr: '/account/sms_err',
  // // get my info
  // UserInfo: '/user/info'
}

// export function login (parameter) {
//   return axios({
//     url: '/auth/login',
//     method: 'post',
//     data: parameter
//   })
// }

// export function getSmsCaptcha (parameter) {
//   return axios({
//     url: api.SendSms,
//     method: 'post',
//     data: parameter
//   })
// }

// export function getInfo () {
//   return axios({
//     url: '/user/info',
//     method: 'get',
//     headers: {
//       'Content-Type': 'application/json;charset=UTF-8'
//     }
//   })
// }

// export function getCurrentUserNav (token) {
//   return axios({
//     url: '/user/nav',
//     method: 'get'
//   })
// }

// export function logout () {
//   return axios({
//     url: '/auth/logout',
//     method: 'post',
//     headers: {
//       'Content-Type': 'application/json;charset=UTF-8'
//     }
//   })
// }

// /**
//  * get user 2step code open?
//  * @param parameter {*}
//  */
// export function get2step (parameter) {
//   return axios({
//     url: api.twoStepCode,
//     method: 'post',
//     data: parameter
//   })
// }

const config = {
  login: {
    url: api.login,
    method: 'post',
    dataFields: ['username', 'password']
    // mock: true
  },
  getUserInfo: {
    url: api.getUserInfo,
    method: 'post',
    // mock: true,
    paramsFields: ['username'],
    disableMessage: true
  },
  logout: {
    url: api.logout,
    method: 'post',
    // mock: true,
    paramsFields: ['username']
  }
}

export default config
