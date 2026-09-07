import {
  Col,
  Container,
  Row,
  Form,
  Spinner,
  Card,
  Table,
  Button,
  Modal,
} from "react-bootstrap"
import { HrNavbar } from "./HrNavbar"
import { FcSearch } from "react-icons/fc"
import { FaEye } from "react-icons/fa"
import { useEffect, useState } from "react"
import { getAllUsers } from "../../services/adminService"
import type { AccountStatus, UserProfileResponseDTO } from "../../types/users"

export const HrUsers = () => {
  const [searchName, setSearchName] = useState("")
  const [loading, setLoading] = useState(false)
  const [users, setUsers] = useState<UserProfileResponseDTO[]>([])

  const [selectedUser, setSelectedUser] =
    useState<UserProfileResponseDTO | null>(null)

  const [showUserDetailsModal, setShowUserDetailsModal] = useState(false)

  //GET TUTTI GLI UTENTI
  const fetchUsers = async () => {
    try {
      setLoading(true)

      const response = await getAllUsers(0, 15)

      setUsers(response.content)
    } catch (error) {
      console.error("Errore nel recupero degli utenti:", error)
      alert(
        error instanceof Error
          ? error.message
          : "Errore nel recupero degli utenti.",
      )
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchUsers()
  }, [])

  //FUNZIONE APERTURA MODALE DETTAGLI
  const handleOpenUserDetails = (user: UserProfileResponseDTO) => {
    setSelectedUser(user)
    setShowUserDetailsModal(true)
  }

  //funzione per tradurre lo stato
  const translateStatus = (status: AccountStatus) => {
    switch (status) {
      case "ACTIVE":
        return "ATTIVO"
      case "DISABLED":
        return "DISABILITATO"
      case "PENDING":
        return "IN ATTESA"
      case "REJECTED":
        return "RIFIUTATO"
      default:
        return "NESSUNO STATO"
    }
  }

  //funzione per colorare lo stato
  const colorStatus = (status: AccountStatus) => {
    switch (status) {
      case "ACTIVE":
        return "text-success"
      case "PENDING":
        return "text-warning"
      case "DISABLED":
        return "text-danger"
      case "REJECTED":
        return "text-danger"
      default:
        return "text-dark"
    }
  }

  //funzione per tradurre i ruoli
  const translateRole = (role: string) => {
    switch (role) {
      case "ADMIN":
        return "Amministratore"

      case "HR":
        return "Risorse Umane"

      case "SHIFT MANAGER":
        return "Responsabile Turni"

      case "STAFF":
        return "Staff"

      case "AP E PAYROLL SPECIALIST":
        return "Specialista Paghe"

      case "MANAGER":
        return "Manager"

      case "COORDINATOR":
        return "Coordinatore"

      default:
        return role
    }
  }

  const filteredUsers = users.filter((user) => {
    const fullName = `${user.name} ${user.surname}`.toLowerCase()

    return fullName.includes(searchName.toLowerCase())
  })

  return (
    <>
      <HrNavbar />
      <Container
        fluid
        className="d-flex flex-column my-4 align-items-center flex-grow-1"
      >
        <Row className="w-100 justify-content-center mb-2">
          <Col xs={12} md={11} className="ps-0">
            <h3 className="small-title text-dark mb-0">
              Informazioni sugli utenti
            </h3>
          </Col>
        </Row>
        {/*SEARCH*/}
        <Row className="w-100 justify-content-center mb-2">
          <Col xs={12} md={11} className="px-0">
            <div className="d-flex justify-content-lg-start my-2">
              <Form>
                <Form.Group className="mb-3" controlId="search">
                  <Form.Label className="text-muted small-text mb-0">
                    Cerca dipendente
                  </Form.Label>
                  <div className="position-relative">
                    <FcSearch className="position-absolute top-50 start-0 translate-middle-y ms-2 text-muted" />

                    <Form.Control
                      type="text"
                      placeholder="Nome o cognome"
                      className="input ps-4 small-text"
                      value={searchName}
                      onChange={(e) => setSearchName(e.target.value)}
                    />
                  </div>
                </Form.Group>
              </Form>
            </div>

            {/*TABELLA */}
            {loading ? (
              <div className="d-flex justify-content-center py-4">
                <Spinner animation="border" variant="primary" />
              </div>
            ) : (
              <>
                <Card className="border border-1 border-secondary shadow-lg">
                  <div className="table-responsive m-2">
                    <Table
                      striped
                      bordered
                      hover
                      className="align-middle text-center"
                    >
                      <thead>
                        <tr>
                          <th className="text-dark small-text text-center">
                            Nome
                          </th>
                          <th className="text-dark small-text text-center">
                            Email
                          </th>
                          <th className="text-dark small-text text-center">
                            Numero di cellulare
                          </th>
                          <th className="text-dark small-text text-center">
                            Indirizzo
                          </th>
                          <th className="text-dark small-text text-center">
                            Ruolo
                          </th>
                          <th className="text-dark small-text text-center">
                            Ufficio assegnato
                          </th>
                          <th className="text-dark small-text text-center">
                            Stato
                          </th>
                        </tr>
                      </thead>
                      <tbody>
                        {filteredUsers.map((user) => (
                          <tr key={user.userId}>
                            <td className="text-dark small-text">
                              <div className="d-flex align-items-center justify-content-center gap-2">
                                <span>
                                  {user.name} {user.surname}
                                </span>

                                <Button
                                  className="p-1  text-secondary rounded-circle border-0 btn-custom2 d-flex align-items-center justify-content-center"
                                  disabled={loading}
                                  onClick={() => handleOpenUserDetails(user)}
                                >
                                  <FaEye size={8} />
                                </Button>
                              </div>
                            </td>
                            <td className="text-dark small-text">
                              {user.email}
                            </td>
                            <td className="text-dark small-text">
                              {user.phoneNumber}
                            </td>
                            <td className="text-dark small-text">
                              <p className="mb-0">
                                {user.streetAddress} {user.houseNumber}
                              </p>

                              <p className="mb-0">
                                {user.zipCode}, {user.city} {user.province}
                              </p>
                            </td>
                            <td className="text-dark small-text">
                              {user.roleNames.length > 0 && (
                                <div className="d-flex flex-column justify-content-center align-items-center w-100">
                                  {user.roleNames
                                    .map((role) => translateRole(role))
                                    .join(", ")}
                                </div>
                              )}
                            </td>
                            <td className="text-dark small-text">
                              <div
                                className="position-relative d-flex justify-content-center align-items-center"
                                style={{ minHeight: "50px" }}
                              >
                                {user.officeName ===
                                "Nessuna sede assegnata" ? (
                                  <div className="d-flex flex-column justify-content-center align-items-center w-100">
                                    <p className="text-muted small-text text-nowrap">
                                      Nessuna sede assegnata
                                    </p>
                                  </div>
                                ) : (
                                  <div className="d-flex flex-column justify-content-center align-items-center w-100">
                                    <p className="text-dark small-text">
                                      {user.officeName}
                                    </p>
                                  </div>
                                )}
                              </div>
                            </td>
                            <td
                              className={`small-text text-center ${colorStatus(user.accountStatus)}`}
                            >
                              <div>
                                <div className="d-flex align-items-center justify-content-center h-100">
                                  {translateStatus(user.accountStatus)}
                                </div>
                              </div>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </Table>
                  </div>
                </Card>
              </>
            )}
          </Col>
        </Row>
        {/* MODALE DETTAGLI UTENTE */}
        <Modal
          show={showUserDetailsModal}
          onHide={() => setShowUserDetailsModal(false)}
          centered
          size="lg"
        >
          <Modal.Header closeButton>
            <Modal.Title className="text-primary fw-bold">
              Dettagli utente
            </Modal.Title>
          </Modal.Header>

          <Modal.Body>
            {selectedUser && (
              <>
                {/* INTESTAZIONE */}
                <div className="d-flex justify-content-between align-items-start mb-4">
                  <div>
                    <h4 className="text-dark fw-bold mb-1">
                      {selectedUser.name} {selectedUser.surname}
                    </h4>

                    <p className="text-muted mb-0">{selectedUser.email}</p>
                  </div>

                  {/* FOTO PROFILO */}
                  {selectedUser.photoUrl ? (
                    <img
                      src={selectedUser.photoUrl}
                      alt={`${selectedUser.name} ${selectedUser.surname}`}
                      className="rounded-circle"
                      style={{
                        width: "80px",
                        height: "80px",
                        objectFit: "cover",
                      }}
                    />
                  ) : (
                    <div
                      className="rounded-circle bg-secondary d-flex align-items-center justify-content-center text-white"
                      style={{
                        width: "80px",
                        height: "80px",
                      }}
                    >
                      ?
                    </div>
                  )}
                </div>

                {/* DATI PERSONALI */}
                <h6 className="text-primary fw-bold mb-3">Dati personali</h6>

                <div className="row g-3 mb-4">
                  <div className="col-md-6">
                    <strong>Nome e cognome</strong>
                    <div>
                      {selectedUser.name} {selectedUser.surname}
                    </div>
                  </div>

                  <div className="col-md-6">
                    <strong>Data di nascita</strong>
                    <div>{selectedUser.dateOfBirth}</div>
                  </div>

                  <div className="col-md-6">
                    <strong>Luogo di nascita</strong>
                    <div>{selectedUser.placeOfBirth}</div>
                  </div>

                  <div className="col-md-6">
                    <strong>Codice fiscale</strong>
                    <div>{selectedUser.taxCode}</div>
                  </div>
                </div>

                <hr />

                {/* CONTATTI */}
                <h6 className="text-primary fw-bold mb-3">Contatti</h6>

                <div className="row g-3 mb-4">
                  <div className="col-md-6">
                    <strong>Email</strong>
                    <div>{selectedUser.email}</div>
                  </div>

                  <div className="col-md-6">
                    <strong>Numero di cellulare</strong>
                    <div>{selectedUser.phoneNumber}</div>
                  </div>

                  <div className="col-md-6">
                    <strong>IBAN</strong>
                    <div>{selectedUser.iban}</div>
                  </div>

                  <div className="col-md-6">
                    <strong>Ufficio assegnato</strong>
                    <div>{selectedUser.officeName}</div>
                  </div>
                </div>

                <hr />

                {/* DOCUMENTO */}
                <h6 className="text-primary fw-bold mb-3">Documento</h6>

                <div className="row g-3 mb-4">
                  <div className="col-md-6">
                    <strong>Tipo di documento</strong>
                    <div>{selectedUser.documentType}</div>
                  </div>

                  <div className="col-md-6">
                    <strong>Numero documento</strong>
                    <div>{selectedUser.documentNumber}</div>
                  </div>

                  <div className="col-md-6">
                    <strong>Data emissione</strong>
                    <div>{selectedUser.issueDate}</div>
                  </div>

                  <div className="col-md-6">
                    <strong>Data scadenza</strong>
                    <div>{selectedUser.expirationDate}</div>
                  </div>
                </div>

                {/* FOTO DOCUMENTO */}
                <div className="row g-3 mb-4">
                  <div className="col-md-6">
                    <strong>Documento fronte</strong>

                    {selectedUser.documentFrontUrl ? (
                      <img
                        src={selectedUser.documentFrontUrl}
                        alt="Documento fronte"
                        className="img-fluid rounded border mt-2"
                      />
                    ) : (
                      <div className="text-muted mt-2">Non disponibile</div>
                    )}
                  </div>

                  <div className="col-md-6">
                    <strong>Documento retro</strong>

                    {selectedUser.documentBackUrl ? (
                      <img
                        src={selectedUser.documentBackUrl}
                        alt="Documento retro"
                        className="img-fluid rounded border mt-2"
                      />
                    ) : (
                      <div className="text-muted mt-2">Non disponibile</div>
                    )}
                  </div>
                </div>

                <hr />

                {/* TESSERA SANITARIA */}
                <h6 className="text-primary fw-bold mb-3">Tessera sanitaria</h6>

                <div className="row g-3">
                  <div className="col-md-6">
                    <strong>Tessera sanitaria fronte</strong>

                    {selectedUser.taxCodeCardFrontUrl ? (
                      <img
                        src={selectedUser.taxCodeCardFrontUrl}
                        alt="Tessera sanitaria fronte"
                        className="img-fluid rounded border mt-2"
                      />
                    ) : (
                      <div className="text-muted mt-2">Non disponibile</div>
                    )}
                  </div>

                  <div className="col-md-6">
                    <strong>Tessera sanitaria retro</strong>

                    {selectedUser.taxCodeCardBackUrl ? (
                      <img
                        src={selectedUser.taxCodeCardBackUrl}
                        alt="Tessera sanitaria retro"
                        className="img-fluid rounded border mt-2"
                      />
                    ) : (
                      <div className="text-muted mt-2">Non disponibile</div>
                    )}
                  </div>
                </div>
              </>
            )}
          </Modal.Body>

          <Modal.Footer>
            <Button
              className="btn-custom2"
              onClick={() => setShowUserDetailsModal(false)}
            >
              Chiudi
            </Button>
          </Modal.Footer>
        </Modal>
      </Container>
    </>
  )
}
