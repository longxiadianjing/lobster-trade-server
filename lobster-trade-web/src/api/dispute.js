// 用户申诉 API
import request from '@/utils/request'

export function submitDispute(orderId, reason, description, images) {
  return request({
    url: '/dispute',
    method: 'post',
    data: { orderId, reason, description, images }
  })
}

export function getDisputeDetail(orderId) {
  return request({
    url: `/dispute/${orderId}`,
    method: 'get'
  })
}
