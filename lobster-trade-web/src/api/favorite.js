import request from '@/utils/request'

export function addFavorite(productId) {
  return request.post('/favorite/add', null, { params: { productId } })
}

export function removeFavorite(productId) {
  return request.delete('/favorite/remove', { params: { productId } })
}

export function getMyFavorites() {
  return request.get('/favorite/list')
}

export function checkFavorite(productId) {
  return request.get('/favorite/check', { params: { productId } })
}