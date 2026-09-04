export interface OfficeResponseDTO {
  id: string
  name: string
  street: string
  houseNumber: string
  zipCode: string
  city: string
  province: string
  openingTime: string
  closingTime: string
  status: string
  latitude: number
  longitude: number
}

export type OfficeStatus =
  | "ATTIVO"
  | "CHIUSO TEMPORANEAMENTE"
  | "CHIUSO DEFINITIVAMENTE"

export interface officeList {
  id: string
  name: string
  street: string
  houseNumber: string
  zipCode: string
  city: string
  province: string
  openingTime: string
  closingTime: string
  officeStatus: OfficeStatus
  latitude: number
  longitude: number
}
