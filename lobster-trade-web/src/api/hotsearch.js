import request from '@/utils/request'

export function getHotSearchWords() {
  return request({
    url: '/hot-search/words',
    method: 'get'
  })
}
