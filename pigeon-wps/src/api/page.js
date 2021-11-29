
// import Gateway from '@/api/gateway'
export const api = {
  pageInfo: '/system/parameters/latest_system_release',
  getSystemMessage: '/system-message'
}

// const pageInfoJsonDef = {
//   terms_of_service: {
//     type: 'string',
//     property: 'terms_of_service_url',
//     defaultValue: 'Terms of Service'
//   },
//   privacy_policy: {
//     type: 'string',
//     property: 'privacy_policy_url',
//     defaultValue: 'Privacy Policy'
//   },
//   release_version: {
//     type: 'string',
//     defaultValue: '1.0.0'
//   },
//   vendor_name: {
//     type: 'string',
//     defaultValue: 'Inkstone'
//   }
// }

const config = {
  getPageInfo: {
    url: api.pageInfo,
    method: 'get',
    mock: false
  },
  savePageInfo: {
    url: api.pageInfo,
    method: 'post'
  },
  getSystemMessage: {
    url: api.getSystemMessage,
    method: 'get',
    mock: true
  }
}

export default config
