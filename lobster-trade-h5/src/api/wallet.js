import request from './request'

export function getWalletInfo() {
  return request({
    url: '/wallet/info',
    method: 'get'
  })
}

export function recharge(data) {
  return request({
    url: '/wallet/recharge',
    method: 'post',
    data
  })
}

export function withdraw(data) {
  return request({
    url: '/wallet/withdraw',
    method: 'post',
    data
  })
}

export function getWalletTransactions(params) {
  return request({
    url: '/wallet/transactions',
    method: 'get',
    params
  })
}