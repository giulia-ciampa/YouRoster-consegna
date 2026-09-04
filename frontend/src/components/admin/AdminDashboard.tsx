import { useEffect, useState } from "react"
import {
  fetchAcceptAccount,
  fetchAssignmentsByDate,
  fetchClockingsByUser,
  fetchDailyTasks,
  fetchPendingAccounts,
  fetchRejectAccount,
} from "../../services/adminService"
import type { Clocking, ShiftAssignment, ShiftTask } from "../../types/shift"
import AdminNavbar from "./AdminNavbar"
import {
  Container,
  Row,
  Form,
  Col,
  Dropdown,
  Button,
  Modal,
  Table,
  Card,
} from "react-bootstrap"
import { PiBellRingingLight } from "react-icons/pi"
import "../../styles/mobileText.css"
import { TiTick } from "react-icons/ti"
import type {
  AccountToApprove,
  PendingAccountDTO,
} from "../../types/approveRequest"
import { IoClose } from "react-icons/io5"
import type { UserAccount } from "../../types/users"
import type { OfficeResponseDTO } from "../../types/office"
import { fetchActiveOffices } from "../../services/officeService"

const AdminDashBoard = () => {
  // Stati per i dati
  const [assignments, setAssignments] = useState<ShiftAssignment[]>([])
  const [tasks, setTasks] = useState<ShiftTask[]>([])
  const [clockings, setClockings] = useState<Record<string, Clocking>>({})
  const [pendingAccounts, setPendingAccounts] = useState<PendingAccountDTO[]>(
    [],
  )

  //account da approvare + assegna sede e ruolo
  const [showModal, setShowModal] = useState<boolean>(false)
  const [accountToApprove, setAccountToApprove] =
    useState<AccountToApprove | null>(null)
  const [selectedRole, setSelectedRole] = useState<string>("STAFF")
  const [selectedOfficeId, setSelectedOfficeId] = useState<string>("")
  const [officesList, setOfficesList] = useState<OfficeResponseDTO[]>([])

  //caricamento e errori
  const [loadingAccounts, setLoadingAccounts] = useState(false)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")

  //form data
  const today = new Date().toISOString().split("T")[0]
  const [selectedDate, setSelectedDate] = useState(today)

  // Funzione per caricare gli account in sospeso
  useEffect(() => {
    const loadPendingAccounts = async () => {
      try {
        setLoadingAccounts(true)
        const data = await fetchPendingAccounts()
        setPendingAccounts(data.content || data)
      } catch (error) {
        console.error("Errore nel caricamento degli account in sospeso:", error)
      } finally {
        setLoadingAccounts(false)
      }
    }
    loadPendingAccounts()
  }, [])

  //accetta account
  const handleApprove = async (
    accountId: string,
    role: string,
    officeId: string | null,
  ) => {
    try {
      const response = await fetchAcceptAccount(accountId, role, officeId)
      console.log(response.message) // Messaggio di successo dal backend

      window.alert(response.message)
      // Ricarica la lista o rimuovi l'account approvato dallo stato locale
      setPendingAccounts((prev) =>
        prev.filter((acc) => acc.accountId !== accountId),
      )
    } catch (err: unknown) {
      if (err instanceof Error) {
        alert(err.message)
      }
    }
  }

  //APRI IL MODALE
  const handleOpenApprovalModal = async (account: UserAccount) => {
    setAccountToApprove(account)
    setSelectedRole("STAFF") // reset di base
    setSelectedOfficeId("")
    setShowModal(true)

    try {
      const offices = await fetchActiveOffices()
      setOfficesList(offices)
    } catch (err) {
      console.error("Errore nel caricamento delle sedi", err)
    }
  }

  //elimina richiesta
  const handleReject = async (accountId: string) => {
    try {
      const response = await fetchRejectAccount(accountId)
      window.alert(response.message)
      // Ricarica la lista o rimuovi l'account approvato dallo stato locale
      setPendingAccounts((prev) =>
        prev.filter((acc) => acc.accountId !== accountId),
      )
    } catch (err: unknown) {
      if (err instanceof Error) {
        alert(err.message)
      }
    }
  }

  useEffect(() => {
    const loadDashboardData = async (date: string) => {
      try {
        setLoading(true)
        setError("")

        const assignmentsData = await fetchAssignmentsByDate(date)
        setAssignments(assignmentsData.content || assignmentsData)

        const tasksData = await fetchDailyTasks(date)
        setTasks(tasksData.content || tasksData)

        const assignmentList = assignmentsData.content || assignmentsData
        const clockingMap: Record<string, Clocking> = {}

        for (const assignment of assignmentList) {
          const userId = assignment.user?.id
          if (userId) {
            try {
              const clockingData = await fetchClockingsByUser(userId, date)
              const userClockings = clockingData.content || clockingData
              if (userClockings.length > 0) {
                clockingMap[userId] = userClockings[0]
              }
            } catch (err) {
              console.error(`Errore timbrature per utente ${userId}:`, err)
            }
          }
        }
        setClockings(clockingMap)
      } catch (err) {
        console.error("Errore nel caricamento della dashboard:", err)
        setError("Impossibile caricare i dati della dashboard.")
      } finally {
        setLoading(false)
      }
    }

    loadDashboardData(selectedDate)
  }, [selectedDate])

  if (loading)
    return (
      <div className="text-center p-5 text-primary background2">
        Caricamento in corso...{" "}
      </div>
    )
  if (error)
    return (
      <div className="text-warning border border-warning border-1 p-4 rounded-2  m-5">
        {error}
      </div>
    )

  return (
    <>
      {/* navbar*/}
      <AdminNavbar />
      <Container
        fluid
        className="d-flex justify-content-center align-items-center flex-grow-1 my-4"
      >
        <Row className="g-4 w-100 justify-content-center">
          <Col xs={12} md={11}>
            <div className="d-flex justify-content-between align-items-center mb-4">
              <h3 className="text-dark small-title mb-0">
                Panoramica Giornaliera
              </h3>
              {/* Selettore della data */}
              <div className="d-flex align-items-center">
                <Form>
                  <Form.Group controlId="formDate">
                    <Form.Control
                      type="date"
                      className="form-control w-auto border border-1 border-secondary"
                      value={selectedDate}
                      onChange={(e) => setSelectedDate(e.target.value)}
                    />
                  </Form.Group>
                </Form>

                {/*DROPDOWN RICHIESTE IN SOSPESO */}
                <Dropdown align="end" className="myDropDown">
                  <Dropdown.Toggle
                    as="div"
                    className="position-relative d-inline-block"
                  >
                    <Button className="text-secondary border-0 bg-transparent">
                      {" "}
                      <PiBellRingingLight size={26} />
                    </Button>

                    {/* Mostra il badge rosso solo se ci sono richieste in sospeso */}
                    {pendingAccounts.length > 0 && (
                      <span
                        className="position-absolute top-0 start-100  badge rounded-pill bg-danger"
                        style={{ fontSize: "0.5rem", translate: "-100% -7%" }}
                      >
                        {pendingAccounts.length}
                      </span>
                    )}
                  </Dropdown.Toggle>

                  <Dropdown.Menu
                    className="shadow-sm border-0 p-2"
                    style={{
                      minWidth: "300px",
                      maxHeight: "350px",
                      overflowY: "auto",
                    }}
                  >
                    <Dropdown.Header className="fw-bold text-dark">
                      Richieste di Registrazione
                    </Dropdown.Header>
                    <Dropdown.Divider />

                    {loadingAccounts ? (
                      <div className="text-center text-muted small py-3">
                        Caricamento...
                      </div>
                    ) : pendingAccounts.length === 0 ? (
                      <div className="text-center text-muted small py-3">
                        Nessuna nuova richiesta
                      </div>
                    ) : (
                      pendingAccounts.map((account) => (
                        <div
                          key={account.accountId}
                          className="px-2 py-2 d-flex justify-content-between align-items-center border-bottom"
                        >
                          <div>
                            <p className="mb-0 fw-semibold small">
                              {account.name} {account.surname}
                            </p>
                            <p
                              className="mb-0 text-muted"
                              style={{ fontSize: "0.75rem" }}
                            >
                              {account.email}
                            </p>
                          </div>
                          <div>
                            {/* Pulsanti per approvare o rifiutare */}
                            <Button
                              className="bg-transparent border border-1 border-success p-0 me-1"
                              title="Approva"
                              onClick={() => {
                                handleOpenApprovalModal(account)
                              }}
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
                              onClick={() => handleReject(account.accountId)}
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
                <Modal
                  show={showModal}
                  onHide={() => setShowModal(false)}
                  centered
                >
                  <Modal.Header closeButton>
                    <Modal.Title>Approva Registrazione</Modal.Title>
                  </Modal.Header>
                  <Modal.Body>
                    {accountToApprove && (
                      <p>
                        Stai approvando l'account di{" "}
                        <strong>
                          {accountToApprove.name} {accountToApprove.surname}
                        </strong>
                        .
                      </p>
                    )}

                    {/* Selezione del Ruolo */}
                    <Form.Group className="mb-3">
                      <Form.Label>Assegna Ruolo</Form.Label>
                      <Dropdown
                        onSelect={(eventKey) => {
                          if (eventKey) {
                            setSelectedRole(eventKey)
                            setError("")
                          }
                        }}
                      >
                        <Dropdown.Toggle
                          className="w-100 bg-white small-text text-muted text-start input d-flex justify-content-between align-items-center"
                          id="dropdown-role"
                        >
                          {selectedRole === "COORDINATOR"
                            ? "COORDINATORE"
                            : selectedRole === "MANAGER"
                              ? "MANAGER"
                              : selectedRole === "SHIFT MANAGER"
                                ? "RESPONSABILE TURNI"
                                : selectedRole === "HR"
                                  ? "RISORSE UMANE"
                                  : selectedRole === "AP E PAYROLL SPECIALIST"
                                    ? "PAYROLL SPECIALIST"
                                    : "STAFF"}
                        </Dropdown.Toggle>
                        {/* menu che si apre */}
                        <Dropdown.Menu className="w-100 small-text">
                          <Dropdown.Item eventKey="COORDINATOR">
                            COORDINATORE
                          </Dropdown.Item>
                          <Dropdown.Item eventKey="MANAGER">
                            MANAGER
                          </Dropdown.Item>
                          <Dropdown.Item eventKey="SHIFT MANAGER">
                            RESPONSABILE TURNI
                          </Dropdown.Item>
                          <Dropdown.Item eventKey="HR">
                            RISORSE UMANE
                          </Dropdown.Item>
                          <Dropdown.Item eventKey="AP E PAYROLL SPECIALIST">
                            PAYROLL SPECIALIST
                          </Dropdown.Item>
                        </Dropdown.Menu>
                      </Dropdown>
                    </Form.Group>

                    {/* Selezione dell'Ufficio (compare SOLO se il ruolo è COORDINATOR) */}
                    {selectedRole === "COORDINATOR" && (
                      <Form.Group className="mb-3">
                        <Form.Label className="text-warning">
                          Seleziona Sede (Obbligatoria per Coordinatore)
                        </Form.Label>
                        <Dropdown
                          onSelect={(eventKey) => {
                            if (eventKey) {
                              setSelectedOfficeId(eventKey)
                              setError("")
                            }
                          }}
                        >
                          <Dropdown.Toggle
                            className="w-100 bg-white small-text text-muted text-start input d-flex justify-content-between align-items-center"
                            id="dropdown-office"
                          >
                            {/* Mostra il nome dell'ufficio selezionato o un placeholder */}
                            {selectedOfficeId
                              ? officesList.find(
                                  (o) => o.id.toString() === selectedOfficeId,
                                )?.name
                              : "Seleziona un ufficio..."}
                          </Dropdown.Toggle>

                          <Dropdown.Menu className="w-100">
                            {officesList.map((office) => (
                              <Dropdown.Item
                                key={office.id}
                                eventKey={office.id.toString()}
                              >
                                {office.name}
                              </Dropdown.Item>
                            ))}
                          </Dropdown.Menu>
                        </Dropdown>
                      </Form.Group>
                    )}
                  </Modal.Body>
                  <Modal.Footer>
                    <Button
                      className="btn-custom1"
                      onClick={() => {
                        handleApprove(
                          accountToApprove?.accountId ?? "",
                          selectedRole,
                          selectedOfficeId ? selectedOfficeId : null,
                        )
                        setShowModal(false)
                      }}
                    >
                      Conferma e Approva
                    </Button>
                    <Button
                      className="btn-custom2"
                      onClick={() => setShowModal(false)}
                    >
                      Annulla
                    </Button>
                  </Modal.Footer>
                </Modal>
              </div>
            </div>

            {/* Tabella principale */}
            <Card className="bg-light border border-secondary rounded shadow-sm">
              <Card.Body className="p-4">
                <h4 className="text-primary fw-semibold mb-4 small-title">
                  Turnazioni
                </h4>
                <Table
                  bordered
                  hover
                  responsive
                  className="text-center align-middle"
                >
                  <thead className="table-secondary">
                    <tr>
                      <th className="text-dark smaller-text">Dipendente</th>
                      <th className="text-dark smaller-text">Ufficio / Sede</th>
                      <th className="text-dark smaller-text">Turno Previsto</th>
                      <th className="text-dark smaller-text">Ruolo</th>
                      <th className="text-dark smaller-text">Task Assegnato</th>
                    </tr>
                  </thead>
                  <tbody>
                    {assignments.length > 0 ? (
                      assignments.map((assignment) => {
                        // Trova il task corrispondente per questo turno/utente se c'è un collegamento
                        const matchedTask = tasks.find(
                          (t) => t.shiftAssignment?.id === assignment.id,
                        )

                        return (
                          <tr key={assignment.id}>
                            <td>
                              <strong>
                                {assignment.userName} {assignment.userSurname}
                              </strong>
                            </td>
                            <td>
                              {assignment.officeName ||
                                "Nessun ufficio assegnato"}
                            </td>
                            <td>
                              {assignment.startTime} - {assignment.endTime}
                            </td>
                            <td>{assignment.roleNames}</td>
                            <td>
                              {matchedTask ? (
                                <span>
                                  {matchedTask.title ||
                                    matchedTask.description ||
                                    "Task attivo"}
                                </span>
                              ) : (
                                <span className="text-muted">Nessun task</span>
                              )}
                            </td>
                          </tr>
                        )
                      })
                    ) : (
                      <tr>
                        <td colSpan={5} className="text-center py-4 text-muted">
                          Nessun turno trovato per la data selezionata.
                        </td>
                      </tr>
                    )}
                  </tbody>
                </Table>
              </Card.Body>
            </Card>
          </Col>
          {/* Seconda Tabella: Timbrature */}
          <Col xs={12} md={11}>
            <Card className="bg-light border border-secondary rounded shadow-sm">
              <Card.Body className="p-4">
                <h4 className="text-primary fw-semibold mb-4 small-title">
                  Timbrature giornaliere
                </h4>

                <Table
                  bordered
                  hover
                  responsive
                  className="text-center align-middle"
                >
                  <thead className="table-secondary">
                    <tr>
                      <th className="text-dark smaller-text">Dipendente</th>
                      <th className="text-dark smaller-text">Entrata</th>
                      <th className="text-dark smaller-text">Uscita</th>
                      <th className="text-dark smaller-text">Ore lavorate</th>
                      <th className="text-dark smaller-text">
                        Minuti lavorati
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {assignments.length > 0 ? (
                      assignments.map((assignment) => {
                        const userId = assignment.userId
                        // Recuperiamo la timbratura associata a questo utente tramite il suo ID
                        const userClocking = userId
                          ? clockings[userId]
                          : undefined

                        return (
                          <tr key={`clocking-${assignment.id}`}>
                            <td>
                              <strong>
                                {assignment.userName} {assignment.userSurname}
                              </strong>
                            </td>
                            <td>
                              {userClocking?.actualStartTime ? (
                                <span className="text-success fw-bold">
                                  {userClocking.actualStartTime}
                                </span>
                              ) : (
                                <span className="text-muted">Non timbrato</span>
                              )}
                            </td>
                            <td>
                              {userClocking?.actualEndTime ? (
                                <span className="text-danger fw-bold">
                                  {userClocking.actualEndTime}
                                </span>
                              ) : (
                                <span className="text-muted">
                                  Non timbrato / Non timbrato
                                </span>
                              )}
                            </td>
                            <td>
                              {userClocking?.workedHours ? (
                                <span>{userClocking.workedHours} h</span>
                              ) : (
                                <span className="text-muted">-</span>
                              )}
                            </td>
                            <td>
                              {userClocking?.workedMinutes ? (
                                <span>{userClocking.workedMinutes} h</span>
                              ) : (
                                <span className="text-muted">-</span>
                              )}
                            </td>
                          </tr>
                        )
                      })
                    ) : (
                      <tr>
                        <td colSpan={5} className="text-center py-4 text-muted">
                          Nessuna timbratura trovata per la data selezionata.
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

export default AdminDashBoard
