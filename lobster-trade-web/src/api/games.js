import request from '@/utils/request'

export function getGames(params) {
  return request({
    url: '/games',
    method: 'get',
    params
  })
}
