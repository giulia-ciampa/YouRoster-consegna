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
import { HrNavbar } from "./HrNavbar"
import { useEffect, useState } from "react"
import type {
  AbsenceCertificationReviewResponse,
  CertificateType,
  HrCertificationRequest,
} from "../../types/requests"
import {
  approveCertificationRequest,
  getAllCertificationRequests,
  getPendingCertificationRequests,
  rejectCertificationRequest,
} from "../../services/requestService"
import { IoIosArrowBack, IoIosArrowForward } from "react-icons/io"

export const HrRequestsToManage = () => {
  const [loading, setLoading] = useState(false)
  const [pendingCertifications, setPendingCertifications] = useState<
    HrCertificationRequest[]
  >([])

  const [showModal, setShowModal] = useState(false)
  const [reviewerNotes, setReviewerNotes] = useState("")
  const [selectedRequest, setSelectedRequest] =
    useState<HrCertificationRequest | null>(null)

  const [allCertifications, setAllCertifications] = useState<
    AbsenceCertificationReviewResponse[]
  >([])

  const [allCertificationsPage, setAllCertificationsPage] = useState(0)
  const [allCertificationsTotalPages, setAllCertificationsTotalPages] =
    useState(0)

  const handleOpenRequestModal = (request: HrCertificationRequest) => {
    setSelectedRequest(request)
    setShowModal(true)
  }

  const fetchPendingCertifications = async () => {
    try {
      setLoading(true)

      const response = await getPendingCertificationRequests()

      setPendingCertifications(response.content)
    } catch (error) {
      console.error(
        "Errore nel recupero delle certificazioni da lavorare:",
        error,
      )
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchPendingCertifications()
  }, [])

  //APPROVA RICHIESTA
  const handleApproveRequest = async () => {
    if (!selectedRequest) return

    try {
      setLoading(true)

      await approveCertificationRequest(selectedRequest.id, {
        notes: reviewerNotes,
      })

      alert("Richiesta approvata con successo.")

      setShowModal(false)
      setSelectedRequest(null)
      setReviewerNotes("")

      await fetchPendingCertifications()
    } catch (error: unknown) {
      console.error("Errore durante l'approvazione:", error)

      alert(
        error instanceof Error
          ? error.message
          : "Errore durante l'approvazione della richiesta.",
      )
    } finally {
      setLoading(false)
    }
  }

  //RIFIUTA RICHIESTA
  const handleRejectRequest = async () => {
    if (!selectedRequest) return

    try {
      setLoading(true)

      await rejectCertificationRequest(selectedRequest.id, {
        notes: reviewerNotes,
      })

      alert("Richiesta rifiutata con successo.")

      setShowModal(false)
      setSelectedRequest(null)
      setReviewerNotes("")

      await fetchPendingCertifications()
    } catch (error: unknown) {
      console.error("Errore durante il rifiuto:", error)

      alert(
        error instanceof Error
          ? error.message
          : "Errore durante il rifiuto della richiesta.",
      )
    } finally {
      setLoading(false)
    }
  }

  //TUTTE LE RICHIESTE CON CERTIFICAZIONE
  const fetchAllCertifications = async (page = 0) => {
    try {
      setLoading(true)

      const response = await getAllCertificationRequests(page, 15)

      setAllCertifications(response.content)
      setAllCertificationsPage(response.number)
      setAllCertificationsTotalPages(response.totalPages)
    } catch (error) {
      console.error("Errore nel recupero delle richieste certificate:", error)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchAllCertifications()
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

  const translateRequestType = (certificationType: CertificateType) => {
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
  }

  //funzione per colorare lo stato
  const colorState = (requestStatus: string) => {
    switch (requestStatus) {
      case "APPROVED":
        return "text-success"

      case "REJECTED":
        return "text-danger"

      case "CANCELLED":
        return "text-danger"

      case "SENT":
        return "text-warning"
    }
  }

  return (
    <>
      <HrNavbar />
      <Container
        fluid
        className="d-flex flex-column justify-content-center align-items-center flex-grow-1 my-4"
      >
        {/* TITOLO */}
        <Row className="w-100 justify-content-center mb-3">
          <Col xs={12} md={11} className="ps-0">
            <h3 className="small-title text-dark mb-0">
              Gestione richieste con certificato
            </h3>
          </Col>
        </Row>

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
                          Tipo di certificato
                        </th>
                        <th className="small-text text-dark text-center text-nowrap">
                          Emesso in data
                        </th>
                        <th className="small-text text-dark text-center text-nowrap">
                          Numero di protocollo
                        </th>
                        <th className="small-text text-dark text-center text-nowrap">
                          Link certificato
                        </th>
                        <th className="small-text text-dark text-center text-nowrap">
                          Periodo
                        </th>
                        <th className="small-text text-dark text-center text-nowrap">
                          Totale giorni
                        </th>
                        <th className="small-text text-dark text-center text-nowrap">
                          Note dipendente
                        </th>
                        <th className="small-text text-dark text-center text-nowrap">
                          Lavora richiesta
                        </th>
                      </tr>
                    </thead>
                    <tbody>
                      {pendingCertifications &&
                      pendingCertifications.length > 0 ? (
                        pendingCertifications.map((req) => (
                          <tr key={req.id}>
                            {/* DATA CREAZIONE */}
                            <td className="small-text text-dark">
                              {req.createdAt}
                            </td>

                            {/* DIPENDENTE */}
                            <td className="small-text text-dark">
                              {req.employeeName}
                            </td>

                            {/*TIPO DI CERTIFICATO */}
                            <td className=" richiesta_certificato  small-text text-dark">
                              {translateRequestType(req.certificateType)}
                            </td>
                            {/* EMESSO IN DATA */}
                            <td className="small-text text-dark">
                              {req.issueDate}
                            </td>

                            {/*NUMERO DI PROTOCOLLO*/}
                            <td className="small-text text-dark text-nowrap">
                              {req.protocolCode}
                            </td>

                            {/*LINK CERTIFICATO */}
                            <td className="small-text">
                              <a
                                href={req.certificateUrl}
                                target="_blank"
                                rel="noopener noreferrer"
                              >
                                Clicca qui
                              </a>
                            </td>

                            {/* PERIODO*/}
                            <td className="small-text text-dark">
                              <div>
                                {req.startDate} - {req.endDate}
                              </div>
                            </td>

                            {/* TOTALE GIORNI */}
                            <td className="small-text text-dark">
                              <>{req.totalDays} giorni</>
                            </td>

                            {/* NOTE DIPENDENTE */}
                            <td className="small-text text-dark">
                              {req.employeeNotes || "-"}
                            </td>

                            {/* AZIONE */}
                            <td className="small-text">
                              <Button
                                className="btn-custom1 p-1 text-nowrap smaller-text"
                                onClick={() => handleOpenRequestModal(req)}
                              >
                                Lavora richiesta
                              </Button>
                            </td>
                          </tr>
                        ))
                      ) : (
                        <tr>
                          <td colSpan={10} className="text-muted">
                            Nessuna richiesta da lavorare
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
                          Tipo certificato
                        </th>
                        <th className="small-text text-dark text-center text-nowrap">
                          Emesso in data
                        </th>
                        <th className="small-text text-dark text-center text-nowrap">
                          Numero protocollo
                        </th>
                        <th className="small-text text-dark text-center text-nowrap">
                          Link certificato
                        </th>
                        <th className="small-text text-dark text-center text-nowrap">
                          Periodo
                        </th>
                        <th className="small-text text-dark text-center text-nowrap">
                          Totale giorni
                        </th>
                        <th className="small-text text-dark text-center text-nowrap">
                          Note richiedente
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
                      {allCertifications.map((req) => (
                        <tr key={req.id}>
                          {/*GIORNO E ORA*/}
                          <td className="small-text text-dark">
                            {req.createdAt}
                          </td>
                          {/*NOME*/}
                          <td className="small-text text-dark">
                            {req.employeeName}
                          </td>
                          {/*TIPO CERTIFICATO*/}
                          <td className=" richiesta_certificato  small-text text-dark">
                            {translateRequestType(req.certificateType)}
                          </td>
                          {/*EMESSO IN DATA*/}
                          <td className="small-text text-dark">
                            {req.issueDate}
                          </td>
                          {/*NUMERO PROTOCOLLO*/}
                          <td className="small-text text-dark">
                            {req.protocolCode}
                          </td>
                          {/*LINK CERTIFICATO*/}
                          <td className="small-text text-dark">
                            <a
                              href={req.certificateUrl}
                              target="_blank"
                              rel="noopener noreferrer"
                            >
                              Clicca qui
                            </a>
                          </td>
                          {/*PERIODO*/}
                          <td className="small-text text-dark">
                            {req.startDate}-{req.endDate}
                          </td>

                          {/*TOTALE GIORNI */}
                          <td className="small-text text-dark">
                            {req.totalDays}
                          </td>

                          {/*NOTE RICHIEDENTE*/}
                          <td className="small-text text-dark">
                            {req.employeeNotes}
                          </td>
                          {/*NOTE REVISORE*/}
                          <td className="small-text text-dark">
                            {req.reviewerNotes}
                          </td>
                          {/*STATO RICHIESTA*/}
                          <td
                            className={`${colorState(req.requestStatus)} small-text`}
                          >
                            {getRequestStatusLabel(req.requestStatus).label}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </Table>
                )}
              </Card.Body>
            </Card>
            <div className="d-flex justify-content-center align-items-center gap-3 mt-3">
              <Button
                className="d-flex align-items-center justify-content-center btn-custom1 p-1"
                disabled={allCertificationsPage === 0}
                onClick={() =>
                  fetchAllCertifications(allCertificationsPage - 1)
                }
              >
                <IoIosArrowBack />
              </Button>

              <span className="small-text text-primary">
                Pagina {allCertificationsPage + 1} di{" "}
                {allCertificationsTotalPages}
              </span>

              <Button
                className="d-flex align-items-center justify-content-center btn-custom1 p-1"
                disabled={
                  allCertificationsPage >= allCertificationsTotalPages - 1
                }
                onClick={() =>
                  fetchAllCertifications(allCertificationsPage + 1)
                }
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
          }}
          centered
        >
          <Modal.Header closeButton>
            <Modal.Title>Lavora richiesta</Modal.Title>
          </Modal.Header>

          <Modal.Body>
            <p className="fw-semibold text-primary">Dettagli richiesta</p>
            <div>
              <p className="mt-0">
                <span className="text-dark small-text fw-semibold me-1">
                  Richiesta inviata da:
                </span>
                {selectedRequest?.employeeName}
              </p>
              <p>
                <span className="text-dark small-text fw-semibold me-1">
                  Data e ora invio richiesta:
                </span>
                {selectedRequest?.createdAt}
              </p>
              <ul>
                <li>
                  <span className="text-dark small-text fw-semibold me-1">
                    Certificato emesso in data:
                  </span>
                  {selectedRequest?.issueDate}
                </li>
                <li>
                  <span className="text-dark small-text fw-semibold me-1">
                    Per i giorni:
                  </span>
                  {selectedRequest?.startDate}-{selectedRequest?.endDate}
                </li>
                <li>
                  <span className="text-dark small-text fw-semibold me-1">
                    Tipo di certificato:
                  </span>
                  {selectedRequest?.certificateType}
                </li>
                <li>
                  <span className="text-dark small-text fw-semibold me-1">
                    Numero di protocollo:
                  </span>
                  {selectedRequest?.protocolCode}
                </li>
                <li>
                  <span className="text-dark small-text fw-semibold me-1">
                    Totale giorni:
                  </span>
                  {selectedRequest?.totalDays}
                </li>
                <li>
                  <span className="text-dark small-text fw-semibold me-1">
                    Note richiedente:
                  </span>
                  {selectedRequest?.employeeNotes}
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
          </Modal.Body>
          <Modal.Footer>
            <Button
              className="btn-custom1"
              onClick={handleApproveRequest}
              disabled={loading}
            >
              {loading ? "Approvazione..." : "Approva"}
            </Button>
            <Button
              className="btn-custom-danger"
              onClick={handleRejectRequest}
              disabled={loading}
            >
              {loading ? "Rifiuto..." : "Rifiuta"}
            </Button>
          </Modal.Footer>
        </Modal>
      </Container>
    </>
  )
}
