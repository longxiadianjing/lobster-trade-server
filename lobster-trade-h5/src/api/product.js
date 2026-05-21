import request from './request'

export function getProductList(params) {
  return request({
    url: '/product/list',
    method: 'get',
    params
  })
}

export function getProductDetail(id) {
  return request({
    url: `/product/detail/${id}`,
    method: 'get'
  })
}

export function getMyProducts() {
  return request({
    url: '/product/my',
    method: 'get'
  })
}

export function searchProduct(params) {
  return request({
    url: '/product/search',
    method: 'get',
    params
  })
}

export function publishProduct(data) {
  return request({
    url: '/product',
    method: 'post',
    data
  })
}

export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/upload',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}