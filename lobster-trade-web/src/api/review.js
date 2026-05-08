import request from '@/utils/request'

// Review APIs - some return {code,message,data} via Result, need careful handling
function handleResponse(res) {
  // Result-style responses: {code, message, data} where code=0 means success
  if (res && res.code !== undefined) {
    if (res.code !== 0 && res.code !== 200) return Promise.reject(res)
    return res.data || res
  }
  // Standard API response
  return res
}

export const createReview = (data) => request({ url: '/review', method: 'post', data }).then(handleResponse)

export const getReview = (reviewId) => request({ url: `/review/${reviewId}` }).then(handleResponse)

export const getProductReviews = (productId) => request({ url: `/review/product/${productId}` }).then(handleResponse)

export const getSellerReviews = (sellerId) => request({ url: `/review/seller/${sellerId}` }).then(handleResponse)

export const getUserProfile = (userId) => request({ url: `/review/profile/${userId}` }).then(handleResponse)

export const replyReview = (reviewId, replyContent) => request({ url: `/review/reply/${reviewId}`, method: 'post', data: { replyContent } }).then(handleResponse)

export const createDispute = (data) => request({ url: '/dispute', method: 'post', data })

export const cancelDispute = (orderId) => request({ url: `/dispute/cancel/${orderId}`, method: 'post' })