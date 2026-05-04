import axios from '@/utils/request'

export const createReview = (data) => axios.post('/review', data)

export const getReview = (reviewId) => axios.get(`/review/${reviewId}`)

export const getProductReviews = (productId) => axios.get(`/review/product/${productId}`)

export const getSellerReviews = (sellerId) => axios.get(`/review/seller/${sellerId}`)

export const getUserProfile = (userId) => axios.get(`/review/profile/${userId}`)

export const replyReview = (reviewId, replyContent) => axios.post(`/review/reply/${reviewId}`, { replyContent })

export const createDispute = (data) => axios.post('/dispute', data)

export const cancelDispute = (orderId) => axios.post(`/dispute/cancel/${orderId}`)