import request from '@/utils/request'

export function applyCertification(data) {
    return request({
        url: '/certification/apply',
        method: 'post',
        data
    })
}

export function getMyCertification() {
    return request({
        url: '/certification/my',
        method: 'get'
    })
}

export function getCertifiedProviders(params) {
    return request({
        url: '/certification/list',
        method: 'get',
        params
    })
}
