import request from '@/utils/request'

// 发送短信验证码
export function sendSmsCode(phone, type) {
  return request({
    url: '/auth/sms/send',
    method: 'post',
    data: { phone, type }
  })
}

// 用户注册
export function register(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

// 用户登录
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

// 重置密码
export function resetPassword(data) {
  return request({
    url: '/auth/password/reset',
    method: 'post',
    data
  })
}
