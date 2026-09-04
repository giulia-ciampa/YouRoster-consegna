// ==========================
// REGISTRATION
// ==========================

export interface RegisterPayload {
  name: string
  surname: string
  taxCode: string
  dateOfBirth: string
  placeOfBirth: string
  nationality: string
  phoneNumber: string
  streetAddress: string
  houseNumber: string
  zipCode: string
  city: string
  province: string
  referenceOfficeId?: string | null
  iban: string
  documentNumber: string
  documentType: string
  issueDate: string
  expirationDate: string
  documentFront: File | null
  documentBack: File | null
  taxCodeFront: File | null
  taxCodeBack: File | null
  email: string
  password: string
  confirmPassword: string
  avatar?: File | null
}

export interface ResisterResponse {
  id: string
  name: string
  surname: string
  email: string
  message: string
  time: string
}

// ==========================
// LOGIN
// ==========================

export interface LoginPayload {
  email: string
  password?: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  photoUrl: string
  roleName: string
}

// ==========================
// MODIFICHE EMAIL E PASSWORD
// ==========================

export interface UpdateCredentialsPayload {
  email?: string
  oldPassword?: string
  newPassword?: string
  confirmNewPassword?: string
}

export interface UpdateCredentialsResponse {
  message: string
  time: string
}

export interface ApiErrorResponse {
  errorsList?: string[]
  message?: string
}
