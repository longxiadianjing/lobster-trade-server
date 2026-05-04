import request from '@/utils/request'

export function getRecommendedSlots(slotKey) {
  return request({
    url: '/recommend-slot/active',
    method: 'get',
    params: { slotKey }
  })
}

export function getBatchSlots(slotKeys) {
  return request({
    url: '/recommend-slot/batch',
    method: 'get',
    params: { slotKeys }
  })
}
