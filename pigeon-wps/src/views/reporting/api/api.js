import { axios } from '@/utils/request'

const api = {
    serviceUrl: '/lotus-rpts/'
}

export function get (url, params) {
    return axios({
        url: api.serviceUrl + url,
        method: 'get',
        params: params
    })
}

export function post (url, params, data) {
    return axios({
        url: api.serviceUrl + url,
        method: 'post',
        params: params,
        data: data
    })
}
