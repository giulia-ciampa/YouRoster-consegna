import {
  Col,
  Container,
  Row,
  Form,
  Button,
  Table,
  Modal,
} from "react-bootstrap"
import { ShiftManagerNavbar } from "./ShiftManagerNavbar"
import { useCallback, useEffect, useState } from "react"
import { IoIosArrowBack, IoIosArrowForward, IoMdAdd } from "react-icons/io"
import "../../styles/mobileText.css"
import {
  createShiftAssignment,
  deleteShiftAssignment,
  getAssignmentsBetweenDates,
  updateShiftAssignment,
} from "../../services/shiftAssignmentService"
import type {
  AssignedUser,
  AssignmentType,
  Shift,
  ShiftAssignment,
} from "../../types/shift"
import { getActiveUsers } from "../../services/userService"
import { fetchShifts } from "../../services/shiftService"
import { FaPencilAlt, FaRegTrashAlt } from "react-icons/fa"

export const ShiftManagerAssignment = () => {
  //-----------------------------DATE
  const formatDate = (date: Date) => {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, "0")
    const day = String(date.getDate()).padStart(2, "0")

    return `${year}-${month}-${day}`
  }

  const [currentMonth, setCurrentMonth] = useState(new Date())

  const goToPreviousMonth = () => {
    setCurrentMonth(
      new Date(currentMonth.getFullYear(), currentMonth.getMonth() - 1, 1),
    )
  }

  const goToNextMonth = () => {
    setCurrentMonth(
      new Date(currentMonth.getFullYear(), currentMonth.getMonth() + 1, 1),
    )
  }

  const monthLabel = currentMonth.toLocaleDateString("it-IT", {
    month: "long",
    year: "numeric",
  })

  const startDate = formatDate(
    new Date(currentMonth.getFullYear(), currentMonth.getMonth(), 1),
  )

  const endDate = formatDate(
    new Date(currentMonth.getFullYear(), currentMonth.getMonth() + 1, 0),
  )

  const normalizeDate = (d: string) => {
    if (!d) return ""

    // DD/MM/YYYY
    if (d.includes("/")) {
      const [day, month, year] = d.split("/")
      return `${year}-${month}-${day}`
    }

    return d.substring(0, 10)
  }
  //----------------------------------------------------------------------

  // Stati per il modale di assegnazione turno (al click del +)
  const [showShiftModal, setShowShiftModal] = useState<boolean>(false)
  const [activeCellData, setActiveCellData] = useState<{
    userId: string
    userName: string
    date: string
  } | null>(null)

  // Stati interni al modale per la selezione
  const [shifts, setShifts] = useState<Shift[]>([])
  const [selectedOfficeName, setSelectedOfficeName] = useState<string>("")
  const [selectedShiftId, setSelectedShiftId] = useState<string>("")

  // TIPI DI ASSEGNAZIONE
  const [assignmentType, setAssignmentType] = useState<AssignmentType>("WORK")

  //ASSEGNAZIONE UTENTE
  const [assignedUser, setAssignedUser] = useState<AssignedUser[]>([])

  //GESTIONE CARICAMENTO ED ERRORI
  const [loading, setLoading] = useState(false)

  const [assignments, setAssignments] = useState<ShiftAssignment[]>([])

  const [currentPage, setCurrentPage] = useState(0)

  //MODIFICA ASSEGNAZIONE
  const [editingAssignment, setEditingAssignment] =
    useState<ShiftAssignment | null>(null)

  const handleFetchActiveUsers = async () => {
    setLoading(true)

    try {
      const data = await getActiveUsers()

      setAssignedUser(data)
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : "Errore imprevisto."
      setAssignedUser([])
      alert(message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    const loadUsers = async () => {
      await handleFetchActiveUsers()
    }

    loadUsers()
  }, [])

  //funzione per recuperare le assegnazioni
  const loadAssignments = useCallback(async () => {
    try {
      const data = await getAssignmentsBetweenDates(startDate, endDate)

      setAssignments(data.content)
    } catch (error) {
      console.error("Errore nel caricamento delle assegnazioni:", error)
    }
  }, [startDate, endDate])

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadAssignments()
  }, [loadAssignments])

  // Funzione che si attiva al click sul + della tabella
  const handleOpenShiftModal = (
    userId: string,
    userName: string,
    date: string,
  ) => {
    setActiveCellData({ userId, userName, date })
    setEditingAssignment(null)
    setSelectedOfficeName("")
    setSelectedShiftId("")
    setAssignmentType("WORK")
    setShowShiftModal(true)
  }

  // Funzione per generare l'intervallo di date tra inizio e fine
  const getDatesInRange = (startStr: string, endStr: string): string[] => {
    const dates: string[] = []

    const current = new Date(`${startStr}T00:00:00`)
    const end = new Date(`${endStr}T00:00:00`)

    while (current <= end) {
      dates.push(formatDate(current))

      current.setDate(current.getDate() + 1)
    }

    return dates
  }

  // Funzione per gestire il salvataggio dell'assegnazione turno
  const handleSaveAssignment = async () => {
    if (!activeCellData) {
      return
    }

    // Lo shift è obbligatorio solo per le assegnazioni WORK
    if (assignmentType === "WORK" && !selectedShiftId) {
      alert("Seleziona un turno prima di salvare.")
      return
    }

    const payload = {
      userId: activeCellData.userId,
      shiftDate: activeCellData.date,
      assignmentType,
      ...(assignmentType === "WORK" && {
        shiftId: selectedShiftId,
      }),
    }

    try {
      if (editingAssignment) {
        // MODIFICA
        await updateShiftAssignment(editingAssignment.id, {
          assignmentType,
          ...(assignmentType === "WORK" && {
            shiftId: selectedShiftId,
          }),
        })
      } else {
        // NUOVA ASSEGNAZIONE
        await createShiftAssignment(payload)
      }

      await loadAssignments()
      setShowShiftModal(false)
      setEditingAssignment(null)
    } catch (error: unknown) {
      console.error("Errore durante il salvataggio:", error)
      alert(
        error instanceof Error
          ? error.message
          : "Errore durante il salvataggio",
      )
    }
  }

  useEffect(() => {
    const loadShifts = async () => {
      try {
        const data = await fetchShifts(true, 0, 50)
        setShifts(data.content)
      } catch (error) {
        console.error("Errore nel caricamento dei turni:", error)
      }
    }

    loadShifts()
  }, [])

  // Prendi tutti i nomi degli uffici unici direttamente dai turni caricati
  const uniqueOffices = Array.from(
    new Set(shifts.map((shift) => shift.officeName)),
  )

  // Filtriamo i turni in base alla sede selezionata
  const availableShiftsForOffice = shifts.filter(
    (shift) => shift.officeName === selectedOfficeName,
  )

  //------------------------------------PAGINAZIONE

  const columnsDates = getDatesInRange(startDate, endDate)

  const sortedAssignedUser = [...assignedUser].sort((a, b) =>
    a.name.localeCompare(b.name, "it"),
  )

  const usersPerPage = 5

  const totalPages = Math.ceil(sortedAssignedUser.length / usersPerPage)

  const paginatedUsers = sortedAssignedUser.slice(
    currentPage * usersPerPage,
    currentPage * usersPerPage + usersPerPage,
  )

  //____________________________________________________

  //funzione per aprire il modale di modifica
  const handleOpenEditModal = (assignment: ShiftAssignment) => {
    setEditingAssignment(assignment)

    setActiveCellData({
      userId: assignment.userId,
      userName: assignment.userName,
      date: normalizeDate(assignment.shiftDate),
    })

    setSelectedOfficeName(assignment.officeName)
    setSelectedShiftId(assignment.shiftId ?? "")
    setAssignmentType(assignment.assignmentType)
    setShowShiftModal(true)
  }

  //funzione elimina assegnazione
  const handleDeleteAssignment = async () => {
    if (!editingAssignment) {
      return
    }

    try {
      await deleteShiftAssignment(editingAssignment.id)

      console.log("Assegnazione eliminata con successo")

      await loadAssignments()

      setShowShiftModal(false)
      setEditingAssignment(null)
    } catch (error: unknown) {
      console.error("Errore durante la cancellazione:", error)

      alert(
        (error as Error).message ||
          "Errore durante la cancellazione dell'assegnazione.",
      )
    }
  }

  return (
    <>
      <ShiftManagerNavbar />

      <Container
        fluid
        className="d-flex flex-column my-4 align-items-center flex-grow-1"
      >
        <Row className="w-100 justify-content-center mb-2">
          <Col xs={12} md={11} className="ps-0">
            <h3 className="small-title text-dark mb-0">Assegnazione turni</h3>
          </Col>
        </Row>

        <Row className="g-4 w-100 flex-column align-items-center mb-2">
          <Col xs={12} md={11}>
            <div className="d-flex justify-content-center align-items-center mt-4 mb-4">
              <Button
                className="btn-custom1 rounded-circle d-flex align-items-center justify-content-center small-text me-3"
                style={{ width: "20px", height: "20px", padding: "0" }}
                onClick={goToPreviousMonth}
              >
                <IoIosArrowBack />
              </Button>

              <h5 className="mb-0 text-capitalize fw-semibold small-title">
                {monthLabel}
              </h5>
              <p></p>

              <Button
                className="btn-custom1 rounded-circle d-flex align-items-center justify-content-center small-text ms-3"
                style={{ width: "20px", height: "20px", padding: "0" }}
                onClick={goToNextMonth}
              >
                <IoIosArrowForward />
              </Button>
            </div>
          </Col>
        </Row>

        {/*tabella */}
        <Row className="w-100 justify-content-center">
          <Col
            xs={12}
            md={11}
            className="bg-white p-4 border border-secondary rounded shadow-sm"
          >
            <div className="assignment-table-wrapper">
              <Table
                bordered
                hover
                className="text-center align-middle assignment-table"
              >
                <thead>
                  <tr>
                    <th className="text-center small-text text-dark sticky-column column-width">
                      Nome Dipendente
                    </th>

                    {columnsDates.map((date) => {
                      const [year, month, day] = date.split("-")
                      const displayDate = `${day}/${month}/${year}`

                      return (
                        <th
                          key={date}
                          className="small-text text-muted"
                          style={{ minWidth: "100px" }}
                        >
                          <div className="mb-1 fw-bold text-dark">
                            {displayDate}
                          </div>
                        </th>
                      )
                    })}
                  </tr>
                </thead>
                <tbody>
                  {paginatedUsers.map((employee) => (
                    <tr key={employee.userId}>
                      {/* Colonna del nome dipendente */}
                      <td className=" text-dark small-text fw-semibold sticky-column column-width">
                        <div className="d-flex justify-content-center align-items-center">
                          <span>{employee.name}</span>
                        </div>
                      </td>

                      {/* Colonne delle date per ogni dipendente */}

                      {columnsDates.map((dateStr, index) => {
                        const existingAssignment = assignments.find(
                          (a) =>
                            a.userId === employee.userId &&
                            normalizeDate(a.shiftDate) === dateStr,
                        )

                        return (
                          <td key={index} className="small-text">
                            {existingAssignment ? (
                              existingAssignment.assignmentType === "WORK" ? (
                                // WORK → MOSTRA IL TURNO + MATITA
                                <div>
                                  <div className="text-dark d-flex flex-column align-items-center">
                                    <p className="mb-0 fw-bold">
                                      {existingAssignment.officeName}
                                    </p>

                                    <p className="mb-0 text-start text-nowrap">
                                      {existingAssignment.startTime} -{" "}
                                      {existingAssignment.endTime}
                                    </p>
                                  </div>

                                  <div className="d-flex justify-content-end">
                                    <Button
                                      className="p-1 rounded-circle border-0 backgroundOrange text-light d-flex align-items-center justify-content-center small-text"
                                      onClick={() => {
                                        handleOpenEditModal(existingAssignment)
                                      }}
                                      disabled={loading}
                                    >
                                      <FaPencilAlt size={10} />
                                    </Button>
                                  </div>
                                </div>
                              ) : existingAssignment.assignmentType ===
                                "OFF" ? (
                                // OFF → MOSTRA RIPOSO + MATITA
                                <div>
                                  <div className="text-dark d-flex flex-column align-items-center">
                                    <p className="mb-0 text-secondary fw-bold">
                                      OFF
                                    </p>
                                  </div>

                                  <div className="d-flex justify-content-end">
                                    <Button
                                      className="p-1 rounded-circle border-0 backgroundOrange text-light d-flex align-items-center justify-content-center small-text"
                                      onClick={() => {
                                        handleOpenEditModal(existingAssignment)
                                      }}
                                      disabled={loading}
                                    >
                                      <FaPencilAlt size={10} />
                                    </Button>
                                  </div>
                                </div>
                              ) : (
                                // FERIE, MALATTIA, PERMESSO, ECC.
                                <div className="text-dark d-flex flex-column align-items-center">
                                  <p className="mb-0 text-secondary fw-bold">
                                    {existingAssignment.assignmentType ===
                                    "ON_HOLIDAY"
                                      ? "FERIE"
                                      : existingAssignment.assignmentType ===
                                          "MATERNITY"
                                        ? "MATERNITÀ"
                                        : existingAssignment.assignmentType ===
                                            "SICK"
                                          ? "MALATTIA"
                                          : existingAssignment.assignmentType ===
                                              "PARENTAL_LEAVE"
                                            ? "CONGEDO PARENTALE"
                                            : existingAssignment.assignmentType ===
                                                "ABSENT"
                                              ? "ASSENTE"
                                              : existingAssignment.assignmentType}
                                  </p>
                                </div>
                              )
                            ) : (
                              // NESSUNA ASSEGNAZIONE → MOSTRA +
                              <div className="d-flex justify-content-center align-items-center">
                                <Button
                                  className="p-1 btn-custom1 rounded-circle d-flex align-items-center justify-content-center small-text"
                                  onClick={() => {
                                    handleOpenShiftModal(
                                      employee.userId,
                                      employee.name,
                                      dateStr,
                                    )
                                  }}
                                  disabled={loading}
                                >
                                  <IoMdAdd size={10} className="p-0" />
                                </Button>
                              </div>
                            )}
                          </td>
                        )
                      })}
                    </tr>
                  ))}
                </tbody>
              </Table>
            </div>
            {totalPages > 1 && (
              <div className="d-flex justify-content-center align-items-center gap-2 mt-3">
                <Button
                  className="btn-custom1"
                  size="sm"
                  disabled={currentPage === 0}
                  onClick={() => setCurrentPage((prev) => prev - 1)}
                >
                  <IoIosArrowBack />
                </Button>

                {Array.from({ length: totalPages }, (_, index) => (
                  <Button
                    key={index}
                    className={
                      currentPage === index ? "primary" : "btn-custom1"
                    }
                    size="sm"
                    onClick={() => setCurrentPage(index)}
                  >
                    {index + 1}
                  </Button>
                ))}

                <Button
                  className="btn-custom1"
                  size="sm"
                  disabled={currentPage === totalPages - 1}
                  onClick={() => setCurrentPage((prev) => prev + 1)}
                >
                  <IoIosArrowForward />
                </Button>
              </div>
            )}
          </Col>
        </Row>
        <Modal
          show={showShiftModal}
          onHide={() => setShowShiftModal(false)}
          centered
        >
          <Modal.Header closeButton>
            <Modal.Title className="small-title">
              {" "}
              {editingAssignment ? "Aggiorna turno" : "Assegna turno"}
            </Modal.Title>
          </Modal.Header>
          <Modal.Body>
            {activeCellData && (
              <p className="text-muted small-text mb-3">
                Dipendente:{" "}
                <strong className="text-dark">{activeCellData.userName}</strong>{" "}
                | Data:{" "}
                <strong className="text-dark">
                  {activeCellData.date.split("-").reverse().join("/")}
                </strong>
              </p>
            )}

            <Form className="d-flex flex-column gap-3">
              <Form.Group className="mb-3">
                <Form.Label className="small-text text-muted">
                  Tipo assegnazione
                </Form.Label>

                <Form.Select
                  value={assignmentType}
                  onChange={(e) => {
                    const type = e.target.value as AssignmentType

                    setAssignmentType(type)

                    // Se non è WORK, non servono sede e turno
                    if (type !== "WORK") {
                      setSelectedOfficeName("")
                      setSelectedShiftId("")
                    }
                  }}
                >
                  <option value="WORK">Lavoro</option>
                  <option value="OFF">Riposo</option>
                  <option value="ON_HOLIDAY">Ferie</option>
                  <option value="SICK">Malattia</option>
                  <option value="MATERNITY">Maternità</option>
                  <option value="PATERNITY">Paternità</option>
                  <option value="PARENTAL_LEAVE">Congedo parentale</option>
                  <option value="ABSENT">Assente</option>
                </Form.Select>
              </Form.Group>
              {/* 1. Selezione della sede*/}
              {assignmentType === "WORK" && (
                <>
                  <Form.Group className="mb-3">
                    <Form.Label className="small-text text-muted">
                      Sede / Ufficio
                    </Form.Label>
                    <Form.Select
                      value={selectedOfficeName}
                      onChange={(e) => {
                        setSelectedOfficeName(e.target.value)
                        setSelectedShiftId("")
                      }}
                    >
                      <option value="">Seleziona sede...</option>
                      {uniqueOffices.map((officeName, index) => (
                        <option key={index} value={officeName}>
                          {officeName}
                        </option>
                      ))}
                    </Form.Select>
                  </Form.Group>

                  {/* 2. Selezione dell'orario / Turno disponibile */}
                  <Form.Group className="mb-3">
                    <Form.Label className="small-text text-muted">
                      Orario / Turno disponibile
                    </Form.Label>
                    <Form.Select
                      value={selectedShiftId}
                      onChange={(e) => setSelectedShiftId(e.target.value)}
                      disabled={!selectedOfficeName}
                    >
                      <option value="">Seleziona orario turno...</option>
                      {availableShiftsForOffice.map((shift) => (
                        <option key={shift.id} value={shift.id}>
                          {shift.startTime} - {shift.endTime}
                        </option>
                      ))}
                    </Form.Select>
                  </Form.Group>
                </>
              )}
            </Form>
          </Modal.Body>
          <Modal.Footer className="d-flex justify-content-end">
            {editingAssignment ? (
              <div className="me-auto">
                <Button
                  className="btn-custom3 d-flex justify-content-center align-items-center p-2"
                  onClick={() => {
                    handleDeleteAssignment()
                  }}
                >
                  {" "}
                  <FaRegTrashAlt />
                </Button>
              </div>
            ) : (
              ""
            )}

            <Button
              className="btn-custom1"
              onClick={() => {
                handleSaveAssignment()
              }}
            >
              {editingAssignment
                ? "Aggiorna assegnazione"
                : "Salva assegnazione"}
            </Button>
            <Button
              className="btn-custom2"
              onClick={() => setShowShiftModal(false)}
            >
              Annulla
            </Button>
          </Modal.Footer>
        </Modal>
      </Container>
    </>
  )
}
