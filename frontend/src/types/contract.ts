export type ContractType =
  | "FULL_TIME_FIXED_TERM_CONTRACT"
  | "PART_TIME_FIXED_TERM_CONTRACT"
  | "FULL_TIME_PERMANENT_CONTRACT"
  | "PART_TIME_PERMANENT_CONTRACT"
  | "APPRENTICESHIP_CONTRACT"
  | "EXTRACURRICULAR_INTERNSHIP"

export type ContractStatus = "ACTIVE" | "EXPIRED" | "SUSPENDED" | "TERMINATED"

export interface ContractDTO {
  userId: string
  startDate: string
  endDate?: string | null
  contractType: ContractType
  hoursOfLeaveDuePerYear: number
  daysOfLeaveDuePerYear: number
  workingHoursPerWeek: number
}

export interface ContractResponseDTO {
  id: string
  userId: string
  userName: string
  userSurname: string
  startDate: string
  endDate: string | null
  contractType: ContractType
  hoursOfLeaveDuePerYear: number
  daysOfLeaveDuePerYear: number
  workingHoursPerWeek: number
  contractStatus: ContractStatus
}

export interface UpdateContractDTO {
  startDate?: string
  endDate?: string | null
  contractType?: ContractType
  hoursOfLeaveDuePerYear?: number
  daysOfLeaveDuePerYear?: number
  workingHoursPerWeek?: number
  contractStatus?: ContractStatus
}
