export interface AdminApprovalRequestDTO {
  roles?: string[]
  officeId?: string | null
}

export interface MessageResponseDTO {
  message: string
  timestamp: string // o LocalDateTime gestito come stringa in JSON
}

export interface PendingAccountDTO {
  accountId: string
  name: string
  surname: string
  phoneNumber: string
  photoUrl: string
  officeName: string
  email: string
  status: string
  roleNames: string[]
}

export interface AccountToApprove {
  accountId: string | null
  name: string
  surname: string
  phoneNumber: string
  photoUrl: string
  officeName: string
  email: string
  status: string
  roleNames: string[]
}
