/**
 * IM相关API
 */
import request from '../utils/request'

export function getMySessions() {
  return request.get('/im/sessions')
}

export function getSession(sessionId) {
  return request.get('/im/session/' + sessionId)
}

export function getOrCreateSessionByOrder(orderId) {
  return request.post('/im/session/by-order/' + orderId)
}

export function sendMessage(data) {
  return request.post('/im/message', data)
}

export function markRead(sessionId) {
  return request.post('/im/session/' + sessionId + '/read')
}

export function getOrCreateSessionByProduct(productId) {
  return request.post('/im/session/by-product/' + productId)
}
