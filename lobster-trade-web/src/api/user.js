import request from '@/utils/request'

// 获取用户信息
export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

// 更新用户信息
export function updateUserInfo(data) {
  return request({
    url: '/user/info',
    method: 'put',
    data
  })
}

// 设置支付密码
export function setPayPassword(payPassword) {
  return request({
    url: '/user/pay-password/set',
    method: 'post',
    data: { payPassword }
  })
}

// 申请实名认证
export function applyRealName(data) {
  return request({
    url: '/user/real-name/apply',
    method: 'post',
    data
  })
}

// 查询实名状态
export function getRealNameStatus() {
  return request({
    url: '/user/real-name/status',
    method: 'get'
  })
}
