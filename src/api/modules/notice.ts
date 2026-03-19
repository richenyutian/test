import request from '../request'
import type { NoticeCenter } from '../../types/notice'

export const getNoticeCenterApi = () =>
  request.get<unknown, NoticeCenter>('/v1/notifications')

export const markNoticeReadApi = (noticeId: number) =>
  request.put<unknown, null>(`/v1/notifications/${noticeId}/read`)
