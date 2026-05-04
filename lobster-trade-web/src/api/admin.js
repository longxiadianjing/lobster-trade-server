import request from '@/utils/request'

export function getAdminInfo() {
  return request({ url: '/admin/info', method: 'get' })
}

export function adminLogin(data) {
  return request({ url: '/admin/login', method: 'post', data })
}

// 管理员CRUD
export function getAdminList(params) {
  return request({ url: '/admin/admins', method: 'get', params })
}

export function getAdminById(id) {
  return request({ url: `/admin/admin/${id}`, method: 'get' })
}

export function createAdmin(data) {
  return request({ url: '/admin/admin', method: 'post', data })
}

export function updateAdmin(id, data) {
  return request({ url: `/admin/admin/${id}`, method: 'put', data })
}

export function deleteAdmin(id) {
  return request({ url: `/admin/admin/${id}`, method: 'delete' })
}

export function updateAdminStatus(id, status) {
  return request({ url: `/admin/admin/${id}/status`, method: 'post', data: { status } })
}

export function grantPermissions(id, permissions) {
  return request({ url: `/admin/admin/${id}/permissions`, method: 'post', data: { permissions } })
}

export function assignRole(id, roleCode) {
  return request({ url: `/admin/admin/${id}/assign-role`, method: 'post', data: { roleCode } })
}

// 角色管理
export function getRoles() {
  return request({ url: '/admin/roles', method: 'get' })
}

export function createRole(data) {
  return request({ url: '/admin/role', method: 'post', data })
}

export function updateRole(id, data) {
  return request({ url: `/admin/role/${id}`, method: 'put', data })
}

export function deleteRole(id) {
  return request({ url: `/admin/role/${id}`, method: 'delete' })
}

// 权限说明列表
export function getPermissions() {
  return request({ url: '/admin/permissions', method: 'get' })
}

// 统计
export function getStatsOverview() {
  return request({ url: '/admin/stats/overview', method: 'get' })
}

// 用户
export function getUserList(params) {
  return request({ url: '/admin/users', method: 'get', params })
}
export function banUser(id) {
  return request({ url: `/admin/user/${id}/ban`, method: 'post' })
}
export function unbanUser(id) {
  return request({ url: `/admin/user/${id}/unban`, method: 'post' })
}

// 订单
export function getOrderList(params) {
  return request({ url: '/admin/orders', method: 'get', params })
}
export function updateOrder(id, data) {
  return request({ url: `/admin/order/${id}`, method: 'put', data })
}

// 商品
export function getProductList(params) {
  return request({ url: '/admin/products', method: 'get', params })
}
export function productOff(id) {
  return request({ url: `/admin/product/${id}/off`, method: 'post' })
}
export function productOn(id) {
  return request({ url: `/admin/product/${id}/on`, method: 'post' })
}
export function productBan(id) {
  return request({ url: `/admin/product/${id}/ban`, method: 'post' })
}

// 纠纷
export function getDisputeList(params) {
  return request({ url: '/admin/disputes', method: 'get', params })
}
export function resolveDispute(id, result) {
  return request({ url: `/admin/dispute/resolve/${id}`, method: 'post', data: { result } })
}

// 游戏
export function getGameList() {
  return request({ url: '/admin/games', method: 'get' })
}
export function createGame(data) {
  return request({ url: '/admin/game', method: 'post', data })
}
export function updateGame(id, data) {
  return request({ url: `/admin/game/${id}`, method: 'put', data })
}
export function toggleGameStatus(id, status) {
  return request({ url: `/admin/game/${id}/status`, method: 'post', data: { status } })
}

// 公告
export function getAnnouncementList(params) {
  return request({ url: '/admin/announcement/list', method: 'get', params })
}
export function createAnnouncement(data) {
  return request({ url: '/admin/announcement', method: 'post', data })
}
export function updateAnnouncement(id, data) {
  return request({ url: `/admin/announcement/${id}`, method: 'put', data })
}
export function deleteAnnouncement(id) {
  return request({ url: `/admin/announcement/${id}`, method: 'delete' })
}

// 优惠券
export function getCouponList(params) {
  return request({ url: '/admin/coupon/list', method: 'get', params })
}
export function createCoupon(data) {
  return request({ url: '/admin/coupon', method: 'post', data })
}
export function updateCoupon(id, data) {
  return request({ url: `/admin/coupon/${id}`, method: 'put', data })
}

// 客服
export function getCsSessions(params) {
  return request({ url: '/admin/cs/sessions', method: 'get', params })
}
export function getCsStats() {
  return request({ url: '/admin/cs/stats', method: 'get' })
}

// 实名认证
export function getCertList(params) {
  return request({ url: '/admin/certification/pending', method: 'get', params })
}
export function approveCert(id) {
  return request({ url: `/admin/certification/${id}/approve`, method: 'post' })
}
export function rejectCert(id) {
  return request({ url: `/admin/certification/${id}/reject`, method: 'post' })
}

// 系统通知
export function getNotificationList(params) {
  return request({ url: '/admin/notification/list', method: 'get', params })
}
export function getUnreadCount() {
  return request({ url: '/admin/notification/unread-count', method: 'get' })
}
export function sendNotification(data) {
  return request({ url: '/admin/notification/send', method: 'post', data })
}

// 工单
export function getTicketList(params) {
  return request({ url: '/admin/ticket/list', method: 'get', params })
}
export function getTicketStats() {
  return request({ url: '/admin/ticket/stats', method: 'get' })
}

// 评价
export function getReviewList(params) {
  return request({ url: '/admin/review/list', method: 'get', params })
}
export function hideReview(id) {
  return request({ url: `/admin/review/${id}/hide`, method: 'post', data: { id } })
}

// 热搜词
export function getHotSearchList(params) {
  return request({ url: '/admin/hot-search/list', method: 'get', params })
}
export function updateHotSearch(id, data) {
  return request({ url: `/admin/hot-search/${id}`, method: 'put', data })
}

// 推荐位
export function getRecommendSlotList() {
  return request({ url: '/admin/recommend-slot/list', method: 'get' })
}
export function updateRecommendSlot(id, data) {
  return request({ url: `/admin/recommend-slot/${id}`, method: 'put', data })
}

// 审计日志
export function getAuditLogList(params) {
  return request({ url: '/admin/audit/list', method: 'get', params })
}

// ========== 向后兼容别名（适配旧页面）==========
export const getAdminUsers = getUserList
export const getAdminProducts = getProductList
export const getAdminOrders = getOrderList
export const getAdminAnnouncements = getAnnouncementList
export const getAdminGames = getGameList
export const getDisputes = getDisputeList
export const getAdminStatsOverview = getStatsOverview
export const getAdminInfo2 = getAdminInfo
export const setGameStatus = toggleGameStatus
