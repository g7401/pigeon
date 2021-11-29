import Gateway from '@/api/gateway'
import sha1 from 'sha1'
export const api = {
  user: '/account',
  queryUser: '/account/username/query'
}

const config = {
  createUser: {
    url: api.user,
    method: 'post',
    gateway: new Gateway({
      reqBody: {
        password: {
          type: 'string',
          formater (value) {
            return sha1(value)
          }
        }
      }
    }, true)
  },
  updateUser: {
    url: api.user,
    method: 'patch'
  },
  deleteUser: {
    url: api.user,
    method: 'delete',
    paramsFields: ['username']
  },
  queryUser: {
    url: api.queryUser,
    method: 'get',
    paramsFields: ['username'],
    cache: true
  }
}

export default config
