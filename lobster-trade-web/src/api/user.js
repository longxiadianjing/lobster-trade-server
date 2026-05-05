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

// 获取登录日志
export function getLoginLogs() {
  return request({
    url: '/user/login-logs',
    method: 'get'
  })
}

// 获取登录设备列表
export function getLoginDevices() {
  return request({
    url: '/security/devices',
    method: 'get'
  })
}

// 切换设备可信状态
export function toggleDeviceTrust(deviceId) {
  return request({
    url: `/security/devices/${deviceId}/trust`,
    method: 'put'
  })
}

// 移除登录设备
export function removeLoginDevice(deviceId) {
  return request({
    url: `/security/devices/${deviceId}`,
    method: 'delete'
  })
}
