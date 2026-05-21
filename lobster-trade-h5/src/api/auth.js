import request from './request'

export function sendSmsCode(phone, type) {
  return request({
    url: '/auth/sms/send',
    method: 'post',
    data: { phone, type }
  })
}

export function register(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export function resetPassword(data) {
  return request({
    url: '/auth/password/reset',
    method: 'post',
    data
  })
}