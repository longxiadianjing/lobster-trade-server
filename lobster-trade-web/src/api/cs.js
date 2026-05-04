import axios from '@/utils/request'

export const startCsSession = (data) => axios.post('/cs/start', data)

export const getCsSession = (sessionId) => axios.get(`/cs/session/${sessionId}`)

export const sendCsMessage = (data) => axios.post('/cs/message', data)

export const closeCsSession = (sessionId) => axios.post(`/cs/close/${sessionId}`)

export const getMyCsSessions = () => axios.get('/cs/my-sessions')