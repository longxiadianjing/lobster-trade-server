import request from '@/utils/request'

export function getNotificationList(params) {
  return request({
    url: '/notification/list',
    method: 'get',
    params
  })
}

export function markNotificationRead(id) {
  return request({
    url: `/notification/${id}/read`,
    method: 'put'
  })
}

export function markAllNotificationsRead() {
  return request({
    url: '/notification/read-all',
    method: 'put'
  })
}

export function getUnreadCount() {
  return request({
    url: '/notification/unread-count',
    method: 'get'
  })
}