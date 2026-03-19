import request from '../request'
import type { PageResponse } from '../../types/api'
import type {
  TicketAcceptRequest,
  TicketAssignRequest,
  TicketAttachment,
  TicketCloseRequest,
  TicketCompleteRequest,
  TicketCreateRequest,
  TicketDetail,
  TicketEscalateRequest,
  TicketLifecycleRule,
  TicketListItem,
  TicketReopenRequest,
  TicketSuspendRequest,
  TicketTransferRequest,
} from '../../types/ticket'

export const getTicketPageApi = (params: Record<string, unknown>) =>
  request.get<unknown, PageResponse<TicketListItem>>('/v1/tickets', { params })

export const getTicketDetailApi = (ticketId: number) =>
  request.get<unknown, TicketDetail>(`/v1/tickets/${ticketId}`)

export const getTicketLifecycleRulesApi = () =>
  request.get<unknown, TicketLifecycleRule[]>('/v1/tickets/lifecycle-rules')

export const createTicketApi = (payload: TicketCreateRequest) =>
  request.post<unknown, number>('/v1/tickets', payload)

export const acceptTicketApi = (ticketId: number, payload: TicketAcceptRequest) =>
  request.put<unknown, null>(`/v1/tickets/${ticketId}/accept`, payload)

export const assignTicketApi = (ticketId: number, payload: TicketAssignRequest) =>
  request.put<unknown, null>(`/v1/tickets/${ticketId}/assign`, payload)

export const claimTicketApi = (ticketId: number) =>
  request.put<unknown, null>(`/v1/tickets/${ticketId}/claim`)

export const transferTicketApi = (ticketId: number, payload: TicketTransferRequest) =>
  request.put<unknown, null>(`/v1/tickets/${ticketId}/transfer`, payload)

export const suspendTicketApi = (ticketId: number, payload: TicketSuspendRequest) =>
  request.put<unknown, null>(`/v1/tickets/${ticketId}/suspend`, payload)

export const resumeTicketApi = (ticketId: number) =>
  request.put<unknown, null>(`/v1/tickets/${ticketId}/resume`)

export const completeTicketApi = (ticketId: number, payload: TicketCompleteRequest) =>
  request.put<unknown, null>(`/v1/tickets/${ticketId}/complete`, payload)

export const confirmTicketApi = (ticketId: number) =>
  request.put<unknown, null>(`/v1/tickets/${ticketId}/confirm`)

export const closeTicketApi = (ticketId: number, payload: TicketCloseRequest) =>
  request.put<unknown, null>(`/v1/tickets/${ticketId}/close`, payload)

export const reopenTicketApi = (ticketId: number, payload: TicketReopenRequest) =>
  request.put<unknown, null>(`/v1/tickets/${ticketId}/reopen`, payload)

export const escalateTicketApi = (ticketId: number, payload: TicketEscalateRequest) =>
  request.put<unknown, null>(`/v1/tickets/${ticketId}/escalate`, payload)

export const uploadTicketAttachmentApi = (ticketId: number, file: File) => {
  const formData = new FormData()
  formData.append('file', file)

  return request.post<unknown, TicketAttachment>(
    `/v1/tickets/${ticketId}/attachments`,
    formData,
    {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    },
  )
}

export const downloadTicketAttachmentApi = (attachmentId: number) =>
  request.get<unknown, Blob>(`/v1/tickets/attachments/${attachmentId}/download`, {
    responseType: 'blob',
  })
