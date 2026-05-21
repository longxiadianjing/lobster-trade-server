import request from './request'

export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

export function updateUserInfo(data) {
  return request({
    url: '/user/info',
    method: 'put',
    data
  })
}

export function setPayPassword(payPassword) {
  return request({
    url: '/user/pay-password/set',
    method: 'post',
    data: { payPassword }
  })
}

export function applyRealName(data) {
  return request({
    url: '/real-name/apply',
    method: 'post',
    data
  })
}

export function getRealNameStatus() {
  return request({
    url: '/real-name/status',
    method: 'get'
  })
}