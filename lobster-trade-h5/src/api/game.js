import request from './request'

export function getGameList() {
  return request({
    url: '/games',
    method: 'get'
  })
}

export function getGameTree() {
  return request({
    url: '/game/tree',
    method: 'get'
  })
}