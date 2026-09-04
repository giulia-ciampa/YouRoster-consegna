import {
  Alert,
  Button,
  Card,
  Col,
  Container,
  Dropdown,
  Row,
  Spinner,
  Table,
  Form,
  Modal,
} from "react-bootstrap"
import { HrNavbar } from "./HrNavbar"
import { PiBellRingingLight, PiUserLight } from "react-icons/pi"
import type { UserProfileResponse } from "../../types/users"
import { useEffect, useState } from "react"
import type { Clocking, ShiftAssignment } from "../../types/shift"
import { getMyProfile, updateMyAvatar } from "../../services/userService"
import { useDispatch } from "react-redux"
import { setUser } from "../../redux/reducers/userSlice"
import { getMyAssignmentsByDate } from "../../services/shiftAssignmentService"
import {
  getMyClockingForDate,
  handleClockIn,
  handleClockOut,
} from "../../services/clockingsService"
import { MdAddAPhoto } from "react-icons/md"
import { useNavigate } from "react-router"
import type { HrCertificationRequest } from "../../types/requests"
import {
  approveCertificationRequest,
  getPendingCertificationRequests,
  rejectCertificationRequest,
} from "../../services/requestService"
import { TiTick } from "react-icons/ti"
import { IoClose } from "react-icons/io5"

export const HrDashboard = () => {
  const [userProfile, setUserProfile] = useState<UserProfileResponse | null>(
    null,
  )
  const [todayAssignment, setTodayAssignment] =
    useState<ShiftAssignment | null>(null)

  const [loadingProfile, setLoadingProfile] = useState(true)

  const todayString = new Date().toISOString().split("T")[0]
  const [selectedDate, setSelectedDate] = useState<string>(todayString)

  const [avatarPreview, setAvatarPreview] = useState<string | null>(null)

  const [showAlert, setShowAlert] = useState(false)
  const [alertMessage, setAlertMessage] = useState("")

  const [clocking, setClocking] = useState<Clocking | null>(null)
  const [loading, setLoading] = useState(false)

  const [showClockInModal, setShowClockInModal] = useState(false)
  const [clockInNote, setClockInNote] = useState("")

  const [showClockOutModal, setShowClockOutModal] = useState(false)
  const [clockOutNote, setClockOutNote] = useState("")

  const [pendingCertifications, setPendingCertifications] = useState<
    HrCertificationRequest[]
  >([])

  const dispatch = useDispatch()

  const navigate = useNavigate()

  // 1. Chiamata per i dati del profilo utente (al caricamento della pagina)
  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const data = await getMyProfile()
        setUserProfile(data)
        dispatch(
          setUser({
            photoUrl: data.photoUrl,
            name: data.name,
            surname: data.surname,
            roleNames: data.roleNames,
          }),
        )
      } catch (error) {
        console.error("Errore nel caricamento del profilo utente", error)
      } finally {
        setLoadingProfile(false)
      }
    }

    fetchProfile()
  }, [])

  // 2. Chiamata unica per i turni che si aggiorna ogni volta che cambia 'selectedDate'
  useEffect(() => {
    const fetchAssignmentsForDate = async () => {
      try {
        const assignments = await getMyAssignmentsByDate(selectedDate)

        if (assignments.length > 0) {
          setTodayAssignment(assignments[0])
        } else {
          setTodayAssignment(null)
        }
      } catch (error) {
        console.error(
          "Errore nel recupero dei turni per la data selezionata",
          error,
        )

        setTodayAssignment(null)
      }
    }

    fetchAssignmentsForDate()
  }, [selectedDate])

  //RICHIESTE CON CERTIFICATO DA APPROVARE
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
  const handleApproveRequest = async (request: HrCertificationRequest) => {
    try {
      setLoading(true)

      await approveCertificationRequest(request.id)

      alert("Richiesta approvata con successo.")

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
  const handleRejectRequest = async (request: HrCertificationRequest) => {
    try {
      setLoading(true)

      await rejectCertificationRequest(request.id)

      alert("Richiesta rifiutata con successo.")

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

  //VISUALIZZA TIMBRATURA
  useEffect(() => {
    const fetchClocking = async () => {
      try {
        const data = await getMyClockingForDate(selectedDate)

        console.log("TIMBRATURA:", data)

        setClocking(data)
      } catch (error) {
        console.error("Errore nel recupero della timbratura:", error)
        setClocking(null)
      }
    }

    fetchClocking()
  }, [selectedDate])

  //APERTURA MODALE ENTRATA
  const handleClockInClick = async () => {
    setShowClockInModal(true)
  }

  // APERTURA MODALE USCITA
  const handleClockOutClick = () => {
    setShowClockOutModal(true)
  }

  //CONFERMA TIMBRATURA ENTRATA
  const handleConfirmClockIn = async () => {
    setLoading(true)

    try {
      const success = await handleClockIn(clockInNote)

      if (success) {
        const data = await getMyClockingForDate(selectedDate)
        setClocking(data)

        // Chiudo il modale
        setShowClockInModal(false)

        // Pulisco la nota
        setClockInNote("")
      }
    } catch (error) {
      console.error(error)
    } finally {
      setLoading(false)
    }
  }

  // CONFERMA TIMBRATURA USCITA
  const handleConfirmClockOut = async () => {
    setLoading(true)

    try {
      const success = await handleClockOut(clockOutNote)

      if (success) {
        // Ricarichiamo i dati aggiornati dal backend per quella data
        const data = await getMyClockingForDate(selectedDate)
        setClocking(data)

        // Chiudiamo il modale
        setShowClockOutModal(false)

        // Puliamo la nota
        setClockOutNote("")
      }
    } catch (error) {
      console.error(error)
    } finally {
      setLoading(false)
    }
  }

  // Upload foto
  const handleAvatarUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0]

      setAvatarPreview(URL.createObjectURL(file))

      try {
        // Chiamata al service che comunica col backend (Cloudinary + salvataggio DB)
        const updatedProfile = await updateMyAvatar(file)
        setUserProfile(updatedProfile)

        if (updatedProfile.photoUrl) {
          localStorage.setItem("photoUrl", updatedProfile.photoUrl)
        }

        // Mostra l'alert di successo
        setAlertMessage("Foto profilo aggiornata con successo!")
        setShowAlert(true)

        // Nascondi l'alert dopo 3 secondi
        setTimeout(() => setShowAlert(false), 3000)
      } catch (error) {
        console.error(
          "Errore durante l'aggiornamento della foto profilo",
          error,
        )
        setAlertMessage("Errore durante il caricamento della foto.")
        setShowAlert(true)
      }
    }
  }

  //funzione per tradurre il tipo di assegnazione
  const translateAssignmentType = (assignmentType: string) => {
    switch (assignmentType) {
      case "OFF":
        return "OFF"

      case "ON_HOLIDAY":
        return "FERIE"

      case "PERMISSION":
        return "ORE DI PERMESSO"

      case "MATERNITY":
        return "MATERNITÀ"

      case "PATERNITY":
        return "PATERNITÀ"

      case "PARENTAL_LEAVE":
        return "CONGEDO PARENTALE"

      case "SICK":
        return "MALATTIA"

      case "ABSENT":
        return "ASSENZA INGIUSTIFICATA"

      default:
        return assignmentType
    }
  }

  //funzione colore tipo assegnazione
  const getAssignmentTypeClass = (assignmentType: string) => {
    switch (assignmentType) {
      case "ON_HOLIDAY":
        return "ferie"

      case "PERMISSION":
        return "permesso"

      case "MATERNITY":
        return "richiesta_certificato"

      case "PATERNITY":
        return "richiesta_certificato"

      case "PARENTAL_LEAVE":
        return "richiesta_certificato"

      case "SICKNESS":
        return "richiesta_certificato"

      case "OFF":
        return "off"

      case "WORK":
        return "turno"

      default:
        return ""
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
        <Row className="w-100 justify-content-center mb-4">
          <Col xs={12} md={11} className="ps-0">
            <div className="d-flex justify-content-between align-items-center">
              <h3 className="small-title text-dark mb-0">
                Panoramica giornaliera
              </h3>
              <Dropdown align="end" className="myDropDown">
                <Dropdown.Toggle
                  as="div"
                  className="position-relative d-inline-block"
                >
                  <Button className="text-secondary border-0 bg-transparent">
                    {" "}
                    <PiBellRingingLight size={26} />
                  </Button>
                  {pendingCertifications.length > 0 && (
                    <span
                      className="position-absolute top-0 start-100  badge rounded-pill bg-danger"
                      style={{ fontSize: "0.5rem", translate: "-100% -7%" }}
                    >
                      {pendingCertifications.length}
                    </span>
                  )}
                </Dropdown.Toggle>
                <Dropdown.Menu
                  className="shadow-sm border-0 p-2"
                  style={{
                    minWidth: "400px",
                    maxHeight: "450px",
                    overflowY: "auto",
                  }}
                >
                  <Dropdown.Header className="fw-bold text-dark">
                    Richieste con certificato
                  </Dropdown.Header>
                  <Dropdown.Divider />
                  {pendingCertifications.length === 0 ? (
                    <div className="text-center text-muted small py-3">
                      Nessuna nuova richiesta
                    </div>
                  ) : (
                    pendingCertifications.map((req) => (
                      <div
                        key={req.id}
                        className="px-2 py-2 d-flex justify-content-between align-items-center border-bottom"
                      >
                        <div>
                          <p className="text-dark small-text mb-0">
                            Richiesta inviata da:{" "}
                            <span className="fw-semibold ms-1">
                              {req.employeeName}
                            </span>
                          </p>
                          <p className="text-dark small-text mb-0">
                            Con certificato di:{" "}
                            <span className="fw-semibold ms-1">
                              {req.certificateType}
                            </span>
                          </p>
                          <p className="text-dark small-text mb-0">
                            Per le date da a:{" "}
                            <span className="fw-semibold ms-1">
                              {req.startDate} - {req.endDate}
                            </span>
                          </p>
                        </div>
                        <div>
                          <Button
                            className="bg-transparent border border-1 border-success p-0 me-1"
                            title="Approva"
                            onClick={() => handleApproveRequest(req)}
                          >
                            <p className="mb-0 px-1 small-text">
                              <TiTick
                                size={15}
                                className="text-success fw-bold p-0"
                              />
                            </p>
                          </Button>
                          <Button
                            className="bg-transparent border border-1 border-danger p-0 ms-1"
                            title="Rifiuta"
                            onClick={() => handleRejectRequest(req)}
                          >
                            <p className="mb-0 px-1 small-text">
                              <IoClose
                                size={15}
                                className="text-danger fw-bold p-0"
                              />
                            </p>
                          </Button>
                        </div>
                      </div>
                    ))
                  )}
                </Dropdown.Menu>
              </Dropdown>
            </div>
          </Col>
        </Row>
        <Row className="g-4 w-100 justify-content-center">
          {/* COLONNA SINISTRA: Foto profilo, nome, cognome, email */}
          <Col
            xs={12}
            md={4}
            className="bg-light d-flex flex-column align-items-center justify-content-center p-4 border border-secondary rounded shadow-sm"
          >
            {showAlert && (
              <Alert
                variant={
                  alertMessage.includes("Errore") ? "warning" : "primary"
                }
                className="w-100 py-2 text-center small"
              >
                {alertMessage}
              </Alert>
            )}

            {loadingProfile ? (
              <Spinner variant="primary" animation="border" size="sm" />
            ) : (
              <>
                {/* Contenitore tondo della foto con effetto hover/click */}
                <div
                  className="position-relative d-inline-block mb-3"
                  style={{ width: "110px", height: "110px", cursor: "pointer" }}
                  onClick={() =>
                    document.getElementById("avatarInput")?.click()
                  }
                >
                  <div className="w-100 h-100 rounded-circle overflow-hidden bg-light border border-2 border-secondary d-flex align-items-center justify-content-center">
                    {avatarPreview ? (
                      <img
                        src={avatarPreview}
                        alt="Avatar Preview"
                        className="w-100 h-100"
                        style={{ objectFit: "cover" }}
                      />
                    ) : userProfile?.photoUrl ? (
                      <img
                        src={userProfile.photoUrl}
                        alt="User Profile"
                        className="w-100 h-100"
                        style={{ objectFit: "cover" }}
                      />
                    ) : (
                      <PiUserLight size={80} className="text-secondary" />
                    )}
                  </div>

                  {/* Icona della fotocamera in basso a destra */}
                  <div
                    className="position-absolute bottom-0 end-0 bg-white rounded-circle p-1 shadow-sm border border-primary d-flex align-items-center justify-content-center"
                    style={{ width: "30px", height: "30px" }}
                  >
                    <MdAddAPhoto size={16} className="text-primary" />
                  </div>
                </div>

                {/* Input file nascosto */}
                <input
                  id="avatarInput"
                  type="file"
                  className="d-none"
                  onChange={handleAvatarUpload}
                />

                <h4 className="fw-bold mb-1">
                  {userProfile?.name} {userProfile?.surname}
                </h4>
                <p className="text-muted small">{userProfile?.roleNames}</p>
              </>
            )}
            <div className="w-100 mt-4 text-center">
              <h5 className="fw-semibold mb-3">Gestione Presenze</h5>
              <div className="d-flex flex-column align-items-center gap-2">
                {/*BOTTONE PER APRIRE IL MODALE */}
                <Button
                  disabled={clocking?.actualStartTime != null}
                  onClick={handleClockInClick}
                  className="w-75 btn-custom1 p-3"
                >
                  {clocking?.actualStartTime
                    ? `Entrata timbrata`
                    : "Timbra entrata"}
                </Button>
                {/* Bottone Uscita */}
                <Button
                  onClick={handleClockOutClick}
                  disabled={
                    !clocking?.actualStartTime ||
                    clocking?.actualEndTime != null ||
                    loading
                  }
                  className="w-75 btn-custom1 p-3"
                >
                  {clocking?.actualEndTime != null
                    ? "Uscita Timbrata"
                    : "Timbra Uscita"}
                </Button>
                <Modal
                  show={showClockInModal}
                  onHide={() => setShowClockInModal(false)}
                  centered
                >
                  <Modal.Header closeButton>
                    <Modal.Title>Timbra entrata</Modal.Title>
                  </Modal.Header>

                  <Modal.Body>
                    <Form.Group>
                      <Form.Label>Nota (facoltativa)</Form.Label>

                      <Form.Control
                        type="text"
                        value={clockInNote}
                        onChange={(e) => setClockInNote(e.target.value)}
                        placeholder="Inserisci una nota..."
                      />
                    </Form.Group>
                  </Modal.Body>

                  <Modal.Footer>
                    <Button
                      className="btn-custom1"
                      onClick={handleConfirmClockIn}
                      disabled={loading}
                    >
                      {loading ? "Timbratura..." : "Timbra"}
                    </Button>
                    <Button
                      className="btn-custom2"
                      onClick={() => {
                        setShowClockInModal(false)
                        setClockInNote("")
                      }}
                      disabled={loading}
                    >
                      Annulla
                    </Button>
                  </Modal.Footer>
                </Modal>
                <Modal
                  show={showClockOutModal}
                  onHide={() => setShowClockOutModal(false)}
                  centered
                >
                  <Modal.Header closeButton>
                    <Modal.Title>Timbra uscita</Modal.Title>
                  </Modal.Header>

                  <Modal.Body>
                    <Form.Group>
                      <Form.Label>Nota (facoltativa)</Form.Label>

                      <Form.Control
                        type="text"
                        value={clockOutNote}
                        onChange={(e) => setClockOutNote(e.target.value)}
                        placeholder="Inserisci una nota..."
                      />
                    </Form.Group>
                  </Modal.Body>

                  <Modal.Footer>
                    <Button
                      variant="secondary"
                      onClick={() => {
                        setShowClockOutModal(false)
                        setClockOutNote("")
                      }}
                      disabled={loading}
                    >
                      Annulla
                    </Button>

                    <Button
                      className="btn-custom2"
                      onClick={handleConfirmClockOut}
                      disabled={loading}
                    >
                      {loading ? "Timbratura..." : "Timbra"}
                    </Button>
                  </Modal.Footer>
                </Modal>
                <div>
                  {clocking?.actualStartTime && (
                    <p className="text-muted mt-2">
                      Entrata ore:{" "}
                      <span className="text-dark">
                        {clocking.actualStartTime}
                      </span>
                      <br></br>
                      {clocking.note && (
                        <>
                          Note:{" "}
                          <span className="text-dark">{clocking.note}</span>
                        </>
                      )}
                    </p>
                  )}
                  {clocking?.actualEndTime && (
                    <p className="text-muted mt-2">
                      Usicta ore:{" "}
                      <span className="text-dark">
                        {clocking.actualEndTime}
                      </span>
                      <br></br>
                      {clocking.note && (
                        <>
                          Note:{" "}
                          <span className="text-dark">{clocking.note}</span>
                        </>
                      )}
                    </p>
                  )}
                </div>
              </div>
            </div>
          </Col>

          {/* COLONNA DESTRA: Turno del giorno */}
          <Col
            xs={12}
            md={7}
            className="bg-light ms-3 d-flex flex-column justify-content-center p-4 border border-secondary rounded shadow-sm"
          >
            {/* Box del turno del giorno */}
            <Card className="mb-4 shadow-sm">
              <Card.Body>
                <Card.Title className=" text-primary">
                  Turno del giorno
                </Card.Title>
                <Form>
                  <Form.Group className="mb-3" controlId="FormDate">
                    <Form.Control
                      className="w-50 mt-3 input"
                      type="date"
                      value={selectedDate}
                      onChange={(e) => setSelectedDate(e.target.value)}
                    />
                  </Form.Group>
                </Form>
              </Card.Body>
            </Card>

            {todayAssignment?.assignmentType === "WORK" ? (
              <Card className="shadow-sm my-3">
                <Card.Body>
                  <Table
                    striped
                    bordered
                    hover
                    responsive
                    className="text-center align-middle"
                  >
                    <thead>
                      <tr>
                        <th className="text-center text-smaller text-dark w-50">
                          Ufficio/Sede
                        </th>
                        <th className="text-center text-smaller text-dark w-50">
                          Turno
                        </th>
                      </tr>
                    </thead>

                    <tbody>
                      <tr>
                        <td className="text-center text-smaller text-dark">
                          {todayAssignment.officeName || "Nessuna sede"}
                        </td>

                        <td className="text-center text-smaller text-dark">
                          {todayAssignment.startTime} -{" "}
                          {todayAssignment.endTime}
                        </td>
                      </tr>
                    </tbody>
                  </Table>
                </Card.Body>
              </Card>
            ) : todayAssignment ? (
              <Card className="shadow-sm my-3">
                <Card.Body>
                  <Table striped bordered hover responsive>
                    <thead>
                      <tr>
                        <th className="text-center text-smaller text-dark w-50">
                          Ufficio/Sede
                        </th>
                        <th className="text-center text-smaller text-dark w-50">
                          Turno
                        </th>
                      </tr>
                    </thead>

                    <tbody>
                      <tr>
                        <td
                          colSpan={2}
                          className={`text-center text-smaller text-dark ${getAssignmentTypeClass(todayAssignment.assignmentType)}`}
                        >
                          {translateAssignmentType(
                            todayAssignment.assignmentType,
                          )}
                        </td>
                      </tr>
                    </tbody>
                  </Table>
                </Card.Body>
              </Card>
            ) : (
              <Card className="shadow-sm my-3">
                <Card.Body>
                  <Table striped bordered hover responsive>
                    <thead>
                      <tr>
                        <th className="text-center text-smaller text-dark w-50">
                          Ufficio/Sede
                        </th>
                        <th className="text-center text-smaller text-dark w-50">
                          Turno
                        </th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr>
                        <td
                          colSpan={2}
                          className="text-muted small-text text-center"
                        >
                          Nessuna assegnazione per oggi
                        </td>
                      </tr>
                    </tbody>
                  </Table>
                </Card.Body>
              </Card>
            )}

            <Card className="shadow-sm">
              <Card.Body>
                <Card.Title className="text-primary mb-3">
                  Richieste con certificato
                </Card.Title>
                <p className="text-muted">
                  Gestisci le richieste con certificato
                </p>
                <Button
                  className="btn-custom1"
                  onClick={() => navigate("/requests/hr")}
                >
                  Vai alle richieste
                </Button>
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Container>
    </>
  )
}
