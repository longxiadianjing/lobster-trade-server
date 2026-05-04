import request from '@/utils/request'

export function createOrder(data) {
  return request({
    url: '/order',
    method: 'post',
    data
  })
}

export function getBuyerOrders(params) {
  return request({
    url: '/order/buyer/list',
    method: 'get',
    params
  })
}

export function getSellerOrders(params) {
  return request({
    url: '/order/seller/list',
    method: 'get',
    params
  })
}

export function getOrderDetail(id) {
  return request({
    url: `/order/detail/${id}`,
    method: 'get'
  })
}

export function payOrder(id, paymentMethod) {
  return request({
    url: `/order/pay/${id}?paymentMethod=${paymentMethod}`,
    method: 'post'
  })
}

export function submitDelivery(id, deliveryImages, deliveryRemark) {
  return request({
    url: `/order/delivery/${id}`,
    method: 'post',
    params: { deliveryImages, deliveryRemark }
  })
}

export function confirmOrder(id) {
  return request({
    url: `/order/confirm/${id}`,
    method: 'post'
  })
}

export function cancelOrder(id, reason) {
  return request({
    url: `/order/cancel/${id}`,
    method: 'post',
    params: { reason }
  })
}

export function submitReview(orderId, role, rating, content) {
  return request({
    url: '/review/create',
    method: 'post',
    params: { orderId, role, rating, content }
  })
}

export function submitDispute(orderId, reason, description, images) {
  return request({
    url: '/dispute',
    method: 'post',
    data: { orderId, reason, description, images }
  })
}
