import request from '@/utils/request'

export function getMyCoupons(status) {
  return request({
    url: '/coupon/my',
    method: 'get',
    params: { status }
  })
}

export function getAvailableCoupons() {
  return request({
    url: '/coupon/available',
    method: 'get'
  })
}

export function receiveCoupon(couponId) {
  return request({
    url: '/coupon/receive',
    method: 'post',
    data: { couponId }
  })
}

export function getUsableCouponsForOrder(amount) {
  return request({
    url: '/coupon/order/usable',
    method: 'get',
    params: { amount }
  })
}