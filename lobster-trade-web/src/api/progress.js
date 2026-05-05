/**
 * 订单进度API（代练服务）
 */
import request from '../utils/request'

export function getOrderProgress(orderId) {
  return request.get('/order/progress/' + orderId)
}

export function updateOrderProgress(orderId, data) {
  return request({
    url: '/order/progress/' + orderId,
    method: 'post',
    params: data
  })
}

export function ackOrderProgress(orderId, ack) {
  return request({
    url: '/order/progress/' + orderId + '/ack',
    method: 'post',
    params: { ack: ack ? 1 : 0 }
  })
}
