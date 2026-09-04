import {
  Col,
  Container,
  Row,
  Form,
  Card,
  Table,
  Button,
  Spinner,
} from "react-bootstrap"
import { UserNavbar } from "./UserNavbar"
import { useEffect, useState } from "react"
import { getMyAssignmentsBetweenDates } from "../../services/shiftAssignmentService"
import type { ShiftAssignment } from "../../types/shift"
import { IoIosArrowBack, IoIosArrowForward, IoMdClose } from "react-icons/io"

export const UserShifts = () => {
  //il giorno della settimana inizia da lunedì
  const getMonday = (date: Date): Date => {
    const result = new Date(date)
    const day = result.getDay()

    const diff = day === 0 ? -6 : 1 - day

    result.setDate(result.getDate() + diff)

    return result
  }

  const [currentWeekStart, setCurrentWeekStart] = useState<Date>(
    getMonday(new Date()),
  )

  //funzione per ottenere i 7 giorni
  const getWeekDates = (startDate: Date): Date[] => {
    return Array.from({ length: 7 }, (_, index) => {
      const date = new Date(startDate)
      date.setDate(startDate.getDate() + index)
      return date
    })
  }

  //funzione dal giorno al giorno
  const getDatesInRange = (startDate: string, endDate: string): Date[] => {
    const dates: Date[] = []

    const [startYear, startMonth, startDay] = startDate.split("-").map(Number)
    const [endYear, endMonth, endDay] = endDate.split("-").map(Number)

    const start = new Date(startYear, startMonth - 1, startDay)
    const end = new Date(endYear, endMonth - 1, endDay)

    const current = new Date(start)

    while (current <= end) {
      dates.push(new Date(current))
      current.setDate(current.getDate() + 1)
    }

    return dates
  }

  const weekDates = getWeekDates(currentWeekStart)

  const [assignments, setAssignments] = useState<ShiftAssignment[]>([])
  const [loading, setLoading] = useState(false)

  const formatDateForApi = (date: Date): string => {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, "0")
    const day = String(date.getDate()).padStart(2, "0")

    return `${year}-${month}-${day}`
  }

  //giorno dela settimana
  const getDayName = (date: Date) => {
    return date.toLocaleDateString("it-IT", { weekday: "short" }).toUpperCase()
  }

  //frecce paginazione
  const goToPreviousWeek = () => {
    const previousWeek = new Date(currentWeekStart)
    previousWeek.setDate(previousWeek.getDate() - 7)
    setCurrentWeekStart(previousWeek)
  }

  const goToNextWeek = () => {
    const nextWeek = new Date(currentWeekStart)
    nextWeek.setDate(nextWeek.getDate() + 7)
    setCurrentWeekStart(nextWeek)
  }

  const [currentMonth, setCurrentMonth] = useState(new Date())

  useEffect(() => {
    const loadMonthAssignments = async () => {
      try {
        setLoading(true)

        const year = currentMonth.getFullYear()
        const month = currentMonth.getMonth()

        // Primo giorno del mese
        const firstDay = new Date(year, month, 1)

        // Ultimo giorno del mese
        const lastDay = new Date(year, month + 1, 0)

        const startDate = formatDateForApi(firstDay)
        const endDate = formatDateForApi(lastDay)

        const data = await getMyAssignmentsBetweenDates(startDate, endDate)
        console.log("INTERVALLO CALENDARIO:", startDate, endDate)
        console.log("ASSIGNMENTS CALENDARIO:", data)
        console.log(
          "TIPI ASSEGNAZIONI:",
          data.map((assignment) => ({
            date: assignment.shiftDate,
            type: assignment.assignmentType,
          })),
        )

        setAssignments(data)
      } catch (error: unknown) {
        alert((error as Error).message || "Errore nel recupero dei tuoi turni.")
      } finally {
        setLoading(false)
      }
    }

    loadMonthAssignments()
  }, [currentMonth])

  //frecce per il mese
  const goToPreviousMonth = () => {
    setCurrentMonth((prev) => {
      const date = new Date(prev)
      date.setMonth(date.getMonth() - 1)
      return date
    })
  }

  const goToNextMonth = () => {
    setCurrentMonth((prev) => {
      const date = new Date(prev)
      date.setMonth(date.getMonth() + 1)
      return date
    })
  }

  //funzione per il colore
  const getAssignmentClass = (assignment: ShiftAssignment) => {
    switch (assignment.assignmentType) {
      case "WORK":
        return "turno"

      case "PERMISSION":
        return "permesso"

      case "OFF":
        return "off"

      case "ON_HOLIDAY":
        return "ferie"

      case "MATERNITY":
        return "richiesta_certificato"

      case "PATERNITY":
        return "richiesta_certificato"

      case "PARENTAL_LEAVE":
        return "richiesta_certificato"

      default:
        return ""
    }
  }

  //funzione per il tipo di assegnazione
  const getAssignmentLabel = (assignment: ShiftAssignment) => {
    switch (assignment.assignmentType) {
      case "OFF":
        return "OFF"

      case "ON_HOLIDAY":
        return "FERIE"

      case "MATERNITY":
        return "MATERNITÀ"

      case "PATERNITY":
        return "PATERNITÀ"

      case "PARENTAL_LEAVE":
        return "CONGEDO PARENTALE"

      case "PERMISSION":
        return "PERMESSO"

      default:
        return ""
    }
  }

  const formatDate = (date: Date): string => {
    return date.toLocaleDateString("it-IT")
  }

  //terza tabella
  const [showSearchResults, setShowSearchResults] = useState(false)
  const [searchStartDate, setSearchStartDate] = useState("")
  const [searchEndDate, setSearchEndDate] = useState("")
  const [searchResults, setSearchResults] = useState<ShiftAssignment[]>([])

  const handleSearch = async () => {
    console.log("DATA INIZIO:", searchStartDate)
    console.log("DATA FINE:", searchEndDate)
    if (!searchStartDate || !searchEndDate) {
      return alert(
        "Inserisci una data di inizio e una data di fine per la ricerca",
      )
    }

    try {
      const results = await getMyAssignmentsBetweenDates(
        searchStartDate,
        searchEndDate,
      )

      console.log("RISULTATI GET:", results)

      setSearchResults(results)
      setShowSearchResults(true)
    } catch (error) {
      console.error("Errore nella ricerca delle assegnazioni:", error)
    }
  }
  const searchDates =
    searchStartDate && searchEndDate
      ? getDatesInRange(searchStartDate, searchEndDate)
      : []

  const resetForm = () => {
    setSearchStartDate("")
    setSearchEndDate("")
  }

  return (
    <>
      <UserNavbar />
      <Container
        fluid
        className="d-flex flex-column justify-content-center align-items-center flex-grow-1 my-4"
      >
        {/* TITOLO */}
        <Row className="w-100 mb-3 justify-content-center">
          <Col xs={12} md={11} className="ps-0">
            <div className="text-start">
              <h3 className="small-title text-dark mb-0">I miei turni</h3>
            </div>
          </Col>
        </Row>
        {/*DATE DA SELEZIONARE */}
        <Row className="w-100 mb-3 justify-content-center">
          <Col xs={12} md={11} className="ps-0">
            <Card className="p-3 border border-1 border-secondary">
              <Form className="d-flex align-items-center">
                <Form.Group className="mb-3 me-3" controlId="FormStartDate">
                  <Form.Label className="small-text text-muted mb-0">
                    Dalla data
                  </Form.Label>
                  <Form.Control
                    type="date"
                    value={searchStartDate}
                    onChange={(e) => setSearchStartDate(e.target.value)}
                  />
                </Form.Group>
                <Form.Group className="mb-3" controlId="FormEndDate">
                  <Form.Label className="small-text text-muted mb-0">
                    Alla data
                  </Form.Label>
                  <Form.Control
                    type="date"
                    value={searchEndDate}
                    onChange={(e) => setSearchEndDate(e.target.value)}
                  />
                </Form.Group>
                <Button
                  type="button"
                  className="btn-custom1 ms-2 mt-2 small-text p-1"
                  onClick={handleSearch}
                >
                  Cerca
                </Button>
              </Form>
              {/*TABELLA CHE COMPARE AL CLICK */}
              {showSearchResults && (
                <>
                  <div className="d-flex justify-content-between">
                    <h4 className="text-primary fw-semibold mb-4 small-title">
                      Risultati della ricerca
                    </h4>
                    <Button
                      className="bg-transparent border-0"
                      onClick={() => {
                        setShowSearchResults(false)
                        resetForm()
                      }}
                    >
                      <IoMdClose
                        className="text-secondary bg-transparent"
                        size={30}
                      />
                    </Button>
                  </div>
                  <Table
                    bordered
                    hover
                    responsive
                    className="text-center align-middle"
                  >
                    <thead>
                      <tr>
                        {searchDates.map((date) => (
                          <th
                            key={date.toISOString()}
                            className="small-text text-dark"
                          >
                            {getDayName(date)} <br />
                            {date.getDate()}
                          </th>
                        ))}
                      </tr>
                    </thead>

                    <tbody>
                      <tr>
                        {searchDates.map((date) => {
                          const formattedDate = formatDate(date)

                          const assignment = searchResults.find(
                            (assignment) =>
                              assignment.shiftDate === formattedDate,
                          )

                          return (
                            <td
                              key={formattedDate}
                              {...(assignment
                                ? { className: getAssignmentClass(assignment) }
                                : { className: "bg-light" })}
                            >
                              {assignment ? (
                                assignment.assignmentType === "WORK" ? (
                                  <>
                                    <strong className="small-text text-nowrap text-dark">
                                      {assignment.officeName}
                                    </strong>

                                    <p className="text-nowrap mb-0 text-dark">
                                      {assignment.startTime} -{" "}
                                      {assignment.endTime}
                                    </p>
                                  </>
                                ) : (
                                  getAssignmentLabel(assignment)
                                )
                              ) : (
                                "-"
                              )}
                            </td>
                          )
                        })}
                      </tr>
                    </tbody>
                  </Table>
                </>
              )}
            </Card>
          </Col>
        </Row>

        {/*TABELLA TURNI SETTIMANA CORRENTE */}
        <Row className="w-100 mb-3 justify-content-center">
          <Col xs={12} md={11} className="ps-0">
            {loading ? (
              <div className="d-flex justify-content-center py-4">
                <Spinner animation="border" />
              </div>
            ) : (
              <Card className="p-3 border border-1 border-secondary">
                <div className="d-flex align-items-center justify-content-center mb-3">
                  <Button
                    type="button"
                    className="btn-custom1 rounded-circle d-flex align-items-center justify-content-center small-text me-1"
                    style={{ width: "15px", height: "15px", padding: "0" }}
                    onClick={goToPreviousWeek}
                  >
                    <IoIosArrowBack />
                  </Button>

                  <h6 className="mb-1 text-muted small-text">
                    {weekDates[0].toLocaleDateString("it-IT", {
                      day: "2-digit",
                      month: "long",
                    })}{" "}
                    -{" "}
                    {weekDates[6].toLocaleDateString("it-IT", {
                      day: "2-digit",
                      month: "long",
                    })}
                  </h6>

                  <Button
                    type="button"
                    className="btn-custom1 rounded-circle d-flex align-items-center justify-content-center small-text ms-1"
                    style={{ width: "15px", height: "15px", padding: "0" }}
                    onClick={goToNextWeek}
                  >
                    <IoIosArrowForward />
                  </Button>
                </div>
                <Table
                  bordered
                  hover
                  responsive
                  className="text-center align-middle"
                >
                  <thead>
                    <tr>
                      {weekDates.map((date) => (
                        <th
                          key={date.toISOString()}
                          className="small-text text-dark"
                        >
                          {getDayName(date)} <br></br> {date.getDate()}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    <tr>
                      {weekDates.map((date) => {
                        const formattedDate = formatDate(date)

                        const assignment = assignments.find(
                          (assignment) =>
                            assignment.shiftDate === formattedDate,
                        )

                        return (
                          <td
                            key={formattedDate}
                            {...(assignment
                              ? { className: getAssignmentClass(assignment) }
                              : { className: "bg-light" })}
                          >
                            {assignment ? (
                              assignment.assignmentType === "WORK" ? (
                                <>
                                  <strong className="small-text text-nowrap text-dark">
                                    {assignment.officeName}
                                  </strong>
                                  <p className="text-nowrap mb-0 text-dark">
                                    {assignment.startTime} -{" "}
                                    {assignment.endTime}
                                  </p>
                                </>
                              ) : (
                                getAssignmentLabel(assignment)
                              )
                            ) : (
                              "-"
                            )}
                          </td>
                        )
                      })}
                    </tr>
                  </tbody>
                </Table>
              </Card>
            )}
          </Col>
        </Row>
        {/*TABELLA TURNI MENSILE*/}

        <Row className="w-100 mb-3 justify-content-center">
          <Col xs={12} md={11} className="ps-0">
            {loading ? (
              <div className="d-flex justify-content-center py-4">
                <Spinner animation="border" />
              </div>
            ) : (
              <Card className="p-3 border border-1 border-secondary">
                <div className="d-flex align-items-center justify-content-center mb-3">
                  <Button
                    type="button"
                    className="btn-custom1 rounded-circle d-flex align-items-center justify-content-center small-text me-1"
                    style={{ width: "15px", height: "15px", padding: "0" }}
                    onClick={goToPreviousMonth}
                  >
                    <IoIosArrowBack />
                  </Button>

                  <h5 className=" mb-0 text-muted small-text">
                    {currentMonth
                      .toLocaleDateString("it-IT", {
                        month: "long",
                        year: "numeric",
                      })
                      .toUpperCase()}
                  </h5>

                  <Button
                    type="button"
                    className="btn-custom1 rounded-circle d-flex align-items-center justify-content-center small-text ms-1"
                    style={{ width: "15px", height: "15px", padding: "0" }}
                    onClick={goToNextMonth}
                  >
                    <IoIosArrowForward />
                  </Button>
                </div>
                <Table
                  bordered
                  hover
                  responsive
                  className="text-center align-middle"
                >
                  <thead>
                    <tr>
                      {weekDates.map((date) => (
                        <th
                          key={date.toISOString()}
                          className="small-text text-dark"
                        >
                          {getDayName(date)} <br></br>
                          {date.getDate()}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    <tr>
                      {weekDates.map((date) => {
                        const formattedDate = formatDate(date)

                        const assignment = assignments.find(
                          (assignment) =>
                            assignment.shiftDate === formattedDate,
                        )

                        return (
                          <td
                            key={formattedDate}
                            {...(assignment
                              ? { className: getAssignmentClass(assignment) }
                              : { className: "bg-light" })}
                          >
                            {assignment ? (
                              assignment.assignmentType === "WORK" ? (
                                <>
                                  <strong className="small-text text-nowrap text-dark">
                                    {assignment.officeName}
                                  </strong>
                                  <p className="text-nowrap mb-0 text-dark">
                                    {assignment.startTime} -{" "}
                                    {assignment.endTime}
                                  </p>
                                </>
                              ) : (
                                getAssignmentLabel(assignment)
                              )
                            ) : (
                              "-"
                            )}
                          </td>
                        )
                      })}
                    </tr>
                  </tbody>
                </Table>
              </Card>
            )}
          </Col>
        </Row>
      </Container>
    </>
  )
}
