// ==========================
// PAGINAZIONE
// ==========================
export interface UserAccount {
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

export interface PageResponse<T> {
  content: T[]
  pageable?: {
    pageNumber: number
    pageSize: number
  }
  totalElements?: number
  totalPages?: number
  last?: boolean
  size?: number
  number?: number
}

// ==========================
// AGGIORNA IL TUO PROFILO CON LE INFO DI REGISTRAZIONE
// ==========================

export interface UpdateProfilePayload {
  phoneNumber?: string | null
  photoUrl?: File | null
  streetAddress?: string | null
  houseNumber?: string | number | null
  zipCode?: string | null
  iban?: string | null
  documentNumber?: string | null
  documentType?: string | null
  issueDate?: string | null
  expirationDate?: string | null
  documentFront?: File | null
  documentBack?: File | null
  taxCodeFront?: File | null
  taxCodeBack?: File | null
}

export interface UserProfileResponse {
  accountId: string
  name: string
  surname: string
  email: string
  phoneNumber: string
  photoUrl: string
  streetAddress: string
  houseNumber: string
  zipCode: string
  iban: string
  documentNumber: string
  documentType: string
  issueDate: string
  expirationDate: string
  documentFrontUrl: string
  documentBackUrl: string
  taxCodeCardFrontUrl: string
  taxCodeCardBackUrl: string
  officeName: string
  roleNames: string[]
}

// ==========================
//ADMIN ACCETTA E ASSEGNA I RUOLI
// ==========================

export interface UpdateUserRolesPayload {
  roles: string[] // Es. ['SHIFT MANAGER'], ['COORDINATOR'], ecc.
}

export interface UpdateUserRolesResponse {
  message: string
  timestamp: string
}

// ==========================
//ADMIN CAMBIA SEDE A COORDINATOR
// ==========================
export interface CoordinatorResponse {
  userId: string
  name: string
  surname: string
  phoneNumber: string
  email: string
  officeName: string
  roleNames: string[]
}

//SUMMARY

export interface UserSummaryResponse {
  accruedDaysTotal: number
  accruedDaysThisMonth: number
  usedDays: number
  remainingDays: number

  accruedHoursTotal: number
  accruedHoursThisMonth: number
  usedHours: number
  remainingHours: number
}

//UTENTI ATTIVI
export interface ActiveUser {
  userId: string
  name: string
}

export type DocumentType = "IDENTITY_CARD" | "PASSPORT" | "DRIVING_LICENSE"
export type AccountStatus = "PENDING" | "REJECTED" | "DISABLED" | "ACTIVE"

export interface UserProfileResponseDTO {
  userId: string
  name: string
  surname: string
  dateOfBirth: string
  placeOfBirth: string
  phoneNumber: string
  taxCode: string
  photoUrl: string | null
  streetAddress: string
  houseNumber: string
  zipCode: string
  city: string
  province: string
  iban: string
  documentNumber: string
  documentType: DocumentType
  issueDate: string
  expirationDate: string
  documentFrontUrl: string | null
  documentBackUrl: string | null
  taxCodeCardFrontUrl: string | null
  taxCodeCardBackUrl: string | null
  officeName: string
  roleNames: string[]
  email: string
  accountStatus: AccountStatus
  officeId: string
  accountId: string
}

export interface UserBasicInformationResponseDTO {
  userId: string
  name: string
  surname: string
  phoneNumber: string
  email: string
  officeName: string
  roleNames: string[]
}
