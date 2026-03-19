export interface NoticeItem {
  noticeId: number
  noticeTitle: string
  noticeContent: string
  noticeType: string
  businessType?: string
  businessId?: number
  readFlag: boolean
  readTime?: string
  createdAt: string
}

export interface NoticeCenter {
  unreadCount: number
  messages: NoticeItem[]
}
