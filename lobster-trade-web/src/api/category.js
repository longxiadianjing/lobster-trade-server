import request from '@/utils/request'

export function getCategoriesByGameId(gameId) {
  return request({
    url: `/category/game/${gameId}`,
    method: 'get'
  })
}