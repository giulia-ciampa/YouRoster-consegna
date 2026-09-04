import {
  Col,
  Container,
  Row,
  Form,
  Table,
  Card,
  Spinner,
  Alert,
  Button,
  Modal,
} from "react-bootstrap"
import { PiUserLight } from "react-icons/pi"
import { UserNavbar } from "./UserNavbar"
import { useEffect, useState } from "react"
import type { Clocking, ShiftAssignment } from "../../types/shift"
import {
  getColleaguesWithMyShift,
  getMyAssignmentsByDate,
} from "../../services/shiftAssignmentService"
import { getMyProfile, updateMyAvatar } from "../../services/userService" // <-- La funzione per i dati utente
import type { UserProfileResponse } from "../../types/users"
import { MdAddAPhoto } from "react-icons/md"
import { useDispatch } from "react-redux"
import { setUser } from "../../redux/reducers/userSlice"
import {
  getMyClockingForDate,
  handleClockIn,
  handleClockOut,
} from "../../services/clockingsService"

export const UserDashboard = () => {
  const [userProfile, setUserProfile] = useState<UserProfileResponse | null>(
    null,
  )
  const [todayAssignment, setTodayAssignment] =
    useState<ShiftAssignment | null>(null)
  const [coworkers, setCoworkers] = useState<ShiftAssignment[]>([])
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

  const dispatch = useDispatch()

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

          try {
            const colleaguesData = await getColleaguesWithMyShift(selectedDate)

            setCoworkers(colleaguesData)
          } catch (error) {
            console.log("Nessun collega trovato per questa data:", error)
            setCoworkers([])
          }
        } else {
          setTodayAssignment(null)
          setCoworkers([])
        }
      } catch (error) {
        console.error(
          "Errore nel recupero dei turni per la data selezionata",
          error,
        )

        setTodayAssignment(null)
        setCoworkers([])
      }
    }

    fetchAssignmentsForDate()
  }, [selectedDate])

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
      <UserNavbar />
      <Container
        fluid
        className="d-flex flex-column justify-content-center align-items-center flex-grow-1 my-2"
      >
        {/* TITOLO */}
        <Row className="w-100 justify-content-center mb-4">
          <Col xs={12} md={11} className="ps-0">
            <div className="text-start">
              <h3 className="small-title text-dark mb-0">
                Panoramica giornaliera
              </h3>
            </div>
          </Col>
        </Row>
        <Row className=" w-100 justify-content-center">
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
              <Spinner animation="border" size="sm" />
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

            {/* Resto del form data... */}
          </Col>

          {/* COLONNA DESTRA: Turno del giorno + Tabella colleghi */}
          <Col
            xs={12}
            md={7}
            className="ms-lg-3 bg-light d-flex flex-column justify-content-center p-4 border border-secondary rounded shadow-sm"
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
                  <Table striped bordered hover responsive>
                    <thead>
                      <tr>
                        <th className="text-center text-smaller text-dark">
                          Ufficio/Sede
                        </th>
                        <th className="text-center text-smaller text-dark">
                          Turno
                        </th>
                        <th className="text-center text-smaller text-dark">
                          Mansioni
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

                        <td className="text-center text-smaller text-dark">
                          {todayAssignment.tasks ||
                            "Non ci sono mansioni assegnate"}
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
                        <th className="text-center text-smaller text-dark">
                          Ufficio/Sede
                        </th>
                        <th className="text-center text-smaller text-dark">
                          Turno
                        </th>
                        <th className="text-center text-smaller text-dark">
                          Mansioni
                        </th>
                      </tr>
                    </thead>

                    <tbody>
                      <tr>
                        <td
                          colSpan={3}
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
                        <th className="text-center text-smaller text-dark">
                          Ufficio/Sede
                        </th>
                        <th className="text-center text-smaller text-dark">
                          Turno
                        </th>
                        <th className="text-center text-smaller text-dark">
                          Mansioni
                        </th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr>
                        <td
                          colSpan={3}
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

            {/* Tabella chi è in turno con te */}
            <Card className="shadow-sm">
              <Card.Body>
                <Card.Title className="text-primary mb-3">
                  Colleghi in turno con te
                </Card.Title>
                <Table striped bordered hover responsive>
                  <thead>
                    <tr>
                      <th className="text-center text-smaller text-dark">
                        Nome
                      </th>
                      <th className="text-center text-smaller text-dark">
                        Cognome
                      </th>
                      <th className="text-center text-smaller text-dark">
                        Ruolo
                      </th>
                      <th className="text-center text-smaller text-dark">
                        Mansioni
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {coworkers.length > 0 ? (
                      coworkers.map((colleague, index) => (
                        <tr key={index}>
                          <td className="text-center text-smaller text-dark">
                            {colleague.userName}
                          </td>
                          <td className="text-center text-smaller text-dark">
                            {colleague.userSurname}
                          </td>
                          <td className="text-center text-smaller text-dark">
                            {colleague.roleNames}
                          </td>
                          <td className="text-center text-smaller text-dark">
                            {colleague.tasks}
                          </td>
                        </tr>
                      ))
                    ) : (
                      <tr>
                        <td colSpan={4} className="text-center text-muted">
                          Nessun collega in turno o nessun turno attivo.
                        </td>
                      </tr>
                    )}
                  </tbody>
                </Table>
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Container>
    </>
  )
}
