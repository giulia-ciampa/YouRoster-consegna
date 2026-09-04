import {
  Button,
  Card,
  Col,
  Container,
  Modal,
  Row,
  Spinner,
  Table,
  Form,
} from "react-bootstrap"
import { ShiftManagerNavbar } from "./ShiftManagerNavbar"
import { useEffect, useState } from "react"

import {
  approveChangeHolidayRequest,
  approveChangeLeaveHoursRequest,
  approveHolidayRequest,
  approveLeaveHoursRequest,
  getAllRequests,
  rejectChangeHolidayRequest,
  rejectChangeLeaveHoursRequest,
  rejectHolidayRequest,
  rejectLeaveHoursRequest,
} from "../../services/requestService"
import type { CertificateType, RequestResponseDTO } from "../../types/requests"

import "../../styles/mobileText.css"
import { IoIosArrowBack, IoIosArrowForward } from "react-icons/io"

export const ShiftManagerRequests = () => {
  const [allRequests, setAllRequests] = useState<RequestResponseDTO[]>([])
  const [loading, setLoading] = useState(false)

  const [showModal, setShowModal] = useState(false)
  const [selectedRequest, setSelectedRequest] =
    useState<RequestResponseDTO | null>(null)

  const [reviewerNotes, setReviewerNotes] = useState("")
  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)

  const fetchRequests = async (page = 0) => {
    try {
      setLoading(true)

      const response = await getAllRequests(page, 20)

      setAllRequests(response.content)
      setCurrentPage(response.number ?? 0)
      setTotalPages(response.totalPages ?? 0)
    } catch (error) {
      console.error("Errore nel recupero delle richieste:", error)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchRequests()
  }, [])

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

  //funzione approva richiesta
  const handleApprove = async () => {
    if (!selectedRequest) return

    try {
      setLoading(true)

      if (selectedRequest.requestType === "HOLIDAY") {
        await approveHolidayRequest(
          selectedRequest.id,
          reviewerNotes || undefined,
        )
      }

      if (selectedRequest.requestType === "LEAVE_HOURS") {
        await approveLeaveHoursRequest(
          selectedRequest.id,
          reviewerNotes || undefined,
        )
      }

      alert("Richiesta approvata con successo!")

      setShowModal(false)
      setSelectedRequest(null)
      setReviewerNotes("")

      // qui poi aggiorniamo la tabella
      await fetchRequests()
    } catch (error) {
      console.error("Errore nell'approvazione:", error)
      alert("Si è verificato un errore durante l'approvazione della richiesta.")
    } finally {
      setLoading(false)
    }
  }

  //richiesta singola di modifica da lavorare
  const handleWorkRequest = (req: RequestResponseDTO) => {
    setSelectedRequest(req)
    setReviewerNotes(req.reviewerNotes ?? "")
    setShowModal(true)
  }

  //funzione rifiuta richiesta
  const handleReject = async () => {
    if (!selectedRequest) return

    try {
      setLoading(true)

      if (selectedRequest.requestType === "HOLIDAY") {
        await rejectHolidayRequest(
          selectedRequest.id,
          reviewerNotes || undefined,
        )
      }

      if (selectedRequest.requestType === "LEAVE_HOURS") {
        await rejectLeaveHoursRequest(
          selectedRequest.id,
          reviewerNotes || undefined,
        )
      }

      alert("Richiesta rifiutata con successo!")

      setShowModal(false)
      setSelectedRequest(null)
      setReviewerNotes("")

      await fetchRequests()
    } catch (error) {
      console.error("Errore nel rifiuto della richiesta:", error)
    } finally {
      setLoading(false)
    }
  }

  //funzione colore tipo
  const getRequestTypeClass = (requestType: string) => {
    switch (requestType) {
      case "HOLIDAY":
      case "CHANGE_HOLIDAY":
        return "ferie"

      case "LEAVE_HOURS":
      case "CHANGE_LEAVE_HOURS":
        return "permesso"

      case "CERTIFICATION":
        return "richiesta_certificato"

      default:
        return ""
    }
  }

  //funzione per colorare lo stato
  const colorState = (requestStatus: string) => {
    switch (requestStatus) {
      case "APPROVED":
        return "text-success APPROVATA"

      case "REJECTED":
        return "text-danger RIFIUTATA"

      case "CANCELLED":
        return "text-danger CANCELLATA"

      case "SENT":
        return "text-warning INVIATA"
    }
  }

  // funzione per tradurre lo stato della richiesta
  const translateRequestStatus = (requestStatus: string) => {
    switch (requestStatus) {
      case "APPROVED":
        return "APPROVATA"

      case "REJECTED":
        return "RIFIUTATA"

      case "CANCELLED":
        return "CANCELLATA"

      case "SENT":
        return "INVIATA"

      default:
        return requestStatus
    }
  }

  const translateRequestType = (
    requestType: string,
    certificationType?: CertificateType,
  ) => {
    switch (requestType) {
      case "HOLIDAY":
        return "FERIE"

      case "LEAVE_HOURS":
        return "ORE DI PERMESSO"

      case "CHANGE_HOLIDAY":
        return "FERIE"

      case "CHANGE_LEAVE_HOURS":
        return "ORE DI PERMESSO"

      case "CERTIFICATION":
        switch (certificationType) {
          case "MATERNITY":
            return "MATERNITÀ"
          case "PATERNITY":
            return "PATERNITÀ"
          case "PARENTAL_LEAVE":
            return "CONGEDO PARENTALE"
          case "SICKNESS":
            return "MALATTIA"
          default:
            return "CERTIFICATO"
        }

      default:
        return requestType
    }
  }

  //tutte le richieste

  const loadRequests = async () => {
    try {
      setLoading(true)

      const data = await getAllRequests(0, 15)

      setAllRequests(data.content)
    } catch (error) {
      console.error("Errore nel caricamento delle richieste:", error)
    } finally {
      setLoading(false)
    }
  }

  // RICHIESTE DI MODIFICA DA LAVORARE

  const handleApproveChangeRequest = async () => {
    if (!selectedRequest) return

    try {
      if (selectedRequest.requestType === "CHANGE_HOLIDAY") {
        await approveChangeHolidayRequest(selectedRequest.id, reviewerNotes)

        alert("La richiesta di modifica ferie è stata approvata.")
      } else if (selectedRequest.requestType === "CHANGE_LEAVE_HOURS") {
        await approveChangeLeaveHoursRequest(selectedRequest.id, reviewerNotes)

        alert("La richiesta di modifica del permesso è stata approvata.")
      }

      setShowModal(false)
      setSelectedRequest(null)
      setReviewerNotes("")

      // ricarichiamo le richieste
      await loadRequests()
    } catch (error) {
      console.error(
        "Errore nell'approvazione della richiesta di modifica:",
        error,
      )

      alert(
        error instanceof Error
          ? error.message
          : "Si è verificato un errore durante l'approvazione della richiesta.",
      )
    }
  }

  const handleRejectChangeRequest = async () => {
    if (!selectedRequest) return

    try {
      if (selectedRequest.requestType === "CHANGE_HOLIDAY") {
        await rejectChangeHolidayRequest(selectedRequest.id, reviewerNotes)
      }

      if (selectedRequest.requestType === "CHANGE_LEAVE_HOURS") {
        await rejectChangeLeaveHoursRequest(selectedRequest.id, reviewerNotes)
      }

      setShowModal(false)
      setSelectedRequest(null)
      setReviewerNotes("")

      await loadRequests()
    } catch (error) {
      console.error("Errore nel rifiuto della richiesta di modifica:", error)

      alert(
        error instanceof Error
          ? error.message
          : "Si è verificato un errore durante il rifiuto della richiesta.",
      )
    }
  }

  return (
    <>
      <ShiftManagerNavbar />
      <Container
        fluid
        className="d-flex flex-column justify-content-center align-items-center flex-grow-1 my-4"
      >
        {/* TITOLO */}
        <Row className="w-100 justify-content-center mb-3">
          <Col xs={12} md={11} className="ps-0">
            <h3 className="small-title text-dark mb-0">Gestione richieste</h3>
          </Col>
        </Row>

        {/*RICHIESTE DA APPROVARE */}
        <Row className="w-100 justify-content-center mb-3 my-3">
          <Col xs={12} md={11} className="ps-0">
            <Card className="bg-light border border-secondary rounded shadow-sm">
              <Card.Body className="p-4">
                <h4 className="text-primary fw-semibold mb-4 small-title">
                  Richieste da lavorare
                </h4>
                {loading ? (
                  <div className="text-center py-4">
                    <Spinner animation="border" variant="primary" />
                  </div>
                ) : (
                  <Table
                    bordered
                    hover
                    responsive
                    className="text-center align-middle"
                  >
                    <thead>
                      <tr>
                        <th className="small-text text-dark text-center text-nowrap">
                          Giorno e ora
                        </th>
                        <th className="small-text text-dark text-center text-nowrap">
                          Nome
                        </th>
                        <th className="small-text text-dark text-center text-nowrap">
                          Tipo
                        </th>
                        <th className="small-text text-dark text-center text-nowrap">
                          Periodo
                        </th>
                        <th className="small-text text-dark text-center text-nowrap">
                          Quantità
                        </th>
                        <th className="small-text text-dark text-center text-nowrap">
                          Note dipendente
                        </th>
                        <th className="small-text text-dark text-center text-nowrap">
                          Note revisore
                        </th>
                        <th className="small-text text-dark text-center text-nowrap">
                          Stato richiesta
                        </th>
                        <th className="small-text text-dark text-center text-nowrap">
                          Lavora richiesta
                        </th>
                      </tr>
                    </thead>
                    <tbody>
                      {allRequests.filter((req) => req.requestStatus === "SENT")
                        .length > 0 ? (
                        allRequests
                          .filter((req) => req.requestStatus === "SENT")
                          .map((req) => (
                            <tr key={req.id}>
                              {/* DATA CREAZIONE */}
                              <td className="small-text text-dark">
                                {req.createdAt}
                              </td>

                              {/* DIPENDENTE */}
                              <td className="small-text text-dark">
                                {req.employeeName}
                              </td>

                              {/* TIPO RICHIESTA */}
                              <td
                                className={`small-text ${getRequestTypeClass(req.requestType)}`}
                              >
                                {translateRequestType(
                                  req.requestType,
                                  req.certificationType,
                                )}

                                {(req.requestType === "CHANGE_HOLIDAY" ||
                                  req.requestType === "CHANGE_LEAVE_HOURS") && (
                                  <div className="text-muted small mt-1 text-nowrap">
                                    Richiesta di modifica
                                  </div>
                                )}
                              </td>

                              {/* PERIODO / ORARIO */}
                              <td className="small-text text-dark">
                                {req.requestType === "HOLIDAY" ||
                                req.requestType === "CHANGE_HOLIDAY" ? (
                                  <div>
                                    {req.startDate} - {req.endDate}
                                  </div>
                                ) : (
                                  <>
                                    {req.date}
                                    <p className="text-nowrap mb-0">
                                      {req.startTime} - {req.endTime}
                                    </p>
                                  </>
                                )}
                              </td>

                              {/* QUANTITÀ */}
                              <td className="small-text text-dark">
                                {req.requestType === "HOLIDAY" ||
                                req.requestType === "CHANGE_HOLIDAY" ? (
                                  <>{req.totalDays} giorni</>
                                ) : (
                                  <>{req.totalHours} ore</>
                                )}
                              </td>

                              {/* NOTE DIPENDENTE */}
                              <td className="small-text text-dark">
                                {req.employeeNotes || "-"}
                              </td>

                              {/* NOTE REVISORE */}
                              <td className="small-text text-dark">
                                {req.reviewerNotes || "-"}
                              </td>

                              {/* STATO */}
                              <td className="small-text text-dark">
                                {(() => {
                                  const status = getRequestStatusLabel(
                                    req.requestStatus,
                                  )

                                  return (
                                    <span className={status.className}>
                                      {status.label}
                                    </span>
                                  )
                                })()}
                              </td>

                              {/* AZIONE */}
                              <td className="small-text">
                                <Button
                                  className="btn-custom1 p-1 text-nowrap smaller-text"
                                  onClick={() => handleWorkRequest(req)}
                                >
                                  Lavora richiesta
                                </Button>
                              </td>
                            </tr>
                          ))
                      ) : (
                        <tr>
                          <td
                            colSpan={9}
                            className="text-center text-muted py-3"
                          >
                            Nessuna richiesta da lavorare.
                          </td>
                        </tr>
                      )}
                    </tbody>
                  </Table>
                )}
              </Card.Body>
            </Card>
          </Col>
        </Row>
        {/*TUTTE LE RICHIESTE */}
        <Row className="w-100 justify-content-center mb-3 my-3">
          <Col xs={12} md={11} className="ps-0">
            <Card className="bg-light border border-secondary rounded shadow-sm">
              <Card.Body className="p-4">
                <h4 className="text-primary fw-semibold mb-4 small-title">
                  Tutte le richieste
                </h4>
                {loading ? (
                  <div className="text-center py-4">
                    <Spinner animation="border" variant="primary" />
                  </div>
                ) : (
                  <Table
                    bordered
                    hover
                    responsive
                    className="text-center align-middle"
                  >
                    <thead>
                      <tr>
                        <th className="small-text text-dark text-center text-nowrap">
                          Giorno e ora
                        </th>
                        <th className="small-text text-dark text-center text-nowrap">
                          Nome
                        </th>
                        <th className="small-text text-dark text-center text-nowrap">
                          Tipo
                        </th>
                        <th className="small-text text-dark text-center text-nowrap">
                          Periodo
                        </th>
                        <th className="small-text text-dark text-center text-nowrap">
                          Quantità
                        </th>
                        <th className="small-text text-dark text-center text-nowrap">
                          Note dipendente
                        </th>
                        <th className="small-text text-dark text-center text-nowrap">
                          Note revisore
                        </th>
                        <th className="small-text text-dark text-center text-nowrap">
                          Stato richiesta
                        </th>
                      </tr>
                    </thead>

                    <tbody>
                      {allRequests.length === 0 ? (
                        <tr>
                          <td colSpan={8} className="text-center text-muted">
                            Nessuna richiesta trovata.
                          </td>
                        </tr>
                      ) : (
                        allRequests.map((request) => {
                          return (
                            <tr key={request.id}>
                              {/* Giorno e ora */}
                              <td className="small-text">
                                {request.createdAt}
                              </td>

                              {/* Nome */}
                              <td className="small-text">
                                {request.employeeName}
                              </td>

                              {/* Tipo */}
                              <td
                                className={`small-text ${getRequestTypeClass(request.requestType)}`}
                              >
                                {translateRequestType(
                                  request.requestType,
                                  request.certificationType,
                                )}

                                {(request.requestType === "CHANGE_HOLIDAY" ||
                                  request.requestType ===
                                    "CHANGE_LEAVE_HOURS") && (
                                  <div className="text-muted small mt-1 text-nowrap">
                                    Richiesta modificata dall'utente
                                  </div>
                                )}
                              </td>

                              {/* Periodo */}
                              <td className="small-text">
                                {request.startDate && request.endDate ? (
                                  <>
                                    {request.startDate}-{request.endDate}
                                  </>
                                ) : request.date ? (
                                  <>
                                    <div className="d-flex flex-column">
                                      <p>{request.date}</p>
                                      <p>
                                        {request.startTime} - {request.endTime}
                                      </p>
                                    </div>
                                  </>
                                ) : (
                                  "-"
                                )}
                              </td>

                              {/* Quantità */}
                              <td className="small-text">
                                {request.totalDays
                                  ? `${request.totalDays} giorni`
                                  : request.totalHours
                                    ? `${request.totalHours} ore`
                                    : "-"}
                              </td>

                              {/* Note dipendente */}
                              <td className="small-text">
                                {request.employeeNotes || "-"}
                              </td>

                              {/* Note revisore */}
                              <td className="small-text">
                                {request.reviewerNotes || "-"}
                              </td>

                              {/* Stato */}
                              <td
                                className={`small-text ${colorState(request.requestStatus)}`}
                              >
                                {translateRequestStatus(request.requestStatus)}
                              </td>
                            </tr>
                          )
                        })
                      )}
                    </tbody>
                  </Table>
                )}
              </Card.Body>
            </Card>
            <div className="d-flex justify-content-center align-items-center gap-3 mt-3">
              <Button
                disabled={currentPage === 0}
                onClick={() => fetchRequests(currentPage - 1)}
                className="d-flex align-items-center justify-content-center btn-custom1 p-1"
              >
                <IoIosArrowBack />
              </Button>

              <span className="small-text text-primary">
                Pagina {currentPage + 1} di {totalPages}
              </span>

              <Button
                disabled={currentPage >= totalPages - 1}
                onClick={() => fetchRequests(currentPage + 1)}
                className="d-flex align-items-center justify-content-center btn-custom1 p-1"
              >
                <IoIosArrowForward />
              </Button>
            </div>
          </Col>
        </Row>

        {/*MODALE */}
        <Modal
          show={showModal}
          onHide={() => {
            setShowModal(false)
            setSelectedRequest(null)
          }}
          centered
        >
          <Modal.Header closeButton>
            <Modal.Title>
              {selectedRequest?.requestType === "CHANGE_HOLIDAY"
                ? "Lavora richiesta modifica ferie"
                : selectedRequest?.requestType === "CHANGE_LEAVE_HOURS"
                  ? "Lavora richiesta modifica permesso"
                  : selectedRequest?.requestType === "HOLIDAY"
                    ? "Lavora richiesta ferie"
                    : "Lavora richiesta di permesso"}
            </Modal.Title>
          </Modal.Header>

          <Modal.Body>
            {/* ==================== FERIE ==================== */}
            {/*ORIGINALE */}
            {selectedRequest?.requestType === "HOLIDAY" && (
              <div className="px-2">
                <p className="mb-0 text-dark small-text">
                  <span className="fw-semibold text-dark small-text">
                    Richiesta inviata da:
                  </span>
                  {selectedRequest.employeeName}
                </p>

                <p className="text-dark small-text">
                  <span className="fw-semibold">
                    Data e ora invio richiesta:
                  </span>
                  {selectedRequest.createdAt}{" "}
                </p>

                <ul>
                  <li>
                    <span className="fw-semibold me-1">Giorni richiesti:</span>
                    {selectedRequest.startDate} - {selectedRequest.endDate}
                  </li>

                  <li>
                    <span className="fw-semibold me-1">Totale giorni:</span>
                    {selectedRequest.totalDays}
                  </li>
                  <li>
                    {" "}
                    <span className="fw-semibold me-1">Note richiedente:</span>
                    {selectedRequest.employeeNotes
                      ? selectedRequest.employeeNotes
                      : "-"}
                  </li>
                </ul>

                <Form>
                  <Form.Group className="mb-3" controlId="FormReviewerNotes">
                    <Form.Label className="small-text text-muted mb-0">
                      Nota
                    </Form.Label>

                    <Form.Control
                      as="textarea"
                      rows={3}
                      placeholder="Inserisci nota (facoltativo)"
                      value={reviewerNotes}
                      onChange={(e) => setReviewerNotes(e.target.value)}
                    />
                  </Form.Group>
                </Form>
              </div>
            )}
            {/*MODIFICA FERIE */}
            {selectedRequest?.requestType === "CHANGE_HOLIDAY" && (
              <div className="px-2">
                <p className="fw-bold text-primary">Richiesta Originale</p>
                <p className="mb-0">
                  <span className="fw-semibold me-1">
                    Richiesta inviata da:
                  </span>
                  {selectedRequest.employeeName}
                </p>
                <p className="mb-0">
                  <span className="fw-semibold me-1"> In data e ora:</span>
                  {selectedRequest.originalRequestCreatedAt}
                </p>
                <p>
                  <span className="fw-semibold me-1"> Lavorata in data:</span>
                  {selectedRequest.originalResponseDate ? (
                    <>{selectedRequest.originalResponseDate}</>
                  ) : (
                    "-"
                  )}
                </p>
                <ul>
                  <li>
                    <span className="fw-semibold me-1">Giorni richiesti:</span>
                    {selectedRequest.startDateOriginalRequest} -{" "}
                    {selectedRequest.endDateOriginalRequest}
                  </li>

                  <li>
                    <span className="fw-semibold me-1">Totale giorni:</span>
                    {selectedRequest.originalRequestTotalDays}
                  </li>
                  <li>
                    <span className="fw-semibold me-1">Note richiedente:</span>
                    {selectedRequest.originalRequestEmployeeNotes
                      ? selectedRequest.originalRequestEmployeeNotes
                      : "-"}
                  </li>
                  <li>
                    <span className="fw-semibold me-1">Note revisore:</span>
                    {selectedRequest.originalRequestReviewerNotes
                      ? selectedRequest.originalRequestReviewerNotes
                      : "-"}
                  </li>
                </ul>
                <p className="fw-bold text-primary mt-2">
                  Richiesta modificata dall'utente
                </p>
                <ul>
                  <li>
                    <span className="fw-semibold me-1">Giorni richiesti:</span>
                    {selectedRequest.startDate} - {selectedRequest.endDate}
                  </li>

                  <li>
                    <span className="fw-semibold me-1">Totale giorni:</span>
                    {selectedRequest.totalDays}
                  </li>
                  <li>
                    <span className="fw-semibold me-1">Note richiedente:</span>
                    {selectedRequest.employeeNotes
                      ? selectedRequest.employeeNotes
                      : "-"}
                  </li>
                </ul>
                <Form>
                  <Form.Group className="mb-3" controlId="FormReviewerNotes">
                    <Form.Label className="small-text text-muted mb-0">
                      Nota
                    </Form.Label>

                    <Form.Control
                      as="textarea"
                      rows={3}
                      placeholder="Inserisci nota (facoltativo)"
                      value={reviewerNotes}
                      onChange={(e) => setReviewerNotes(e.target.value)}
                    />
                  </Form.Group>
                </Form>
              </div>
            )}

            {/* ==================== ORE DI PERMESSO ==================== */}
            {selectedRequest?.requestType === "LEAVE_HOURS" && (
              <div className="px-2">
                <p className="mb-0 text-dark small-text">
                  <span className="fw-semibold text-dark small-text">
                    Richiesta inviata da:
                  </span>
                  {selectedRequest.employeeName}
                </p>

                <p className="text-dark small-text">
                  <span className="fw-semibold">
                    Data e ora invio richiesta:
                  </span>
                  {selectedRequest.createdAt}{" "}
                </p>

                <ul>
                  <li>
                    <span className="fw-semibold me-1">Giorno richiesto:</span>
                    {selectedRequest.date}
                  </li>

                  <li>
                    <span className="fw-semibold me-1">
                      Fascia oraria richiesta:
                    </span>
                    {selectedRequest.startTime} - {selectedRequest.endTime}
                  </li>

                  <li>
                    <span className="fw-semibold me-1">Totale ore:</span>
                    {selectedRequest.totalHours}
                  </li>
                  <li>
                    {" "}
                    <span className="fw-semibold me-1">Note richiedente:</span>
                    {selectedRequest.employeeNotes
                      ? selectedRequest.employeeNotes
                      : "-"}
                  </li>
                </ul>
                <Form>
                  <Form.Group className="mb-3" controlId="FormReviewerNotes">
                    <Form.Label className="small-text text-muted mb-0">
                      Nota
                    </Form.Label>

                    <Form.Control
                      as="textarea"
                      rows={3}
                      placeholder="Inserisci nota (facoltativo)"
                      value={reviewerNotes}
                      onChange={(e) => setReviewerNotes(e.target.value)}
                    />
                  </Form.Group>
                </Form>
              </div>
            )}

            {/* RICHIESTA MODIFICA ORE PERMESSO */}
            {selectedRequest?.requestType === "CHANGE_LEAVE_HOURS" && (
              <div className="px-2">
                <p className="fw-bold text-primary">Richiesta Originale</p>
                <p className="mb-0">
                  <span className="fw-semibold me-1">
                    Richiesta inviata da:
                  </span>
                  {selectedRequest.employeeName}
                </p>
                <p className="mb-0">
                  <span className="fw-semibold me-1"> In data e ora:</span>
                  {selectedRequest.originalRequestCreatedAt}
                </p>
                <p>
                  <span className="fw-semibold me-1"> Lavorata in data:</span>
                  {selectedRequest.originalResponseDate ? (
                    <>{selectedRequest.originalResponseDate}</>
                  ) : (
                    "-"
                  )}
                </p>
                <ul>
                  <li>
                    <span className="fw-semibold me-1">Giorno richiesto:</span>
                    {selectedRequest.originalDate}
                  </li>

                  <li>
                    <span className="fw-semibold me-1">
                      Fascia oraria richiesta:
                    </span>
                    {selectedRequest.startTimeOriginalRequest} -{" "}
                    {selectedRequest.endTimeOriginalRequest}
                  </li>

                  <li>
                    <span className="fw-semibold me-1">Totale ore:</span>
                    {selectedRequest.originalRequestTotalHours}
                  </li>
                  <li>
                    <span className="fw-semibold me-1">Note richiedente:</span>
                    {selectedRequest.originalRequestEmployeeNotes
                      ? selectedRequest.originalRequestEmployeeNotes
                      : "-"}
                  </li>
                  <li>
                    <span className="fw-semibold me-1">Note revisore:</span>
                    {selectedRequest.originalRequestReviewerNotes
                      ? selectedRequest.originalRequestReviewerNotes
                      : "-"}
                  </li>
                </ul>
                <p className="fw-bold text-primary mt-2">
                  Richiesta di modifica
                </p>
                <ul>
                  <li>
                    <span className="fw-semibold me-1">Giorno richiesto:</span>
                    {selectedRequest.date}
                  </li>
                  <li>
                    <span className="fw-semibold me-1">
                      Fascia oraria richiesta:
                    </span>
                    {selectedRequest.startTime} - {selectedRequest.endTime}
                  </li>

                  <li>
                    <span className="fw-semibold me-1">Totale ore:</span>
                    {selectedRequest.totalHours}
                  </li>
                  <li>
                    <span className="fw-semibold me-1">Note richiedente:</span>
                    {selectedRequest.employeeNotes
                      ? selectedRequest.employeeNotes
                      : "-"}
                  </li>
                </ul>
                <Form>
                  <Form.Group className="mb-3" controlId="FormReviewerNotes">
                    <Form.Label className="small-text text-muted mb-0">
                      Nota
                    </Form.Label>

                    <Form.Control
                      as="textarea"
                      rows={3}
                      placeholder="Inserisci nota (facoltativo)"
                      value={reviewerNotes}
                      onChange={(e) => setReviewerNotes(e.target.value)}
                    />
                  </Form.Group>
                </Form>
              </div>
            )}
          </Modal.Body>
          <Modal.Footer>
            <Button
              className="btn-custom1"
              onClick={() => {
                if (
                  selectedRequest?.requestType === "CHANGE_HOLIDAY" ||
                  selectedRequest?.requestType === "CHANGE_LEAVE_HOURS"
                ) {
                  handleApproveChangeRequest()
                } else {
                  handleApprove()
                }
              }}
            >
              Approva
            </Button>

            <Button
              className="btn-custom2"
              onClick={() => {
                if (
                  selectedRequest?.requestType === "CHANGE_HOLIDAY" ||
                  selectedRequest?.requestType === "CHANGE_LEAVE_HOURS"
                ) {
                  handleRejectChangeRequest()
                } else {
                  handleReject()
                }
              }}
            >
              Rifiuta
            </Button>
          </Modal.Footer>
        </Modal>
      </Container>
    </>
  )
}
