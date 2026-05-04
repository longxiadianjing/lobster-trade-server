import request from '@/utils/request'

export function getGameList() {
  return request({
    url: '/games',
    method: 'get'
  })
}
