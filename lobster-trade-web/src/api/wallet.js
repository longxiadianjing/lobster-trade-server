import request from '@/utils/request'

// 获取钱包信息
export function getWalletInfo() {
  return request({
    url: '/wallet/info',
    method: 'get'
  })
}

// 钱包充值
export function recharge(data) {
  return request({
    url: '/wallet/recharge',
    method: 'post',
    data
  })
}

// 提现申请
export function withdraw(data) {
  return request({
    url: '/wallet/withdraw',
    method: 'post',
    data
  })
}

// 获取钱包流水
export function getWalletTransactions(params) {
  return request({
    url: '/wallet/transactions',
    method: 'get',
    params
  })
}
