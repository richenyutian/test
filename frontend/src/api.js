import axios from 'axios'

const client = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

client.interceptors.response.use(
  (response) => {
    const payload = response.data
    if (payload && payload.success === false) {
      return Promise.reject(new Error(payload.message || '请求失败'))
    }
    return payload?.data
  },
  (error) => Promise.reject(new Error(error.response?.data?.message || error.message || '请求失败')),
)

export function apiGet(url, params) {
  return client.get(url, { params })
}

export function apiPost(url, data) {
  return client.post(url, data)
}
