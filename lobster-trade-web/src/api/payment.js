import request from '../utils/request'

// 创建充值支付单
export function createRechargePayment(amount, channel) {
  return request.post('/payment/create-recharge', null, { params: { amount, channel } })
}

// 创建订单支付单
export function createOrderPayment(orderId, channel) {
  return request.post('/payment/create-order-pay', null, { params: { orderId, channel } })
}

// 查询支付状态
export function getPaymentStatus(paymentNo) {
  return request.get('/payment/status/' + paymentNo)
}

// 模拟支付回调
export function mockPaymentCallback(paymentNo) {
  return request.post('/payment/mock-callback', null, { params: { paymentNo } })
}