import type { RequestType } from "../components/users/UserRequests"
import type { AssignmentType } from "./shift"

export interface HolidayRequestDTO {
  startDate: string
  endDate: string
  employeeNotes?: string
}

export type LeaveHoursType =
  | "ROL"
  | "LAW_104"
  | "STUDY_LEAVE"
  | "BLOOD_DONATION_LEAVE"
  | "BEREAVEMENT_LEAVE"
  | "PERSONAL_LEAVE"
  | "MEDICAL_APPOINTMENT"

export interface LeaveHoursRequestDTO {
  date: string
  startTime: string
  endTime: string
  leaveHoursType?: LeaveHoursType
  employeeNotes?: string
}

export type CertificateType =
  | "SICKNESS"
  | "MATERNITY"
  | "PATERNITY"
  | "PARENTAL_LEAVE"

export interface AbsenceCertificationRequestDTO {
  protocolCode: string
  startDate: string
  endDate: string
  issueDate: string
  certificateFile: File
  certificateType: CertificateType
  employeeNotes?: string
}

export type RequestStatus = "SENT" | "APPROVED" | "REJECTED" | "CANCELLED"

export interface HolidayRequestResponse {
  id: string
  startDate: string
  endDate: string
  employeeNotes?: string
  createdAt: string
  requestStatus: RequestStatus
  reviewerNotes?: string
}

export interface LeaveHoursRequestResponse {
  id: string
  date: string
  startTime: string
  endTime: string
  leaveHoursType: LeaveHoursType
  employeeNotes?: string
  createdAt: string
  requestStatus: RequestStatus
  reviewerNotes?: string
}

export interface AbsenceCertificationResponse {
  id: string
  protocolCode: string
  startDate: string
  endDate: string
  totalDays: number
  issueDate: string
  certificateUrl: string
  certificateType: CertificateType
  requestStatus: RequestStatus
  createdAt: string
  employeeNotes?: string
  reviewerNotes?: string
}

export interface UpdateCertificationRequestDTO {
  protocolCode?: string
  startDate?: string
  endDate?: string
  issueDate?: string
  certificateFile?: File
  certificateType?: CertificateType
  employeeNotes?: string
}

export interface UpdateHolidayRequestDTO {
  startDate?: string
  endDate?: string
  employeeNotes?: string
}

export interface UpdateLeaveHoursRequestDTO {
  date?: string
  startTime?: string
  endTime?: string
  leaveHoursType?: LeaveHoursType
  employeeNotes?: string
}

export interface RequestResponse {
  id: string
  employeeName: string
  requestStatus: string
  requestType: string
  createdAt: string
  employeeNotes: string | null
  reviewerNotes: string | null
  reviewerName: string | null

  startDate: string | null
  endDate: string | null
  startTime: string | null
  endTime: string | null
  date: string | null
  totalHours: number | null
  totalDays: number | null
  protocolCode: string | null
}

export interface ChangeHolidayRequestDTO {
  startDate: string
  endDate: string
  employeeNotes?: string
}

export interface ChangeLeaveHoursRequestDTO {
  date: string
  startTime: string
  endTime: string
  leaveHoursType: LeaveHoursType
  employeeNotes: string
}

export interface ChangeHolidayRequestResponseDTO {
  id: string
  originalRequestId: string
  startDate: string
  endDate: string
  employeeNotes: string | null
  startDateOriginalRequest: string
  endDateOriginalRequest: string
  createdAt: string
  changeRequestStatus: RequestStatus
  reviewCount: number
  totalDays: number
  reviewerName: string
  responseDate: string
  employeeName: string
  reviewerNotes: string
  originalRequestCreatedAt: string
  originalRequestEmployeeNotes: string
  originalRequestTotalDays: number
  originalReviewerNotes: string
  originalResponseDate: string
}

export interface ChangeLeaveHoursRequestResponseDTO {
  id: string
  originalRequestId: string
  date: string
  startTime: string
  endTime: string
  employeeNotes: string | null
  startTimeOriginalRequest: string
  endTimeOriginalRequest: string
  createdAt: string
  changeRequestStatus: RequestStatus
  reviewCount: number
  totalHours: number
  reviewerName: string
  responseDate: string
  leaveHoursType?: LeaveHoursType
  employeeName: string
  reviewerNotes: string
  originalRequestCreatedAt: string
  originalRequestEmployeeNotes: string
  originalRequestTotalHours: number
  originalReviewerNotes: string
  originalResponseDate: string
  originalDate: string
}

export interface ChangeLeaveHoursRequestReviewerResponseDTO {
  id: string
  originalRequestId: string
  date: string
  startTime: string
  endTime: string
  employeeNotes: string
  startTimeOriginalRequest: string
  endTimeOriginalRequest: string
  createdAt: string
  requestStatus: RequestStatus
  reviewCount: number
  responseDate: string
  reviewerNotes: string
  reviewerName: string
  assignmentType: AssignmentType
  totalHours: number
  originalRequestCreatedAt: string
  originalRequestEmployeeNotes: string
  originalRequestTotalHours: number
}

export interface ChangeHolidayRequestReviewerResponseDTO {
  id: string
  originalRequestId: string
  startDate: string
  endDate: string
  employeeNotes: string
  startDateOriginalRequest: string
  endDateOriginalRequest: string
  createdAt: string
  requestStatus: RequestStatus
  reviewCount: number
  responseDate: string
  reviewerNotes: string
  reviewerName: string
  assignmentType: AssignmentType
  totalDays: number
  originalRequestCreatedAt: string
  originalRequestEmployeeNotes: string
  originalRequestTotalDays: number
}

export interface RequestResponseDTO {
  id: string
  employeeName?: string
  requestStatus: string
  requestType: RequestType
  createdAt: string
  employeeNotes?: string
  reviewerNotes?: string
  reviewerName?: string
  startDate?: string
  endDate?: string
  startTime?: string
  endTime?: string
  date?: string
  totalHours?: number
  totalDays?: number
  protocolCode?: string
  originalRequestCreatedAt: string
  endDateOriginalRequest: string
  startDateOriginalRequest: string
  originalResponseDate: string
  responseDate: string
  originalRequestEmployeeNotes: string
  originalRequestReviewerNotes: string
  originalRequestTotalDays: number
  originalRequestTotalHours: number
  originalDate: string
  startTimeOriginalRequest: string
  endTimeOriginalRequest: string
  certificationType: CertificateType
}

export interface HrCertificationRequest {
  id: string
  protocolCode: string
  startDate: string
  endDate: string
  totalDays: number
  issueDate: string
  certificateUrl: string
  certificateType: CertificateType
  requestStatus: RequestStatus
  createdAt: string
  employeeNotes: string | null
  reviewerNotes: string | null
  employeeName: string
}

export interface ReviewerNotesDTO {
  notes: string
}

export interface AbsenceCertificationReviewResponse {
  id: string
  protocolCode: string
  startDate: string
  endDate: string
  totalDays: number
  issueDate: string
  certificateUrl: string
  certificateType: CertificateType
  requestStatus: RequestStatus
  createdAt: string
  employeeNotes: string | null
  reviewerNotes: string | null
  reviewerRole: string[]
  reviewer: string | null
  assignmentType: AssignmentType
  employeeName: string
}
