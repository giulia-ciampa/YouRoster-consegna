import {
  Button,
  Card,
  Col,
  Container,
  Row,
  Spinner,
  Table,
  Form,
  Modal,
} from "react-bootstrap"
import { UserNavbar } from "./UserNavbar"
import { useEffect, useState } from "react"
import {
  cancelMyChangeHolidayRequest,
  cancelMyChangeLeaveHoursRequest,
  createAbsenceCertificationRequest,
  createChangeHolidayRequest,
  createChangeLeaveHoursRequest,
  createHolidayRequest,
  createLeaveHoursRequest,
  deleteAbsenceCertificationRequest,
  deleteHolidayRequest,
  deleteLeaveHoursRequest,
  getMyAbsenceCertifications,
  getMyChangeHolidayRequest,
  getMyChangeHolidayRequests,
  getMyChangeLeaveHoursRequest,
  getMyChangeLeaveHoursRequests,
  getMyHolidayRequests,
  getMyLeaveHoursRequests,
  getUserLeaveSummary,
  updateAbsenceCertificationRequest,
  updateHolidayRequest,
  updateLeaveHoursRequest,
} from "../../services/requestService"
import type { UserSummaryResponse } from "../../types/users"
import { IoMdAdd } from "react-icons/io"
import type {
  CertificateType,
  ChangeHolidayRequestResponseDTO,
  ChangeLeaveHoursRequestResponseDTO,
  LeaveHoursType,
} from "../../types/requests"
import { FaPencilAlt, FaRegTrashAlt } from "react-icons/fa"
import "../../styles/mobileText.css"
import type { ApiErrorResponse } from "../../types/auth"

export type RequestType =
  | "HOLIDAY"
  | "LEAVE_HOURS"
  | "CERTIFICATION"
  | "CHANGE_LEAVE_HOURS"
  | "CHANGE_HOLIDAY"

export type UserRequest = {
  id: string
  requestType: RequestType
  createdAt: string
  startDate?: string
  endDate?: string
  date?: string
  startTime?: string
  endTime?: string
  requestStatus: string
  employeeNotes?: string
  reviewerNotes?: string
  reviewer?: string
  leaveHoursType?: LeaveHoursType
  certificateType?: CertificateType
  protocolCode?: string
  issueDate?: string
  totalDays?: number
  totalHours?: number
  employeeName?: string
  startDateOriginalRequest?: string
  endDateOriginalRequest?: string
  startTimeOriginalRequest?: string
  endTimeOriginalRequest?: string
  originalRequestCreatedAt?: string
  originalRequestTotalDays?: number
  originalRequestTotalHours?: number
  originalRequestEmployeeNotes?: string
  responseDate?: string
  originalReviewerNotes?: string
  originalResponseDate?: string
  originalDate?: string
}

export const UserRequests = () => {
  const [showDetails, setShowDetails] = useState(false)

  const [summary, setSummary] = useState<UserSummaryResponse | null>(null)
  const [loadingSummary, setLoadingSummary] = useState(true)

  const [showRequestModal, setShowRequestModal] = useState(false)
  const [requestType, setRequestType] = useState("")

  // FERIE E CERTIFICATO
  const [startDate, setStartDate] = useState("")
  const [endDate, setEndDate] = useState("")

  // PERMESSO ORARIO
  const [date, setDate] = useState("")
  const [startTime, setStartTime] = useState("")
  const [endTime, setEndTime] = useState("")
  const [leaveHoursType, setLeaveHoursType] = useState<LeaveHoursType | "">("")

  // CERTIFICATO
  const [protocolCode, setProtocolCode] = useState("")
  const [issueDate, setIssueDate] = useState("")
  const [certificateFile, setCertificateFile] = useState<File | null>(null)
  const [certificateType, setCertificateType] = useState<CertificateType | "">(
    "",
  )

  //note dipendente
  const [employeeNotes, setEmployeeNotes] = useState("")

  //richieste
  const [requests, setRequests] = useState<UserRequest[]>([])

  //ricarica tabella
  const [loadingRequest, setLoadingRequest] = useState(false)

  //modale per la modifica
  const [editingRequest, setEditingRequest] = useState<UserRequest | null>(null)

  //modifica richiesta approvata
  const [isApprovedEdit, setIsApprovedEdit] = useState(false)

  //richieste di modifica se approvata
  const [changeHolidayRequest, setChangeHolidayRequest] =
    useState<ChangeHolidayRequestResponseDTO | null>(null)

  const [changeLeaveHoursRequest, setChangeLeaveHoursRequest] =
    useState<ChangeLeaveHoursRequestResponseDTO | null>(null)

  const [changeHolidayRequests, setChangeHolidayRequests] = useState<
    ChangeHolidayRequestResponseDTO[]
  >([])

  const [changeLeaveHoursRequests, setChangeLeaveHoursRequests] = useState<
    ChangeLeaveHoursRequestResponseDTO[]
  >([])

  const certificateTypes = [
    { value: "SICKNESS", label: "Malattia" },
    { value: "MATERNITY", label: "Maternità" },
    { value: "PATERNITY", label: "Paternità" },
    { value: "PARENTAL_LEAVE", label: "Congedo parentale" },
  ]

  const leaveHoursTypes = [
    { value: "ROL", label: "ROL" },
    { value: "LAW_104", label: "Legge 104" },
    { value: "STUDY_LEAVE", label: "Permesso studio" },
    { value: "BLOOD_DONATION_LEAVE", label: "Donazione sangue" },
    { value: "BEREAVEMENT_LEAVE", label: "Permesso per lutto" },
    { value: "PERSONAL_LEAVE", label: "Permesso personale" },
    { value: "MEDICAL_APPOINTMENT", label: "Visita medica" },
  ]

  useEffect(() => {
    const loadSummary = async () => {
      try {
        const data = await getUserLeaveSummary()

        setSummary(data)
      } catch (error) {
        console.error("Errore nel caricamento del riepilogo:", error)
      } finally {
        setLoadingSummary(false)
      }
    }

    loadSummary()
  }, [])

  //funzione per resettare il form
  const resetForm = () => {
    setStartDate("")
    setEndDate("")
    setEmployeeNotes("")

    setDate("")
    setStartTime("")
    setEndTime("")
    setLeaveHoursType("")

    setProtocolCode("")
    setStartDate("")
    setEndDate("")
    setIssueDate("")
    setCertificateFile(null)
    setCertificateType("")
  }

  const loadRequest = async () => {
    try {
      setLoadingRequest(true)

      const [
        holidays,
        leaveHours,
        certifications,
        changeHolidays,
        changeLeaveHours,
      ] = await Promise.all([
        getMyHolidayRequests(),
        getMyLeaveHoursRequests(),
        getMyAbsenceCertifications(),
        getMyChangeHolidayRequests(),
        getMyChangeLeaveHoursRequests(),
      ])

      const allRequests: UserRequest[] = [
        ...holidays.content.map((request) => ({
          ...request,
          requestType: "HOLIDAY" as RequestType,
        })),

        ...leaveHours.content.map((request) => ({
          ...request,
          requestType: "LEAVE_HOURS" as RequestType,
        })),

        ...certifications.content.map((request) => ({
          ...request,
          requestType: "CERTIFICATION" as RequestType,
        })),
      ]

      allRequests.sort(
        (a, b) =>
          new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime(),
      )

      setRequests(allRequests)
      setChangeHolidayRequests(changeHolidays.content)
      setChangeLeaveHoursRequests(changeLeaveHours.content)
    } catch (error) {
      console.error("Errore nel caricamento delle richieste:", error)
    } finally {
      setLoadingRequest(false)
    }
  }

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadRequest()
  }, [])

  //funzione post richiesta
  const handleSubmitRequest = async () => {
    setLoadingRequest(true)

    try {
      if (!requestType) {
        throw new Error("Seleziona il tipo di richiesta.")
      }

      if (editingRequest) {
        // RICHIESTA APPROVATA
        if (isApprovedEdit) {
          if (requestType === "HOLIDAY") {
            await createChangeHolidayRequest(editingRequest.id, {
              startDate,
              endDate,
              employeeNotes,
            })
          }

          if (requestType === "LEAVE_HOURS") {
            if (!leaveHoursType) {
              throw new Error("Seleziona il tipo di permesso.")
            }

            await createChangeLeaveHoursRequest(editingRequest.id, {
              date,
              startTime,
              endTime,
              leaveHoursType,
              employeeNotes,
            })
          }

          if (requestType === "CERTIFICATION") {
            await updateAbsenceCertificationRequest(editingRequest.id, {
              protocolCode,
              startDate,
              endDate,
              issueDate,
              certificateFile: certificateFile || undefined,
              certificateType: certificateType || undefined,
              employeeNotes,
            })
          }

          setShowRequestModal(false)

          if (requestType === "CERTIFICATION") {
            alert("Richiesta modificata con successo!")
          } else {
            alert("Richiesta di modifica inviata con successo!")
          }

          resetForm()
          await loadRequest()
          return
        }
        if (!isApprovedEdit) {
          //MODIFICA RICHIESTA SENT
          if (requestType === "HOLIDAY") {
            await updateHolidayRequest(editingRequest.id, {
              startDate,
              endDate,
              employeeNotes,
            })
          }

          if (requestType === "LEAVE_HOURS") {
            await updateLeaveHoursRequest(editingRequest.id, {
              date,
              startTime,
              endTime,
              leaveHoursType: leaveHoursType || undefined,
              employeeNotes,
            })
          }

          if (requestType === "CERTIFICATION") {
            await updateAbsenceCertificationRequest(editingRequest.id, {
              protocolCode,
              startDate,
              endDate,
              issueDate,
              certificateFile: certificateFile || undefined,
              certificateType: certificateType || undefined,
              employeeNotes,
            })
          }

          setShowRequestModal(false)
          alert("Richiesta modificata con successo!")
          resetForm()
          await loadRequest()

          return
        }
      }

      //nuova richiesta
      if (requestType === "HOLIDAY") {
        await createHolidayRequest({
          startDate: startDate,
          endDate: endDate,
          employeeNotes,
        })
      }

      if (requestType === "LEAVE_HOURS") {
        if (!leaveHoursType) {
          throw new Error("Seleziona il tipo di permesso.")
        }

        await createLeaveHoursRequest({
          date: date,
          startTime: startTime,
          endTime: endTime,
          leaveHoursType,
          employeeNotes,
        })
      }

      if (requestType === "CERTIFICATION") {
        if (!certificateFile) {
          throw new Error("È necessario caricare il certificato.")
        }

        if (!certificateType) {
          throw new Error("Seleziona il tipo di certificato.")
        }

        if (!protocolCode) {
          throw new Error("Inserire il numero di protocollo.")
        }

        await createAbsenceCertificationRequest({
          protocolCode,
          startDate: startDate,
          endDate: endDate,
          issueDate,
          certificateFile,
          certificateType,
          employeeNotes,
        })
      }

      setShowRequestModal(false)
      alert("Richiesta inviata con successo!")

      resetForm()

      await loadRequest()
    } catch (error) {
      const apiError = error as ApiErrorResponse

      if (apiError.errorsList?.length) {
        alert(apiError.errorsList.join("\n"))
      } else {
        alert(apiError.message)
      }
    } finally {
      setLoadingRequest(false)
    }
  }

  //periodo per la tabella
  const getRequestPeriod = (request: UserRequest) => {
    if (request.requestType === "LEAVE_HOURS") {
      return (
        <>
          {request.date}
          <br />
          {request.startTime} - {request.endTime}
        </>
      )
    }

    if (request.startDate && request.endDate) {
      return `${request.startDate} - ${request.endDate}`
    }

    return "-"
  }

  //traduzione tipo di richiesta
  const getRequestTypeLabel = (requestType: RequestType) => {
    switch (requestType) {
      case "HOLIDAY":
        return "Ferie"

      case "LEAVE_HOURS":
        return "Ore di permesso"

      case "CERTIFICATION":
        return "Richiesta con certificato"

      default:
        return requestType
    }
  }

  //traduzione stato richiesta
  const getRequestStatusLabel = (status: string) => {
    switch (status) {
      case "SENT":
        return {
          label: "INVIATA",
          className: "text-warning bg-transparent",
        }

      case "APPROVED":
        return {
          label: "APPROVATA",
          className: "text-success",
        }

      case "REJECTED":
        return {
          label: "RIFIUTATA",
          className: "text-danger",
        }

      default:
        return {
          label: status,
          className: "",
        }
    }
  }

  //conversione date
  const convertDateForInput = (date: string) => {
    const [day, month, year] = date.split("/")
    return `${year}-${month}-${day}`
  }

  const handleEditRequest = (request: UserRequest) => {
    setEditingRequest(request)

    // capiamo se stiamo modificando una richiesta APPROVATA
    setIsApprovedEdit(request.requestStatus === "APPROVED")

    setRequestType(request.requestType)
    setEmployeeNotes(request.employeeNotes ?? "")

    if (request.requestType === "HOLIDAY") {
      setStartDate(
        request.startDate ? convertDateForInput(request.startDate) : "",
      )
      setEndDate(request.endDate ? convertDateForInput(request.endDate) : "")
    }

    if (request.requestType === "LEAVE_HOURS") {
      setDate(request.date ? convertDateForInput(request.date) : "")
      setStartTime(request.startTime ?? "")
      setEndTime(request.endTime ?? "")
      setLeaveHoursType(request.leaveHoursType ?? "")
    }

    if (request.requestType === "CERTIFICATION") {
      setProtocolCode(request.protocolCode ?? "")
      setStartDate(
        request.startDate ? convertDateForInput(request.startDate) : "",
      )
      setEndDate(request.endDate ? convertDateForInput(request.endDate) : "")
      setIssueDate(
        request.issueDate ? convertDateForInput(request.issueDate) : "",
      )
      setCertificateType(request.certificateType ?? "")
    }

    setShowRequestModal(true)
  }
  //eliminazione richiesta
  const handleDeleteRequest = async () => {
    if (!editingRequest) {
      return
    }

    const confirmed = window.confirm(
      "Sei sicuro di voler eliminare questa richiesta?",
    )

    if (!confirmed) {
      return
    }

    setLoadingRequest(true)

    try {
      if (requestType === "HOLIDAY") {
        await deleteHolidayRequest(editingRequest.id)
      }

      if (requestType === "LEAVE_HOURS") {
        await deleteLeaveHoursRequest(editingRequest.id)
      }

      if (requestType === "CERTIFICATION") {
        await deleteAbsenceCertificationRequest(editingRequest.id)
      }

      alert("Richiesta eliminata con successo!")

      setShowRequestModal(false)
      resetForm()
      await loadRequest()
    } catch (error) {
      console.error("Errore nell'eliminazione della richiesta:", error)

      alert(
        error instanceof Error
          ? error.message
          : "Si è verificato un errore nell'eliminazione della richiesta.",
      )
    } finally {
      setLoadingRequest(false)
    }
  }

  //FUNZIONE PER APRIRE I DETTAGLI
  const handleShowDetails = async (request: UserRequest) => {
    setEditingRequest(request)
    setRequestType(request.requestType)

    setChangeHolidayRequest(null)
    setChangeLeaveHoursRequest(null)

    if (
      request.requestStatus === "APPROVED" &&
      request.requestType === "HOLIDAY"
    ) {
      try {
        const changeRequest = await getMyChangeHolidayRequest(request.id)
        setChangeHolidayRequest(changeRequest)
      } catch {
        //nessuna richiesta di modifica presente
      }
    }

    if (
      request.requestStatus === "APPROVED" &&
      request.requestType === "LEAVE_HOURS"
    ) {
      try {
        const changeRequest = await getMyChangeLeaveHoursRequest(request.id)
        setChangeLeaveHoursRequest(changeRequest)
      } catch {
        // 404 = nessuna modifica presente, non è un errore
      }
    }

    // Il modale si apre SEMPRE
    setShowDetails(true)
  }

  const hasPendingChange = (request: UserRequest) => {
    if (request.requestStatus !== "APPROVED") {
      return false
    }

    if (request.requestType === "HOLIDAY") {
      return changeHolidayRequests.some(
        (change) =>
          change.originalRequestId === request.id &&
          change.changeRequestStatus === "SENT",
      )
    }

    if (request.requestType === "LEAVE_HOURS") {
      return changeLeaveHoursRequests.some(
        (change) =>
          change.originalRequestId === request.id &&
          change.changeRequestStatus === "SENT",
      )
    }

    return false
  }

  const handleCancelChangeRequest = async () => {
    try {
      if (requestType === "HOLIDAY" && changeHolidayRequest) {
        await cancelMyChangeHolidayRequest(changeHolidayRequest.id)
      }

      if (requestType === "LEAVE_HOURS" && changeLeaveHoursRequest) {
        await cancelMyChangeLeaveHoursRequest(changeLeaveHoursRequest.id)
      }

      alert("Richiesta di modifica annullata con successo.")

      setShowDetails(false)

      // Pulizia
      setChangeHolidayRequest(null)
      setChangeLeaveHoursRequest(null)
    } catch (error) {
      console.error("Errore nell'annullamento:", error)

      alert(
        error instanceof Error
          ? error.message
          : "Errore nell'annullamento della richiesta di modifica.",
      )
    }
  }
  return (
    <>
      <UserNavbar />
      <Container
        fluid
        className="d-flex flex-column justify-content-center align-items-center flex-grow-1 my-2"
      >
        <Row className="w-100 justify-content-center mb-3 my-3">
          <Col xs={12} md={11}>
            <div className="d-flex align-items-center">
              <h3 className="small-title text-dark mb-0">Le mie richieste</h3>
              <div className="ms-auto">
                <Button
                  className="btn-custom1 rounded-circle d-flex align-items-center justify-content-center small-text"
                  style={{ width: "30px", height: "30px", padding: "0" }}
                  onClick={() => {
                    setEditingRequest(null)
                    resetForm()
                    setShowRequestModal(true)
                  }}
                >
                  <IoMdAdd />
                </Button>

                <Modal
                  show={showRequestModal}
                  onHide={() => setShowRequestModal(false)}
                  centered
                >
                  <Modal.Header closeButton>
                    <Modal.Title className="text-dark small-title">
                      {editingRequest
                        ? "Modifica richiesta"
                        : "Nuova richiesta"}
                    </Modal.Title>
                  </Modal.Header>

                  <Modal.Body>
                    <Form.Group className="mb-3">
                      <Form.Label className="text-muted ms-1 small-text">
                        Tipo di richiesta
                      </Form.Label>

                      <Form.Select
                        value={requestType}
                        onChange={(e) => setRequestType(e.target.value)}
                        className="small-text"
                      >
                        <option value="" className="small-text">
                          Seleziona una richiesta
                        </option>
                        <option value="HOLIDAY" className="small-text">
                          Ferie
                        </option>
                        <option value="LEAVE_HOURS" className="small-text">
                          Ore di permesso
                        </option>
                        <option value="CERTIFICATION" className="small-text">
                          Richiesta con certificato
                        </option>
                      </Form.Select>
                    </Form.Group>

                    {/* FERIE */}

                    {requestType === "HOLIDAY" && (
                      <>
                        <Form.Group className="mb-3">
                          <Form.Label className="small-text text-muted">
                            Data inizio ferie
                          </Form.Label>

                          <Form.Control
                            type="date"
                            value={startDate}
                            onChange={(e) => setStartDate(e.target.value)}
                          />
                        </Form.Group>

                        <Form.Group className="mb-3">
                          <Form.Label className="small-text text-muted">
                            Data fine ferie
                          </Form.Label>

                          <Form.Control
                            type="date"
                            value={endDate}
                            onChange={(e) => setEndDate(e.target.value)}
                          />
                        </Form.Group>

                        <Form.Group className="mb-3">
                          <Form.Label className="small-text text-muted">
                            Note
                          </Form.Label>

                          <Form.Control
                            as="textarea"
                            rows={3}
                            value={employeeNotes}
                            onChange={(e) => setEmployeeNotes(e.target.value)}
                            placeholder="Inserisci una nota (facoltativa)"
                          />
                        </Form.Group>
                      </>
                    )}
                    {/* ORE DI PERMESSO */}
                    {requestType === "LEAVE_HOURS" && (
                      <>
                        <Form.Group className="mb-3">
                          <Form.Label>Data</Form.Label>

                          <Form.Control
                            type="date"
                            value={date}
                            onChange={(e) => setDate(e.target.value)}
                          />
                        </Form.Group>

                        <Form.Group className="mb-3">
                          <Form.Label>Ora di inizio</Form.Label>

                          <Form.Control
                            type="time"
                            value={startTime}
                            onChange={(e) => setStartTime(e.target.value)}
                          />
                        </Form.Group>

                        <Form.Group className="mb-3">
                          <Form.Label>Ora di fine</Form.Label>

                          <Form.Control
                            type="time"
                            value={endTime}
                            onChange={(e) => setEndTime(e.target.value)}
                          />
                        </Form.Group>

                        <Form.Group className="mb-3">
                          <Form.Label>Tipo di permesso</Form.Label>

                          <Form.Select
                            value={leaveHoursType}
                            onChange={(e) =>
                              setLeaveHoursType(
                                e.target.value as LeaveHoursType | "",
                              )
                            }
                          >
                            <option value="">
                              Seleziona il tipo di permesso
                            </option>

                            {leaveHoursTypes.map((type) => (
                              <option key={type.value} value={type.value}>
                                {type.label}
                              </option>
                            ))}
                          </Form.Select>
                        </Form.Group>

                        <Form.Group className="mb-3">
                          <Form.Label>Note</Form.Label>

                          <Form.Control
                            as="textarea"
                            rows={3}
                            value={employeeNotes}
                            onChange={(e) => setEmployeeNotes(e.target.value)}
                            placeholder="Inserisci una nota (facoltativa)"
                          />
                        </Form.Group>
                      </>
                    )}

                    {/*RICHIESTE CON CERTIFICATO */}
                    {requestType === "CERTIFICATION" && (
                      <>
                        <Form.Group className="mb-3">
                          <Form.Label>Numero di protocollo</Form.Label>

                          <Form.Control
                            type="text"
                            value={protocolCode}
                            onChange={(e) => setProtocolCode(e.target.value)}
                            placeholder="Inserisci il numero di protocollo"
                          />
                        </Form.Group>

                        <Form.Group className="mb-3">
                          <Form.Label>Data inizio assenza</Form.Label>

                          <Form.Control
                            type="date"
                            value={startDate}
                            onChange={(e) => setStartDate(e.target.value)}
                          />
                        </Form.Group>

                        <Form.Group className="mb-3">
                          <Form.Label>Data fine assenza</Form.Label>

                          <Form.Control
                            type="date"
                            value={endDate}
                            onChange={(e) => setEndDate(e.target.value)}
                          />
                        </Form.Group>

                        <Form.Group className="mb-3">
                          <Form.Label>Data rilascio certificato</Form.Label>

                          <Form.Control
                            type="date"
                            value={issueDate}
                            onChange={(e) => setIssueDate(e.target.value)}
                          />
                        </Form.Group>

                        <Form.Group className="mb-3">
                          <Form.Label>Tipo di certificato</Form.Label>

                          <Form.Select
                            value={certificateType}
                            onChange={(e) =>
                              setCertificateType(
                                e.target.value as CertificateType | "",
                              )
                            }
                          >
                            <option value="">
                              Seleziona il tipo di certificato
                            </option>

                            {certificateTypes.map((type) => (
                              <option key={type.value} value={type.value}>
                                {type.label}
                              </option>
                            ))}
                          </Form.Select>
                        </Form.Group>

                        <Form.Group className="mb-3">
                          <Form.Label>Certificato</Form.Label>

                          <Form.Control
                            type="file"
                            onChange={(e) => {
                              const input = e.target as HTMLInputElement
                              setCertificateFile(input.files?.[0] ?? null)
                            }}
                          />
                        </Form.Group>

                        <Form.Group className="mb-3">
                          <Form.Label>Note</Form.Label>

                          <Form.Control
                            as="textarea"
                            rows={3}
                            value={employeeNotes}
                            onChange={(e) => setEmployeeNotes(e.target.value)}
                            placeholder="Inserisci una nota (facoltativa)"
                          />
                        </Form.Group>
                      </>
                    )}
                  </Modal.Body>
                  <Modal.Footer>
                    {editingRequest ? (
                      <>
                        <div className="me-auto">
                          <Button
                            className="btn-custom3 d-flex justify-content-center align-items-center p-2"
                            onClick={handleDeleteRequest}
                          >
                            {" "}
                            <FaRegTrashAlt />
                          </Button>
                        </div>
                      </>
                    ) : (
                      ""
                    )}

                    <Button
                      className="btn-custom1 small-text"
                      onClick={handleSubmitRequest}
                      disabled={loadingRequest}
                    >
                      {loadingRequest
                        ? "Invio..."
                        : editingRequest
                          ? "Salva modifiche"
                          : "Invia richiesta"}
                    </Button>
                    <Button
                      className="btn-custom2 small-text"
                      onClick={() => setShowRequestModal(false)}
                    >
                      Annulla
                    </Button>
                  </Modal.Footer>
                </Modal>
              </div>
            </div>
          </Col>
        </Row>
        {/*TABELLA SUMMARY*/}
        <Row className="w-100 justify-content-center mb-3 my-3">
          <Col xs={12} md={11}>
            <Card className="bg-light border border-secondary rounded shadow-sm">
              <Card.Body className="p-4">
                <h4 className="text-primary fw-semibold mb-4 small-title">
                  Riepilogo ferie e permessi
                </h4>

                {loadingSummary ? (
                  <div className="d-flex justify-content-center py-4">
                    <Spinner animation="border" />
                  </div>
                ) : summary ? (
                  <Table
                    bordered
                    hover
                    responsive
                    className="text-center align-middle"
                  >
                    <thead>
                      <tr>
                        <th></th>
                        <th className="small-text text-dark text-nowrap">
                          Totale maturato
                        </th>
                        <th className="small-text text-dark text-nowrap">
                          Maturato questo mese
                        </th>
                        <th className="small-text text-dark text-nowrap">
                          Utilizzato
                        </th>
                        <th className="small-text text-dark text-nowrap">
                          Rimanente
                        </th>
                      </tr>
                    </thead>

                    <tbody>
                      <tr>
                        <td className=" fw-bold text-start small-text text-dark text-nowrap">
                          Ferie
                        </td>
                        <td className="text-dark small-text text-center">
                          {summary.accruedDaysTotal} giorni
                        </td>
                        <td className="text-dark small-text text-center">
                          {summary.accruedDaysThisMonth} giorni
                        </td>
                        <td className="text-dark small-text text-center">
                          {summary.usedDays} giorni
                        </td>
                        <td className="fw-bold text-dark small-text text-center">
                          {summary.remainingDays} giorni
                        </td>
                      </tr>

                      <tr>
                        <td className="fw-bold text-start small-text text-dark text-nowrap">
                          Ore di permesso
                        </td>
                        <td className="text-dark small-text text-center">
                          {summary.accruedHoursTotal} ore
                        </td>
                        <td className="text-dark small-text text-center">
                          {summary.accruedHoursThisMonth} ore
                        </td>
                        <td className="text-dark small-text text-center">
                          {summary.usedHours} ore
                        </td>
                        <td className="fw-bold text-dark small-text text-center">
                          {summary.remainingHours} ore
                        </td>
                      </tr>
                    </tbody>
                  </Table>
                ) : (
                  <p className="text-muted small-text text-center mb-0">
                    Impossibile caricare il riepilogo.
                  </p>
                )}
              </Card.Body>
            </Card>
          </Col>
        </Row>
        {/*TABELLA DI VISUALIZZAZIONE*/}
        <Row className="w-100 justify-content-center mb-3 my-3">
          <Col xs={12} md={11}>
            <Card className="bg-light border border-secondary rounded shadow-sm">
              <Card.Body className="p-4">
                <h4 className="text-primary fw-semibold mb-4 small-title">
                  Visualizza richieste
                </h4>
                <Table responsive hover bordered className="align-middle">
                  <thead>
                    <tr>
                      <th className="small-text text-dark text-nowrap text-center">
                        Data
                      </th>
                      <th className="small-text text-dark text-nowrap text-center">
                        Tipo
                      </th>
                      <th className="small-text text-dark text-nowrap text-center">
                        Periodo
                      </th>
                      <th className="small-text text-dark text-nowrap text-center">
                        Quantità
                      </th>
                      <th className="small-text text-dark text-nowrap text-center">
                        Note
                      </th>
                      <th className="small-text text-dark text-nowrap text-center">
                        Revisore
                      </th>
                      <th className="small-text text-dark text-nowrap text-center">
                        Note revisore
                      </th>
                      <th className="small-text text-dark text-nowrap text-center">
                        Stato
                      </th>
                      <th className="small-text text-dark text-nowrap text-center">
                        Modifica
                      </th>
                      <th className="small-text text-dark text-nowrap text-center">
                        Visualizza dettagli
                      </th>
                    </tr>
                  </thead>

                  <tbody>
                    {requests.map((request) => (
                      <tr key={request.id}>
                        <td className="small-text text-dark text-center">
                          {request.createdAt}
                        </td>
                        <td className="small-text text-dark text-center">
                          {getRequestTypeLabel(request.requestType)}
                        </td>
                        <td className="small-text text-dark text-center">
                          {" "}
                          {getRequestPeriod(request)}
                        </td>
                        <td className="small-text text-dark text-center">
                          {request.requestType === "CERTIFICATION"
                            ? request.totalDays + " giorni"
                            : request.requestType === "HOLIDAY"
                              ? request.totalDays + " giorni"
                              : request.requestType === "LEAVE_HOURS"
                                ? request.totalHours?.toFixed(2) + " ore"
                                : ""}
                        </td>
                        <td className="small-text text-dark text-center">
                          {request.employeeNotes || "-"}
                        </td>
                        <td className="small-text text-dark text-center">
                          {request.reviewer || "-"}
                        </td>
                        <td className="small-text text-dark text-center">
                          {request.reviewerNotes || "-"}
                        </td>

                        <td className="smaller-text text-center text-nowrap">
                          {(() => {
                            const status = getRequestStatusLabel(
                              request.requestStatus,
                            )

                            return (
                              <>
                                <span className={status.className}>
                                  {status.label}
                                </span>

                                {hasPendingChange(request) && (
                                  <div className="text-muted small mt-1">
                                    Inviata richiesta di modifica
                                  </div>
                                )}
                              </>
                            )
                          })()}
                        </td>
                        <td className="small-text">
                          {request.requestStatus === "SENT" ? (
                            <div className="d-flex justify-content-center align-item-center">
                              <Button
                                className="p-1 rounded-circle border-0 backgroundPink text-light d-flex align-items-center justify-content-center small-text"
                                onClick={() => handleEditRequest(request)}
                              >
                                <FaPencilAlt size={10} />
                              </Button>
                            </div>
                          ) : request.requestStatus === "APPROVED" ? (
                            <div className="d-flex justify-content-center align-item-center">
                              <Button
                                className="p-1 rounded-circle border-0 backgroundOrange text-light d-flex align-items-center justify-content-center small-text"
                                onClick={() => handleEditRequest(request)}
                              >
                                <FaPencilAlt size={10} />
                              </Button>
                            </div>
                          ) : request.requestStatus === "REJECTED" ? (
                            <div className="d-flex justify-content-center align-item-center">
                              <Button
                                className="p-1 rounded-circle border-0 backgroundOrange text-light d-flex align-items-center justify-content-center small-text"
                                onClick={() => handleEditRequest(request)}
                              >
                                <FaPencilAlt size={10} />
                              </Button>
                            </div>
                          ) : (
                            ""
                          )}
                        </td>
                        <td>
                          <div className="d-flex justify-content-center">
                            <Button
                              className="btn-custom1 small-text p-1"
                              onClick={() => handleShowDetails(request)}
                            >
                              Dettagli
                            </Button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              </Card.Body>
            </Card>
          </Col>
        </Row>
        <Modal
          show={showDetails}
          onHide={() => {
            setShowDetails(false)
          }}
        >
          <Modal.Header closeButton>
            <Modal.Title className="small-title text-dark">
              Visualizza i dettagli
            </Modal.Title>
          </Modal.Header>

          <Modal.Body>
            {editingRequest && (
              <>
                {/* RICHIESTA ORIGINALE */}
                <h6 className="fw-bold mb-3 text-primary">
                  Richiesta originale
                </h6>

                <ul className="mb-4 small-text text-dark">
                  {requestType === "HOLIDAY" && (
                    <>
                      <li>
                        <strong>Periodo:</strong> {editingRequest.startDate} -{" "}
                        {editingRequest.endDate}
                      </li>

                      <li>
                        <strong>Giorni:</strong> {editingRequest.totalDays}
                      </li>

                      <li>
                        <strong>Nota:</strong>{" "}
                        {editingRequest.employeeNotes || "-"}
                      </li>
                    </>
                  )}

                  {requestType === "LEAVE_HOURS" && (
                    <>
                      <li>
                        <strong>Data:</strong> {editingRequest.date}
                      </li>

                      <li>
                        <strong>Orario:</strong> {editingRequest.startTime} -{" "}
                        {editingRequest.endTime}
                      </li>

                      <li>
                        <strong>Ore:</strong> {editingRequest.totalHours}
                      </li>

                      <li>
                        <strong>Nota:</strong>{" "}
                        {editingRequest.employeeNotes || "-"}
                      </li>
                    </>
                  )}

                  {requestType === "CERTIFICATION" && (
                    <>
                      <li>
                        <strong>Periodo:</strong> {editingRequest.startDate} -{" "}
                        {editingRequest.endDate}
                      </li>

                      <li>
                        <strong>Giorni:</strong> {editingRequest.totalDays}
                      </li>

                      <li>
                        <strong>Tipo certificato:</strong>{" "}
                        {editingRequest.certificateType || "-"}
                      </li>

                      <li>
                        <strong>Protocollo:</strong>{" "}
                        {editingRequest.protocolCode || "-"}
                      </li>

                      <li>
                        <strong>Nota:</strong>{" "}
                        {editingRequest.employeeNotes || "-"}
                      </li>
                    </>
                  )}
                </ul>

                {/* ================================================= */}
                {/* RICHIESTA DI MODIFICA - SOLO PER APPROVED */}
                {/* ================================================= */}

                {editingRequest.requestStatus === "APPROVED" &&
                  requestType === "HOLIDAY" &&
                  changeHolidayRequest && (
                    <>
                      <hr />

                      <h6 className="fw-bold mb-3 text-primary">
                        Richiesta di modifica
                      </h6>

                      <ul className="mb-0 small-text text-dark">
                        <li>
                          <strong>Nuovo periodo:</strong>{" "}
                          {changeHolidayRequest.startDate} -{" "}
                          {changeHolidayRequest.endDate}
                        </li>

                        <li>
                          <strong>Nuovi giorni:</strong>{" "}
                          {changeHolidayRequest.totalDays}
                        </li>

                        <li>
                          <strong>Nota:</strong>{" "}
                          {changeHolidayRequest.employeeNotes || "-"}
                        </li>

                        <li>
                          <strong>Stato modifica:</strong>{" "}
                          {
                            getRequestStatusLabel(
                              changeHolidayRequest.changeRequestStatus,
                            ).label
                          }
                        </li>
                      </ul>
                    </>
                  )}

                {editingRequest.requestStatus === "APPROVED" &&
                  requestType === "LEAVE_HOURS" &&
                  changeLeaveHoursRequest && (
                    <>
                      <hr />

                      <h6 className="fw-bold mb-3">Richiesta di modifica</h6>

                      <ul className="mb-0">
                        <li>
                          <strong>Nuova data:</strong>{" "}
                          {changeLeaveHoursRequest.date}
                        </li>

                        <li>
                          <strong>Nuovo orario:</strong>{" "}
                          {changeLeaveHoursRequest.startTime} -{" "}
                          {changeLeaveHoursRequest.endTime}
                        </li>

                        <li>
                          <strong>Nuove ore:</strong>{" "}
                          {changeLeaveHoursRequest.totalHours}
                        </li>

                        <li>
                          <strong>Nota:</strong>{" "}
                          {changeLeaveHoursRequest.employeeNotes || "-"}
                        </li>

                        <li>
                          <strong>Stato modifica:</strong>{" "}
                          {
                            getRequestStatusLabel(
                              changeLeaveHoursRequest.changeRequestStatus,
                            ).label
                          }
                        </li>
                      </ul>
                    </>
                  )}
              </>
            )}
          </Modal.Body>
          <Modal.Footer>
            {changeHolidayRequest?.changeRequestStatus === "SENT" && (
              <Button
                className="btn-custom2"
                onClick={handleCancelChangeRequest}
              >
                Annulla richiesta di modifica
              </Button>
            )}
            {changeLeaveHoursRequest?.changeRequestStatus === "SENT" && (
              <Button
                className="btn-custom2"
                onClick={handleCancelChangeRequest}
              >
                Annulla richiesta di modifica
              </Button>
            )}
          </Modal.Footer>
        </Modal>
      </Container>
    </>
  )
}
