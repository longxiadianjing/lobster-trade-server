/**
 * 订单进度API（代练服务）
 */
import request from '../utils/request'

export function getOrderProgress(orderId) {
  return request.get('/order/progress/' + orderId)
}

export function updateOrderProgress(orderId, data) {
  return request.post('/order/progress/' + orderId, null, { params: data })
}

export function ackOrderProgress(orderId, ack) {
  return request.post('/order/progress/' + orderId + '/ack', null, { params: { ack } })
}
